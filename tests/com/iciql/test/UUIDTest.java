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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

/**
 * Tests of UUID type.
 * <p>
 * H2 only.
 */
public class UUIDTest {

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
	public void testUUIDs() throws Exception {
		// do not test non-H2 databases
		Assume.assumeTrue(IciqlSuite.isH2(db));

		List<UUIDRecord> originals = UUIDRecord.getList();
		db.insertAll(originals);
		UUIDRecord u = new UUIDRecord();
		List<UUIDRecord> retrieved = db.from(u).orderBy(u.id).select();
		assertEquals(originals.size(), retrieved.size());
		for (int i = 0; i < originals.size(); i++) {
			UUIDRecord a = originals.get(i);
			UUIDRecord b = retrieved.get(i);
			assertTrue(a.equivalentTo(b));
		}

		UUIDRecord second = db.from(u).where(u.uuid).is(originals.get(1).uuid).selectFirst();
		assertTrue(originals.get(1).equivalentTo(second));
	}

	/**
	 * A simple class used in this test.
	 */
	@IQTable(name = "UUID_TEST")
	public static class UUIDRecord {

		@IQColumn(primaryKey = true)
		public Integer id;

		@IQColumn()
		public UUID uuid;

		public UUIDRecord() {
			// public constructor
		}

		private UUIDRecord(int id) {
			this.id = id;
			this.uuid = UUID.randomUUID();
		}

		public boolean equivalentTo(UUIDRecord b) {
			boolean same = true;
			same &= id == b.id;
			same &= uuid.equals(b.uuid);
			return same;
		}

		public String toString() {
			return id + ": " + uuid;
		}

		public static List<UUIDRecord> getList() {
			List<UUIDRecord> list = new ArrayList<UUIDRecord>();
			for (int i = 0; i < 10; i++) {
				list.add(new UUIDRecord(i + 1));
			}
			return list;
		}
	}
}
