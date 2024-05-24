package dev.mathops.db.old.logic.mathplan.data;

/**
 * Possible statuses for a course.
 */
public enum ECourseStatus {

    /** Student completed course at CSU, earning the required grade. */
    COMPLETED_AT_CSU(true),

    /** Student completed course at CSU, but did not earn the required grade. */
    COMPLETED_AT_CSU_GRADE_TOO_LOW(false),

    /** Student received transfer credit, earning the required grade. */
    TRANSFER_CREDIT(true),

    /** Student received transfer credit, but did not earn the required grade. */
    TRANSFER_CREDIT_GRADE_TOO_LOW(false),

    /** Student earned credit for the course on the challenge exam. */
    MATH_CHALLENGE_EXAM(true),

    /** Student placed out of course through a placement activity. */
    PLACED_OUT(true),

    /** None of the above - course is still required. */
    NONE(false);

    /** Flag indicating this status satisfies the requirement. */
    public final boolean sufficient;

    /**
     * Constructs a new {@code ECourseStatus}.
     *
     * @param isSufficient {@code true} if this status satisfies the requirement
     */
    ECourseStatus(final boolean isSufficient) {

        this.sufficient = isSufficient;
    }

}
