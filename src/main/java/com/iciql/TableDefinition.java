/*
 * Copyright 2004-2011 H2 Group.
 * Copyright 2011 James Moger.
 * Copyright 2012 Frédéric Gaillard.
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.iciql.Iciql.ConstraintDeferrabilityType;
import com.iciql.Iciql.ConstraintDeleteType;
import com.iciql.Iciql.ConstraintUpdateType;
import com.iciql.Iciql.DataTypeAdapter;
import com.iciql.Iciql.EnumId;
import com.iciql.Iciql.EnumType;
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQConstraint;
import com.iciql.Iciql.IQContraintForeignKey;
import com.iciql.Iciql.IQContraintUnique;
import com.iciql.Iciql.IQContraintsForeignKey;
import com.iciql.Iciql.IQContraintsUnique;
import com.iciql.Iciql.IQEnum;
import com.iciql.Iciql.IQIgnore;
import com.iciql.Iciql.IQIndex;
import com.iciql.Iciql.IQIndexes;
import com.iciql.Iciql.IQSchema;
import com.iciql.Iciql.IQTable;
import com.iciql.Iciql.IQVersion;
import com.iciql.Iciql.IQView;
import com.iciql.Iciql.IndexType;
import com.iciql.Iciql.StandardJDBCTypeAdapter;
import com.iciql.util.IciqlLogger;
import com.iciql.util.StatementBuilder;
import com.iciql.util.StringUtils;
import com.iciql.util.Utils;

/**
 * A table definition contains the index definitions of a table, the field
 * definitions, the table name, and other meta data.
 *
 * @param <T>
 *            the table type
 */

public class TableDefinition<T> {

	/**
	 * The meta data of an index.
	 */

	public static class IndexDefinition {
		public IndexType type;
		public String indexName;

		public List<String> columnNames;
	}

	/**
	 * The meta data of a constraint on foreign key.
	 */

	public static class ConstraintForeignKeyDefinition {

		public String constraintName;
		public List<String> foreignColumns;
		public String referenceTable;
		public List<String> referenceColumns;
		public ConstraintDeleteType deleteType = ConstraintDeleteType.UNSET;
		public ConstraintUpdateType updateType = ConstraintUpdateType.UNSET;
		public ConstraintDeferrabilityType deferrabilityType = ConstraintDeferrabilityType.UNSET;
	}

	/**
	 * The meta data of a unique constraint.
	 */

	public static class ConstraintUniqueDefinition {

		public String constraintName;
		public List<String> uniqueColumns;
	}


	/**
	 * The meta data of a field.
	 */

	static class FieldDefinition {
		String columnName;
		Field field;
		String dataType;
		int length;
		int scale;
		boolean isPrimaryKey;
		boolean isAutoIncrement;
		boolean trim;
		boolean nullable;
		String defaultValue;
		EnumType enumType;
		Class<?> enumTypeClass;
		boolean isPrimitive;
		String constraint;
		Class<? extends DataTypeAdapter<?>> typeAdapter;

		Object getValue(Object obj) {
			try {
				return field.get(obj);
			} catch (Exception e) {
				throw new IciqlException(e);
			}
		}

		private Object initWithNewObject(Object obj) {
			Object o = Utils.newObject(field.getType());
			setValue(null, obj, o);
			return o;
		}

		private void setValue(SQLDialect dialect, Object obj, Object o) {
			try {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				Class<?> targetType = field.getType();
				if (targetType.isEnum()) {
					o = Utils.convertEnum(o, targetType, enumType);
				} else if (dialect != null && typeAdapter != null) {
					o = dialect.deserialize(o, typeAdapter);
				} else {
					o = Utils.convert(o, targetType);
				}

				if (targetType.isPrimitive() && o == null) {
					// do not attempt to set a primitive to null
					return;
				}

				field.set(obj, o);
			} catch (IciqlException e) {
				throw e;
			} catch (Exception e) {
				throw new IciqlException(e);
			}
		}

		private Object read(ResultSet rs, int columnIndex) {
			try {
				return rs.getObject(columnIndex);
			} catch (SQLException e) {
				throw new IciqlException(e);
			}
		}

