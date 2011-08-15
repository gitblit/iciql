/*
 * Copyright 2004-2011 H2 Group.
 * Copyright 2011 James Moger.
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

import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import com.iciql.Iciql.EnumType;
import com.iciql.bytecode.ClassReader;
import com.iciql.util.JdbcUtils;
import com.iciql.util.StatementLogger;
import com.iciql.util.Utils;

/**
 * This class represents a query.
 * 
 * @param <T>
 *            the return type
 */

public class Query<T> {

	private Db db;
	private SelectTable<T> from;
	private ArrayList<Token> conditions = Utils.newArrayList();
	private ArrayList<UpdateColumn> updateColumnDeclarations = Utils.newArrayList();
	private ArrayList<SelectTable<T>> joins = Utils.newArrayList();
	private final IdentityHashMap<Object, SelectColumn<T>> aliasMap = Utils.newIdentityHashMap();
	private ArrayList<OrderExpression<T>> orderByList = Utils.newArrayList();
	private ArrayList<Object> groupByExpressions = Utils.newArrayList();
	private long limit;
	private long offset;

	private Query(Db db) {
		this.db = db;
	}

	/**
	 * from() is a static factory method to build a Query object.
	 * 
	 * @param db
	 * @param alias
	 * @return a query object
	 */
	@SuppressWarnings("unchecked")
	static <T> Query<T> from(Db db, T alias) {
		Query<T> query = new Query<T>(db);
		TableDefinition<T> def = (TableDefinition<T>) db.define(alias.getClass());
		query.from = new SelectTable<T>(db, query, alias, false);
		def.initSelectObject(query.from, alias, query.aliasMap);
		return query;
	}

	public long selectCount() {
		SQLStatement stat = getSelectStatement(false);
		stat.appendSQL("COUNT(*) ");
		appendFromWhere(stat);
		ResultSet rs = stat.executeQuery();
		try {
			rs.next();
			long value = rs.getLong(1);
			return value;
		} catch (SQLException e) {
			throw IciqlException.fromSQL(stat.getSQL(), e);
		} finally {
			JdbcUtils.closeSilently(rs, true);
		}
	}

	public List<T> select() {
		return select(false);
	}

	public T selectFirst() {
		return select(false).get(0);
	}

	public List<T> selectDistinct() {
		return select(true);
	}

	@SuppressWarnings("unchecked")
	public <X, Z> X selectFirst(Z x) {
		List<X> list = (List<X>) select(x);
		return list.isEmpty() ? null : list.get(0);
	}

	public String getSQL() {
		SQLStatement stat = getSelectStatement(false);
		stat.appendSQL("*");
		appendFromWhere(stat);
		return stat.getSQL().trim();
	}

	private List<T> select(boolean distinct) {
		List<T> result = Utils.newArrayList();
		TableDefinition<T> def = from.getAliasDefinition();
		SQLStatement stat = getSelectStatement(distinct);
		def.appendSelectList(stat);
		appendFromWhere(stat);
		ResultSet rs = stat.executeQuery();
		try {
			while (rs.next()) {
				T item = from.newObject();
				from.getAliasDefinition().readRow(item, rs);
				result.add(item);
			}
		} catch (SQLException e) {
			throw IciqlException.fromSQL(stat.getSQL(), e);
		} finally {
			JdbcUtils.closeSilently(rs, true);
		}
		return result;
	}

	public int delete() {
		SQLStatement stat = new SQLStatement(db);
		stat.appendSQL("DELETE FROM ");
		from.appendSQL(stat);
		appendWhere(stat);
		StatementLogger.delete(stat.getSQL());
		return stat.executeUpdate();
	}

	public <A> UpdateColumnSet<T, A> set(A field) {
		return new UpdateColumnSet<T, A>(this, field);
	}

	public UpdateColumnSet<T, Boolean> set(boolean field) {
		return setPrimitive(field);
	}

	public UpdateColumnSet<T, Byte> set(byte field) {
		return setPrimitive(field);
	}

