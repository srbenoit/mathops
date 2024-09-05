package dev.mathops.app.sim.registration;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.EMeetingDays;
import dev.mathops.app.sim.courses.OfferedCourse;
import dev.mathops.app.sim.courses.OfferedSection;
import dev.mathops.app.sim.courses.OfferedSectionMeetingTime;
import dev.mathops.app.sim.courses.SpurCourses;
import dev.mathops.app.sim.rooms.SpurRooms;
import dev.mathops.app.sim.students.SpurStudents;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.log.Log;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * A simulation of the Spur first-year Fall semester.
 */
final class SpurFirstYearFall {

    /** The population size. */
    private static final int POPULATION_SIZE = 160;

    /** A meeting time label. */
    private static final String CLASS = "Class";

    /** A meeting time label. */
    private static final String LAB = "Lab";

    /** A meeting time label. */
    private static final String RECITATION = "Recitation";

    /** A classroom ID. */
    private static final String CLASSROOM_1 = SpurRooms.CLASSROOM_1.getId();

    /** A classroom ID. */
    private static final String CLASSROOM_2 = SpurRooms.CLASSROOM_2.getId();

    /** A classroom ID. */
    private static final String LAB_1 = SpurRooms.LAB_1.getId();

    /**
     * Constructs a new {@code SpurFirstYearFall}.
     */
    private SpurFirstYearFall() {

        // No action
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        // Set up the offered courses

        final Map<Course, OfferedCourse> offeredCourses = new HashMap<>(20);

        // SEMINAR

        final OfferedCourse courseSeminar = new OfferedCourse(SpurCourses.SEMINAR);
        offeredCourses.put(SpurCourses.SEMINAR, courseSeminar);

        final Collection<OfferedSection> sectionsSeminar = new ArrayList<>(4);
        sectionsSeminar.add(new OfferedSection("SEMINAR", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.T, CLASSROOM_1,
                        LocalTime.of(3, 30), LocalTime.of(4, 20))));
        sectionsSeminar.add(new OfferedSection("SEMINAR", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.R, CLASSROOM_1,
                        LocalTime.of(3, 30), LocalTime.of(4, 20))));
        sectionsSeminar.add(new OfferedSection("SEMINAR", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.F, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsSeminar.add(new OfferedSection("SEMINAR", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.F, CLASSROOM_1,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        courseSeminar.addSectionsList(sectionsSeminar);

        // LIFE 102

        final OfferedCourse courseLife102 = new OfferedCourse(SpurCourses.LIFE102);
        offeredCourses.put(SpurCourses.LIFE102, courseLife102);

        final Collection<OfferedSection> sectionsLife102Class = new ArrayList<>(4);
        sectionsLife102Class.add(new OfferedSection("LIFE 102", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        sectionsLife102Class.add(new OfferedSection("LIFE 102", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        sectionsLife102Class.add(new OfferedSection("LIFE 102", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(2, 0), LocalTime.of(2, 50))));
        sectionsLife102Class.add(new OfferedSection("LIFE 102", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(4, 0), LocalTime.of(4, 50))));
        courseLife102.addSectionsList(sectionsLife102Class);

        final Collection<OfferedSection> sectionsLife102Lab = new ArrayList<>(4);
        sectionsLife102Lab.add(new OfferedSection("LIFE 102", 26,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(8, 0), LocalTime.of(10, 45))));
        sectionsLife102Lab.add(new OfferedSection("LIFE 102", 26,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(1, 45))));
        sectionsLife102Lab.add(new OfferedSection("LIFE 102", 26,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(1, 45))));
        sectionsLife102Lab.add(new OfferedSection("LIFE 102", 26,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(2, 0), LocalTime.of(4, 45))));
        sectionsLife102Lab.add(new OfferedSection("LIFE 102", 26,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(2, 0), LocalTime.of(4, 45))));
        courseLife102.addSectionsList(sectionsLife102Lab);

        // MATH 112

        final OfferedCourse courseMath112 = new OfferedCourse(SpurCourses.MATH112);
        offeredCourses.put(SpurCourses.MATH112, courseMath112);

        final Collection<OfferedSection> sectionsMath112Class = new ArrayList<>(4);
        sectionsMath112Class.add(new OfferedSection("MATH 112", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsMath112Class.add(new OfferedSection("MATH 112", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(11, 50))));
        sectionsMath112Class.add(new OfferedSection("MATH 112", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(1, 0), LocalTime.of(1, 50))));
        sectionsMath112Class.add(new OfferedSection("MATH 112", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(3, 0), LocalTime.of(3, 50))));
        courseMath112.addSectionsList(sectionsMath112Class);

        // CS 150B

        final OfferedCourse courseCs150b = new OfferedCourse(SpurCourses.CS150B);
        offeredCourses.put(SpurCourses.CS150B, courseCs150b);

        final Collection<OfferedSection> sectionsCs150bClass = new ArrayList<>(2);
        sectionsCs150bClass.add(new OfferedSection("CS 150B", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MW, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsCs150bClass.add(new OfferedSection("CS 150B", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MW, CLASSROOM_1,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        courseCs150b.addSectionsList(sectionsCs150bClass);

        final Collection<OfferedSection> sectionsCs150bRecitation = new ArrayList<>(2);
        sectionsCs150bRecitation.add(new OfferedSection("CS 150B", 40,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.MW, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(10, 45))));
        sectionsCs150bRecitation.add(new OfferedSection("CS 150B", 40,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.MW, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(10, 45))));
        courseCs150b.addSectionsList(sectionsCs150bRecitation);

        // IDEA 110

        final OfferedCourse courseIdea110 = new OfferedCourse(SpurCourses.IDEA110);
        offeredCourses.put(SpurCourses.IDEA110, courseIdea110);

        final Collection<OfferedSection> sectionsIdea110Class = new ArrayList<>(2);
        sectionsIdea110Class.add(new OfferedSection("IDEA 110", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        sectionsIdea110Class.add(new OfferedSection("IDEA 110", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(2, 0), LocalTime.of(2, 50))));
        courseIdea110.addSectionsList(sectionsIdea110Class);

        // HDFS 101

        final OfferedCourse courseHdfs101 = new OfferedCourse(SpurCourses.HDFS101);
        offeredCourses.put(SpurCourses.HDFS101, courseHdfs101);

        final Collection<OfferedSection> sectionsHdfs101Class = new ArrayList<>(2);
        sectionsHdfs101Class.add(new OfferedSection("HDFS 101", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(11, 50))));
        sectionsHdfs101Class.add(new OfferedSection("HDFS 101", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(1, 0), LocalTime.of(1, 50))));
        courseHdfs101.addSectionsList(sectionsHdfs101Class);

        // AGRI 116

        final OfferedCourse courseAgri116 = new OfferedCourse(SpurCourses.AGRI116);
        offeredCourses.put(SpurCourses.AGRI116, courseAgri116);

        final Collection<OfferedSection> sectionsAgri116Class = new ArrayList<>(2);
        sectionsAgri116Class.add(new OfferedSection("AGRI 116", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsAgri116Class.add(new OfferedSection("AGRI 116", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(12, 30), LocalTime.of(1, 45))));
        courseAgri116.addSectionsList(sectionsAgri116Class);

        // AB 111

        final OfferedCourse courseAb111 = new OfferedCourse(SpurCourses.AB111);
        offeredCourses.put(SpurCourses.AB111, courseAb111);

        final Collection<OfferedSection> sectionsAb111Class = new ArrayList<>(2);
        sectionsAb111Class.add(new OfferedSection("AB 111", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(2, 0), LocalTime.of(3, 15))));
        courseAb111.addSectionsList(sectionsAb111Class);

        // EHRS 220

        final OfferedCourse courseEhrs220 = new OfferedCourse(SpurCourses.EHRS220);
        offeredCourses.put(SpurCourses.EHRS220, courseEhrs220);

        final Collection<OfferedSection> sectionsEhrs220Class = new ArrayList<>(2);
        sectionsEhrs220Class.add(new OfferedSection("EHRS 220", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(3, 30), LocalTime.of(4, 45))));
        courseEhrs220.addSectionsList(sectionsEhrs220Class);

        // POLS 131

        final OfferedCourse coursePols131 = new OfferedCourse(SpurCourses.POLS131);
        offeredCourses.put(SpurCourses.POLS131, coursePols131);

        final Collection<OfferedSection> sectionsPols131Class = new ArrayList<>(2);
        sectionsPols131Class.add(new OfferedSection("POLS 131", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(4, 0), LocalTime.of(4, 50))));
        coursePols131.addSectionsList(sectionsPols131Class);

        // AREC 222

        final OfferedCourse courseArec222 = new OfferedCourse(SpurCourses.AREC222);
        offeredCourses.put(SpurCourses.AREC222, courseArec222);

        final Collection<OfferedSection> sectionsArec222Class = new ArrayList<>(2);
        sectionsArec222Class.add(new OfferedSection("AREC 222", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(9, 30), LocalTime.of(10, 45))));
        sectionsArec222Class.add(new OfferedSection("AREC 222", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(12, 30), LocalTime.of(1, 45))));
        courseArec222.addSectionsList(sectionsArec222Class);

        // SPCM 100

        final OfferedCourse courseSpcm100 = new OfferedCourse(SpurCourses.SPCM100);
        offeredCourses.put(SpurCourses.SPCM100, courseSpcm100);

        final Collection<OfferedSection> sectionsSpcm100Class = new ArrayList<>(2);
        sectionsSpcm100Class.add(new OfferedSection("SPCM 100", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsSpcm100Class.add(new OfferedSection("SPCM 100", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(2, 0), LocalTime.of(3, 15))));
        courseSpcm100.addSectionsList(sectionsSpcm100Class);

        // BZ 101

        final OfferedCourse courseBz101 = new OfferedCourse(SpurCourses.BZ101);
        offeredCourses.put(SpurCourses.BZ101, courseBz101);

        final Collection<OfferedSection> sectionsBz101Class = new ArrayList<>(2);
        sectionsBz101Class.add(new OfferedSection("BZ 101", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(3, 0), LocalTime.of(3, 50))));
        courseBz101.addSectionsList(sectionsBz101Class);

        // Generate the students who will register (160 students using the Fall preference distribution)

        final StudentPopulation population = new StudentPopulation(SpurStudents.SPUR_FALL_DISTRIBUTION,
                POPULATION_SIZE);
        final Map<StudentClassPreferences, Integer> counts = population.getCounts();

        // Generate a list of student preferences in random order

        final List<StudentClassPreferences> unordered = new ArrayList<>(POPULATION_SIZE);
        for (final Map.Entry<StudentClassPreferences, Integer> entry : counts.entrySet()) {
            final StudentClassPreferences prefs = entry.getKey();
            final int count = entry.getValue().intValue();
            for (int i = 0; i < count; ++i) {
                unordered.add(prefs);
            }
        }
        final long seed = System.currentTimeMillis() + System.nanoTime();
        final Random rnd = new Random(seed);
        int remaining = unordered.size();
        final List<StudentClassPreferences> randomized = new ArrayList<>(remaining);
        while (remaining > 0) {
            final int index = rnd.nextInt(remaining);
            randomized.add(unordered.get(index));
            --remaining;
        }

        // Simulate the registration process

        final Collection<EnrollingStudent> students = new ArrayList<>(POPULATION_SIZE);
        for (final StudentClassPreferences prefs : randomized) {
            final EnrollingStudent student = registerStudent(prefs, offeredCourses, rnd);
            students.add(student);
        }

        // TODO: Print student schedules

        for (final EnrollingStudent student : students) {
            Log.info(student);
        }
    }

    /**
     * Simulate the registration of a single enrolling student.
     *
     * @param prefs          the class preferences
     * @param offeredCourses the set of offered courses
     * @param rnd            a random number generator
     * @return the enrolling student record
     */
    private static EnrollingStudent registerStudent(final StudentClassPreferences prefs,
                                                    final Map<Course, OfferedCourse> offeredCourses, final Random rnd) {

        int totalCredits = 0;

        final List<Course> registeredForCourses = new ArrayList<>(6);

        // Add any courses marked as "mandatory"

        for (final Map.Entry<Course, OfferedCourse> entry : offeredCourses.entrySet()) {
            final Course course = entry.getKey();
            if (course.mandatory) {
                // TODO: Add these sections

                registeredForCourses.add(course);
            }
        }

        // Attempt to choose a course we have not already chosen until we have reached a target number of credits in
        // the student's schedule

        for (int j = 0; j < 1000; ++j) {
            final Course selected = prefs.pick(rnd);

            if (registeredForCourses.contains(selected)) {
                continue;
            }

            final OfferedCourse offered = offeredCourses.get(selected);
            if (Objects.nonNull(offered)) {
                final int credits = selected.numCredits;
                if (totalCredits + credits > prefs.maxCredits) {
                    break;
                }

                // TODO: Add these sections

                registeredForCourses.add(selected);
                totalCredits += credits;

                if (totalCredits >= prefs.minCredits) {
                    final int span = prefs.maxCredits - prefs.minCredits + 1;
                    final int delta = totalCredits - prefs.minCredits + 1;
                    if (rnd.nextInt(span) < delta) {
                        break;
                    }
                }
            }
        }

        final EnrollingStudent result = new EnrollingStudent(prefs, registeredForCourses);

        if (totalCredits < prefs.minCredits) {
            Log.warning("Unable to reach minimum desired credits");
        }

        return result;
    }

    /**
     * Main method to execute the simulation.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        runSimulation();
    }
}
