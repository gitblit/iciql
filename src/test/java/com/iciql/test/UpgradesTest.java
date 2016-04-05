/*
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

package com.iciql.test;

import com.iciql.Db;
import com.iciql.DbUpgrader;
import com.iciql.Iciql.IQVersion;
import com.iciql.test.models.Product;
import com.iciql.test.models.SupportedTypes;
import com.iciql.test.models.SupportedTypes.SupportedTypes2;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests the database and table upgrade functions.
 */
public class UpgradesTest {

    @Test
    public void testDatabaseUpgrade() {
        Db db = IciqlSuite.openNewDb();

        List<Product> products = Product.getList();

        // set the v1 upgrader and insert a record.
        // this will trigger the upgrade.
        V1DbUpgrader v1 = new V1DbUpgrader();
        db.setDbUpgrader(v1);
        db.insert(products.get(0));

        // confirm that upgrade occurred
        assertEquals(0, v1.oldVersion.get());
        assertEquals(1, v1.newVersion.get());

        // open a second connection to the database
        // and then apply the v2 upgrade.
        // For an in-memory db its important to keep the first connection
        // alive so that the database is not destroyed.
        Db db2 = IciqlSuite.openCurrentDb();

        // set the v2 upgrader and insert a record.
        // this will trigger the upgrade.
        V2DbUpgrader v2 = new V2DbUpgrader();
        db2.setDbUpgrader(v2);
        db2.insert(products.get(1));

        // confirm that upgrade occurred
        assertEquals(1, v2.oldVersion.get());
        assertEquals(2, v2.newVersion.get());

        db.executeUpdate("DROP TABLE iq_versions");
        db.close();
        db2.close();
    }

    @Test
    public void testDatabaseInheritedUpgrade() {
        Db db = IciqlSuite.openNewDb();

        List<Product> products = Product.getList();

        // set the v1 upgrader and insert a record.
        // this will trigger the upgrade.
        V1DbUpgrader v1 = new V1DbUpgrader();
        db.setDbUpgrader(v1);
        db.insert(products.get(0));

        // confirm that upgrade occurred
        assertEquals(0, v1.oldVersion.get());
        assertEquals(1, v1.newVersion.get());

        // open a second connection to the database
        // and then apply the v2 upgrade.
        // For an in-memory db its important to keep the first connection
        // alive so that the database is not destroyed.
        Db db2 = IciqlSuite.openCurrentDb();

        // set the v2 upgrader and insert a record.
        // this will trigger the upgrade.
        V2DbUpgraderImpl v2 = new V2DbUpgraderImpl();
        db2.setDbUpgrader(v2);
        db2.insert(products.get(1));

        // confirm that upgrade occurred
        assertEquals(1, v2.oldVersion.get());
        assertEquals(2, v2.newVersion.get());

        db.executeUpdate("DROP TABLE iq_versions");
        db.close();
        db2.close();
    }

    @Test
    public void testTableUpgrade() {
        Db db = IciqlSuite.openNewDb();

        // insert first, this will create version record automatically
        List<SupportedTypes> original = SupportedTypes.createList();
        db.insertAll(original);

        // reset the dbUpgrader (clears the update check cache)
        V2DbUpgrader dbUpgrader = new V2DbUpgrader();
        db.setDbUpgrader(dbUpgrader);

        SupportedTypes2 s2 = new SupportedTypes2();

        List<SupportedTypes2> types = db.from(s2).select();
        assertEquals(10, types.size());
        assertEquals(1, dbUpgrader.oldVersion.get());
        assertEquals(2, dbUpgrader.newVersion.get());
        db.executeUpdate("DROP TABLE iq_versions");
        db.close();
    }

    /**
     * A sample database upgrader class.
     */
    class BaseDbUpgrader implements DbUpgrader {
        final AtomicInteger oldVersion = new AtomicInteger(0);
        final AtomicInteger newVersion = new AtomicInteger(0);

        @Override
        public boolean upgradeTable(Db db, String schema, String table, int fromVersion, int toVersion) {
            // just claims success on upgrade request
            oldVersion.set(fromVersion);
            newVersion.set(toVersion);
            return true;
        }

        @Override
        public boolean upgradeDatabase(Db db, int fromVersion, int toVersion) {
            // just claims success on upgrade request
            oldVersion.set(fromVersion);
            newVersion.set(toVersion);
            return true;
        }
    }

    /**
     * A sample V1 database upgrader class.
     */
    @IQVersion(1)
    class V1DbUpgrader extends BaseDbUpgrader {
    }

    /**
     * A sample V2 database upgrader class.
     */
    @IQVersion(2)
    class V2DbUpgrader extends BaseDbUpgrader {
    }


    /**
     * A sample V2 database upgrader class which inherits its
     * version from the parent class.
     */
    @IQVersion()
    class V2DbUpgraderImpl extends V2DbUpgrader {
    }

}
