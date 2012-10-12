/*
 * Copyright 2004-2011 H2 Group.
 * Copyright 2011 James Moger.
 * Copyright 2012 Fr�d�ric Gaillard.
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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import com.iciql.Iciql.ConstraintDeleteType;
import com.iciql.Iciql.ConstraintUpdateType;
import com.iciql.TableDefinition.ConstraintForeignKeyDefinition;
import com.iciql.TableDefinition.ConstraintUniqueDefinition;
import com.iciql.TableDefinition.FieldDefinition;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.IciqlLogger;
import com.iciql.util.StatementBuilder;
import com.iciql.util.StringUtils;

/**
 * Default implementation of an SQL dialect.
 */
public class SQLDialectDefault implements SQLDialect {
	
	final String LITERAL = "'";

	float databaseVersion;
	String databaseName;
	String productVersion;

	@Override
	public String toString() {
		return getClass().getName() + ": " + databaseName + " " + productVersion;
	}

	@Override
	public void configureDialect(String databaseName, DatabaseMetaData data) {
		this.databaseName = databaseName;
		try {
			databaseVersion = Float.parseFloat(data.getDatabaseMajorVersion() + "."
					+ data.getDatabaseMinorVersion());
			productVersion = data.getDatabaseProductVersion();
		} catch (SQLException e) {
			throw new IciqlException(e);
		}
	}

	/**
	 * Allows subclasses to change the type of a column for a CREATE statement.
	 * 
	 * @param sqlType
	 * @return the SQL type or a preferred alternative
	 */
	@Override
	public String convertSqlType(String sqlType) {
		return sqlType;
	}

	@Override
	public Class<? extends java.util.Date> getDateTimeClass() {
		return java.util.Date.class;
	}

	@Override
	public String prepareTableName(String schemaName, String tableName) {
		if (StringUtils.isNullOrEmpty(schemaName)) {
			return tableName;
		}
		return schemaName + "." + tableName;
	}

	@Override
	public String prepareColumnName(String name) {
		return name;
	}

	@Override
	public <T> void prepareDropTable(SQLStatement stat, TableDefinition<T> def) {
		StatementBuilder buff = new StatementBuilder("DROP TABLE IF EXISTS "
				+ prepareTableName(def.schemaName, def.tableName));
		stat.setSQL(buff.toString());
		return;
	}

	protected <T> String prepareCreateTable(TableDefinition<T> def) {
		return "CREATE TABLE";
	}

	@Override
	public <T> void prepareCreateTable(SQLStatement stat, TableDefinition<T> def) {
		StatementBuilder buff = new StatementBuilder();
		buff.append(prepareCreateTable(def));
		buff.append(" ");
		buff.append(prepareTableName(def.schemaName, def.tableName)).append('(');

		boolean hasIdentityColumn = false;
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(field.columnName)).append(' ');
			String dataType = field.dataType;
			if (dataType.equals("VARCHAR")) {
				// check to see if we should use VARCHAR or CLOB
				if (field.length <= 0) {
					dataType = "CLOB";
				}
				buff.append(convertSqlType(dataType));
				if (field.length > 0) {
					buff.append('(').append(field.length).append(')');
				}
			} else if (dataType.equals("DECIMAL")) {
				// DECIMAL(precision,scale)
				buff.append(convertSqlType(dataType));
				if (field.length > 0) {
					buff.append('(').append(field.length);
					if (field.scale > 0) {
						buff.append(',').append(field.scale);
					}
					buff.append(')');
				}
			} else {
				// other
				hasIdentityColumn |= prepareColumnDefinition(buff, convertSqlType(dataType),
						field.isAutoIncrement, field.isPrimaryKey);
			}

			// default values
			if (!field.isAutoIncrement && !field.isPrimaryKey) {
				String dv = field.defaultValue;
				if (!StringUtils.isNullOrEmpty(dv)) {
					if (ModelUtils.isProperlyFormattedDefaultValue(dv)
							&& ModelUtils.isValidDefaultValue(field.field.getType(), dv)) {
						buff.append(" DEFAULT " + dv);
					}
				}
			}

