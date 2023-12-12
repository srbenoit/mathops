package dev.mathops.db.old.cfg;

/**
 * Enumeration of supported database uses.
 */
public enum EDbUse {

    /** Production data. */
    PROD("PROD"),

    /** Development data. */
    DEV("DEV"),

    /** TEst data. */
    TEST("TEST"),

    /** Archive database. */
    ARCH("ARCH"),

    /** Live registration database. */
    LIVE("LIVE"),

    /** Operational data store. */
    ODS("ODS");

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
    /* default */
    static EDbUse forName(final String theName) {

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
