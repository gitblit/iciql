/*
 * Copyright 2004-2011 H2 Group.
 * Copyright 2011 James Moger.
 * Copyright 2012 Frédéric Gaillard.
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

import com.iciql.Iciql.IQColumn;
import com.iciql.Iciql.IQIndex;
import com.iciql.Iciql.IQIndexes;
import com.iciql.Iciql.IQSchema;
import com.iciql.Iciql.IQTable;
import com.iciql.Iciql.IndexType;
import com.iciql.TableDefinition.ConstraintForeignKeyDefinition;
import com.iciql.TableDefinition.ConstraintUniqueDefinition;
import com.iciql.TableDefinition.FieldDefinition;
import com.iciql.TableDefinition.IndexDefinition;
import com.iciql.util.StatementBuilder;
import com.iciql.util.StringUtils;
import com.iciql.util.Utils;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.iciql.ValidationRemark.*;
import static com.iciql.util.JdbcUtils.closeSilently;
import static com.iciql.util.StringUtils.isNullOrEmpty;
import static java.text.MessageFormat.format;

/**
 * Class to inspect the contents of a particular table including its indexes.
 * This class does the bulk of the work in terms of model generation and model
 * validation.
 */
public class TableInspector {

    private String schema;
    private String table;
    private Class<? extends java.util.Date> dateTimeClass;
    private List<String> primaryKeys = Utils.newArrayList();
    private Map<String, IndexInspector> indexes;
    private Map<String, ColumnInspector> columns;
    private final String eol = "\n";

    TableInspector(String schema, String table, Class<? extends java.util.Date> dateTimeClass) {
        this.schema = schema;
        this.table = table;
        this.dateTimeClass = dateTimeClass;
    }

    /**
     * Tests to see if this TableInspector represents schema.table.
     * <p>
     *
     * @param schema the schema name
     * @param table  the table name
     * @return true if the table matches
     */
    boolean matches(String schema, String table) {
        if (isNullOrEmpty(schema)) {
            // table name matching
            return this.table.equalsIgnoreCase(table);
        } else if (isNullOrEmpty(table)) {
            // schema name matching
            return this.schema.equalsIgnoreCase(schema);
        } else {
            // exact table matching
            return this.schema.equalsIgnoreCase(schema) && this.table.equalsIgnoreCase(table);
        }
    }

    /**
     * Reads the DatabaseMetaData for the details of this table including
     * primary keys and indexes.
     *
     * @param metaData the database meta data
     */
    void read(DatabaseMetaData metaData) throws SQLException {
        ResultSet rs = null;

        // primary keys
        try {
            rs = metaData.getPrimaryKeys(null, schema, table);
            while (rs.next()) {
                String c = rs.getString("COLUMN_NAME");
                primaryKeys.add(c);
            }
            closeSilently(rs);

            // indexes
            rs = metaData.getIndexInfo(null, schema, table, false, true);
            indexes = Utils.newHashMap();
            while (rs.next()) {
                IndexInspector info = new IndexInspector(rs);
                if (info.type.equals(IndexType.UNIQUE)) {
                    if(info.name == null) continue;
                    String name = info.name.toLowerCase();
                    if (name.startsWith("primary") || name.startsWith("sys_idx_sys_pk")
                            || name.startsWith("sql") || name.endsWith("_pkey")) {
                        // skip primary key indexes
                        continue;
                    }
                }
                if (indexes.containsKey(info.name)) {
                    indexes.get(info.name).addColumn(rs);
                } else {
                    indexes.put(info.name, info);
                }
            }
            closeSilently(rs);

            // columns
            rs = metaData.getColumns(null, schema, table, null);
            columns = Utils.newHashMap();
            while (rs.next()) {
                ColumnInspector col = new ColumnInspector();
                col.name = rs.getString("COLUMN_NAME");
                col.type = rs.getString("TYPE_NAME");
                col.clazz = ModelUtils.getClassForSqlType(col.type, dateTimeClass);
                col.size = rs.getInt("COLUMN_SIZE");
                col.nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                try {
                    Object autoIncrement = rs.getObject("IS_AUTOINCREMENT");
                    if (autoIncrement instanceof Boolean) {
                        col.isAutoIncrement = (Boolean) autoIncrement;
                    } else if (autoIncrement instanceof String) {
                        String val = autoIncrement.toString().toLowerCase();
                        col.isAutoIncrement = val.equals("true") | val.equals("yes");
                    } else if (autoIncrement instanceof Number) {
                        Number n = (Number) autoIncrement;
                        col.isAutoIncrement = n.intValue() > 0;
                    }
                } catch (SQLException s) {
//					throw s;
                }
                if (primaryKeys.size() == 1) {
                    if (col.name.equalsIgnoreCase(primaryKeys.get(0))) {
                        col.isPrimaryKey = true;
                    }
                }
                if (!col.isAutoIncrement) {
                    col.defaultValue = rs.getString("COLUMN_DEF");
                }
                columns.put(col.name.toLowerCase(), col);
            }
        } finally {
            closeSilently(rs);
        }
    }

