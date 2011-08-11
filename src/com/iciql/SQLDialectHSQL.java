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

import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StatementBuilder;

/**
 * HyperSQL database dialect.
 */
public class SQLDialectHSQL extends SQLDialectDefault {
		
	@Override
	public boolean supportsMemoryTables() {
		return true;
	}
		
	@Override
	protected boolean prepareColumnDefinition(StatementBuilder buff, boolean isAutoIncrement, boolean isPrimaryKey) {
		boolean isIdentity = false;
		if (isAutoIncrement && isPrimaryKey) {
			buff.append(" IDENTITY");
			isIdentity = true;
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
			buff.append(col);
		}
		buff.append(")");
		stat.setSQL(buff.toString());
	}
}