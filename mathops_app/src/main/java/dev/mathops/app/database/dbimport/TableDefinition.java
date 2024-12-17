package dev.mathops.app.database.dbimport;

import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A table definition, as interpreted from the SQL file of an export.
 */
final class TableDefinition {

    /** The table name. */
    final String tableName;

    /** The unload file name. */
    final String unloadFile;

    /** The number of rows. */
    final int numRows;

    /** The fields. */
    final List<FieldDefinition> fields;

    /** The length of the longest field name. */
    private int maxFieldNameLen = 0;

    /** The table data (one entry for each row). */
    final List<Object[]> data;

    /**
     * Constructs a new {@code TableDefinition}.
     *
     * @param theTableName  the table name
     * @param theUnloadFile the name of the unload file
     * @param theNumRows    the number of rows we expect to find in the unload file
     */
    TableDefinition(final String theTableName, final String theUnloadFile, final int theNumRows) {

        this.tableName = theTableName;
        this.unloadFile = theUnloadFile;
        this.numRows = theNumRows;
        this.fields = new ArrayList<>(10);
        this.data = new ArrayList<>(theNumRows);
    }

    /**
     * Computes the length of the longest field name.
     */
    void computeLongestFieldName() {

        for (final FieldDefinition field : this.fields) {
            final int len = field.fieldName.length();
            this.maxFieldNameLen = Math.max(this.maxFieldNameLen, len);
        }
    }

    /**
     * Attempts to interpret one line from the unload file and store the row data
     *
     * @param rowLine    the line from the unload file to interpret
     * @param delimiter  the delimiter character
     * @param lineNumber the line number in the unload file, for error reporting
     * @throws IllegalArgumentException if the row line could not be interpreted
     */
    void addRow(final String rowLine, final int delimiter, final int lineNumber) throws IllegalArgumentException {

        final int numFields = this.fields.size();
        final Object[] values = new Object[numFields];

        final int lineLen = rowLine.length();
        int fieldStart = 0;
        int onField = 0;

        for (int pos = 0; pos < lineLen; ++pos) {
            if ((int) rowLine.charAt(pos) == delimiter) {
                if (onField == numFields) {
                    throw new IllegalArgumentException("Table has " + numFields + " field definitions, but line "
                                                       + lineNumber + " in '" + this.unloadFile + "' has extra values");
                }
                final String fieldStr = rowLine.substring(fieldStart, pos);
                try {
                    values[onField] = this.fields.get(onField).interpret(fieldStr);
                } catch (final IllegalArgumentException ex) {
                    throw new IllegalArgumentException("The string '" + fieldStr + "' in field " + (onField + 1)
                                                       + " in line " + lineNumber + " of '" + this.unloadFile
                                                       + "' cannot be interpreted", ex);
                }
                ++onField;
                fieldStart = pos + 1;
            }
        }

        if (onField < numFields) {
            throw new IllegalArgumentException("Table has " + numFields + " field definitions, but line " + lineNumber
                                               + " in '" + this.unloadFile + "' has only " + onField + " values");
        }

        this.data.add(values);
    }

    /**
     * Generates an SQL statement to create the table.
     *
     * @param schemaName the schema name
     * @return the SQL statement
     */
    String makeCreateSql(final String schemaName) {

        final HtmlBuilder builder = new HtmlBuilder(100);

        final int numFields = this.fields.size();
        final List<FieldDefinition> actualFields = new ArrayList<>(numFields);
        for (final FieldDefinition def : this.fields) {
            if ("desc".equals(def.fieldName)) {
                continue;
            }
            actualFields.add(def);
        }
        final int numActual = actualFields.size();

        builder.addln("CREATE TABLE ", schemaName, ".", this.tableName, " (");
        if (numActual > 0) {
            builder.add("  ");
            final FieldDefinition first = actualFields.getFirst();
            first.appendCreateSql(builder, this.maxFieldNameLen);

            for (int i = 1; i < numActual; ++i) {
                builder.addln(",").add("  ");
                final FieldDefinition field = actualFields.get(i);
                field.appendCreateSql(builder, this.maxFieldNameLen);
            }
        }
        builder.addln().addln(") TABLESPACE primary_ts;");

        return builder.toString();
    }

    /**
     * Generates SQL to create a prepared statement that can insert records into the table.
     *
     * @param schemaName the schema name
     * @return the SQL statement
     */
    String makeInsertPreparedStatementSql(final String schemaName) {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.addln("INSERT INTO ", schemaName, ".", this.tableName, " (");

        boolean comma1 = false;
        for (final FieldDefinition field : this.fields) {
            if ("desc".equals(field.fieldName)) {
                continue;
            }
            if (comma1) {
                builder.add(",");
            }
            builder.add(field.fieldName);
            comma1 = true;
        }
        builder.addln(")").add("  VALUES (");

        boolean comma2 = false;
        for (final FieldDefinition field : this.fields) {
            if ("desc".equals(field.fieldName)) {
                continue;
            }
            if (comma2) {
                builder.add(",");
            }
            builder.add("?");
            comma2 = true;
        }
        builder.addln(");");

        return builder.toString();
    }
}
