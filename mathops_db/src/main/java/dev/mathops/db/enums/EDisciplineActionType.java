package dev.mathops.db.enums;

/**
 * Types of disciplinary action that can be taken in response to a cheating incident.
 */
public enum EDisciplineActionType {

    /** Verbal reprimand. */
    VERBAL_REPRIMAND("01", "Verbal reprimand"),

    /** Deferred decision (to be replaced later). */
    DEFERRED_DECISION("02", "Deferred decision"),

    /** Deferred F grade. */
    DEFERRED_F_GRADE("03", "Deferred F grade"),

    /** Can never re-register for course. */
    CANT_REREGISTER("04", "Can never re-register for course"),

    /** Standard controlled testing. */
    CTRL_TESTING("05", "Standard controlled testing in course "),

    /** Controlled testing with hand-graded exams. */
    HAND_GRADE("06", "Controlled testing in course + hand-graded exams"),

    /** Controlled testing with zero in unit. */
    CTRL_TESTING_ZERO_UNIT("07", "Controlled testing in course  + zero for unit"),

    /** Controlled testing in all courses. */
    CTRL_TESTING_ALL("08", "Controlled testing in all courses"),

    /** Hand-graded exams in all courses. */
    HAND_GRADE_ALL("09", "Hand-graded exams in all courses"),

    /** F assigned in course. */
    F("10", "F assigned in course"),

    /** F in current term, controlled testing in course. */
    TERM_F_CTRL_TESTING("11", "F in current term, controlled testing in course"),

    /** F in current term, hand-grading in course. */
    TERM_F_HAND_GRADE("12", "F in current term, hand-graded exams in course"),

    /** F in current term, hand-grading in all courses. */
    TERM_F_HAND_GRADE_ALL("13", "F in current term, hand-graded exams in all courses"),

    /** F in current term, controlled testing in all courses. */
    TERN_F_CTRL_TESTING_ALL("14", "F in current term, controlled testing in all courses"),

    /** Zero for unit exam, no controlled testing. */
    ZERO_UNIT("15", "Zero for unit exam, but no controlled testing"),

    /** Second incident on record. */
    TWO_INCIDENTS("16", "Two incidents on record"),

    /** Other. */
    OTHER("17", "Other - describe in comments");

    /** The status code. */
    public final String code;

    /** The status label. */
    public final String label;

    /**
     * Constructs a new {@code EDisciplineActionType}.
     *
     * @param theCode  the status code
     * @param theLabel the status label
     */
    EDisciplineActionType(final String theCode, final String theLabel) {

        this.code = theCode;
        this.label = theLabel;
    }

    /**
     * Gets the {@code EDisciplineActionType} that has a specified status code.
     *
     * @param theCode the status code
     * @return the matching {@code EDisciplineActionType}; {@code null} if none match
     */
    public static EDisciplineActionType forCode(final String theCode) {

        EDisciplineActionType result = null;

        for (final EDisciplineActionType test : values()) {
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

        return this.label;
    }
}
