package dev.mathops.db.old.logic.mathplan;

/**
 * Possible methods by which the Math Placement requirement can be satisfied.
 */
public enum EHowSatisfiedPlacement {

    /** The Math Placement tool was completed. */
    MATH_PLACEMENT_COMPLETED("Math Placement Tool Completed"),

    /** Math placement requirement was satisfied by standardized test scores. */
    TEST_SCORES("Math Placement not needed based on standardized test scores"),

    /** Math placement requirement was satisfied by transfer credit. */
    TRANSFER_CREDIT("Math Placement not needed based on transfer/AP credit"),

    /** Math placement requirement was satisfied by course credit. */
    COURSE_CREDIT("Math Placement not needed based on course credit");

    /** The message. */
    public final String msg;

    /**
     * Constructs a new {@code EHowSatisfiedPlacement}.
     *
     * @param theMsg the message
     */
    EHowSatisfiedPlacement(final String theMsg) {

        this.msg = theMsg;
    }
}
