package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.rooms.ERoomUsage;
import dev.mathops.app.sim.rooms.Room;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.app.sim.students.StudentDistribution;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Determines the maximum population size that can be accommodated in a set of available classrooms and labs
 */
enum ComputePopulationSize {
    ;

    /** The number of attempts to make per population size. */
    private static final int ATTEMPTS_PER_POP_SIZE = 200;

    /**
     * Calculates the largest population size that can be accommodated for a given set of course offerings, a given set
     * of classrooms and labs, and a given student distribution.  In the process, the number of seats needed in each
     * offered course is computed and stored.
     *
     * @param courses             the list of courses offered
     * @param studentDistribution the student distribution
     * @param rooms               the set of available rooms
     * @param minPopulation       the minimum population to simulate
     * @param maxPopulation       the maximum population to simulate
     * @return the content of a CSV file with a report
     */
    static String compute(final List<Course> courses, final StudentDistribution studentDistribution,
                          final List<Room> rooms, final int minPopulation, final int maxPopulation) {

        final int numCourses = courses.size();
        final int numPopulations = maxPopulation - minPopulation + 1;

        // Data tracking (for each population size, and then for each course) - this will generate CSV file.

        final int[] populations = new int[numPopulations];

        final float[] percentSuccessful = new float[numPopulations];
        final float[] totalClassSections = new float[numPopulations];
        final float[] totalLabSections = new float[numPopulations];
        final float[] totalRecitationSections = new float[numPopulations];

        final float[][] averageClassSections = new float[numCourses][numPopulations];
        final float[][] averageLabSections = new float[numCourses][numPopulations];
        final float[][] averageRecitationSections = new float[numCourses][numPopulations];

        final int[] classSectionCount = new int[numCourses];
        final int[] labSectionCount = new int[numCourses];
        final int[] recitationSectionCount = new int[numCourses];

        for (int pop = minPopulation; pop <= maxPopulation; ++pop) {
            final int arrayIndex = pop - minPopulation;
            populations[arrayIndex] = pop;

            Arrays.fill(classSectionCount, 0);
            Arrays.fill(labSectionCount, 0);
            Arrays.fill(recitationSectionCount, 0);

            int numSuccess = 0;
            for (int attempt = 0; attempt < ATTEMPTS_PER_POP_SIZE; ++attempt) {

                final StudentPopulation population = new StudentPopulation(studentDistribution, pop);
                simulateRegistrations(courses, population);

                final Map<Course, List<AbstractSection>> result = ComputeSectionRoomAssignments.compute(courses, rooms);

                if (!result.isEmpty()) {
                    for (int i = 0; i < numCourses; ++i) {
                        final Course course = courses.get(i);
                        final List<AbstractSection> list = result.get(course);

                        if (Objects.nonNull(list)) {
                            for (final AbstractSection sect : list) {
                                final ERoomUsage usage = sect.usage();
                                if (usage == ERoomUsage.CLASSROOM) {
                                    ++classSectionCount[i];
                                } else if (usage == ERoomUsage.LAB) {
                                    ++labSectionCount[i];
                                } else if (usage == ERoomUsage.RECITATION) {
                                    ++recitationSectionCount[i];
                                }
                            }
                        }
                    }

                    ++numSuccess;
                }
            }

            percentSuccessful[arrayIndex] = (float) numSuccess * 100.0f / (float) ATTEMPTS_PER_POP_SIZE;

            final String popStr = Integer.toString(pop);
            final String percentageStr = Float.toString(percentSuccessful[arrayIndex]);
            Log.info("When population size is ", popStr, ", a schedule could be found ", percentageStr,
                    "% of the time");

            for (int i = 0; i < numCourses; ++i) {
                averageClassSections[i][arrayIndex] = (float) classSectionCount[i] / (float) numSuccess;
                averageLabSections[i][arrayIndex] = (float) labSectionCount[i] / (float) numSuccess;
                averageRecitationSections[i][arrayIndex] = (float) recitationSectionCount[i] / (float) numSuccess;

                totalClassSections[arrayIndex] += averageClassSections[i][arrayIndex];
                totalLabSections[arrayIndex] += averageLabSections[i][arrayIndex];
                totalRecitationSections[arrayIndex] += averageRecitationSections[i][arrayIndex];
            }
        }

        final HtmlBuilder csv = new HtmlBuilder(1000);
        final NumberFormat floatFormat = new DecimalFormat("0.00");

        csv.addln("SPUR Fall Hypothetical Population Model");
        csv.addln("(", ATTEMPTS_PER_POP_SIZE, " simulated registration cycles per population)");
        csv.addln();

        csv.addln("\"Each registration cycle allows N students to register,\"");
        csv.addln("\"selecting courses randomly with probability based on their\"");
        csv.addln("\"individual profiles, and then determines total sections of\"");
        csv.addln("\"each course needed, and tries to assign them to rooms.\"");
        csv.addln("\"A 'successful' cycle is one in which there was enough room\"");
        csv.addln("\"capacity for all sections requested by all students.\"");
        csv.addln();

        csv.add("Population:");
        for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
            csv.add(",", populations[arrayIndex]);
        }
        csv.addln();

        csv.add("Percentage successful outcomes:");
        for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
            csv.add(",", percentSuccessful[arrayIndex]);
        }
        csv.addln();

        for (int i = 0; i < numCourses; ++i) {
            final Course course = courses.get(i);

            float totalClass = 0.0f;
            float totalLab = 0.0f;
            float totalRecitation = 0.0f;
            for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
                totalClass += averageClassSections[i][arrayIndex];
                totalLab += averageLabSections[i][arrayIndex];
                totalRecitation += averageRecitationSections[i][arrayIndex];
            }
            if (totalClass > 0.01f) {
                csv.add("Average ", course.courseId, " Class sections:");
                for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
                    csv.add(",", floatFormat.format(averageClassSections[i][arrayIndex]));
                }
                csv.addln();
            }
            if (totalLab > 0.01f) {
                csv.add("Average ", course.courseId, " Lab sections:");
                for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
                    csv.add(",", floatFormat.format(averageLabSections[i][arrayIndex]));
                }
                csv.addln();
            }
            if (totalRecitation > 0.01f) {
                csv.add("Average ", course.courseId, " Recitation sections:");
                for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
                    csv.add(",", floatFormat.format(averageRecitationSections[i][arrayIndex]));
                }
                csv.addln();
            }
        }
        csv.addln();

        csv.add("Total Class sections:");
        for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
            csv.add(",", floatFormat.format(totalClassSections[arrayIndex]));
        }
        csv.addln();

        csv.add("Total Lab sections:");
        for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
            csv.add(",", floatFormat.format(totalLabSections[arrayIndex]));
        }
        csv.addln();

        csv.add("Total Recitation sections:");
        for (int arrayIndex = 0; arrayIndex < numPopulations; ++arrayIndex) {
            csv.add(",", floatFormat.format(totalRecitationSections[arrayIndex]));
        }
        csv.addln();

        return csv.toString();
    }

    /**
     * Simulates registrations with a specified student population.  After this method, the {@code OfferedCourse}
     * objects will have the number of seats required filled in, and there will be a list of student schedules, each
     * with a list of courses populated.
     *
     * @param courses    the set of offered courses
     * @param population the student population
     */
    private static void simulateRegistrations(final Collection<Course> courses, final StudentPopulation population) {

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
