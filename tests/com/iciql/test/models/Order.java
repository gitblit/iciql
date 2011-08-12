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

import static com.iciql.Define.length;
import static com.iciql.Define.primaryKey;
import static com.iciql.Define.scale;
import static com.iciql.Define.tableName;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.iciql.Iciql;

/**
 * A table containing order data.
 */

public class Order implements Iciql {
	public String customerId;
	public Integer orderId;
	public Date orderDate;	
	public BigDecimal total;

	public Order(String customerId, Integer orderId, String total, String orderDate) {
		this.customerId = customerId;
		this.orderId = orderId;
		this.total = new BigDecimal(total);
		this.orderDate = java.sql.Date.valueOf(orderDate);
	}

	public Order() {
		// public constructor
	}

	public void defineIQ() {
		tableName("Orders");
		length(customerId, 25);
		length(total, 10);
		scale(total, 2);
		primaryKey(customerId, orderId);
	}

	public static List<Order> getList() {
		Order[] list = { new Order("ALFKI", 10702, "330.00", "2007-01-02"),
				new Order("ALFKI", 10952, "471.20", "2007-02-03"), new Order("ANATR", 10308, "88.80", "2007-01-03"),
				new Order("ANATR", 10625, "479.75", "2007-03-03"), new Order("ANATR", 10759, "320.00", "2007-04-01"),
				new Order("ANTON", 10365, "403.20", "2007-02-13"), new Order("ANTON", 10682, "375.50", "2007-03-13"),
				new Order("ANTON", 10355, "480.00", "2007-04-11") };
		return Arrays.asList(list);
	}

}
