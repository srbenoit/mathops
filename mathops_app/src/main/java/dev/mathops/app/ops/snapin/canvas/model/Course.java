package dev.mathops.app.ops.snapin.canvas.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A course.
 */
public final class Course {

    /** The course ID. */
    public final long id;

    /** The course code. */
    public final String code;

    /** The course name. */
    public final String name;

    /** The course state (unpublished, available, completed, or deleted). */
    public final String state;

    /** The course start date (null for development course shells). */
    public final LocalDate startDate;

    /** The course end date (null for development course shells). */
    public final LocalDate endDate;

    /** The assignment groups that exist in the course. */
    public final List<AssignmentGroup> assignmentGroups;

    /**
     * Constructs a new {@code Course}.
     *
     * @param theId the course ID.
     * @param theCode the course code
     * @param theName the course name
     * @param theState the course state (unpublished, available, completed, or deleted)
     * @param theStartDate the start date
     * @param theEndDate the end date
     */
    public Course(final long theId, final String theCode, final String theName, final String theState,
                  final ZonedDateTime theStartDate, final ZonedDateTime theEndDate) {

        this.id = theId;
        this.code = theCode;
        this.name = theName;
        this.state = theState;
        this.startDate = theStartDate == null ? null : theStartDate.toLocalDate();
        this.endDate = theEndDate == null ? null : theEndDate.toLocalDate();
        this.assignmentGroups = new ArrayList<>(4);
    }

    /**
     * Tests whether this course's start/end dates indicate it is either an active course (whose end date is not before
     * a given date), or will start in the future.
     *
     * @param today the date to consider "today" when determining what constitutes a "future" date
     * @return true if this course is active now or in the future
     */
    public boolean isActiveOrFuture(final ChronoLocalDate today) {

        return this.startDate != null && (this.endDate == null || !this.endDate.isBefore(today));
    }
}
