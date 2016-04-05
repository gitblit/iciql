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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * The Dao interface defines all CRUD methods for handling SQL object operations.
 *
 * @author James Moger
 */
public interface Dao extends AutoCloseable {

    /**
     * Insert an object into the database.
     *
     * @param t
     * @return true if successful
     */
    <T> boolean insert(T t);

    /**
     * Insert an object into the database and return it's primary key.
     *
     * @param t
     * @return
     */
    <T> long insertAndGetKey(T t);

    /**
     * Insert all objects into the database.
     *
     * @param list
     */
    <T> void insertAll(List<T> list);

    /**
     * Insert all objects into the database and return the list of primary keys.
     *
     * @param t
     * @return a list of primary keys
     */
    <T> List<Long> insertAllAndGetKeys(List<T> t);

    /**
     * Updates an object in the database.
     *
     * @param t
     * @return true if successful
     */
    <T> boolean update(T t);

    /**
     * Updates all objects in the database.
     *
     * @param list
     */
    <T> void updateAll(List<T> list);

    /**
     * Inserts or updates an object in the database.
     *
     * @param t
     */
    <T> void merge(T t);

    /**
     * Deletes an object from the database.
     *
     * @param t
     * @return true if successful
     */
    <T> boolean delete(T t);

    /**
     * Deletes all objects from the database.
     *
     * @param list
     */
    <T> void deleteAll(List<T> list);

    /**
     * Returns the underlying Db instance for lower-level access to database methods
     * or direct JDBC access.
     *
     * @return the db instance
     */
    Db db();

    /**
     * Close the underlying Db instance.
     */
    @Override
    void close();

    /**
     * Used to specify custom names for method parameters to be used
     * for the SqlQuery or SqlUpdate annotations.
     * <p>
     * You don't need to explicitly bind the parameters as each parameter
     * is accessible by the standard "argN" syntax (0-indexed).
     * <p>
     * Additionally, if you are compiling with Java 8 AND specifying the
     * -parameters flag for javac, then you may use the parameter's name.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface Bind {
        String value();
    }

    /**
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface BindBean {
        String value() default "";
    }

    /**
     * Used to indicate that a method should execute a query.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface SqlQuery {
        String value();
    }

    /**
     * Used to indicate that a method should execute a statement.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface SqlStatement {
        String value();
    }

    public class BeanBinder {
        public void bind(BindBean bind, Object obj) {

        }
    }
}
