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

import com.iciql.TableDefinition.FieldDefinition;

/**
 * This class represents a column of a table in a query.
 *
 * @param <T> the table data type
 */

class SelectColumn<T> {
    private SelectTable<T> selectTable;
    private FieldDefinition fieldDef;

    SelectColumn(SelectTable<T> table, FieldDefinition fieldDef) {
        this.selectTable = table;
        this.fieldDef = fieldDef;
    }

    void appendSQL(SQLStatement stat) {
        if (selectTable.getQuery().isJoin()) {
            stat.appendSQL(selectTable.getAs() + "." + fieldDef.columnName);
        } else {
            stat.appendColumn(fieldDef.columnName);
        }
    }

    FieldDefinition getFieldDefinition() {
        return fieldDef;
    }

    SelectTable<T> getSelectTable() {
        return selectTable;
    }

    Object getCurrentValue() {
        return fieldDef.getValue(selectTable.getCurrent());
    }
}
