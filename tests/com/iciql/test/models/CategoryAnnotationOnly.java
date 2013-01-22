/*
 * Copyright 2012 Frédéric Gaillard.
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

package com.iciql.test.models;

import java.util.Arrays;
import java.util.List;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQContraintUnique;
import com.iciql.Iciql.IQTable;

/**
 * A table containing category data.
 */

@IQTable(name = "AnnotatedCategory", primaryKey = "id")
// @IQIndex(value = "categ", type=IndexType.UNIQUE)
@IQContraintUnique(uniqueColumns = { "categ" })
public class CategoryAnnotationOnly {

	@IQColumn(name = "id", autoIncrement = true)
	public Long categoryId;

	@IQColumn(name = "categ", length = 15, trim = true)
	public String category;

	public CategoryAnnotationOnly() {
		// public constructor
	}

	private CategoryAnnotationOnly(long categoryId, String category) {
		this.categoryId = categoryId;
		this.category = category;
	}

	private static CategoryAnnotationOnly create(int categoryId, String category) {
		return new CategoryAnnotationOnly(categoryId, category);
	}

	public static List<CategoryAnnotationOnly> getList() {
		CategoryAnnotationOnly[] list = { 
				create(1, "Beverages"),
				create(2, "Condiments"),
				create(3, "Produce"),
				create(4, "Meat/Poultry"),
				create(5,"Seafood")
		};
		return Arrays.asList(list);
	}

	public String toString() {
		return category;
	}

}
