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

import com.iciql.Define;
import com.iciql.Iciql;
import com.iciql.Iciql.IQIndex;
import com.iciql.Iciql.IQTable;

/**
 * A table containing product data.
 */

@IQTable(annotationsOnly = false)
@IQIndex({ "name", "cat" })
public class ProductMixedAnnotation implements Iciql {

	public Double unitPrice;
	public Integer unitsInStock;
	public String mappedField;

	@IQIgnore
	public String productDescription;

	@IQColumn(name = "cat", length = 255)
	public String category;

	@IQColumn(name = "id", primaryKey = true)
	private Integer productId;

	@IQColumn(name = "name", length = 255)
	private String productName;
	
	public ProductMixedAnnotation() {
		// public constructor
	}

	protected ProductMixedAnnotation(int productId, String productName, String category, double unitPrice,
			int unitsInStock, String mappedField) {
		this.productId = productId;
		this.productName = productName;
		this.category = category;
		this.unitPrice = unitPrice;
		this.unitsInStock = unitsInStock;
		this.mappedField = mappedField;
		this.productDescription = category + ": " + productName;
	}

	private static ProductMixedAnnotation create(int productId, String productName, String category,
			double unitPrice, int unitsInStock, String mappedField) {
		return new ProductMixedAnnotation(productId, productName, category, unitPrice, unitsInStock,
				mappedField);
	}

	public static List<ProductMixedAnnotation> getList() {
		String mappedField = "mapped";
		ProductMixedAnnotation[] list = { create(1, "Chai", "Beverages", 18, 39, mappedField),
				create(2, "Chang", "Beverages", 19.0, 17, mappedField),
				create(3, "Aniseed Syrup", "Condiments", 10.0, 13, mappedField),
				create(4, "Chef Anton's Cajun Seasoning", "Condiments", 22.0, 53, mappedField),
				create(5, "Chef Anton's Gumbo Mix", "Condiments", 21.3500, 0, mappedField),
				create(6, "Grandma's Boysenberry Spread", "Condiments", 25.0, 120, mappedField),
				create(7, "Uncle Bob's Organic Dried Pears", "Produce", 30.0, 15, mappedField),
				create(8, "Northwoods Cranberry Sauce", "Condiments", 40.0, 6, mappedField),
				create(9, "Mishi Kobe Niku", "Meat/Poultry", 97.0, 29, mappedField),
				create(10, "Ikura", "Seafood", 31.0, 31, mappedField), };
		return Arrays.asList(list);
	}

	public String toString() {
		return productName + ": " + unitsInStock;
	}

	public int id() {
		return productId;
	}

	public String name() {
		return productName;
	}

	@Override
	public void defineIQ() {
		Define.length(mappedField, 25);
	}
}
