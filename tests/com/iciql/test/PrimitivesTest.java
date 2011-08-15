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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.iciql.Db;
import com.iciql.test.models.PrimitivesModel;

/**
 * Tests primitives with autoboxing within the framework.
 */
public class PrimitivesTest {

	@Test
	public void testPrimitives() {
		Db db = IciqlSuite.openNewDb();

		// insert random models in reverse order
		List<PrimitivesModel> models = PrimitivesModel.getList();
		PrimitivesModel model = models.get(0);
		Collections.reverse(models);
		// insert them in reverse order
		db.insertAll(models);

		PrimitivesModel p = new PrimitivesModel();

		// retrieve model and compare
		PrimitivesModel retrievedModel = db.from(p).orderBy(p.myLong).selectFirst();
		assertTrue(model.equivalentTo(retrievedModel));

		retrievedModel = db.from(p).where("mylong = ? and myinteger = ?", model.myLong, model.myInteger)
				.selectFirst();
		assertTrue(model.equivalentTo(retrievedModel));

		// retrieve with conditions and compare
		retrievedModel = db.from(p).where(p.myLong).is(model.myLong).and(p.myInteger).is(model.myInteger)
				.selectFirst();
		assertTrue(model.equivalentTo(retrievedModel));

		// set myInteger & myDouble
		db.from(p).set(p.myInteger).to(10).set(p.myDouble).to(3.0d).where(p.myLong).is(model.myLong).update();
		retrievedModel = db.from(p).orderBy(p.myLong).selectFirst();

		assertEquals(10, retrievedModel.myInteger);
		assertEquals(3d, retrievedModel.myDouble, 0.001d);

		// increment my double by pi
		db.from(p).increment(p.myDouble).by(3.14d).update();
		retrievedModel = db.from(p).orderBy(p.myLong).selectFirst();
		assertEquals(6.14d, retrievedModel.myDouble, 0.001d);

		// test order by
		List<PrimitivesModel> list = db.from(p).orderBy(p.myLong).select();
		assertEquals(models.size(), list.size());
		assertEquals("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]", list.toString());

		// test model update
		retrievedModel.myInteger = 1337;
		assertEquals(1, db.update(retrievedModel));
		assertEquals(1, db.delete(retrievedModel));

		db.close();
	}
}
