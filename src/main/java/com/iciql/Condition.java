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
 * A condition contains one or two operands and a compare operation.
 * 
 * @param <A>
 *            the operand type
 */

class Condition<A> implements Token {
	CompareType compareType;
	A x, y, z;
	Iterable<A> i;

	Condition(A x, CompareType compareType) {
		this(x, null, null, null, compareType);
	}

	Condition(A x, A y, CompareType compareType) {
		this(x, y, null, null, compareType);
	}

	Condition(A x, A y, A z, CompareType compareType) {
		this(x, y, z, null, compareType);
	}

	Condition(A x, Iterable<A> i, CompareType compareType) {
		this(x,  null, null, i, compareType);
	}

	Condition(A x, A y, A z, Iterable<A> i, CompareType compareType) {
		this.compareType = compareType;
		this.x = x;
		this.y = y;
		this.z = z;
		this.i = i;
	}

	@SuppressWarnings("unchecked")
	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		query.appendSQL(stat, null, x);
		stat.appendSQL(" ");
		stat.appendSQL(compareType.getString());
		if (compareType.hasRightExpression()) {
			if (i == null) {
				stat.appendSQL(" ");
				if (z == null) {
					query.appendSQL(stat, x, y);
				} else {
					query.appendSQL(stat, x, y, z, compareType);
				}
			} else {
				query.appendSQL(stat, x, (Iterable<Object>)i, compareType);
			}
		}
	}
}
