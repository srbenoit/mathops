package dev.mathops.db.enums;

/**
 * Possible grade modes for courses in the University catalog.
 */
public enum EGradeMode {

    /** Traditional letter grades. */
    TRADITIONAL(1),

    /** Satisfactory/Unsatisfactory (S/U) grading only. */
    SU(2),

    /** Traditional or S/U at student's option. */
    STUDENT_OPTION(3),

    /** Traditional or S/U at instructor's option. */
    INSTRUCTOR_OPTION(4);

    /** The code. */
    private final int code;

    /**
     * Constructs a new {@code EGradeMode}.
     *
     * @param theCode the code
     */
    EGradeMode(final int theCode) {

        this.code = theCode;
    }

    /**
     * Gets the {@code EGradeMode} that has a specified status code.
     *
     * @param theCode the status code
     * @return the matching {@code EProctoringOption}; {@code null} if none match
     */
    public static EGradeMode forCode(final int theCode) {

        EGradeMode result = null;

        for (final EGradeMode test : values()) {
            if (test.code == theCode) {
                result = test;
                break;
            }
        }

        return result;
    }
}
