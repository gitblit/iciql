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
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.iciql.Iciql.EnumId;
import com.iciql.Iciql.EnumType;
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQEnum;
import com.iciql.Iciql.IQIndex;
import com.iciql.Iciql.IQIndexes;
import com.iciql.Iciql.IQSchema;
import com.iciql.Iciql.IQTable;
import com.iciql.Iciql.IQVersion;
import com.iciql.Iciql.IndexType;
import com.iciql.util.StatementBuilder;
import com.iciql.util.StatementLogger;
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
		boolean isPrimitive;

		Object getValue(Object obj) {
			try {
				return field.get(obj);
			} catch (Exception e) {
				throw new IciqlException(e);
			}
		}

		Object initWithNewObject(Object obj) {
			Object o = Utils.newObject(field.getType());
			setValue(obj, o);
			return o;
		}

		void setValue(Object obj, Object o) {
			try {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				Class<?> targetType = field.getType();
				if (targetType.isEnum()) {
					o = Utils.convertEnum(o, targetType, enumType);
				} else {
					o = Utils.convert(o, targetType);
				}
				field.set(obj, o);
			} catch (IciqlException e) {
				throw e;
			} catch (Exception e) {
				throw new IciqlException(e);
			}
		}

		Object read(ResultSet rs, int columnIndex) {
			try {
				return rs.getObject(columnIndex);
			} catch (SQLException e) {
				throw new IciqlException(e);
			}
		}
	}

	public ArrayList<FieldDefinition> fields = Utils.newArrayList();
	String schemaName;
	String tableName;
	int tableVersion;
	List<String> primaryKeyColumnNames;
	boolean memoryTable;

	private boolean createTableIfRequired = true;
	private Class<T> clazz;
	private IdentityHashMap<Object, FieldDefinition> fieldMap = Utils.newIdentityHashMap();
	private ArrayList<IndexDefinition> indexes = Utils.newArrayList();

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

	FieldDefinition getField(String name) {
		for (FieldDefinition field : fields) {
			if (field.columnName.equalsIgnoreCase(name)) {
				return field;
			}
		}
		return null;
	}

	void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Define a primary key by the specified model fields.
	 * 
	 * @param modelFields
	 *            the ordered list of model fields
	 */
	void setPrimaryKey(Object[] modelFields) {
		List<String> columnNames = mapColumnNames(modelFields);
		setPrimaryKey(columnNames);
	}

	/**
	 * Define a primary key by the specified column names.
	 * 
	 * @param columnNames
	 *            the ordered list of column names
	 */
	void setPrimaryKey(List<String> columnNames) {
		primaryKeyColumnNames = Utils.newArrayList(columnNames);
		// set isPrimaryKey flag for all field definitions
		for (FieldDefinition fieldDefinition : fieldMap.values()) {
			fieldDefinition.isPrimaryKey = this.primaryKeyColumnNames.contains(fieldDefinition.columnName);
		}
	}

	<A> String getColumnName(A fieldObject) {
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
	 * @param type
	 *            the index type (STANDARD, HASH, UNIQUE, UNIQUE_HASH)
	 * @param modelFields
	 *            the ordered list of model fields
	 */
	void addIndex(IndexType type, Object[] modelFields) {
		List<String> columnNames = mapColumnNames(modelFields);
		addIndex(null, type, columnNames);
	}

	/**
	 * Defines an index with the specified column names.
	 * 
	 * @param type
	 *            the index type (STANDARD, HASH, UNIQUE, UNIQUE_HASH)
	 * @param columnNames
	 *            the ordered list of column names
	 */
	void addIndex(String name, IndexType type, List<String> columnNames) {
		IndexDefinition index = new IndexDefinition();
		if (StringUtils.isNullOrEmpty(name)) {
			index.indexName = tableName + "_" + indexes.size();
		} else {
			index.indexName = name;
		}
		index.columnNames = Utils.newArrayList(columnNames);
		index.type = type;
		indexes.add(index);
	}

	public void setColumnName(Object column, String columnName) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.columnName = columnName;
		}
	}

	public void setLength(Object column, int length) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.length = length;
		}
	}

	public void setScale(Object column, int scale) {
		FieldDefinition def = fieldMap.get(column);
		if (def != null) {
			def.scale = scale;
		}
	}

	void mapFields() {
		boolean byAnnotationsOnly = false;
		boolean inheritColumns = false;
		if (clazz.isAnnotationPresent(IQTable.class)) {
			IQTable tableAnnotation = clazz.getAnnotation(IQTable.class);
			byAnnotationsOnly = tableAnnotation.annotationsOnly();
			inheritColumns = tableAnnotation.inheritColumns();
		}

		List<Field> classFields = Utils.newArrayList();
		classFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		if (inheritColumns) {
			Class<?> superClass = clazz.getSuperclass();
			classFields.addAll(Arrays.asList(superClass.getDeclaredFields()));
		}

		T defaultObject = Db.instance(clazz);
		for (Field f : classFields) {
			// default to field name
			String columnName = f.getName();
			boolean isAutoIncrement = false;
			boolean isPrimaryKey = false;
			int length = 0;
			int scale = 0;
			boolean trim = false;
			boolean nullable = !f.getType().isPrimitive();
			EnumType enumType = null;
			String defaultValue = "";
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

				// annotation overrides
				if (!StringUtils.isNullOrEmpty(col.defaultValue())) {
					defaultValue = col.defaultValue();
				}
			}

			boolean isPublic = Modifier.isPublic(f.getModifiers());
			boolean reflectiveMatch = isPublic && !byAnnotationsOnly;
			if (reflectiveMatch || hasAnnotation) {
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
				fieldDef.dataType = ModelUtils.getDataType(fieldDef);
				fields.add(fieldDef);
			}
		}
		List<String> primaryKey = Utils.newArrayList();
		for (FieldDefinition fieldDef : fields) {
			if (fieldDef.isPrimaryKey) {
				primaryKey.add(fieldDef.columnName);
			}
		}
		if (primaryKey.size() > 0) {
			setPrimaryKey(primaryKey);
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
				EnumId enumid = (EnumId) value;
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
		// standard behavior
		return value;
	}

	long insert(Db db, Object obj, boolean returnKey) {
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
			stat.addParameter(value);
		}
		buff.append(')');
		stat.setSQL(buff.toString());
		StatementLogger.insert(stat.getSQL());
		if (returnKey) {
			return stat.executeInsert();
		}
		return stat.executeUpdate();
	}

	private boolean skipInsertField(FieldDefinition field, Object obj) {
		// skip uninitialized primitive autoincrement values
		if (field.isAutoIncrement && field.isPrimitive) {
			Object value = getValue(obj, field);
			if (value.toString().equals("0")) {
				return true;
			}
		}
		return false;
	}

	void merge(Db db, Object obj) {
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.size() == 0) {
			throw new IllegalStateException("No primary key columns defined " + "for table " + obj.getClass()
					+ " - no update possible");
		}
		SQLStatement stat = new SQLStatement(db);
		db.getDialect().prepareMerge(stat, schemaName, tableName, this, obj);
		StatementLogger.merge(stat.getSQL());
		stat.executeUpdate();
	}

	int update(Db db, Object obj) {
		if (primaryKeyColumnNames == null || primaryKeyColumnNames.size() == 0) {
			throw new IllegalStateException("No primary key columns defined " + "for table " + obj.getClass()
					+ " - no update possible");
		}
		SQLStatement stat = new SQLStatement(db);
		StatementBuilder buff = new StatementBuilder("UPDATE ");
		buff.append(db.getDialect().prepareTableName(schemaName, tableName)).append(" SET ");
		buff.resetCount();

		for (FieldDefinition field : fields) {
			if (!field.isPrimaryKey) {
				buff.appendExceptFirst(", ");
				buff.append(db.getDialect().prepareColumnName(field.columnName));
				buff.append(" = ?");
				Object value = getValue(obj, field);
				stat.addParameter(value);
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
		StatementLogger.update(stat.getSQL());
		return stat.executeUpdate();
	}

	int delete(Db db, Object obj) {
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
		StatementLogger.delete(stat.getSQL());
		return stat.executeUpdate();
	}

	TableDefinition<T> createTableIfRequired(Db db) {
		if (!createTableIfRequired) {
			// skip table and index creation
			// but still check for upgrades
			db.upgradeTable(this);
			return this;
		}
		SQLStatement stat = new SQLStatement(db);
		db.getDialect().prepareCreateTable(stat, this);
		StatementLogger.create(stat.getSQL());
		stat.executeUpdate();

		// create indexes
		for (IndexDefinition index : indexes) {
			stat = new SQLStatement(db);
			db.getDialect().prepareCreateIndex(stat, schemaName, tableName, index);
			StatementLogger.create(stat.getSQL());
			try {
				stat.executeUpdate();
			} catch (IciqlException e) {
				if (e.getIciqlCode() != IciqlException.CODE_INDEX_ALREADY_EXISTS) {
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
			createTableIfRequired = tableAnnotation.create();

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
	}

	void addIndex(IQIndex index) {
		List<String> columns = Arrays.asList(index.value());
		addIndex(index.name(), index.type(), columns);
	}

	List<IndexDefinition> getIndexes() {
		return indexes;
	}

	void initObject(Object obj, Map<Object, FieldDefinition> map) {
		for (FieldDefinition def : fields) {
			Object newValue = def.initWithNewObject(obj);
			map.put(newValue, def);
		}
	}

	void initSelectObject(SelectTable<T> table, Object obj, Map<Object, SelectColumn<T>> map) {
		for (FieldDefinition def : fields) {
			Object newValue = def.initWithNewObject(obj);
			SelectColumn<T> column = new SelectColumn<T>(table, def);
			map.put(newValue, column);
		}
	}

	void readRow(Object item, ResultSet rs) {
		for (int i = 0; i < fields.size(); i++) {
			FieldDefinition def = fields.get(i);
			Object o = def.read(rs, i + 1);
			def.setValue(item, o);
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
		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				stat.appendSQL(", ");
			}
			FieldDefinition def = fields.get(i);
			Object obj = def.getValue(x);
			query.appendSQL(stat, x, obj);
		}
	}
}
