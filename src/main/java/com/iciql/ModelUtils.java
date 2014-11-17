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

import static com.iciql.util.StringUtils.isNullOrEmpty;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.iciql.TableDefinition.FieldDefinition;
import com.iciql.util.StringUtils;

/**
 * Utility methods for models related to type mapping, default value validation,
 * and class or field name creation.
 */
class ModelUtils {

	/**
	 * The list of supported data types. It is used by the runtime mapping for
	 * CREATE statements.
	 */
	private static final Map<Class<?>, String> SUPPORTED_TYPES = new HashMap<Class<?>, String>();

	static {
		Map<Class<?>, String> m = SUPPORTED_TYPES;
		m.put(String.class, "VARCHAR");
		m.put(Boolean.class, "BOOLEAN");
		m.put(Byte.class, "TINYINT");
		m.put(Short.class, "SMALLINT");
		m.put(Integer.class, "INT");
		m.put(Long.class, "BIGINT");
		m.put(Float.class, "REAL");
		m.put(Double.class, "DOUBLE");
		m.put(BigDecimal.class, "DECIMAL");
		m.put(java.sql.Timestamp.class, "TIMESTAMP");
		m.put(java.util.Date.class, "TIMESTAMP");
		m.put(java.sql.Date.class, "DATE");
		m.put(java.sql.Time.class, "TIME");
		m.put(byte[].class, "BLOB");
		m.put(UUID.class, "UUID");

		// map primitives
		m.put(boolean.class, m.get(Boolean.class));
		m.put(byte.class, m.get(Byte.class));
		m.put(short.class, m.get(Short.class));
		m.put(int.class, m.get(Integer.class));
		m.put(long.class, m.get(Long.class));
		m.put(float.class, m.get(Float.class));
		m.put(double.class, m.get(Double.class));
	}

	/**
	 * Convert SQL type aliases to the list of supported types. This map is used
	 * by generation and validation.
	 */
	private static final Map<String, String> SQL_TYPES = new HashMap<String, String>();

	static {
		Map<String, String> m = SQL_TYPES;
		m.put("CHAR", "VARCHAR");
		m.put("CHARACTER", "VARCHAR");
		m.put("NCHAR", "VARCHAR");
		m.put("VARCHAR_CASESENSITIVE", "VARCHAR");
		m.put("VARCHAR_IGNORECASE", "VARCHAR");
		m.put("LONGVARCHAR", "VARCHAR");
		m.put("VARCHAR2", "VARCHAR");
		m.put("NVARCHAR", "VARCHAR");
		m.put("NVARCHAR2", "VARCHAR");
		m.put("TEXT", "VARCHAR");
		m.put("NTEXT", "VARCHAR");
		m.put("TINYTEXT", "VARCHAR");
		m.put("MEDIUMTEXT", "VARCHAR");
		m.put("LONGTEXT", "VARCHAR");
		m.put("CLOB", "VARCHAR");
		m.put("NCLOB", "VARCHAR");

		// logic
		m.put("BIT", "BOOLEAN");
		m.put("BOOL", "BOOLEAN");

		// numeric
		m.put("BYTE", "TINYINT");
		m.put("INT2", "SMALLINT");
		m.put("YEAR", "SMALLINT");
		m.put("INTEGER", "INT");
		m.put("MEDIUMINT", "INT");
		m.put("INT4", "INT");
		m.put("SIGNED", "INT");
		m.put("INT8", "BIGINT");
		m.put("IDENTITY", "BIGINT");
		m.put("SERIAL", "INT");
		m.put("BIGSERIAL", "BIGINT");

		// decimal
		m.put("NUMBER", "DECIMAL");
		m.put("DEC", "DECIMAL");
		m.put("NUMERIC", "DECIMAL");
		m.put("FLOAT", "DOUBLE");
		m.put("FLOAT4", "DOUBLE");
		m.put("FLOAT8", "DOUBLE");
		m.put("DOUBLE PRECISION", "DOUBLE");

		// date
		m.put("DATETIME", "TIMESTAMP");
		m.put("SMALLDATETIME", "TIMESTAMP");

		// binary types
		m.put("TINYBLOB", "BLOB");
		m.put("MEDIUMBLOB", "BLOB");
		m.put("LONGBLOB", "BLOB");
		m.put("IMAGE", "BLOB");
		m.put("OID", "BLOB");
	}

