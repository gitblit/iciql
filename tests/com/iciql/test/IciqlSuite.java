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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.iciql.Constants;
import com.iciql.Db;
import com.iciql.test.models.BooleanModel;
import com.iciql.test.models.ComplexObject;
import com.iciql.test.models.Customer;
import com.iciql.test.models.DefaultValuesModel;
import com.iciql.test.models.EnumModels.EnumIdModel;
import com.iciql.test.models.EnumModels.EnumOrdinalModel;
import com.iciql.test.models.EnumModels.EnumStringModel;
import com.iciql.test.models.Order;
import com.iciql.test.models.PrimitivesModel;
import com.iciql.test.models.Product;
import com.iciql.test.models.ProductAnnotationOnly;
import com.iciql.test.models.ProductInheritedAnnotation;
import com.iciql.test.models.ProductMixedAnnotation;
import com.iciql.test.models.SupportedTypes;
import com.iciql.util.StatementLogger;
import com.iciql.util.StatementLogger.StatementListener;
import com.iciql.util.StatementLogger.StatementType;
import com.iciql.util.StringUtils;

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
		RuntimeQueryTest.class, SamplesTest.class, UpdateTest.class, UpgradesTest.class, UUIDTest.class })
public class IciqlSuite {

	private static final TestDb[] TEST_DBS = { new TestDb("H2 (embedded)", "jdbc:h2:mem:db{0,number,000}"),
			new TestDb("HSQL (embedded)", "jdbc:hsqldb:mem:db{0,number,000}"),
			new TestDb("Derby (embedded)", "jdbc:derby:memory:db{0,number,000};create=true"),
			new TestDb("MySQL (tcp/myisam)", "jdbc:mysql://localhost:3306/iciql") };

	private static final TestDb DEFAULT_TEST_DB = TEST_DBS[0];

	private static final PrintStream ERR = System.err;

	private static AtomicInteger openCount = new AtomicInteger(0);

	private static String username = "sa";

	private static String password = "sa";

	private static PrintStream out = System.out;

	public static void assertStartsWith(String value, String startsWith) {
		Assert.assertTrue(MessageFormat.format("Expected \"{0}\", got: \"{1}\"", startsWith, value),
				value.startsWith(startsWith));
	}

	public static boolean equivalentTo(double expected, double actual) {
		if (Double.compare(expected, actual) == 0) {
			return true;
		}
		return Math.abs(expected - actual) <= 0.000001d;
	}

	/**
	 * Increment the database counter, open and create a new database.
	 * 
	 * @return a fresh database
	 */
	public static Db openNewDb() {
		String testUrl = System.getProperty("iciql.url");
		if (testUrl == null) {
			testUrl = DEFAULT_TEST_DB.url;
		}
		testUrl = MessageFormat.format(testUrl, openCount.incrementAndGet());
		Db db = Db.open(testUrl, username, password);

		// drop tables
		db.dropTable(BooleanModel.class);
		db.dropTable(ComplexObject.class);
		db.dropTable(Customer.class);
		db.dropTable(DefaultValuesModel.class);
		db.dropTable(EnumIdModel.class);
		db.dropTable(EnumOrdinalModel.class);
		db.dropTable(EnumStringModel.class);
		db.dropTable(Order.class);
		db.dropTable(PrimitivesModel.class);
		db.dropTable(Product.class);
		db.dropTable(ProductAnnotationOnly.class);
		db.dropTable(ProductInheritedAnnotation.class);
		db.dropTable(ProductMixedAnnotation.class);
		db.dropTable(SupportedTypes.class);

		return db;
	}

	/**
	 * Open the current database.
	 * 
	 * @return the current database
	 */
	public static Db openCurrentDb() {
		String testUrl = System.getProperty("iciql.url");
		if (testUrl == null) {
			testUrl = DEFAULT_TEST_DB.url;
		}
		testUrl = MessageFormat.format(testUrl, openCount.get());
		return Db.open(testUrl, username, password);
	}

	/**
	 * Drops all tables from the current database.
	 * 
	 * @return the current database
	 */
	public static void dropAllTables(Db db) {

	}

