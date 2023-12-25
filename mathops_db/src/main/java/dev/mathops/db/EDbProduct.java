package dev.mathops.db;

/**
 * Enumeration of supported database products.
 */
public enum EDbProduct {

    /** The Informix database. */
    INFORMIX("Informix"),

    /** The Oracle database. */
    ORACLE("Oracle"),

    /** The PostgreSQL database. */
    POSTGRESQL("PostgreSQL"),

    /** The MySQL database. */
    MYSQL("MySQL"),

    /** The Apache Cassandra database. */
    CASSANDRA("Cassandra");

    /** The type name. */
    public final String name;

    /**
     * Constructs a new {@code EDbProduct}.
     *
     * @param theName the type name
     */
    EDbProduct(final String theName) {

        this.name = theName;
    }

    /**
     * Generates a string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.name;
    }

    /**
     * Returns the {@code EDbProduct} that corresponds to a name. Name comparison is case-insensitive.
     *
     * @param name the name
     * @return the matching {@code EDbProduct}; {@code null} if none matches
     */
    public static EDbProduct forName(final String name) {

        EDbProduct result = null;

        for (final EDbProduct value : values()) {
            if (value.name.equalsIgnoreCase(name)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
