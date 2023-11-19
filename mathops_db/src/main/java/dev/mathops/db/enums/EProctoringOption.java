package dev.mathops.db.enums;

/**
 * Possible proctoring options.
 */
public enum EProctoringOption {

    /** Department Testing Center. */
    DEPT_TEST_CENTER("DTC"),

    /** University Testing Center. */
    UNIV_TEST_CENTER("UTC"),

    /** Assistive Services Testing Center. */
    ASSIST_TEST_CENTER("ATC"),

    /** Distance Testing Centers. */
    DIST_TEST_CENTER("OTC"),

    /** Authorized (Human) Proctor. */
    HUMAN("HUM"),

    /** ProctorU - Student Pay. */
    PROCTOR_U_STUDENT("PUS"),

    /** ProctorU - University Pay. */
    PROCTOR_U_UNIV("PUU"),

    /** Department Online Proctoring. */
    RESPONDUS("RES"),

    /** Department Online Proctoring. */
    HONORLOCK("HON"),

    /** Department Online Proctoring. */
    DEPT_ONLINE("ONL");

    /** The status code. */
    private final String code;

    /**
     * Constructs a new {@code EProctoringOption}.
     *
     * @param theCode the status code
     */
    EProctoringOption(final String theCode) {

        this.code = theCode;
    }

//    /**
//     * Gets the {@code EProctoringOption} that has a specified status code.
//     *
//     * @param theCode the status code
//     * @return the matching {@code EProctoringOption}; {@code null} if none match
//     */
//     public static EProctoringOption forCode(final String theCode) {
//
//     EProctoringOption result = null;
//
//     for (EProctoringOption test : EProctoringOption.values()) {
//     if (test.code.equals(theCode)) {
//     result = test;
//     break;
//     }
//     }
//
//     return result;
//     }

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
