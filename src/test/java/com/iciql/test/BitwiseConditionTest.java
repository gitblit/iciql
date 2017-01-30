/*
 * Copyright 2017 James Moger.
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
import com.iciql.test.models.Product;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author James Moger
 */
public class BitwiseConditionTest {

    /**
     * This object represents a database (actually a connection to the
     * database).
     */

    private Db db;

    @Before
    public void setUp() {
        db = IciqlSuite.openNewDb();
        db.insertAll(Product.getList());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testBitwiseAndWhereCondition() throws SQLException {
        Product products = new Product();
        List<Product> list = db.from(products).where(products.unitsInStock).bitAnd(1).exceeds(0).select();

        assertTrue("Bitwise AND list is empty!", !list.isEmpty());
        assertEquals("Unexpected results for bitwise AND test", 7, list.size());
        Set<Integer> ids = new HashSet<Integer>();
        for (Product p : list) {
            ids.add(p.getId());
        }
        assertTrue("Unexpected id in Bitwise AND where condition test", ids.containsAll(Arrays.asList(1, 2, 3, 4, 7, 9, 10)));
    }

    @Test
    public void testBitwiseXorWhereCondition() throws SQLException {
        Product products = new Product();
        List<Product> list = db.from(products).where(products.unitsInStock).bitXor(39).exceeds(0).select();

        assertTrue("Bitwise XOR list is empty!", !list.isEmpty());
        assertEquals("Unexpected results for bitwise XOR test", 9, list.size());
        Set<Integer> ids = new HashSet<Integer>();
        for (Product p : list) {
            ids.add(p.getId());
        }
        assertTrue("Expected id in Bitwise XOR where condition test", ids.containsAll(Arrays.asList( 2, 3, 4, 5, 6, 7, 8, 9, 10)));
    }

}
