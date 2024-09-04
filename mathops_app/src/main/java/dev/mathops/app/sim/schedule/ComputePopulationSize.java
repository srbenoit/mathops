package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.registration.EnrollingStudent;
import dev.mathops.app.sim.rooms.Room;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.app.sim.students.StudentDistribution;
import dev.mathops.app.sim.students.StudentPopulation;
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

    /** The number of attempts to make per population size. */
    private static final int ATTEMPTS_PER_POP_SIZE = 100;

    /**
     * Calculates the largest population size that can be accommodated for a given set of course offerings, a given set
     * of classrooms and labs, and a given student distribution.  In the process, the number of seats needed in each
     * offered course is computed and stored.
     *
     * @param courses             the list of courses offered
     * @param studentDistribution the student distribution
     * @param rooms               the set of available rooms
     * @return the maximum population size
     */
    static int compute(final Collection<Course> courses, final StudentDistribution studentDistribution,
                       final List<Room> rooms) {

        int pop = 159;

        boolean solutionFound;

//        final HtmlBuilder builder = new HtmlBuilder(50);

        int numSuccess;

        do {
            ++pop;

            numSuccess = 0;
            for (int attempt = 0; attempt < ATTEMPTS_PER_POP_SIZE; ++attempt) {

                final StudentPopulation population = new StudentPopulation(studentDistribution, pop);
                final List<EnrollingStudent> students = simulateRegistrations(courses, population);

                solutionFound = ComputeSectionRoomAssignments.canCompute(courses, rooms);

                if (solutionFound) {
                    ++numSuccess;
                }
            }

            final float percentage = (float) numSuccess * 100.0f / (float) ATTEMPTS_PER_POP_SIZE;
            Log.info("When population size is ", pop, ", a schedule could be found ", percentage, "% of the time");

        } while (numSuccess > 0);

        --pop;

        return pop;
    }

    /**
     * Simulates registrations with a specified student population.  After this method, the {@code OfferedCourse}
     * objects will have the number of seats required filled in, and there will be a list of student schedules, each
     * with a list of courses populated.
     *
     * @param courses    the set of offered courses
     * @param population the student population
     * @return a list of enrolling students, each with a list of selected courses
     */
    private static List<EnrollingStudent> simulateRegistrations(final Collection<Course> courses,
                                                                final StudentPopulation population) {

        final List<EnrollingStudent> enrollingStudents = new ArrayList<>(200);

        // Generate hypothetical registrations and see how many seats are used in each course

        for (final Course course : courses) {
            course.resetNumSeatsNeeded();
        }

        final Map<StudentClassPreferences, Integer> counts = population.getCounts();

        final long seed = System.currentTimeMillis() + System.nanoTime();
        final RandomGenerator rnd = new Random(seed);

        int studentId = 1;

        for (final Map.Entry<StudentClassPreferences, Integer> entry : counts.entrySet()) {
            final StudentClassPreferences classPreferences = entry.getKey();
            final int count = entry.getValue().intValue();

            for (int i = 0; i < count; ++i) {
                final List<Course> coursesToTake = chooseCourses(rnd, courses, classPreferences);

                for (final Course course : coursesToTake) {
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
    private static List<Course> chooseCourses(final RandomGenerator rnd, final Collection<Course> courses,
                                              final StudentClassPreferences classPreferences) {

        final List<Course> result = new ArrayList<>(5);

        // This outer loop lets us retry a stochastic process that could fail sometimes many (but not infinite) times.
        for (int i = 0; i < 100; ++i) {
            result.clear();

            int totalCredits = 0;

            // Add all mandatory courses first
            for (final Course course : courses) {
                if (course.mandatory) {
                    result.add(course);
                    totalCredits += course.numCredits;
                }
            }

            // This inner loop attempts to choose a course we have not already chosen until we have reached a target
            // number of credits in the student's schedule

            for (int j = 0; j < 1000; ++j) {
                final Course selected = classPreferences.pick(rnd);

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
                }
            }

            if (totalCredits < classPreferences.minCredits) {
                Log.warning("Unable to reach minimum desired credits");
            }
        }

        return result;
    }
}
