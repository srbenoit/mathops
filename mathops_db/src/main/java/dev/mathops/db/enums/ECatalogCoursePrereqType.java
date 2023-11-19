package dev.mathops.db.enums;

/**
 * A type field for a catalog course prerequisite node.
 */
public enum ECatalogCoursePrereqType {

    /** Simple course specification, a leaf node in the prerequisite tree. */
    SIMPLE("S"),

    /** AND combination of sub-nodes. */
    AND("A"),

    /** OR combination of sub-nodes. */
    OR("O");

    /** The type code. */
    private final String code;

    /**
     * Constructs a new {@code ECatalogCoursePrereqType}.
     *
     * @param theCode the status code
     */
    ECatalogCoursePrereqType(final String theCode) {

        this.code = theCode;
    }

    /**
     * Gets the {@code EProctoringOption} that has a specified status code.
     *
     * @param theCode the status code
     * @return the matching {@code EProctoringOption}; {@code null} if none match
     */
    public static ECatalogCoursePrereqType forCode(final String theCode) {

        ECatalogCoursePrereqType result = null;

        for (final ECatalogCoursePrereqType test : values()) {
            if (test.code.equals(theCode)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Returns the string representation of the object.
     *
     * @return the string representation (the code)
     */
    @Override
    public String toString() {

        return this.code;
    }
}
