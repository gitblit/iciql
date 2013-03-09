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

package com.iciql.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iciql.Iciql;
import com.iciql.util.IciqlLogger.IciqlListener;
import com.iciql.util.IciqlLogger.StatementType;

/**
 * Slf4jIciqlListener interfaces the IciqlLogger to the SLF4J logging framework.
 */
public class Slf4jIciqlListener implements IciqlListener {

	private Logger logger = LoggerFactory.getLogger(Iciql.class);

	/**
	 * Enumeration representing the SLF4J log levels.
	 */
	public enum Level {
		ERROR, WARN, INFO, DEBUG, TRACE, OFF;
	}

	private final Level defaultLevel;

	private final Map<StatementType, Level> levels;

	public Slf4jIciqlListener() {
		this(Level.TRACE);
	}

	public Slf4jIciqlListener(Level defaultLevel) {
		this.defaultLevel = defaultLevel;
		levels = new HashMap<StatementType, Level>();
		for (StatementType type : StatementType.values()) {
			levels.put(type, defaultLevel);
		}
	}

	/**
	 * Sets the logging level for a particular statement type.
	 * 
	 * @param type
	 * @param level
	 */
	public void setLevel(StatementType type, Level level) {
		levels.put(type, defaultLevel);
	}

	@Override
	public void logIciql(StatementType type, String statement) {
		Level level = levels.get(type);
		switch (level) {
		case ERROR:
			logger.error(statement);
			break;
		case WARN:
			logger.warn(statement);
			break;
		case INFO:
			logger.info(statement);
			break;
		case DEBUG:
			logger.debug(statement);
			break;
		case TRACE:
			logger.trace(statement);
			break;
		case OFF:
			break;
		}
	}
}
