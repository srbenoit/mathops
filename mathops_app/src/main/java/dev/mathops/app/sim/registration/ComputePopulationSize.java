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
    static int compute(final Collection<OfferedCourse> courses, final StudentDistribution studentDistribution,
                       final List<AvailableClassroom> allClassrooms, final List<AvailableLab> allLabs) {

        int pop = 0;

        int hoursFree = Integer.MAX_VALUE;

        while (hoursFree >= 0) {
            ++pop;

            final StudentPopulation population = new StudentPopulation(studentDistribution, pop);

            final String popStr = Integer.toString(pop);
            Log.info("Attempting to modeled a population of ", popStr, " students.");
            final Map<StudentClassPreferences, Integer> counts = population.getCounts();
            for (final Map.Entry<StudentClassPreferences, Integer> entry : counts.entrySet()) {
                final Integer count = entry.getValue();
                final String name = entry.getKey().key;

                Log.info("    ", count, " students with ", name, " preferences");
            }

            final List<EnrollingStudent> students = simulateRegistrations(courses, population);
            Log.info("Registration process has been simulated:");
            for (final OfferedCourse course : courses) {
                final int count = course.getNumSeatsNeeded();
                if (count > 0) {
                    final String countStr = Integer.toString(count);
                    Log.info("    ", course.courseId, " requires ", countStr, " seats");
                }
            }

            hoursFree = ComputeSectionRoomAssignments.compute(courses, allClassrooms, allLabs);

            if (hoursFree >= 0) {
                Log.info("Success with " + hoursFree + " hours free");

                for (final OfferedCourse course : courses) {
                    final List<AssignedSection> classSections = course.getClassSections();
                    final int numClassSections = classSections.size();

                    final List<AssignedSection> labSections = course.getLabSections();
                    final int numLabSections = labSections.size();
                    final int total = numClassSections + numLabSections;

                    if (total > 0) {
                        final String numClassSectionsStr = Integer.toString(numClassSections);
                        final String numLabSectionsStr = Integer.toString(numLabSections);

                        if (numLabSections == 0) {
                            Log.info("    ", course.courseId, " has  ", numClassSectionsStr, " class sections.");
                        } else if (numClassSections == 0) {
                            Log.info("    ", course.courseId, " has  ", numLabSectionsStr, " lab sections.");
                        } else {
                            Log.info("    ", course.courseId, " has  ", numClassSectionsStr, " class sections and ",
                                    numLabSectionsStr, " lab sections.");
                        }
                    }
                }
            }
        }

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
    private static List<EnrollingStudent> simulateRegistrations(final Collection<OfferedCourse> courses,
                                                                final StudentPopulation population) {

        final List<EnrollingStudent> enrollingStudents = new ArrayList<>(200);

        // Generate hypothetical registrations and see how many seats are used in each course

        for (final OfferedCourse course : courses) {
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
    private static List<OfferedCourse> chooseCourses(final RandomGenerator rnd, final Collection<OfferedCourse> courses,
                                                     final StudentClassPreferences classPreferences) {

        final List<OfferedCourse> result = new ArrayList<>(5);

        // This outer loop lets us retry a stochastic process that could fail sometimes many (but not infinite) times.
        for (int i = 0; i < 100; ++i) {
            result.clear();

            int totalCredits = 0;

            // Add all mandatory courses first
            for (final OfferedCourse course : courses) {
                if (course.mandatory) {
                    result.add(course);
                    totalCredits += course.numCredits;
                }
            }

            // This inner loop attempts to choose a course we have not already chosen until we have reached a target
            // number of credits in the student's schedule

            for (int j = 0; j < 1000; ++j) {
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
                }
            }

            if (totalCredits < classPreferences.minCredits) {
                Log.warning("Unable to reach minimum desired credits");
            }
        }

        return result;
    }
}
