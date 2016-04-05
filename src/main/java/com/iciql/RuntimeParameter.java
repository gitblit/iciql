/*
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
 * A runtime parameter is used to generate x=? conditions so that iciql can
 * build re-usable dynamic queries with parameter substitution done manually at
 * runtime.
 *
 * @param <A> the operand type
 */

class RuntimeParameter<A> implements Token {

    public final static String PARAMETER = "";

    A x;
    CompareType compareType;

    RuntimeParameter(A x, CompareType type) {
        this.x = x;
        this.compareType = type;
    }

    public <T> void appendSQL(SQLStatement stat, Query<T> query) {
        query.appendSQL(stat, null, x);
        stat.appendSQL(" ");
        stat.appendSQL(compareType.getString());
        if (compareType.hasRightExpression()) {
            stat.appendSQL(" ");
            query.appendSQL(stat, x, PARAMETER);
        }
    }
}
