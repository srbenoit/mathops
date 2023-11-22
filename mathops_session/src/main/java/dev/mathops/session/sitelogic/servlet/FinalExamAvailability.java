package dev.mathops.session.sitelogic.servlet;

/**
 * A final exam's availability.
 */
public class FinalExamAvailability {

    /** The course ID. */
    final String course;

    /** The unit. */
    final Integer unit;

    /**
     * Constructs a new {@code FinalExamAvailability}.
     *
     * @param theCourse the course ID
     * @param theUnit   the unit
     */
    public FinalExamAvailability(final String theCourse, final Integer theUnit) {

        this.course = theCourse;
        this.unit = theUnit;
    }
}
