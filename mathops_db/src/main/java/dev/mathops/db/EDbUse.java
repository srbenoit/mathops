package dev.mathops.db;

import java.util.Locale;

/**
 * Enumeration of supported database uses.
 */
public enum EDbUse {

    /** Production data. */
    PROD,

    /** Development data. */
    DEV,

    /** TEst data. */
    TEST,

    /** Live registration database. */
    LIVE,

    /** Live registration database, development instance. */
    LIVE_DEV,

    /** Operational data store. */
    ODS,

    /** Operational data store, development instance. */
    ODS_DEV;

    /**
     * Finds the {@code EDbUse} with a particular name.
     *
     * @param name the name
     * @return the corresponding {@code EDbUse}; {@code null} if none corresponds to the name
     */
    public static EDbUse forName(final String name) {

        final String uppercase = name.toUpperCase(Locale.ROOT);

        EDbUse result = null;

        for (final EDbUse value : values()) {
            final String valueName = value.name();
            if (valueName.equals(uppercase)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
