/*
 * Copyright 2014 James Moger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iciql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.iciql.Iciql.DataTypeAdapter;
import com.iciql.util.JdbcUtils;
import com.iciql.util.StringUtils;
import com.iciql.util.Utils;

/**
 * DaoProxy creates a dynamic instance of the provided Dao interface.
 *
 * @author James Moger
 *
 * @param <X>
 */
final class DaoProxy<X extends Dao> implements InvocationHandler, Dao {

	private final Db db;

	private final Class<X> daoInterface;

	private final char bindingDelimiter = ':';

	private final Map<Method, IndexedSql> indexedSqlCache;

	DaoProxy(Db db, Class<X> daoInterface) {
		this.db = db;
		this.daoInterface = daoInterface;
		this.indexedSqlCache = new ConcurrentHashMap<Method, IndexedSql>();
	}

	/**
	 * Builds a proxy object for the DAO interface.
	 *
	 * @return a proxy object
	 */
	@SuppressWarnings("unchecked")
	X build() {

		if (!daoInterface.isInterface()) {
			throw new IciqlException("Dao {0} must be an interface!", daoInterface.getName());
		}

		ClassLoader classLoader = daoInterface.getClassLoader();

		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		interfaces.add(Dao.class);
		interfaces.add(daoInterface);
		for (Class<?> clazz : daoInterface.getInterfaces()) {
			interfaces.add(clazz);
		}

		Class<?>[] constructorParams = { InvocationHandler.class };
		Class<?>[] allInterfaces = interfaces.toArray(new Class<?>[interfaces.size()]);

		try {

			Class<?> proxyClass = Proxy.getProxyClass(classLoader, allInterfaces);
			Constructor<?> proxyConstructor = proxyClass.getConstructor(constructorParams);
			return (X) proxyConstructor.newInstance(new Object[] { this });

		} catch (Exception e) {
			throw new IciqlException(e);
		}
	}

