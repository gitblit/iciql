/*
 * Copyright 2017 James Moger.
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
 * A bitwise condition contains two operands, a bit operation, and a comparison test.
 *
 * @param <A> the operand type
 */

class BitCondition<A, T> implements Token, Bitwise<A, T> {

    enum Bitwise {
        AND, XOR
    }

    Bitwise bitwiseType;
    CompareType compareType;
    A x, y, z;
    Query<T> query;

    BitCondition(A x, A y, Bitwise bitwiseType, Query<T> query) {
        this.bitwiseType = bitwiseType;
        this.x = x;
        this.y = y;
        this.query = query;
    }

    public QueryWhere<T> exceeds(A y) {
        z = y;
        compareType = CompareType.EXCEEDS;
        return new QueryWhere<T>(query);
    }

    public QueryWhere<T> atLeast(A y) {
        z = y;
        compareType = CompareType.AT_LEAST;
        return new QueryWhere<T>(query);
    }

    public QueryWhere<T> lessThan(A y) {
        z = y;
        compareType = CompareType.LESS_THAN;
        return new QueryWhere<T>(query);
    }

    public QueryWhere<T> atMost(A y) {
        z = y;
        compareType = CompareType.AT_MOST;
        return new QueryWhere<T>(query);
    }

    @SuppressWarnings("unchecked")
    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        stat.appendSQL("(");
        switch (bitwiseType) {
            case AND:
                query.getDb().getDialect().prepareBitwiseAnd(stat, query, x, y);
                break;
            case XOR:
                query.getDb().getDialect().prepareBitwiseXor(stat, query, x, y);
                break;
        }
        stat.appendSQL(")");
        stat.appendSQL(compareType.getString());
        stat.appendSQL("" + z);
    }
}
