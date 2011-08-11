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
package com.iciql.test;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.iciql.Db;

/**
 * JUnit 4 iciql test suite.
 * 
 * By default this test suite will run against the H2 database. You can change
 * this by switching the DEFAULT_TEST_DB value.
 * <p>
 * Alternatively, you can run this class an application which will run all tests
 * for all tested databases.
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ AliasMapTest.class, AnnotationsTest.class, BooleanModelTest.class, ClobTest.class,
		ConcurrencyTest.class, EnumsTest.class, ModelsTest.class, PrimitivesTest.class,
		RuntimeQueryTest.class, SamplesTest.class, UpdateTest.class, UUIDTest.class })
public class IciqlSuite {

	private static final TestDb[] TEST_DBS = { 
			new TestDb("H2", "jdbc:h2:mem:"),
			new TestDb("HSQL", "jdbc:hsqldb:mem:db{0,number,000}") };

	private static final TestDb DEFAULT_TEST_DB = TEST_DBS[0];

	private static AtomicInteger openCount = new AtomicInteger(0);

	public static void assertStartsWith(String value, String startsWith) {
		Assert.assertTrue(MessageFormat.format("Expected \"{0}\", got: \"{1}\"", startsWith, value),
				value.startsWith(startsWith));
	}

	public static Db openDb() {
		String testUrl = System.getProperty("iciql.url");
		if (testUrl == null) {
			testUrl = DEFAULT_TEST_DB.url;
		}
		testUrl = MessageFormat.format(testUrl, openCount.incrementAndGet());
		return Db.open(testUrl, "sa", "sa");
	}

	public static String getDatabaseName(Db db) {
		String database = "";
		try {
			database = db.getConnection().getMetaData().getDatabaseProductName();
		} catch (SQLException s) {
		}
		return database;
	}

	public static void main(String... args) {
		SuiteClasses suiteClasses = IciqlSuite.class.getAnnotation(SuiteClasses.class);
		for (TestDb testDb : TEST_DBS) {
			System.out.println("*********************************************");
			System.out.println("Testing " + testDb.name + " " + testDb.getVersion());
			System.out.println("*********************************************");
			System.setProperty("iciql.url", testDb.url);
			Result result = JUnitCore.runClasses(suiteClasses.value());
			System.out.println(MessageFormat.format("{0} runs, {1} failures, {2} ignores in {3} msecs",
					result.getRunCount(), result.getFailureCount(), result.getIgnoreCount(),
					result.getRunTime()));
			for (Failure failure : result.getFailures()) {
				System.out.println(MessageFormat.format("{0}: {1}", failure.getTestHeader(),
						failure.getMessage()));
			}
			System.out.println();
		}
	}

	/**
	 * Represents a test database url.
	 */
	private static class TestDb {
		final String name;
		final String url;

		TestDb(String name, String url) {
			this.name = name;
			this.url = url;
		}

		String getVersion() {
			try {
				Db db = Db.open(url, "sa", "sa");
				String version = db.getConnection().getMetaData().getDatabaseProductVersion();
				db.close();
				return version;
			} catch (SQLException s) {
			}
			return "";
		}
	}
}
