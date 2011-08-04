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
 * An OR or an AND condition.
 */

enum ConditionAndOr implements Token {
	AND("AND"), OR("OR");

	private String text;

	ConditionAndOr(String text) {
		this.text = text;
	}

	public <T> void appendSQL(SQLStatement stat, Query<T> query) {
		stat.appendSQL(text);
	}

}