	/**
	 * Invoke intercepts method calls and delegates execution to the appropriate object.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {

			if (method.getDeclaringClass() == Dao.class) {

				return method.invoke(this, args);

			} else if (method.isAnnotationPresent(SqlQuery.class)) {

				String sql = method.getAnnotation(SqlQuery.class).value();
				String statement = db.getDaoStatementProvider().getStatement(sql, db.getMode());
				return executeQuery(method, args, statement);

			} else if (method.isAnnotationPresent(SqlStatement.class)) {

				String sql = method.getAnnotation(SqlStatement.class).value();
				String statement = db.getDaoStatementProvider().getStatement(sql, db.getMode());
				return executeStatement(method, args, statement);

			} else {

				throw new IciqlException("Can not invoke non-dao method {0}.{1}",
						method.getDeclaringClass().getSimpleName(), method.getName());

			}

		} catch (InvocationTargetException te) {
			throw te.getCause();
		}
	}

	/**
	 * Execute a query.
	 *
	 * @param method
	 * @param methodArgs
	 * @param sql
	 * @return the result
	 */
	private Object executeQuery(Method method, Object[] methodArgs, String sql) {

		/*
		 * Determine and validate the return type
		 */
		Class<?> returnType = method.getReturnType();

		if (void.class == returnType) {
			throw new IciqlException("You must specify a return type for @{0} {1}.{2}!",
					SqlQuery.class.getSimpleName(), method.getDeclaringClass().getSimpleName(), method.getName());
		}

		if (Collection.class.isAssignableFrom(returnType)) {
			throw new IciqlException("You may not return a collection for an @{0} method, please change the return type of {1}.{2} to YourClass[]!",
					SqlQuery.class.getSimpleName(), method.getDeclaringClass().getSimpleName(), method.getName());
		}

		boolean isArray = false;
		if (returnType.isArray()) {
			isArray = true;
			returnType = returnType.getComponentType();
		}

		boolean isJavaType = returnType.isEnum()
				|| returnType.isPrimitive()
				|| java.lang.Boolean.class.isAssignableFrom(returnType)
				|| java.lang.Number.class.isAssignableFrom(returnType)
				|| java.lang.String.class.isAssignableFrom(returnType)
				|| java.util.Date.class.isAssignableFrom(returnType)
				|| byte[].class.isAssignableFrom(returnType);

		Class<? extends DataTypeAdapter<?>> adapter = Utils.getDataTypeAdapter(method.getAnnotations());
		if (adapter == null) {
			adapter = Utils.getDataTypeAdapter(returnType.getAnnotations());
		}

		/*
		 * Prepare & execute sql
		 */
		PreparedSql preparedSql = prepareSql(method, methodArgs, sql);

		List<Object> objects;
		if (!isJavaType && adapter == null) {

			// query of an Iciql model
			objects = db.executeQuery(returnType, preparedSql.sql, preparedSql.parameters);

		} else {

			// query of (array of) standard Java type or a DataTypeAdapter type
			objects = Utils.newArrayList();
			ResultSet rs = db.executeQuery(preparedSql.sql, preparedSql.parameters);
			try {

				while (rs.next()) {

					Object value = db.getDialect().deserialize(rs, 1, returnType, adapter);
					objects.add(value);

					if (!isArray) {
						// we are not returning an array so we break
						// the loop and return the first result
						break;
					}
				}

			} catch (SQLException e) {
				throw new IciqlException(e);
			} finally {
				JdbcUtils.closeSilently(rs);
			}

		}

		/*
		 * Return the results
		 */
		if (objects == null || objects.isEmpty()) {

			// no results
			if (isArray) {
				// return an empty array
				return Array.newInstance(returnType, 0);
			}

			// nothing to return!
			return null;

		} else if (isArray) {

			// return an array of object results
			Object array = Array.newInstance(returnType, objects.size());
			for (int i = 0; i < objects.size(); i++) {
				Array.set(array, i, objects.get(i));
			}
			return array;

		}

		// return first element
		return objects.get(0);
	}


	/**
	 * Execute a statement.
	 *
	 * @param method
	 * @param methodArgs
	 * @param sql
	 * @return the result
	 */
	private Object executeStatement(Method method, Object[] methodArgs, String sql) {

		/*
		 * Determine and validate the return type
		 */
		Class<?> returnType = method.getReturnType();

		if (void.class != returnType && boolean.class != returnType && int.class != returnType) {

			throw new IciqlException("Invalid return type '{0}' for @{1} {2}.{3}!",
					returnType.getSimpleName(), SqlQuery.class.getSimpleName(),
					method.getDeclaringClass().getSimpleName(), method.getName());
		}

		/*
		 * Prepare & execute sql
		 */
		PreparedSql preparedSql = prepareSql(method, methodArgs, sql);
		int rows = db.executeUpdate(preparedSql.sql, preparedSql.parameters);

		/*
		 * Return the results
		 */
		if (void.class == returnType) {

			// return nothing
			return null;

		} else if (boolean.class == returnType) {

			// return true if any rows were affected
			return rows > 0;

		} else {

			// return number of rows
			return rows;

		}
	}

	/**
	 * Prepares an sql statement and execution parameters based on the supplied
	 * method and it's arguments.
	 *
	 * @param method
	 * @param methodArgs
	 * @param sql
	 * @return a prepared sql statement and arguments
	 */
	private PreparedSql prepareSql(Method method, Object[] methodArgs, String sql) {

		if (methodArgs == null || methodArgs.length == 0) {
			// no method arguments
			return new PreparedSql(sql, null);
		}

		IndexedSql indexedSql = indexedSqlCache.get(method);

		if (indexedSql == null) {

			// index the sql and method args
			indexedSql = indexSql(method, sql);

			// cache the indexed sql for re-use
			indexedSqlCache.put(method, indexedSql);
		}

		final PreparedSql preparedSql = indexedSql.prepareSql(db, methodArgs);
		return preparedSql;
	}