	private static final List<String> KEYWORDS = Arrays.asList("abstract", "assert", "boolean", "break",
			"byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else",
			"enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import",
			"instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected",
			"public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
			"throw", "throws", "transient", "try", "void", "volatile", "while", "false", "null", "true");

	/**
	 * Returns a SQL type mapping for a Java class.
	 *
	 * @param fieldDef
	 *            the field to map
	 * @return
	 */
	static String getDataType(FieldDefinition fieldDef) {
		Class<?> fieldClass = fieldDef.field.getType();
		if (fieldClass.isEnum()) {
			switch (fieldDef.enumType) {
			case ORDINAL:
				return "INT";
			case ENUMID:
				String sqlType = SUPPORTED_TYPES.get(fieldDef.enumTypeClass);
				if (sqlType == null) {
					throw new IciqlException("Unsupported enum mapping type {0} for {1}",
							fieldDef.enumTypeClass, fieldDef.columnName);
				}
				return sqlType;
			case NAME:
			default:
				return "VARCHAR";
			}
		}
		if (SUPPORTED_TYPES.containsKey(fieldClass)) {
			return SUPPORTED_TYPES.get(fieldClass);
		}
		throw new IciqlException("Unsupported type " + fieldClass.getName());
	}

	/**
	 * Returns the Java class for a given SQL type.
	 *
	 * @param sqlType
	 * @param dateTimeClass
	 *            the preferred date class (java.util.Date or
	 *            java.sql.Timestamp)
	 * @return
	 */
	static Class<?> getClassForSqlType(String sqlType, Class<? extends java.util.Date> dateTimeClass) {
		sqlType = sqlType.toUpperCase();
		// XXX dropping "UNSIGNED" or parts like that could be trouble
		sqlType = sqlType.split(" ")[0].trim();
		if (sqlType.indexOf('(') > -1) {
			// strip out length or precision
			sqlType = sqlType.substring(0, sqlType.indexOf('('));
		}

		if (SQL_TYPES.containsKey(sqlType)) {
			// convert the sqlType to a standard type
			sqlType = SQL_TYPES.get(sqlType);
		}
		Class<?> mappedClass = null;
		for (Class<?> clazz : SUPPORTED_TYPES.keySet()) {
			if (clazz.isPrimitive()) {
				// do not map from SQL TYPE to primitive type
				continue;
			}
			if (SUPPORTED_TYPES.get(clazz).equalsIgnoreCase(sqlType)) {
				mappedClass = clazz;

				break;
			}
		}
		if (mappedClass != null) {
			if (mappedClass.equals(java.util.Date.class) || mappedClass.equals(java.sql.Timestamp.class)) {
				return dateTimeClass;
			}
			return mappedClass;
		}
		return null;
	}

	/**
	 * Tries to create a convert a SQL table name to a camel case class name.
	 *
	 * @param tableName
	 *            the SQL table name
	 * @return the class name
	 */
	static String convertTableToClassName(String tableName) {
		String[] chunks = StringUtils.arraySplit(tableName, '_', false);
		StringBuilder className = new StringBuilder();
		for (String chunk : chunks) {
			if (chunk.length() == 0) {
				// leading or trailing _
				continue;
			}
			String[] subchunks = StringUtils.arraySplit(chunk, ' ', false);
			for (String subchunk : subchunks) {
				if (subchunk.length() == 0) {
					// leading or trailing space
					continue;
				}
				className.append(Character.toUpperCase(subchunk.charAt(0)));
				className.append(subchunk.substring(1).toLowerCase());
			}
		}
		return className.toString();
	}

	/**
	 * Ensures that SQL column names don't collide with Java keywords.
	 *
	 * @param columnName
	 *            the column name
	 * @return the Java field name
	 */
	static String convertColumnToFieldName(String columnName) {
		String lower = columnName.toLowerCase();
		if (KEYWORDS.contains(lower)) {
			lower += "Value";
		}
		return lower;
	}

