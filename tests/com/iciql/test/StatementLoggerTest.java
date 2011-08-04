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

import org.junit.Test;

import com.iciql.Db;
import com.iciql.test.models.Product;
import com.iciql.util.StatementLogger;

/**
 * Tests the statement logger. 
 */
public class StatementLoggerTest {

	@Test
	public void testStatementLogger() {
		StatementLogger.activateConsoleLogger();
		Db db = Db.open("jdbc:h2:mem:", "sa", "sa");
		db.insertAll(Product.getList());
		db.close();
		StatementLogger.logStats();
		StatementLogger.deactivateConsoleLogger();
	}
}