    /**
     * Generates a model (class definition) from this table. The model includes
     * indexes, primary keys, default values, lengths, and nullables.
     * information.
     * <p>
     * The caller may optionally set a destination package name, whether or not
     * to include the schema name (setting schema can be a problem when using
     * the model between databases), and if to automatically trim strings for
     * those that have a maximum length.
     * <p>
     *
     * @param packageName
     * @param annotateSchema
     * @param trimStrings
     * @return a complete model (class definition) for this table as a string
     */
    String generateModel(String packageName, boolean annotateSchema, boolean trimStrings) {

        // import statements
        Set<String> imports = Utils.newHashSet();
        imports.add(Serializable.class.getCanonicalName());
        imports.add(IQSchema.class.getCanonicalName());
        imports.add(IQTable.class.getCanonicalName());
        imports.add(IQIndexes.class.getCanonicalName());
        imports.add(IQIndex.class.getCanonicalName());
        imports.add(IQColumn.class.getCanonicalName());
        imports.add(IndexType.class.getCanonicalName());

        // fields
        StringBuilder fields = new StringBuilder();
        List<ColumnInspector> sortedColumns = Utils.newArrayList(columns.values());
        Collections.sort(sortedColumns);
        for (ColumnInspector col : sortedColumns) {
            fields.append(generateColumn(imports, col, trimStrings));
        }

        // build complete class definition
        StringBuilder model = new StringBuilder();
        if (!isNullOrEmpty(packageName)) {
            // package
            model.append("package " + packageName + ";");
            model.append(eol).append(eol);
        }

        // imports
        List<String> sortedImports = new ArrayList<String>(imports);
        Collections.sort(sortedImports);
        for (String imp : sortedImports) {
            model.append("import ").append(imp).append(';').append(eol);
        }
        model.append(eol);

        // @IQSchema
        if (annotateSchema && !isNullOrEmpty(schema)) {
            model.append('@').append(IQSchema.class.getSimpleName());
            model.append('(');
            AnnotationBuilder ap = new AnnotationBuilder();
            ap.addParameter(null, schema);
            model.append(ap);
            model.append(')').append(eol);
        }

        // @IQTable
        model.append('@').append(IQTable.class.getSimpleName());
        model.append('(');

        // IQTable annotation parameters
        AnnotationBuilder ap = new AnnotationBuilder();
        ap.addParameter("name", table);

        if (primaryKeys.size() > 1) {
            ap.addParameter("primaryKey", primaryKeys);
        }

        // finish @IQTable annotation
        model.append(ap);
        model.append(')').append(eol);

        // @IQIndexes
        // @IQIndex
        String indexAnnotations = generateIndexAnnotations();
        if (!StringUtils.isNullOrEmpty(indexAnnotations)) {
            model.append(indexAnnotations);
        }

        // class declaration
        String clazzName = ModelUtils.convertTableToClassName(table);
        model.append(format("public class {0} implements Serializable '{'", clazzName)).append(eol);
        model.append(eol);
        model.append("\tprivate static final long serialVersionUID = 1L;").append(eol);
        model.append(eol);

        // field declarations
        model.append(fields);

        // default constructor
        model.append("\t" + "public ").append(clazzName).append("() {").append(eol);
        model.append("\t}").append(eol);

        // end of class body
        model.append('}');
        model.trimToSize();
        return model.toString();
    }

