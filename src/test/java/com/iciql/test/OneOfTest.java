/*
 * Copyright (c) 2009-2014, Architector Inc., Japan
 * All rights reserved.
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
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.iciql.Db;
import com.iciql.test.models.Customer;

public class OneOfTest {

	private Db db;

	@Before
	public void setUp() {
		db = IciqlSuite.openNewDb();
	}

	@After
	public void tearDown() {
		db.close();
	}

	@SuppressWarnings("serial")
	@Test
	public void oneOfTest() {
		Customer c;

		c = new Customer();
		assertEquals(
				db.from(c)
						.where(c.customerId).oneOf(new ArrayList<String>() {{
							this.add("a");
						}})
						.toSQL(),
				"SELECT * FROM Customer WHERE customerId IN('a')");
		assertEquals(
				db.from(c)
						.where(c.customerId).oneOf(new ArrayList<String>() {{
							this.add("a");
							this.add("b");
						}})
						.toSQL(),
				"SELECT * FROM Customer WHERE customerId IN('a', 'b')");
	}

	@SuppressWarnings("serial")
	@Test
	public void noneOfTest() {
		Customer c;

		c = new Customer();
		assertEquals(
				db.from(c)
						.where(c.customerId).noneOf(new ArrayList<String>() {{
							this.add("a");
						}})
						.toSQL(),
				"SELECT * FROM Customer WHERE customerId NOT IN('a')");
		assertEquals(
				db.from(c)
						.where(c.customerId).noneOf(new ArrayList<String>() {{
							this.add("a");
							this.add("b");
						}})
						.toSQL(),
				"SELECT * FROM Customer WHERE customerId NOT IN('a', 'b')");
	}

}
