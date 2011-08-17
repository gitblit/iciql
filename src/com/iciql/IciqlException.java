/*
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

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

/**
 * Iciql wraps all exceptions with this class.
 */
public class IciqlException extends RuntimeException {

	public static final int CODE_UNMAPPED_FIELD = 1;
	public static final int CODE_DUPLICATE_KEY = 2;
	public static final int CODE_OBJECT_NOT_FOUND = 3;
	public static final int CODE_OBJECT_ALREADY_EXISTS = 4;

	private static final String TOKEN_UNMAPPED_FIELD = "\\? (=|\\>|\\<|\\<\\>|!=|\\>=|\\<=|LIKE|BETWEEN) \\?";

	private static final long serialVersionUID = 1L;

	private String sql;

	private int iciqlCode;

	public IciqlException(Throwable t) {
		super(t.getMessage(), t);
		configureCode(t);
	}

	public IciqlException(String message, Object... parameters) {
		super(parameters.length > 0 ? MessageFormat.format(message, parameters) : message);
	}

	public IciqlException(Throwable t, String message, Object... parameters) {
		super(parameters.length > 0 ? MessageFormat.format(message, parameters) : message, t);
		configureCode(t);
	}

	public static void checkUnmappedField(String sql) {
		if (Pattern.compile(IciqlException.TOKEN_UNMAPPED_FIELD).matcher(sql).find()) {
			IciqlException e = new IciqlException("unmapped field in statement!");
			e.sql = sql;
			e.iciqlCode = CODE_UNMAPPED_FIELD;
			throw e;
		}
	}

	public static IciqlException fromSQL(String sql, Throwable t) {
		if (Pattern.compile(TOKEN_UNMAPPED_FIELD).matcher(sql).find()) {
			IciqlException e = new IciqlException(t, "unmapped field in statement!");
			e.sql = sql;
			e.iciqlCode = CODE_UNMAPPED_FIELD;
			return e;
		} else {
			IciqlException e = new IciqlException(t, t.getMessage());
			e.sql = sql;
			return e;
		}
	}

	public void setSQL(String sql) {
		this.sql = sql;
	}

	public String getSQL() {
		return sql;
	}

	public int getIciqlCode() {
		return iciqlCode;
	}

	private void configureCode(Throwable t) {
		if (t == null) {
			return;
		}
		if (t instanceof SQLException) {
			// http://developer.mimer.com/documentation/html_92/Mimer_SQL_Mobile_DocSet/App_Return_Codes2.html
			SQLException s = (SQLException) t;
			String state = s.getSQLState();
			if ("23000".equals(state)) {
				// MySQL duplicate primary key on insert
				iciqlCode = CODE_DUPLICATE_KEY;
			} else if ("23505".equals(state)) {
				// Derby duplicate primary key on insert
				iciqlCode = CODE_DUPLICATE_KEY;
			} else if ("42000".equals(state)) {
				// MySQL duplicate unique index value on insert
				iciqlCode = CODE_DUPLICATE_KEY;
			} else if ("42Y07".equals(state)) {
				// Derby schema not found
				iciqlCode = CODE_OBJECT_NOT_FOUND;
			} else if ("42X05".equals(state)) {
				// Derby table not found
				iciqlCode = CODE_OBJECT_NOT_FOUND;
			} else if ("42S02".equals(state)) {
				// H2 table not found
				iciqlCode = CODE_OBJECT_NOT_FOUND;
			} else if ("42501".equals(state)) {
				// HSQL table not found
				iciqlCode = CODE_OBJECT_NOT_FOUND;
			} else if ("42P01".equals(state)) {
				// PostgreSQL table not found
				iciqlCode = CODE_OBJECT_NOT_FOUND;
			} else if ("X0Y32".equals(state)) {
				// Derby table already exists
				iciqlCode = CODE_OBJECT_ALREADY_EXISTS;
			} else if ("42P07".equals(state)) {
				// PostgreSQL table or index already exists
				iciqlCode = CODE_OBJECT_ALREADY_EXISTS;
			} else if ("42S11".equals(state)) {
				// H2 index already exists
				iciqlCode = CODE_OBJECT_ALREADY_EXISTS;
			} else if ("42504".equals(state)) {
				// HSQL index already exists
				iciqlCode = CODE_OBJECT_ALREADY_EXISTS;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		String message = getLocalizedMessage();
		if (message != null) {
			sb.append(": ").append(message);
		}
		if (sql != null) {
			sb.append('\n').append(sql);
		}
		return sb.toString();
	}
}
