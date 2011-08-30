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

import com.iciql.Iciql.IndexType;

/**
 * This class provides utility methods to define primary keys, indexes, and set
 * the name of the table.
 */

public class Define {

	private static TableDefinition<?> currentTableDefinition;
	private static Iciql currentTable;

	public static void skipCreate() {
		checkInDefine();
		currentTableDefinition.defineSkipCreate();
	}

	public static void index(IndexType type, Object... columns) {
		checkInDefine();
		currentTableDefinition.defineIndex(null, type, columns);
	}

	public static void index(String name, IndexType type, Object... columns) {
		checkInDefine();
		currentTableDefinition.defineIndex(name, type, columns);
	}

	public static void primaryKey(Object... columns) {
		checkInDefine();
		currentTableDefinition.definePrimaryKey(columns);
	}
	
	public static void schemaName(String schemaName) {
		checkInDefine();
		currentTableDefinition.defineSchemaName(schemaName);
	}

	public static void tableName(String tableName) {
		checkInDefine();
		currentTableDefinition.defineTableName(tableName);
	}

	public static void memoryTable() {
		checkInDefine();
		currentTableDefinition.defineMemoryTable();
	}

	public static void columnName(Object column, String columnName) {
		checkInDefine();
		currentTableDefinition.defineColumnName(column, columnName);
	}

	public static void autoIncrement(Object column) {
		checkInDefine();
		currentTableDefinition.defineAutoIncrement(column);
	}

	public static void length(Object column, int length) {
		checkInDefine();
		currentTableDefinition.defineLength(column, length);
	}

	public static void scale(Object column, int scale) {
		checkInDefine();
		currentTableDefinition.defineScale(column, scale);
	}
	
	public static void trim(Object column) {
		checkInDefine();
		currentTableDefinition.defineTrim(column);
	}
	
	public static void nullable(Object column, boolean isNullable) {
		checkInDefine();
		currentTableDefinition.defineNullable(column, isNullable);
	}
	
	public static void defaultValue(Object column, String defaultValue) {
		checkInDefine();
		currentTableDefinition.defineDefaultValue(column, defaultValue);
	}
	
	static synchronized <T> void define(TableDefinition<T> tableDefinition, Iciql table) {
		currentTableDefinition = tableDefinition;
		currentTable = table;
		tableDefinition.mapObject(table);
		table.defineIQ();
		currentTable = null;
		currentTableDefinition = null;
	}

	private static void checkInDefine() {
		if (currentTable == null) {
			throw new IciqlException("This method may only be called "
					+ "from within the define() method, and the define() method "
					+ "is called by the framework.");
		}
	}

}
