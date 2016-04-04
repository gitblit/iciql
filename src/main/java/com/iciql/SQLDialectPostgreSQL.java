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

import com.iciql.TableDefinition.FieldDefinition;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StatementBuilder;

/**
 * PostgreSQL database dialect.
 */
public class SQLDialectPostgreSQL extends SQLDialectDefault {

	@Override
	public Class<? extends java.util.Date> getDateTimeClass() {
		return java.sql.Timestamp.class;
	}

	@Override
	public String convertSqlType(String sqlType) {
		if ("DOUBLE".equals(sqlType)) {
			return "DOUBLE PRECISION";
		} else if ("TINYINT".equals(sqlType)) {
			// PostgreSQL does not have a byte type
			return "SMALLINT";
		} else if ("CLOB".equals(sqlType)) {
			return "TEXT";
		} else if ("BLOB".equals(sqlType)) {
			return "BYTEA";
		}
		return sqlType;
	}

	@Override
	protected boolean prepareColumnDefinition(StatementBuilder buff, String dataType,
			boolean isAutoIncrement, boolean isPrimaryKey) {
		String convertedType = convertSqlType(dataType);
		if (isIntegerType(dataType)) {
			if (isAutoIncrement) {
				if ("BIGINT".equals(dataType)) {
					buff.append("BIGSERIAL");
				} else {
					buff.append("SERIAL");
				}
			} else {
				buff.append(convertedType);
			}
		} else {
			buff.append(convertedType);
		}
		return false;
	}

	@Override
	public void prepareCreateIndex(SQLStatement stat, String schemaName, String tableName,
			IndexDefinition index) {
		StatementBuilder buff = new StatementBuilder();
		buff.append("CREATE ");
		switch (index.type) {
		case UNIQUE:
			buff.append("UNIQUE ");
			break;
		case UNIQUE_HASH:
			buff.append("UNIQUE ");
			break;
		}
		buff.append("INDEX ");
		buff.append(index.indexName);
		buff.append(" ON ");
		buff.append(tableName);

		switch (index.type) {
		case HASH:
			buff.append(" USING HASH");
			break;
		case UNIQUE_HASH:
			buff.append(" USING HASH");
			break;
		}

		buff.append(" (");
		for (String col : index.columnNames) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(col));
		}
		buff.append(") ");

		stat.setSQL(buff.toString().trim());
	}

	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName,
			TableDefinition<T> def, Object obj) {

		FieldDefinition primaryKey = null;
		for (FieldDefinition field : def.fields) {
			if (field.isPrimaryKey) {
				primaryKey = field;
			}
		}

		if (primaryKey == null || databaseVersion < 9.5f) {
			// simulated UPSERT for <= 9.4 release
			super.prepareMerge(stat, schemaName, tableName, def, obj);
			return;
		}

		// official UPSERT added in 9.5 release
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		buff.append(prepareTableName(schemaName, tableName)).append(" (");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(field.columnName);
		}
		buff.resetCount();
		buff.append(") VALUES (");
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append('?');
			Object value = def.getValue(obj, field);
			Object parameter = serialize(value, field.typeAdapter);
			stat.addParameter(parameter);
		}

		buff.append(") ON CONFLICT (");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			if (field.isPrimaryKey) {
				buff.appendExceptFirst(", ");
				buff.append(field.columnName);
			}
		}
		buff.append(") DO UPDATE SET ");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(field.columnName);
			buff.append("=?");
			Object value = def.getValue(obj, field);
			Object parameter = serialize(value, field.typeAdapter);
			stat.addParameter(parameter);
		}
		stat.setSQL(buff.toString());
	}

}