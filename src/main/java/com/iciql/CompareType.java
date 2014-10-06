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
 * An enumeration of compare operations.
 */

enum CompareType {
	EQUAL("=", true), EXCEEDS(">", true), AT_LEAST(">=", true), LESS_THAN("<", true), AT_MOST("<=", true), NOT_EQUAL(
			"<>", true), IS_NOT_NULL("IS NOT NULL", false), IS_NULL("IS NULL", false), LIKE("LIKE", true), BETWEEN(
			"BETWEEN", true), IN("IN", true), NOT_IN("NOT IN", true);

	private String text;
	private boolean hasRightExpression;

	CompareType(String text, boolean hasRightExpression) {
		this.text = text;
		this.hasRightExpression = hasRightExpression;
	}

	String getString() {
		return text;
	}

	boolean hasRightExpression() {
		return hasRightExpression;
	}

}
