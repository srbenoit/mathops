package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * A simulation of registration for classes based on offered sections, a student population size, and student
 * preferences.  The result has a "desirability" rating based on how well student were able to register for courses they
 * wanted and the friendliness of their commuting, lunch, and class schedules.
 */
public class RegistrationSimulation {

    /** The list of offered sections. */
    private final List<OfferedSection> sections;

    /** The list of enrolling students. */
    private final List<EnrollingStudent> students;

    /**
     * Constructs a new {@code RegistrationSimulation}.
     */
    RegistrationSimulation() {

        this.sections = new ArrayList<>(20);
        this.students = new ArrayList<>(200);
    }

    /**
     * Adds sections.
     *
     * @param toAdd the sections to add
     */
    void addSections(final OfferedSection... toAdd) {

        if (toAdd != null) {
            for (final OfferedSection section : toAdd) {
                if (section != null) {
                    this.sections.add(section);
                }
            }
        }
    }

    /**
     * Retrieves a copy of this object's list of offered sections.
     *
     * @return a copy of the offered sections list
     */
    List<OfferedSection> getSections() {

        return new ArrayList<>(this.sections);
    }

    /**
     * Adds students.
     *
     * @param toAdd the students to add
     */
    void addStudents(final EnrollingStudent... toAdd) {

        if (toAdd != null) {
            for (final EnrollingStudent student : toAdd) {
                if (student != null) {
                    this.students.add(student);
                }
            }
        }
    }

    /**
     * Retrieves a copy of this object's list of enrolling student.
     *
     * @return a copy of the enrolling student list
     */
    List<EnrollingStudent> getStudents() {

        return new ArrayList<>(this.students);
    }

    /**
     * Runs the registration process.
     */
    public void runRegistration() {

        // Randomly order the students
        final long seed = System.currentTimeMillis() + System.nanoTime();
        final RandomGenerator rnd = new Random(seed);
        int count = this.students.size();
        final Collection<EnrollingStudent> randomized = new ArrayList<>(count);
        while (count > 0) {
            final int index = rnd.nextInt(count);
            final EnrollingStudent stu = this.students.remove(index);
            randomized.add(stu);
            --count;
        }

        // Allow each student to register, in turn
        for (final EnrollingStudent stu : randomized) {
            allowStudentToRegister(stu);
        }
    }

    /**
     * Runs the registration process for a single student.
     *
     * @param stu the student
     */
    private void allowStudentToRegister(final EnrollingStudent stu) {

        final StudentClassPreferences prefs = stu.preferences();

        // For every offered section (that is not already full), define a preference level
        final List<OfferedSection> availableSections = new ArrayList<>(20);
        final List<Double> preferences = new ArrayList<>(20);
        for (final OfferedSection sect : this.sections) {
            if (sect.isFull()) {
                continue;
            }

            final double prefLevel = prefs.getPreference(sect.courseId);
            if (prefLevel > 0.0) {
                availableSections.add(sect);
                final Double prefLevelObj = Double.valueOf(prefLevel);
                preferences.add(prefLevelObj);
            }
        }

        // TODO: Build all possible choices where a student could pick different sections of a course, take the one
        //  with the highest desirability.

    }
}