    /**
     * Generates the specified index annotation.
     *
     * @param ap
     */
    String generateIndexAnnotations() {
        if (indexes == null || indexes.size() == 0) {
            // no matching indexes
            return null;
        }
        AnnotationBuilder ap = new AnnotationBuilder();
        if (indexes.size() == 1) {
            // single index
            IndexInspector index = indexes.values().toArray(new IndexInspector[1])[0];
            ap.append(generateIndexAnnotation(index));
            ap.append(eol);
        } else {
            // multiple indexes
            ap.append('@').append(IQIndexes.class.getSimpleName());
            ap.append("({");
            ap.resetCount();
            for (IndexInspector index : indexes.values()) {
                ap.appendExceptFirst(", ");
                ap.append(generateIndexAnnotation(index));
            }
            ap.append("})").append(eol);
        }
        return ap.toString();
    }

    private String generateIndexAnnotation(IndexInspector index) {
        AnnotationBuilder ap = new AnnotationBuilder();
        ap.append('@').append(IQIndex.class.getSimpleName());
        ap.append('(');
        ap.resetCount();
        if (!StringUtils.isNullOrEmpty(index.name)) {
            ap.addParameter("name", index.name);
        }
        if (!index.type.equals(IndexType.STANDARD)) {
            ap.addEnum("type", index.type);
        }
        if (ap.getCount() > 0) {
            // multiple fields specified
            ap.addParameter("value", index.columns);
        } else {
            // default value
            ap.addParameter(null, index.columns);
        }
        ap.append(')');
        return ap.toString();
    }

    private StatementBuilder generateColumn(Set<String> imports, ColumnInspector col, boolean trimStrings) {
        StatementBuilder sb = new StatementBuilder();
        Class<?> clazz = col.clazz;
        String column = ModelUtils.convertColumnToFieldName(col.name.toLowerCase());
        sb.append('\t');
        if (clazz == null) {
            // unsupported type
            clazz = Object.class;
            sb.append("// unsupported type " + col.type);
        } else {
            // Imports
            // don't import primitives, java.lang classes, or byte []
            if (clazz.getPackage() == null) {
            } else if (clazz.getPackage().getName().equals("java.lang")) {
            } else if (clazz.equals(byte[].class)) {
            } else {
                imports.add(clazz.getCanonicalName());
            }
            // @IQColumn
            sb.append('@').append(IQColumn.class.getSimpleName());

            // IQColumn annotation parameters
            AnnotationBuilder ap = new AnnotationBuilder();

            // IQColumn.name
            if (!col.name.equalsIgnoreCase(column)) {
                ap.addParameter("name", col.name);
            }

            // IQColumn.primaryKey
            // composite primary keys are annotated on the table
            if (col.isPrimaryKey && primaryKeys.size() == 1) {
                ap.addParameter("primaryKey=true");
            }

            // IQColumn.length
            if ((clazz == String.class) && (col.size > 0) && (col.size < Integer.MAX_VALUE)) {
                ap.addParameter("length", col.size);

                // IQColumn.trim
                if (trimStrings) {
                    ap.addParameter("trim=true");
                }
            } else {
                // IQColumn.AutoIncrement
                if (col.isAutoIncrement) {
                    ap.addParameter("autoIncrement=true");
                }
            }

            // IQColumn.nullable
            if (!col.nullable) {
                ap.addParameter("nullable=false");
            }

            // IQColumn.defaultValue
            if (!isNullOrEmpty(col.defaultValue)) {
                ap.addParameter("defaultValue=\"" + col.defaultValue + "\"");
            }

            // add leading and trailing ()
            if (ap.length() > 0) {
                ap.insert(0, '(');
                ap.append(')');
            }
            sb.append(ap);
        }
        sb.append(eol);

        // variable declaration
        sb.append("\t" + "public ");
        sb.append(clazz.getSimpleName());
        sb.append(' ');
        sb.append(column);
        sb.append(';');
        sb.append(eol).append(eol);
        return sb;
    }

