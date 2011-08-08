package com.iciql.dialect;

import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StatementBuilder;

/**
 * H2 database dialect.
 */
public class H2Dialect extends DefaultSQLDialect {

	@Override
	public boolean supportsMemoryTables() {
		return true;
	}

	@Override
	public boolean supportsMerge() {
		return true;
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
		case HASH:
			buff.append("HASH ");
			break;
		case UNIQUE_HASH:
			buff.append("UNIQUE HASH ");
			break;
		}
		buff.append("INDEX IF NOT EXISTS ");
		buff.append(index.indexName);
		buff.append(" ON ");
		buff.append(table);
		buff.append("(");
		for (String col : index.columnNames) {
			buff.appendExceptFirst(", ");
			buff.append(col);
		}
		buff.append(")");
		return buff.toString();
	}
}