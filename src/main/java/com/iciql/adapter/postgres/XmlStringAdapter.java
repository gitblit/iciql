/*
 * Copyright 2014 James Moger.
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
package com.iciql.adapter.postgres;

import java.sql.SQLException;

import org.postgresql.util.PGobject;

import com.iciql.Iciql.DataTypeAdapter;

/**
 * Handles transforming raw strings to/from the Postgres XML data type.
 */
public class XmlStringAdapter implements DataTypeAdapter<String> {

	@Override
	public String getDataType() {
		return "xml";
	}

	@Override
	public Class<String> getJavaType() {
		return String.class;
	}

	@Override
	public Object serialize(String value) {
		PGobject pg = new PGobject();
		pg.setType(getDataType());
		try {
			pg.setValue(value);
		} catch (SQLException e) {
			// not thrown on base PGobject
		}
		return pg;
	}

	@Override
	public String deserialize(Object value) {
		return value.toString();
	}
}