	/**
	 * Indexes an sql statement and method args based on the supplied
	 * method and it's arguments.
	 *
	 * @param method
	 * @param sql
	 * @return an indexed sql statement and arguments
	 */
	private IndexedSql indexSql(Method method, String sql) {

		Map<String, IndexedArgument> parameterIndex = buildParameterIndex(method);

		// build a regex to extract parameter names from the sql statement
		StringBuilder sb = new StringBuilder();
		sb.append(bindingDelimiter);
		sb.append("{1}(\\?");
		for (String name : parameterIndex.keySet()) {
			sb.append("|");
			// strip binding delimeter from name
			sb.append(name);
		}
		sb.append(')');

		// identify parameters, replace with the '?' PreparedStatement
		// delimiter and build the PreparedStatement parameters array
		final String regex = sb.toString();
		final Pattern p = Pattern.compile(regex);
		final Matcher m = p.matcher(sql);
		final StringBuffer buffer = new StringBuffer();

		List<IndexedArgument> indexedArgs = Utils.newArrayList();
		int count = 0;
		while (m.find()) {
			String binding = m.group(1);
			m.appendReplacement(buffer, "?");

			IndexedArgument indexedArg;
			if ("?".equals(binding)) {
				// standard ? JDBC placeholder
				indexedArg = parameterIndex.get("arg" + count);
			} else {
				// named placeholder
				indexedArg = parameterIndex.get(binding);
			}

			if (indexedArg == null) {
				throw new IciqlException("Unbound SQL parameter '{0}' in {1}.{2}",
						binding, method.getDeclaringClass().getSimpleName(), method.getName());
			}
			indexedArgs.add(indexedArg);

			count++;
		}
		m.appendTail(buffer);

		final String statement = buffer.toString();

		// create an IndexedSql container for the statement and indexes
		return new IndexedSql(statement, Collections.unmodifiableList(indexedArgs));

	}

	/**
	 * Builds an index of parameter name->(position,typeAdapter) from the method arguments
	 * array. This index is calculated once per method.
	 *
	 * @param method
	 * @return a bindings map of ("name", IndexedArgument) pairs
	 */
	private Map<String, IndexedArgument> buildParameterIndex(Method method) {

		Map<String, IndexedArgument> index = new TreeMap<String, IndexedArgument>();

		Annotation [][] annotationsMatrix = method.getParameterAnnotations();
		for (int i = 0; i < annotationsMatrix.length; i++) {

			Annotation [] annotations = annotationsMatrix[i];

			/*
			 * Conditionally map the bean properties of the method argument
			 * class to Method and Field instances.
			 */
			BindBean bean = getAnnotation(BindBean.class, annotations);
			if (bean != null) {
				final String prefix = bean.value();
				final Class<?> argumentClass = method.getParameterTypes()[i];
				Map<String, IndexedArgument> beanIndex = buildBeanIndex(i, prefix, argumentClass);
				index.putAll(beanIndex);
			}

			Class<? extends DataTypeAdapter<?>> typeAdapter = Utils.getDataTypeAdapter(annotations);
			final IndexedArgument indexedArgument = new IndexedArgument(i, typeAdapter);

			// :N - 1-indexed, like JDBC ResultSet
			index.put("" + (i + 1), indexedArgument);

			// argN - 0-indexed, like Reflection
			index.put("arg" + i, indexedArgument);

			// Bound name
			Bind binding = getAnnotation(Bind.class, annotations);
			if (binding!= null && !binding.value().isEmpty()) {
				index.put(binding.value(), indexedArgument);
			}

			// try mapping Java 8 argument names, may overwrite argN
			try {
				Class<?> nullArgs = null;
				Method getParameters = method.getClass().getMethod("getParameters", nullArgs);
				if (getParameters != null) {
					Object [] parameters = (Object []) getParameters.invoke(method, nullArgs);
					if (parameters != null) {
						Object o = parameters[i];
						Method getName = o.getClass().getMethod("getName", nullArgs);
						String j8name = getName.invoke(o, nullArgs).toString();
						if (!j8name.isEmpty()) {
							index.put(j8name, indexedArgument);
						}
					}
				}
			} catch (Throwable t) {
			}
		}

		return index;
	}

