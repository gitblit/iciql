/*
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

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQConstraint;
import com.iciql.Iciql.IQView;

/**
 * A view containing product data.
 */

@IQView(name = "AnnotatedProductView", tableName = "AnnotatedProduct")
public class ProductView {

    public String unmappedField;

    @IQColumn(name = "id", autoIncrement = true)
    @IQConstraint("this <= 7 AND this > 2")
    public Long productId;

    @IQColumn(name = "name")
    public String productName;

    public ProductView() {
        // public constructor
    }

    public String toString() {
        return productName + " (" + productId + ")";
    }

}
