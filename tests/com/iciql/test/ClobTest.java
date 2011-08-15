/*
 * Copyright 2004-2011 H2 Group.
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

import static com.iciql.Define.primaryKey;
import static com.iciql.Define.tableName;
import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.iciql.Db;
import com.iciql.Iciql;

/**
 * Tests if converting a CLOB to a String works.
 */
public class ClobTest {

	@Test
	public void testClob() throws Exception {
		String create = "CREATE TABLE CLOB_TEST(ID INT PRIMARY KEY, WORDS {0})";
		Db db = IciqlSuite.openNewDb();
		db.executeUpdate(MessageFormat.format(create, "VARCHAR(255)"));
		db.insertAll(StringRecord.getList());
		testSimpleUpdate(db, "VARCHAR fail");
		db.close();

		db = IciqlSuite.openNewDb();
		db.executeUpdate(MessageFormat.format(create, "CLOB"));
		db.insertAll(StringRecord.getList());
		testSimpleUpdate(db, "CLOB fail because of single quote artifacts");
		db.close();
	}

	private void testSimpleUpdate(Db db, String failureMsg) {
		String newWords = "I changed the words";
		StringRecord r = new StringRecord();
		StringRecord originalRecord = db.from(r).where(r.id).is(2).selectFirst();
		String oldWords = originalRecord.words;
		originalRecord.words = newWords;
		db.update(originalRecord);

		StringRecord r2 = new StringRecord();
		StringRecord revisedRecord = db.from(r2).where(r2.id).is(2).selectFirst();
		assertEquals(failureMsg, newWords, revisedRecord.words);

		// undo update
		originalRecord.words = oldWords;
		db.update(originalRecord);
	}

	/**
	 * A simple class used in this test.
	 */
	public static class StringRecord implements Iciql {

		public Integer id;
		public String words;

		public StringRecord() {
			// public constructor
		}

		private StringRecord(int id, String words) {
			this.id = id;
			this.words = words;
		}

		public void defineIQ() {
			tableName("CLOB_TEST");
			primaryKey(id);
		}

		private static StringRecord create(int id, String words) {
			return new StringRecord(id, words);
		}

		public static List<StringRecord> getList() {
			StringRecord[] list = {
					create(1, "Once upon a midnight dreary, while I pondered weak and weary,"),
					create(2, "Over many a quaint and curious volume of forgotten lore,"),
					create(3, "While I nodded, nearly napping, suddenly there came a tapping,"),
					create(4, "As of some one gently rapping, rapping at my chamber door."),
					create(5, "`'Tis some visitor,' I muttered, `tapping at my chamber door -"),
					create(6, "Only this, and nothing more.'") };

			return Arrays.asList(list);
		}

		public String toString() {
			return id + ": " + words;
		}
	}
}
