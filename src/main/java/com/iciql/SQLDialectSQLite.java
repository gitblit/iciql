/*
 * Copyright 2014 James Moger.
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

import com.iciql.Iciql.DataTypeAdapter;
import com.iciql.TableDefinition.FieldDefinition;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.IciqlLogger;
import com.iciql.util.StatementBuilder;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;


/**
 * SQLite database dialect.
 */
public class SQLDialectSQLite extends SQLDialectDefault {

    @Override
    public boolean supportsSavePoints() {
        // SAVEPOINT support was added after the 3.8.7 release
        String[] chunks = productVersion.split("\\.");
        if (Integer.parseInt(chunks[0]) > 3) {
            return true;
        }
        float f = Float.parseFloat(chunks[1] + "." + chunks[2]);
        return (f > 8.7);
    }

    @Override
    protected <T> String prepareCreateTable(TableDefinition<T> def) {
        return "CREATE TABLE IF NOT EXISTS";
    }

    @Override
    protected <T> String prepareCreateView(TableDefinition<T> def) {
        return "CREATE VIEW IF NOT EXISTS";
    }

    @Override
    public String convertSqlType(String sqlType) {
        if (isIntegerType(sqlType)) {
            return "INTEGER";
        }
        return sqlType;
    }

    @Override
    protected boolean prepareColumnDefinition(StatementBuilder buff, String dataType,
                                              boolean isAutoIncrement, boolean isPrimaryKey) {
        String convertedType = convertSqlType(dataType);
        buff.append(convertedType);
        if (isPrimaryKey) {
            buff.append(" PRIMARY KEY");
            if (isAutoIncrement) {
                buff.append(" AUTOINCREMENT");
            }
            return true;
        }
        return false;
    }

    @Override
    public <T> void prepareDropView(SQLStatement stat, TableDefinition<T> def) {
        StatementBuilder buff = new StatementBuilder("DROP VIEW IF EXISTS "
                + prepareTableName(def.schemaName, def.tableName));
        stat.setSQL(buff.toString());
        return;
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
            default:
                IciqlLogger.warn("{0} does not support hash indexes", getClass().getSimpleName());
        }
        buff.append("INDEX IF NOT EXISTS ");
        buff.append(index.indexName);
        buff.append(" ON ");
        buff.append(tableName);
        buff.append("(");
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
        StatementBuilder buff = new StatementBuilder("INSERT OR REPLACE INTO ");
        buff.append(prepareTableName(schemaName, tableName)).append(" (");
        buff.resetCount();
        for (FieldDefinition field : def.fields) {
            buff.appendExceptFirst(", ");
            buff.append(field.columnName);
        }
        buff.append(") ");
        buff.resetCount();
        buff.append("VALUES (");
        for (FieldDefinition field : def.fields) {
            buff.appendExceptFirst(", ");
            buff.append('?');
            Object value = def.getValue(obj, field);
            Object parameter = serialize(value, field.typeAdapter);
            stat.addParameter(parameter);
        }
        buff.append(')');
        stat.setSQL(buff.toString());
    }

    @Override
    public Object deserialize(ResultSet rs, int columnIndex, Class<?> targetType, Class<? extends DataTypeAdapter<?>> typeAdapter) {
        try {
            return super.deserialize(rs, columnIndex, targetType, typeAdapter);
        } catch (IciqlException e) {
            if (typeAdapter == null && e.getMessage().startsWith("Can not convert")) {
                try {
                    // give the SQLite JDBC driver an opportunity to deserialize DateTime objects
                    if (Timestamp.class.equals(targetType)) {
                        return rs.getTimestamp(columnIndex);
                    } else if (Time.class.equals(targetType)) {
                        return rs.getTime(columnIndex);
                    } else if (Date.class.equals(targetType)) {
                        return rs.getDate(columnIndex);
                    } else if (java.util.Date.class.equals(targetType)) {
                        Timestamp timestamp = rs.getTimestamp(columnIndex);
                        return new java.util.Date(timestamp.getTime());
                    }
                } catch (SQLException x) {
                    throw new IciqlException(x, "Can not convert the value at column {0} to {1}",
                            columnIndex, targetType.getName());
                }
            }

            // rethrow e
            throw e;
        }
    }

    @Override
    public String prepareStringParameter(Object o) {
        if (o instanceof Boolean) {
            // SQLite does not have an explicit BOOLEAN type
            Boolean bool = (Boolean) o;
            return bool ? "1" : "0";
        }
        return super.prepareStringParameter(o);
    }
}