			if (!field.nullable) {
				buff.append(" NOT NULL");
			}
		}

		// if table does not have identity column then specify primary key
		if (!hasIdentityColumn) {
			if (def.primaryKeyColumnNames != null && def.primaryKeyColumnNames.size() > 0) {
				buff.append(", PRIMARY KEY(");
				buff.resetCount();
				for (String n : def.primaryKeyColumnNames) {
					buff.appendExceptFirst(", ");
					buff.append(prepareColumnName(n));
				}
				buff.append(')');
			}
		}
		buff.append(')');
		stat.setSQL(buff.toString());
	}

	@Override
	public <T> void prepareDropView(SQLStatement stat, TableDefinition<T> def) {
		StatementBuilder buff = new StatementBuilder("DROP VIEW "
				+ prepareTableName(def.schemaName, def.tableName));
		stat.setSQL(buff.toString());
		return;
	}

	protected <T> String prepareCreateView(TableDefinition<T> def) {
		return "CREATE VIEW";
	}

	@Override
	public <T> void prepareCreateView(SQLStatement stat, TableDefinition<T> def) {
		StatementBuilder buff = new StatementBuilder();
		buff.append(" FROM ");
		buff.append(prepareTableName(def.schemaName, def.viewTableName));

		StatementBuilder where = new StatementBuilder();
		for (FieldDefinition field : def.fields) {
			if (!StringUtils.isNullOrEmpty(field.constraint)) {
				where.appendExceptFirst(", ");
				String col = prepareColumnName(field.columnName);
				String constraint = field.constraint.replace("{0}", col).replace("this", col);
				where.append(constraint);
			}
		}
		if (where.length() > 0) {
			buff.append(" WHERE ");
			buff.append(where.toString());
		}
		
		prepareCreateView(stat, def, buff.toString());
	}
	
	@Override
	public <T> void prepareCreateView(SQLStatement stat, TableDefinition<T> def, String fromWhere) {
		StatementBuilder buff = new StatementBuilder();
		buff.append(prepareCreateView(def));
		buff.append(" ");
		buff.append(prepareTableName(def.schemaName, def.tableName));

		buff.append(" AS SELECT ");
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(field.columnName));
		}
		buff.append(fromWhere);
		stat.setSQL(buff.toString());
	}
	
	protected boolean isIntegerType(String dataType) {
		if ("INT".equals(dataType)) {
			return true;
		} else if ("BIGINT".equals(dataType)) {
			return true;
		} else if ("TINYINT".equals(dataType)) {
			return true;
		} else if ("SMALLINT".equals(dataType)) {
			return true;
		}
		return false;
	}

	protected boolean prepareColumnDefinition(StatementBuilder buff, String dataType,
			boolean isAutoIncrement, boolean isPrimaryKey) {
		buff.append(dataType);
		if (isAutoIncrement) {
			buff.append(" AUTO_INCREMENT");
		}
		return false;
	}

	@Override
	public void prepareCreateIndex(SQLStatement stat, String schemaName, String tableName,
			IndexDefinition index) {
		StatementBuilder buff = new StatementBuilder();
		buff.append("CREATE ");
		switch (index.type) {
		case UNIQUE:
			buff.append("UNIQUE ");
			break;
		case UNIQUE_HASH:
			buff.append("UNIQUE ");
			break;
		default:
			IciqlLogger.warn("{0} does not support hash indexes", getClass().getSimpleName());
		}
		buff.append("INDEX ");
		buff.append(index.indexName);
		buff.append(" ON ");
		// FIXME maybe we can use schemaName ?
		// buff.append(prepareTableName(schemaName, tableName));
		buff.append(tableName);
		buff.append("(");
		for (String col : index.columnNames) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(col));
		}
		buff.append(") ");

		stat.setSQL(buff.toString().trim());
	}

	/**
	 * PostgreSQL and Derby do not support the SQL2003 MERGE syntax, but we can
	 * use a trick to insert a row if it does not exist and call update() in
	 * Db.merge() if the affected row count is 0.
	 * <p>
	 * Databases that do support a MERGE syntax should override this method.
	 * <p>
	 * http://stackoverflow.com/questions/407688
	 */
	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName,
			TableDefinition<T> def, Object obj) {
		StatementBuilder buff = new StatementBuilder("INSERT INTO ");
		buff.append(prepareTableName(schemaName, tableName));
		buff.append(" (");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(field.columnName));
		}
		buff.append(") (SELECT ");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append('?');
			Object value = def.getValue(obj, field);
			stat.addParameter(value);
		}
		buff.append(" FROM ");
		buff.append(prepareTableName(schemaName, tableName));
		buff.append(" WHERE ");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			if (field.isPrimaryKey) {
				buff.appendExceptFirst(" AND ");
				buff.append(MessageFormat.format("{0} = ?", prepareColumnName(field.columnName)));
				Object value = def.getValue(obj, field);
				stat.addParameter(value);
			}
		}
		buff.append(" HAVING count(*)=0)");
		stat.setSQL(buff.toString());
	}

	@Override
	public void appendLimitOffset(SQLStatement stat, long limit, long offset) {
		if (limit > 0) {
			stat.appendSQL(" LIMIT " + limit);
		}
		if (offset > 0) {
			stat.appendSQL(" OFFSET " + offset);
		}
	}
	
	@Override
	public String prepareParameter(Object o) {
		if (o instanceof String) {
			return LITERAL + o.toString().replace(LITERAL, "''") + LITERAL;
		} else if (o instanceof Character) {
			return LITERAL + o.toString() + LITERAL;
		} else if (o instanceof java.sql.Time) {
			return LITERAL + new SimpleDateFormat("HH:mm:ss").format(o) + LITERAL;
		} else if (o instanceof java.sql.Date) {
			return LITERAL + new SimpleDateFormat("yyyy-MM-dd").format(o) + LITERAL;
		} else if (o instanceof java.util.Date) {
			return LITERAL + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(o) + LITERAL;
		}
		return o.toString();
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void prepareCreateConstraintForeignKey(SQLStatement stat, String schemaName, String tableName, ConstraintForeignKeyDefinition constraint) {
		StatementBuilder buff = new StatementBuilder();
		buff.append("ALTER TABLE ");
		buff.append(prepareTableName(schemaName, tableName));
		buff.append(" ADD CONSTRAINT ");
		buff.append(constraint.constraintName);
		buff.append(" FOREIGN KEY ");
		buff.append(" (");
		for (String col : constraint.foreignColumns) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(col));
		}
		buff.append(") ");
		buff.append(" REFERENCES ");
		buff.append(constraint.referenceTable);
		buff.append(" (");
		buff.resetCount();
		for (String col : constraint.referenceColumns) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(col));
		}
		buff.append(") ");
		if (constraint.deleteType != ConstraintDeleteType.UNSET) {
			buff.append(" ON DELETE ");
			switch (constraint.deleteType) {
			case CASCADE:
				buff.append("CASCADE ");
				break;
			case RESTRICT:
				buff.append("RESTRICT ");
				break;
			case SET_NULL:
				buff.append("SET NULL ");
				break;
			case NO_ACTION:
				buff.append("NO ACTION ");
				break;
			case SET_DEFAULT:
				buff.append("SET DEFAULT ");
				break;
			}
		}
		if (constraint.updateType != ConstraintUpdateType.UNSET) {
			buff.append(" ON UPDATE ");
			switch (constraint.updateType) {
			case CASCADE:
				buff.append("CASCADE ");
				break;
			case RESTRICT:
				buff.append("RESTRICT ");
				break;
			case SET_NULL:
				buff.append("SET NULL ");
				break;
			case NO_ACTION:
				buff.append("NO ACTION ");
				break;
			case SET_DEFAULT:
				buff.append("SET DEFAULT ");
				break;
			}
		}
		switch (constraint.deferrabilityType) {
		case DEFERRABLE_INITIALLY_DEFERRED:
			buff.append("DEFERRABLE INITIALLY DEFERRED ");
			break;
		case DEFERRABLE_INITIALLY_IMMEDIATE:
			buff.append("DEFERRABLE INITIALLY IMMEDIATE ");
			break;
		case NOT_DEFERRABLE:
			buff.append("NOT DEFERRABLE ");
			break;
		case UNSET:
			break;
		}
		stat.setSQL(buff.toString().trim());
	}

	@Override
	public void prepareCreateConstraintUnique(SQLStatement stat, String schemaName, String tableName, ConstraintUniqueDefinition constraint) {
		StatementBuilder buff = new StatementBuilder();
		buff.append("ALTER TABLE ");
		buff.append(prepareTableName(schemaName, tableName));
		buff.append(" ADD CONSTRAINT ");
		buff.append(constraint.constraintName);
		buff.append(" UNIQUE ");
		buff.append(" (");
		for (String col : constraint.uniqueColumns) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(col));
		}
		buff.append(") ");
		stat.setSQL(buff.toString().trim());
	}

}