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

import java.sql.DatabaseMetaData;

import com.iciql.Iciql.DataTypeAdapter;
import com.iciql.TableDefinition.ConstraintForeignKeyDefinition;
import com.iciql.TableDefinition.ConstraintUniqueDefinition;
import com.iciql.TableDefinition.IndexDefinition;

/**
 * This interface defines points where iciql can build different statements
 * depending on the database used.
 */
public interface SQLDialect {

	/**
	 * Returns the registered instance of the type adapter.
	 *
	 * @param typeAdapter
	 * @return the type adapter instance
	 */
	DataTypeAdapter<?> getTypeAdapter(Class<? extends DataTypeAdapter<?>> typeAdapter);

	/**
	 * Serialize the Java object into a type or format that the database will accept.
	 *
	 * @param value
	 * @param typeAdapter
	 * @return the serialized object
	 */
	<T> Object serialize(T value, Class<? extends DataTypeAdapter<?>> typeAdapter);

	/**
	 * Deserialize the object received from the database into a Java type.
	 *
	 * @param value
	 * @param typeAdapter
	 * @return the deserialized object
	 */
	Object deserialize(Object value, Class<? extends DataTypeAdapter<?>> typeAdapter);

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
	 *            return the SQL statement
	 * @param def
	 *            table definition
	 */
	<T> void prepareCreateView(SQLStatement stat, TableDefinition<T> def);

	/**
	 * Get the CREATE VIEW statement.
	 *
	 * @param stat
	 *            return the SQL statement
	 * @param def
	 *            table definition
	 * @param fromWhere
	 */
	<T> void prepareCreateView(SQLStatement stat, TableDefinition<T> def, String fromWhere);

	/**
	 * Get the DROP VIEW statement.
	 *
	 * @param stat
	 *            return the SQL statement
	 * @param def
	 *            table definition
	 */
	<T> void prepareDropView(SQLStatement stat, TableDefinition<T> def);

	/**
	 * Get the CREATE INDEX statement.
	 *
	 * @param stat
	 *            return the SQL statement
	 * @param schemaName
	 *            the schema name
	 * @param tableName
	 *            the table name
	 * @param index
	 *            the index definition
	 */
	void prepareCreateIndex(SQLStatement stat, String schemaName, String tableName, IndexDefinition index);

	/**
	 * Get the ALTER statement.
	 *
	 * @param stat
	 *            return the SQL statement
	 * @param schemaName
	 *            the schema name
	 * @param tableName
	 *            the table name
	 * @param constraint
	 *            the constraint definition
	 */
	void prepareCreateConstraintForeignKey(SQLStatement stat, String schemaName, String tableName, ConstraintForeignKeyDefinition constraint);

	/**
	 * Get the ALTER statement.
	 *
	 * @param stat
	 *            return the SQL statement
	 * @param schemaName
	 *            the schema name
	 * @param tableName
	 *            the table name
	 * @param constraint
	 *            the constraint definition
	 * return the SQL statement
	 */
	void prepareCreateConstraintUnique(SQLStatement stat, String schemaName, String tableName, ConstraintUniqueDefinition constraint);

	/**
	 * Get a MERGE or REPLACE INTO statement.
	 *
	 * @param stat
	 *            return the SQL statement
	 * @param schemaName
	 *            the schema name
	 * @param tableName
	 *            the table name
	 * @param def
	 *            the table definition
	 * @param obj
	 *            values
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
	String prepareStringParameter(Object o);

}
