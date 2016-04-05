/*
 * Copyright 2012 James Moger.
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
import com.iciql.test.models.ProductAnnotationOnly;
import com.iciql.test.models.ProductView;
import com.iciql.test.models.ProductViewFromQuery;
import com.iciql.test.models.ProductViewInherited;
import com.iciql.test.models.ProductViewInheritedComplex;
import com.mysql.jdbc.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test annotation processing.
 */
public class ViewsTest {

    /**
     * This object represents a database (actually a connection to the
     * database).
     */

    private Db db;

    @Before
    public void setUp() {
        db = IciqlSuite.openNewDb();
        db.insertAll(ProductAnnotationOnly.getList());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testProductView() {
        ProductView view = new ProductView();
        List<ProductView> products = db.from(view).select();
        assertEquals(5, products.size());
        for (int i = 0; i < products.size(); i++) {
            assertEquals(3 + i, products.get(i).productId.intValue());
        }
    }

    @Test
    public void testProductViewInherited() {
        ProductViewInherited view = new ProductViewInherited();
        List<ProductViewInherited> products = db.from(view).select();
        assertEquals(5, products.size());
        for (int i = 0; i < products.size(); i++) {
            assertEquals(3 + i, products.get(i).productId.intValue());
        }
    }

    @Test
    public void testComplexInheritance() {
        ProductViewInheritedComplex view = new ProductViewInheritedComplex();
        List<ProductViewInheritedComplex> products = db.from(view).select();
        assertEquals(5, products.size());
        for (int i = 0; i < products.size(); i++) {
            assertEquals(3 + i, products.get(i).productId.intValue());
            assertTrue(!StringUtils.isNullOrEmpty(products.get(i).productName));
        }
    }

    @Test
    public void testCreateViewFromQuery() {
        // create view from query
        ProductAnnotationOnly product = new ProductAnnotationOnly();
        db.from(product).where(product.productId).exceeds(2L).and(product.productId).atMost(7L).createView(ProductViewFromQuery.class);

        // select from the created view
        ProductViewFromQuery view = new ProductViewFromQuery();
        List<ProductViewFromQuery> products = db.from(view).select();
        assertEquals(5, products.size());
        for (int i = 0; i < products.size(); i++) {
            assertEquals(3 + i, products.get(i).productId.intValue());
        }

        // replace the view
        db.from(product).where(product.productId).exceeds(3L).and(product.productId).atMost(8L).replaceView(ProductViewFromQuery.class);

        // select from the replaced view
        products = db.from(view).select();
        assertEquals(5, products.size());
        for (int i = 0; i < products.size(); i++) {
            assertEquals(4 + i, products.get(i).productId.intValue());
        }
    }
}
