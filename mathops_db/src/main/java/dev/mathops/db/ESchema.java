package dev.mathops.db;

import java.util.Locale;

/**
 * The types of schemas in the PostgreSQL database.
 */
public enum ESchema {

    /** The system schema. */
    SYSTEM,

    /** The main schema. */
    MAIN,

    /** The external data schema. */
    EXTERN,

    /** The analytics schema. */
    ANALYTICS,

    /** The term schema (a separate schema exists for each term). */
    TERM,

    /** The legacy schema. */
    LEGACY,

    /** The live schema. */
    LIVE,

    /** The ODS schema. */
    ODS;

    /**
     * Finds the {@code ESchema} with a particular name.
     *
     * @param name the name
     * @return the corresponding {@code ESchema}; {@code null} if none corresponds to the name
     */
    public static ESchema forName(final String name) {

        final String uppercase = name.toUpperCase(Locale.ROOT);

        ESchema result = null;

        for (final ESchema value : values()) {
            final String valueName = value.name();
            if (valueName.equals(uppercase)) {
                result = value;
                break;
            }
        }

        return result;
    }
}
