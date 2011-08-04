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

import java.util.List;

/**
 * This class represents a query with a condition.
 * 
 * @param <T>
 *            the return type
 */

public class QueryWhere<T> {

	Query<T> query;

	QueryWhere(Query<T> query) {
		this.query = query;
	}

	public <A> QueryCondition<T, A> and(A x) {
		query.addConditionToken(ConditionAndOr.AND);
		return new QueryCondition<T, A>(query, x);
	}

	public <A> QueryCondition<T, A> or(A x) {
		query.addConditionToken(ConditionAndOr.OR);
		return new QueryCondition<T, A>(query, x);
	}

	public QueryWhere<T> limit(long limit) {
		query.limit(limit);
		return this;
	}

	public QueryWhere<T> offset(long offset) {
		query.offset(offset);
		return this;
	}

	public <X, Z> List<X> select(Z x) {
		return query.select(x);
	}

	public String getSQL() {
		SQLStatement stat = new SQLStatement(query.getDb());
		stat.appendSQL("SELECT *");
		query.appendFromWhere(stat);
		return stat.getSQL().trim();
	}

	public <X, Z> List<X> selectDistinct(Z x) {
		return query.selectDistinct(x);
	}

	public <X, Z> X selectFirst(Z x) {
		List<X> list = query.select(x);
		return list.isEmpty() ? null : list.get(0);
	}

	public List<T> select() {
		return query.select();
	}

	public T selectFirst() {
		List<T> list = select();
		return list.isEmpty() ? null : list.get(0);
	}

	public List<T> selectDistinct() {
		return query.selectDistinct();
	}

	/**
	 * Order by a number of columns.
	 * 
	 * @param expressions
	 *            the order by expressions
	 * @return the query
	 */

	public QueryWhere<T> orderBy(Object... expressions) {
		for (Object expr : expressions) {
			OrderExpression<T> e = new OrderExpression<T>(query, expr, false, false, false);
			query.addOrderBy(e);
		}
		return this;
	}

	public QueryWhere<T> orderByNullsFirst(Object expr) {
		OrderExpression<T> e = new OrderExpression<T>(query, expr, false, true, false);
		query.addOrderBy(e);
		return this;
	}

	public QueryWhere<T> orderByNullsLast(Object expr) {
		OrderExpression<T> e = new OrderExpression<T>(query, expr, false, false, true);
		query.addOrderBy(e);
		return this;
	}

	public QueryWhere<T> orderByDesc(Object expr) {
		OrderExpression<T> e = new OrderExpression<T>(query, expr, true, false, false);
		query.addOrderBy(e);
		return this;
	}

	public QueryWhere<T> orderByDescNullsFirst(Object expr) {
		OrderExpression<T> e = new OrderExpression<T>(query, expr, true, true, false);
		query.addOrderBy(e);
		return this;
	}

	public QueryWhere<T> orderByDescNullsLast(Object expr) {
		OrderExpression<T> e = new OrderExpression<T>(query, expr, true, false, true);
		query.addOrderBy(e);
		return this;
	}

	public int delete() {
		return query.delete();
	}

	public int update() {
		return query.update();
	}

	public long selectCount() {
		return query.selectCount();
	}

}
