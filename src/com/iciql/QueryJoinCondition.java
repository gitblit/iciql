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

public class QueryJoinCondition<A> {

	private Query<?> query;
	private SelectTable<?> join;
	private A x;

	QueryJoinCondition(Query<?> query, SelectTable<?> join, A x) {
		this.query = query;
		this.join = join;
		this.x = x;
	}

	public Query<?> is(boolean y) {
		return addPrimitive(y);
	}	

	public Query<?> is(byte y) {
		return addPrimitive(y);
	}	

	public Query<?> is(short y) {
		return addPrimitive(y);
	}	

	public Query<?> is(int y) {
		return addPrimitive(y);
	}
	
	public Query<?> is(long y) {
		return addPrimitive(y);
	}	

	public Query<?> is(float y) {
		return addPrimitive(y);
	}	

	public Query<?> is(double y) {
		return addPrimitive(y);
	}	

	@SuppressWarnings("unchecked")
	private Query<?> addPrimitive(Object o) {		
		A alias = query.getPrimitiveAliasByValue((A) o);
		if (alias == null) {
			join.addConditionToken(new Condition<A>(x, (A) o, CompareType.EQUAL));
		} else {
			join.addConditionToken(new Condition<A>(x, alias, CompareType.EQUAL));
		}
		return query;		
	}

	public Query<?> is(A y) {
		join.addConditionToken(new Condition<A>(x, y, CompareType.EQUAL));
		return query;
	}
}
