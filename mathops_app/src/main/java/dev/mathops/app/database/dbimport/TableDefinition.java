package dev.mathops.app.database.dbimport;

import java.util.ArrayList;
import java.util.List;

/**
 * A table definition, as interpreted from the SQL file of an export.
 */
class TableDefinition {

    /** The fields. */
    final List<FieldDefinition> fields;

    /**
     * Constructs a new {@code TableDefinition}.
     *
     * @param tableName  the table name
     * @param unloadFile the name of the unload file
     * @param numRows    the number of rows we expect to find in the unload file
     */
    TableDefinition(final String tableName, final String unloadFile, final int numRows) {

        this.fields = new ArrayList<>(10);
    }
}
