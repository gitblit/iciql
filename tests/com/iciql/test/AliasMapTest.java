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

import java.util.List;

import org.junit.Test;

import com.iciql.Db;
import com.iciql.test.models.Product;

/**
 * Tests that columns (p.unitsInStock) are not compared by value with the value
 * (9), but by reference (using an identity hash map). See
 * http://code.google.com/p/h2database/issues/detail?id=119
 * 
 * @author d moebius at scoop dash gmbh dot de
 */
public class AliasMapTest {

	@Test
	public void testAliasMapping() throws Exception {
		Db db = Db.open("jdbc:h2:mem:", "sa", "sa");
		db.insertAll(Product.getList());

		Product p = new Product();
		List<Product> products = db.from(p).where(p.unitsInStock).is(9).orderBy(p.productId).select();

		assertEquals("[]", products.toString());

		db.close();
	}
}