	public UpdateColumnSet<T, Short> set(short field) {
		return setPrimitive(field);
	}

	public UpdateColumnSet<T, Integer> set(int field) {
		return setPrimitive(field);
	}

	public UpdateColumnSet<T, Long> set(long field) {
		return setPrimitive(field);
	}

	public UpdateColumnSet<T, Float> set(float field) {
		return setPrimitive(field);
	}

	public UpdateColumnSet<T, Double> set(double field) {
		return setPrimitive(field);
	}

	private <A> UpdateColumnSet<T, A> setPrimitive(A field) {
		A alias = getPrimitiveAliasByValue(field);
		if (alias == null) {
			// this will result in an unmapped field exception
			return set(field);
		}
		return set(alias);
	}

	public <A> UpdateColumnIncrement<T, A> increment(A field) {
		return new UpdateColumnIncrement<T, A>(this, field);
	}

	public UpdateColumnIncrement<T, Byte> increment(byte field) {
		return incrementPrimitive(field);
	}

	public UpdateColumnIncrement<T, Short> increment(short field) {
		return incrementPrimitive(field);
	}

	public UpdateColumnIncrement<T, Integer> increment(int field) {
		return incrementPrimitive(field);
	}

	public UpdateColumnIncrement<T, Long> increment(long field) {
		return incrementPrimitive(field);
	}

	public UpdateColumnIncrement<T, Float> increment(float field) {
		return incrementPrimitive(field);
	}

	public UpdateColumnIncrement<T, Double> increment(double field) {
		return incrementPrimitive(field);
	}

	private <A> UpdateColumnIncrement<T, A> incrementPrimitive(A field) {
		A alias = getPrimitiveAliasByValue(field);
		if (alias == null) {
			// this will result in an unmapped field exception
			return increment(field);
		}
		return increment(alias);
	}

	public int update() {
		if (updateColumnDeclarations.size() == 0) {
			throw new IciqlException("Missing set or increment call.");
		}
		SQLStatement stat = new SQLStatement(db);
		stat.appendSQL("UPDATE ");
		from.appendSQL(stat);
		stat.appendSQL(" SET ");
		int i = 0;
		for (UpdateColumn declaration : updateColumnDeclarations) {
			if (i++ > 0) {
				stat.appendSQL(", ");
			}
			declaration.appendSQL(stat);
		}
		appendWhere(stat);
		StatementLogger.update(stat.getSQL());
		return stat.executeUpdate();
	}

	public <X, Z> List<X> selectDistinct(Z x) {
		return select(x, true);
	}

	public <X, Z> List<X> select(Z x) {
		return select(x, false);
	}

	@SuppressWarnings("unchecked")
	private <X, Z> List<X> select(Z x, boolean distinct) {
		Class<?> clazz = x.getClass();
		if (Utils.isSimpleType(clazz)) {
			return selectSimple((X) x, distinct);
		}
		clazz = clazz.getSuperclass();
		return select((Class<X>) clazz, (X) x, distinct);
	}

