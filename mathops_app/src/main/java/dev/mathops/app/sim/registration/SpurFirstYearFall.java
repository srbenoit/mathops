package dev.mathops.app.sim.registration;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.SpurFallCourses;
import dev.mathops.app.sim.rooms.SpurRooms;
import dev.mathops.app.sim.students.StudentClassPreferences;
import dev.mathops.app.sim.students.StudentDistribution;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.log.Log;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * A simulation of the Spur first-year Fall semester.
 */
final class SpurFirstYearFall {

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

        final Collection<OfferedCourse> offeredCourses = new ArrayList<>(20);

        // SEMINAR

        final OfferedCourse courseSeminar = new OfferedCourse("SEMINAR");
        offeredCourses.add(courseSeminar);

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

        final OfferedCourse courseLife102 = new OfferedCourse("LIFE 102");
        offeredCourses.add(courseLife102);

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

        final OfferedCourse courseMath112 = new OfferedCourse("MATH 112");
        offeredCourses.add(courseMath112);

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

        final OfferedCourse courseCs150b = new OfferedCourse("CS 150B");
        offeredCourses.add(courseCs150b);

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

        final OfferedCourse courseIdea110 = new OfferedCourse("IDEA 110");
        offeredCourses.add(courseIdea110);

        final Collection<OfferedSection> sectionsIdea110Class = new ArrayList<>(2);
        sectionsIdea110Class.add(new OfferedSection("IDEA 110", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        sectionsIdea110Class.add(new OfferedSection("IDEA 110", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(2, 0), LocalTime.of(2, 50))));
        courseIdea110.addSectionsList(sectionsIdea110Class);

        // HDFS 101

        final OfferedCourse courseHdfs101 = new OfferedCourse("HDFS 101");
        offeredCourses.add(courseHdfs101);

        final Collection<OfferedSection> sectionsHdfs101Class = new ArrayList<>(2);
        sectionsHdfs101Class.add(new OfferedSection("HDFS 101", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(11, 50))));
        sectionsHdfs101Class.add(new OfferedSection("HDFS 101", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(1, 0), LocalTime.of(1, 50))));
        courseHdfs101.addSectionsList(sectionsHdfs101Class);

        // AGRI 116

        final OfferedCourse courseAgri116 = new OfferedCourse("AGRI 116");
        offeredCourses.add(courseAgri116);

        final Collection<OfferedSection> sectionsAgri116Class = new ArrayList<>(2);
        sectionsAgri116Class.add(new OfferedSection("AGRI 116", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsAgri116Class.add(new OfferedSection("AGRI 116", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(12, 30), LocalTime.of(1, 45))));
        courseAgri116.addSectionsList(sectionsAgri116Class);

        // AB 111

        final OfferedCourse courseAb111 = new OfferedCourse("AB 111");
        offeredCourses.add(courseAb111);

        final Collection<OfferedSection> sectionsAb111Class = new ArrayList<>(2);
        sectionsAb111Class.add(new OfferedSection("AB 111", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(2, 0), LocalTime.of(3, 15))));
        courseAb111.addSectionsList(sectionsAb111Class);

        // EHRS 220

        final OfferedCourse courseEhrs220 = new OfferedCourse("EHRS 220");
        offeredCourses.add(courseEhrs220);

        final Collection<OfferedSection> sectionsEhrs220Class = new ArrayList<>(2);
        sectionsEhrs220Class.add(new OfferedSection("EHRS 220", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(3, 30), LocalTime.of(4, 45))));
        courseEhrs220.addSectionsList(sectionsEhrs220Class);

        // POLS 131

        final OfferedCourse coursePols131 = new OfferedCourse("POLS 131");
        offeredCourses.add(coursePols131);

        final Collection<OfferedSection> sectionsPols131Class = new ArrayList<>(2);
        sectionsPols131Class.add(new OfferedSection("POLS 131", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(4, 0), LocalTime.of(4, 50))));
        coursePols131.addSectionsList(sectionsPols131Class);

        // AREC 222

        final OfferedCourse courseArec222 = new OfferedCourse("AREC 222");
        offeredCourses.add(courseArec222);

        final Collection<OfferedSection> sectionsArec222Class = new ArrayList<>(2);
        sectionsArec222Class.add(new OfferedSection("AREC 222", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(9, 30), LocalTime.of(10, 45))));
        sectionsArec222Class.add(new OfferedSection("AREC 222", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(12, 30), LocalTime.of(1, 45))));
        courseArec222.addSectionsList(sectionsArec222Class);

        // SPCM 100

        final OfferedCourse courseSpcm100 = new OfferedCourse("SPCM 100");
        offeredCourses.add(courseSpcm100);

        final Collection<OfferedSection> sectionsSpcm100Class = new ArrayList<>(2);
        sectionsSpcm100Class.add(new OfferedSection("SPCM 100", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsSpcm100Class.add(new OfferedSection("SPCM 100", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(2, 0), LocalTime.of(3, 15))));
        courseSpcm100.addSectionsList(sectionsSpcm100Class);

        // BZ 101

        final OfferedCourse courseBz101 = new OfferedCourse("BZ 101");
        offeredCourses.add(courseBz101);

        final Collection<OfferedSection> sectionsBz101Class = new ArrayList<>(2);
        sectionsBz101Class.add(new OfferedSection("BZ 101", 40,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(3, 0), LocalTime.of(3, 50))));
        courseBz101.addSectionsList(sectionsBz101Class);

        // Set up the preferences for each "exploratory studies" track

        final StudentClassPreferences prefs1 = new StudentClassPreferences(HEALTH_LIFE_FOOD, 13, 17);
        prefs1.setPreference(SpurFallCourses.SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            prefs1.setPreference(SpurFallCourses.MATH112, 1.0);
        }
        prefs1.setPreference(SpurFallCourses.AGRI116, 0.3);
        prefs1.setPreference(SpurFallCourses.AREC222, 0.3);
        if (INCLUDE_POLS) {
            prefs1.setPreference(SpurFallCourses.POLS131, 0.1);
        }
        prefs1.setPreference(SpurFallCourses.AB111, 0.1);
        prefs1.setPreference(SpurFallCourses.BZ101, 0.2);
        prefs1.setPreference(SpurFallCourses.LIFE102, 0.9);
        if (INCLUDE_EHRS) {
            prefs1.setPreference(SpurFallCourses.EHRS220, 0.1);
        }
        prefs1.setPreference(SpurFallCourses.SPCM100, 0.25);
        prefs1.setPreference(SpurFallCourses.CS150B, 0.25);
        prefs1.setPreference(SpurFallCourses.IDEA110, 0.25);
        prefs1.setPreference(SpurFallCourses.HDFS101, 0.25);

        final StudentClassPreferences prefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        prefs2.setPreference(SpurFallCourses.SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            prefs2.setPreference(SpurFallCourses.MATH112, 1.0);
        }
        prefs2.setPreference(SpurFallCourses.AGRI116, 0.4);
        prefs2.setPreference(SpurFallCourses.AREC222, 0.4);
        if (INCLUDE_POLS) {
            prefs2.setPreference(SpurFallCourses.POLS131, 0.1);
        }
        prefs2.setPreference(SpurFallCourses.AB111, 0.1);
        prefs2.setPreference(SpurFallCourses.BZ101, 0.2);
        prefs2.setPreference(SpurFallCourses.LIFE102, 0.8);
        if (INCLUDE_EHRS) {
            prefs2.setPreference(SpurFallCourses.EHRS220, 0.1);
        }
        prefs2.setPreference(SpurFallCourses.SPCM100, 0.25);
        prefs2.setPreference(SpurFallCourses.CS150B, 0.25);
        prefs2.setPreference(SpurFallCourses.IDEA110, 0.2);
        prefs2.setPreference(SpurFallCourses.HDFS101, 0.2);

        final StudentClassPreferences prefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        prefs3.setPreference(SpurFallCourses.SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            prefs3.setPreference(SpurFallCourses.MATH112, 1.0);
        }
        prefs3.setPreference(SpurFallCourses.AGRI116, 0.25);
        prefs3.setPreference(SpurFallCourses.AREC222, 0.25);
        if (INCLUDE_POLS) {
            prefs3.setPreference(SpurFallCourses.POLS131, 0.25);
        }
        prefs3.setPreference(SpurFallCourses.AB111, 0.1);
        prefs3.setPreference(SpurFallCourses.BZ101, 0.1);
        prefs3.setPreference(SpurFallCourses.LIFE102, 0.9);
        if (INCLUDE_EHRS) {
            prefs3.setPreference(SpurFallCourses.EHRS220, 0.1);
        }
        prefs3.setPreference(SpurFallCourses.SPCM100, 0.1);
        prefs3.setPreference(SpurFallCourses.CS150B, 0.7);
        prefs3.setPreference(SpurFallCourses.IDEA110, 0.25);
        prefs3.setPreference(SpurFallCourses.HDFS101, 0.1);

        final StudentClassPreferences prefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);
        prefs4.setPreference(SpurFallCourses.SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            prefs4.setPreference(SpurFallCourses.MATH112, 1.0);
        }
        prefs4.setPreference(SpurFallCourses.AGRI116, 0.4);
        prefs4.setPreference(SpurFallCourses.AREC222, 0.4);
        if (INCLUDE_POLS) {
            prefs4.setPreference(SpurFallCourses.POLS131, 0.1);
        }
        prefs4.setPreference(SpurFallCourses.AB111, 0.2);
        prefs4.setPreference(SpurFallCourses.BZ101, 0.1);
        prefs4.setPreference(SpurFallCourses.LIFE102, 0.7);
        if (INCLUDE_EHRS) {
            prefs4.setPreference(SpurFallCourses.EHRS220, 0.2);
        }
        prefs4.setPreference(SpurFallCourses.SPCM100, 0.25);
        prefs4.setPreference(SpurFallCourses.CS150B, 0.25);
        prefs4.setPreference(SpurFallCourses.IDEA110, 0.2);
        prefs4.setPreference(SpurFallCourses.HDFS101, 0.2);

        // Set up the student distribution

        final StudentDistribution distribution = new StudentDistribution();
        distribution.addGroup(prefs1, 0.411);
        distribution.addGroup(prefs2, 0.142);
        distribution.addGroup(prefs3, 0.265);
        distribution.addGroup(prefs4, 0.182);

        // SIMULATION PART 1 - DETERMINE MAXIMUM POSSIBLE POPULATION SIZE THAT DOES NOT EXCEED TOTAL CLASSROOM SPACE
        final int maxPopulation = ComputePopulationSize.compute(courses, distribution, rooms);
        Log.info("The maximum population supported was " + maxPopulation);

        // SIMULATION PART 2 - Try to build an assignment of courses to sections across classrooms and labs
        final StudentPopulation population160 = new StudentPopulation(distribution, 160);
        final Map<Course, Integer> seatCounts = ComputeSectionsNeeded.compute(courses, population160, rooms);
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
