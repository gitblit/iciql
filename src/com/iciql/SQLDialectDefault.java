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
import java.sql.SQLException;

import com.iciql.TableDefinition.FieldDefinition;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StatementBuilder;
import com.iciql.util.StringUtils;

/**
 * Default implementation of an SQL dialect. Does not support merge nor index
 * creation.
 */
public class SQLDialectDefault implements SQLDialect {
	float databaseVersion;
	String databaseName;
	String productVersion;

	@Override
	public String toString() {
		return getClass().getName() + ": " + databaseName + " " + productVersion;
	}

	@Override
	public void configureDialect(String databaseName, DatabaseMetaData data) {
		this.databaseName = databaseName;
		try {
			databaseVersion = Float.parseFloat(data.getDatabaseMajorVersion() + "."
					+ data.getDatabaseMinorVersion());
			productVersion = data.getDatabaseProductVersion();
		} catch (SQLException e) {
			throw new IciqlException(e);
		}
	}

	/**
	 * Allows subclasses to change the type of a column for a CREATE statement.
	 * 
	 * @param sqlType
	 * @return the SQL type or a preferred alternative
	 */
	protected String convertSqlType(String sqlType) {
		return sqlType;
	}

	@Override
	public boolean supportsMemoryTables() {
		return false;
	}

	@Override
	public boolean supportsLimitOffset() {
		return true;
	}

	@Override
	public String prepareTableName(String schemaName, String tableName) {
		if (StringUtils.isNullOrEmpty(schemaName)) {
			return tableName;
		}
		return schemaName + "." + tableName;
	}

	@Override
	public String prepareColumnName(String name) {
		return name;
	}

	@Override
	public <T> void prepareCreateTable(SQLStatement stat, TableDefinition<T> def) {
		StatementBuilder buff;
		if (def.memoryTable && supportsMemoryTables()) {
			buff = new StatementBuilder("CREATE MEMORY TABLE IF NOT EXISTS ");
		} else {
			buff = new StatementBuilder("CREATE TABLE IF NOT EXISTS ");
		}

		buff.append(prepareTableName(def.schemaName, def.tableName)).append('(');

		boolean hasIdentityColumn = false;
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(field.columnName)).append(' ');
			String dataType = field.dataType;
			if (dataType.equals("VARCHAR")) {
				// check to see if we should use VARCHAR or CLOB
				if (field.length <= 0) {
					dataType = "CLOB";
				}
				buff.append(convertSqlType(dataType));
				if (field.length > 0) {
					buff.append('(').append(field.length).append(')');
				}
			} else if (dataType.equals("DECIMAL")) {
				// DECIMAL(precision,scale)
				buff.append(convertSqlType(dataType));
				if (field.length > 0) {
					buff.append('(').append(field.length);
					if (field.scale > 0) {
						buff.append(',').append(field.scale);
					}
					buff.append(')');
				}
			} else {
				// other
				buff.append(convertSqlType(dataType));
			}

			hasIdentityColumn |= prepareColumnDefinition(buff, field.isAutoIncrement, field.isPrimaryKey);

			if (!field.nullable) {
				buff.append(" NOT NULL");
			}

			// default values
			if (!field.isAutoIncrement && !field.isPrimaryKey) {
				String dv = field.defaultValue;
				if (!StringUtils.isNullOrEmpty(dv)) {
					if (ModelUtils.isProperlyFormattedDefaultValue(dv)
							&& ModelUtils.isValidDefaultValue(field.field.getType(), dv)) {
						buff.append(" DEFAULT " + dv);
					}
				}
			}
		}

		// if table does not have identity column then specify primary key
		if (!hasIdentityColumn) {
			if (def.primaryKeyColumnNames != null && def.primaryKeyColumnNames.size() > 0) {
				buff.append(", PRIMARY KEY(");
				buff.resetCount();
				for (String n : def.primaryKeyColumnNames) {
					buff.appendExceptFirst(", ");
					buff.append(prepareColumnName(n));
				}
				buff.append(')');
			}
		}
		buff.append(')');
		stat.setSQL(buff.toString());
	}

	protected boolean prepareColumnDefinition(StatementBuilder buff, boolean isAutoIncrement,
			boolean isPrimaryKey) {
		boolean isIdentity = false;
		if (isAutoIncrement && isPrimaryKey) {
			buff.append(" IDENTITY");
			isIdentity = true;
		} else if (isAutoIncrement) {
			buff.append(" AUTO_INCREMENT");
		}
		return isIdentity;
	}

	@Override
	public void prepareCreateIndex(SQLStatement stat, String schemaName, String tableName,
			IndexDefinition index) {
		throw new IciqlException("{0} does not support index creation!", getClass().getSimpleName());
	}

	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName,
			TableDefinition<T> def, Object obj) {
		throw new IciqlException("{0} does not support merge statements!", getClass().getSimpleName());
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