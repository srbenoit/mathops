package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.campus.Room;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Given a student population with preferences and a list of course section offered (with times), simulates the
 * registration process some number of times and computes an average "quality factor" for the schedule and highlights
 * classes that filled and prevented students from enrolling, or where students wanted to enroll in a class, but it
 * conflicted with another course they already had.  Each course gets a "hotspot" rating.
 *
 * <p>
 * Then, the system considers pair-wise swaps of the "hot-spot" courses to other positions in the schedule and
 * recomputes the "quality factor" after each such swap.
 */
enum ComputeSectionLayoutQuality {
    ;

    /** The number of attempts over which to average. */
    private static final int ATTEMPTS = 100;

    /**
     * Calculates the largest population size that can be accommodated for a given set of course offerings, a given set
     * of classrooms and labs, and a given student distribution.  In the process, the number of seats needed in each
     * offered course is computed and stored.
     *
     * @param courses    the list of courses offered
     * @param population the student population
     */
    static void compute(final Collection<Course> courses, final StudentPopulation population,
                        final List<Room> rooms) {

        // Generate a randomly ordered list of student preferences

        final List<StudentClassPreferences> unordered = new ArrayList<>(200);
        for (final Map.Entry<StudentClassPreferences, Integer> entry : population.getCounts().entrySet()) {
            final StudentClassPreferences prefs = entry.getKey();
            final int count = entry.getValue().intValue();

            for (int i = 0; i < count; ++i) {
                unordered.add(prefs);
            }
        }

        final long seed = System.currentTimeMillis() + System.nanoTime();
        final Random rnd = new Random(seed);

        int count = unordered.size();
        final List<StudentClassPreferences> randomized = new ArrayList<>(count);
        while (count > 0) {
            final int which = rnd.nextInt(count);
            randomized.add(unordered.remove(which));
            --count;
        }

        // Simulate the registration process for each student in turn
        for (final StudentClassPreferences prefs : randomized) {

            final List<Course> chosen = chooseCourses(rnd, courses, prefs);

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
