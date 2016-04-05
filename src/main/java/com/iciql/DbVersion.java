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

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

/**
 * A system table to track database and table versions.
 */
@IQTable(name = "iq_versions", primaryKey = {"schemaName", "tableName"}, memoryTable = true)
public class DbVersion {

    @IQColumn(length = 255)
    String schemaName = "";

    @IQColumn(length = 255)
    String tableName = "";

    @IQColumn
    Integer version;

    public DbVersion() {
        // nothing to do
    }

    /**
     * Constructor for defining a version entry. Both the schema and the table
     * are empty strings, which means this is the row for the 'database'.
     *
     * @param version the database version
     */
    public DbVersion(int version) {
        this.schemaName = "";
        this.tableName = "";
        this.version = version;
    }

}
