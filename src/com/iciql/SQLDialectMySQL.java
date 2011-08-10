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
 * MySQL database dialect.
 */
public class SQLDialectMySQL extends SQLDialectDefault {

	@Override
	public boolean supportsMemoryTables() {
		return false;
	}

	@Override
	public boolean supportsMerge() {
		return true;
	}

	@Override
	public String prepareColumnName(String name) {
		return "`" + name + "`";
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
		case UNIQUE_HASH:
			buff.append("UNIQUE ");
			break;
		}
		buff.append("INDEX ");
		buff.append(index.indexName);
		buff.append(" ON ");
		buff.append(table);
		buff.append("(");
		for (String col : index.columnNames) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(col));
		}
		buff.append(") ");
		
		// USING
		switch (index.type) {
		case HASH:
			buff.append("USING HASH");
			break;
		case UNIQUE_HASH:
			buff.append("USING HASH");
			break;
		}
		return buff.toString().trim();
	}
	
	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName, TableDefinition<T> def, Object obj) {		
		StatementBuilder buff = new StatementBuilder("REPLACE INTO ");
		buff.append(prepareTableName(schemaName, tableName)).append('(');
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(field.columnName));
		}
		buff.append(") VALUES(");
		buff.resetCount();
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