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

import com.iciql.Conditions.And;
import com.iciql.Conditions.Or;

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
		query.getFrom().getAliasDefinition().checkMultipleEnums(x);
		query.addConditionToken(ConditionAndOr.AND);
		return new QueryCondition<T, A>(query, x);
	}

	public QueryWhere<T> and(And<T> conditions) {
		andOpenTrue();
		query.addConditionToken(conditions.where.query);
		return close();
	}

	public QueryWhere<T> and(Or<T> conditions) {
		andOpenFalse();
		query.addConditionToken(conditions.where.query);
		return close();
	}

	public QueryWhere<T> andOpenTrue() {
		return open(ConditionAndOr.AND, true);
	}

	public QueryWhere<T> andOpenFalse() {
		return open(ConditionAndOr.AND, false);
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
		query.getFrom().getAliasDefinition().checkMultipleEnums(x);
		query.addConditionToken(ConditionAndOr.OR);
		return new QueryCondition<T, A>(query, x);
	}

	public QueryWhere<T> or(And<T> conditions) {
		orOpenTrue();
		query.addConditionToken(conditions.where.query);
		return close();
	}

	public QueryWhere<T> or(Or<T> conditions) {
		orOpenFalse();
		query.addConditionToken(conditions.where.query);
		return close();
	}

	public QueryWhere<T> orOpenTrue() {
		return open(ConditionAndOr.OR, true);
	}

	public QueryWhere<T> orOpenFalse() {
		return open(ConditionAndOr.OR, false);
	}

	private QueryWhere<T> open(ConditionAndOr andOr, Boolean condition) {
		query.addConditionToken(andOr);
		query.addConditionToken(ConditionOpenClose.OPEN);
		query.addConditionToken(new Function("", condition));
		return this;
	}

	public QueryWhere<T> close() {
		query.addConditionToken(ConditionOpenClose.CLOSE);
		return this;
	}

	public QueryWhere<T> limit(long limit) {
		query.limit(limit);
		return this;
	}

	public QueryWhere<T> offset(long offset) {
		query.offset(offset);
		return this;
	}

	public String getSQL() {
		SQLStatement stat = new SQLStatement(query.getDb());
		stat.appendSQL("SELECT *");
		query.appendFromWhere(stat);
		return stat.getSQL().trim();
	}

	/**
	 * toSQL returns a static string version of the query with runtime variables
	 * properly encoded. This method is also useful when combined with the where
	 * clause methods like isParameter() or atLeastParameter() which allows
	 * iciql to generate re-usable parameterized string statements.
	 * 
	 * @return the sql query as plain text
	 */
	public String toSQL() {
		return query.toSQL(false);
	}

	/**
	 * toSQL returns a static string version of the query with runtime variables
	 * properly encoded. This method is also useful when combined with the where
	 * clause methods like isParameter() or atLeastParameter() which allows
	 * iciql to generate re-usable parameterized string statements.
	 * 
	 * @param distinct
	 *            if true SELECT DISTINCT is used for the query
	 * @return the sql query as plain text
	 */
	public String toSQL(boolean distinct) {
		return query.toSQL(distinct);
	}

	/**
	 * toSQL returns a static string version of the query with runtime variables
	 * properly encoded. This method is also useful when combined with the where
	 * clause methods like isParameter() or atLeastParameter() which allows
	 * iciql to generate re-usable parameterized string statements.
	 * 
	 * @param distinct
	 *            if true SELECT DISTINCT is used for the query
	 * @param k
	 *            k is used to select only the columns of the specified alias
	 *            for an inner join statement. An example of a generated
	 *            statement is: SELECT DISTINCT t1.* FROM sometable AS t1 INNER
	 *            JOIN othertable AS t2 ON t1.id = t2.id WHERE t2.flag = true
	 *            without the alias parameter the statement would start with
	 *            SELECT DISTINCT * FROM...
	 * @return the sql query as plain text
	 */
	public <K> String toSQL(boolean distinct, K k) {
		return query.toSQL(distinct, k);
	}
	
	public <Z> SubQuery<T, Z> subQuery(Z x) {
		return new SubQuery<T, Z>(query, x);
	}

	public SubQuery<T, Boolean> subQuery(boolean x) {		
		return subQuery(query.getPrimitiveAliasByValue(x));
	}

	public SubQuery<T, Byte> subQuery(byte x) {
		return subQuery(query.getPrimitiveAliasByValue(x));
	}

	public SubQuery<T, Short> subQuery(short x) {
		return subQuery(query.getPrimitiveAliasByValue(x));
	}

	public SubQuery<T, Integer> subQuery(int x) {
		return subQuery(query.getPrimitiveAliasByValue(x));
	}

	public SubQuery<T, Long> subQuery(long x) {
		return subQuery(query.getPrimitiveAliasByValue(x));
	}

	public SubQuery<T, Float> subQuery(float x) {
		return subQuery(query.getPrimitiveAliasByValue(x));
	}

	public SubQuery<T, Double> subQuery(double x) {
		return subQuery(query.getPrimitiveAliasByValue(x));
	}
	
	public <X, Z> List<X> select(Z x) {
		return query.select(x);
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
	
	public void createView(Class<?> viewClass) {
		query.createView(viewClass);
	}

	public void replaceView(Class<?> viewClass) {
		query.replaceView(viewClass);
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
		query.getFrom().getAliasDefinition().checkMultipleEnums(field);
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
		query.getFrom().getAliasDefinition().checkMultipleEnums(expr);
		OrderExpression<T> e = new OrderExpression<T>(query, expr, false, true, false);
		query.addOrderBy(e);
		return this;
	}

	public QueryWhere<T> orderByNullsLast(Object expr) {
		query.getFrom().getAliasDefinition().checkMultipleEnums(expr);
		OrderExpression<T> e = new OrderExpression<T>(query, expr, false, false, true);
		query.addOrderBy(e);
		return this;
	}

	public QueryWhere<T> orderByDesc(Object expr) {
		query.getFrom().getAliasDefinition().checkMultipleEnums(expr);
		OrderExpression<T> e = new OrderExpression<T>(query, expr, true, false, false);
		query.addOrderBy(e);
		return this;
	}

	public QueryWhere<T> orderByDescNullsFirst(Object expr) {
		query.getFrom().getAliasDefinition().checkMultipleEnums(expr);
		OrderExpression<T> e = new OrderExpression<T>(query, expr, true, true, false);
		query.addOrderBy(e);
		return this;
	}

	public QueryWhere<T> orderByDescNullsLast(Object expr) {
		query.getFrom().getAliasDefinition().checkMultipleEnums(expr);
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
