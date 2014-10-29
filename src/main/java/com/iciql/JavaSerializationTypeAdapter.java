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

package com.iciql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import com.iciql.Iciql.DataTypeAdapter;

/**
 * Base class for inserting/retrieving a Java Object as a BLOB field using Java Serialization.
 * <p>You use this by creating a subclass which defines your object class.</p>
 * <pre>
 * public class CustomObjectAdapter extends JavaSerializationTypeAdapter&lt;CustomObject&gt; {
 *
 *    public Class&lt;CustomObject&gt; getJavaType() {
 *        return CustomObject.class;
 *    }
 * }
 * </pre>
 * @param <T>
 */
public abstract class JavaSerializationTypeAdapter<T> implements DataTypeAdapter<T> {

	@Override
	public final String getDataType() {
		return "BLOB";
	}

	@Override
	public final Object serialize(T value) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(os).writeObject(value);
			return os.toByteArray();
		} catch (IOException e) {
			throw new IciqlException(e);
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				throw new IciqlException (e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final T deserialize(Object value) {
		InputStream is = null;
		if (value instanceof Blob) {
			Blob blob = (Blob) value;
			try {
				is = blob.getBinaryStream();
			} catch (SQLException e) {
				throw new IciqlException(e);
			}
		} else if (value instanceof byte[]) {
			byte [] bytes = (byte []) value;
			is = new ByteArrayInputStream(bytes);
		}

		try {
			T object = (T) new ObjectInputStream(is).readObject();
			return object;
		} catch (Exception e) {
			throw new IciqlException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new IciqlException (e);
				}
			}
		}
	}
}
