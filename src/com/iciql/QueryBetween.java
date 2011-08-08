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

public class QueryBetween<T, A> {
	
	private Query<T> query;
	private A x;
	private A y;

	public QueryBetween(Query<T> query, A x, A y) {
		this.query = query;
		this.x = x;
		this.y = y;
	}
	
	public QueryWhere<T> and(A z) {
		query.addConditionToken(new Condition<A>(x, y, z, CompareType.BETWEEN));
		return new QueryWhere<T>(query);
	}
}
