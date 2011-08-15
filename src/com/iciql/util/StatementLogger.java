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

import java.text.DecimalFormat;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class to optionally log generated statements to StatementListeners.<br>
 * Statement logging is disabled by default.
 * <p>
 * This class also tracks the counts for generated statements by major type.
 * 
 */
public class StatementLogger {

	/**
	 * Enumeration of the different statement types that are logged.
	 */
	public enum StatementType {
		STAT, TOTAL, CREATE, INSERT, UPDATE, MERGE, DELETE, SELECT;
	}

	/**
	 * Interface that defines a statement listener.
	 */
	public interface StatementListener {
		void logStatement(StatementType type, String statement);
	}

	private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
	private static final Set<StatementListener> LISTENERS = Utils.newHashSet();
	private static final StatementListener CONSOLE = new StatementListener() {

		@Override
		public void logStatement(StatementType type, String message) {
			System.out.println(message);
		}
	};

	private static final AtomicLong SELECT_COUNT = new AtomicLong();
	private static final AtomicLong CREATE_COUNT = new AtomicLong();
	private static final AtomicLong INSERT_COUNT = new AtomicLong();
	private static final AtomicLong UPDATE_COUNT = new AtomicLong();
	private static final AtomicLong MERGE_COUNT = new AtomicLong();
	private static final AtomicLong DELETE_COUNT = new AtomicLong();

	/**
	 * Activates the Console Logger.
	 */
	public static void activateConsoleLogger() {
		LISTENERS.add(CONSOLE);
	}

	/**
	 * Deactivates the Console Logger.
	 */
	public static void deactivateConsoleLogger() {
		LISTENERS.remove(CONSOLE);
	}

	/**
	 * Registers a listener with the relay.
	 * 
	 * @param listener
	 */
	public static void registerListener(StatementListener listener) {
		LISTENERS.add(listener);
	}

	/**
	 * Unregisters a listener with the relay.
	 * 
	 * @param listener
	 */
	public static void unregisterListener(StatementListener listener) {
		LISTENERS.remove(listener);
	}

	public static void create(String statement) {
		CREATE_COUNT.incrementAndGet();
		logStatement(StatementType.CREATE, statement);
	}

	public static void insert(String statement) {
		INSERT_COUNT.incrementAndGet();
		logStatement(StatementType.INSERT, statement);
	}

	public static void update(String statement) {
		UPDATE_COUNT.incrementAndGet();
		logStatement(StatementType.UPDATE, statement);
	}

	public static void merge(String statement) {
		MERGE_COUNT.incrementAndGet();
		logStatement(StatementType.MERGE, statement);
	}

	public static void delete(String statement) {
		DELETE_COUNT.incrementAndGet();
		logStatement(StatementType.DELETE, statement);
	}

	public static void select(String statement) {
		SELECT_COUNT.incrementAndGet();
		logStatement(StatementType.SELECT, statement);
	}

	private static void logStatement(final StatementType type, final String statement) {
		for (final StatementListener listener : LISTENERS) {
			EXEC.execute(new Runnable() {
				public void run() {
					listener.logStatement(type, statement);
				}
			});
		}
	}

	public static long getCreateCount() {
		return CREATE_COUNT.longValue();
	}

	public static long getInsertCount() {
		return INSERT_COUNT.longValue();
	}

	public static long getUpdateCount() {
		return UPDATE_COUNT.longValue();
	}

	public static long getMergeCount() {
		return MERGE_COUNT.longValue();
	}

	public static long getDeleteCount() {
		return DELETE_COUNT.longValue();
	}

	public static long getSelectCount() {
		return SELECT_COUNT.longValue();
	}

	public static long getTotalCount() {
		return getCreateCount() + getInsertCount() + getUpdateCount() + getDeleteCount() + getMergeCount()
				+ getSelectCount();
	}

	public static void logStats() {
		logStatement(StatementType.STAT, "iciql Runtime Statistics");
		logStatement(StatementType.STAT, "========================");
		logStat(StatementType.CREATE, getCreateCount());
		logStat(StatementType.INSERT, getInsertCount());
		logStat(StatementType.UPDATE, getUpdateCount());
		logStat(StatementType.MERGE, getMergeCount());
		logStat(StatementType.DELETE, getDeleteCount());
		logStat(StatementType.SELECT, getSelectCount());
		logStatement(StatementType.STAT, "========================");
		logStat(StatementType.TOTAL, getTotalCount());
	}

	private static void logStat(StatementType type, long value) {
		if (value > 0) {
			DecimalFormat df = new DecimalFormat("###,###,###,###");
			logStatement(StatementType.STAT,
					StringUtils.pad(type.name(), 6, " ", true) + " = " + df.format(value));
		}
	}
}