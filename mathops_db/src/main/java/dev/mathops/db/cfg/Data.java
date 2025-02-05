package dev.mathops.db.cfg;

import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;

/**
 * A "data" object from the database configuration file.
 */
public final class Data {

    /** A character that is valid in prefixes. */
    private static final int UNDERSCORE = (int) '_';

    /** The database that owns the data. */
    public final Database database;

    /** The data ID. */
    public final String id;

    /** The schema. */
    public final ESchema schema;

    /** The use. */
    public final EDbUse use;

    /** The prefix. */
    public final String prefix;

    /**
     * Constructs a new {@code Data}.
     *
     * @param theDatabase the database that owns the data
     * @param theId       the data ID
     * @param theSchema   the schema
     * @param theUse      the use
     * @param thePrefix   the prefix
     * @throws IllegalArgumentException if the ID, schema, or use is null
     */
    Data(final Database theDatabase, final String theId, final ESchema theSchema, final EDbUse theUse,
         final String thePrefix) {

        if (theDatabase == null || theId == null || theId.isBlank() || theSchema == null || theUse == null) {
            final String msg = Res.get(Res.DATA_NULL_DB_ID_SCHEMA_USE);
            throw new IllegalArgumentException(msg);
        }

        // "Prefix" will get injected into SQL - we need to make sure it's just a sequence of alphabetic and digit
        // characters or underscores (where the first character is alphabetic) and not arbitrary SQL code.
        if (thePrefix != null) {
            if (thePrefix.isBlank()) {
                final String msg = Res.get(Res.DATA_PREFIX_BLANK);
                throw new IllegalArgumentException(msg);
            }
            final int ch0 = (int) thePrefix.charAt(0);
            if (Character.isAlphabetic(ch0)) {
                final int len = thePrefix.length();
                for (int i = 1; i < len; ++i) {
                    final int chr = (int) thePrefix.charAt(i);
                    if (!(Character.isAlphabetic(chr) || Character.isDigit(chr) || chr == UNDERSCORE)) {
                        final String msg = Res.get(Res.DATA_BAD_PREFIX);
                        throw new IllegalArgumentException(msg);
                    }
                }
            } else {
                final String msg = Res.get(Res.DATA_BAD_PREFIX);
                throw new IllegalArgumentException(msg);
            }
        }

        this.database = theDatabase;
        this.id = theId;
        this.schema = theSchema;
        this.use = theUse;
        this.prefix = thePrefix;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.database.hashCode() + this.id.hashCode() + this.schema.hashCode() + this.use.hashCode();
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param o the other object
     * @return true if the objects are equal
     */
    public boolean equals(final Object o) {

        final boolean equal;

        if (o == this) {
            equal = true;
        } else if (o instanceof final Data data) {
            equal = this.schema == data.schema && this.use == data.use && this.id.equals(data.id)
                    && this.database.equals(data.database);
        } else {
            equal = false;
        }

        return equal;
    }
}
