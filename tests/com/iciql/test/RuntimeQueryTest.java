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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.iciql.Db;
import com.iciql.test.models.Product;
import com.iciql.util.JdbcUtils;

/**
 * Tests the runtime dynamic query function.
 */
public class RuntimeQueryTest {

	@Test
	public void testRuntimeQuery() {
		Db db = IciqlSuite.openDb();
		db.insertAll(Product.getList());

		Product p = new Product();
		List<Product> products = db.from(p).where("unitsInStock=?", 120).orderBy(p.productId).select();
		assertEquals(1, products.size());

		products = db.from(p).where("unitsInStock=? and productName like ? order by productId", 0, "Chef%")
				.select();
		assertEquals(1, products.size());

		db.close();
	}

	@Test
	public void testExecuteQuery() throws SQLException {
		Db db = IciqlSuite.openDb();
		db.insertAll(Product.getList());

		// test plain statement
		List<Product> products = db.executeQuery(Product.class, "select * from product where unitsInStock=120");
		assertEquals(1, products.size());
		assertEquals("Condiments", products.get(0).category);
		
		// test prepared statement
		products = db.executeQuery(Product.class, "select * from product where unitsInStock=?", 120);
		assertEquals(1, products.size());
		assertEquals("Condiments", products.get(0).category);

		db.close();
	}
	
	@Test
	public void testBuildObjects() throws SQLException {
		Db db = IciqlSuite.openDb();
		db.insertAll(Product.getList());

		// test plain statement
		ResultSet rs = db.executeQuery("select * from product where unitsInStock=120");
		List<Product> products = db.buildObjects(Product.class, rs);
		JdbcUtils.closeSilently(rs, true);

		assertEquals(1, products.size());
		assertEquals("Condiments", products.get(0).category);
		
		// test prepared statement
		rs = db.executeQuery("select * from product where unitsInStock=?", 120);
		products = db.buildObjects(Product.class, rs);
		JdbcUtils.closeSilently(rs, true);

		assertEquals(1, products.size());
		assertEquals("Condiments", products.get(0).category);

		db.close();
	}
}
