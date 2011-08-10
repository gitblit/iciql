package com.iciql.dialect;

import com.iciql.SQLStatement;
import com.iciql.TableDefinition;
import com.iciql.TableDefinition.FieldDefinition;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StatementBuilder;

/**
 * H2 database dialect.
 */
public class MySQLDialect extends DefaultSQLDialect {

	@Override
	public boolean supportsMemoryTables() {
		return false;
	}

	@Override
	public boolean supportsMerge() {
		return true;
	}

	@Override
	public String prepareColumnName(String name) {
		return "`" + name + "`";
	}
	
	@Override
	public String prepareCreateIndex(String schema, String table, IndexDefinition index) {
		StatementBuilder buff = new StatementBuilder();
		buff.append("CREATE ");
		switch (index.type) {
		case STANDARD:
			break;
		case UNIQUE:
			buff.append("UNIQUE ");
			break;
		case UNIQUE_HASH:
			buff.append("UNIQUE ");
			break;
		}
		buff.append("INDEX ");
		buff.append(index.indexName);
		buff.append(" ON ");
		buff.append(table);
		buff.append("(");
		for (String col : index.columnNames) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(col));
		}
		buff.append(") ");
		
		// USING
		switch (index.type) {
		case HASH:
			buff.append("USING HASH");
			break;
		case UNIQUE_HASH:
			buff.append("USING HASH");
			break;
		}
		return buff.toString().trim();
	}
	
	@Override
	public <T> void prepareMerge(SQLStatement stat, String schemaName, String tableName, TableDefinition<T> def, Object obj) {		
		StatementBuilder buff = new StatementBuilder("REPLACE INTO ");
		buff.append(prepareTableName(schemaName, tableName)).append('(');
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append(prepareColumnName(field.columnName));
		}
		buff.append(") VALUES(");
		buff.resetCount();
		for (FieldDefinition field : def.fields) {
			buff.appendExceptFirst(", ");
			buff.append('?');
			Object value = def.getValue(obj, field);
			stat.addParameter(value);
		}
		buff.append(')');
		stat.setSQL(buff.toString());
	}
}