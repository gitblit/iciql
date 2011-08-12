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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.iciql.Db;
import com.iciql.IciqlException;
import com.iciql.test.models.PrimitivesModel;
import com.iciql.test.models.Product;
import com.iciql.util.Utils;

/**
 * Tests object and primitive alias referencing.
 */
public class AliasMapTest {

	/**
	 * Tests that columns (p.unitsInStock) are not compared by value with the
	 * value (9), but by reference (using an identity hash map). See
	 * http://code.google.com/p/h2database/issues/detail?id=119
	 * 
	 * @author d moebius at scoop dash gmbh dot de
	 */
	@Test
	public void testObjectAliasMapping() throws Exception {
		Db db = IciqlSuite.openDb();
		db.insertAll(Product.getList());

		// baseline count is the next id value
		long bc = Utils.COUNTER.get();
		// number of fields in primitives model class
		// each from() call will increment Utils.COUNTER by this amount
		int fc = Product.class.getFields().length;

		Product p = new Product();
		// This test confirms standard object referencing querying.
		long count = db.from(p).where(p.productId).is(9).selectCount();
		assertEquals(1, count);
		// Confirms that productId counter value is baseline counter value
		assertEquals(bc, p.productId.intValue());
		try {
			// This test compares "bc + fc" which is the counter value of
			// unitsInStock assigned by Utils.newObject() after the 2nd pass
			// through from().
			//
			// Object fields map by REFERENCE, not value.
			db.from(p).where(Long.valueOf(bc + fc).intValue()).is(9).orderBy(p.productId).select();
			assertTrue("Fail: object field is mapping by value.", false);
		} catch (IciqlException e) {
			assertEquals(IciqlException.CODE_UNMAPPED_FIELD, e.getIciqlCode());
			assertEquals(bc + 5, p.productId.intValue());
		}

		try {
			// This test compares Integer(bc) which is the counter value of
			// unitsInStock assigned by Utils.newObject() after the 3rd pass
			// through from().
			//
			// Object fields map by REFERENCE, not value.
			db.from(p).where(Long.valueOf(bc).intValue()).is(9).orderBy(p.productId).select();
			assertTrue("Fail: object field is mapping by value.", false);
		} catch (IciqlException e) {
			assertEquals(IciqlException.CODE_UNMAPPED_FIELD, e.getIciqlCode());
			assertEquals(bc + (2 * fc), p.productId.intValue());
		}

		db.close();
	}

	/**
	 * Confirms that primitive aliases ARE mapped by value.
	 */
	@Test
	public void testPrimitiveAliasMapping() throws Exception {
		Db db = IciqlSuite.openDb();
		PrimitivesModel model = new PrimitivesModel();
		model.myLong = 100L;
		db.insert(model);
		model.myLong = 200L;
		db.insert(model);

		// baseline count is the next id value
		long bc = Utils.COUNTER.get();
		// number of fields in primitives model class
		// each from() call will increment Utils.COUNTER by this amount
		int fc = PrimitivesModel.class.getFields().length;

		PrimitivesModel p = new PrimitivesModel();
		// This test confirms standard primitive referencing querying.
		long count = db.from(p).where(p.myLong).is(100L).selectCount();
		assertEquals(1, count);
		// Confirms that myLong counter value is bc
		assertEquals(bc, p.myLong);
		try {
			// This test compares "bc + fc" which is the counter value
			// of myLong assigned by Utils.newObject() after the 2nd pass
			// through from().
			//
			// Primitive fields map by VALUE.
			count = db.from(p).where(bc + fc).is(100L).selectCount();
			assertEquals(1, count);
			assertEquals(bc + fc, p.myLong);
		} catch (IciqlException e) {
			assertTrue(e.getMessage(), false);
		}
		try {
			// This test compares "bc" which was the counter value of
			// myLong assigned by Utils.newObject() after the 1st pass
			// through from(). "bc" is unmapped now and will throw an
			// exception.
			//
			// Primitive fields map by VALUE.
			db.from(p).where(bc).is(100L).select();
		} catch (IciqlException e) {
			assertEquals(IciqlException.CODE_UNMAPPED_FIELD, e.getIciqlCode());
			assertEquals(bc + (2 * fc), p.myLong);
		}
		db.close();
	}
}