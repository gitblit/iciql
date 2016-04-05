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

import com.iciql.Db;
import com.iciql.test.models.Customer;
import com.iciql.test.models.PrimitivesModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class OneOfTest {

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

    @SuppressWarnings("serial")
    @Test
    public void oneOfSyntaxTest() {
        String PrimitivesTest = db.getDialect().prepareTableName(null, "PrimitivesTest");
        String Customer = db.getDialect().prepareTableName(null, "Customer");
        String myInteger = db.getDialect().prepareColumnName("myInteger");
        String customerId = db.getDialect().prepareColumnName("customerId");

        PrimitivesModel p = new PrimitivesModel();
        assertEquals(
                String.format("SELECT * FROM %s WHERE %s IN(0)", PrimitivesTest, myInteger),
                db.from(p)
                        .where(p.myInteger).oneOf(0)
                        .toSQL());
        assertEquals(
                String.format("SELECT * FROM %s WHERE %s IN(0, 1)", PrimitivesTest, myInteger),
                db.from(p)
                        .where(p.myInteger).oneOf(0, 1)
                        .toSQL());
        Customer c = new Customer();
        assertEquals(
                String.format("SELECT * FROM %s WHERE %s IN('a')", Customer, customerId),
                db.from(c)
                        .where(c.customerId).oneOf(new ArrayList<String>() {{
                    this.add("a");
                }})
                        .toSQL());
        assertEquals(
                String.format("SELECT * FROM %s WHERE %s IN('a', 'b')", Customer, customerId),
                db.from(c)
                        .where(c.customerId).oneOf(new ArrayList<String>() {{
                    this.add("a");
                    this.add("b");
                }})
                        .toSQL());
    }

    @SuppressWarnings("serial")
    @Test
    public void noneOfSyntaxTest() {
        String PrimitivesTest = db.getDialect().prepareTableName(null, "PrimitivesTest");
        String Customer = db.getDialect().prepareTableName(null, "Customer");
        String myInteger = db.getDialect().prepareColumnName("myInteger");
        String customerId = db.getDialect().prepareColumnName("customerId");

        PrimitivesModel p = new PrimitivesModel();
        assertEquals(
                String.format("SELECT * FROM %s WHERE %s NOT IN(0)", PrimitivesTest, myInteger),
                db.from(p)
                        .where(p.myInteger).noneOf(0)
                        .toSQL());
        assertEquals(
                String.format("SELECT * FROM %s WHERE %s NOT IN(0, 1)", PrimitivesTest, myInteger),
                db.from(p)
                        .where(p.myInteger).noneOf(0, 1)
                        .toSQL());
        Customer c = new Customer();
        assertEquals(
                String.format("SELECT * FROM %s WHERE %s NOT IN('a')", Customer, customerId),
                db.from(c)
                        .where(c.customerId).noneOf(new ArrayList<String>() {{
                    this.add("a");
                }})
                        .toSQL());
        assertEquals(
                String.format("SELECT * FROM %s WHERE %s NOT IN('a', 'b')", Customer, customerId),
                db.from(c)
                        .where(c.customerId).noneOf(new ArrayList<String>() {{
                    this.add("a");
                    this.add("b");
                }})
                        .toSQL());
    }

    public void noneOfTest() {
        Customer c = new Customer();
        List<Customer> meAndny = db.from(c).where(c.region).noneOf("WA", "CA", "LA").select();
        assertEquals(2, meAndny.size());

        Set<String> regions = new TreeSet<String>();
        for (Customer customer : meAndny) {
            regions.add(customer.region);
        }
        assertEquals("[ME, NY]", regions.toString());
    }

    public void oneOfTest() {
        Customer c = new Customer();
        List<Customer> meAndny = db.from(c).where(c.region).oneOf("ME", "NY").select();
        assertEquals(2, meAndny.size());

        Set<String> regions = new TreeSet<String>();
        for (Customer customer : meAndny) {
            regions.add(customer.region);
        }
        assertEquals("[ME, NY]", regions.toString());
    }

}
