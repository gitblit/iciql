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
 * H2 database dialect.
 */
public class SQLDialectH2 extends SQLDialectDefault {

	@Override
	public boolean supportsMemoryTables() {
		return true;
	}

	@Override
	protected boolean prepareColumnDefinition(StatementBuilder buff, String dataType, boolean isAutoIncrement,
			boolean isPrimaryKey) {
		String convertedType = convertSqlType(dataType);
		boolean isIdentity = false;
		if (isIntegerType(dataType)) {
			if (isAutoIncrement && isPrimaryKey) {
				buff.append("IDENTITY");
				isIdentity =  true;
			} else if (isAutoIncrement) {
				buff.append(convertedType);
				buff.append(" AUTO_INCREMENT");
			} else {
				buff.append(convertedType);	
			}
		} else {
			buff.append(convertedType);
		}
		return isIdentity;
	}

	@Override
	public void prepareCreateIndex(SQLStatement stat, String schema, String table, IndexDefinition index) {
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
		stat.setSQL(buff.toString());
	}

	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName,
			TableDefinition<T> def, Object obj) {
		StatementBuilder buff = new StatementBuilder("MERGE INTO ");
		buff.append(prepareTableName(schemaName, tableName)).append(" (");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(field.columnName);
		}
		buff.append(") KEY(");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			if (field.isPrimaryKey) {
				buff.appendExceptFirst(", ");
				buff.append(field.columnName);
			}
		}
		buff.append(") ");
		buff.resetCount();
		buff.append("VALUES (");
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append('?');
			Object value = def.getValue(obj, field);
			stat.addParameter(value);
		}
		buff.append(')');
		stat.setSQL(buff.toString());
	}
}