	/**
	 * Converts a DEFAULT clause value into an object.
	 *
	 * @param field
	 *            definition
	 * @return object
	 */
	static Object getDefaultValue(FieldDefinition def, Class<? extends Date> dateTimeClass) {
		Class<?> valueType = getClassForSqlType(def.dataType, dateTimeClass);
		if (String.class.isAssignableFrom(valueType)) {
			if (StringUtils.isNullOrEmpty(def.defaultValue)) {
				// literal default must be specified within single quotes
				return null;
			}
			if (def.defaultValue.charAt(0) == '\''
					&& def.defaultValue.charAt(def.defaultValue.length() - 1) == '\'') {
				// strip leading and trailing single quotes
				return def.defaultValue.substring(1, def.defaultValue.length() - 1).trim();
			}
			return def.defaultValue;
		}

		if (StringUtils.isNullOrEmpty(def.defaultValue)) {
			// can not create object from empty string
			return null;
		}

		// strip leading and trailing single quotes
		String content = def.defaultValue;
		if (content.charAt(0) == '\'') {
			content = content.substring(1);
		}
		if (content.charAt(content.length() - 1) == '\'') {
			content = content.substring(0, content.length() - 2);
		}

		if (StringUtils.isNullOrEmpty(content)) {
			// can not create object from empty string
			return null;
		}

		if (Boolean.class.isAssignableFrom(valueType) || boolean.class.isAssignableFrom(valueType)) {
			return Boolean.parseBoolean(content);
		}

		if (Number.class.isAssignableFrom(valueType)) {
			try {
				// delegate to static valueOf() method to parse string
				Method m = valueType.getMethod("valueOf", String.class);
				return m.invoke(null, content);
			} catch (NumberFormatException e) {
				throw new IciqlException(e, "Failed to parse {0} as a number!", def.defaultValue);
			} catch (Throwable t) {
			}
		}

		String dateRegex = "[0-9]{1,4}[-/\\.][0-9]{1,2}[-/\\.][0-9]{1,2}";
		String timeRegex = "[0-2]{1}[0-9]{1}:[0-5]{1}[0-9]{1}:[0-5]{1}[0-9]{1}";

		if (java.sql.Date.class.isAssignableFrom(valueType)) {
			// this may be a little loose....
			// 00-00-00
			// 00/00/00
			// 00.00.00
			Pattern pattern = Pattern.compile(dateRegex);
			if (pattern.matcher(content).matches()) {
				DateFormat df = DateFormat.getDateInstance();
				try {
					return df.parse(content);
				} catch (Exception e) {
					throw new IciqlException(e, "Failed to parse {0} as a date!", def.defaultValue);
				}
			}
		}

		if (java.sql.Time.class.isAssignableFrom(valueType)) {
			// 00:00:00
			Pattern pattern = Pattern.compile(timeRegex);
			if (pattern.matcher(content).matches()) {
				DateFormat df = DateFormat.getTimeInstance();
				try {
					return df.parse(content);
				} catch (Exception e) {
					throw new IciqlException(e, "Failed to parse {0} as a time!", def.defaultValue);
				}
			}
		}

		if (java.util.Date.class.isAssignableFrom(valueType)) {
			// this may be a little loose....
			// 00-00-00 00:00:00
			// 00/00/00T00:00:00
			// 00.00.00T00:00:00
			Pattern pattern = Pattern.compile(dateRegex + "." + timeRegex);
			if (pattern.matcher(content).matches()) {
				DateFormat df = DateFormat.getDateTimeInstance();
				try {
					return df.parse(content);
				} catch (Exception e) {
					throw new IciqlException(e, "Failed to parse {0} as a datetimestamp!", def.defaultValue);
				}
			}
		}
		return content;
	}

