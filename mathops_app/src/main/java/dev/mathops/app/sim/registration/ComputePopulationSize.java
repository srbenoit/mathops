package dev.mathops.app.sim.registration;

import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Determines the maximum population size that can be accommodated in a set of available classrooms and labs
 */
enum ComputePopulationSize {
    ;

    /**
     * Calculates the largest population size that can be accommodated for a given set of course offerings, a given set
     * of classrooms and labs, and a given student distribution.  In the process, the number of seats needed in each
     * offered course is computed and stored.
     *
     * @param courses             the list of courses offered
     * @param studentDistribution the student distribution
     * @param allClassrooms       the list of available classrooms
     * @param allLabs             the list of available labs
     * @return the maximum population size
     */
    static int compute(final List<OfferedCourse> courses, final StudentDistribution studentDistribution,
                       final List<AvailableClassroom> allClassrooms, final List<AvailableLab> allLabs) {

        int pop = 0;

        int hoursFree = Integer.MAX_VALUE;

        while (hoursFree >= 0) {
            ++pop;
            final StudentPopulation population = new StudentPopulation(studentDistribution, 1);
            final List<EnrollingStudent> students = simulateRegistrations(courses, population, allClassrooms, allLabs);

        }

        --pop;

        return pop;
    }

    /**
     * Simulates registrations with a specified student population.  After this method, the {@code OfferedCourse}
     * objects will have the number of seats required filled in, and there will be a list of student schedules, each
     * with a list of courses populated.
     *
     * @param courses       the set of offered courses
     * @param population    the student population
     * @param allClassrooms the set of all available classrooms
     * @param allLabs       the set of all available labs
     * @return a list of enrolling students, each with a list of selected courses
     */
    private static List<EnrollingStudent> simulateRegistrations(final Collection<OfferedCourse> courses,
                                                                final StudentPopulation population,
                                                                final List<AvailableClassroom> allClassrooms,
                                                                final List<AvailableLab> allLabs) {

        final List<EnrollingStudent> enrollingStudents = new ArrayList<>(200);

        // Generate hypothetical registrations and see how many seats are used in each course

        for (final OfferedCourse course : courses) {
            course.setNumSeatsNeeded(0);
        }

        final Map<StudentClassPreferences, Integer> counts = population.getCounts();

        final long seed = System.currentTimeMillis() + System.nanoTime();
        final RandomGenerator rnd = new Random(seed);

        int studentId = 1;

        for (final Map.Entry<StudentClassPreferences, Integer> entry : counts.entrySet()) {
            final StudentClassPreferences classPreferences = entry.getKey();
            final int count = entry.getValue().intValue();

            for (int i = 0; i < count; ++i) {
                final List<OfferedCourse> coursesToTake = chooseCourses(rnd, courses, classPreferences);

                for (final OfferedCourse course : coursesToTake) {
                    course.incrementNumSeatsNeeded();
                }

                final EnrollingStudent student = new EnrollingStudent(studentId, classPreferences, coursesToTake);
                enrollingStudents.add(student);
                ++studentId;
            }
        }

        return enrollingStudents;
    }

    /**
     * Chooses (randomly) a list of courses using a given set of preferences.
     *
     * @param rnd              the random number generator
     * @param courses          the list of offered courses
     * @param classPreferences the class preferences
     * @return the list of courses the student registered for
     */
    private static List<OfferedCourse> chooseCourses(final RandomGenerator rnd,
                                                     final Collection<OfferedCourse> courses,
                                                     final StudentClassPreferences classPreferences) {

        final List<OfferedCourse> result = new ArrayList<>(5);

        int totalCredits = 0;

        int numTries = 0;
        for (int i = 0; i < 1000; ++i) {
            final OfferedCourse selected = classPreferences.pick(rnd);
            if (!result.contains(selected) && courses.contains(selected)) {

                final int credits = selected.numCredits;
                if (totalCredits + credits > classPreferences.maxCredits) {
                    break;
                }

                result.add(selected);
                totalCredits += credits;

                if (totalCredits >= classPreferences.minCredits) {
                    final int span = classPreferences.maxCredits - classPreferences.minCredits + 1;
                    final int delta = totalCredits - classPreferences.minCredits + 1;
                    if (rnd.nextInt(span) < delta) {
                        break;
                    }
                }
                numTries = 0;
            } else {
                ++numTries;
                if (numTries > 100) {
                    Log.warning("Unable to pick a course after 100 tries!");
                    break;
                }
            }
        }

        return result;
    }
}
