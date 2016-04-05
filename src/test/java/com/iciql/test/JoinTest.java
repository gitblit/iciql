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
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;
import com.iciql.QueryWhere;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests of Joins.
 */
public class JoinTest {

    Db db;

    @Before
    public void setup() {
        db = IciqlSuite.openNewDb();

        db.insertAll(UserId.getList());
        db.insertAll(UserNote.getList());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testPrimitiveJoin() throws Exception {
        final UserId u = new UserId();
        final UserNote n = new UserNote();

        List<UserNote> notes = db.from(u).innerJoin(n).on(u.id).is(n.userId).where(u.id).is(2)
                .select(new UserNote() {
                    {
                        userId = n.userId;
                        noteId = n.noteId;
                        text = n.text;
                    }
                });
        assertEquals(3, notes.size());
    }

    @Test
    public void testJoin() throws Exception {
        final UserId u = new UserId();
        final UserNote n = new UserNote();

        // this query returns 1 UserId if the user has a note
        // it's purpose is to confirm fluency/type-safety on a very simple
        // join case where the main table is filtered/reduced by hits in a
        // related table

        List<UserId> users = db.from(u).innerJoin(n).on(u.id).is(n.userId).where(u.id).is(2).selectDistinct();

        assertEquals(1, users.size());
        assertEquals(2, users.get(0).id);
    }

    @Test
    public void testLeftJoin() throws Exception {
        final UserId u = new UserId();
        final UserNote n = new UserNote();

        List<UserId> notes = db.from(u).leftJoin(n).on(u.id).is(n.userId).where(u.id).is(4).select();
        assertEquals(1, notes.size());
        assertEquals(4, notes.get(0).id);
    }

    @Test
    public void testSubQuery() throws Exception {
        final UserId u = new UserId();
        final UserNote n = new UserNote();

        QueryWhere<UserId> q = db.from(u).where(u.id).in(db.from(n).where(n.userId).exceeds(0).subQuery(n.userId));
        List<UserId> notes = q.select();
        assertEquals(3, notes.size());

        // do not test MySQL on this statement because the databases
        if (IciqlSuite.isMySQL(db)) {
            assertEquals("SELECT * FROM UserId WHERE `id` in (SELECT `userId` FROM UserNote WHERE `userId` > 0 )", q.toSQL());
        } else {
            assertEquals("SELECT * FROM UserId WHERE id in (SELECT userId FROM UserNote WHERE userId > 0 )", q.toSQL());
        }
    }

    @IQTable
    public static class UserId {

        @IQColumn(primaryKey = true)
        public int id;

        @IQColumn(length = 10)
        public String name;

        public UserId() {
            // public constructor
        }

        public UserId(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String toString() {
            return name + " (" + id + ")";
        }

        public static List<UserId> getList() {
            UserId[] list = {new UserId(1, "Tom"), new UserId(2, "Dick"), new UserId(3, "Harry"), new UserId(4, "Jack")};
            return Arrays.asList(list);
        }
    }

    @IQTable
    public static class UserNote {

        @IQColumn(autoIncrement = true, primaryKey = true)
        public int noteId;

        @IQColumn
        public int userId;

        @IQColumn(length = 10)
        public String text;

        public UserNote() {
            // public constructor
        }

        public UserNote(int userId, String text) {
            this.userId = userId;
            this.text = text;
        }

        public String toString() {
            return text;
        }

        public static List<UserNote> getList() {
            UserNote[] list = {new UserNote(1, "A"), new UserNote(2, "B"), new UserNote(3, "C"),
                    new UserNote(1, "D"), new UserNote(2, "E"), new UserNote(3, "F"), new UserNote(1, "G"),
                    new UserNote(2, "H"), new UserNote(3, "I"),};
            return Arrays.asList(list);
        }
    }
}