	/**
	 * Converts the object into a DEFAULT clause value.
	 *
	 * @param o
	 *            the default object
	 * @return the value formatted for a DEFAULT clause
	 */
	static String formatDefaultValue(Object o) {
		Class<?> objectClass = o.getClass();
		String value = null;
		if (Number.class.isAssignableFrom(objectClass)) {
			// NUMBER
			return ((Number) o).toString();
		} else if (Boolean.class.isAssignableFrom(objectClass)) {
			// BOOLEAN
			return o.toString();
		} else if (java.sql.Date.class.isAssignableFrom(objectClass)) {
			// DATE
			value = new SimpleDateFormat("yyyy-MM-dd").format((Date) o);
		} else if (java.sql.Time.class.isAssignableFrom(objectClass)) {
			// TIME
			value = new SimpleDateFormat("HH:mm:ss").format((Date) o);
		} else if (Date.class.isAssignableFrom(objectClass)) {
			// DATETIME
			value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) o);
		} else if (String.class.isAssignableFrom(objectClass)) {
			// STRING
			value = o.toString();
		}
		if (value == null) {
			return "''";
		}
		return MessageFormat.format("''{0}''", value);
	}

	/**
	 * Checks the formatting of IQColumn.defaultValue().
	 *
	 * @param defaultValue
	 *            the default value
	 * @return true if it is
	 */
	static boolean isProperlyFormattedDefaultValue(String defaultValue) {
		if (isNullOrEmpty(defaultValue)) {
			return true;
		}
		Pattern literalDefault = Pattern.compile("'.*'");
		Pattern functionDefault = Pattern.compile("[^'].*[^']");
		return literalDefault.matcher(defaultValue).matches()
				|| functionDefault.matcher(defaultValue).matches();
	}

	/**
	 * Checks to see if the default value matches the class.
	 *
	 * @param modelClass
	 *            the class
	 * @param defaultValue
	 *            the value
	 * @return true if it does
	 */
	static boolean isValidDefaultValue(Class<?> modelClass, String defaultValue) {

		if (defaultValue == null) {
			// NULL
			return true;
		}
		if (defaultValue.trim().length() == 0) {
			// NULL (effectively)
			return true;
		}

		// function / variable
		Pattern functionDefault = Pattern.compile("[^'].*[^']");
		if (functionDefault.matcher(defaultValue).matches()) {
			// hard to validate this since its in the database
			// assume it is good
			return true;
		}

		// STRING
		if (modelClass == String.class) {
			Pattern stringDefault = Pattern.compile("'(.|\\n)*'");
			return stringDefault.matcher(defaultValue).matches();
		}

		String dateRegex = "[0-9]{1,4}[-/\\.][0-9]{1,2}[-/\\.][0-9]{1,2}";
		String timeRegex = "[0-2]{1}[0-9]{1}:[0-5]{1}[0-9]{1}:[0-5]{1}[0-9]{1}";

		// TIMESTAMP
		if (modelClass == java.util.Date.class || modelClass == java.sql.Timestamp.class) {
			// this may be a little loose....
			// 00-00-00 00:00:00
			// 00/00/00T00:00:00
			// 00.00.00T00:00:00
			Pattern pattern = Pattern.compile("'" + dateRegex + "." + timeRegex + "'");
			return pattern.matcher(defaultValue).matches();
		}

		// DATE
		if (modelClass == java.sql.Date.class) {
			// this may be a little loose....
			// 00-00-00
			// 00/00/00
			// 00.00.00
			Pattern pattern = Pattern.compile("'" + dateRegex + "'");
			return pattern.matcher(defaultValue).matches();
		}

		// TIME
		if (modelClass == java.sql.Time.class) {
			// 00:00:00
			Pattern pattern = Pattern.compile("'" + timeRegex + "'");
			return pattern.matcher(defaultValue).matches();
		}

		// NUMBER
		if (Number.class.isAssignableFrom(modelClass)) {
			// strip single quotes
			String unquoted = defaultValue;
			if (unquoted.charAt(0) == '\'') {
				unquoted = unquoted.substring(1);
			}
			if (unquoted.charAt(unquoted.length() - 1) == '\'') {
				unquoted = unquoted.substring(0, unquoted.length() - 1);
			}

			try {
				// delegate to static valueOf() method to parse string
				Method m = modelClass.getMethod("valueOf", String.class);
				m.invoke(null, unquoted);
			} catch (NumberFormatException ex) {
				return false;
			} catch (Throwable t) {
			}
		}
		return true;
	}
}
