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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class that implements this interface can be used as a database table.
 * <p>
 * You may implement the Table interface on your model object and optionally use
 * IQColumn annotations (which imposes a compile-time and runtime-dependency on
 * iciql), or may choose to use the IQTable and IQColumn annotations only (which
 * imposes a compile-time and runtime-dependency on this file only).
 * <p>
 * If a class is annotated with IQTable and at the same time implements Table,
 * the define() method is not called.
 * <p>
 * Fully Supported Data Types:
 * <table>
 * <tr>
 * <th colspan="2">All Databases</th>
 * </tr>
 * <tr>
 * <td>java.lang.String</td>
 * <td>VARCHAR (length > 0) or CLOB (length == 0)</td>
 * </tr>
 * <tr>
 * <td>java.lang.Boolean</td>
 * <td>BIT</td>
 * </tr>
 * <tr>
 * <td>java.lang.Byte</td>
 * <td>TINYINT</td>
 * </tr>
 * <tr>
 * <td>java.lang.Short</td>
 * <td>SMALLINT</td>
 * </tr>
 * <tr>
 * <td>java.lang.Integer</td>
 * <td>INT</td>
 * </tr>
 * <tr>
 * <td>java.lang.Long</td>
 * <td>BIGINT</td>
 * </tr>
 * <tr>
 * <td>java.lang.Float</td>
 * <td>REAL</td>
 * </tr>
 * <tr>
 * <td>java.lang.Double</td>
 * <td>DOUBLE</td>
 * </tr>
 * <tr>
 * <td>java.math.BigDecimal</td>
 * <td>DECIMAL (length == 0)<br/>
 * DECIMAL(length, scale) (length > 0)</td>
 * </tr>
 * <tr>
 * <td>java.sql.Date</td>
 * <td>DATE</td>
 * </tr>
 * <tr>
 * <td>java.sql.Time</td>
 * <td>TIME</td>
 * </tr>
 * <tr>
 * <td>java.sql.Timestamp</td>
 * <td>TIMESTAMP</td>
 * </tr>
 * <tr>
 * <td>java.util.Date</td>
 * <td>TIMESTAMP</td>
 * </tr>
 * <tr>
 * <td>java.lang.Enum.name()</td>
 * <td>VARCHAR (length > 0) or CLOB (length == 0)<br/>
 * EnumType.NAME</td>
 * </tr>
 * <tr>
 * <td>java.lang.Enum.ordinal()</td>
 * <td>INT<br/>
 * EnumType.ORDINAL</td>
 * </tr>
 * <tr>
 * <td>java.lang.Enum implements<br/>
 * com.iciql.Iciql.EnumID.enumId()</td>
 * <td>INT<br/>
 * EnumType.ENUMID</td>
 * </tr>
 * <tr>
 * <th colspan="2">H2 Databases</th>
 * </tr>
 * <tr>
 * <td>java.util.UUID</td>
 * <td>UUID</td>
 * </tr>
 * </table>
 * <p>
 * Partially Supported Data Types:
 * <p>
 * The following data types can be mapped to columns for all general statements
 * BUT these field types may not be used to specify compile-time clauses or
 * constraints.
 * <table>
 * <tr>
 * <td>byte []</td>
 * <td>BLOB</td>
 * </tr>
 * <tr>
 * <td>boolean</td>
 * <td>BIT</td>
 * </tr>
 * <tr>
 * <td>byte</td>
 * <td>TINYINT</td>
 * </tr>
 * <tr>
 * <td>short</td>
 * <td>SMALLINT</td>
 * </tr>
 * <tr>
 * <td>int</td>
 * <td>INT</td>
 * </tr>
 * <tr>
 * <td>long</td>
 * <td>BIGINT</td>
 * </tr>
 * <tr>
 * <td>float</td>
 * <td>REAL</td>
 * </tr>
 * <tr>
 * <td>double</td>
 * <td>DOUBLE</td>
 * </tr>
 * </table>
 * <p>
 * Table and field mapping: by default, the mapped table name is the class name
 * and the public fields are reflectively mapped, by their name, to columns. As
 * an alternative, you may specify both the table and column definition by
 * annotations.
 * <p>
 * Table Interface: you may set additional parameters such as table name,
 * primary key, and indexes in the define() method.
 * <p>
 * Annotations: you may use the annotations with or without implementing the
 * Table interface. The annotations allow you to decouple your model completely
 * from iciql other than this file.
 * <p>
 * Automatic model generation: you may automatically generate model classes as
 * strings with the Db and DbInspector objects:
 * 
 * <pre>
 * Db db = Db.open(&quot;jdbc:h2:mem:&quot;, &quot;sa&quot;, &quot;sa&quot;);
 * DbInspector inspector = new DbInspector(db);
 * List&lt;String&gt; models =
 *         inspector.generateModel(schema, table, packageName,
 *         annotateSchema, trimStrings)
 * </pre>
 * 
 * Or you may use the GenerateModels tool to generate and save your classes to
 * the file system:
 * 
 * <pre>
 * java -jar iciql.jar
 *      -url &quot;jdbc:h2:mem:&quot;
 *      -user sa -password sa -schema schemaName -table tableName
 *      -package packageName -folder destination
 *      -annotateSchema false -trimStrings true
 * </pre>
 * 
 * Model validation: you may validate your model class with DbInspector object.
 * The DbInspector will report errors, warnings, and suggestions:
 * 
 * <pre>
 * Db db = Db.open(&quot;jdbc:h2:mem:&quot;, &quot;sa&quot;, &quot;sa&quot;);
 * DbInspector inspector = new DbInspector(db);
 * List&lt;Validation&gt; remarks = inspector.validateModel(new MyModel(), throwOnError);
 * for (Validation remark : remarks) {
 * 	System.out.println(remark);
 * }
 * </pre>
 */
