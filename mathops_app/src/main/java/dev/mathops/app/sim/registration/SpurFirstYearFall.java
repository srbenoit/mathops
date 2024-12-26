package dev.mathops.app.sim.registration;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.courses.EMeetingDays;
import dev.mathops.app.sim.courses.OfferedCourse;
import dev.mathops.app.sim.courses.OfferedSection;
import dev.mathops.app.sim.courses.OfferedSectionMeetingTime;
import dev.mathops.app.sim.courses.SpurCourses;
import dev.mathops.app.sim.rooms.SpurRooms;
import dev.mathops.app.sim.students.SpurStudents;
import dev.mathops.app.sim.students.StudentPopulation;
import dev.mathops.commons.log.Log;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A simulation of the Spur first-year Fall semester.
 */
final class SpurFirstYearFall {

    /** The population size. */
    private static final int POPULATION_SIZE = 80;

    /** The size of a general classroom. */
    private static final int CLASSROOM_SIZE = 40;

    /** The size of a general lab. */
    private static final int LAB_SIZE = 26;

    /** A meeting time label. */
    private static final String CLASS = "Class";

    /** A meeting time label. */
    private static final String LAB = "Lab";

    /** A meeting time label. */
    private static final String RECITATION = "Recitation";

    /** A classroom ID. */
    private static final String CLASSROOM_1 = SpurRooms.CAMPUS_ROOM_1.getId();

    /** A classroom ID. */
    private static final String CLASSROOM_2 = SpurRooms.CAMPUS_ROOM_2.getId();

    /** A classroom ID. */
    private static final String LAB_1 = SpurRooms.CAMPUS_LAB_1.getId();

