/*
 * Copyright (c) 2009-2014, Architector Inc., Japan
 * All rights reserved.
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

public abstract class NestedConditions<T> {

	public static class And<T> extends NestedConditions<T> {

		public And(Db db, T alias) {
			super(db, alias);
		}

		protected QueryCondition<T, Boolean> and(boolean x) {
			return where.and(x);
		}

		protected QueryCondition<T, Byte> and(byte x) {
			return where.and(x);
		}

		protected QueryCondition<T, Short> and(short x) {
			return where.and(x);
		}

		protected QueryCondition<T, Integer> and(int x) {
			return where.and(x);
		}

		protected QueryCondition<T, Long> and(long x) {
			return where.and(x);
		}

		protected QueryCondition<T, Float> and(float x) {
			return where.and(x);
		}

		protected QueryCondition<T, Double> and(double x) {
			return where.and(x);
		}

		protected <A> QueryCondition<T, A> and(A x) {
			return where.and(x);
		}

		protected QueryWhere<T> and(And<T> conditions) {
			where.andOpen();
			where.query.addConditionToken(conditions.where.query);
			return where.close();
		}

		protected QueryWhere<T> and(Or<T> conditions) {
			where.andOpen();
			where.query.addConditionToken(conditions.where.query);
			return where.close();
		}

	}

	public static class Or<T> extends NestedConditions<T> {

		public Or(Db db, T alias) {
			super(db, alias);
		}

		protected QueryCondition<T, Boolean> or(boolean x) {
			return where.or(x);
		}

		protected QueryCondition<T, Byte> or(byte x) {
			return where.or(x);
		}

		protected QueryCondition<T, Short> or(short x) {
			return where.or(x);
		}

		protected QueryCondition<T, Integer> or(int x) {
			return where.or(x);
		}

		protected QueryCondition<T, Long> or(long x) {
			return where.or(x);
		}

		protected QueryCondition<T, Float> or(float x) {
			return where.or(x);
		}

		protected QueryCondition<T, Double> or(double x) {
			return where.or(x);
		}

		protected <A> QueryCondition<T, A> or(A x) {
			return where.or(x);
		}

		protected QueryWhere<T> or(And<T> conditions) {
			where.orOpen();
			where.query.addConditionToken(conditions.where.query);
			return where.close();
		}

		protected QueryWhere<T> or(Or<T> conditions) {
			where.orOpen();
			where.query.addConditionToken(conditions.where.query);
			return where.close();
		}

	}

	QueryWhere<T> where;

	private NestedConditions(Db db, T alias) {
		where = new QueryWhere<T>(Query.rebuild(db, alias));
	}

}
