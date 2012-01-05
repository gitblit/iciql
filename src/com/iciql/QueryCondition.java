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
 * This class represents a query with an incomplete condition.
 * 
 * @param <T>
 *            the return type of the query
 * @param <A>
 *            the incomplete condition data type
 */

public class QueryCondition<T, A> {

	private Query<T> query;
	private A x;

	QueryCondition(Query<T> query, A x) {
		this.query = query;
		this.x = x;
	}

	public QueryWhere<T> is(A y) {
		query.addConditionToken(new Condition<A>(x, y, CompareType.EQUAL));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> isNot(A y) {
		query.addConditionToken(new Condition<A>(x, y, CompareType.NOT_EQUAL));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> isNull() {
		query.addConditionToken(new Condition<A>(x, null, CompareType.IS_NULL));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> isNotNull() {
		query.addConditionToken(new Condition<A>(x, null, CompareType.IS_NOT_NULL));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> exceeds(A y) {
		query.addConditionToken(new Condition<A>(x, y, CompareType.EXCEEDS));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> atLeast(A y) {
		query.addConditionToken(new Condition<A>(x, y, CompareType.AT_LEAST));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> lessThan(A y) {
		query.addConditionToken(new Condition<A>(x, y, CompareType.LESS_THAN));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> atMost(A y) {
		query.addConditionToken(new Condition<A>(x, y, CompareType.AT_MOST));
		return new QueryWhere<T>(query);
	}

	public QueryBetween<T, A> between(A y) {
		return new QueryBetween<T, A>(query, x, y);
	}

	public QueryWhere<T> like(A pattern) {
		query.addConditionToken(new Condition<A>(x, pattern, CompareType.LIKE));
		return new QueryWhere<T>(query);
	}

	/*
	 * These method allows you to generate "x=?", "x!=?", etc where conditions.
	 * Parameter substitution must be done manually later with db.executeQuery.
	 * This allows for building re-usable SQL string statements from your model
	 * classes.
	 */
	public QueryWhere<T> isParameter() {
		query.addConditionToken(new RuntimeParameter<A>(x, CompareType.EQUAL));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> isNotParameter() {
		query.addConditionToken(new RuntimeParameter<A>(x, CompareType.NOT_EQUAL));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> exceedsParameter() {
		query.addConditionToken(new RuntimeParameter<A>(x, CompareType.EXCEEDS));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> lessThanParameter() {
		query.addConditionToken(new RuntimeParameter<A>(x, CompareType.LESS_THAN));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> atMostParameter() {
		query.addConditionToken(new RuntimeParameter<A>(x, CompareType.AT_MOST));
		return new QueryWhere<T>(query);
	}

	public QueryWhere<T> likeParameter() {
		query.addConditionToken(new RuntimeParameter<A>(x, CompareType.LIKE));
		return new QueryWhere<T>(query);
	}
}
