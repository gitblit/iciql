package com.iciql.test;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.iciql.Db;
import com.iciql.test.models.Customer;

public class PatchTest {

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
	public void test() {
		Customer c;

		c = new Customer();
		System.out.println(
				db.from(c)
						.whereTrue()
						.toSQL());
		System.out.println(
				db.from(c)
						.whereTrue()
						.and(c.customerId).oneOf(new ArrayList<String>() {{
							this.add("a");
						}})
						.toSQL());
		System.out.println(
				db.from(c)
						.whereTrue()
						.and(c.customerId).oneOf(new ArrayList<String>() {{
							this.add("a");
							this.add("b");
						}})
						.toSQL());
		System.out.println(
				db.from(c)
						.whereTrue()
						.and(c.customerId).oneOf(new ArrayList<String>() {{
							this.add("a");
							this.add("b");
						}})
						.andOpenFalse()
						.or(c.region).is("ja")
						.or(c.region).is("fr")
						.close()
						.toSQL());
	}

}
