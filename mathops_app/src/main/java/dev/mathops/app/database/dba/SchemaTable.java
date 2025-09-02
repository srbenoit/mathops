package dev.mathops.app.database.dba;

import dev.mathops.db.schema.ESchema;

/** A record of a schema and a table in that schema. */
public record SchemaTable(ESchema schema, String table) {

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation (the table name)
     */
    public String toString() {

        return table();
    }
}
