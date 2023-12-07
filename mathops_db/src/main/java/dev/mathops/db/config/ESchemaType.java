package dev.mathops.db.config;

/**
 * Enumeration of supported schema types.
 */
public enum ESchemaType {

    /** Primary data schema. */
    PRIMARY("primary"),

    /** Live data schema. */
    LIVE("live"),

    /** Operational data store. */
    STORE("store");

    /** The use name. */
    public final String name;

    /**
     * Constructs a new {@code ESchemaType}.
     *
     * @param theName the use name
     */
    ESchemaType(final String theName) {

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
     * Finds the {@code ESchemaType} with a particular name.
     *
     * @param theName the name
     * @return the corresponding {@code ESchemaType}; {@code null} if none corresponds to the name
     */
    public static ESchemaType forName(final String theName) {

        final ESchemaType[] values = values();
        ESchemaType result = null;

        for (final ESchemaType value : values) {
            if (value.name.equals(theName)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
