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

import static com.iciql.Define.index;
import static com.iciql.Define.length;
import static com.iciql.Define.primaryKey;
import static com.iciql.Define.tableName;

import java.util.Arrays;
import java.util.List;

import com.iciql.Iciql;

/**
 * A table containing product data.
 */

public class Product implements Iciql {

	public Integer productId;
	public String productName;
	public String category;
	public Double unitPrice;
	public Integer unitsInStock;

	public Product() {
		// public constructor
	}

	private Product(int productId, String productName, String category, double unitPrice, int unitsInStock) {
		this.productId = productId;
		this.productName = productName;
		this.category = category;
		this.unitPrice = unitPrice;
		this.unitsInStock = unitsInStock;
	}

	public void defineIQ() {
		tableName("Product");
		primaryKey(productId);
		length(category, 255);
		index(productName, category);
	}

	private static Product create(int productId, String productName, String category, double unitPrice, int unitsInStock) {
		return new Product(productId, productName, category, unitPrice, unitsInStock);
	}

	public static List<Product> getList() {
		Product[] list = { create(1, "Chai", "Beverages", 18, 39), create(2, "Chang", "Beverages", 19.0, 17),
				create(3, "Aniseed Syrup", "Condiments", 10.0, 13),
				create(4, "Chef Anton's Cajun Seasoning", "Condiments", 22.0, 53),
				create(5, "Chef Anton's Gumbo Mix", "Condiments", 21.3500, 0),
				create(6, "Grandma's Boysenberry Spread", "Condiments", 25.0, 120),
				create(7, "Uncle Bob's Organic Dried Pears", "Produce", 30.0, 15),
				create(8, "Northwoods Cranberry Sauce", "Condiments", 40.0, 6),
				create(9, "Mishi Kobe Niku", "Meat/Poultry", 97.0, 29), create(10, "Ikura", "Seafood", 31.0, 31), };

		return Arrays.asList(list);
	}

	public String toString() {
		return productName + ": " + unitsInStock;
	}

}