    /**
     * Constructs a new {@code SpurFirstYearFall}.
     */
    private SpurFirstYearFall() {

        super();
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        // Set up the offered courses

        final Map<Course, OfferedCourse> courseMap = new HashMap<>(20);

        // CO 150
        {
            final OfferedCourse courseCO150 = new OfferedCourse(SpurCourses.CO150);
            courseMap.put(SpurCourses.CO150, courseCO150);

            final Collection<OfferedSection> sectionsCO150 = new ArrayList<>(4);
            sectionsCO150.add(new OfferedSection(courseCO150, 20,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(14, 0), LocalTime.of(14, 50))));
            sectionsCO150.add(new OfferedSection(courseCO150, 20,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(15, 0), LocalTime.of(15, 50))));
            sectionsCO150.add(new OfferedSection(courseCO150, 20,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(10, 0), LocalTime.of(10, 50))));
            sectionsCO150.add(new OfferedSection(courseCO150, 20,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(11, 0), LocalTime.of(11, 50))));
            courseCO150.addSectionsList(sectionsCO150);
        }

        // PRECALC
        {
            final OfferedCourse coursePRECALC = new OfferedCourse(SpurCourses.PRECALC);
            courseMap.put(SpurCourses.PRECALC, coursePRECALC);

            final Collection<OfferedSection> sectionsPRECALC = new ArrayList<>(2);
            sectionsPRECALC.add(new OfferedSection(coursePRECALC, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(10, 0), LocalTime.of(10, 50))));
            sectionsPRECALC.add(new OfferedSection(coursePRECALC, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(13, 0), LocalTime.of(13, 50))));
            coursePRECALC.addSectionsList(sectionsPRECALC);

            final Collection<OfferedSection> sectionsPrecalcRecitation = new ArrayList<>(1);
            sectionsPrecalcRecitation.add(new OfferedSection(coursePRECALC, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(RECITATION, EMeetingDays.T, CLASSROOM_1,
                            LocalTime.of(10, 0), LocalTime.of(10, 50))));
            sectionsPrecalcRecitation.add(new OfferedSection(coursePRECALC, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(RECITATION, EMeetingDays.T, CLASSROOM_1,
                            LocalTime.of(13, 0), LocalTime.of(13, 50))));
            coursePRECALC.addSectionsList(sectionsPrecalcRecitation);
        }

        // KEY 175
        {
            final OfferedCourse courseKEY175 = new OfferedCourse(SpurCourses.KEY175);
            courseMap.put(SpurCourses.KEY175, courseKEY175);

            final Collection<OfferedSection> sectionsKEY175 = new ArrayList<>(4);
            sectionsKEY175.add(new OfferedSection(courseKEY175, 30,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.M, CLASSROOM_1,
                            LocalTime.of(11, 0), LocalTime.of(11, 50))));
            sectionsKEY175.add(new OfferedSection(courseKEY175, 30,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.W, CLASSROOM_1,
                            LocalTime.of(11, 0), LocalTime.of(11, 50))));
            sectionsKEY175.add(new OfferedSection(courseKEY175, 30,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.R, CLASSROOM_1,
                            LocalTime.of(10, 0), LocalTime.of(10, 50))));
            sectionsKEY175.add(new OfferedSection(courseKEY175, 30,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.R, CLASSROOM_1,
                            LocalTime.of(13, 0), LocalTime.of(13, 50))));
            courseKEY175.addSectionsList(sectionsKEY175);
        }

        // LIFE 102
        {
            final OfferedCourse courseLIFE102 = new OfferedCourse(SpurCourses.LIFE102);
            courseMap.put(SpurCourses.LIFE102, courseLIFE102);

            final Collection<OfferedSection> sectionsLIFE102Class = new ArrayList<>(2);
            sectionsLIFE102Class.add(new OfferedSection(courseLIFE102, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(13, 0), LocalTime.of(13, 50))));
            sectionsLIFE102Class.add(new OfferedSection(courseLIFE102, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(14, 0), LocalTime.of(14, 50))));
            courseLIFE102.addSectionsList(sectionsLIFE102Class);

            final Collection<OfferedSection> sectionsLIFE102Lab = new ArrayList<>(4);
            sectionsLIFE102Lab.add(new OfferedSection(courseLIFE102, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                            LocalTime.of(9, 0), LocalTime.of(11, 50))));
            sectionsLIFE102Lab.add(new OfferedSection(courseLIFE102, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                            LocalTime.of(13, 0), LocalTime.of(15, 50))));
            sectionsLIFE102Lab.add(new OfferedSection(courseLIFE102, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                            LocalTime.of(9, 0), LocalTime.of(11, 50))));
            sectionsLIFE102Lab.add(new OfferedSection(courseLIFE102, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                            LocalTime.of(13, 0), LocalTime.of(15, 50))));
            courseLIFE102.addSectionsList(sectionsLIFE102Lab);
        }

        // AB 111
        {
            final OfferedCourse courseAb111 = new OfferedCourse(SpurCourses.AB111);
            courseMap.put(SpurCourses.AB111, courseAb111);

            final Collection<OfferedSection> sectionsAb111Class = new ArrayList<>(1);
            sectionsAb111Class.add(new OfferedSection(courseAb111, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(9, 0), LocalTime.of(9, 50))));
            courseAb111.addSectionsList(sectionsAb111Class);
        }

        // EHRS 220
        {
            final OfferedCourse courseERHS220 = new OfferedCourse(SpurCourses.ERHS220);
            courseMap.put(SpurCourses.ERHS220, courseERHS220);

            final Collection<OfferedSection> sectionsERHS220 = new ArrayList<>(2);
            sectionsERHS220.add(new OfferedSection(courseERHS220, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(15, 00), LocalTime.of(15, 50))));
            courseERHS220.addSectionsList(sectionsERHS220);
        }

        // CS 150B
        {
            final OfferedCourse courseCS150B = new OfferedCourse(SpurCourses.CS150B);
            courseMap.put(SpurCourses.CS150B, courseCS150B);

            final Collection<OfferedSection> sectionsCS150BClass = new ArrayList<>(1);
            sectionsCS150BClass.add(new OfferedSection(courseCS150B, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MW, CLASSROOM_1,
                            LocalTime.of(16, 0), LocalTime.of(16, 50))));
            courseCS150B.addSectionsList(sectionsCS150BClass);

            final Collection<OfferedSection> sectionsCS150BRecit = new ArrayList<>(1);
            sectionsCS150BRecit.add(new OfferedSection(courseCS150B, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(RECITATION, EMeetingDays.R, CLASSROOM_1,
                            LocalTime.of(11, 0), LocalTime.of(12, 50))));
            courseCS150B.addSectionsList(sectionsCS150BRecit);
        }

        // SPCM 100
        {
            final OfferedCourse courseSPCM100 = new OfferedCourse(SpurCourses.SPCM100);
            courseMap.put(SpurCourses.SPCM100, courseSPCM100);

            final Collection<OfferedSection> sectionsSPCM100 = new ArrayList<>(1);
            sectionsSPCM100.add(new OfferedSection(courseSPCM100, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                            LocalTime.of(9, 0), LocalTime.of(10, 15))));
            courseSPCM100.addSectionsList(sectionsSPCM100);
        }

        // IDEA 110
        {
            final OfferedCourse courseIDEA110 = new OfferedCourse(SpurCourses.IDEA110);
            courseMap.put(SpurCourses.IDEA110, courseIDEA110);

            final Collection<OfferedSection> sectionsIDEA110 = new ArrayList<>(1);
            sectionsIDEA110.add(new OfferedSection(courseIDEA110, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                            LocalTime.of(14, 0), LocalTime.of(14, 15))));
            courseIDEA110.addSectionsList(sectionsIDEA110);
        }

        // ECON 202
        {
            final OfferedCourse courseECON202 = new OfferedCourse(SpurCourses.ECON202);
            courseMap.put(SpurCourses.ECON202, courseECON202);

            final Collection<OfferedSection> sectionsECON202 = new ArrayList<>(1);
            sectionsECON202.add(new OfferedSection(courseECON202, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(9, 00), LocalTime.of(9, 50))));
            courseECON202.addSectionsList(sectionsECON202);
        }

        // HDFS 101
        {
            final OfferedCourse courseHDFS101 = new OfferedCourse(SpurCourses.HDFS101);
            courseMap.put(SpurCourses.HDFS101, courseHDFS101);

            final Collection<OfferedSection> sectionsHDFS101 = new ArrayList<>(1);
            sectionsHDFS101.add(new OfferedSection(courseHDFS101, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                            LocalTime.of(11, 0), LocalTime.of(12, 15))));
            courseHDFS101.addSectionsList(sectionsHDFS101);
        }

        // ART 100
        {
            final OfferedCourse courseART100 = new OfferedCourse(SpurCourses.ART100);
            courseMap.put(SpurCourses.ART100, courseART100);

            final Collection<OfferedSection> sectionsART100 = new ArrayList<>(1);
            sectionsART100.add(new OfferedSection(courseART100, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                            LocalTime.of(14, 0), LocalTime.of(15, 15))));
            courseART100.addSectionsList(sectionsART100);
        }

        // Generate the students who will register

        final StudentPopulation population = new StudentPopulation(SpurStudents.SPUR_FALL_DISTRIBUTION,
                POPULATION_SIZE);

        // permute all combinations of 3 "3-credit" courses [AB111, SPCM100, ERHS220, ART100, ECON202, IDEA110]
        SpurRegSim.findBest(1,courseMap, population, SpurCourses.AB111, SpurCourses.SPCM100, SpurCourses.ERHS220);
        SpurRegSim.findBest(2,courseMap, population, SpurCourses.AB111, SpurCourses.SPCM100, SpurCourses.ART100);
        SpurRegSim.findBest(3,courseMap, population, SpurCourses.AB111, SpurCourses.SPCM100, SpurCourses.ECON202);
        SpurRegSim.findBest(4,courseMap, population, SpurCourses.AB111, SpurCourses.SPCM100, SpurCourses.IDEA110);
        SpurRegSim.findBest(5,courseMap, population, SpurCourses.AB111, SpurCourses.ERHS220, SpurCourses.ART100);
        SpurRegSim.findBest(6,courseMap, population, SpurCourses.AB111, SpurCourses.ERHS220, SpurCourses.ECON202);
        SpurRegSim.findBest(7,courseMap, population, SpurCourses.AB111, SpurCourses.ERHS220, SpurCourses.IDEA110);
        SpurRegSim.findBest(8,courseMap, population, SpurCourses.AB111, SpurCourses.ART100, SpurCourses.ECON202);
        SpurRegSim.findBest(9,courseMap, population, SpurCourses.AB111, SpurCourses.ART100, SpurCourses.IDEA110);
        SpurRegSim.findBest(10,courseMap, population, SpurCourses.AB111, SpurCourses.ECON202, SpurCourses.IDEA110);
        SpurRegSim.findBest(11,courseMap, population, SpurCourses.SPCM100, SpurCourses.ERHS220, SpurCourses.ART100);
        SpurRegSim.findBest(12,courseMap, population, SpurCourses.SPCM100, SpurCourses.ERHS220, SpurCourses.ECON202);
        SpurRegSim.findBest(13,courseMap, population, SpurCourses.SPCM100, SpurCourses.ERHS220, SpurCourses.IDEA110);
        SpurRegSim.findBest(14,courseMap, population, SpurCourses.SPCM100, SpurCourses.ART100, SpurCourses.ECON202);
        SpurRegSim.findBest(15,courseMap, population, SpurCourses.SPCM100, SpurCourses.ART100, SpurCourses.IDEA110);
        SpurRegSim.findBest(16,courseMap, population, SpurCourses.SPCM100, SpurCourses.ECON202, SpurCourses.IDEA110);
        SpurRegSim.findBest(17,courseMap, population, SpurCourses.ERHS220, SpurCourses.ART100, SpurCourses.ECON202);
        SpurRegSim.findBest(18,courseMap, population, SpurCourses.ERHS220, SpurCourses.ART100, SpurCourses.IDEA110);
        SpurRegSim.findBest(19,courseMap, population, SpurCourses.ERHS220, SpurCourses.ECON202, SpurCourses.IDEA110);
        SpurRegSim.findBest(20,courseMap, population, SpurCourses.ART100, SpurCourses.ECON202, SpurCourses.IDEA110);

        final double score = SpurRegSim.simulateRegistrations(courseMap, population, true);
        final String scoreStr = Double.toString(score);
        Log.info("*** Final score = ", scoreStr);
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
