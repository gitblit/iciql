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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;
import com.iciql.util.IciqlLogger;

/**
 * Tests of Joins.
 */
public class JoinTest {

	Db db;

	@Before
	public void setup() {
		db = IciqlSuite.openNewDb();
	}

	@After
	public void tearDown() {
		db.close();
	}

	@Test
	public void testPrimitiveJoin() throws Exception {
		db.insertAll(UserId.getList());
		db.insertAll(UserNote.getList());

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

		db.dropTable(UserId.class);
		db.dropTable(UserNote.class);

		assertEquals(3, notes.size());
	}
	
	@Test
	public void testJoin() throws Exception {
		db.insertAll(UserId.getList());
		db.insertAll(UserNote.getList());

		final UserId u = new UserId();
		final UserNote n = new UserNote();

		// this query returns 1 UserId if the user has a note
		List<UserId> users = (List<UserId>) db.from(u).innerJoin(n).on(u.id).is(n.userId).groupBy(u.id).where(u.id).is(2).select();
		
		db.dropTable(UserId.class);
		db.dropTable(UserNote.class);

		assertEquals(1, users.size());
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
			UserId[] list = { new UserId(1, "Tom"), new UserId(2, "Dick"), new UserId(3, "Harry") };
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
			UserNote[] list = { new UserNote(1, "A"), new UserNote(2, "B"), new UserNote(3, "C"),
					new UserNote(1, "D"), new UserNote(2, "E"), new UserNote(3, "F"), new UserNote(1, "G"),
					new UserNote(2, "H"), new UserNote(3, "I"), };
			return Arrays.asList(list);
		}
	}
}
