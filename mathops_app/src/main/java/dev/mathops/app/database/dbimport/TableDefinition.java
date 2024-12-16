package dev.mathops.app.database.dbimport;

import dev.mathops.commons.builder.HtmlBuilder;

import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    int maxFieldNameLen;

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
    void addRow(final String rowLine, final int delimiter, final int lineNumber)
            throws IllegalArgumentException {

        final int numFields = this.fields.size();
        final Object[] values = new Object[numFields];

        final int lineLen = rowLine.length();
        int fieldStart = 0;
        int onField = 0;

        int pos = 0;
        for (pos = 0; pos < lineLen; ++pos) {
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
     * @param schemaName     the schema name
     * @param tablespaceName the tablespace name
     * @return the SQL statement
     */
    String makeCreateSql(final String schemaName, final String tablespaceName) {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.addln("CREATE TABLE ", schemaName, ".", this.tableName, " (");
        final int numFields = this.fields.size();
        if (numFields > 0) {
            builder.add("  ");
            final FieldDefinition first = this.fields.getFirst();
            first.appendCreateSql(builder, this.maxFieldNameLen);

            for (int i = 1; i < numFields; ++i) {
                builder.addln(",").add("  ");
                final FieldDefinition field = this.fields.get(i);
                field.appendCreateSql(builder, this.maxFieldNameLen);
            }
        }
        builder.addln().addln(") TABLESPACE ", tablespaceName, ";");

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

        builder.addln("INSERT INTO ", schemaName, ".", this.tableName);

        final int numFields = this.fields.size();
        if (numFields > 0) {
            final FieldDefinition first = this.fields.getFirst();
            builder.add("  (", first.fieldName);

            for (int i = 1; i < numFields; ++i) {
                final FieldDefinition field = this.fields.get(i);
                builder.add(",", field.fieldName);
            }
        }
        builder.addln(")").add("  VALUES (");
        if (numFields > 0) {
            builder.add("?");
            for (int i = 1; i < numFields; ++i) {
                builder.add(",?");
            }
        }
        builder.addln(");");

        return builder.toString();
    }
}
