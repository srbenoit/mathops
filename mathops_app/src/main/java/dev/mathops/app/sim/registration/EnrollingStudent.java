package dev.mathops.app.sim.registration;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.OfferedSection;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

    /** The number of credits this student is taking. */
    private final int numCredits;

    /** The student's perceived quality score. */
    private final double quality;

    /** The list of sections in which the student is enrolled. */
    private final List<OfferedSection> enrolledSections;

    /**
     * Constructs a new {@code EnrollingStudent}.
     *
     * @param thePreferences      the student's class preferences
     * @param theQuality          the quality score
     * @param theEnrolledSections the list of sections in which the student is enrolled
     */
    EnrollingStudent(final StudentClassPreferences thePreferences,
                     final Collection<OfferedSection> theEnrolledSections,
                     final double theQuality) {

        this.preferences = thePreferences;
        this.enrolledSections = new ArrayList<>(theEnrolledSections);

        final Collection<Course> courses = new HashSet<>(10);
        for (final OfferedSection sect : theEnrolledSections) {
            final Course course = sect.getOfferedCourse().getCourse();
            courses.add(course);
        }

        int count = 0;
        for (final Course course : courses) {
            count += course.numCredits;
        }
        this.numCredits = count;
        this.quality = theQuality;
    }

    /**
     * Gets the student's quality score.
     *
     * @return the quality score
     */
    double getQuality() {

        return this.quality;
    }

    /**
     * Generates a string representation of the object.
     *
     * @return the string representation
     */
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        final String numCreditsStr = Integer.toString(this.numCredits);

        builder.add("Student with ", this.preferences.key, " preferences in ", numCreditsStr, " credits {");
        boolean comma = false;
        for (final OfferedSection sect : this.enrolledSections) {

            if (comma) {
                builder.add(", ");
            }
            builder.add(sect);
            comma = true;
        }
        builder.add("}");

        return builder.toString();
    }
}