		@Override
		public int hashCode() {
			return columnName.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof FieldDefinition) {
				return o.hashCode() == hashCode();
			}
			return false;
		}
	}

	public ArrayList<FieldDefinition> fields = Utils.newArrayList();
	String schemaName;
	String tableName;
	String viewTableName;
	int tableVersion;
	List<String> primaryKeyColumnNames;
	boolean memoryTable;
	boolean multiplePrimitiveBools;

	private boolean createIfRequired = true;
	private Class<T> clazz;
	private IdentityHashMap<Object, FieldDefinition> fieldMap = Utils.newIdentityHashMap();
	private ArrayList<IndexDefinition> indexes = Utils.newArrayList();
	private ArrayList<ConstraintForeignKeyDefinition> constraintsForeignKey = Utils.newArrayList();
	private ArrayList<ConstraintUniqueDefinition> constraintsUnique = Utils.newArrayList();

	TableDefinition(Class<T> clazz) {
		this.clazz = clazz;
		schemaName = null;
		tableName = clazz.getSimpleName();
	}

	Class<T> getModelClass() {
		return clazz;
	}

	List<FieldDefinition> getFields() {
		return fields;
	}

	void defineSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	void defineTableName(String tableName) {
		this.tableName = tableName;
	}

	void defineViewTableName(String viewTableName) {
		this.viewTableName = viewTableName;
	}

	void defineMemoryTable() {
		this.memoryTable = true;
	}

	void defineSkipCreate() {
		this.createIfRequired = false;
	}

	/**
	 * Define a primary key by the specified model fields.
	 *
	 * @param modelFields
	 *            the ordered list of model fields
	 */
	void definePrimaryKey(Object[] modelFields) {
		List<String> columnNames = mapColumnNames(modelFields);
		setPrimaryKey(columnNames);
	}

	/**
	 * Define a primary key by the specified column names.
	 *
	 * @param columnNames
	 *            the ordered list of column names
	 */
	private void setPrimaryKey(List<String> columnNames) {
		primaryKeyColumnNames = Utils.newArrayList(columnNames);
		List<String> pkNames = Utils.newArrayList();
		for (String name : columnNames) {
			pkNames.add(name.toLowerCase());
		}
		// set isPrimaryKey flag for all field definitions
		for (FieldDefinition fieldDefinition : fieldMap.values()) {
			fieldDefinition.isPrimaryKey = pkNames.contains(fieldDefinition.columnName.toLowerCase());
		}
	}

	private <A> String getColumnName(A fieldObject) {
		FieldDefinition def = fieldMap.get(fieldObject);
		return def == null ? null : def.columnName;
	}

	private ArrayList<String> mapColumnNames(Object[] columns) {
		ArrayList<String> columnNames = Utils.newArrayList();
		for (Object column : columns) {
			columnNames.add(getColumnName(column));
		}
		return columnNames;
	}

	/**
	 * Defines an index with the specified model fields.
	 *
	 * @param name
	 *            the index name (optional)
	 * @param type
	 *            the index type (STANDARD, HASH, UNIQUE, UNIQUE_HASH)
	 * @param modelFields
	 *            the ordered list of model fields
	 */
	void defineIndex(String name, IndexType type, Object[] modelFields) {
		List<String> columnNames = mapColumnNames(modelFields);
		addIndex(name, type, columnNames);
	}

	/**
	 * Defines an index with the specified column names.
	 *
	 * @param type
	 *            the index type (STANDARD, HASH, UNIQUE, UNIQUE_HASH)
	 * @param columnNames
	 *            the ordered list of column names
	 */
	private void addIndex(String name, IndexType type, List<String> columnNames) {
		IndexDefinition index = new IndexDefinition();
		if (StringUtils.isNullOrEmpty(name)) {
			index.indexName = tableName + "_idx_" + indexes.size();
		} else {
			index.indexName = name;
		}
		index.columnNames = Utils.newArrayList(columnNames);
		index.type = type;
		indexes.add(index);
	}

	/**
	 * Defines an unique constraint with the specified model fields.
	 *
	 * @param name
	 *            the constraint name (optional)
	 * @param modelFields
	 *            the ordered list of model fields
	 */
	void defineConstraintUnique(String name, Object[] modelFields) {
		List<String> columnNames = mapColumnNames(modelFields);
		addConstraintUnique(name, columnNames);
	}

	/**
	 * Defines an unique constraint.
	 *
	 * @param name
	 * @param columnNames
	 */
	private void addConstraintUnique(String name, List<String> columnNames) {
		ConstraintUniqueDefinition constraint = new ConstraintUniqueDefinition();
		if (StringUtils.isNullOrEmpty(name)) {
			constraint.constraintName = tableName + "_unique_" + constraintsUnique.size();
		} else {
			constraint.constraintName = name;
		}
		constraint.uniqueColumns = Utils.newArrayList(columnNames);
		constraintsUnique.add(constraint);
	}

	/**
	 * Defines a foreign key constraint with the specified model fields.
	 *
	 * @param name
	 *            the constraint name (optional)
	 * @param modelFields
	 *            the ordered list of model fields
	 */
	void defineForeignKey(String name, Object[] modelFields, String refTableName, Object[] refModelFields,
			ConstraintDeleteType deleteType, ConstraintUpdateType updateType,
			ConstraintDeferrabilityType deferrabilityType) {
		List<String> columnNames = mapColumnNames(modelFields);
		List<String> referenceColumnNames = mapColumnNames(refModelFields);
		addConstraintForeignKey(name, columnNames, refTableName, referenceColumnNames,
				deleteType, updateType, deferrabilityType);
	}

	void defineColumnName(Object column, String columnName) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.columnName = columnName;
		}
	}

	void defineAutoIncrement(Object column) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.isAutoIncrement = true;
		}
	}

	void defineLength(Object column, int length) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.length = length;
		}
	}

	void defineScale(Object column, int scale) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.scale = scale;
		}
	}

	void defineTrim(Object column) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.trim = true;
		}
	}

	void defineNullable(Object column, boolean isNullable) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.nullable = isNullable;
		}
	}

	void defineDefaultValue(Object column, String defaultValue) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.defaultValue = defaultValue;
		}
	}

	void defineConstraint(Object column, String constraint) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.constraint = constraint;
		}
	}

	void defineTypeAdapter(Object column, Class<? extends DataTypeAdapter<?>> typeAdapter) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.typeAdapter = typeAdapter;
		}
	}

	void mapFields(Db db) {
		boolean byAnnotationsOnly = false;
		boolean inheritColumns = false;
		if (clazz.isAnnotationPresent(IQTable.class)) {
			IQTable tableAnnotation = clazz.getAnnotation(IQTable.class);
			byAnnotationsOnly = tableAnnotation.annotationsOnly();
			inheritColumns = tableAnnotation.inheritColumns();
		}

		if (clazz.isAnnotationPresent(IQView.class)) {
			IQView viewAnnotation = clazz.getAnnotation(IQView.class);
			byAnnotationsOnly = viewAnnotation.annotationsOnly();
			inheritColumns = viewAnnotation.inheritColumns();
		}

		List<Field> classFields = Utils.newArrayList();
		classFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		if (inheritColumns) {
			Class<?> superClass = clazz.getSuperclass();
			classFields.addAll(Arrays.asList(superClass.getDeclaredFields()));

			if (superClass.isAnnotationPresent(IQView.class)) {
				IQView superView = superClass.getAnnotation(IQView.class);
				if (superView.inheritColumns()) {
					// inherit columns from super.super.class
					Class<?> superSuperClass = superClass.getSuperclass();
					classFields.addAll(Arrays.asList(superSuperClass.getDeclaredFields()));
				}
			} else if (superClass.isAnnotationPresent(IQTable.class)) {
				IQTable superTable = superClass.getAnnotation(IQTable.class);
				if (superTable.inheritColumns()) {
					// inherit columns from super.super.class
					Class<?> superSuperClass = superClass.getSuperclass();
					classFields.addAll(Arrays.asList(superSuperClass.getDeclaredFields()));
				}
			}
		}

		Set<FieldDefinition> uniqueFields = new LinkedHashSet<FieldDefinition>();
		T defaultObject = Db.instance(clazz);
		for (Field f : classFields) {
			// check if we should skip this field
			if (f.isAnnotationPresent(IQIgnore.class)) {
				continue;
			}

			// default to field name
			String columnName = f.getName();
			boolean isAutoIncrement = false;
			boolean isPrimaryKey = false;
			int length = 0;
			int scale = 0;
			boolean trim = false;
			boolean nullable = !f.getType().isPrimitive();
			EnumType enumType = null;
			Class<?> enumTypeClass = null;
			String defaultValue = "";
			String constraint = "";
			String dataType = null;
			Class<? extends DataTypeAdapter<?>> typeAdapter = null;

			// configure Java -> SQL enum mapping
			if (f.getType().isEnum()) {
				enumType = EnumType.DEFAULT_TYPE;
				if (f.getType().isAnnotationPresent(IQEnum.class)) {
					// enum definition is annotated for all instances
					IQEnum iqenum = f.getType().getAnnotation(IQEnum.class);
					enumType = iqenum.value();
				}
				if (f.isAnnotationPresent(IQEnum.class)) {
					// this instance of the enum is annotated
					IQEnum iqenum = f.getAnnotation(IQEnum.class);
					enumType = iqenum.value();
				}

				if (EnumId.class.isAssignableFrom(f.getType())) {
					// custom enumid mapping
					enumTypeClass = ((EnumId<?>) f.getType().getEnumConstants()[0]).enumIdClass();
				}
			}

			// try using default object
			try {
				f.setAccessible(true);
				Object value = f.get(defaultObject);
				if (value != null) {
					if (value.getClass().isEnum()) {
						// enum default, convert to target type
						Enum<?> anEnum = (Enum<?>) value;
						Object o = Utils.convertEnum(anEnum, enumType);
						defaultValue = ModelUtils.formatDefaultValue(o);
					} else {
						// object default
						defaultValue = ModelUtils.formatDefaultValue(value);
					}
				}
			} catch (IllegalAccessException e) {
				throw new IciqlException(e, "failed to get default object for {0}", columnName);
			}

			boolean hasAnnotation = f.isAnnotationPresent(IQColumn.class);
			if (hasAnnotation) {
				IQColumn col = f.getAnnotation(IQColumn.class);
				if (!StringUtils.isNullOrEmpty(col.name())) {
					columnName = col.name();
				}
				isAutoIncrement = col.autoIncrement();
				isPrimaryKey = col.primaryKey();
				length = col.length();
				scale = col.scale();
				trim = col.trim();
				nullable = col.nullable();

				if (col.typeAdapter() != null && col.typeAdapter() != StandardJDBCTypeAdapter.class) {
					typeAdapter = col.typeAdapter();
					DataTypeAdapter<?> dtt = db.getDialect().getAdapter(col.typeAdapter());
					dataType = dtt.getDataType();
				}

				// annotation overrides
				if (!StringUtils.isNullOrEmpty(col.defaultValue())) {
					defaultValue = col.defaultValue();
				}
			}

			boolean hasConstraint = f.isAnnotationPresent(IQConstraint.class);
			if (hasConstraint) {
				IQConstraint con = f.getAnnotation(IQConstraint.class);
				// annotation overrides
				if (!StringUtils.isNullOrEmpty(con.value())) {
					constraint = con.value();
				}
			}

			boolean reflectiveMatch = !byAnnotationsOnly;
			if (reflectiveMatch || hasAnnotation || hasConstraint) {
				FieldDefinition fieldDef = new FieldDefinition();
				fieldDef.isPrimitive = f.getType().isPrimitive();
				fieldDef.field = f;
				fieldDef.columnName = columnName;
				fieldDef.isAutoIncrement = isAutoIncrement;
				fieldDef.isPrimaryKey = isPrimaryKey;
				fieldDef.length = length;
				fieldDef.scale = scale;
				fieldDef.trim = trim;
				fieldDef.nullable = nullable;
				fieldDef.defaultValue = defaultValue;
				fieldDef.enumType = enumType;
				fieldDef.enumTypeClass = enumTypeClass;
				fieldDef.dataType = StringUtils.isNullOrEmpty(dataType) ? ModelUtils.getDataType(fieldDef) : dataType;
				fieldDef.typeAdapter = typeAdapter;
				fieldDef.constraint = constraint;
				uniqueFields.add(fieldDef);
			}
		}
		fields.addAll(uniqueFields);

		List<String> primaryKey = Utils.newArrayList();
		int primitiveBoolean = 0;
		for (FieldDefinition fieldDef : fields) {
			if (fieldDef.isPrimaryKey) {
				primaryKey.add(fieldDef.columnName);
			}
			if (fieldDef.isPrimitive && fieldDef.field.getType().equals(boolean.class)) {
				primitiveBoolean++;
			}
		}
		if (primitiveBoolean > 1) {
			multiplePrimitiveBools = true;
			IciqlLogger
					.warn("Model {0} has multiple primitive booleans! Possible where,set,join clause problem!");
		}
		if (primaryKey.size() > 0) {
			setPrimaryKey(primaryKey);
		}
	}

	void checkMultipleBooleans() {
		if (multiplePrimitiveBools) {
			throw new IciqlException(
					"Can not explicitly reference a primitive boolean if there are multiple boolean fields in your model class!");
		}
	}

	void checkMultipleEnums(Object o) {
		if (o == null) {
			return;
		}
		Class<?> clazz = o.getClass();
		if (!clazz.isEnum()) {
			return;
		}

		int fieldCount = 0;
		for (FieldDefinition fieldDef : fields) {
			Class<?> targetType = fieldDef.field.getType();
			if (clazz.equals(targetType)) {
				fieldCount++;
			}
		}

		if (fieldCount > 1) {
			throw new IciqlException(
					"Can not explicitly reference {0} because there are {1} {0} fields in your model class!",
					clazz.getSimpleName(), fieldCount);
		}
	}

	/**
	 * Optionally truncates strings to the maximum length and converts
	 * java.lang.Enum types to Strings or Integers.
	 */
	Object getValue(Object obj, FieldDefinition field) {
		Object value = field.getValue(obj);
		if (value == null) {
			return value;
		}
		if (field.enumType != null) {
			// convert enumeration to INT or STRING
			Enum<?> iqenum = (Enum<?>) value;
			switch (field.enumType) {
			case NAME:
				if (field.trim && field.length > 0) {
					if (iqenum.name().length() > field.length) {
						return iqenum.name().substring(0, field.length);
					}
				}
				return iqenum.name();
			case ORDINAL:
				return iqenum.ordinal();
			case ENUMID:
				if (!EnumId.class.isAssignableFrom(value.getClass())) {
					throw new IciqlException(field.field.getName() + " does not implement EnumId!");
				}
				EnumId<?> enumid = (EnumId<?>) value;
				return enumid.enumId();
			}
		}

		if (field.trim && field.length > 0) {
			if (value instanceof String) {
				// clip strings
				String s = (String) value;
				if (s.length() > field.length) {
					return s.substring(0, field.length);
				}
				return s;
			}
			return value;
		}

		// return the value unchanged
		return value;
	}

	PreparedStatement createInsertStatement(Db db, Object obj, boolean returnKey) {
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		buff.append(db.getDialect().prepareTableName(schemaName, tableName)).append('(');
		for (FieldDefinition field : fields) {
			if (skipInsertField(field, obj)) {
				continue;
			}
			buff.appendExceptFirst(", ");
			buff.append(db.getDialect().prepareColumnName(field.columnName));
		}
		buff.append(") VALUES(");
		buff.resetCount();
		for (FieldDefinition field : fields) {
			if (skipInsertField(field, obj)) {
				continue;
			}
			buff.appendExceptFirst(", ");
			buff.append('?');
			Object value = getValue(obj, field);
			if (value == null && !field.nullable) {
				// try to interpret and instantiate a default value
				value = ModelUtils.getDefaultValue(field, db.getDialect().getDateTimeClass());
			}
			Object parameter = db.getDialect().serialize(value, field.typeAdapter);
			stat.addParameter(parameter);
		}
		buff.append(')');
		stat.setSQL(buff.toString());
		IciqlLogger.insert(stat.getSQL());
		return stat.prepare(returnKey);
	}

	long insert(Db db, Object obj, boolean returnKey) {
		if (!StringUtils.isNullOrEmpty(viewTableName)) {
			throw new IciqlException("Iciql does not support inserting rows into views!");
		}
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		buff.append(db.getDialect().prepareTableName(schemaName, tableName)).append('(');
		for (FieldDefinition field : fields) {
			if (skipInsertField(field, obj)) {
				continue;
			}
			buff.appendExceptFirst(", ");
			buff.append(db.getDialect().prepareColumnName(field.columnName));
		}
		buff.append(") VALUES(");
		buff.resetCount();
		for (FieldDefinition field : fields) {
			if (skipInsertField(field, obj)) {
				continue;
			}
			buff.appendExceptFirst(", ");
			buff.append('?');
			Object value = getValue(obj, field);
			if (value == null && !field.nullable) {
				// try to interpret and instantiate a default value
				value = ModelUtils.getDefaultValue(field, db.getDialect().getDateTimeClass());
			}
			Object parameter = db.getDialect().serialize(value, field.typeAdapter);
			stat.addParameter(parameter);
		}
		buff.append(')');
		stat.setSQL(buff.toString());
		IciqlLogger.insert(stat.getSQL());
		if (returnKey) {
			return stat.executeInsert();
		}
		return stat.executeUpdate();
	}

	private boolean skipInsertField(FieldDefinition field, Object obj) {
		if (field.isAutoIncrement) {
			Object value = getValue(obj, field);
			if (field.isPrimitive) {
				// skip uninitialized primitive autoincrement values
				if (value.toString().equals("0")) {
					return true;
				}
			} else if (value == null) {
				// skip null object autoincrement values
				return true;
			}
		} else {
			// conditionally skip insert of null
			Object value = getValue(obj, field);
			if (value == null) {
				if (field.nullable) {
					// skip null assignment, field is nullable
					return true;
				} else if (StringUtils.isNullOrEmpty(field.defaultValue)) {
					IciqlLogger.warn("no default value, skipping null insert assignment for {0}.{1}",
							tableName, field.columnName);
					return true;
				}
			}
		}
		return false;
	}

	int merge(Db db, Object obj) {
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.size() == 0) {
			throw new IllegalStateException("No primary key columns defined for table " + obj.getClass()
					+ " - no update possible");
		}
		SQLStatement stat = new SQLStatement(db);
		db.getDialect().prepareMerge(stat, schemaName, tableName, this, obj);
		IciqlLogger.merge(stat.getSQL());
		return stat.executeUpdate();
	}

	int update(Db db, Object obj) {
		if (!StringUtils.isNullOrEmpty(viewTableName)) {
			throw new IciqlException("Iciql does not support updating rows in views!");
		}
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.size() == 0) {
			throw new IllegalStateException("No primary key columns defined for table " + obj.getClass()
					+ " - no update possible");
		}
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("UPDATE ");
		buff.append(db.getDialect().prepareTableName(schemaName, tableName)).append(" SET ");
		buff.resetCount();

		for (FieldDefinition field : fields) {
			if (!field.isPrimaryKey) {
				Object value = getValue(obj, field);
				if (value == null && !field.nullable) {
					// try to interpret and instantiate a default value
					value = ModelUtils.getDefaultValue(field, db.getDialect().getDateTimeClass());
				}
				buff.appendExceptFirst(", ");
				buff.append(db.getDialect().prepareColumnName(field.columnName));
				buff.append(" = ?");
				Object parameter = db.getDialect().serialize(value, field.typeAdapter);
				stat.addParameter(parameter);
			}
		}
		Object alias = Utils.newObject(obj.getClass());
		Query<Object> query = Query.from(db, alias);
		boolean firstCondition = true;
		for (FieldDefinition field : fields) {
			if (field.isPrimaryKey) {
				Object fieldAlias = field.getValue(alias);
				Object value = field.getValue(obj);
				if (field.isPrimitive) {
					fieldAlias = query.getPrimitiveAliasByValue(fieldAlias);
				}
				if (!firstCondition) {
					query.addConditionToken(ConditionAndOr.AND);
				}
				firstCondition = false;
				query.addConditionToken(new Condition<Object>(fieldAlias, value, CompareType.EQUAL));
			}
		}
		stat.setSQL(buff.toString());
		query.appendWhere(stat);
		IciqlLogger.update(stat.getSQL());
		return stat.executeUpdate();
	}

	int delete(Db db, Object obj) {
		if (!StringUtils.isNullOrEmpty(viewTableName)) {
			throw new IciqlException("Iciql does not support deleting rows from views!");
		}
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.size() == 0) {
			throw new IllegalStateException("No primary key columns defined for table " + obj.getClass()
					+ " - no update possible");
		}
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("DELETE FROM ");
		buff.append(db.getDialect().prepareTableName(schemaName, tableName));
		buff.resetCount();
		Object alias = Utils.newObject(obj.getClass());
		Query<Object> query = Query.from(db, alias);
		boolean firstCondition = true;
		for (FieldDefinition field : fields) {
			if (field.isPrimaryKey) {
				Object fieldAlias = field.getValue(alias);
				Object value = field.getValue(obj);
				if (field.isPrimitive) {
					fieldAlias = query.getPrimitiveAliasByValue(fieldAlias);
				}
				if (!firstCondition) {
					query.addConditionToken(ConditionAndOr.AND);
				}
				firstCondition = false;
				query.addConditionToken(new Condition<Object>(fieldAlias, value, CompareType.EQUAL));
			}
		}
		stat.setSQL(buff.toString());
		query.appendWhere(stat);
		IciqlLogger.delete(stat.getSQL());
		return stat.executeUpdate();
	}

	TableDefinition<T> createIfRequired(Db db) {
		// globally enable/disable check of create if required
		if (db.getSkipCreate()) {
			return this;
		}
		if (!createIfRequired) {
			// skip table and index creation
			// but still check for upgrades
			db.upgradeTable(this);
			return this;
		}
		if (db.hasCreated(clazz)) {
			return this;
		}
		SQLStatement stat = new SQLStatement(db);
		if (StringUtils.isNullOrEmpty(viewTableName)) {
			db.getDialect().prepareCreateTable(stat, this);
		} else {
			db.getDialect().prepareCreateView(stat, this);
		}
		IciqlLogger.create(stat.getSQL());
		try {
			stat.executeUpdate();
		} catch (IciqlException e) {
			if (e.getIciqlCode() != IciqlException.CODE_OBJECT_ALREADY_EXISTS) {
				throw e;
			}
		}

		// create indexes
		for (IndexDefinition index : indexes) {
			stat = new SQLStatement(db);
			db.getDialect().prepareCreateIndex(stat, schemaName, tableName, index);
			IciqlLogger.create(stat.getSQL());
			try {
				stat.executeUpdate();
			} catch (IciqlException e) {
				if (e.getIciqlCode() != IciqlException.CODE_OBJECT_ALREADY_EXISTS
						&& e.getIciqlCode() != IciqlException.CODE_DUPLICATE_KEY) {
					throw e;
				}
			}
		}

		// create unique constraints
		for (ConstraintUniqueDefinition constraint : constraintsUnique) {
			stat = new SQLStatement(db);
			db.getDialect().prepareCreateConstraintUnique(stat, schemaName, tableName, constraint);
			IciqlLogger.create(stat.getSQL());
			try {
				stat.executeUpdate();
			} catch (IciqlException e) {
				if (e.getIciqlCode() != IciqlException.CODE_OBJECT_ALREADY_EXISTS
						&& e.getIciqlCode() != IciqlException.CODE_DUPLICATE_KEY) {
					throw e;
				}
			}
		}

		// create foreign keys constraints
		for (ConstraintForeignKeyDefinition constraint : constraintsForeignKey) {
			stat = new SQLStatement(db);
			db.getDialect().prepareCreateConstraintForeignKey(stat, schemaName, tableName, constraint);
			IciqlLogger.create(stat.getSQL());
			try {
				stat.executeUpdate();
			} catch (IciqlException e) {
				if (e.getIciqlCode() != IciqlException.CODE_OBJECT_ALREADY_EXISTS
						&& e.getIciqlCode() != IciqlException.CODE_DUPLICATE_KEY) {
					throw e;
				}
			}
		}

		// tables are created using IF NOT EXISTS
		// but we may still need to upgrade
		db.upgradeTable(this);
		return this;
	}

	void mapObject(Object obj) {
		fieldMap.clear();
		initObject(obj, fieldMap);

		if (clazz.isAnnotationPresent(IQSchema.class)) {
			IQSchema schemaAnnotation = clazz.getAnnotation(IQSchema.class);
			// setup schema name mapping, if properly annotated
			if (!StringUtils.isNullOrEmpty(schemaAnnotation.value())) {
				schemaName = schemaAnnotation.value();
			}
		}

		if (clazz.isAnnotationPresent(IQTable.class)) {
			IQTable tableAnnotation = clazz.getAnnotation(IQTable.class);

			// setup table name mapping, if properly annotated
			if (!StringUtils.isNullOrEmpty(tableAnnotation.name())) {
				tableName = tableAnnotation.name();
			}

			// allow control over createTableIfRequired()
			createIfRequired = tableAnnotation.create();

			// model version
			if (clazz.isAnnotationPresent(IQVersion.class)) {
				IQVersion versionAnnotation = clazz.getAnnotation(IQVersion.class);
				if (versionAnnotation.value() > 0) {
					tableVersion = versionAnnotation.value();
				}
			}

			// setup the primary index, if properly annotated
			if (tableAnnotation.primaryKey().length > 0) {
				List<String> primaryKey = Utils.newArrayList();
				primaryKey.addAll(Arrays.asList(tableAnnotation.primaryKey()));
				setPrimaryKey(primaryKey);
			}
		}

		if (clazz.isAnnotationPresent(IQView.class)) {
			IQView viewAnnotation = clazz.getAnnotation(IQView.class);

			// setup view name mapping, if properly annotated
			// set this as the table name so it fits in seemlessly with iciql
			if (!StringUtils.isNullOrEmpty(viewAnnotation.name())) {
				tableName = viewAnnotation.name();
			} else {
				tableName = clazz.getSimpleName();
			}

			// setup source table name mapping, if properly annotated
			if (!StringUtils.isNullOrEmpty(viewAnnotation.tableName())) {
				viewTableName = viewAnnotation.tableName();
			} else {
				// check for IQTable annotation on super class
				Class<?> superClass = clazz.getSuperclass();
				if (superClass.isAnnotationPresent(IQTable.class)) {
					IQTable table = superClass.getAnnotation(IQTable.class);
					if (StringUtils.isNullOrEmpty(table.name())) {
						// super.SimpleClassName
						viewTableName = superClass.getSimpleName();
					} else {
						// super.IQTable.name()
						viewTableName = table.name();
					}
				} else if (superClass.isAnnotationPresent(IQView.class)) {
					// super class is a view
					IQView parentView = superClass.getAnnotation(IQView.class);
					if (StringUtils.isNullOrEmpty(parentView.tableName())) {
						// parent view does not define a tableName, must be inherited
						Class<?> superParent = superClass.getSuperclass();
						if (superParent != null && superParent.isAnnotationPresent(IQTable.class)) {
							IQTable superParentTable = superParent.getAnnotation(IQTable.class);
							if (StringUtils.isNullOrEmpty(superParentTable.name())) {
								// super.super.SimpleClassName
								viewTableName = superParent.getSimpleName();
							} else {
								// super.super.IQTable.name()
								viewTableName = superParentTable.name();
							}
						}
					} else {
						// super.IQView.tableName()
						viewTableName = parentView.tableName();
					}
				}

				if (StringUtils.isNullOrEmpty(viewTableName)) {
					// still missing view table name
					throw new IciqlException("View model class \"{0}\" is missing a table name!", tableName);
				}
			}

			// allow control over createTableIfRequired()
			createIfRequired = viewAnnotation.create();
		}

		if (clazz.isAnnotationPresent(IQIndex.class)) {
			// single table index
			IQIndex index = clazz.getAnnotation(IQIndex.class);
			addIndex(index);
		}

		if (clazz.isAnnotationPresent(IQIndexes.class)) {
			// multiple table indexes
			IQIndexes indexes = clazz.getAnnotation(IQIndexes.class);
			for (IQIndex index : indexes.value()) {
				addIndex(index);
			}
		}

		if (clazz.isAnnotationPresent(IQContraintUnique.class)) {
			// single table unique constraint
			IQContraintUnique constraint = clazz.getAnnotation(IQContraintUnique.class);
			addConstraintUnique(constraint);
		}

		if (clazz.isAnnotationPresent(IQContraintsUnique.class)) {
			// multiple table unique constraints
			IQContraintsUnique constraints = clazz.getAnnotation(IQContraintsUnique.class);
			for (IQContraintUnique constraint : constraints.value()) {
				addConstraintUnique(constraint);
			}
		}

		if (clazz.isAnnotationPresent(IQContraintForeignKey.class)) {
			// single table constraint
			IQContraintForeignKey constraint = clazz.getAnnotation(IQContraintForeignKey.class);
			addConstraintForeignKey(constraint);
		}

		if (clazz.isAnnotationPresent(IQContraintsForeignKey.class)) {
			// multiple table constraints
			IQContraintsForeignKey constraints = clazz.getAnnotation(IQContraintsForeignKey.class);
			for (IQContraintForeignKey constraint : constraints.value()) {
				addConstraintForeignKey(constraint);
			}
		}

	}

	private void addConstraintForeignKey(IQContraintForeignKey constraint) {
		List<String> foreignColumns = Arrays.asList(constraint.foreignColumns());
		List<String> referenceColumns = Arrays.asList(constraint.referenceColumns());
		addConstraintForeignKey(constraint.name(), foreignColumns, constraint.referenceName(), referenceColumns, constraint.deleteType(), constraint.updateType(), constraint.deferrabilityType());
	}

	private void addConstraintUnique(IQContraintUnique constraint) {
		List<String> uniqueColumns = Arrays.asList(constraint.uniqueColumns());
		addConstraintUnique(constraint.name(), uniqueColumns);
	}

	/**
	 * Defines a foreign key constraint with the specified parameters.
	 *
	 * @param name
	 *            name of the constraint
	 * @param foreignColumns
	 *            list of columns declared as foreign
	 * @param referenceName
	 *            reference table name
	 * @param referenceColumns
	 *            list of columns used in reference table
	 * @param deleteType
	 *            action on delete
	 * @param updateType
	 *            action on update
	 * @param deferrabilityType
	 *            deferrability mode
	 */
	private void addConstraintForeignKey(String name,
			List<String> foreignColumns, String referenceName,
			List<String> referenceColumns, ConstraintDeleteType deleteType,
			ConstraintUpdateType updateType, ConstraintDeferrabilityType deferrabilityType) {
		ConstraintForeignKeyDefinition constraint = new ConstraintForeignKeyDefinition();
		if (StringUtils.isNullOrEmpty(name)) {
			constraint.constraintName = tableName + "_fkey_" + constraintsForeignKey.size();
		} else {
			constraint.constraintName = name;
		}
		constraint.foreignColumns = Utils.newArrayList(foreignColumns);
		constraint.referenceColumns = Utils.newArrayList(referenceColumns);
		constraint.referenceTable = referenceName;
		constraint.deleteType = deleteType;
		constraint.updateType = updateType;
		constraint.deferrabilityType = deferrabilityType;
		constraintsForeignKey.add(constraint);
	}

	private void addIndex(IQIndex index) {
		List<String> columns = Arrays.asList(index.value());
		addIndex(index.name(), index.type(), columns);
	}

	List<IndexDefinition> getIndexes() {
		return indexes;
	}

	List<ConstraintUniqueDefinition> getContraintsUnique() {
		return constraintsUnique;
	}

	List<ConstraintForeignKeyDefinition> getContraintsForeignKey() {
		return constraintsForeignKey;
	}

	private void initObject(Object obj, Map<Object, FieldDefinition> map) {
		for (FieldDefinition def : fields) {
			Object newValue = def.initWithNewObject(obj);
			map.put(newValue, def);
		}
	}

	void initSelectObject(SelectTable<T> table, Object obj, Map<Object, SelectColumn<T>> map, boolean reuse) {
		for (FieldDefinition def : fields) {
			Object value;
			if (!reuse) {
				value = def.initWithNewObject(obj);
			} else {
				value = def.getValue(obj);
			}
			SelectColumn<T> column = new SelectColumn<T>(table, def);
			map.put(value, column);
		}
	}

	/**
	 * Most queries executed by iciql have named select lists (select alpha,
	 * beta where...) but sometimes a wildcard select is executed (select *).
	 * When a wildcard query is executed on a table that has more columns than
	 * are mapped in your model object, this creates a column mapping issue.
	 * JaQu assumed that you can always use the integer index of the
	 * reflectively mapped field definition to determine position in the result
	 * set.
	 *
	 * This is not always true.
	 *
	 * iciql identifies when a select * query is executed and maps column names
	 * to a column index from the result set. If the select statement is
	 * explicit, then the standard assumed column index is used instead.
	 *
	 * @param rs
	 * @return
	 */
	int[] mapColumns(boolean wildcardSelect, ResultSet rs) {
		int[] columns = new int[fields.size()];
		for (int i = 0; i < fields.size(); i++) {
			try {
				FieldDefinition def = fields.get(i);
				int columnIndex;
				if (wildcardSelect) {
					// select *
					// create column index by field name
					columnIndex = rs.findColumn(def.columnName);
				} else {
					// select alpha, beta, gamma, etc
					// explicit select order
					columnIndex = i + 1;
				}
				columns[i] = columnIndex;
			} catch (SQLException s) {
				throw new IciqlException(s);
			}
		}
		return columns;
	}

	void readRow(SQLDialect dialect, Object item, ResultSet rs, int[] columns) {
		for (int i = 0; i < fields.size(); i++) {
			FieldDefinition def = fields.get(i);
			int index = columns[i];
			Object o = def.read(rs, index);
			def.setValue(dialect, item, o);
		}
	}

	void appendSelectList(SQLStatement stat) {
		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				stat.appendSQL(", ");
			}
			FieldDefinition def = fields.get(i);
			stat.appendColumn(def.columnName);
		}
	}

	<Y, X> void appendSelectList(SQLStatement stat, Query<Y> query, X x) {
		// select t0.col1, t0.col2, t0.col3...
		// select table1.col1, table1.col2, table1.col3...
		String selectDot = "";
		SelectTable<?> sel = query.getSelectTable(x);
		if (sel != null) {
			if (query.isJoin()) {
				selectDot = sel.getAs() + ".";
			} else {
				String sn = sel.getAliasDefinition().schemaName;
				String tn = sel.getAliasDefinition().tableName;
				selectDot = query.getDb().getDialect().prepareTableName(sn, tn) + ".";
			}
		}

		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				stat.appendSQL(", ");
			}
			stat.appendSQL(selectDot);
			FieldDefinition def = fields.get(i);
			if (def.isPrimitive) {
				Object obj = def.getValue(x);
				Object alias = query.getPrimitiveAliasByValue(obj);
				query.appendSQL(stat, x, alias);
			} else {
				Object obj = def.getValue(x);
				query.appendSQL(stat, x, obj);
			}
		}
	}
}
