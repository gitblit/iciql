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

import com.iciql.Iciql.DataTypeAdapter;
import com.thoughtworks.xstream.XStream;

/**
 * Base class for inserting/retrieving a Java Object (de)serialized as XML using XStream.
 * <p>You use this by creating a subclass which defines your object class.</p>
 * <pre>
 * public class CustomObjectAdapter extends XStreamTypeAdapter&lt;CustomObject&gt; {
 *
 *    public Class&lt;CustomObject&gt; getJavaType() {
 *        return CustomObject.class;
 *    }
 * }
 * </pre>
 * @param <T>
 */
public abstract class XStreamTypeAdapter<T> implements DataTypeAdapter<T> {

	protected XStream xstream() {
		return new XStream();
	}

	@Override
	public String getDataType() {
		return "TEXT";
	}

	@Override
	public Object serialize(T value) {
		return xstream().toXML(value);
	}

	@Override
	public T deserialize(Object value) {
		String xml = value.toString();
		XStream xstream = xstream();
		T t = (T) xstream.fromXML(xml);
		return t;
	}
}
