package dev.mathops.db.generalized;

import dev.mathops.commons.builder.SimpleBuilder;

import java.util.Arrays;

/**
 * An immutable definition of a database table, which stores any number of records.  The table definition specifies an
 * ordered list of typed fields with possible constraints.
 */
public class Table {

    /** Valid characters to start a table name. */
    private static final String VALID_NAME_START = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /** Valid characters in table names. */
    private static final String VALID_NAME_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_0123456789";

    /** The schema name. */
    private final String schema;

    /** The table name. */
    private final String name;

    /** The ordered list of fields. */
    private final Field[] fields;

    /**
     * Constructs a new {@code Table}.
     *
     * @param theSchema the schema name
     * @param theName the table name
     * @param theFields the ordered list of fields
     */
    protected Table(final String theSchema, final String theName, final Field... theFields) {

        if (theSchema == null || theSchema.isBlank()) {
            throw new IllegalArgumentException("Schema name may not be null or blank");
        }
        if (theName == null || theName.isBlank()) {
            throw new IllegalArgumentException("Table name may not be null or blank");
        }

        final int len = theName.length();
        final char ch1 = theName.charAt(0);
        if (VALID_NAME_START.indexOf((int) ch1) == -1) {
            throw new IllegalArgumentException("Invalid character at start of table name.");
        }
        for (int i = 1; i < len; ++i) {
            final char ch2 = theName.charAt(i);
            if (VALID_NAME_CHARS.indexOf((int) ch2) == -1) {
                throw new IllegalArgumentException("Invalid character within table name.");
            }
        }

        if (theFields == null || theFields.length == 0) {
            throw new IllegalArgumentException("Field list may not be null or empty");
        }
        for (final Field test : theFields) {
            if (test == null) {
                throw new IllegalArgumentException("Field list may not contain null values");
            }
        }

        this.schema = theSchema;
        this.name = theName;
        this.fields = theFields.clone();
    }

    /**
     * Gets the schema name.
     *
     * @return the schema name
     */
    public final String getSchema() {

        return this.schema;
    }

    /**
     * Gets the table name.
     *
     * @return the table name
     */
    public final String getName() {

        return this.name;
    }

    /**
     * Gets the number of fields.
     *
     * @return the number of fields
     */
    public final int getNumFields() {

        return this.fields.length;
    }

    /**
     * Gets a specific field definition.
     *
     * @param index the zero-based index of the field
     * @return the field definition
     */
    public final Field getField(final int index) {

        return this.fields[index];
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public final String toString() {

        final String fieldsStr = Arrays.toString(this.fields);

        return SimpleBuilder.concat("Table{name='", this.name, "', fields=", fieldsStr, "}");
    }
}
