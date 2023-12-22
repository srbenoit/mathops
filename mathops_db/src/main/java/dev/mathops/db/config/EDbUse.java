package dev.mathops.db.config;

/**
 * Database usages, which can control which database or schema is used to locate tables.
 */
public enum EDbUse {

    /** Production. */
    PRODUCTION("prod"),

    /** Development. */
    DEVELOPMENT("dev"),

    /** Testing. */
    TESTING("test");

    /** The use name. */
    public final String name;

    /**
     * Constructs a new {@code EDbUse}.
     *
     * @param theName the use name
     */
    EDbUse(final String theName) {

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
     * Finds the {@code EDbUse} with a particular name.
     *
     * @param theName the name
     * @return the corresponding {@code EDbUse}; {@code null} if none corresponds to the name
     */
    public static EDbUse forName(final String theName) {

        final EDbUse[] values = values();
        EDbUse result = null;

        for (final EDbUse value : values) {
            if (value.name.equals(theName)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
