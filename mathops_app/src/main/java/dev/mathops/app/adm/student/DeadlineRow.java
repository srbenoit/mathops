package dev.mathops.app.adm.student;

import java.time.LocalDate;

/**
 * The data for one row in the deadline table.
 */
class DeadlineRow {

    /** The order. */
    private final int order;

    /** The milestone. */
    private final int milestone;

    /** The course. */
    private final String course;

    /** The unit. */
    private final int unit;

    /** The type. */
    private final String type;

    /** The original deadline. */
    private final LocalDate origDeadline;

    /** The override deadline. */
    private final LocalDate overrideDeadline;

    /** The date completed. */
    private final LocalDate completed;

    /** True if on time, false if late. */
    private final boolean onTime;

    /**
     * Constructs a new {@code DeadlineRow}.
     *
     * @param theOrder            the order
     * @param theMilestone        the milestone
     * @param theCourse           the course
     * @param theUnit             the unit
     * @param theType             the type
     * @param theOrigDeadline     the original deadline date
     * @param theOverrideDeadline the override deadline date
     * @param theCompleted        the completed date
     * @param theOnTime           true if completed on-time
     */
    DeadlineRow(final int theOrder, final int theMilestone, final String theCourse,
                final int theUnit, final String theType, final LocalDate theOrigDeadline,
                final LocalDate theOverrideDeadline, final LocalDate theCompleted,
                final boolean theOnTime) {

        this.order = theOrder;
        this.milestone = theMilestone;
        this.course = theCourse;
        this.unit = theUnit;
        this.type = theType;
        this.origDeadline = theOrigDeadline;
        this.overrideDeadline = theOverrideDeadline;
        this.completed = theCompleted;
        this.onTime = theOnTime;
    }
}
