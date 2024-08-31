package dev.mathops.app.sim.registration;

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
class EnrollingStudent {

    /** The unique student ID. */
    final int id;

    /** The student's class preferences. */
    final StudentClassPreferences preferences;

    /** The list of courses for which the student has registered. */
    final List<Course> courses;

    /**
     * Constructs a new {@code EnrollingStudent}.
     *
     * @param theId          the student ID
     * @param thePreferences the student's class preferences
     * @param theCourses     the list of courses for which the student is enrolling
     */
    EnrollingStudent(final int theId, final StudentClassPreferences thePreferences,
                     final Collection<Course> theCourses) {

        this.id = theId;
        this.preferences = thePreferences;
        this.courses = new ArrayList<>(theCourses);
    }

}
