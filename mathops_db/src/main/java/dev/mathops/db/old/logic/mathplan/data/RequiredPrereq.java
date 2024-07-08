package dev.mathops.db.old.logic.mathplan.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An immutable record of a prerequisite that is required to register for a course. This differs from the "prereq"
 * table, which stores all courses which can satisfy a prerequisite for a course (for example, a calculus course CAN
 * satisfy a prerequisite for an algebra course, but is not a required prerequisite).
 *
 * <p>
 * There may be multiple records for a single course, if there are multiple prerequisites. If so, the student must
 * satisfy ALL prerequisites in order to register for the course.
 *
 * <p>
 * A single prerequisite may allow multiple course options (like "M 159" or "M 160"). This is represented by multiple
 * entries in the {@code prereqCourses} list.
 */
public final class RequiredPrereq implements Serializable, Comparable<RequiredPrereq> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4268142442615952095L;

    /** The course. */
    private final String courseId;

    /** The prerequisite courses. */
    final List<String> prereqCourses;

    /** The prerequisite grades (null for courses with no grade requirement). */
    final List<Float> prereqGrades;

    /** True if courses may be taken concurrently. */
    final Boolean mayBeConcurrent;

    /**
     * Constructs a new {@code RequiredPrereq}.
     *
     * @param theCourseId        the course ID
     * @param theMayBeConcurrent the may-be-concurrent flag
     * @param requiredCourses    required prerequisite courses
     */
    public RequiredPrereq(final String theCourseId, final Boolean theMayBeConcurrent,
                          final String... requiredCourses) {

        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }

        this.prereqCourses = new ArrayList<>(2);
        this.prereqGrades = new ArrayList<>(2);

        this.courseId = theCourseId;
        this.mayBeConcurrent = theMayBeConcurrent;

        if (requiredCourses != null) {
            final List<String> list = Arrays.asList(requiredCourses);
            this.prereqCourses.addAll(list);
        }
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.courseId.hashCode() + Objects.hashCode(this.mayBeConcurrent)
                + Objects.hashCode(this.prereqCourses) + Objects.hashCode(this.prereqGrades);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RequiredPrereq prereq) {
            equal = this.courseId.equals(prereq.courseId)
                    && Objects.equals(this.mayBeConcurrent, prereq.mayBeConcurrent)
                    && Objects.equals(this.prereqCourses, prereq.prereqCourses)
                    && Objects.equals(this.prereqGrades, prereq.prereqGrades);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RequiredPrereq o) {

        return this.courseId.compareTo(o.courseId);
    }
}
