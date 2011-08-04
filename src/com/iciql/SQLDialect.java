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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StatementBuilder;
import com.iciql.util.StringUtils;

/**
 * This interface defines points where iciql can build different statements
 * depending on the database used.
 */
public interface SQLDialect {

	/**
	 * Configure the dialect from the database connection.
	 * 
	 * @param conn
	 */
	void configureDialect(Connection conn);

	/**
	 * Returns a properly formatted table name for the dialect.
	 * 
	 * @param schema
	 *            the schema name, or null for no schema
	 * @param table
	 *            the properly formatted table name
	 * @return the SQL snippet
	 */
	String prepareTableName(String schema, String table);

	/**
	 * Returns a properly formatted column name for the dialect.
	 * 
	 * @param name
	 *            the column name
	 * @return the properly formatted column name
	 */
	String prepareColumnName(String name);

	/**
	 * Get the CREATE INDEX statement.
	 * 
	 * @param schema
	 *            the schema name
	 * @param table
	 *            the table name
	 * @param index
	 *            the index definition
	 * @return the SQL statement
	 */
	String prepareCreateIndex(String schema, String table, IndexDefinition index);

	/**
	 * Append "LIMIT limit" to the SQL statement.
	 * 
	 * @param stat
	 *            the statement
	 * @param limit
	 *            the limit
	 */
	void appendLimit(SQLStatement stat, long limit);

	/**
	 * Append "OFFSET offset" to the SQL statement.
	 * 
	 * @param stat
	 *            the statement
	 * @param offset
	 *            the offset
	 */
	void appendOffset(SQLStatement stat, long offset);

	/**
	 * Whether memory tables are supported.
	 * 
	 * @return true if they are
	 */
	boolean supportsMemoryTables();

	/**
	 * Whether merge is a supported function.
	 * 
	 * @return true if they are
	 */
	boolean supportsMerge();

	/**
	 * Whether LIMIT/OFFSET notation is supported.
	 * 
	 * @return true if they are
	 */
	boolean supportsLimitOffset();

	/**
	 * Default implementation of an SQL dialect.
	 * Does not support merge nor index creation.
	 */
	public static class DefaultSQLDialect implements SQLDialect {
		float databaseVersion;
		String productName;
		String productVersion;

		@Override
		public String toString() {
			return getClass().getName() + ": " + productName + " " + productVersion;
		}
		
		@Override
		public void configureDialect(Connection conn) {
			loadIdentity(conn);
		}
		
		protected void loadIdentity(Connection conn) {
			try {
				DatabaseMetaData data = conn.getMetaData();
				databaseVersion = Float.parseFloat(data.getDatabaseMajorVersion() + "."
						+ data.getDatabaseMinorVersion());
				productName = data.getDatabaseProductName();
				productVersion = data.getDatabaseProductVersion();
			} catch (SQLException e) {
				throw new IciqlException(e);
			}
		}		

		@Override
		public boolean supportsMemoryTables() {
			return false;
		}

		@Override
		public boolean supportsMerge() {
			return false;
		}
		
		@Override
		public boolean supportsLimitOffset() {
			return true;
		}

		@Override
		public String prepareTableName(String schema, String table) {
			if (StringUtils.isNullOrEmpty(schema)) {
				return table;
			}
			return schema + "." + table;
		}

		@Override
		public String prepareColumnName(String name) {
			return name;
		}

		@Override
		public String prepareCreateIndex(String schema, String table, IndexDefinition index) {
			throw new IciqlException("Dialect does not support index creation!");
		}

		@Override
		public void appendLimit(SQLStatement stat, long limit) {
			stat.appendSQL(" LIMIT " + limit);
		}

		@Override
		public void appendOffset(SQLStatement stat, long offset) {
			stat.appendSQL(" OFFSET " + offset);
		}
	}
	

	/**
	 * H2 database dialect.
	 */
	public static class H2Dialect extends DefaultSQLDialect {

		@Override
		public boolean supportsMemoryTables() {
			return true;
		}

		@Override
		public boolean supportsMerge() {
			return true;
		}
		
		@Override
		public String prepareCreateIndex(String schema, String table, IndexDefinition index) {
			StatementBuilder buff = new StatementBuilder();
			buff.append("CREATE ");
			switch (index.type) {
			case STANDARD:
				break;
			case UNIQUE:
				buff.append("UNIQUE ");
				break;
			case HASH:
				buff.append("HASH ");
				break;
			case UNIQUE_HASH:
				buff.append("UNIQUE HASH ");
				break;
			}
			buff.append("INDEX IF NOT EXISTS ");
			buff.append(index.indexName);
			buff.append(" ON ");
			buff.append(table);
			buff.append("(");
			for (String col : index.columnNames) {
				buff.appendExceptFirst(", ");
				buff.append(col);
			}
			buff.append(")");
			return buff.toString();
		}
	}
}