	/**
	 * Returns the name of the underlying database engine for the Db object.
	 * 
	 * @param db
	 * @return the database engine name
	 */
	public static String getDatabaseEngineName(Db db) {
		String database = "";
		try {
			database = db.getConnection().getMetaData().getDatabaseProductName();
		} catch (SQLException s) {
		}
		return database;
	}

	/**
	 * Returns true if the underlying database engine is Derby.
	 * 
	 * @param db
	 * @return true if underlying database engine is Derby
	 */
	public static boolean isDerby(Db db) {
		return IciqlSuite.getDatabaseEngineName(db).equals("Apache Derby");
	}

	/**
	 * Returns true if the underlying database engine is H2.
	 * 
	 * @param db
	 * @return true if underlying database engine is H2
	 */
	public static boolean isH2(Db db) {
		return IciqlSuite.getDatabaseEngineName(db).equals("H2");
	}

	/**
	 * Returns true if the underlying database engine is MySQL.
	 * 
	 * @param db
	 * @return true if underlying database engine is MySQL
	 */
	public static boolean isMySQL(Db db) {
		return IciqlSuite.getDatabaseEngineName(db).equals("MySQL");
	}

	/**
	 * Gets the default schema of the underlying database engine.
	 * 
	 * @param db
	 * @return the default schema
	 */
	public static String getDefaultSchema(Db db) {
		if (isDerby(db)) {
			// Derby sets default schema name to username
			return username.toUpperCase();
		} else if (isMySQL(db)) {
			// MySQL does not have schemas
			return null;
		}

		return "PUBLIC";
	}

