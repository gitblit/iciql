/*
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
 * This class represents a "between y and z" condition.
 *
 * @param <T> the return type of the query
 * @param <A> the incomplete condition data type
 */
public class QueryBetween<T, A> {

    private Query<T> query;
    private A x;
    private A y;

    /**
     * Construct a between condition.
     *
     * @param query the query
     * @param x     the alias
     * @param y     the lower bound of the between condition
     */
    public QueryBetween(Query<T> query, A x, A y) {
        this.query = query;
        this.x = x;
        this.y = y;
    }

    /**
     * Set the upper bound of the between condition.
     *
     * @param z the upper bound of the between condition
     * @return the query
     */
    public QueryWhere<T> and(A z) {
        query.addConditionToken(new Condition<A>(x, y, z, CompareType.BETWEEN));
        return new QueryWhere<T>(query);
    }
}