    /**
     * Validates that a table definition (annotated, interface, or both) matches
     * the current state of the table and indexes in the database. Results are
     * returned as a list of validation remarks which includes recommendations,
     * warnings, and errors about the model. The caller may choose to have
     * validate throw an exception on any validation ERROR.
     *
     * @param def        the table definition
     * @param throwError whether or not to throw an exception if an error was found
     * @return a list if validation remarks
     */
    <T> List<ValidationRemark> validate(TableDefinition<T> def, boolean throwError) {
        List<ValidationRemark> remarks = Utils.newArrayList();

        // model class definition validation
        if (!Modifier.isPublic(def.getModelClass().getModifiers())) {
            remarks.add(error(table, "SCHEMA",
                    format("Class {0} MUST BE PUBLIC!", def.getModelClass().getCanonicalName())).throwError(
                    throwError));
        }

        // Schema Validation
        if (!isNullOrEmpty(schema)) {
            if (isNullOrEmpty(def.schemaName)) {
                remarks.add(consider(table, "SCHEMA",
                        format("@{0}(\"{1}\")", IQSchema.class.getSimpleName(), schema)));
            } else if (!schema.equalsIgnoreCase(def.schemaName)) {
                remarks.add(error(
                        table,
                        "SCHEMA",
                        format("@{0}(\"{1}\") != {2}", IQSchema.class.getSimpleName(), def.schemaName, schema))
                        .throwError(throwError));
            }
        }

        // index validation
        for (IndexInspector index : indexes.values()) {
            validate(remarks, def, index, throwError);
        }

        // field column validation
        for (FieldDefinition fieldDef : def.getFields()) {
            validate(remarks, fieldDef, throwError);
        }
        return remarks;
    }

    /**
     * Validates an inspected index from the database against the
     * IndexDefinition within the TableDefinition.
     */
    private <T> void validate(List<ValidationRemark> remarks, TableDefinition<T> def, IndexInspector index,
                              boolean throwError) {
        List<IndexDefinition> defIndexes = def.getIndexes();
        if (defIndexes.size() > indexes.size()) {
            remarks.add(warn(table, IndexType.STANDARD.name(), "More model indexes  than database indexes"));
        } else if (defIndexes.size() < indexes.size()) {
            remarks.add(warn(table, IndexType.STANDARD.name(), "Model class is missing indexes"));
        }
        // TODO complete index validation.
        // need to actually compare index types and columns within each index.

        // TODO add constraints validation
        List<ConstraintUniqueDefinition> defContraintsU = def.getContraintsUnique();
        List<ConstraintForeignKeyDefinition> defContraintsFK = def.getContraintsForeignKey();
    }

