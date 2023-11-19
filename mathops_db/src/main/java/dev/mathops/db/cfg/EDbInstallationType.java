package dev.mathops.db.cfg;

/**
 * Enumeration of supported JDBC server types.
 */
public enum EDbInstallationType {

    /** The Informix database. */
    INFORMIX("Informix"),

    /** The Oracle database. */
    ORACLE("Oracle"),

    /** The PostgreSQL database. */
    POSTGRESQL("PostgreSQL"),

    /** The MySQL database. */
    MYSQL("MySQL");

    /** The type name. */
    public final String name;

    /**
     * Constructs a new {@code EServerType}.
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
     * Returns the {@code EServerType} that corresponds to a name. Name comparison is case-insensitive.
     *
     * @param name the name
     * @return the matching {@code EServerType}; {@code null} if none matches
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
