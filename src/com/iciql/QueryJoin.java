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
 * This class represents a query with a join.
 */

public class QueryJoin {

	private Query<?> query;
	private SelectTable<?> join;

	QueryJoin(Query<?> query, SelectTable<?> join) {
		this.query = query;
		this.join = join;
	}

	public QueryJoinCondition<Boolean> on(boolean x) {
		return addPrimitive(x);
	}

	public QueryJoinCondition<Byte> on(byte x) {
		return addPrimitive(x);
	}

	public QueryJoinCondition<Short> on(short x) {
		return addPrimitive(x);
	}

	public QueryJoinCondition<Integer> on(int x) {
		return addPrimitive(x);
	}

	public QueryJoinCondition<Long> on(long x) {
		return addPrimitive(x);
	}

	public QueryJoinCondition<Float> on(float x) {
		return addPrimitive(x);
	}

	public QueryJoinCondition<Double> on(double x) {
		return addPrimitive(x);
	}

	private <A> QueryJoinCondition<A> addPrimitive(A x) {		
		A alias = query.getPrimitiveAliasByValue(x);
		if (alias == null) {
			// this will result in an unmapped field exception
			return new QueryJoinCondition<A>(query, join, x);
		}
		return new QueryJoinCondition<A>(query, join, alias);
	}

	public <A> QueryJoinCondition<A> on(A x) {
		return new QueryJoinCondition<A>(query, join, x);
	}
}
