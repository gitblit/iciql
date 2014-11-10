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

import java.io.InputStream;
import java.util.Properties;

import com.iciql.Iciql.Mode;

/**
 * Loads DAO statements from Properties resource files the classpath.
 *
 * @author James Moger
 *
 */
public class DaoClasspathStatementProvider implements DaoStatementProvider {

	private final Properties externalStatements;

	public DaoClasspathStatementProvider() {
		externalStatements = load();
	}

	/**
	 * Returns the list of statement resources to try locating.
	 *
	 * @return
	 */
	protected String[] getStatementResources() {
		return new String[] { "/iciql.properties", "/iciql.xml", "/conf/iciql.properties", "/conf/iciql.xml" };
	}

	/**
	 * Loads the first statement resource found on the classpath.
	 *
	 * @return the loaded statements
	 */
	private Properties load() {

		Properties props = new Properties();
		for (String resource : getStatementResources()) {

			InputStream is = null;

			try {
				is = DaoProxy.class.getResourceAsStream(resource);

				if (is != null) {

					if (resource.toLowerCase().endsWith(".xml")) {
						// load an .XML statements file
						props.loadFromXML(is);
					} else {
						// load a .Properties statements file
						props.load(is);
					}

					break;
				}

			} catch (Exception e) {
				throw new IciqlException(e, "Failed to parse {0}", resource);
			} finally {
				try {
					is.close();
				} catch (Exception e) {
				}
			}

		}
		return props;
	}

	@Override
	public String getStatement(String idOrStatement, Mode mode) {
		final String modePrefix = "%" + mode.name().toLowerCase() + ".";
		String value = externalStatements.getProperty(idOrStatement, idOrStatement);
		value = externalStatements.getProperty(modePrefix + idOrStatement, value);
		return value;
	}

}
