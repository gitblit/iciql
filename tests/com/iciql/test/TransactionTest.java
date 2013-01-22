/*
 * Copyright 2012 Frédéric Gaillard.
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

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.IciqlException;
import com.iciql.test.models.CategoryAnnotationOnly;
import com.iciql.test.models.ProductAnnotationOnlyWithForeignKey;

/**
 * Tests of transactions.
 */
public class TransactionTest {

	/**
	 * This object represents a database (actually a connection to the
	 * database).
	 */

	private Db db;

	@Before
	public void setUp() {
		db = IciqlSuite.openNewDb();
		
		// tables creation
		db.from(new CategoryAnnotationOnly());
		db.from(new ProductAnnotationOnlyWithForeignKey());
		
		startTransactionMode();
	}

	@After
	public void tearDown() {
		
		endTransactionMode();
		
		db.dropTable(ProductAnnotationOnlyWithForeignKey.class);
		db.dropTable(CategoryAnnotationOnly.class);
		db.close();
	}

	@Test
	public void testTransaction() {
		
		// insert in 2 tables inside a transaction
		
		// insertAll don't use save point in this transaction
		db.insertAll(CategoryAnnotationOnly.getList());
		db.insertAll(ProductAnnotationOnlyWithForeignKey.getList());

		// don't commit changes
		try {
			db.getConnection().rollback();
		} catch (SQLException e) {
			throw new IciqlException(e, "Can't rollback");
		}
		
		ProductAnnotationOnlyWithForeignKey p = new ProductAnnotationOnlyWithForeignKey();
		long count1 = db.from(p).selectCount();
		
		CategoryAnnotationOnly c = new CategoryAnnotationOnly();
		long count2 = db.from(c).selectCount();
		
		// verify changes aren't committed
		assertEquals(count1, 0L);
		assertEquals(count2, 0L);
	}

	/**
	 * Helper to set transaction mode
	 */
	private void startTransactionMode() {
		db.setSkipCreate(true);
		db.setAutoSavePoint(false);
		
		try {
			db.getConnection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new IciqlException(e, "Could not change auto-commit mode");
		}
	}
	
	/**
	 * Helper to return to initial mode
	 */
	private void endTransactionMode() {
		try {
			db.getConnection().setAutoCommit(true);
		} catch (SQLException e) {
			throw new IciqlException(e, "Could not change auto-commit mode");
		}
		// returns to initial states
		db.setSkipCreate(false);
		db.setAutoSavePoint(true);
	}
	
}
