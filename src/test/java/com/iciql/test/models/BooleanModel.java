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
package com.iciql.test.models;

import java.util.Arrays;
import java.util.List;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

/**
 * Boolean types model.
 */
@IQTable(name = "BooleanTest")
public class BooleanModel {

	@IQColumn(primaryKey = true)
	public Integer id;

	@IQColumn
	public Boolean mybool;

	public BooleanModel() {
	}

	BooleanModel(int id, boolean val) {
		this.id = id;
		this.mybool = val;
	}

	public static List<BooleanModel> getList() {
		return Arrays.asList(new BooleanModel(1, true), new BooleanModel(2, false),
				new BooleanModel(3, true), new BooleanModel(4, false));
	}

	/**
	 * Test boolean as Integer
	 */
	@IQTable(name = "BooleanTest")
	public static class BooleanAsIntModel {
		@IQColumn(primaryKey = true)
		public Integer id;

		@IQColumn
		public Integer mybool;

		public BooleanAsIntModel() {
		}

		BooleanAsIntModel(int id, boolean val) {
			this.id = id;
			this.mybool = val ? 1 : 0;
		}

		public static List<BooleanAsIntModel> getList() {
			return Arrays.asList(new BooleanAsIntModel(1, true), new BooleanAsIntModel(2, false),
					new BooleanAsIntModel(3, true), new BooleanAsIntModel(4, false));
		}
	}

	/**
	 * Test boolean as primitive short
	 */
	@IQTable(name = "BooleanTest")
	public static class BooleanAsPrimitiveShortModel {
		@IQColumn(primaryKey = true)
		public Integer id;

		@IQColumn
		public short mybool;

		public BooleanAsPrimitiveShortModel() {
		}

		BooleanAsPrimitiveShortModel(int id, boolean val) {
			this.id = id;
			this.mybool = (short) (val ? 1 : 0);
		}

		public static List<BooleanAsPrimitiveShortModel> getList() {
			return Arrays.asList(new BooleanAsPrimitiveShortModel(1, true), new BooleanAsPrimitiveShortModel(2, false),
					new BooleanAsPrimitiveShortModel(3, true), new BooleanAsPrimitiveShortModel(4, false));
		}
	}
}
