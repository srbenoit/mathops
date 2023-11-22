package dev.mathops.session.sitelogic.servlet;

/**
 * A unit exam's availability.
 */
public class UnitExamAvailability {

    /** The course ID. */
    final String course;

    /** The unit. */
    final Integer unit;

    /**
     * Constructs a new {@code UnitExamAvailability}.
     *
     * @param theCourse the course ID
     * @param theUnit   the unit
     */
    public UnitExamAvailability(final String theCourse, final Integer theUnit) {

        this.course = theCourse;
        this.unit = theUnit;
    }
}
