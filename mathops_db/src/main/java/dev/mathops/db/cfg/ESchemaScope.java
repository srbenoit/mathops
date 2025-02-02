package dev.mathops.db.cfg;

/**
 * A schema scope.
 */
public enum ESchemaScope {

    /** The "global" scope.  The schema is used for all terms. */
    GLOBAL,

    /**
     * The "term" scope.  If the schema prefix includes "YYYYTT", those tags are replaced by the year and term indicator
     * for a specific term (one schema per term).  If the prefix does not include this string, the schema is treated as
     * "global", serving all terms.
     */
    TERM;

    /**
     * Attempts to parse a string into a {@code ESchemaScope} object.
     *
     * @param str the string
     * @return the parsed {@code ESchemaScope}; null if none matches
     */
    public static ESchemaScope parse(final String str) {

        ESchemaScope result = null;

        if ("global".equalsIgnoreCase(str)) {
            result = GLOBAL;
        } else if ("term".equalsIgnoreCase(str)) {
            result = TERM;
        }

        return result;
    }
}
