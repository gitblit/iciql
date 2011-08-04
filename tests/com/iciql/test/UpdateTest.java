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

import static java.sql.Date.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.test.models.Customer;
import com.iciql.test.models.Order;
import com.iciql.test.models.Product;

/**
 * Tests the Db.update() function.
 * 
 * @author dmoebius at scoop dash gmbh dot de
 */
public class UpdateTest {

	private Db db;

	@Before
	public void setUp() throws Exception {
		db = Db.open("jdbc:h2:mem:", "sa", "sa");
		db.insertAll(Product.getList());
		db.insertAll(Customer.getList());
		db.insertAll(Order.getList());
	}

	@After
	public void tearDown() {
		db.close();
	}

	@Test
	public void testSimpleUpdate() {
		Product p = new Product();
		Product pChang = db.from(p).where(p.productName).is("Chang").selectFirst();
		// update unitPrice from 19.0 to 19.5
		pChang.unitPrice = 19.5;
		// update unitsInStock from 17 to 16
		pChang.unitsInStock = 16;
		db.update(pChang);

		Product p2 = new Product();
		Product pChang2 = db.from(p2).where(p2.productName).is("Chang").selectFirst();
		assertEquals(19.5, pChang2.unitPrice.doubleValue(), 0.001);
		assertEquals(16, pChang2.unitsInStock.intValue());

		// undo update
		pChang.unitPrice = 19.0;
		pChang.unitsInStock = 17;
		db.update(pChang);
	}

	@Test
	public void testSimpleUpdateWithCombinedPrimaryKey() {
		Order o = new Order();
		Order ourOrder = db.from(o).where(o.orderDate).is(valueOf("2007-01-02")).selectFirst();
		ourOrder.orderDate = valueOf("2007-01-03");
		db.update(ourOrder);

		Order ourUpdatedOrder = db.from(o).where(o.orderDate).is(valueOf("2007-01-03")).selectFirst();
		assertTrue("updated order not found", ourUpdatedOrder != null);

		// undo update
		ourOrder.orderDate = valueOf("2007-01-02");
		db.update(ourOrder);
	}

	@Test
	public void testSimpleMerge() {
		Product p = new Product();
		Product pChang = db.from(p).where(p.productName).is("Chang").selectFirst();
		// update unitPrice from 19.0 to 19.5
		pChang.unitPrice = 19.5;
		// update unitsInStock from 17 to 16
		pChang.unitsInStock = 16;
		db.merge(pChang);

		Product p2 = new Product();
		Product pChang2 = db.from(p2).where(p2.productName).is("Chang").selectFirst();
		assertEquals(19.5, pChang2.unitPrice, 0.001);
		assertEquals(16, pChang2.unitsInStock.intValue());

		// undo update
		pChang.unitPrice = 19.0;
		pChang.unitsInStock = 17;
		db.merge(pChang);
	}

	@Test
	public void testSimpleMergeWithCombinedPrimaryKey() {
		Order o = new Order();
		Order ourOrder = db.from(o).where(o.orderDate).is(valueOf("2007-01-02")).selectFirst();
		ourOrder.orderDate = valueOf("2007-01-03");
		db.merge(ourOrder);

		Order ourUpdatedOrder = db.from(o).where(o.orderDate).is(valueOf("2007-01-03")).selectFirst();
		assertTrue("updated order not found", ourUpdatedOrder != null);

		// undo update
		ourOrder.orderDate = valueOf("2007-01-02");
		db.merge(ourOrder);
	}

	@Test
	public void testSetColumns() {
		Product p = new Product();
		Product original = db.from(p).where(p.productId).is(1).selectFirst();

		// update string and double columns
		db.from(p).set(p.productName).to("updated").increment(p.unitPrice).by(3.14).increment(p.unitsInStock)
				.by(2).where(p.productId).is(1).update();

		// confirm the data was properly updated
		Product revised = db.from(p).where(p.productId).is(1).selectFirst();
		assertEquals("updated", revised.productName);
		assertEquals(original.unitPrice + 3.14, revised.unitPrice, 0.001);
		assertEquals(original.unitsInStock + 2, revised.unitsInStock.intValue());

		// restore the data
		db.from(p).set(p.productName).to(original.productName).set(p.unitPrice).to(original.unitPrice)
				.increment(p.unitsInStock).by(-2).where(p.productId).is(1).update();

		// confirm the data was properly restored
		Product restored = db.from(p).where(p.productId).is(1).selectFirst();
		assertEquals(original.productName, restored.productName);
		assertEquals(original.unitPrice, restored.unitPrice);
		assertEquals(original.unitsInStock, restored.unitsInStock);

		double unitPriceOld = db.from(p).where(p.productId).is(1).selectFirst().unitPrice;
		// double the unit price
		db.from(p).increment(p.unitPrice).by(p.unitPrice).where(p.productId).is(1).update();
		double unitPriceNew = db.from(p).where(p.productId).is(1).selectFirst().unitPrice;
		assertEquals(unitPriceOld * 2, unitPriceNew, 0.001);

	}

}
