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
 * This class represents a query with join and an incomplete condition.
 * 
 * @param <A>
 *            the incomplete condition data type
 */

public class QueryJoinCondition<T, A> {

	private Query<T> query;
	private SelectTable<T> join;
	private A x;

	QueryJoinCondition(Query<T> query, SelectTable<T> join, A x) {
		this.query = query;
		this.join = join;
		this.x = x;
	}

	public Query<T> is(boolean y) {
		return addPrimitive(y);
	}	

	public Query<T> is(byte y) {
		return addPrimitive(y);
	}	

	public Query<T> is(short y) {
		return addPrimitive(y);
	}	

	public Query<T> is(int y) {
		return addPrimitive(y);
	}
	
	public Query<T> is(long y) {
		return addPrimitive(y);
	}	

	public Query<T> is(float y) {
		return addPrimitive(y);
	}	

	public Query<T> is(double y) {
		return addPrimitive(y);
	}	

	@SuppressWarnings("unchecked")
	private Query<T> addPrimitive(Object o) {		
		A alias = query.getPrimitiveAliasByValue((A) o);
		if (alias == null) {
			join.addConditionToken(new Condition<A>(x, (A) o, CompareType.EQUAL));
		} else {
			join.addConditionToken(new Condition<A>(x, alias, CompareType.EQUAL));
		}
		return query;		
	}

	public Query<T> is(A y) {
		join.addConditionToken(new Condition<A>(x, y, CompareType.EQUAL));
		return query;
	}
}
