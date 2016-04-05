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

/**
 * This class represents "SET column = value" in an UPDATE statement.
 *
 * @param <T> the query type
 * @param <A> the new value data type
 */

public class UpdateColumnSet<T, A> implements UpdateColumn {

    private Query<T> query;
    private A x;
    private A y;
    private boolean isParameter;

    UpdateColumnSet(Query<T> query, A x) {
        this.query = query;
        this.x = x;
    }

    public Query<T> to(A y) {
        query.addUpdateColumnDeclaration(this);
        this.y = y;
        return query;
    }

    public Query<T> toParameter() {
        query.addUpdateColumnDeclaration(this);
        isParameter = true;
        return query;
    }

    public void appendSQL(SQLStatement stat) {
        query.appendSQL(stat, null, x);
        stat.appendSQL(" = ");
        if (isParameter) {
            query.appendSQL(stat, x, RuntimeParameter.PARAMETER);
        } else {
            query.appendSQL(stat, x, y);
        }
    }

}
