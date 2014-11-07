/*
 * Copyright 2014 James Moger.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.iciql.Db;
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;
import com.iciql.Iciql.TypeAdapter;
import com.iciql.JavaSerializationTypeAdapter;
import com.iciql.test.models.SupportedTypes;

/**
 * Tests insertion and retrieval of a custom data type that is automatically transformed
 * by a Java Object Serialization-based type adapter.
 */
public class DataTypeAdapterTest extends Assert {

	private Db db;


	@Before
	public void setUp() {
		db = IciqlSuite.openNewDb();
	}

	@After
	public void tearDown() {
		db.close();
	}

	@Test
	public void testSerializedObjectDataType() {

		SerializedObjectTypeAdapterTest row = new SerializedObjectTypeAdapterTest();
		row.received = new Date();
		row.obj = SupportedTypes.createList().get(1);
		db.insert(row);

		SerializedObjectTypeAdapterTest table = new SerializedObjectTypeAdapterTest();
		SerializedObjectTypeAdapterTest q1 = db.from(table).selectFirst();

		assertNotNull(q1);
		assertTrue(row.obj.equivalentTo(q1.obj));

	}

	@IQTable(name="dataTypeAdapters")
	public static class SerializedObjectTypeAdapterTest {

		@IQColumn(autoIncrement = true, primaryKey = true)
		private long id;

		@IQColumn
		public java.util.Date received;

		@IQColumn
		@SupportedTypesAdapter
		public SupportedTypes obj;

	}

	/**
	 * Maps a SupportedType instance to a BLOB using Java Object serialization.
	 *
	 */
	public static class SupportedTypesAdapterImpl extends JavaSerializationTypeAdapter<SupportedTypes> {

		@Override
		public Class<SupportedTypes> getJavaType() {
			return SupportedTypes.class;
		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
	@TypeAdapter(SupportedTypesAdapterImpl.class)
	public @interface SupportedTypesAdapter { }

}
