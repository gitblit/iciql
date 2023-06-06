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
import com.iciql.IciqlException;
import com.iciql.test.models.EnumModels;
import com.iciql.test.models.EnumModels.EnumIdModel;
import com.iciql.test.models.EnumModels.EnumOrdinalModel;
import com.iciql.test.models.EnumModels.EnumStringModel;
import com.iciql.test.models.EnumModels.Genus;
import com.iciql.test.models.EnumModels.Tree;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests enum support.
 */
public class EnumsTest {

    private Db db;

    @Before
    public void setUp() {
        db = IciqlSuite.openNewDb();
        db.insertAll(EnumIdModel.createList());
        db.insertAll(EnumOrdinalModel.createList());
        db.insertAll(EnumStringModel.createList());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testEnumQueries() {
        testIntEnums(new EnumIdModel());
        testIntEnums(new EnumOrdinalModel());
        testStringEnums(new EnumStringModel());
        testStringEnumIds(new EnumStringModel());
        testStringEnumsSelectObject();
    }

    private void testIntEnums(EnumModels e) {
        // ensure all records inserted
        long count = db.from(e).selectCount();
        assertEquals(5, count);

        // special case:
        // value is first enum constant which is also the alias object.
        // the first enum constant is used as the alias because we can not
        // instantiate an enum reflectively.
        EnumModels firstEnumValue = db.from(e).where(e.tree()).is(Tree.PINE).selectFirst();
        assertEquals(Tree.PINE, firstEnumValue.tree());

        EnumModels model = db.from(e).where(e.tree()).is(Tree.WALNUT).selectFirst();

        assertEquals(400, model.id.intValue());
        assertEquals(Tree.WALNUT, model.tree());

        List<EnumModels> list = db.from(e).where(e.tree()).atLeast(Tree.BIRCH).select();
        assertEquals(3, list.size());

        // between is an int compare
        list = db.from(e).where(e.tree()).between(Tree.BIRCH).and(Tree.WALNUT).select();
        assertEquals(2, list.size());

    }

    private void testStringEnums(EnumModels e) {
        // ensure all records inserted
        long count = db.from(e).selectCount();
        assertEquals(5, count);

        // special case:
        // value is first enum constant which is also the alias object.
        // the first enum constant is used as the alias because we can not
        // instantiate an enum reflectively.
        EnumModels firstEnumValue = db.from(e).where(e.tree()).is(Tree.PINE).selectFirst();
        assertEquals(Tree.PINE, firstEnumValue.tree());

        EnumModels model = db.from(e).where(e.tree()).is(Tree.WALNUT).selectFirst();

        assertEquals(400, model.id.intValue());
        assertEquals(Tree.WALNUT, model.tree());

        List<EnumModels> list = db.from(e).where(e.tree()).isNot(Tree.BIRCH).select();
        assertEquals(count - 1, list.size());

        // between is a string compare
        list = db.from(e).where(e.tree()).between(Tree.MAPLE).and(Tree.PINE).select();
        assertEquals(3, list.size());
    }

    private void testStringEnumsSelectObject() {
        final EnumOrdinalModel eom = new EnumOrdinalModel();
        final EnumStringModel esm = new EnumStringModel();
        // ensure all records inserted
        List<EnumJoin> enumJoinList = db.from(eom)
            .innerJoin(esm).on(eom.id).is(esm.id)
            .orderBy(eom.id)
            .select(
                new EnumJoin() {
                    {
                        id = eom.id;
                        genus = esm.genus();
                    }
                });

        assertEquals(5, enumJoinList.size());
        assertEquals(enumJoinList.get(0).id.intValue(), 100);
        assertEquals(enumJoinList.get(0).genus, Genus.PINUS);
        assertEquals(enumJoinList.get(1).id.intValue(), 200);
        assertEquals(enumJoinList.get(1).genus, Genus.QUERCUS);
        assertEquals(enumJoinList.get(2).id.intValue(), 300);
        assertEquals(enumJoinList.get(2).genus, Genus.BETULA);
        assertEquals(enumJoinList.get(3).id.intValue(), 400);
        assertEquals(enumJoinList.get(3).genus, Genus.JUGLANS);
        assertEquals(enumJoinList.get(4).id.intValue(), 500);
        assertEquals(enumJoinList.get(4).genus, Genus.ACER);

        List<EnumJoin> enumJoinWhereList = db.from(eom)
            .innerJoin(esm).on(eom.id).is(esm.id)
            .where(esm.tree()).is(Tree.OAK)
            .select(
                new EnumJoin() {
                    {
                        id = eom.id;
                        genus = esm.genus();
                    }
                });
        
        assertEquals(1, enumJoinWhereList.size());
        assertEquals(enumJoinWhereList.get(0).id.intValue(), 200);
        assertEquals(enumJoinWhereList.get(0).genus, Genus.QUERCUS);
    
        List<EnumJoin> enumJoinGroupByList = db.from(eom)
            .innerJoin(esm).on(eom.id).is(esm.id)
            .where(esm.tree()).is(Tree.BIRCH)
            .groupBy(eom.id, esm.genus())
            .select(
                new EnumJoin() {
                    {
                        id = eom.id;
                        genus = esm.genus();
                    }
                });
        
        assertEquals(1, enumJoinGroupByList.size());
        assertEquals(enumJoinGroupByList.get(0).id.intValue(), 300);
        assertEquals(enumJoinGroupByList.get(0).genus, Genus.BETULA);
    }

    private void testStringEnumIds(EnumModels e) {
        // ensure all records inserted
        long count = db.from(e).selectCount();
        assertEquals(5, count);

        // special case:
        // value is first enum constant which is also the alias object.
        // the first enum constant is used as the alias because we can not
        // instantiate an enum reflectively.
        EnumModels firstEnumValue = db.from(e).where(e.genus()).is(Genus.PINUS).selectFirst();
        assertEquals(Tree.PINE, firstEnumValue.tree());
        assertEquals(Genus.PINUS, firstEnumValue.genus());

        EnumModels model = db.from(e).where(e.genus()).is(Genus.JUGLANS).selectFirst();

        assertEquals(400, model.id.intValue());
        assertEquals(Tree.WALNUT, model.tree());
        assertEquals(Genus.JUGLANS, model.genus());

        List<EnumModels> list = db.from(e).where(e.genus()).isNot(Genus.BETULA).select();
        assertEquals(count - 1, list.size());

    }

    @Test
    public void testMultipleEnumInstances() {
        BadEnums b = new BadEnums();
        try {
            db.from(b).where(b.tree1).is(Tree.BIRCH).and(b.tree2).is(Tree.MAPLE).getSQL();
            assertTrue("Failed to detect multiple Tree fields?!", false);
        } catch (IciqlException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Can not explicitly reference Tree"));
        }
    }

    public static class BadEnums {
        Tree tree1 = Tree.BIRCH;
        Tree tree2 = Tree.MAPLE;
    }
}
