/*
 * Copyright 2012 Alex Telepov.
 * Copyright 2012 James Moger.
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

/**
 * MS SQL Server database dialect.
 */
public class SQLDialectMSSQL extends SQLDialectDefault {

    @Override
    public String extractColumnName(String name) {
        return super.extractColumnName(name).replace('[', ' ').replace(']', ' ').trim();
    }

    /**
     * Append limit and offset rows
     *
     * @param stat   Statement
     * @param limit  Limit rows
     * @param offset Offset rows
     */
    @Override
    public void appendLimitOffset(SQLStatement stat, long limit, long offset) {
        if (offset > 0) {
            throw new IciqlException("iciql does not support offset for MSSQL dialect!");
        }
        StringBuilder query = new StringBuilder(stat.getSQL());

        // for databaseVersion >= 2012 need Offset
        if (limit > 0) {
            int indexSelect = query.indexOf("SELECT");

            if (indexSelect >= 0) {
                StringBuilder subPathQuery = new StringBuilder(" TOP ");
                subPathQuery.append(Long.toString(limit));

                query.insert(indexSelect + "SELECT".length(), subPathQuery);

                stat.setSQL(query.toString());
            }
        }
    }
}
