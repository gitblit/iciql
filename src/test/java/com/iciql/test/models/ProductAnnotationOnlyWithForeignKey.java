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

import com.iciql.Iciql.ConstraintDeleteType;
import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQContraintForeignKey;
import com.iciql.Iciql.IQIndex;
import com.iciql.Iciql.IQIndexes;
import com.iciql.Iciql.IQTable;
import com.iciql.Iciql.IndexType;

import java.util.Arrays;
import java.util.List;

/**
 * A table containing product data.
 */

@IQTable(name = "AnnotatedProduct", primaryKey = "id")
@IQIndexes({@IQIndex({"name", "cat"}), @IQIndex(name = "nameidx", type = IndexType.HASH, value = "name")})
@IQContraintForeignKey(
        foreignColumns = {"cat"},
        referenceName = "AnnotatedCategory",
        referenceColumns = {"categ"},
        deleteType = ConstraintDeleteType.CASCADE
)
public class ProductAnnotationOnlyWithForeignKey {

    public String unmappedField;

    @IQColumn(name = "id", autoIncrement = true)
    public Long productId;

    @IQColumn(name = "cat", length = 15, trim = true)
    public String category;

    @IQColumn(name = "name", length = 50)
    public String productName;

    @SuppressWarnings("unused")
    @IQColumn
    private Double unitPrice;

    @IQColumn
    private Integer unitsInStock;

    public ProductAnnotationOnlyWithForeignKey() {
        // public constructor
    }

    private ProductAnnotationOnlyWithForeignKey(long productId, String productName, String category, double unitPrice,
                                                int unitsInStock, String unmappedField) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.unitsInStock = unitsInStock;
        this.unmappedField = unmappedField;
    }

    private static ProductAnnotationOnlyWithForeignKey create(int productId, String productName, String category,
                                                              double unitPrice, int unitsInStock, String unmappedField) {
        return new ProductAnnotationOnlyWithForeignKey(productId, productName, category, unitPrice, unitsInStock,
                unmappedField);
    }

    public static List<ProductAnnotationOnlyWithForeignKey> getList() {
        String unmappedField = "unmapped";
        ProductAnnotationOnlyWithForeignKey[] list = {create(1, "Chai", "Beverages", 18, 39, unmappedField),
                create(2, "Chang", "Beverages", 19.0, 17, unmappedField),
                create(3, "Aniseed Syrup", "Condiments", 10.0, 13, unmappedField),
                create(4, "Chef Anton's Cajun Seasoning", "Condiments", 22.0, 53, unmappedField),
                create(5, "Chef Anton's Gumbo Mix", "Condiments", 21.3500, 0, unmappedField),
                create(6, "Grandma's Boysenberry Spread", "Condiments", 25.0, 120, unmappedField),
                create(7, "Uncle Bob's Organic Dried Pears", "Produce", 30.0, 15, unmappedField),
                create(8, "Northwoods Cranberry Sauce", "Condiments", 40.0, 6, unmappedField),
                create(9, "Mishi Kobe Niku", "Meat/Poultry", 97.0, 29, unmappedField),
                create(10, "Ikura", "Seafood", 31.0, 31, unmappedField),};
        return Arrays.asList(list);
    }

    public String toString() {
        return productName + ": " + unitsInStock;
    }

}
