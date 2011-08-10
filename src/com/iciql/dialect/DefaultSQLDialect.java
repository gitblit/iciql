package com.iciql.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.iciql.IciqlException;
import com.iciql.SQLDialect;
import com.iciql.SQLStatement;
import com.iciql.TableDefinition;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StringUtils;

/**
 * Default implementation of an SQL dialect. Does not support merge nor index
 * creation.
 */
public class DefaultSQLDialect implements SQLDialect {
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