	private <X> List<X> select(Class<X> clazz, X x, boolean distinct) {
		List<X> result = Utils.newArrayList();
		TableDefinition<X> def = db.define(clazz);
		SQLStatement stat = getSelectStatement(distinct);
		def.appendSelectList(stat, this, x);
		appendFromWhere(stat);
		ResultSet rs = stat.executeQuery();
		try {
			while (rs.next()) {
				X row = Utils.newObject(clazz);
				def.readRow(row, rs);
				result.add(row);
			}
		} catch (SQLException e) {
			throw IciqlException.fromSQL(stat.getSQL(), e);
		} finally {
			JdbcUtils.closeSilently(rs, true);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <X> List<X> selectSimple(X x, boolean distinct) {
		SQLStatement stat = getSelectStatement(distinct);
		appendSQL(stat, null, x);
		appendFromWhere(stat);
		ResultSet rs = stat.executeQuery();
		List<X> result = Utils.newArrayList();
		try {
			while (rs.next()) {
				X value;
				Object o = rs.getObject(1);
				// Convert CLOB and BLOB now because we close the resultset
				if (Clob.class.isAssignableFrom(o.getClass())) {
					value = (X) Utils.convert(o, String.class);
				} else if (Blob.class.isAssignableFrom(o.getClass())) {
					value = (X) Utils.convert(o, byte[].class);
				} else {
					value = (X) o;
				}
				result.add(value);
			}
		} catch (Exception e) {
			throw IciqlException.fromSQL(stat.getSQL(), e);
		} finally {
			JdbcUtils.closeSilently(rs, true);
		}
		return result;
	}

	private SQLStatement getSelectStatement(boolean distinct) {
		SQLStatement stat = new SQLStatement(db);
		stat.appendSQL("SELECT ");
		if (distinct) {
			stat.appendSQL("DISTINCT ");
		}
		return stat;
	}

	/**
	 * Begin a primitive boolean field condition clause.
	 * 
	 * @param x
	 *            the primitive boolean field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Boolean> where(boolean x) {
		return wherePrimitive(x);
	}

	/**
	 * Begin a primitive short field condition clause.
	 * 
	 * @param x
	 *            the primitive short field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Byte> where(byte x) {
		return wherePrimitive(x);
	}

	/**
	 * Begin a primitive short field condition clause.
	 * 
	 * @param x
	 *            the primitive short field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Short> where(short x) {
		return wherePrimitive(x);
	}

	/**
	 * Begin a primitive int field condition clause.
	 * 
	 * @param x
	 *            the primitive int field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Integer> where(int x) {
		return wherePrimitive(x);
	}

	/**
	 * Begin a primitive long field condition clause.
	 * 
	 * @param x
	 *            the primitive long field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Long> where(long x) {
		return wherePrimitive(x);
	}

	/**
	 * Begin a primitive float field condition clause.
	 * 
	 * @param x
	 *            the primitive float field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Float> where(float x) {
		return wherePrimitive(x);
	}

	/**
	 * Begin a primitive double field condition clause.
	 * 
	 * @param x
	 *            the primitive double field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Double> where(double x) {
		return wherePrimitive(x);
	}

	/**
	 * Begins a primitive field condition clause.
	 * 
	 * @param value
	 * @return a query condition to continue building the condition
	 */
	private <A> QueryCondition<T, A> wherePrimitive(A value) {
		A alias = getPrimitiveAliasByValue(value);
		if (alias == null) {
			// this will result in an unmapped field exception
			return where(value);
		}
		return where(alias);
	}

	/**
	 * Begin an Object field condition clause.
	 * 
	 * @param x
	 *            the mapped object to query
	 * @return a query condition to continue building the condition
	 */
	public <A> QueryCondition<T, A> where(A x) {
		return new QueryCondition<T, A>(this, x);
	}

	public <A> QueryWhere<T> where(Filter filter) {
		HashMap<String, Object> fieldMap = Utils.newHashMap();
		for (Field f : filter.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				Object obj = f.get(filter);
				if (obj == from.getAlias()) {
					List<TableDefinition.FieldDefinition> fields = from.getAliasDefinition().getFields();
					String name = f.getName();
					for (TableDefinition.FieldDefinition field : fields) {
						String n = name + "." + field.field.getName();
						Object o = field.field.get(obj);
						fieldMap.put(n, o);
					}
				}
				fieldMap.put(f.getName(), f.get(filter));
			} catch (Exception e) {
				throw new IciqlException(e);
			}
		}
		Token filterCode = new ClassReader().decompile(filter, fieldMap, "where");
		// String filterQuery = filterCode.toString();
		conditions.add(filterCode);
		return new QueryWhere<T>(this);
	}

	public QueryWhere<T> where(String fragment, Object... args) {
		conditions.add(new RuntimeToken(fragment, args));
		return new QueryWhere<T>(this);
	}

	public QueryWhere<T> whereTrue(Boolean condition) {
		Token token = new Function("", condition);
		addConditionToken(token);
		return new QueryWhere<T>(this);
	}

	/**
	 * Sets the Limit and Offset of a query.
	 * 
	 * @return the query
	 */

	public Query<T> limit(long limit) {
		this.limit = limit;
		return this;
	}

	public Query<T> offset(long offset) {
		this.offset = offset;
		return this;
	}

	public Query<T> orderBy(boolean field) {
		return orderByPrimitive(field);
	}

	public Query<T> orderBy(byte field) {
		return orderByPrimitive(field);
	}

	public Query<T> orderBy(short field) {
		return orderByPrimitive(field);
	}

	public Query<T> orderBy(int field) {
		return orderByPrimitive(field);
	}

	public Query<T> orderBy(long field) {
		return orderByPrimitive(field);
	}

	public Query<T> orderBy(float field) {
		return orderByPrimitive(field);
	}

	public Query<T> orderBy(double field) {
		return orderByPrimitive(field);
	}

	Query<T> orderByPrimitive(Object field) {
		Object alias = getPrimitiveAliasByValue(field);
		if (alias == null) {
			return orderBy(field);
		}
		return orderBy(alias);
	}

	public Query<T> orderBy(Object expr) {
		OrderExpression<T> e = new OrderExpression<T>(this, expr, false, false, false);
		addOrderBy(e);
		return this;
	}

	/**
	 * Order by a number of columns.
	 * 
	 * @param expressions
	 *            the columns
	 * @return the query
	 */

	public Query<T> orderBy(Object... expressions) {
		for (Object expr : expressions) {
			OrderExpression<T> e = new OrderExpression<T>(this, expr, false, false, false);
			addOrderBy(e);
		}
		return this;
	}

	public Query<T> orderByDesc(Object expr) {
		OrderExpression<T> e = new OrderExpression<T>(this, expr, true, false, false);
		addOrderBy(e);
		return this;
	}

	public Query<T> groupBy(boolean field) {
		return orderByPrimitive(field);
	}

	public Query<T> groupBy(byte field) {
		return orderByPrimitive(field);
	}

	public Query<T> groupBy(short field) {
		return orderByPrimitive(field);
	}

	public Query<T> groupBy(int field) {
		return orderByPrimitive(field);
	}

	public Query<T> groupBy(long field) {
		return orderByPrimitive(field);
	}

	public Query<T> groupBy(float field) {
		return orderByPrimitive(field);
	}

	public Query<T> groupBy(double field) {
		return orderByPrimitive(field);
	}

	Query<T> groupByPrimitive(Object field) {
		Object alias = getPrimitiveAliasByValue(field);
		if (alias == null) {
			return groupBy(field);
		}
		return groupBy(alias);
	}

	public Query<T> groupBy(Object expr) {
		groupByExpressions.add(expr);
		return this;
	}

	public Query<T> groupBy(Object... groupBy) {
		this.groupByExpressions.addAll(Arrays.asList(groupBy));
		return this;
	}

	/**
	 * INTERNAL
	 * 
	 * @param stat
	 *            the statement
	 * @param alias
	 *            the alias object (can be null)
	 * @param value
	 *            the value
	 */
	public void appendSQL(SQLStatement stat, Object alias, Object value) {
		if (value == Function.count()) {
			stat.appendSQL("COUNT(*)");
			return;
		}
		Token token = Db.getToken(value);
		if (token != null) {
			token.appendSQL(stat, this);
			return;
		}
		if (alias != null && value.getClass().isEnum()) {
			// special case:
			// value is first enum constant which is also the alias object.
			// the first enum constant is used as the alias because we can not
			// instantiate an enum reflectively.
			stat.appendSQL("?");
			addParameter(stat, alias, value);
			return;
		}
		SelectColumn<T> col = getColumnByReference(value);
		if (col != null) {
			col.appendSQL(stat);
			return;
		}
		stat.appendSQL("?");
		addParameter(stat, alias, value);
	}

	/**
	 * INTERNAL
	 * 
	 * @param stat
	 *            the statement
	 * @param alias
	 *            the alias object (can be null)
	 * @param valueLeft
	 *            the value on the left of the compound clause
	 * @param valueRight
	 *            the value on the right of the compound clause
	 * @param compareType
	 *            the current compare type (e.g. BETWEEN)
	 */
	public void appendSQL(SQLStatement stat, Object alias, Object valueLeft, Object valueRight,
			CompareType compareType) {
		stat.appendSQL("?");
		stat.appendSQL(" ");
		switch (compareType) {
		case BETWEEN:
			stat.appendSQL("AND");
			break;
		}
		stat.appendSQL(" ");
		stat.appendSQL("?");
		addParameter(stat, alias, valueLeft);
		addParameter(stat, alias, valueRight);
	}

	private void addParameter(SQLStatement stat, Object alias, Object value) {
		if (alias != null && value.getClass().isEnum()) {
			SelectColumn<T> col = getColumnByReference(alias);
			EnumType type = col.getFieldDefinition().enumType;
			Enum<?> anEnum = (Enum<?>) value;
			Object y = Utils.convertEnum(anEnum, type);
			stat.addParameter(y);
		} else {
			stat.addParameter(value);
		}
	}

	void addConditionToken(Token condition) {
		conditions.add(condition);
	}

	void addUpdateColumnDeclaration(UpdateColumn declaration) {
		updateColumnDeclarations.add(declaration);
	}

	void appendWhere(SQLStatement stat) {
		if (!conditions.isEmpty()) {
			stat.appendSQL(" WHERE ");
			for (Token token : conditions) {
				token.appendSQL(stat, this);
				stat.appendSQL(" ");
			}
		}
	}

	void appendFromWhere(SQLStatement stat) {
		stat.appendSQL(" FROM ");
		from.appendSQL(stat);
		for (SelectTable<T> join : joins) {
			join.appendSQLAsJoin(stat, this);
		}
		appendWhere(stat);
		if (!groupByExpressions.isEmpty()) {
			stat.appendSQL(" GROUP BY ");
			int i = 0;
			for (Object obj : groupByExpressions) {
				if (i++ > 0) {
					stat.appendSQL(", ");
				}
				appendSQL(stat, null, obj);
				stat.appendSQL(" ");
			}
		}
		if (!orderByList.isEmpty()) {
			stat.appendSQL(" ORDER BY ");
			int i = 0;
			for (OrderExpression<T> o : orderByList) {
				if (i++ > 0) {
					stat.appendSQL(", ");
				}
				o.appendSQL(stat);
				stat.appendSQL(" ");
			}
		}
		db.getDialect().appendLimitOffset(stat, limit, offset);
		StatementLogger.select(stat.getSQL());
	}

	/**
	 * Join another table.
	 * 
	 * @param alias
	 *            an alias for the table to join
	 * @return the joined query
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <U> QueryJoin innerJoin(U alias) {
		TableDefinition<T> def = (TableDefinition<T>) db.define(alias.getClass());
		SelectTable<T> join = new SelectTable(db, this, alias, false);
		def.initSelectObject(join, alias, aliasMap);
		joins.add(join);
		return new QueryJoin(this, join);
	}

	Db getDb() {
		return db;
	}

	boolean isJoin() {
		return !joins.isEmpty();
	}

	/**
	 * This method returns a mapped Object field by its reference.
	 * 
	 * @param obj
	 * @return
	 */
	private SelectColumn<T> getColumnByReference(Object obj) {
		SelectColumn<T> col = aliasMap.get(obj);
		return col;
	}

	/**
	 * This method returns the alias of a mapped primitive field by its value.
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	<A> A getPrimitiveAliasByValue(A obj) {
		for (Object alias : aliasMap.keySet()) {
			if (alias.equals(obj)) {
				SelectColumn<T> match = aliasMap.get(alias);
				if (match.getFieldDefinition().isPrimitive) {
					return (A) alias;
				}
			}
		}
		return null;
	}

	void addOrderBy(OrderExpression<T> expr) {
		orderByList.add(expr);
	}

}
