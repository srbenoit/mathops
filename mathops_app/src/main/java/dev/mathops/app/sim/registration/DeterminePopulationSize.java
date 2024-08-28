package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Determines the maximum population size that can be accommodated in a set of available classrooms and labs
 */
enum DeterminePopulationSize {
    ;

    /**
     * Computes the largest population size that can be accommodated for a given set of course offerings, a given set of
     * classrooms and labs, and a given student distribution.  In the process, the number of seats needed in each
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

        int pop = 1;
        final StudentPopulation population = new StudentPopulation(studentDistribution, 1);
        int hoursFree = simulateRegistrations(courses, population, allClassrooms, allLabs);

        while (hoursFree >= 0) {
            ++pop;
            final StudentPopulation population2 = new StudentPopulation(studentDistribution, 1);
            hoursFree = simulateRegistrations(courses, population2, allClassrooms, allLabs);
        }

        --pop;

        return pop;
    }

    /**
     * Simulates registrations with a specified student population
     *
     * @param courses       the set of offered courses
     * @param population    the student population
     * @param allClassrooms the set of all available classrooms
     * @param allLabs       the set of all available labs
     * @return the smallest number of available hours for classrooms or labs (negative if classroom or lab space is
     *         over-booked)
     */
    private static int simulateRegistrations(final List<OfferedCourse> courses, final StudentPopulation population,
                                             final List<AvailableClassroom> allClassrooms,
                                             final List<AvailableLab> allLabs) {

        for (final OfferedCourse course : courses) {
            course.setNumSeatsNeeded(0);
        }

        final Map<StudentClassPreferences, Integer> counts = population.getCounts();

        for (final Map.Entry<StudentClassPreferences, Integer> entry : counts.entrySet()) {
            final StudentClassPreferences classPreferences = entry.getKey();
            final int count = entry.getValue().intValue();

            for (int i = 0; i < count; ++i) {
                final List<OfferedCourse> coursesToTake = chooseCourses(courses, classPreferences);
                for (final OfferedCourse course : coursesToTake) {
                    course.incrementNumSeatsNeeded();
                }
            }
        }

        // TODO: Count up total classroom and lab space needed to accommodate the given number of registrations

        return 0;
    }

    /**
     * Chooses (randomly) a list of courses using a given set of preferences.
     *
     * @param courses          the list of offered courses
     * @param classPreferences the class preferences
     * @return the list of courses the student registered for
     */
    private static List<OfferedCourse> chooseCourses(final List<OfferedCourse> courses,
                                                     final StudentClassPreferences classPreferences) {

        final List<OfferedCourse> result = new ArrayList<>(5);

        // TODO:

        return result;
    }
}
