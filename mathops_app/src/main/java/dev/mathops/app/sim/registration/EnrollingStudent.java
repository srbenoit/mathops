package dev.mathops.app.sim.registration;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.students.StudentClassPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A student who wants to enroll in classes.  Given the list of classes available in a semester, each student will have
 * some target number of credits, and some preference level for each class.  We can then simulate their registration
 * choices to optimize their preferences along with the convenience of their daily schedule or commute, gaps for lunch,
 * etc.
 *
 * <p>
 * Many students in a simulation could share the same class preferences, so we manage those preferences as a separate
 * object.
 */
public final class EnrollingStudent {

    /** The student's class preferences. */
    private final StudentClassPreferences preferences;

    /** The list of courses for which the student has registered. */
    private final List<Course> courses;

    /**
     * Constructs a new {@code EnrollingStudent}.
     *
     * @param thePreferences the student's class preferences
     * @param theCourses     the list of courses for which the student is enrolling
     */
    public EnrollingStudent(final StudentClassPreferences thePreferences,
                            final Collection<Course> theCourses) {

        this.preferences = thePreferences;
        this.courses = new ArrayList<>(theCourses);
    }

    /**
     * Tests whether the student is enrolled in a course.
     *
     * @param course the course
     * @return true if the student is enrolled
     */
    boolean hasCourse(final Course course) {

        return this.courses.contains(course);
    }
}
