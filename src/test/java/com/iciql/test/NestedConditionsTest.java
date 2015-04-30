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
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.IciqlException;
import com.iciql.NestedConditions.And;
import com.iciql.NestedConditions.Or;
import com.iciql.QueryWhere;
import com.iciql.test.models.Customer;

public class NestedConditionsTest {

	enum Region {
		JP, FR
	}

	private Db db;

	@Before
	public void setUp() {
		db = IciqlSuite.openNewDb();
		db.insertAll(Customer.getList());
	}

	@After
	public void tearDown() {
		db.close();
	}

	private String search(Region region, String... customerIds) {
		Customer model;
		QueryWhere<Customer> query;

		model = new Customer();
		query = db.from(model).whereTrue();

		if (customerIds != null && customerIds.length > 0) {
			query.andOpen();
			for (String value : customerIds) {
				query.or(model.customerId).is(value);
			}
			query.close();
		}

		if (region != null) {
			query.and(model.region).is(region.name());
		}
		return query.toSQL();
	}

	@Test
	public void andOrSyntaxTest() {
		String Customer = db.getDialect().prepareTableName(null,  "Customer");
		String customerId = db.getDialect().prepareColumnName("customerId");
		String region = db.getDialect().prepareColumnName("region");
		String trueValue = db.getDialect().prepareStringParameter(true);

		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s)", Customer, trueValue),
				search(null, (String[]) null));
		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s)", Customer, trueValue),
				search(null, new String[0]));
		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) AND ( %s = '0001' )",
						Customer, trueValue, customerId),
				search(null, "0001"));
		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) AND ( %s = '0001' OR %s = '0002' )",
						Customer, trueValue, customerId, customerId),
				search(null, "0001", "0002"));
		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) AND %s = 'JP'",
						Customer, trueValue, region),
				search(Region.JP, (String[]) null));
		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) AND %s = 'JP'",
						Customer, trueValue, region),
				search(Region.JP, new String[0]));
		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) AND ( %s = '0001' ) AND %s = 'JP'",
						Customer, trueValue, customerId, region),
				search(Region.JP, "0001"));
		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) AND ( %s = '0001' OR %s = '0002' ) AND %s = 'JP'",
						Customer, trueValue, customerId, customerId, region),
				search(Region.JP, "0001", "0002"));
	}

	@Test
	public void errorTest() {
		Customer model;

		model = new Customer();
		try {
			db.from(model)
					.where(model.customerId).is("0001")
					.andOpen()
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
					.andOpen()
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
					.andOpen()
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
	public void fluentSyntaxTest() {
		String Customer = db.getDialect().prepareTableName(null,  "Customer");
		String customerId = db.getDialect().prepareColumnName("customerId");
		String region = db.getDialect().prepareColumnName("region");
		String trueValue = db.getDialect().prepareStringParameter(true);
		String falseValue = db.getDialect().prepareStringParameter(false);

		final Customer model = new Customer();

		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) AND %s = '0001' AND ( %s = 'CA' OR %s = 'LA' )",
						Customer, trueValue, customerId, region, region),

				db.from(model).where(new And<Customer>(db, model) {{

					and(model.customerId).is("0001");
					and(new Or<Customer>(db, model) {{
						or(model.region).is("CA");
						or(model.region).is("LA");
					}});

				}})

				.toSQL());

		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) OR %s = '0001' OR ( %s = '0002' AND %s = 'LA' )",
						Customer, falseValue, customerId, customerId, region),

				db.from(model).where(new Or<Customer>(db, model) {{

					or(model.customerId).is("0001");

					or(new And<Customer>(db, model) {{
						and(model.customerId).is("0002");
						and(model.region).is("LA");
					}});

				}})

				.toSQL());

		assertEquals(
				String.format("SELECT * FROM %s WHERE (%s) OR ( %s = '0001' AND %s = 'WA' ) OR ( %s = '0002' AND %s = 'LA' )",
						Customer, falseValue, customerId, region, customerId, region),

				db.from(model).where(new Or<Customer>(db, model) {{

					or(new And<Customer>(db, model) {{
						and(model.customerId).is("0001");
						and(model.region).is("WA");
					}});

					or(new And<Customer>(db, model) {{
						and(model.customerId).is("0002");
						and(model.region).is("LA");
					}});

				}})

				.toSQL());

		assertEquals(
				String.format("SELECT * FROM %s WHERE %s = '0001' OR ( %s = '0002' AND %s = 'LA' )",
						Customer, customerId, customerId, region),

				db.from(model).where(model.customerId).is("0001")

					.or(new And<Customer>(db, model) {{
						and(model.customerId).is("0002");
						and(model.region).is("LA");
					}})

					.toSQL());


		assertEquals(
				String.format("SELECT * FROM %s WHERE %s IS NOT NULL AND ( %s = 'LA' OR %s = 'CA' OR %s = 'WA' )",
						Customer, customerId, region, region, region),
				db.from(model)
						.where(model.customerId).isNotNull()

						.and(new Or<Customer>(db, model) {{
							or(model.region).is("LA");
							or(model.region).is("CA");
							or(model.region).is("WA");
						}})

						.toSQL());
	}

	@Test
	public void compoundConditionsTest() {
		final Customer c = new Customer();
		List<Customer> matches = db.from(c)
				.where(c.customerId).like("A%")
				.and(c.region).isNotNull()
				.and(new Or<Customer>(db, c) {{
					or(c.region).is("LA");
					or(c.region).is("CA");
				}}).select();

		assertEquals(2, matches.size());

		Set<String> ids = new TreeSet<String>();
		for (Customer customer : matches) {
			ids.add(customer.customerId);
		}
		assertEquals("[ANTON, ASLAN]", ids.toString());

	}

}
