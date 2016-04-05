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
import com.iciql.TableInspector.ColumnInspector;
import com.iciql.util.StringUtils;

/**
 * A validation remark is a result of running a model validation. Each remark
 * has a level, associated component (schema, table, column, index), and a
 * message.
 */
public class ValidationRemark {

    /**
     * The validation message level.
     */
    public static enum Level {
        CONSIDER, WARN, ERROR;
    }

    public final Level level;
    public final String table;
    public final String fieldType;
    public final String fieldName;
    public final String message;

    private ValidationRemark(Level level, String table, String type, String message) {
        this.level = level;
        this.table = table;
        this.fieldType = type;
        this.fieldName = "";
        this.message = message;
    }

    private ValidationRemark(Level level, String table, FieldDefinition field, String message) {
        this.level = level;
        this.table = table;
        this.fieldType = field.dataType;
        this.fieldName = field.columnName;
        this.message = message;
    }

    private ValidationRemark(Level level, String table, ColumnInspector col, String message) {
        this.level = level;
        this.table = table;
        this.fieldType = col.type;
        this.fieldName = col.name;
        this.message = message;
    }

    public static ValidationRemark consider(String table, String type, String message) {
        return new ValidationRemark(Level.CONSIDER, table, type, message);
    }

    public static ValidationRemark consider(String table, ColumnInspector col, String message) {
        return new ValidationRemark(Level.CONSIDER, table, col, message);
    }

    public static ValidationRemark warn(String table, ColumnInspector col, String message) {
        return new ValidationRemark(Level.WARN, table, col, message);
    }

    public static ValidationRemark warn(String table, String type, String message) {
        return new ValidationRemark(Level.WARN, table, type, message);
    }

    public static ValidationRemark error(String table, ColumnInspector col, String message) {
        return new ValidationRemark(Level.ERROR, table, col, message);
    }

    public static ValidationRemark error(String table, String type, String message) {
        return new ValidationRemark(Level.ERROR, table, type, message);
    }

    public static ValidationRemark error(String table, FieldDefinition field, String message) {
        return new ValidationRemark(Level.ERROR, table, field, message);
    }

    public ValidationRemark throwError(boolean throwOnError) {
        if (throwOnError && isError()) {
            throw new IciqlException(toString());
        }
        return this;
    }

    public boolean isError() {
        return level.equals(Level.ERROR);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.pad(level.name(), 9, " ", true));
        sb.append(StringUtils.pad(table, 25, " ", true));
        sb.append(StringUtils.pad(fieldName, 20, " ", true));
        sb.append(' ');
        sb.append(message);
        return sb.toString();
    }

    public String toCSVString() {
        StringBuilder sb = new StringBuilder();
        sb.append(level.name()).append(',');
        sb.append(table).append(',');
        sb.append(fieldType).append(',');
        sb.append(fieldName).append(',');
        sb.append(message);
        return sb.toString();
    }

}
