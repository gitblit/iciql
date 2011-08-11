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
 * A table containing customer data.
 */
@IQTable
public class Customer {

	@IQColumn(length = 25)
	public String customerId;
	
	@IQColumn(length = 2)
	public String region;

	public Customer() {
		// public constructor
	}

	public Customer(String customerId, String region) {
		this.customerId = customerId;
		this.region = region;
	}

	public String toString() {
		return customerId;
	}

	public static List<Customer> getList() {
		Customer[] list = { new Customer("ALFKI", "WA"), new Customer("ANATR", "WA"),
				new Customer("ANTON", "CA") };
		return Arrays.asList(list);
	}

}
