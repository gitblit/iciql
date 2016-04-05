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

import com.iciql.Db;
import com.iciql.test.models.BooleanModel;
import com.iciql.test.models.BooleanModel.BooleanAsIntModel;
import com.iciql.test.models.BooleanModel.BooleanAsPrimitiveShortModel;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests interchangeable mapping of INT columns with Booleans and BOOL columns
 * with Integers.
 * <ul>
 * <li>mapping a BIT/BOOLEAN column as an Integer
 * <li>mapping a INT column as a Boolean.
 * <li>mapping a BIT/BOOLEAN column as a primitive short
 * </ul>
 */
public class BooleanModelTest {

    @Test
    public void testBooleanColumn() {
        Db db = IciqlSuite.openNewDb();
        db.insertAll(BooleanModel.getList());
        BooleanAsIntModel b = new BooleanAsIntModel();
        List<BooleanAsIntModel> models = db.from(b).select();
        int count = 0;
        for (BooleanAsIntModel model : models) {
            if ((model.id % 2) == 1) {
                // assert that odd ids are true
                assertTrue(model.mybool > 0);
            } else {
                // assert that even ids are false
                assertTrue(model.mybool == 0);
            }

            // count true values
            if (model.mybool > 0) {
                count++;
            }
        }
        assertEquals(2, count);

        // invert boolean values and update
        for (BooleanAsIntModel model : models) {
            model.mybool = model.mybool > 0 ? 0 : 1;
        }
        db.updateAll(models);

        // check even ids are true
        models = db.from(b).select();
        for (BooleanAsIntModel model : models) {
            if ((model.id % 2) == 1) {
                // assert that odd ids are false
                assertTrue(model.mybool == 0);
            } else {
                // assert that even ids are true
                assertTrue(model.mybool > 0);
            }
        }
        db.close();
    }

    @Test
    public void testIntColumn() {
        Db db = IciqlSuite.openNewDb();
        // insert INT column
        db.insertAll(BooleanAsIntModel.getList());

        // select all rows with INT column and map to Boolean
        BooleanModel b = new BooleanModel();
        List<BooleanModel> models = db.from(b).select();
        int count = 0;
        for (BooleanModel model : models) {
            if ((model.id % 2) == 1) {
                // assert that odd ids are true
                assertTrue(model.mybool);
            } else {
                // assert that even ids are false
                assertTrue(!model.mybool);
            }

            // count true values
            if (model.mybool) {
                count++;
            }
        }
        assertEquals(2, count);

        // invert boolean values and update
        for (BooleanModel model : models) {
            model.mybool = !model.mybool;
        }
        db.updateAll(models);

        // check even ids are true
        models = db.from(b).select();
        for (BooleanModel model : models) {
            if ((model.id % 2) == 1) {
                // assert that odd ids are false
                assertTrue(!model.mybool);
            } else {
                // assert that even ids are true
                assertTrue(model.mybool);
            }
        }
        db.close();
    }

    @Test
    public void testPrimitiveShortBooleanColumn() {
        Db db = IciqlSuite.openNewDb();
        db.insertAll(BooleanModel.getList());
        BooleanAsPrimitiveShortModel b = new BooleanAsPrimitiveShortModel();
        List<BooleanAsPrimitiveShortModel> models = db.from(b).select();
        int count = 0;
        for (BooleanAsPrimitiveShortModel model : models) {
            if ((model.id % 2) == 1) {
                // assert that odd ids are true
                assertTrue(model.mybool > 0);
            } else {
                // assert that even ids are false
                assertTrue(model.mybool == 0);
            }

            // count true values
            if (model.mybool > 0) {
                count++;
            }
        }
        assertEquals(2, count);

        // invert boolean values and update
        for (BooleanAsPrimitiveShortModel model : models) {
            model.mybool = (short) (model.mybool > 0 ? 0 : 1);
        }
        db.updateAll(models);

        // check even ids are true
        models = db.from(b).select();
        for (BooleanAsPrimitiveShortModel model : models) {
            if ((model.id % 2) == 1) {
                // assert that odd ids are false
                assertTrue(model.mybool == 0);
            } else {
                // assert that even ids are true
                assertTrue(model.mybool > 0);
            }
        }
        db.close();
    }
}
