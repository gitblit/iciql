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

package com.iciql.util;

import javax.naming.Context;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * This is a utility class with JDBC helper functions.
 */
public class JdbcUtils {

    private static final String[] DRIVERS = {"h2:", "org.h2.Driver", "Cache:",
            "com.intersys.jdbc.CacheDriver", "daffodilDB://", "in.co.daffodil.db.rmi.RmiDaffodilDBDriver",
            "daffodil", "in.co.daffodil.db.jdbc.DaffodilDBDriver", "db2:", "COM.ibm.db2.jdbc.net.DB2Driver",
            "derby:net:", "org.apache.derby.jdbc.ClientDriver", "derby://",
            "org.apache.derby.jdbc.ClientDriver", "derby:", "org.apache.derby.jdbc.EmbeddedDriver",
            "FrontBase:", "com.frontbase.jdbc.FBJDriver", "firebirdsql:", "org.firebirdsql.jdbc.FBDriver",
            "hsqldb:", "org.hsqldb.jdbcDriver", "informix-sqli:", "com.informix.jdbc.IfxDriver", "jtds:",
            "net.sourceforge.jtds.jdbc.Driver", "microsoft:", "com.microsoft.jdbc.sqlserver.SQLServerDriver",
            "mimer:", "com.mimer.jdbc.Driver", "mysql:", "com.mysql.jdbc.Driver", "odbc:",
            "sun.jdbc.odbc.JdbcOdbcDriver", "oracle:", "oracle.jdbc.driver.OracleDriver", "pervasive:",
            "com.pervasive.jdbc.v2.Driver", "pointbase:micro:", "com.pointbase.me.jdbc.jdbcDriver",
            "pointbase:", "com.pointbase.jdbc.jdbcUniversalDriver", "postgresql:", "org.postgresql.Driver",
            "sybase:", "com.sybase.jdbc3.jdbc.SybDriver", "sqlserver:",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver", "teradata:", "com.ncr.teradata.TeraDriver",};

    private JdbcUtils() {
        // utility class
    }

    /**
     * Close a statement without throwing an exception.
     *
     * @param stat the statement or null
     */
    public static void closeSilently(Statement stat) {
        if (stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Close a connection without throwing an exception.
     *
     * @param conn the connection or null
     */
    public static void closeSilently(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Close a result set without throwing an exception.
     *
     * @param rs the result set or null
     */
    public static void closeSilently(ResultSet rs) {
        closeSilently(rs, false);
    }

    /**
     * Close a result set, and optionally its statement without throwing an
     * exception.
     *
     * @param rs the result set or null
     */
    public static void closeSilently(ResultSet rs, boolean closeStatement) {
        if (rs != null) {
            Statement stat = null;
            if (closeStatement) {
                try {
                    stat = rs.getStatement();
                } catch (SQLException e) {
                    // ignore
                }
            }
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
            closeSilently(stat);
        }
    }

    /**
     * Close an XA connection set without throwing an exception.
     *
     * @param conn the XA connection or null
     */
    public static void closeSilently(XAConnection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Open a new database connection with the given settings.
     *
     * @param driver   the driver class name
     * @param url      the database URL
     * @param user     the user name
     * @param password the password
     * @return the database connection
     */
    public static Connection getConnection(String driver, String url, String user, String password)
            throws SQLException {
        Properties prop = new Properties();
        if (user != null) {
            prop.setProperty("user", user);
        }
        if (password != null) {
            prop.setProperty("password", password);
        }
        return getConnection(driver, url, prop);
    }

    /**
     * Escape table or schema patterns used for DatabaseMetaData functions.
     *
     * @param pattern the pattern
     * @return the escaped pattern
     */
    public static String escapeMetaDataPattern(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            return pattern;
        }
        return StringUtils.replaceAll(pattern, "\\", "\\\\");
    }

    /**
     * Open a new database connection with the given settings.
     *
     * @param driver the driver class name
     * @param url    the database URL
     * @param prop   the properties containing at least the user name and password
     * @return the database connection
     */
    public static Connection getConnection(String driver, String url, Properties prop) throws SQLException {
        if (StringUtils.isNullOrEmpty(driver)) {
            JdbcUtils.load(url);
        } else {
            Class<?> d = Utils.loadClass(driver);
            if (java.sql.Driver.class.isAssignableFrom(d)) {
                return DriverManager.getConnection(url, prop);
            } else if (javax.naming.Context.class.isAssignableFrom(d)) {
                // JNDI context
                try {
                    Context context = (Context) d.newInstance();
                    DataSource ds = (DataSource) context.lookup(url);
                    String user = prop.getProperty("user");
                    String password = prop.getProperty("password");
                    if (StringUtils.isNullOrEmpty(user) && StringUtils.isNullOrEmpty(password)) {
                        return ds.getConnection();
                    }
                    return ds.getConnection(user, password);
                } catch (SQLException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SQLException("Failed to get connection for " + url, e);
                }
            } else {
                // Don't know, but maybe it loaded a JDBC Driver
                return DriverManager.getConnection(url, prop);
            }
        }
        return DriverManager.getConnection(url, prop);
    }

    /**
     * Get the driver class name for the given URL, or null if the URL is
     * unknown.
     *
     * @param url the database URL
     * @return the driver class name
     */
    public static String getDriver(String url) {
        if (url.startsWith("jdbc:")) {
            url = url.substring("jdbc:".length());
            for (int i = 0; i < DRIVERS.length; i += 2) {
                String prefix = DRIVERS[i];
                if (url.startsWith(prefix)) {
                    return DRIVERS[i + 1];
                }
            }
        }
        return null;
    }

    /**
     * Load the driver class for the given URL, if the database URL is known.
     *
     * @param url the database URL
     */
    public static void load(String url) {
        String driver = getDriver(url);
        if (driver != null) {
            Utils.loadClass(driver);
        }
    }

}
