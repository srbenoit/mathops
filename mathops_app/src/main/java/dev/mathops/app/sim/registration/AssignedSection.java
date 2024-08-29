package dev.mathops.app.sim.registration;

/**
 * A section of a course assigned to a classroom.  At the point the assignment is made, we have not yet determined
 * weekdays or hour blocks for the assignment - just the fact that a particular section of a course, with a defined
 * number of seats, will meet in a classroom or lab a specified number of hours per week.
 */
public final class AssignedSection {

    /** The course. */
    final OfferedCourse course;

    /** The number of seats used. */
    final int numSeats;

    /**
     * Constructs a new {@code AssignedSection}.
     *
     * @param theCourse   the course
     * @param theNumSeats the number of seats used
     */
    AssignedSection(final OfferedCourse theCourse, final int theNumSeats) {

        this.course = theCourse;
        this.numSeats = theNumSeats;
    }
}
