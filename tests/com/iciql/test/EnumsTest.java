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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.test.models.EnumModels;
import com.iciql.test.models.EnumModels.EnumIdModel;
import com.iciql.test.models.EnumModels.EnumOrdinalModel;
import com.iciql.test.models.EnumModels.EnumStringModel;
import com.iciql.test.models.EnumModels.Tree;

/**
 * Tests enum support.
 */
public class EnumsTest {

	private Db db;

	@Before
	public void setUp() {
		db = IciqlSuite.openDb();
		db.insertAll(EnumIdModel.createList());
		db.insertAll(EnumOrdinalModel.createList());
		db.insertAll(EnumStringModel.createList());
	}

	@After
	public void tearDown() {
		db.close();
	}

	@Test
	public void testEnumQueries() {
		testIntEnums(new EnumIdModel());
		testIntEnums(new EnumOrdinalModel());
		testStringEnums(new EnumStringModel());
	}

	private void testIntEnums(EnumModels e) {
		// ensure all records inserted
		long count = db.from(e).selectCount();
		assertEquals(5, count);

		// special case:
		// value is first enum constant which is also the alias object.
		// the first enum constant is used as the alias because we can not
		// instantiate an enum reflectively.
		EnumModels firstEnumValue = db.from(e).where(e.tree()).is(Tree.PINE).selectFirst();
		assertEquals(Tree.PINE, firstEnumValue.tree());

		EnumModels model = db.from(e).where(e.tree()).is(Tree.WALNUT).selectFirst();

		assertEquals(400, model.id.intValue());
		assertEquals(Tree.WALNUT, model.tree());

		List<EnumModels> list = db.from(e).where(e.tree()).atLeast(Tree.BIRCH).select();
		assertEquals(3, list.size());

		// between is an int compare
		list = db.from(e).where(e.tree()).between(Tree.BIRCH).and(Tree.WALNUT).select();
		assertEquals(2, list.size());

	}

	private void testStringEnums(EnumModels e) {
		// ensure all records inserted
		long count = db.from(e).selectCount();
		assertEquals(5, count);

		// special case:
		// value is first enum constant which is also the alias object.
		// the first enum constant is used as the alias because we can not
		// instantiate an enum reflectively.
		EnumModels firstEnumValue = db.from(e).where(e.tree()).is(Tree.PINE).selectFirst();
		assertEquals(Tree.PINE, firstEnumValue.tree());

		EnumModels model = db.from(e).where(e.tree()).is(Tree.WALNUT).selectFirst();

		assertEquals(400, model.id.intValue());
		assertEquals(Tree.WALNUT, model.tree());

		List<EnumModels> list = db.from(e).where(e.tree()).isNot(Tree.BIRCH).select();
		assertEquals(count - 1, list.size());

		// between is a string compare
		list = db.from(e).where(e.tree()).between(Tree.MAPLE).and(Tree.PINE).select();
		assertEquals(3, list.size());
	}
}
