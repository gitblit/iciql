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

import com.iciql.Iciql.EnumId;
import com.iciql.Iciql.EnumType;
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQEnum;
import com.iciql.Iciql.IQTable;

public abstract class EnumModels {

	/**
	 * Test of @IQEnum annotated enumeration. This strategy is the default
	 * strategy for all fields of the Tree enum.
	 * 
	 * Individual Tree field declarations can override this strategy by
	 * specifying a different @IQEnum annotation.
	 * 
	 * Here ORDINAL specifies that this enum will be mapped to an INT column.
	 */
	@IQEnum(EnumType.ENUMID)
	public enum Tree implements EnumId {
		PINE(10), OAK(20), BIRCH(30), WALNUT(40), MAPLE(50);

		private int enumid;

		Tree(int id) {
			this.enumid = id;
		}

		@Override
		public int enumId() {
			return enumid;
		}
	}

	@IQColumn(primaryKey = true)
	public Integer id;
	
	public abstract Tree tree();

	/**
	 * Test model for enum-as-enumid.
	 */
	@IQTable(inheritColumns = true)
	public static class EnumIdModel extends EnumModels {

		// no need to specify ENUMID type as the enumeration definition
		// specifies it.
		@IQColumn
		private Tree tree;

		public EnumIdModel() {
		}

		public EnumIdModel(int id, Tree tree) {
			this.id = id;
			this.tree = tree;
		}

		@Override
		public Tree tree() {
			return tree;
		}

		public static List<EnumIdModel> createList() {
			return Arrays.asList(new EnumIdModel(400, Tree.WALNUT), new EnumIdModel(200, Tree.OAK),
					new EnumIdModel(500, Tree.MAPLE), new EnumIdModel(300, Tree.BIRCH), new EnumIdModel(100,
							Tree.PINE));
		}
	}

	/**
	 * Test model for enum-as-ordinal.
	 */
	@IQTable(inheritColumns = true)
	public static class EnumOrdinalModel extends EnumModels {

		// override the enumtype to ordinal
		@IQEnum(EnumType.ORDINAL)
		@IQColumn
		private Tree tree;

		public EnumOrdinalModel() {
		}

		public EnumOrdinalModel(int id, Tree tree) {
			this.id = id;
			this.tree = tree;
		}
		
		@Override
		public Tree tree() {
			return tree;
		}

		public static List<EnumOrdinalModel> createList() {
			return Arrays.asList(new EnumOrdinalModel(400, Tree.WALNUT), new EnumOrdinalModel(200, Tree.OAK),
					new EnumOrdinalModel(500, Tree.MAPLE), new EnumOrdinalModel(300, Tree.BIRCH),
					new EnumOrdinalModel(100, Tree.PINE));
		}
	}

	/**
	 * Test model for enum-as-string.
	 */
	@IQTable(inheritColumns = true)
	public static class EnumStringModel extends EnumModels {

		// override the enumtype to string
		// ensure that we specify a length so that the column is VARCHAR
		@IQEnum(EnumType.NAME)
		@IQColumn(length = 25)
		private Tree tree;

		public EnumStringModel() {
		}

		public EnumStringModel(int id, Tree tree) {
			this.id = id;
			this.tree = tree;
		}

		@Override
		public Tree tree() {
			return tree;
		}
		
		public static List<EnumStringModel> createList() {
			return Arrays.asList(new EnumStringModel(400, Tree.WALNUT), new EnumStringModel(200, Tree.OAK),
					new EnumStringModel(500, Tree.MAPLE), new EnumStringModel(300, Tree.BIRCH),
					new EnumStringModel(100, Tree.PINE));
		}
	}
}
