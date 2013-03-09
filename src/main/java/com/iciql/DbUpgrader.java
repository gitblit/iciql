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

package com.iciql;

import com.iciql.Iciql.IQVersion;

/**
 * Interface which defines a class to handle table changes based on model
 * versions. An implementation of <i>DbUpgrader</i> must be annotated with the
 * <i>IQDatabase</i> annotation, which defines the expected database version
 * number.
 */
public interface DbUpgrader {

	/**
	 * Defines method interface to handle database upgrades. This method is only
	 * called if your <i>DbUpgrader</i> implementation is annotated with
	 * IQDatabase.
	 * 
	 * @param db
	 *            the database
	 * @param fromVersion
	 *            the old version
	 * @param toVersion
	 *            the new version
	 * @return true for successful upgrade. If the upgrade is successful, the
	 *         version registry is automatically updated.
	 */
	boolean upgradeDatabase(Db db, int fromVersion, int toVersion);

	/**
	 * Defines method interface to handle table upgrades.
	 * 
	 * @param db
	 *            the database
	 * @param schema
	 *            the schema
	 * @param table
	 *            the table
	 * @param fromVersion
	 *            the old version
	 * @param toVersion
	 *            the new version
	 * @return true for successful upgrade. If the upgrade is successful, the
	 *         version registry is automatically updated.
	 */
	boolean upgradeTable(Db db, String schema, String table, int fromVersion, int toVersion);

	/**
	 * The default database upgrader. It throws runtime exception instead of
	 * handling upgrade requests.
	 */
	@IQVersion(0)
	public static class DefaultDbUpgrader implements DbUpgrader {

		public boolean upgradeDatabase(Db db, int fromVersion, int toVersion) {
			throw new IciqlException("Please provide your own DbUpgrader implementation.");
		}

		public boolean upgradeTable(Db db, String schema, String table, int fromVersion, int toVersion) {
			throw new IciqlException("Please provide your own DbUpgrader implementation.");
		}

	}

}
