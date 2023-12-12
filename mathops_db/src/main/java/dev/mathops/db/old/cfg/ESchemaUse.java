package dev.mathops.db.old.cfg;

/**
 * Enumeration of supported schema uses.
 */
public enum ESchemaUse {

    /** Primary data schema. */
    PRIMARY("PRI"),

    /** Live data schema. */
    LIVE("LIVE"),

    /** Operational data store. */
    ODS("ODS");

    /** The use name. */
    public final String name;

    /**
     * Constructs a new {@code ESchemaUse}.
     *
     * @param theName the use name
     */
    ESchemaUse(final String theName) {

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
     * Finds the {@code ESchemaUse} with a particular name.
     *
     * @param theName the name
     * @return the corresponding {@code ESchemaUse}; {@code null} if none corresponds to the name
     */
    public static ESchemaUse forName(final String theName) {

        final ESchemaUse[] values = values();
        ESchemaUse result = null;

        for (final ESchemaUse value : values) {
            if (value.name.equals(theName)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
