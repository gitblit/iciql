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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.iciql.Db;
import com.iciql.test.models.PrimitivesModel;

/**
 * Tests primitives with autoboxing within the framework.
 */
public class PrimitivesTest {

	@Test
	public void testPrimitives() {
		Db db = Db.open("jdbc:h2:mem:", "sa", "sa");

		// insert random model
		PrimitivesModel model = new PrimitivesModel();
		db.insert(model);

		PrimitivesModel p = new PrimitivesModel();
		
		// retrieve model and compare
		PrimitivesModel retrievedModel = db.from(p).selectFirst();
		assertTrue(model.equivalentTo(retrievedModel));

		// retrieve with conditions and compare 
//		StatementLogger.activateConsoleLogger();
//		retrievedModel = db.from(p).where(p.myLong).is(model.myLong).and(p.myInteger).is(model.myInteger)
//				.selectFirst();
//		assertTrue(model.equivalentTo(retrievedModel));
//		
//		// update myInteger and compare
//		db.from(p).set(p.myInteger).to(10).where(p.myLong).is(model.myLong).update();
//		retrievedModel = db.from(p).selectFirst();
		
//		assertEquals(10, retrievedModel.myInteger);

		db.close();
	}
}
