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
    private static final int POPULATION_SIZE = 180;

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

        final Map<Course, OfferedCourse> offeredCourses = new HashMap<>(20);

        // SEMINAR

        final OfferedCourse courseSeminar = new OfferedCourse(SpurCourses.SEMINAR);
        offeredCourses.put(SpurCourses.SEMINAR, courseSeminar);

        final Collection<OfferedSection> sectionsSeminar = new ArrayList<>(4);
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.T, CLASSROOM_1,
                        LocalTime.of(15, 30), LocalTime.of(16, 20))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.R, CLASSROOM_1,
                        LocalTime.of(15, 30), LocalTime.of(16, 20))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.F, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.F, CLASSROOM_1,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        if (POPULATION_SIZE > 160) {
            sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.R, CLASSROOM_2,
                            LocalTime.of(8, 30), LocalTime.of(9, 20))));
        }
        courseSeminar.addSectionsList(sectionsSeminar);

        // LIFE 102

        final OfferedCourse courseLife102 = new OfferedCourse(SpurCourses.LIFE102);
        offeredCourses.put(SpurCourses.LIFE102, courseLife102);

        final Collection<OfferedSection> sectionsLife102Class = new ArrayList<>(4);
        sectionsLife102Class.add(new OfferedSection(courseLife102, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        sectionsLife102Class.add(new OfferedSection(courseLife102, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        sectionsLife102Class.add(new OfferedSection(courseLife102, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(14, 0), LocalTime.of(14, 50))));
        sectionsLife102Class.add(new OfferedSection(courseLife102, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(16, 0), LocalTime.of(16, 50))));
        courseLife102.addSectionsList(sectionsLife102Class);

        final Collection<OfferedSection> sectionsLife102Lab = new ArrayList<>(4);
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(8, 0), LocalTime.of(10, 45))));
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(13, 45))));
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(13, 45))));
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(14, 0), LocalTime.of(16, 45))));
        sectionsLife102Lab.add(new OfferedSection(courseLife102, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(14, 0), LocalTime.of(16, 45))));
        courseLife102.addSectionsList(sectionsLife102Lab);

        // MATH 112

        final OfferedCourse courseMath112 = new OfferedCourse(SpurCourses.MATH112);
        offeredCourses.put(SpurCourses.MATH112, courseMath112);

        final Collection<OfferedSection> sectionsMath112Class = new ArrayList<>(4);
        sectionsMath112Class.add(new OfferedSection(courseMath112, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsMath112Class.add(new OfferedSection(courseMath112, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(11, 50))));
        sectionsMath112Class.add(new OfferedSection(courseMath112, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(13, 0), LocalTime.of(13, 50))));
        sectionsMath112Class.add(new OfferedSection(courseMath112, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(15, 0), LocalTime.of(15, 50))));
        courseMath112.addSectionsList(sectionsMath112Class);

        // CS 150B

        final OfferedCourse courseCs150b = new OfferedCourse(SpurCourses.CS150B);
        offeredCourses.put(SpurCourses.CS150B, courseCs150b);

        final Collection<OfferedSection> sectionsCs150bClass = new ArrayList<>(2);
        sectionsCs150bClass.add(new OfferedSection(courseCs150b, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MW, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsCs150bClass.add(new OfferedSection(courseCs150b, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MW, CLASSROOM_1,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        courseCs150b.addSectionsList(sectionsCs150bClass);

        final Collection<OfferedSection> sectionsCs150bRecitation = new ArrayList<>(2);
        sectionsCs150bRecitation.add(new OfferedSection(courseCs150b, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.T, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(10, 45))));
        sectionsCs150bRecitation.add(new OfferedSection(courseCs150b, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.R, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(10, 45))));
        courseCs150b.addSectionsList(sectionsCs150bRecitation);

        // IDEA 110

        final OfferedCourse courseIdea110 = new OfferedCourse(SpurCourses.IDEA110);
        offeredCourses.put(SpurCourses.IDEA110, courseIdea110);

        final Collection<OfferedSection> sectionsIdea110Class = new ArrayList<>(2);
        sectionsIdea110Class.add(new OfferedSection(courseIdea110, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        sectionsIdea110Class.add(new OfferedSection(courseIdea110, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(14, 0), LocalTime.of(14, 50))));
        courseIdea110.addSectionsList(sectionsIdea110Class);

        // HDFS 101

        final OfferedCourse courseHdfs101 = new OfferedCourse(SpurCourses.HDFS101);
        offeredCourses.put(SpurCourses.HDFS101, courseHdfs101);

        final Collection<OfferedSection> sectionsHdfs101Class = new ArrayList<>(2);
        sectionsHdfs101Class.add(new OfferedSection(courseHdfs101, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(11, 50))));
        sectionsHdfs101Class.add(new OfferedSection(courseHdfs101, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(13, 0), LocalTime.of(13, 50))));
        courseHdfs101.addSectionsList(sectionsHdfs101Class);

        // AGRI 116

        final OfferedCourse courseAgri116 = new OfferedCourse(SpurCourses.AGRI116);
        offeredCourses.put(SpurCourses.AGRI116, courseAgri116);

        final Collection<OfferedSection> sectionsAgri116Class = new ArrayList<>(2);
        sectionsAgri116Class.add(new OfferedSection(courseAgri116, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsAgri116Class.add(new OfferedSection(courseAgri116, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(12, 30), LocalTime.of(13, 45))));
        courseAgri116.addSectionsList(sectionsAgri116Class);

        // AB 111

        final OfferedCourse courseAb111 = new OfferedCourse(SpurCourses.AB111);
        offeredCourses.put(SpurCourses.AB111, courseAb111);

        final Collection<OfferedSection> sectionsAb111Class = new ArrayList<>(2);
        sectionsAb111Class.add(new OfferedSection(courseAb111, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(14, 0), LocalTime.of(15, 15))));
        courseAb111.addSectionsList(sectionsAb111Class);

        // EHRS 220

        final OfferedCourse courseEhrs220 = new OfferedCourse(SpurCourses.ERHS220);
        offeredCourses.put(SpurCourses.ERHS220, courseEhrs220);

        final Collection<OfferedSection> sectionsEhrs220Class = new ArrayList<>(2);
        sectionsEhrs220Class.add(new OfferedSection(courseEhrs220, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(15, 30), LocalTime.of(16, 45))));
        courseEhrs220.addSectionsList(sectionsEhrs220Class);

        // POLS 131

        final OfferedCourse coursePols131 = new OfferedCourse(SpurCourses.POLS131);
        offeredCourses.put(SpurCourses.POLS131, coursePols131);

        final Collection<OfferedSection> sectionsPols131Class = new ArrayList<>(2);
        sectionsPols131Class.add(new OfferedSection(coursePols131, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(16, 0), LocalTime.of(16, 50))));
        coursePols131.addSectionsList(sectionsPols131Class);

        // AREC 222

        final OfferedCourse courseArec222 = new OfferedCourse(SpurCourses.AREC222);
        offeredCourses.put(SpurCourses.AREC222, courseArec222);

        final Collection<OfferedSection> sectionsArec222Class = new ArrayList<>(2);
        sectionsArec222Class.add(new OfferedSection(courseArec222, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(9, 30), LocalTime.of(10, 45))));
        sectionsArec222Class.add(new OfferedSection(courseArec222, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(12, 30), LocalTime.of(13, 45))));
        courseArec222.addSectionsList(sectionsArec222Class);

        // SPCM 100

        final OfferedCourse courseSpcm100 = new OfferedCourse(SpurCourses.SPCM100);
        offeredCourses.put(SpurCourses.SPCM100, courseSpcm100);

        final Collection<OfferedSection> sectionsSpcm100Class = new ArrayList<>(2);
        sectionsSpcm100Class.add(new OfferedSection(courseSpcm100, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsSpcm100Class.add(new OfferedSection(courseSpcm100, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(14, 0), LocalTime.of(15, 15))));
        courseSpcm100.addSectionsList(sectionsSpcm100Class);

        // BZ 101

        final OfferedCourse courseBz101 = new OfferedCourse(SpurCourses.BZ101);
        offeredCourses.put(SpurCourses.BZ101, courseBz101);

        final Collection<OfferedSection> sectionsBz101Class = new ArrayList<>(2);
        sectionsBz101Class.add(new OfferedSection(courseBz101, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(15, 0), LocalTime.of(15, 50))));
        courseBz101.addSectionsList(sectionsBz101Class);

        // Generate the students who will register

        final StudentPopulation population = new StudentPopulation(SpurStudents.SPUR_FALL_DISTRIBUTION,
                POPULATION_SIZE);

        SpurRegistrationSim.simulateRegistrations(offeredCourses, population);
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
