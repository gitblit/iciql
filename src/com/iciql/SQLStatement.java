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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.iciql.util.JdbcUtils;

/**
 * This class represents a parameterized SQL statement.
 */

public class SQLStatement {
	private Db db;
	private StringBuilder buff = new StringBuilder();
	private String sql;
	private ArrayList<Object> params = new ArrayList<Object>();

	SQLStatement(Db db) {
		this.db = db;
	}

	void setSQL(String sql) {
		this.sql = sql;
		buff = new StringBuilder(sql);
	}

	public SQLStatement appendSQL(String s) {
		buff.append(s);
		sql = null;
		return this;
	}

	public SQLStatement appendTable(String schema, String table) {
		return appendSQL(db.getDialect().prepareTableName(schema, table));
	}
	
	public SQLStatement appendColumn(String column) {
		return appendSQL(db.getDialect().prepareColumnName(column));
	}

	String getSQL() {
		if (sql == null) {
			sql = buff.toString();
		}
		return sql;
	}
	
	SQLStatement addParameter(Object o) {
		params.add(o);
		return this;
	}

	ResultSet executeQuery() {
		try {
			return prepare(false).executeQuery();
		} catch (SQLException e) {
			throw new IciqlException(e);
		}
	}

	int executeUpdate() {
		PreparedStatement ps = null;
		try {
			ps = prepare(false);
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new IciqlException(e);
		} finally {
			JdbcUtils.closeSilently(ps);
		}
	}

	long executeInsert() {
		PreparedStatement ps = null;
		try {
			ps = prepare(true);
			ps.executeUpdate();
			long identity = -1;
			ResultSet rs = ps.getGeneratedKeys();
			if (rs != null && rs.next()) {
				identity = rs.getLong(1);
			}
			JdbcUtils.closeSilently(rs);
			return identity;
		} catch (SQLException e) {
			throw new IciqlException(e);
		} finally {
			JdbcUtils.closeSilently(ps);
		}
	}

	private static void setValue(PreparedStatement prep, int parameterIndex, Object x) {
		try {
			prep.setObject(parameterIndex, x);
		} catch (SQLException e) {
			throw new IciqlException(e);
		}
	}

	private PreparedStatement prepare(boolean returnGeneratedKeys) {
		PreparedStatement prep = db.prepare(getSQL(), returnGeneratedKeys);
		for (int i = 0; i < params.size(); i++) {
			Object o = params.get(i);
			setValue(prep, i + 1, o);
		}
		return prep;
	}

}
