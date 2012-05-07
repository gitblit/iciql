/*
 * Copyright 2012 James Moger.
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
 * A condition that contains a subquery.
 * 
 * @param <A>
 *            the operand type
 */

class SubQueryCondition<A, Y, Z> implements Token {
	A x;
	SubQuery<Y, Z> subquery;

	SubQueryCondition(A x, SubQuery<Y, Z> subquery) {
		this.x = x;
		this.subquery = subquery;
	}

	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, null, x);
		stat.appendSQL(" in (");
		subquery.appendSQL(stat);
		stat.appendSQL(")");
	}
}