	/**
	 * Builds an index of parameter name->(position,method) from the method arguments
	 * array. This index is calculated once per method.
	 *
	 * @param argumentIndex
	 * @param prefix
	 * @param beanClass
	 * @return a bindings map of ("prefix.property", IndexedArgument) pairs
	 */
	private Map<String, IndexedArgument> buildBeanIndex(int argumentIndex, String prefix, Class<?> beanClass) {

		final String beanPrefix = StringUtils.isNullOrEmpty(prefix) ? "" : (prefix + ".");
		final Map<String, IndexedArgument> index = new TreeMap<String, IndexedArgument>();

		// map JavaBean property getters
		for (Method method : beanClass.getMethods()) {

			if (Modifier.isStatic(method.getModifiers())
				|| method.getReturnType() == void.class
				|| method.getParameterTypes().length > 0
				|| method.getDeclaringClass() == Object.class) {

				// not a JavaBean property
				continue;
			}

			final String propertyName;
			final String name = method.getName();
			if (name.startsWith("get")) {
				propertyName = method.getName().substring(3);
			} else if (name.startsWith("is")) {
				propertyName = method.getName().substring(2);
			} else {
				propertyName = null;
			}

			if (propertyName == null) {
				// not a conventional JavaBean property
				continue;
			}

			final String binding = beanPrefix + preparePropertyName(propertyName);
			final IndexedArgument indexedArg = new IndexedArgument(argumentIndex, method);

			index.put(binding, indexedArg);
		}

		// map public instance fields
		for (Field field : beanClass.getFields()) {

			if (Modifier.isStatic(field.getModifiers())) {
				// not a JavaBean property
				continue;
			}

			final String binding = beanPrefix + preparePropertyName(field.getName());
			final IndexedArgument indexedArg = new IndexedArgument(argumentIndex, field);

			index.put(binding, indexedArg);

		}

		return index;
	}

