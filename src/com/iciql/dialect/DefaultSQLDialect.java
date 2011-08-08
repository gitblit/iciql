package com.iciql.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.iciql.IciqlException;
import com.iciql.SQLDialect;
import com.iciql.SQLStatement;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StringUtils;

/**
 * Default implementation of an SQL dialect. Does not support merge nor index
 * creation.
 */
public class DefaultSQLDialect implements SQLDialect {
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