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

import com.iciql.util.Utils;

import java.util.ArrayList;

/**
 * This class represents a table in a query.
 *
 * @param <T> the table class
 */

class SelectTable<T> {

    private Query<T> query;
    private Class<T> clazz;
    private T current;
    private String as;
    private TableDefinition<T> aliasDef;
    private boolean outerJoin;
    private ArrayList<Token> joinConditions = Utils.newArrayList();
    private T alias;

    @SuppressWarnings("unchecked")
    SelectTable(Db db, Query<T> query, T alias, boolean outerJoin) {
        this.alias = alias;
        this.query = query;
        this.outerJoin = outerJoin;
        aliasDef = (TableDefinition<T>) db.getTableDefinition(alias.getClass());
        clazz = Utils.getClass(alias);
        as = "T" + Utils.nextAsCount();
    }

    T getAlias() {
        return alias;
    }

    T newObject() {
        return Utils.newObject(clazz);
    }

    TableDefinition<T> getAliasDefinition() {
        return aliasDef;
    }

    void appendSQL(SQLStatement stat) {
        if (query.isJoin()) {
            stat.appendTable(aliasDef.schemaName, aliasDef.tableName).appendSQL(" AS " + as);
        } else {
            stat.appendTable(aliasDef.schemaName, aliasDef.tableName);
        }
    }

    void appendSQLAsJoin(SQLStatement stat, Query<T> q) {
        if (outerJoin) {
            stat.appendSQL(" LEFT OUTER JOIN ");
        } else {
            stat.appendSQL(" INNER JOIN ");
        }
        appendSQL(stat);
        if (!joinConditions.isEmpty()) {
            stat.appendSQL(" ON ");
            for (Token token : joinConditions) {
                token.appendSQL(stat, q);
                stat.appendSQL(" ");
            }
        }
    }

    boolean getOuterJoin() {
        return outerJoin;
    }

    Query<T> getQuery() {
        return query;
    }

    String getAs() {
        return as;
    }

    void addConditionToken(Token condition) {
        joinConditions.add(condition);
    }

    T getCurrent() {
        return current;
    }

    void setCurrent(T current) {
        this.current = current;
    }

}
