/*
 * Copyright 2014 James Moger.
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

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Dao;
import com.iciql.DaoClasspathStatementProvider;
import com.iciql.Db;
import com.iciql.Iciql.Mode;
import com.iciql.IciqlException;
import com.iciql.test.DataTypeAdapterTest.SerializedObjectTypeAdapterTest;
import com.iciql.test.DataTypeAdapterTest.SupportedTypesAdapter;
import com.iciql.test.models.Order;
import com.iciql.test.models.Product;
import com.iciql.test.models.SupportedTypes;

/**
 * Tests DAO dynamic proxy mechanism.
 *
 * @author James Moger
 */
public class ProductDaoTest extends Assert {

	private Db db;

	@Before
	public void setUp() throws Exception {
		db = IciqlSuite.openNewDb();
		db.insertAll(Product.getList());
		db.insertAll(Order.getList());
		db.setDaoStatementProvider(new DaoClasspathStatementProvider());
	}

	@After
	public void tearDown() {
		db.close();
	}

	@Test
	public void testQueryVoidReturnType() {

		ProductDao dao = db.open(ProductDao.class);

		try {
			dao.getWithIllegalVoid();
			assertTrue("void return type on a query should fail", false);
		} catch (IciqlException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testQueryCollectionReturnType() {

		ProductDao dao = db.open(ProductDao.class);

		try {
			dao.getWithIllegalCollection();
			assertTrue("collection return types on a query should fail", false);
		} catch (IciqlException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testQueryIgnoreDoubleDelimiter() {

		ProductDao dao = db.open(ProductDao.class);

		try {
			dao.getWithDoubleDelimiter();
			assertTrue("the double delimiter should have been ignored", false);
		} catch (IciqlException e) {
			assertTrue(true);
		}

	}

	@Test
	public void testQueryReturnModels() {

		ProductDao dao = db.open(ProductDao.class);

		Product[] products = dao.getAllProducts();
		assertEquals(10, products.length);
	}

	@Test
	public void testQueryNamedOrIndexedParameterBinding() {

		ProductDao dao = db.open(ProductDao.class);

		Product p2 = dao.getProduct(2);
		assertEquals("Chang", p2.productName);

		Product p3 = dao.getProductWithUnusedBoundParameters(true, 3, "test");
		assertEquals("Aniseed Syrup", p3.productName);

		Product p4 = dao.getProductWithUnboundParameters(true, 4, "test");
		assertEquals("Chef Anton's Cajun Seasoning", p4.productName);

		Product p5 = dao.getProductWithUnboundParameters(true, 5, "test");
		assertEquals("Chef Anton's Gumbo Mix", p5.productName);

		// test re-use of IndexedSql (manual check with debugger)
		Product p6 = dao.getProduct(6);
		assertEquals("Grandma's Boysenberry Spread", p6.productName);

	}

	@Test
	public void testJDBCPlaceholderParameterBinding() {

		ProductDao dao = db.open(ProductDao.class);

		Product p2 = dao.getProductWithJDBCPlaceholders(2);
		assertEquals("Chang", p2.productName);

	}

	@Test
	public void testQueryBeanBinding() {

		ProductDao dao = db.open(ProductDao.class);

		Product p4 = dao.getProduct(4);

		long [] products = dao.getSimilarInStockItemIds(p4);

		assertEquals("[6]", Arrays.toString(products));

	}

	@Test
	public void testQueryReturnField() {

		ProductDao dao = db.open(ProductDao.class);

		String n5 = dao.getProductName(5);
		assertEquals("Chef Anton's Gumbo Mix", n5);

		int u4 = dao.getUnitsInStock(4);
		assertEquals(53, u4);

	}

	@Test
	public void testQueryReturnFields() {

		ProductDao dao = db.open(ProductDao.class);

		long [] ids = dao.getProductIdsForCategory("Condiments");
		assertEquals("[3, 4, 5, 6, 8]", Arrays.toString(ids));

		Date date = dao.getMostRecentOrder();
		assertEquals("2007-04-11", date.toString());

	}

	@Test
	public void testUpdateIllegalReturnType() {

		ProductDao dao = db.open(ProductDao.class);

		try {
			dao.setWithIllegalReturnType();
			assertTrue("this should have been an illegal return type", false);
		} catch (IciqlException e) {
			assertTrue(true);
		}

	}

	@Test
	public void testUpdateStatements() {

		ProductDao dao = db.open(ProductDao.class);

		Product p1 = dao.getProduct(1);
		assertEquals("Chai", p1.productName);

		String name = "Tea";
		dao.setProductName(1, name);

		Product p2 = dao.getProduct(1);

		assertEquals(name, p2.productName);

	}

	@Test
	public void testUpdateStatementsReturnsSuccess() {

		ProductDao dao = db.open(ProductDao.class);

		boolean success = dao.setProductNameReturnsSuccess(1, "Tea");
		assertTrue(success);

	}

	@Test
	public void testUpdateStatementsReturnsCount() {

		ProductDao dao = db.open(ProductDao.class);

		int rows = dao.renameProductCategoryReturnsCount("Condiments", "Garnishes");
		assertEquals(5, rows);

	}

	@Test
	public void testQueryWithDataTypeAdapter() {

		// insert our custom serialized object
		SerializedObjectTypeAdapterTest row = new SerializedObjectTypeAdapterTest();
		row.received = new java.util.Date();
		row.obj = SupportedTypes.createList().get(1);
		db.insert(row);

		ProductDao dao = db.open(ProductDao.class);

		// retrieve our object with automatic data type conversion
		SupportedTypes obj = dao.getCustomDataType();
		assertNotNull(obj);
		assertTrue(row.obj.equivalentTo(obj));
	}

	@Test
	public void testUpdateWithDataTypeAdapter() {

		// insert our custom serialized object
		SerializedObjectTypeAdapterTest row = new SerializedObjectTypeAdapterTest();
		row.received = new java.util.Date();
		row.obj = SupportedTypes.createList().get(1);
		db.insert(row);

		ProductDao dao = db.open(ProductDao.class);

		final SupportedTypes obj0 = dao.getCustomDataType();
		assertNotNull(obj0);
		assertTrue(row.obj.equivalentTo(obj0));

		// update the stored object
		final SupportedTypes obj1 = SupportedTypes.createList().get(1);
		obj1.myString = "dta update successful";
		dao.setSupportedTypes(1, obj1);

		// retrieve and validate the update took place
		final SupportedTypes obj2 = dao.getCustomDataType();

		assertNotNull(obj2);
		assertEquals("dta update successful", obj2.myString);

		assertTrue(obj1.equivalentTo(obj2));
	}

	@Test
	public void testDefaultProdResourceQueryReturnModels() {

		ProductDao dao = db.open(ProductDao.class);

		Product[] products = dao.getProductsFromResourceQuery();
		assertEquals(10, products.length);
	}

	@Test
	public void testDevResourceQueryReturnModels() {

		Db db = IciqlSuite.openNewDb(Mode.DEV);
		db.insertAll(Product.getList());
		db.insertAll(Order.getList());
		db.setDaoStatementProvider(new DaoClasspathStatementProvider());

		ProductDao dao = db.open(ProductDao.class);

		Product[] products = dao.getProductsFromResourceQuery();
		assertEquals(5, products.length);

		db.close();
	}

	@Test
	public void testTestResourceQueryReturnModels() {

		Db db = IciqlSuite.openNewDb(Mode.TEST);
		db.insertAll(Product.getList());
		db.insertAll(Order.getList());
		db.setDaoStatementProvider(new DaoClasspathStatementProvider());

		ProductDao dao = db.open(ProductDao.class);

		Product[] products = dao.getProductsFromResourceQuery();
		assertEquals(2, products.length);

		db.close();
	}

	/**
	 * Define the Product DAO interface.
	 */
	public interface ProductDao extends Dao {

		@SqlQuery("select * from Product")
		void getWithIllegalVoid();

		@SqlQuery("select * from Product")
		List<Product> getWithIllegalCollection();

		@SqlQuery("select * from Product where ::id = 1")
		Product getWithDoubleDelimiter();

		@SqlQuery("select * from Product")
		Product[] getAllProducts();

		@SqlQuery("select * from Product where productId = :id")
		Product getProduct(@Bind("id") long id);

		@SqlQuery("select * from Product where productId = :id")
		Product getProductWithUnusedBoundParameters(
				@Bind("irrelevant") boolean whocares,
				@Bind("id") long id,
				@Bind("dontmatter") String something);

		@SqlQuery("select * from Product where productId = :arg1")
		Product getProductWithUnboundParameters(
				boolean whocares,
				long id,
				String something);

		@SqlQuery("select * from Product where productId = :?")
		Product getProductWithJDBCPlaceholders(long id);

		@SqlQuery("select productId from Product where unitsInStock > :p.unitsInStock and category = :p.category")
		long[] getSimilarInStockItemIds(@BindBean("p") Product p);

		@SqlQuery("select productName from Product where productId = :?")
		String getProductName(long id);

		@SqlQuery("select unitsInStock from Product where productId = :?")
		int getUnitsInStock(long id);

		@SqlQuery("select productId from Product where category = :category")
		long[] getProductIdsForCategory(@Bind("category") String cat);

		// will break ResultSet iteration after retrieving first value
		@SqlQuery("select orderDate from Orders order by orderDate desc")
		Date getMostRecentOrder();

		@SqlStatement("update Product set productName = 'test' where productId = 1")
		String setWithIllegalReturnType();

		@SqlStatement("update Product set productName = :name where productId = :id")
		void setProductName(@Bind("id") long id, @Bind("name") String name);

		@SqlStatement("update Product set productName = :name where productId = :id")
		boolean setProductNameReturnsSuccess(@Bind("id") long id, @Bind("name") String name);

		@SqlStatement("update Product set category = :newCategory where category = :oldCategory")
		int renameProductCategoryReturnsCount(@Bind("oldCategory") String oldCategory, @Bind("newCategory") String newCategory);

		// will break ResultSet iteration after retrieving first value
		@SqlQuery("select obj from dataTypeAdapters")
		@SupportedTypesAdapter
		SupportedTypes getCustomDataType();

		@SqlStatement("update dataTypeAdapters set obj=:2 where id=:1")
		boolean setSupportedTypes(long id, @SupportedTypesAdapter SupportedTypes obj);

		@SqlQuery("get.products")
		Product[] getProductsFromResourceQuery();

	}
}