public interface Iciql {

	/**
	 * An annotation for an iciql version.
	 * <p>
	 * 
	 * @IQVersion(1)
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface IQVersion {

		/**
		 * If set to a non-zero value, iciql maintains a "_iq_versions" table
		 * within your database. The version number is used to call to a
		 * registered DbUpgrader implementation to perform relevant ALTER
		 * statements. Default: 0. You must specify a DbUpgrader on your Db
		 * object to use this parameter.
		 */
		int value() default 0;

	}

	/**
	 * An annotation for a schema.
	 * <p>
	 * 
	 * @IQSchema("PUBLIC")
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface IQSchema {

		/**
		 * The schema may be optionally specified. Default: unspecified.
		 */
		String value() default "";

	}

	/**
	 * Enumeration defining the four index types.
	 */
	public static enum IndexType {
		STANDARD, UNIQUE, HASH, UNIQUE_HASH;
	}

	/**
	 * An index annotation.
	 * <p>
	 * <ul>
	 * <li>@IQIndex("name")
	 * <li>@IQIndex({"street", "city"})
	 * <li>@IQIndex(name="streetidx", value={"street", "city"})
	 * <li>@IQIndex(name="addressidx", type=IndexType.UNIQUE,
	 * value={"house_number", "street", "city"})
	 * </ul>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface IQIndex {

		/**
		 * Index name. If null or empty, iciql will generate one.
		 */
		String name() default "";

		/**
		 * Type of the index.
		 * <ul>
		 * <li>com.iciql.iciql.IndexType.STANDARD
		 * <li>com.iciql.iciql.IndexType.UNIQUE
		 * <li>com.iciql.iciql.IndexType.HASH
		 * <li>com.iciql.iciql.IndexType.UNIQUE_HASH
		 * </ul>
		 * 
		 * HASH indexes may only be valid for single column indexes.
		 * 
		 */
		IndexType type() default IndexType.STANDARD;

		/**
		 * Columns to include in index.
		 * <ul>
		 * <li>single column index: value = "id"
		 * <li>multiple column index: value = { "id", "name", "date" }
		 * </ul>
		 */
		String[] value() default {};
	}

	/**
	 * Annotation to specify multiple indexes.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface IQIndexes {
		IQIndex[] value() default {};
	}

	/**
	 * Annotation to define a table.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface IQTable {

		/**
		 * The table name. If not specified the class name is used as the table
		 * name.
		 * <p>
		 * The table name may still be overridden in the define() method if the
		 * model class is not annotated with IQTable. Default: unspecified.
		 */
		String name() default "";

		/**
		 * The primary key may be optionally specified. If it is not specified,
		 * then no primary key is set by the IQTable annotation. You may specify
		 * a composite primary key.
		 * <ul>
		 * <li>single column primaryKey: value = "id"
		 * <li>compound primary key: value = { "id", "name" }
		 * </ul>
		 * The primary key may still be overridden in the define() method if the
		 * model class is not annotated with IQTable. Default: unspecified.
		 */
		String[] primaryKey() default {};

		/**
		 * The inherit columns allows this model class to inherit columns from
		 * its super class. Any IQTable annotation present on the super class is
		 * ignored. Default: false.
		 */
		boolean inheritColumns() default false;

		/**
		 * Whether or not iciql tries to create the table and indexes. Default:
		 * true.
		 */
		boolean create() default true;

		/**
		 * If true, only fields that are explicitly annotated as IQColumn are
		 * mapped. Default: true.
		 */
		boolean annotationsOnly() default true;

		/**
		 * If true, this table is created as a memory table where data is
		 * persistent, but index data is kept in main memory. Valid only for H2
		 * and HSQL databases. Default: false.
		 */
		boolean memoryTable() default false;
	}

	/**
	 * Annotation to define a column. Annotated fields may have any scope
	 * (however, the JVM may raise a SecurityException if the SecurityManager
	 * doesn't allow iciql to access the field.)
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface IQColumn {

		/**
		 * If not specified, the field name is used as the column name. Default:
		 * the field name.
		 */
		String name() default "";

		/**
		 * This column is the primary key. Default: false.
		 */
		boolean primaryKey() default false;

		/**
		 * The column is created with a sequence as the default value. Default:
		 * false.
		 */
		boolean autoIncrement() default false;

		/**
		 * Length is used to define the length of a VARCHAR column or to define
		 * the precision of a DECIMAL(precision, scale) expression.
		 * <p>
		 * If larger than zero, it is used during the CREATE TABLE phase. For
		 * string values it may also be used to prevent database exceptions on
		 * INSERT and UPDATE statements (see trim).
		 * <p>
		 * Any length set in define() may override this annotation setting if
		 * the model class is not annotated with IQTable. Default: 0.
		 */
		int length() default 0;

		/**
		 * Scale is used during the CREATE TABLE phase to define the scale of a
		 * DECIMAL(precision, scale) expression.
		 * <p>
		 * Any scale set in define() may override this annotation setting if the
		 * model class is not annotated with IQTable. Default: 0.
		 */
		int scale() default 0;

		/**
		 * If true, iciql will automatically trim the string if it exceeds
		 * length (value.substring(0, length)). Default: false.
		 */
		boolean trim() default false;

		/**
		 * If false, iciql will set the column NOT NULL during the CREATE TABLE
		 * phase. Default: true.
		 */
		boolean nullable() default true;

		/**
		 * The default value assigned to the column during the CREATE TABLE
		 * phase. This field could contain a literal single-quoted value, or a
		 * function call. Empty strings are considered NULL. Examples:
		 * <ul>
		 * <li>defaultValue="" (null)
		 * <li>defaultValue="CURRENT_TIMESTAMP"
		 * <li>defaultValue="''" (empty string)
		 * <li>defaultValue="'0'"
		 * <li>defaultValue="'1970-01-01 00:00:01'"
		 * </ul>
		 * if the default value is specified, and auto increment is disabled,
		 * and primary key is disabled, then this value is included in the
		 * "DEFAULT ..." phrase of a column during the CREATE TABLE process.
		 * <p>
		 * Alternatively, you may specify a default object value on the field
		 * and this will be converted to a properly formatted DEFAULT expression
		 * during the CREATE TABLE process.
		 * <p>
		 * Default: unspecified (null).
		 */
		String defaultValue() default "";

	}

	/**
	 * Interface for using the EnumType.ENUMID enumeration mapping strategy.
	 * <p>
	 * Enumerations wishing to use EnumType.ENUMID must implement this
	 * interface.
	 */
	public interface EnumId {
		int enumId();
	}

	/**
	 * Enumeration representing how to map a java.lang.Enum to a column.
	 * <p>
	 * <ul>
	 * <li>NAME - name() : string
	 * <li>ORDINAL - ordinal() : int
	 * <li>ENUMID - enumId() : int
	 * </ul>
	 * 
	 * @see com.iciql.Iciql.EnumId interface
	 */
	public enum EnumType {
		NAME, ORDINAL, ENUMID;

		public static final EnumType DEFAULT_TYPE = NAME;
	}

	/**
	 * Annotation to define how a java.lang.Enum is mapped to a column.
	 * <p>
	 * This annotation can be used on:
	 * <ul>
	 * <li>a field instance of an enumeration type
	 * <li>on the enumeration class declaration
	 * </ul>
	 * If you choose to annotate the class declaration, that will be the default
	 * mapping strategy for all @IQColumn instances of the enum. This can still
	 * be overridden for an individual field by specifying the IQEnum
	 * annotation.
	 * <p>
	 * The default mapping is by NAME.
	 * 
	 * <pre>
	 * IQEnum(EnumType.NAME)
	 * </pre>
	 * 
	 * A string mapping will generate either a VARCHAR, if IQColumn.length > 0
	 * or a TEXT column if IQColumn.length == 0
	 * 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.TYPE })
	public @interface IQEnum {
		EnumType value() default EnumType.NAME;
	}

	/**
	 * This method is called to let the table define the primary key, indexes,
	 * and the table name.
	 */
	@Deprecated
	void defineIQ();
}
