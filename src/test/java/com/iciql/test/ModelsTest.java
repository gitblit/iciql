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

package com.iciql.test;

import static com.iciql.test.IciqlSuite.assertEqualsIgnoreCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import com.iciql.Db;
import com.iciql.DbInspector;
import com.iciql.ValidationRemark;
import com.iciql.test.models.Product;
import com.iciql.test.models.ProductAnnotationOnly;
import com.iciql.test.models.ProductMixedAnnotation;
import com.iciql.test.models.SupportedTypes;
import com.iciql.util.StringUtils;

/**
 * Test that the mapping between classes and tables is done correctly.
 */
public class ModelsTest {

	/*
	 * The ErrorCollector Rule allows execution of a test to continue after the
	 * first problem is found and report them all at once
	 */
	@Rule
	public ErrorCollector errorCollector = new ErrorCollector();

	private Db db;

	@Before
	public void setUp() {
		db = IciqlSuite.openNewDb();
		db.insertAll(Product.getList());
		db.insertAll(ProductAnnotationOnly.getList());
		db.insertAll(ProductMixedAnnotation.getList());
	}

	@After
	public void tearDown() {
		db.close();
	}

	@Test
	public void testValidateModels() {
		// SQLite metadata mapping in the JDBC driver needs improvement
		String schemaName = IciqlSuite.getDefaultSchema(db);
		DbInspector inspector = new DbInspector(db);
		validateModel(inspector, schemaName, new ProductAnnotationOnly(), IciqlSuite.isSQLite(db) ? 5 : 2);
		validateModel(inspector, schemaName, new ProductMixedAnnotation(), IciqlSuite.isSQLite(db) ? 6 : 4);
	}

	private void validateModel(DbInspector inspector, String schemaName, Object o, int expected) {
		List<ValidationRemark> remarks = inspector.validateModel(o, false);
		assertTrue("validation remarks are null for " + o.getClass().getName(), remarks != null);
		StringBuilder sb = new StringBuilder();
		sb.append("validation remarks for " + o.getClass().getName());
		sb.append('\n');
		for (ValidationRemark remark : remarks) {
			sb.append(remark.toString());
			sb.append('\n');
			if (remark.isError()) {
				errorCollector.addError(new SQLException(remark.toString()));
			}
		}

		if (IciqlSuite.isSQLite(db)) {
			assertEquals(sb.toString(), expected, remarks.size());
		} else  if (StringUtils.isNullOrEmpty(schemaName)) {
			// no schema expected
			assertEquals(sb.toString(), expected - 1, remarks.size());
		} else {
			assertEquals(sb.toString(), expected, remarks.size());
			assertEqualsIgnoreCase(MessageFormat.format("@IQSchema(\"{0}\")", schemaName),
					remarks.get(0).message);
		}
	}

	@Test
	public void testSupportedTypes() {
		List<SupportedTypes> original = SupportedTypes.createList();
		db.insertAll(original);
		List<SupportedTypes> retrieved = db.from(SupportedTypes.SAMPLE).select();
		assertEquals(original.size(), retrieved.size());
		for (int i = 0; i < original.size(); i++) {
			SupportedTypes o = original.get(i);
			SupportedTypes r = retrieved.get(i);
			assertTrue(o.equivalentTo(r));
		}
	}

	@Test
	public void testModelGeneration() {
		List<SupportedTypes> original = SupportedTypes.createList();
		db.insertAll(original);
		DbInspector inspector = new DbInspector(db);
		List<String> models = inspector.generateModel(null, "SupportedTypes", "com.iciql.test.models", true,
				true);
		assertEquals(1, models.size());
		// a poor test, but a start
		String dbName = IciqlSuite.getDatabaseEngineName(db);
		if (dbName.equals("H2")) {
			assertEquals(1587, models.get(0).length());
		} else if (dbName.startsWith("HSQL")) {
			// HSQL uses Double instead of Float
			assertEquals(1591, models.get(0).length());
		} else if (dbName.equals("Apache Derby")) {
			// Derby uses java.sql.Timestamp not java.util.Date
			// Derby uses username as schema name
			assertEquals(1601, models.get(0).length());
		} else if (dbName.equals("PostgreSQL")) {
			assertEquals(1643, models.get(0).length());
		} else if (dbName.equals("MySQL")) {
			// MySQL uses timestamp default values like
			// 0000-00-00 00:00:00 and CURRENT_TIMESTAMP
			assertEquals(1673, models.get(0).length());
		} else if (dbName.equals("SQLite")) {
			assertEquals(1566, models.get(0).length());
		} else {
			// unknown database
			assertEquals(0, models.get(0).length());
		}
	}

	@Test
	public void testDiscreteUpdateStringTrimming() {
		List<SupportedTypes> original = SupportedTypes.createList();
		db.insertAll(original);
		SupportedTypes s1 = db.from(SupportedTypes.SAMPLE).where(SupportedTypes.SAMPLE.id).is(1).selectFirst();
		db.from(SupportedTypes.SAMPLE)
				.set(SupportedTypes.SAMPLE.myString)
				.to(s1.myString + s1.myString + s1.myString + s1.myString)
				.update();
		SupportedTypes s2 = db.from(SupportedTypes.SAMPLE).where(SupportedTypes.SAMPLE.id).is(1).selectFirst();
		assertEquals(40, s2.myString.length());
	}

	@Test
	public void testColumnSelection() {
		List<SupportedTypes> original = SupportedTypes.createList();
		db.insertAll(original);
		List<String> myStrings = db.from(SupportedTypes.SAMPLE)
				.select(SupportedTypes.SAMPLE.myString);
		assertEquals(10, myStrings.size());

		List<Integer> ids = db.from(SupportedTypes.SAMPLE)
				.orderByDesc(SupportedTypes.SAMPLE.id)
				.selectDistinct(SupportedTypes.SAMPLE.id);
		assertEquals(10, ids.size());
		assertEquals("[10, 9, 8, 7, 6, 5, 4, 3, 2, 1]", ids.toString());
	}
}
