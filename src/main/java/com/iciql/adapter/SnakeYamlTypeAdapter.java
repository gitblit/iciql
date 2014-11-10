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

package com.iciql.adapter;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import com.iciql.Iciql.DataTypeAdapter;
import com.iciql.Iciql.Mode;

/**
 * Base class for inserting/retrieving a Java Object (de)serialized as YAML using SnakeYaml.
 */
public abstract class SnakeYamlTypeAdapter<T> implements DataTypeAdapter<T> {

	protected Mode mode;

	protected Yaml yaml() {
		return new Yaml();
	}

	@Override
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	@Override
	public String getDataType() {
		return "TEXT";
	}

	@Override
	public abstract Class<T> getJavaType();

	@Override
	public Object serialize(Object value) {
		return yaml().dumpAs(value, Tag.MAP, FlowStyle.BLOCK);
	}

	@Override
	public T deserialize(Object value) {
		String yaml = value.toString();
		Yaml processor = yaml();
		T t = processor.loadAs(yaml, getJavaType());
		return t;
	}

}
