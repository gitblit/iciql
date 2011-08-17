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

import java.text.MessageFormat;

import com.iciql.TableDefinition.FieldDefinition;
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
	protected boolean prepareColumnDefinition(StatementBuilder buff, String dataType, boolean isAutoIncrement,
			boolean isPrimaryKey) {
		boolean isIdentity = false;
		String convertedType = convertSqlType(dataType);
		buff.append(convertedType);
		if (isIntegerType(dataType) && isAutoIncrement && isPrimaryKey) {
			buff.append(" IDENTITY");
			isIdentity = true;
		}
		return isIdentity;
	}

	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName,
			TableDefinition<T> def, Object obj) {
		final String valuePrefix = "v";
		StatementBuilder buff = new StatementBuilder("MERGE INTO ");
		buff.append(prepareTableName(schemaName, tableName));
		// a, b, c....
		buff.append(" USING (VALUES(");
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append("CAST(? AS ");
			String dataType = convertSqlType(field.dataType);
			buff.append(dataType);
			if ("VARCHAR".equals(dataType)) {
				if (field.length > 0) {
					// VARCHAR(x)
					buff.append(MessageFormat.format("({0})", field.length));
				}
			} else if ("DECIMAL".equals(dataType)) {
				if (field.length > 0) {
					if (field.scale > 0) {
						// DECIMAL(x,y)
						buff.append(MessageFormat.format("({0},{1})", field.length, field.scale));
					} else {
						// DECIMAL(x)
						buff.append(MessageFormat.format("({0})", field.length));
					}
				}
			}
			buff.append(')');
			Object value = def.getValue(obj, field);
			stat.addParameter(value);
		}

		// map to temporary table
		buff.resetCount();
		buff.append(")) AS vals (");
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(valuePrefix + field.columnName));
		}

		buff.append(") ON ");

		// create the ON condition
		// (va, vb) = (va,vb)
		String[] prefixes = { "", valuePrefix };
		for (int i = 0; i < prefixes.length; i++) {
			String prefix = prefixes[i];
			buff.resetCount();
			buff.append('(');
			for (FieldDefinition field : def.fields) {
				if (field.isPrimaryKey) {
					buff.appendExceptFirst(", ");
					buff.append(prepareColumnName(prefix + field.columnName));
				}
			}
			buff.append(")");
			if (i == 0) {
				buff.append('=');
			}
		}

		// UPDATE
		// set a=va
		buff.append(" WHEN MATCHED THEN UPDATE SET ");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(field.columnName));
			buff.append('=');
			buff.append(prepareColumnName(valuePrefix + field.columnName));
		}

		// INSERT
		// insert va, vb, vc....
		buff.append(" WHEN NOT MATCHED THEN INSERT ");
		buff.resetCount();
		buff.append(" VALUES (");
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(valuePrefix + field.columnName));
		}
		buff.append(')');
		stat.setSQL(buff.toString());
	}
}