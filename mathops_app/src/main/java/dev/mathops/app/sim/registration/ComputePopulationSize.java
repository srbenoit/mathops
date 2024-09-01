package dev.mathops.app.sim.registration;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
     * @param rooms               the set of available rooms
     * @return the maximum population size
     */
    static int compute(final Collection<Course> courses, final StudentDistribution studentDistribution,
                       final Rooms rooms) {

        int pop = 0;

        boolean solutionFound;

        final HtmlBuilder builder = new HtmlBuilder(50);

        do {
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
            for (final Course course : courses) {
                final int count = course.getNumSeatsNeeded();
                if (count > 0) {
                    final String countStr = Integer.toString(count);
                    Log.info("    ", course.courseId, " requires ", countStr, " seats");
                }
            }

            solutionFound = ComputeSectionRoomAssignments.canCompute(courses, rooms);

            if (solutionFound) {
                Log.info("Success!");

                for (final Room room : rooms.getRooms()) {
                    final String roomId = room.getId();
                    Log.fine("Room: ", roomId);
                    for (final RoomAssignment assignment : room.getAssignments()) {
                        Log.fine("    ", assignment);
                    }
                    final int[][] block = room.getTimeBlockGrid();
                    final int[] free = room.getBlocksFree();

                    final int numDays = block.length;
                    for (int i = 0; i < numDays; ++i) {
                        builder.reset();
                        builder.add("    Day ");
                        builder.add(i + 1);
                        builder.add(": ");
                        for (final int id : block[i]) {
                            if (id == 0) {
                                builder.add('.');
                            } else {
                                builder.add((char)('A' + id));
                            }
                        }
                        builder.add(" with ");
                        builder.add(free[i]);
                        builder.add(" blocks free");
                        Log.fine(builder.toString());
                    }

                    Log.fine(CoreConstants.EMPTY);
                }

                for (final Course course : courses) {
                    int total = 0;

                    final Set<ERoomUsage> usages = course.getUsages();
                    for (final ERoomUsage usage : usages) {
                        final List<RoomAssignment> assignments = course.getRoomAssignments(usage);
                        if (assignments != null) {
                            final int numSections = assignments.size();
                            total += numSections;
                        }
                    }

                    if (total > 0) {
                        final String numSectionsStr = Integer.toString(total);
                        final int numUsages = usages.size();

                        if (numUsages == 1) {
                            Log.info("    ", course.courseId, " has  ", numSectionsStr, " total sections of 1 type");
                        } else {
                            final String numUsagesStr = Integer.toString(numUsages);
                            Log.info("    ", course.courseId, " has  ", numSectionsStr, " total sections of ",
                                    numUsagesStr, " types");
                        }
                    }
                }
            }
        } while (solutionFound);

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
