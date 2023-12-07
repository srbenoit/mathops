package dev.mathops.db;

/**
 * Enumeration of supported server installation types.
 */
public enum EDbInstallationType {

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
     * Constructs a new {@code EDbInstallationType}.
     *
     * @param theName the type name
     */
    EDbInstallationType(final String theName) {

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
     * Returns the {@code EDbInstallationType} that corresponds to a name. Name comparison is case-insensitive.
     *
     * @param name the name
     * @return the matching {@code EDbInstallationType}; {@code null} if none matches
     */
    public static EDbInstallationType forName(final String name) {

        EDbInstallationType result = null;

        for (final EDbInstallationType value : values()) {
            if (value.name.equalsIgnoreCase(name)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