	/**
	 * Main entry point for the test suite. Executing this method will run the
	 * test suite on all registered databases.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		Params params = new Params();
		JCommander jc = new JCommander(params);
		try {
			jc.parse(args);
		} catch (ParameterException t) {
			usage(jc, t);
		}

		// Replace System.out with a file
		if (!StringUtils.isNullOrEmpty(params.dbPerformanceFile)) {
			out = new PrintStream(params.dbPerformanceFile);
			System.setErr(out);
		}

		// Statement logging
		final FileWriter statementWriter;
		if (StringUtils.isNullOrEmpty(params.sqlStatementsFile)) {
			statementWriter = null;
		} else {
			statementWriter = new FileWriter(params.sqlStatementsFile);
		}
		StatementListener statementListener = new StatementListener() {
			@Override
			public void logStatement(StatementType type, String statement) {
				if (statementWriter == null) {
					return;
				}
				try {
					statementWriter.append(statement);
					statementWriter.append('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		StatementLogger.registerListener(statementListener);

		SuiteClasses suiteClasses = IciqlSuite.class.getAnnotation(SuiteClasses.class);
		long quickestDatabase = Long.MAX_VALUE;
		String dividerMajor = buildDivider('*', 79);
		String dividerMinor = buildDivider('-', 79);

		// Header
		out.println(dividerMajor);
		out.println(MessageFormat.format("{0} {1} ({2}) testing {3} databases", Constants.NAME,
				Constants.VERSION, Constants.VERSION_DATE, TEST_DBS.length));
		out.println(dividerMajor);
		out.println();

		showProperty("java.vendor");
		showProperty("java.runtime.version");
		showProperty("java.vm.name");
		showProperty("os.name");
		showProperty("os.version");
		showProperty("os.arch");
		showProperty("available processors", "" + Runtime.getRuntime().availableProcessors());
		showProperty(
				"available memory",
				MessageFormat.format("{0,number,0.0} GB", ((double) Runtime.getRuntime().maxMemory())
						/ (1024 * 1024)));
		out.println();

		// Test a database
		long lastCount = 0;
		for (TestDb testDb : TEST_DBS) {
			out.println(dividerMinor);
			out.println("Testing " + testDb.name + " " + testDb.getVersion());
			out.println(dividerMinor);

			// inject a database section delimiter in the statement log
			if (statementWriter != null) {
				statementWriter.append("\n\n");
				statementWriter.append("# ").append(dividerMinor).append('\n');
				statementWriter.append("# ").append("Testing " + testDb.name + " " + testDb.getVersion())
						.append('\n');
				statementWriter.append("# ").append(dividerMinor).append('\n');
				statementWriter.append("\n\n");
			}

			if (testDb.getVersion().equals("OFFLINE")) {
				// Database not available
				out.println("Skipping.  Could not find " + testDb.url);
			} else {
				// Test database
				System.setProperty("iciql.url", testDb.url);
				Result result = JUnitCore.runClasses(suiteClasses.value());
				testDb.runtime = result.getRunTime();
				if (testDb.runtime < quickestDatabase) {
					quickestDatabase = testDb.runtime;
				}
				testDb.statements = StatementLogger.getTotalCount() - lastCount;
				// reset total count for next database
				lastCount = StatementLogger.getTotalCount();

				out.println(MessageFormat.format(
						"{0} tests ({1} failures, {2} ignores)  {3} statements in {4,number,0.000} secs",
						result.getRunCount(), result.getFailureCount(), result.getIgnoreCount(),
						testDb.statements, result.getRunTime() / 1000f));

				if (result.getFailureCount() == 0) {
					out.println();
					out.println("  100% successful test suite run.");
					out.println();
				} else {
					for (Failure failure : result.getFailures()) {
						out.println(MessageFormat.format("\n  + {0}\n    {1}", failure.getTestHeader(),
								failure.getMessage()));
					}
					out.println();
				}
			}
		}

		// Display runtime results sorted by performance leader
		out.println();
		out.println(dividerMajor);
		out.println(MessageFormat.format("{0} {1} ({2}) test suite performance results", Constants.NAME,
				Constants.VERSION, Constants.VERSION_DATE));
		out.println(dividerMajor);
		List<TestDb> dbs = Arrays.asList(TEST_DBS);
		Collections.sort(dbs, new Comparator<TestDb>() {

			@Override
			public int compare(TestDb o1, TestDb o2) {
				if (o1.runtime == 0) {
					return 1;
				}
				if (o2.runtime == 0) {
					return -1;
				}
				if (o1.runtime == o2.runtime) {
					return 0;
				}
				if (o1.runtime > o2.runtime) {
					return 1;
				}
				return -1;
			}
		});
		for (TestDb testDb : dbs) {
			out.println(MessageFormat.format(
					"{0} {1}  {2,number,0} stats/sec  {3,number,0.000} secs  ({4,number,0.0}x)",
					StringUtils.pad(testDb.name, 20, " ", true),
					StringUtils.pad(testDb.getVersion(), 22, " ", true), ((double) testDb.statements)
							/ (testDb.runtime / 1000d), testDb.runtime / 1000f, ((double) testDb.runtime)
							/ quickestDatabase));
		}

		// close PrintStream and restore System.err
		StatementLogger.unregisterListener(statementListener);
		out.close();
		System.setErr(ERR);
		if (statementWriter != null) {
			statementWriter.close();
		}
		System.exit(0);
	}

	private static void showProperty(String name) {
		showProperty(name, System.getProperty(name));
	}

	private static void showProperty(String name, String value) {
		out.print(' ');
		out.print(StringUtils.pad(name, 25, " ", true));
		out.println(value);
	}

	private static void usage(JCommander jc, ParameterException t) {
		System.out.println(Constants.NAME + " test suite v" + Constants.VERSION);
		System.out.println();
		if (t != null) {
			System.out.println(t.getMessage());
			System.out.println();
		}
		if (jc != null) {
			jc.usage();
		}
		System.exit(0);
	}

	private static String buildDivider(char c, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Represents a test database url.
	 */
	private static class TestDb {
		final String name;
		final String url;
		String version;
		long runtime;
		long statements;

		TestDb(String name, String url) {
			this.name = name;
			this.url = url;
		}

		String getVersion() {
			if (version == null) {
				try {
					Db db = Db.open(url, username, password);
					version = db.getConnection().getMetaData().getDatabaseProductVersion();
					db.close();
					return version;
				} catch (Throwable t) {
					version = "OFFLINE";
				}
			}
			return version;
		}
	}

	/**
	 * Command-line parameters for TestSuite.
	 */
	@Parameters(separators = " ")
	private static class Params {

		@Parameter(names = { "--dbFile" }, description = "Database performance results text file", required = false)
		public String dbPerformanceFile;

		@Parameter(names = { "--sqlFile" }, description = "SQL statements log file", required = false)
		public String sqlStatementsFile;
	}
}