	@SuppressWarnings("unchecked")
	private <T> T getAnnotation(Class<T> annotationClass, Annotation [] annotations) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.annotationType() == annotationClass) {
					return (T) annotation;
				}
			}
		}
		return null;
	}

	private String preparePropertyName(String value) {
		return Character.toLowerCase(value.charAt(0)) + value.substring(1);
	}

	/*
	 *
	 * Standard Dao method implementations delegate to the underlying Db
	 *
	 */

	@Override
	public final Db db() {
		return db;
	}

	@Override
	public final <T> boolean insert(T t) {
		return db.insert(t);
	}

	@Override
	public final <T> void insertAll(List<T> t) {
		db.insertAll(t);
	}

	@Override
	public final <T> long insertAndGetKey(T t) {
		return db.insertAndGetKey(t);
	}

	@Override
	public final <T> List<Long> insertAllAndGetKeys(List<T> t) {
		return db.insertAllAndGetKeys(t);
	}

	@Override
	public final <T> boolean update(T t) {
		return db.update(t);
	}

	@Override
	public final <T> void updateAll(List<T> t) {
		db.updateAll(t);
	}

	@Override
	public final <T> void merge(T t) {
		db.merge(t);
	}

	@Override
	public final <T> boolean delete(T t) {
		return db.delete(t);
	}

	@Override
	public final <T> void deleteAll(List<T> t) {
		db.deleteAll(t);
	}

	@Override
	public final void close() {
		db.close();
	}

	/**
	 * Container class to hold the prepared JDBC SQL statement and execution
	 * parameters.
	 */
	private class PreparedSql {
		final String sql;
		final Object [] parameters;

		PreparedSql(String sql, Object [] parameters) {
			this.sql = sql;
			this.parameters = parameters;
		}

		@Override
		public String toString() {
			return sql;
		}

	}

	/**
	 * Container class to hold a parsed JDBC SQL statement and
	 * IndexedParameters.
	 * <p>
	 * Instances of this class are cached because they are functional processing
	 * containers as they contain Method and Field references for binding beans
	 * and matching to method arguments.
	 * </p>
	 */
	private class IndexedSql {
		final String sql;
		final List<IndexedArgument> indexedArgs;

		IndexedSql(String sql, List<IndexedArgument> indexedArgs) {
			this.sql = sql;
			this.indexedArgs = indexedArgs;
		}

		/**
		 * Prepares the method arguments for statement execution.
		 *
		 * @param db
		 * @param methodArgs
		 * @return the prepared sql statement and parameters
		 */
		PreparedSql prepareSql(Db db, Object [] methodArgs) {

			Object [] parameters = new Object[indexedArgs.size()];

			for (int i = 0; i < indexedArgs.size(); i++) {

				IndexedArgument indexedArg = indexedArgs.get(i);
				Object methodArg = methodArgs[indexedArg.index];

				Object value = methodArg;
				Class<? extends DataTypeAdapter<?>> typeAdapter = indexedArg.typeAdapter;

				if (indexedArg.method != null) {

					// execute the bean method
					try {

						value = indexedArg.method.invoke(methodArg);
						typeAdapter = Utils.getDataTypeAdapter(indexedArg.method.getAnnotations());

					} catch (Exception e) {
						throw new IciqlException(e);
					}

				} else if (indexedArg.field != null) {

					// extract the field value
					try {

						value = indexedArg.field.get(methodArg);
						typeAdapter = Utils.getDataTypeAdapter(indexedArg.field.getAnnotations());

					} catch (Exception e) {
						throw new IciqlException(e);
					}

				} else if (typeAdapter == null) {

					// identify the type adapter for the argument class
					typeAdapter = Utils.getDataTypeAdapter(methodArg.getClass().getAnnotations());
				}

				// prepare the parameter
				parameters[i] = db.getDialect().serialize(value, typeAdapter);

			}

			return new PreparedSql(sql, parameters);

		}

		@Override
		public String toString() {
			return sql;
		}
	}

	/**
	 * IndexedArgument holds cached information about how to process an method
	 * argument by it's index in the method arguments array.
	 * <p>
	 * An argument may be passed-through, might be bound to a bean property,
	 * might be transformed with a type adapter, or a combination of these.
	 * </p>
	 */
	private class IndexedArgument {
		final int index;
		final Class<? extends DataTypeAdapter<?>> typeAdapter;
		final Method method;
		final Field field;

		IndexedArgument(int index, Class<? extends DataTypeAdapter<?>> typeAdapter) {
			this.index = index;
			this.typeAdapter = typeAdapter;
			this.method = null;
			this.field = null;
		}

		IndexedArgument(int methodArgIndex, Method method) {
			this.index = methodArgIndex;
			this.typeAdapter = null;
			this.method = method;
			this.field = null;
		}

		IndexedArgument(int methodArgIndex, Field field) {
			this.index = methodArgIndex;
			this.typeAdapter = null;
			this.method = null;
			this.field = field;
		}

		@Override
		public String toString() {

			String accessor;
			if (method != null) {
				accessor = "M:" + method.getDeclaringClass().getSimpleName() + "." + method.getName();
			} else if (field != null) {
				accessor = "F:" + field.getDeclaringClass().getSimpleName() + "." + field.getName();
			} else {
				accessor = "A:arg";
			}

			return index + ":" + accessor + (typeAdapter == null ? "" : (":" + typeAdapter.getSimpleName()));
		}

	}

}
