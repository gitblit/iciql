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

import com.iciql.TableDefinition.IndexDefinition;
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
	public String prepareCreateIndex(String schemaName, String tableName, IndexDefinition index) {
		throw new IciqlException("Dialect does not support index creation!");
	}

	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName, TableDefinition<T> def, Object obj) {
		throw new IciqlException("Dialect does not support merge statements!");
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