    /**
     * Validates a column against the model's field definition. Checks for
     * existence, supported type, type mapping, default value, defined lengths,
     * primary key, autoincrement.
     */
    private void validate(List<ValidationRemark> remarks, FieldDefinition fieldDef, boolean throwError) {
        // unknown field
        if (!columns.containsKey(fieldDef.columnName.toLowerCase())) {
            // unknown column mapping
            remarks.add(error(table, fieldDef, "Does not exist in database!").throwError(throwError));
            return;
        }
        ColumnInspector col = columns.get(fieldDef.columnName.toLowerCase());
        Class<?> fieldClass = fieldDef.field.getType();
        Class<?> jdbcClass = ModelUtils.getClassForSqlType(col.type, dateTimeClass);

        // supported type check
        // iciql maps to VARCHAR for unsupported types.
        if (fieldDef.dataType.equals("VARCHAR") && (fieldClass != String.class)) {
            remarks.add(error(table, fieldDef,
                    "iciql does not currently implement support for " + fieldClass.getName()).throwError(
                    throwError));
        }
        // number types
        if (!fieldClass.equals(jdbcClass)) {
            if (Number.class.isAssignableFrom(fieldClass)) {
                remarks.add(warn(
                        table,
                        col,
                        format("Precision mismatch: ModelObject={0}, ColumnObject={1}",
                                fieldClass.getSimpleName(), jdbcClass.getSimpleName())));
            } else {
                if (!Date.class.isAssignableFrom(jdbcClass)) {
                    remarks.add(warn(
                            table,
                            col,
                            format("Object Mismatch: ModelObject={0}, ColumnObject={1}",
                                    fieldClass.getSimpleName(), jdbcClass.getSimpleName())));
                }
            }
        }

        // string types
        if (fieldClass == String.class) {
            if ((fieldDef.length != col.size) && (col.size < Integer.MAX_VALUE)) {
                remarks.add(warn(
                        table,
                        col,
                        format("{0}.length={1}, ColumnMaxLength={2}", IQColumn.class.getSimpleName(),
                                fieldDef.length, col.size)));
            }
            if (fieldDef.length > 0 && !fieldDef.trim) {
                remarks.add(consider(table, col, format("{0}.trim=true will prevent IciqlExceptions on"
                        + " INSERT or UPDATE, but will clip data!", IQColumn.class.getSimpleName())));
            }
        }

        // numeric autoIncrement
        if (fieldDef.isAutoIncrement != col.isAutoIncrement) {
            remarks.add(warn(
                    table,
                    col,
                    format("{0}.autoIncrement={1}" + " while Column autoIncrement={2}",
                            IQColumn.class.getSimpleName(), fieldDef.isAutoIncrement, col.isAutoIncrement)));
        }
        // default value
        if (!col.isAutoIncrement && !col.isPrimaryKey) {
            String defaultValue = null;
            if (fieldDef.defaultValue != null && fieldDef.defaultValue instanceof String) {
                defaultValue = fieldDef.defaultValue.toString();
            }
            // check Model.defaultValue format
            if (!ModelUtils.isProperlyFormattedDefaultValue(defaultValue)) {
                remarks.add(error(
                        table,
                        col,
                        format("{0}.defaultValue=\"{1}\"" + " is improperly formatted!",
                                IQColumn.class.getSimpleName(), defaultValue)).throwError(throwError));
                // next field
                return;
            }
            // compare Model.defaultValue to Column.defaultValue
            if (isNullOrEmpty(defaultValue) && !isNullOrEmpty(col.defaultValue)) {
                // Model.defaultValue is NULL, Column.defaultValue is NOT NULL
                remarks.add(warn(
                        table,
                        col,
                        format("{0}.defaultValue=\"\"" + " while column default=\"{1}\"",
                                IQColumn.class.getSimpleName(), col.defaultValue)));
            } else if (!isNullOrEmpty(defaultValue) && isNullOrEmpty(col.defaultValue)) {
                // Column.defaultValue is NULL, Model.defaultValue is NOT NULL
                remarks.add(warn(
                        table,
                        col,
                        format("{0}.defaultValue=\"{1}\"" + " while column default=\"\"",
                                IQColumn.class.getSimpleName(), defaultValue)));
            } else if (!isNullOrEmpty(defaultValue) && !isNullOrEmpty(col.defaultValue)) {
                if (!defaultValue.equals(col.defaultValue)) {
                    // Model.defaultValue != Column.defaultValue
                    remarks.add(warn(
                            table,
                            col,
                            format("{0}.defaultValue=\"{1}\"" + " while column default=\"{2}\"",
                                    IQColumn.class.getSimpleName(), defaultValue, col.defaultValue)));
                }
            }

            // sanity check Model.defaultValue literal value
            if (!ModelUtils.isValidDefaultValue(fieldDef.field.getType(), defaultValue)) {
                remarks.add(error(
                        table,
                        col,
                        format("{0}.defaultValue=\"{1}\" is invalid!", IQColumn.class.getSimpleName(),
                                defaultValue)));
            }
        }
    }

