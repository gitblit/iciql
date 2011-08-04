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
 * An expression to order by in a query.
 * 
 * @param <T>
 *            the query data type
 */

class OrderExpression<T> {
	private Query<T> query;
	private Object expression;
	private boolean desc;
	private boolean nullsFirst;
	private boolean nullsLast;

	OrderExpression(Query<T> query, Object expression, boolean desc, boolean nullsFirst, boolean nullsLast) {
		this.query = query;
		this.expression = expression;
		this.desc = desc;
		this.nullsFirst = nullsFirst;
		this.nullsLast = nullsLast;
	}

	void appendSQL(SQLStatement stat) {
		query.appendSQL(stat, expression);
		if (desc) {
			stat.appendSQL(" DESC");
		}
		if (nullsLast) {
			stat.appendSQL(" NULLS LAST");
		}
		if (nullsFirst) {
			stat.appendSQL(" NULLS FIRST");
		}
	}

}
