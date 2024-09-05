package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.rooms.Room;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * For a fixed population size, this determines the number of sections needed of each course.
 */
public enum ComputeSectionsNeeded {
    ;

    /** The number of attempts over which to average. */
    private static final int ATTEMPTS = 500;

    /** A commonly used integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /**
     * Calculates the largest population size that can be accommodated for a given set of course offerings, a given set
     * of classrooms and labs, and a given student distribution.  In the process, the number of seats needed in each
     * offered course is computed and stored.
     *
     * @param courses    the list of courses offered
     * @param population the student population
     * @return a map from each course to the number of sections needed
     */
    static Map<Course, Integer> compute(final Collection<Course> courses, final StudentPopulation population,
                                        final List<Room> rooms) {

        final int numCourses = courses.size();

        final Map<Course, Integer> result = new HashMap<>(numCourses);

        for (final Course course : courses) {
            result.put(course, ZERO);
        }

        for (int attempt = 0; attempt < ATTEMPTS; ++attempt) {

            // The following will set the number of seats needed in each course
            simulateRegistrations(courses, population);

            for (final Course course : courses) {
                final int seats = course.getNumSeatsNeeded();
                if (seats > 0) {
                    final Integer current = result.get(course);
                    final int currentValue = current == null ? 0 : current.intValue();
                    final Integer updated = Integer.valueOf(currentValue + seats);
                    result.put(course, updated);
                }
            }
        }

        Log.info("Seat counts for each course:");
        for (final Course course : courses) {
            final Integer current = result.get(course);
            final int currentValue = current == null ? 0 : current.intValue();
            final int average = (currentValue + ATTEMPTS - 1) / ATTEMPTS;
            final Integer updated = Integer.valueOf(average);
            result.put(course, updated);
            course.setNumSeatsNeeded(average);

            Log.info("    ", course.courseId, ": ", updated);
        }

        if (ComputeSectionRoomAssignments.canCompute(courses, rooms)) {
            Log.info("Rooms have been assigned:");

            for (final Room room : rooms) {
                Log.info("Room: ", room);
                for (final SectionMWF sect : room.getSectionsMWF()) {
                    Log.info("        ", sect);
                }
                for (final SectionTR sect : room.getSectionsTR()) {
                    Log.info("        ", sect);
                }
            }
        }


        return result;
    }

    /**
     * Simulates registrations with a specified student population.  After this method, the {@code OfferedCourse}
     * objects will have the number of seats required filled in, and there will be a list of student schedules, each
     * with a list of courses populated.
     *
     * @param courses    the set of offered courses
     * @param population the student population
     */
    private static void simulateRegistrations(final Collection<Course> courses,
                                              final StudentPopulation population) {
        // Generate hypothetical registrations and see how many seats are used in each course

        for (final Course course : courses) {
            course.resetNumSeatsNeeded();
        }

        final Map<StudentClassPreferences, Integer> counts = population.getCounts();

        final long seed = System.currentTimeMillis() + System.nanoTime();
        final RandomGenerator rnd = new Random(seed);

        for (final Map.Entry<StudentClassPreferences, Integer> entry : counts.entrySet()) {
            final StudentClassPreferences classPreferences = entry.getKey();
            final int count = entry.getValue().intValue();

            for (int i = 0; i < count; ++i) {
                final List<Course> coursesToTake = chooseCourses(rnd, courses, classPreferences);

                for (final Course course : coursesToTake) {
                    course.incrementNumSeatsNeeded();
                }
            }
        }
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
