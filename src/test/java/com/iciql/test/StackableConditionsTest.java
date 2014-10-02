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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.iciql.Conditions.And;
import com.iciql.Conditions.Or;
import com.iciql.Db;
import com.iciql.QueryWhere;
import com.iciql.IciqlException;
import com.iciql.test.models.Customer;

public class StackableConditionsTest {

	private Db db;

	@Before
	public void setUp() {
		db = IciqlSuite.openNewDb();
	}

	@After
	public void tearDown() {
		db.close();
	}

	private String search(Iterable<String> customerIds, String region) {
		Customer model;
		QueryWhere<Customer> query;

		model = new Customer();
		query = db.from(model).whereTrue();
		if (customerIds != null) {
			query.andOpenFalse();
			for (String value : customerIds) {
				query.or(model.customerId).is(value);
			}
			query.close();
		}
		if (region != null) {
			query.and(model.region).is(region);
		}
		return query.toSQL();
	}

	@SuppressWarnings("serial")
	@Test
	public void andOrTest() {
		assertEquals(
				search(
						null,
						null),
				"SELECT * FROM Customer WHERE (true)");
		assertEquals(
				search(
						new ArrayList<String>() {{
						}},
						null),
				"SELECT * FROM Customer WHERE (true) AND ( (false) )");
		assertEquals(
				search(
						new ArrayList<String>() {{
							this.add("0001");
						}},
						null),
				"SELECT * FROM Customer WHERE (true) AND ( (false) OR customerId = '0001' )");
		assertEquals(
				search(
						new ArrayList<String>() {{
							this.add("0001");
							this.add("0002");
						}},
						null),
				"SELECT * FROM Customer WHERE (true) AND ( (false) OR customerId = '0001' OR customerId = '0002' )");
		assertEquals(
				search(
						null,
						"JP"),
				"SELECT * FROM Customer WHERE (true) AND region = 'JP'");
		assertEquals(
				search(
						new ArrayList<String>() {{
						}},
						"JP"),
				"SELECT * FROM Customer WHERE (true) AND ( (false) ) AND region = 'JP'");
		assertEquals(
				search(
						new ArrayList<String>() {{
							this.add("0001");
						}},
						"JP"),
				"SELECT * FROM Customer WHERE (true) AND ( (false) OR customerId = '0001' ) AND region = 'JP'");
		assertEquals(
				search(
						new ArrayList<String>() {{
							this.add("0001");
							this.add("0002");
						}},
						"JP"),
				"SELECT * FROM Customer WHERE (true) AND ( (false) OR customerId = '0001' OR customerId = '0002' ) AND region = 'JP'");
	}

	@Test
	public void errorTest() {
		Customer model;

		model = new Customer();
		try {
			db.from(model)
					.where(model.customerId).is("0001")
					.andOpenFalse()
							.or(model.region).is("FR")
							.or(model.region).is("JP")
					.close()
					.toSQL();
			assertTrue(true);
		}
		catch (IciqlException error) {
			assertTrue(false);
		}
		try {
			db.from(model)
					.where(model.customerId).is("0001")
					.andOpenFalse()
							.or(model.region).is("FR")
							.or(model.region).is("JP")
							.toSQL();
			assertTrue(false);
		}
		catch (IciqlException error) {
			assertTrue(true);
		}
		try {
			db.from(model)
					.where(model.customerId).is("0001")
					.andOpenFalse()
							.or(model.region).is("FR")
							.or(model.region).is("JP")
					.close()
			.close();
			assertTrue(false);
		}
		catch (IciqlException error) {
			assertTrue(true);
		}
	}

	@Test
	public void fluentTest() {
		final Customer model = new Customer();
		assertEquals(
				db.from(model).where(new And<Customer>(db, model) {{
					and(model.customerId).is("0001");
					and(new Or<Customer>(db, model) {{
						or(model.region).is("CA");
						or(model.region).is("LA");
					}});
				}}).toSQL(),
				"SELECT * FROM Customer WHERE (true) AND customerId = '0001' AND ( (false) OR region = 'CA' OR region = 'LA' )");
		assertEquals(
				db.from(model).where(new Or<Customer>(db, model) {{
					or(model.customerId).is("0001");
					or(new And<Customer>(db, model) {{
						and(model.customerId).is("0002");
						and(model.region).is("LA");
					}});
				}}).toSQL(),
				"SELECT * FROM Customer WHERE (false) OR customerId = '0001' OR ( (true) AND customerId = '0002' AND region = 'LA' )");
		assertEquals(
				db.from(model)
						.where(model.customerId).isNotNull()
						.and(new Or<Customer>(db, model) {{
							or(model.region).is("LA");
							or(model.region).is("CA");
						}})
						.and(model.region).isNotNull()
						.toSQL(),
				"SELECT * FROM Customer WHERE customerId IS NOT NULL AND ( (false) OR region = 'LA' OR region = 'CA' ) AND region IS NOT NULL");
	}

}