    /**
     * Represents an index as it exists in the database.
     */
    private static class IndexInspector {

        String name;
        IndexType type;
        private List<String> columns = new ArrayList<String>();

        public IndexInspector(ResultSet rs) throws SQLException {
            name = rs.getString("INDEX_NAME");

            // determine index type
            boolean hash = rs.getInt("TYPE") == DatabaseMetaData.tableIndexHashed;
            boolean unique = !rs.getBoolean("NON_UNIQUE");

            if (!hash && !unique) {
                type = IndexType.STANDARD;
            } else if (hash && unique) {
                type = IndexType.UNIQUE_HASH;
            } else if (unique) {
                type = IndexType.UNIQUE;
            } else if (hash) {
                type = IndexType.HASH;
            }
            columns.add(rs.getString("COLUMN_NAME"));
        }

        public void addColumn(ResultSet rs) throws SQLException {
            columns.add(rs.getString("COLUMN_NAME"));
        }
    }

    /**
     * Represents a column as it exists in the database.
     */
    static class ColumnInspector implements Comparable<ColumnInspector> {
        String name;
        String type;
        int size;
        boolean nullable;
        Class<?> clazz;
        boolean isPrimaryKey;
        boolean isAutoIncrement;
        String defaultValue;

        public int compareTo(ColumnInspector o) {
            if (isPrimaryKey && o.isPrimaryKey) {
                // both primary sort by name
                return name.compareTo(o.name);
            } else if (isPrimaryKey && !o.isPrimaryKey) {
                // primary first
                return -1;
            } else if (!isPrimaryKey && o.isPrimaryKey) {
                // primary first
                return 1;
            } else {
                // neither primary, sort by name
                return name.compareTo(o.name);
            }
        }
    }

    /**
     * Convenience class based on StatementBuilder for creating the annotation
     * parameter list.
     */
    private static class AnnotationBuilder extends StatementBuilder {

        AnnotationBuilder() {
            super();
        }

        void addParameter(String parameter) {

            appendExceptFirst(", ");
            append(parameter);
        }

        <T> void addParameter(String parameter, T value) {
            appendExceptFirst(", ");
            if (!StringUtils.isNullOrEmpty(parameter)) {
                append(parameter);
                append('=');
            }
            if (value instanceof List) {
                append("{ ");
                List<?> list = (List<?>) value;
                StatementBuilder flat = new StatementBuilder();
                for (Object o : list) {
                    flat.appendExceptFirst(", ");
                    if (o instanceof String) {
                        flat.append('\"');
                    }
                    // TODO escape string
                    flat.append(o.toString().trim());
                    if (o instanceof String) {
                        flat.append('\"');
                    }
                }
                append(flat);
                append(" }");
            } else {
                if (value instanceof String) {
                    append('\"');
                }
                // TODO escape
                append(value.toString().trim());
                if (value instanceof String) {
                    append('\"');
                }
            }
        }

        void addEnum(String parameter, Enum<?> value) {
            appendExceptFirst(", ");
            if (!StringUtils.isNullOrEmpty(parameter)) {
                append(parameter);
                append('=');
            }
            append(value.getClass().getSimpleName() + "." + value.name());
        }
    }
}
