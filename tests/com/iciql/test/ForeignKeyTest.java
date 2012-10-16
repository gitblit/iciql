/*
 * Copyright 2012 Frédéric Gaillard.
 * Copyright 2012 James Moger.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.IciqlException;
import com.iciql.test.models.CategoryAnnotationOnly;
import com.iciql.test.models.ProductAnnotationOnlyWithForeignKey;

public class ForeignKeyTest {

	/**
	 * This object represents a database (actually a connection to the
	 * database).
	 */

	private Db db;

	@Before
	public void setUp() {
		db = IciqlSuite.openNewDb();
		db.insertAll(CategoryAnnotationOnly.getList());
		db.insertAll(ProductAnnotationOnlyWithForeignKey.getList());
	}

	@After
	public void tearDown() {
		db.dropTable(ProductAnnotationOnlyWithForeignKey.class);
		db.dropTable(CategoryAnnotationOnly.class);
		db.close();
	}

	@Test
	public void testForeignKeyWithOnDeleteCascade() {
		ProductAnnotationOnlyWithForeignKey p = new ProductAnnotationOnlyWithForeignKey();
		long count1 = db.from(p).selectCount();

		// should remove 2 associated products
		CategoryAnnotationOnly c = new CategoryAnnotationOnly();
		db.from(c).where(c.categoryId).is(1L).delete();

		long count2 = db.from(p).selectCount();

		assertEquals(count1, count2 + 2L);
	}

	@Test
	public void testForeignKeyDropReferenceTable() {
		try {
			db.dropTable(CategoryAnnotationOnly.class);
			assertTrue("Should not be able to drop reference table!", false);
		} catch (IciqlException e) {
			assertEquals(e.getMessage(), IciqlException.CODE_CONSTRAINT_VIOLATION, e.getIciqlCode());
		}
	}

}