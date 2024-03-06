package dev.mathops.app.ops.snapin.canvas.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A term in which a course can exist.
 */
public final class CourseTerm {

    /** The term ID. */
    public final String termId;

    /** The list of all courses in the term. */
    public final List<Course> courses;

    /**
     * Constructs a new {@code CourseTerm}.
     *
     * @param theTermId the term ID, like "2018FA".
     */
    public CourseTerm(final String theTermId) {

        this.termId = theTermId;
        this.courses = new ArrayList<>(20);
    }
}
