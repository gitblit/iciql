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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.iciql.Iciql.IQTable;
import com.iciql.util.JdbcUtils;
import com.iciql.util.StringUtils;
import com.iciql.util.Utils;

/**
 * Class to inspect a model and a database for the purposes of model validation
 * and automatic model generation. This class finds the available schemas and
 * tables and serves as the entry point for model generation and validation.
 */
public class DbInspector {

	private Db db;
	private DatabaseMetaData metaData;
	private Class<? extends java.util.Date> dateTimeClass = java.util.Date.class;

	public DbInspector(Db db) {
		this.db = db;
		setPreferredDateTimeClass(db.getDialect().getDateTimeClass());
	}

	/**
	 * Set the preferred class to store date and time. Possible values are:
	 * java.util.Date (default) and java.sql.Timestamp.
	 * 
	 * @param dateTimeClass
	 *            the new class
	 */
	public void setPreferredDateTimeClass(Class<? extends java.util.Date> dateTimeClass) {
		this.dateTimeClass = dateTimeClass;
	}

	/**
	 * Generates models class skeletons for schemas and tables. If the table
	 * name is undefined, models will be generated for every table within the
	 * specified schema. Additionally, if no schema is defined, models will be
	 * generated for all schemas and all tables.
	 * 
	 * @param schema
	 *            the schema name (optional)
	 * @param table
	 *            the table name (optional)
	 * @param packageName
	 *            the package name (optional)
	 * @param annotateSchema
	 *            (includes schema name in annotation)
	 * @param trimStrings
	 *            (trims strings to maxLength of column)
	 * @return a list of complete model classes as strings, each element a class
	 */
	public List<String> generateModel(String schema, String table, String packageName,
			boolean annotateSchema, boolean trimStrings) {
		try {
			List<String> models = Utils.newArrayList();
			List<TableInspector> tables = getTables(schema, table);
			for (TableInspector t : tables) {
				t.read(metaData);
				String model = t.generateModel(packageName, annotateSchema, trimStrings);
				models.add(model);
			}
			return models;
		} catch (SQLException s) {
			throw new IciqlException(s);
		}
	}

	/**
	 * Validates a model.
	 * 
	 * @param model
	 *            an instance of the model class
	 * @param throwOnError
	 *            if errors should cause validation to fail
	 * @return a list of validation remarks
	 */
	public <T> List<ValidationRemark> validateModel(T model, boolean throwOnError) {
		try {
			TableInspector inspector = getTable(model);
			inspector.read(metaData);
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) model.getClass();
			TableDefinition<T> def = db.define(clazz);
			return inspector.validate(def, throwOnError);
		} catch (SQLException s) {
			throw new IciqlException(s);
		}
	}

	private DatabaseMetaData getMetaData() throws SQLException {
		if (metaData == null) {
			metaData = db.getConnection().getMetaData();
		}
		return metaData;
	}

	/**
	 * Get the table in the database based on the model definition.
	 * 
	 * @param model
	 *            an instance of the model class
	 * @return the table inspector
	 */
	private <T> TableInspector getTable(T model) throws SQLException {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) model.getClass();
		TableDefinition<T> def = db.define(clazz);
		boolean forceUpperCase = getMetaData().storesUpperCaseIdentifiers();
		String schema = (forceUpperCase && def.schemaName != null) ? def.schemaName.toUpperCase()
				: def.schemaName;
		String table = forceUpperCase ? def.tableName.toUpperCase() : def.tableName;
		List<TableInspector> tables = getTables(schema, table);
		return tables.get(0);
	}

	/**
	 * Returns a list of tables. This method always returns at least one
	 * element. If no table is found, an exception is thrown.
	 * 
	 * @param schema
	 *            the schema name
	 * @param table
	 *            the table name
	 * @return a list of table inspectors (always contains at least one element)
	 */
	private List<TableInspector> getTables(String schema, String table) throws SQLException {
		ResultSet rs = null;
		try {
			rs = getMetaData().getSchemas();
			ArrayList<String> schemaList = Utils.newArrayList();
			while (rs.next()) {
				schemaList.add(rs.getString("TABLE_SCHEM"));
			}
			JdbcUtils.closeSilently(rs);

			String iciqlTables = DbVersion.class.getAnnotation(IQTable.class).name();

			List<TableInspector> tables = Utils.newArrayList();
			if (schemaList.size() == 0) {
				schemaList.add(null);
			}
			for (String s : schemaList) {
				rs = getMetaData().getTables(null, s, null, new String[] { "TABLE" });
				while (rs.next()) {
					String t = rs.getString("TABLE_NAME");
					if (t.charAt(0) == '"') {
						t = t.substring(1);
					}
					if (t.charAt(t.length() - 1) == '"') {
						t = t.substring(0, t.length() - 1);
					}
					if (!t.equalsIgnoreCase(iciqlTables)) {
						tables.add(new TableInspector(s, t, dateTimeClass));
					}
				}
			}

			if (StringUtils.isNullOrEmpty(schema) && StringUtils.isNullOrEmpty(table)) {
				// all schemas and tables
				return tables;
			}
			// schema subset OR table subset OR exact match
			List<TableInspector> matches = Utils.newArrayList();
			for (TableInspector t : tables) {
				if (t.matches(schema, table)) {
					matches.add(t);
				}
			}
			if (matches.size() == 0) {
				throw new IciqlException(MessageFormat.format("Failed to find schema={0} table={1}",
						schema == null ? "" : schema, table == null ? "" : table));
			}
			return matches;
		} finally {
			JdbcUtils.closeSilently(rs);
		}
	}

}
