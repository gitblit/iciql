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

import com.iciql.TableDefinition.IndexDefinition;

/**
 * This interface defines points where iciql can build different statements
 * depending on the database used.
 */
public interface SQLDialect {

	/**
	 * Configure the dialect from the database metadata.
	 * 
	 * @param databaseName
	 * @param data
	 */
	void configureDialect(String databaseName, DatabaseMetaData data);

	/**
	 * Allows a dialect to substitute an SQL type.
	 * 
	 * @param sqlType
	 * @return the dialect-safe type
	 */
	String convertSqlType(String sqlType);

	/**
	 * Returns a properly formatted table name for the dialect.
	 * 
	 * @param schemaName
	 *            the schema name, or null for no schema
	 * @param tableName
	 *            the properly formatted table name
	 * @return the SQL snippet
	 */
	String prepareTableName(String schemaName, String tableName);

	/**
	 * Returns a properly formatted column name for the dialect.
	 * 
	 * @param name
	 *            the column name
	 * @return the properly formatted column name
	 */
	String prepareColumnName(String name);

	/**
	 * Get the CREATE TABLE statement.
	 * 
	 * @param stat
	 * @param def
	 */
	<T> void prepareCreateTable(SQLStatement stat, TableDefinition<T> def);

	/**
	 * Get the DROP TABLE statement.
	 * 
	 * @param stat
	 * @param def
	 */
	<T> void prepareDropTable(SQLStatement stat, TableDefinition<T> def);

	
	/**
	 * Get the CREATE VIEW statement.
	 * 
	 * @param stat
	 * @param def
	 */
	<T> void prepareCreateView(SQLStatement stat, TableDefinition<T> def);

	/**
	 * Get the CREATE VIEW statement.
	 * 
	 * @param stat
	 * @param def
	 * @param fromWhere
	 */
	<T> void prepareCreateView(SQLStatement stat, TableDefinition<T> def, String fromWhere);

	/**
	 * Get the DROP VIEW statement.
	 * 
	 * @param stat
	 * @param def
	 */
	<T> void prepareDropView(SQLStatement stat, TableDefinition<T> def);
	
	/**
	 * Get the CREATE INDEX statement.
	 * 
	 * @param schemaName
	 *            the schema name
	 * @param tableName
	 *            the table name
	 * @param index
	 *            the index definition
	 * @return the SQL statement
	 */
	void prepareCreateIndex(SQLStatement stat, String schemaName, String tableName, IndexDefinition index);

	/**
	 * Get a MERGE or REPLACE INTO statement.
	 * 
	 * @param schemaName
	 *            the schema name
	 * @param tableName
	 *            the table name
	 * @param index
	 *            the index definition
	 */
	<T> void prepareMerge(SQLStatement stat, String schemaName, String tableName, TableDefinition<T> def,
			Object obj);

	/**
	 * Append "LIMIT limit OFFSET offset" to the SQL statement.
	 * 
	 * @param stat
	 *            the statement
	 * @param limit
	 *            the limit
	 * @param offset
	 *            the offset
	 */
	void appendLimitOffset(SQLStatement stat, long limit, long offset);

	/**
	 * Returns the preferred DATETIME class for the database.
	 * <p>
	 * Either java.util.Date or java.sql.Timestamp
	 * 
	 * @return preferred DATETIME class
	 */
	Class<? extends java.util.Date> getDateTimeClass();

	/**
	 * When building static string statements this method flattens an object to
	 * a string representation suitable for a static string statement.
	 * 
	 * @param o
	 * @return the string equivalent of this object
	 */
	String prepareParameter(Object o);
}
