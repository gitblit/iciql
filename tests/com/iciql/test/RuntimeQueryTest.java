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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.QueryWhere;
import com.iciql.test.models.EnumModels.Tree;
import com.iciql.test.models.Product;
import com.iciql.test.models.StaticQueries;
import com.iciql.util.JdbcUtils;
import com.iciql.util.Utils;

/**
 * Tests the runtime dynamic query function.
 */
public class RuntimeQueryTest {

	@Test
	public void testParameters() {
		Db db = IciqlSuite.openNewDb();
		
		// do not test non-H2 databases because dialects will get in the way
		// e.g. column quoting, etc
		Assume.assumeTrue(IciqlSuite.isH2(db));

		Product p = new Product();
		String q1 = db.from(p).where(p.unitsInStock).isParameter().and(p.productName).likeParameter().orderBy(p.productId).toSQL();
		String q2 = db.from(p).where(p.unitsInStock).lessThan(100).and(p.productName).like("test").or(p.productName).likeParameter().orderBy(p.productId).toSQL();
		
		StaticQueries.StaticModel1 m1 = new StaticQueries.StaticModel1();
		String q3 = db.from(m1).where(m1.myTree).is(Tree.MAPLE).and(m1.myTree).isParameter().toSQL();
		
		StaticQueries.StaticModel2 m2 = new StaticQueries.StaticModel2();
		String q4 = db.from(m2).where(m2.myTree).is(Tree.MAPLE).and(m2.myTree).isParameter().toSQL();

		StaticQueries.StaticModel3 m3 = new StaticQueries.StaticModel3();
		String q5 = db.from(m3).where(m3.myTree).is(Tree.MAPLE).and(m3.myTree).isParameter().toSQL();

		long now = System.currentTimeMillis();
		java.sql.Date aDate = new java.sql.Date(now);
		java.sql.Time aTime = new java.sql.Time(now);
		java.sql.Timestamp aTimestamp = new java.sql.Timestamp(now);
		
		String q6 = db.from(m1).where(m1.myDate).is(aDate).and(m1.myDate).isParameter().toSQL();
		String q7 = db.from(m1).where(m1.myTime).is(aTime).and(m1.myTime).isParameter().toSQL();
		String q8 = db.from(m1).where(m1.myTimestamp).is(aTimestamp).and(m1.myTimestamp).isParameter().toSQL();

		db.close();
		assertEquals("SELECT * FROM Product WHERE unitsInStock = ? AND productName LIKE ?  ORDER BY productId", q1);
		assertEquals("SELECT * FROM Product WHERE unitsInStock < 100 AND productName LIKE 'test' OR productName LIKE ?  ORDER BY productId", q2);
		
		assertEquals("SELECT * FROM StaticQueryTest1 WHERE myTree = 'MAPLE' AND myTree = ?", q3);
		assertEquals("SELECT * FROM StaticQueryTest2 WHERE myTree = 50 AND myTree = ?", q4);
		assertEquals("SELECT * FROM StaticQueryTest3 WHERE myTree = 4 AND myTree = ?", q5);

		java.util.Date refDate = new java.util.Date(now);
		assertEquals("SELECT * FROM StaticQueryTest1 WHERE myDate = '" + new SimpleDateFormat("yyyy-MM-dd").format(refDate) + "' AND myDate = ?", q6);
		assertEquals("SELECT * FROM StaticQueryTest1 WHERE myTime = '" + new SimpleDateFormat("HH:mm:ss").format(refDate) + "' AND myTime = ?", q7);
		assertEquals("SELECT * FROM StaticQueryTest1 WHERE myTimestamp = '" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(refDate) + "' AND myTimestamp = ?", q8);
	}
	
	@Test
	public void testRuntimeSelectWildcards() {
		Db db = IciqlSuite.openNewDb();
		
		// do not test non-H2 databases because dialects will get in the way
		// e.g. column quoting, etc
		Assume.assumeTrue(IciqlSuite.isH2(db));
		
		StaticQueries.StaticModel1 m1 = new StaticQueries.StaticModel1();
		StaticQueries.StaticModel2 m2 = new StaticQueries.StaticModel2();
		StaticQueries.StaticModel2 m3 = new StaticQueries.StaticModel2();
		
		int t0 = Utils.AS_COUNTER.get() + 1;
		int t1 = t0 + 1;
		
		QueryWhere<?> where = db.from(m1).innerJoin(m2).on(m1.id).is(m2.id).where(m2.myTree).is(Tree.MAPLE); 
		String q1 = where.toSQL(false);
		String q2 = where.toSQL(true);
		String q3 = where.toSQL(false, m1);
		String q4 = where.toSQL(true, m1);
		String q5 = where.toSQL(false, m2);
		String q6 = where.toSQL(true, m2);
		
		// test unused alias
		String q7 = where.toSQL(true, m3);
		
		db.close();
		
		assertEquals(MessageFormat.format("SELECT * FROM StaticQueryTest1 AS T{0,number,0} INNER JOIN StaticQueryTest2 AS T{1,number,0} ON T{0,number,0}.id = T{1,number,0}.id  WHERE T{1,number,0}.myTree = 50", t0, t1), q1);
		assertEquals(MessageFormat.format("SELECT DISTINCT * FROM StaticQueryTest1 AS T{0,number,0} INNER JOIN StaticQueryTest2 AS T{1,number,0} ON T{0,number,0}.id = T{1,number,0}.id  WHERE T{1,number,0}.myTree = 50", t0, t1), q2);
		
		assertEquals(MessageFormat.format("SELECT T{0,number,0}.* FROM StaticQueryTest1 AS T{0,number,0} INNER JOIN StaticQueryTest2 AS T{1,number,0} ON T{0,number,0}.id = T{1,number,0}.id  WHERE T{1,number,0}.myTree = 50", t0, t1), q3);
		assertEquals(MessageFormat.format("SELECT DISTINCT T{0,number,0}.* FROM StaticQueryTest1 AS T{0,number,0} INNER JOIN StaticQueryTest2 AS T{1,number,0} ON T{0,number,0}.id = T{1,number,0}.id  WHERE T{1,number,0}.myTree = 50", t0, t1), q4);
		
		assertEquals(MessageFormat.format("SELECT T{1,number,0}.* FROM StaticQueryTest1 AS T{0,number,0} INNER JOIN StaticQueryTest2 AS T{1,number,0} ON T{0,number,0}.id = T{1,number,0}.id  WHERE T{1,number,0}.myTree = 50", t0, t1), q5);
		assertEquals(MessageFormat.format("SELECT DISTINCT T{1,number,0}.* FROM StaticQueryTest1 AS T{0,number,0} INNER JOIN StaticQueryTest2 AS T{1,number,0} ON T{0,number,0}.id = T{1,number,0}.id  WHERE T{1,number,0}.myTree = 50", t0, t1), q6);
		
		assertEquals(MessageFormat.format("SELECT DISTINCT * FROM StaticQueryTest1 AS T{0,number,0} INNER JOIN StaticQueryTest2 AS T{1,number,0} ON T{0,number,0}.id = T{1,number,0}.id  WHERE T{1,number,0}.myTree = 50", t0, t1), q7);
	}
	
	@Test
	public void testRuntimeQuery() {
		Db db = IciqlSuite.openNewDb();
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
		Db db = IciqlSuite.openNewDb();
		db.insertAll(Product.getList());

		// test plain statement
		List<Product> products = db.executeQuery(Product.class,
				"select * from product where unitsInStock=120");
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
		Db db = IciqlSuite.openNewDb();
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
