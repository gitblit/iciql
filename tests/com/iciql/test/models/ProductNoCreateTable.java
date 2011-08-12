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

package com.iciql.test.models;

import java.util.Arrays;
import java.util.List;

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQTable;

/**
 * A table containing product data.
 */

@IQTable(create = false)
public class ProductNoCreateTable {

	@SuppressWarnings("unused")
	@IQColumn(name = "id")
	private Integer productId;

	@SuppressWarnings("unused")
	@IQColumn(name = "name")
	private String productName;

	public ProductNoCreateTable() {
		// public constructor
	}

	private ProductNoCreateTable(int productId, String productName) {
		this.productId = productId;
		this.productName = productName;
	}

	private static ProductNoCreateTable create(int productId, String productName) {
		return new ProductNoCreateTable(productId, productName);
	}

	public static List<ProductNoCreateTable> getList() {
		ProductNoCreateTable[] list = { create(1, "Chai"), create(2, "Chang") };
		return Arrays.asList(list);
	}

}
