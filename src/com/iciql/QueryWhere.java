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

	/**
	 * Specify an AND condition with a mapped primitive boolean.
	 * 
	 * @param x
	 *            the primitive boolean field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Boolean> and(boolean x) {
		query.getFrom().getAliasDefinition().checkMultipleBooleans();
		return addPrimitive(ConditionAndOr.AND, x);
	}

	/**
	 * Specify an AND condition with a mapped primitive byte.
	 * 
	 * @param x
	 *            the primitive byte field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Byte> and(byte x) {
		return addPrimitive(ConditionAndOr.AND, x);
	}

	/**
	 * Specify an AND condition with a mapped primitive short.
	 * 
	 * @param x
	 *            the primitive short field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Short> and(short x) {
		return addPrimitive(ConditionAndOr.AND, x);
	}

	/**
	 * Specify an AND condition with a mapped primitive int.
	 * 
	 * @param x
	 *            the primitive int field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Integer> and(int x) {
		return addPrimitive(ConditionAndOr.AND, x);
	}

	/**
	 * Specify an AND condition with a mapped primitive long.
	 * 
	 * @param x
	 *            the primitive long field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Long> and(long x) {
		return addPrimitive(ConditionAndOr.AND, x);
	}

	/**
	 * Specify an AND condition with a mapped primitive float.
	 * 
	 * @param x
	 *            the primitive float field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Float> and(float x) {
		return addPrimitive(ConditionAndOr.AND, x);
	}

	/**
	 * Specify an AND condition with a mapped primitive double.
	 * 
	 * @param x
	 *            the primitive double field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Double> and(double x) {
		return addPrimitive(ConditionAndOr.AND, x);
	}

	private <A> QueryCondition<T, A> addPrimitive(ConditionAndOr condition, A x) {		
		query.addConditionToken(condition);
		A alias = query.getPrimitiveAliasByValue(x);
		if (alias == null) {
			// this will result in an unmapped field exception
			return new QueryCondition<T, A>(query, x);
		}
		return new QueryCondition<T, A>(query, alias);
	}

	/**
	 * Specify an AND condition with a mapped Object field.
	 * 
	 * @param x
	 *            the Object field to query
	 * @return a query condition to continue building the condition
	 */
	public <A> QueryCondition<T, A> and(A x) {
		query.addConditionToken(ConditionAndOr.AND);
		return new QueryCondition<T, A>(query, x);
	}

	/**
	 * Specify an OR condition with a mapped primitive boolean.
	 * 
	 * @param x
	 *            the primitive boolean field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Boolean> or(boolean x) {
		query.getFrom().getAliasDefinition().checkMultipleBooleans();
		return addPrimitive(ConditionAndOr.OR, x);
	}

	/**
	 * Specify an OR condition with a mapped primitive byte.
	 * 
	 * @param x
	 *            the primitive byte field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Byte> or(byte x) {
		return addPrimitive(ConditionAndOr.OR, x);
	}

	/**
	 * Specify an OR condition with a mapped primitive short.
	 * 
	 * @param x
	 *            the primitive short field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Short> or(short x) {
		return addPrimitive(ConditionAndOr.OR, x);
	}

	/**
	 * Specify an OR condition with a mapped primitive int.
	 * 
	 * @param x
	 *            the primitive int field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Integer> or(int x) {
		return addPrimitive(ConditionAndOr.OR, x);
	}

	/**
	 * Specify an OR condition with a mapped primitive long.
	 * 
	 * @param x
	 *            the primitive long field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Long> or(long x) {
		return addPrimitive(ConditionAndOr.OR, x);
	}

	/**
	 * Specify an OR condition with a mapped primitive float.
	 * 
	 * @param x
	 *            the primitive float field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Float> or(float x) {
		return addPrimitive(ConditionAndOr.OR, x);
	}

	/**
	 * Specify an OR condition with a mapped primitive double.
	 * 
	 * @param x
	 *            the primitive double field to query
	 * @return a query condition to continue building the condition
	 */
	public QueryCondition<T, Double> or(double x) {
		return addPrimitive(ConditionAndOr.OR, x);
	}

	/**
	 * Specify an OR condition with a mapped Object field.
	 * 
	 * @param x
	 *            the Object field to query
	 * @return a query condition to continue building the condition
	 */
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
	 * Order by primitive boolean field
	 * 
	 * @param field
	 *            a primitive boolean field
	 * @return the query
	 */
	public QueryWhere<T> orderBy(boolean field) {
		query.getFrom().getAliasDefinition().checkMultipleBooleans();
		return orderByPrimitive(field);
	}

	/**
	 * Order by primitive byte field
	 * 
	 * @param field
	 *            a primitive byte field
	 * @return the query
	 */
	public QueryWhere<T> orderBy(byte field) {
		return orderByPrimitive(field);
	}

	/**
	 * Order by primitive short field
	 * 
	 * @param field
	 *            a primitive short field
	 * @return the query
	 */
	public QueryWhere<T> orderBy(short field) {
		return orderByPrimitive(field);
	}

	public QueryWhere<T> orderBy(int field) {
		return orderByPrimitive(field);
	}

	/**
	 * Order by primitive long field
	 * 
	 * @param field
	 *            a primitive long field
	 * @return the query
	 */
	public QueryWhere<T> orderBy(long field) {
		return orderByPrimitive(field);
	}

	/**
	 * Order by primitive float field
	 * 
	 * @param field
	 *            a primitive float field
	 * @return the query
	 */
	public QueryWhere<T> orderBy(float field) {
		return orderByPrimitive(field);
	}

	/**
	 * Order by primitive double field
	 * 
	 * @param field
	 *            a primitive double field
	 * @return the query
	 */
	public QueryWhere<T> orderBy(double field) {
		return orderByPrimitive(field);
	}

	private QueryWhere<T> orderByPrimitive(Object field) {
		query.orderByPrimitive(field);
		return this;
	}

	public QueryWhere<T> orderBy(Object field) {
		query.orderBy(field);
		return this;
	}

	/**
	 * Order by a number of Object columns.
	 * 
	 * @param expressions
	 *            the order by expressions
	 * @return the query
	 */

	public QueryWhere<T> orderBy(Object... expressions) {
		query.orderBy(expressions);
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
