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

import java.text.MessageFormat;

/**
 * Iciql wraps all exceptions with this class.
 */
public class IciqlException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IciqlException(String message, Object... parameters) {
		super(parameters.length > 0 ? MessageFormat.format(message, parameters) : message);

	}

	public IciqlException(Throwable t, String message, Object... parameters) {
		super(parameters.length > 0 ? MessageFormat.format(message, parameters) : message, t);

	}

	public IciqlException(Throwable t) {
		super(t);
	}

	public IciqlException(String message, Throwable t) {
		super(message, t);
	}
}
