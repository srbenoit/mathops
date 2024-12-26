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
 * A simulation of the Spur first-year Spring semester.
 */
final class SpurFirstYearSpring {

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
     * Constructs a new {@code SpurFirstYearSpring}.
     */
    private SpurFirstYearSpring() {

        super();
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        // Set up the offered courses

        final Map<Course, OfferedCourse> courseMap = new HashMap<>(20);

        // MATH 160
        {
            final OfferedCourse courseMATH160 = new OfferedCourse(SpurCourses.MATH160);
            courseMap.put(SpurCourses.MATH160, courseMATH160);

            final Collection<OfferedSection> sectionsMATH160Class = new ArrayList<>(1);
            sectionsMATH160Class.add(new OfferedSection(courseMATH160, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(13, 0), LocalTime.of(13, 50))));
            courseMATH160.addSectionsList(sectionsMATH160Class);

            final Collection<OfferedSection> sectionsMATH160Recitation = new ArrayList<>(1);
            sectionsMATH160Recitation.add(new OfferedSection(courseMATH160, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(RECITATION, EMeetingDays.R, CLASSROOM_2,
                            LocalTime.of(13, 0), LocalTime.of(13, 50))));
            courseMATH160.addSectionsList(sectionsMATH160Recitation);
        }

        // IU 174
        {
            final OfferedCourse courseIU174 = new OfferedCourse(SpurCourses.IU174);
            courseMap.put(SpurCourses.IU174, courseIU174);

            final Collection<OfferedSection> sectionsIU174Class = new ArrayList<>(1);
            sectionsIU174Class.add(new OfferedSection(courseIU174, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(12, 0), LocalTime.of(12, 50))));
            courseIU174.addSectionsList(sectionsIU174Class);
        }

        // SOC 220
        {
            final OfferedCourse courseSOC220 = new OfferedCourse(SpurCourses.SOC220);
            courseMap.put(SpurCourses.SOC220, courseSOC220);

            final Collection<OfferedSection> sectionsSOC220Class = new ArrayList<>(1);
            sectionsSOC220Class.add(new OfferedSection(courseSOC220, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(9, 0), LocalTime.of(9, 50))));
            courseSOC220.addSectionsList(sectionsSOC220Class);
        }

        // LIFE 103
        {
            final OfferedCourse courseLIFE103 = new OfferedCourse(SpurCourses.LIFE103);
            courseMap.put(SpurCourses.LIFE103, courseLIFE103);

            final Collection<OfferedSection> sectionsLIFE103Class = new ArrayList<>(1);
            sectionsLIFE103Class.add(new OfferedSection(courseLIFE103, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                            LocalTime.of(14, 0), LocalTime.of(15, 15))));
            courseLIFE103.addSectionsList(sectionsLIFE103Class);

            final Collection<OfferedSection> sectionsLIFE103Lab = new ArrayList<>(2);
            sectionsLIFE103Lab.add(new OfferedSection(courseLIFE103, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.W, LAB_1,
                            LocalTime.of(14, 0), LocalTime.of(16, 50))));
            sectionsLIFE103Lab.add(new OfferedSection(courseLIFE103, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.F, LAB_1,
                            LocalTime.of(14, 0), LocalTime.of(16, 50))));
            courseLIFE103.addSectionsList(sectionsLIFE103Lab);
        }

        // CHEM 111 / 112
        {
            final OfferedCourse courseCHEM111 = new OfferedCourse(SpurCourses.CHEM111);
            courseMap.put(SpurCourses.CHEM111, courseCHEM111);

            final int classSize = Math.min(CLASSROOM_SIZE, LAB_SIZE * 3 / 2);

            final Collection<OfferedSection> sectionsChem111Class = new ArrayList<>(2);
            sectionsChem111Class.add(new OfferedSection(courseCHEM111, classSize,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(10, 0), LocalTime.of(10, 50))));
            sectionsChem111Class.add(new OfferedSection(courseCHEM111, classSize,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                            LocalTime.of(11, 0), LocalTime.of(11, 50))));
            courseCHEM111.addSectionsList(sectionsChem111Class);

            final Collection<OfferedSection> sectionsChem111Recitation = new ArrayList<>(2);
            sectionsChem111Recitation.add(new OfferedSection(courseCHEM111, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(RECITATION, EMeetingDays.R, CLASSROOM_2,
                            LocalTime.of(10, 0), LocalTime.of(10, 50))));
            sectionsChem111Recitation.add(new OfferedSection(courseCHEM111, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(RECITATION, EMeetingDays.R, CLASSROOM_2,
                            LocalTime.of(11, 0), LocalTime.of(11, 50))));
            courseCHEM111.addSectionsList(sectionsChem111Recitation);

            final Collection<OfferedSection> sectionsChem111Lab = new ArrayList<>(3);
            sectionsChem111Lab.add(new OfferedSection(courseCHEM111, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.M, LAB_1,
                            LocalTime.of(14, 0), LocalTime.of(16, 50))));
            sectionsChem111Lab.add(new OfferedSection(courseCHEM111, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                            LocalTime.of(14, 0), LocalTime.of(16, 50))));
            sectionsChem111Lab.add(new OfferedSection(courseCHEM111, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                            LocalTime.of(8, 0), LocalTime.of(10, 50))));
            courseCHEM111.addSectionsList(sectionsChem111Lab);
        }

        // IDEA 210
        {
            final OfferedCourse courseIDEA210 = new OfferedCourse(SpurCourses.IDEA210);
            courseMap.put(SpurCourses.IDEA210, courseIDEA210);

            final Collection<OfferedSection> sectionsIdea210Class = new ArrayList<>(1);
            sectionsIdea210Class.add(new OfferedSection(courseIDEA210, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(10, 0), LocalTime.of(10, 50))));
            courseIDEA210.addSectionsList(sectionsIdea210Class);
        }

        // CS 201
        {
            final OfferedCourse courseCS201 = new OfferedCourse(SpurCourses.CS201);
            courseMap.put(SpurCourses.CS201, courseCS201);

            final Collection<OfferedSection> sectionsCs201Class = new ArrayList<>(1);
            sectionsCs201Class.add(new OfferedSection(courseCS201, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(14, 0), LocalTime.of(14, 50))));
            courseCS201.addSectionsList(sectionsCs201Class);
        }

        // HIST 15X
        {
            final OfferedCourse courseHIST15x = new OfferedCourse(SpurCourses.HIST15X);
            courseMap.put(SpurCourses.HIST15X, courseHIST15x);

            final Collection<OfferedSection> sectionsHIST15x = new ArrayList<>(1);
            sectionsHIST15x.add(new OfferedSection(courseHIST15x, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                            LocalTime.of(12, 30), LocalTime.of(13, 45))));
            courseHIST15x.addSectionsList(sectionsHIST15x);
        }

        // AMST 101
        {
            final OfferedCourse courseAMST101 = new OfferedCourse(SpurCourses.AMST101);
            courseMap.put(SpurCourses.AMST101, courseAMST101);

            final Collection<OfferedSection> sectionsAMST101 = new ArrayList<>(1);
            sectionsAMST101.add(new OfferedSection(courseAMST101, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                            LocalTime.of(8, 30), LocalTime.of(9, 45))));
            courseAMST101.addSectionsList(sectionsAMST101);
        }

        // ETST 253
        {
            final OfferedCourse courseETST253 = new OfferedCourse(SpurCourses.ETST253);
            courseMap.put(SpurCourses.ETST253, courseETST253);

            final Collection<OfferedSection> sectionsETST253 = new ArrayList<>(1);
            sectionsETST253.add(new OfferedSection(courseETST253, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(13, 0), LocalTime.of(13, 50))));
            courseETST253.addSectionsList(sectionsETST253);
        }

        // ETST 240
        {
            final OfferedCourse courseETST240 = new OfferedCourse(SpurCourses.ETST240);
            courseMap.put(SpurCourses.ETST240, courseETST240);

            final Collection<OfferedSection> sectionsETST240 = new ArrayList<>(1);
            sectionsETST240.add(new OfferedSection(courseETST240, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                            LocalTime.of(8, 0), LocalTime.of(8, 50))));
            courseETST240.addSectionsList(sectionsETST240);
        }

        // KEY 192A
        {
            final OfferedCourse courseKEY192A = new OfferedCourse(SpurCourses.KEY192A);
            courseMap.put(SpurCourses.KEY192A, courseKEY192A);

            final Collection<OfferedSection> sectionsKEY192A = new ArrayList<>(4);
            sectionsKEY192A.add(new OfferedSection(courseKEY192A, 30,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.W, CLASSROOM_1,
                            LocalTime.of(15, 0), LocalTime.of(15, 50))));
            sectionsKEY192A.add(new OfferedSection(courseKEY192A, 30,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.T, CLASSROOM_1,
                            LocalTime.of(10, 0), LocalTime.of(10, 50))));
            sectionsKEY192A.add(new OfferedSection(courseKEY192A, 30,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.T, CLASSROOM_1,
                            LocalTime.of(11, 0), LocalTime.of(11, 50))));
            sectionsKEY192A.add(new OfferedSection(courseKEY192A, 30,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.R, CLASSROOM_1,
                            LocalTime.of(14, 0), LocalTime.of(14, 50))));
            courseKEY192A.addSectionsList(sectionsKEY192A);
        }

        // Generate the students who will register

        final StudentPopulation population = new StudentPopulation(SpurStudents.SPUR_SPRING_DISTRIBUTION,
                POPULATION_SIZE);

        // permute combinations of 3 "3-credit" courses [IU174, SOC220, IDEA210, CS201, HIST15X, AMST101, ETST253,
        // ETST240]
        SpurRegSim.findBest(1, courseMap, population, SpurCourses.IU174, SpurCourses.SOC220, SpurCourses.IDEA210);
        SpurRegSim.findBest(2, courseMap, population, SpurCourses.IU174, SpurCourses.SOC220, SpurCourses.CS201);
        SpurRegSim.findBest(3, courseMap, population, SpurCourses.IU174, SpurCourses.SOC220, SpurCourses.HIST15X);
        SpurRegSim.findBest(4, courseMap, population, SpurCourses.IU174, SpurCourses.SOC220, SpurCourses.AMST101);
        SpurRegSim.findBest(5, courseMap, population, SpurCourses.IU174, SpurCourses.SOC220, SpurCourses.ETST253);
        SpurRegSim.findBest(6, courseMap, population, SpurCourses.IU174, SpurCourses.SOC220, SpurCourses.ETST240);
        SpurRegSim.findBest(7, courseMap, population, SpurCourses.IU174, SpurCourses.IDEA210, SpurCourses.CS201);
        SpurRegSim.findBest(8, courseMap, population, SpurCourses.IU174, SpurCourses.IDEA210, SpurCourses.HIST15X);
        SpurRegSim.findBest(9, courseMap, population, SpurCourses.IU174, SpurCourses.IDEA210, SpurCourses.AMST101);
        SpurRegSim.findBest(10, courseMap, population, SpurCourses.IU174, SpurCourses.IDEA210, SpurCourses.ETST253);
        SpurRegSim.findBest(11, courseMap, population, SpurCourses.IU174, SpurCourses.IDEA210, SpurCourses.ETST240);
        SpurRegSim.findBest(12, courseMap, population, SpurCourses.IU174, SpurCourses.CS201, SpurCourses.HIST15X);
        SpurRegSim.findBest(13, courseMap, population, SpurCourses.IU174, SpurCourses.CS201, SpurCourses.AMST101);
        SpurRegSim.findBest(14, courseMap, population, SpurCourses.IU174, SpurCourses.CS201, SpurCourses.ETST253);
        SpurRegSim.findBest(15, courseMap, population, SpurCourses.IU174, SpurCourses.CS201, SpurCourses.ETST240);
        SpurRegSim.findBest(16, courseMap, population, SpurCourses.IU174, SpurCourses.HIST15X, SpurCourses.AMST101);
        SpurRegSim.findBest(17, courseMap, population, SpurCourses.IU174, SpurCourses.HIST15X, SpurCourses.ETST253);
        SpurRegSim.findBest(18, courseMap, population, SpurCourses.IU174, SpurCourses.HIST15X, SpurCourses.ETST240);
        SpurRegSim.findBest(19, courseMap, population, SpurCourses.IU174, SpurCourses.AMST101, SpurCourses.ETST253);
        SpurRegSim.findBest(20, courseMap, population, SpurCourses.IU174, SpurCourses.AMST101, SpurCourses.ETST240);
        SpurRegSim.findBest(21, courseMap, population, SpurCourses.IU174, SpurCourses.ETST253, SpurCourses.ETST240);
        SpurRegSim.findBest(22, courseMap, population, SpurCourses.SOC220, SpurCourses.IDEA210, SpurCourses.CS201);
        SpurRegSim.findBest(23, courseMap, population, SpurCourses.SOC220, SpurCourses.IDEA210, SpurCourses.HIST15X);
        SpurRegSim.findBest(24, courseMap, population, SpurCourses.SOC220, SpurCourses.IDEA210, SpurCourses.AMST101);
        SpurRegSim.findBest(25, courseMap, population, SpurCourses.SOC220, SpurCourses.IDEA210, SpurCourses.ETST253);
        SpurRegSim.findBest(26, courseMap, population, SpurCourses.SOC220, SpurCourses.IDEA210, SpurCourses.ETST240);
        SpurRegSim.findBest(27, courseMap, population, SpurCourses.SOC220, SpurCourses.CS201, SpurCourses.HIST15X);
        SpurRegSim.findBest(28, courseMap, population, SpurCourses.SOC220, SpurCourses.CS201, SpurCourses.AMST101);
        SpurRegSim.findBest(29, courseMap, population, SpurCourses.SOC220, SpurCourses.CS201, SpurCourses.ETST253);
        SpurRegSim.findBest(30, courseMap, population, SpurCourses.SOC220, SpurCourses.CS201, SpurCourses.ETST240);
        SpurRegSim.findBest(31, courseMap, population, SpurCourses.SOC220, SpurCourses.HIST15X, SpurCourses.AMST101);
        SpurRegSim.findBest(32, courseMap, population, SpurCourses.SOC220, SpurCourses.HIST15X, SpurCourses.ETST253);
        SpurRegSim.findBest(33, courseMap, population, SpurCourses.SOC220, SpurCourses.HIST15X, SpurCourses.ETST240);
        SpurRegSim.findBest(34, courseMap, population, SpurCourses.SOC220, SpurCourses.AMST101, SpurCourses.ETST253);
        SpurRegSim.findBest(35, courseMap, population, SpurCourses.SOC220, SpurCourses.AMST101, SpurCourses.ETST240);
        SpurRegSim.findBest(36, courseMap, population, SpurCourses.SOC220, SpurCourses.ETST253, SpurCourses.ETST240);
        SpurRegSim.findBest(37, courseMap, population, SpurCourses.IDEA210, SpurCourses.CS201, SpurCourses.HIST15X);
        SpurRegSim.findBest(38, courseMap, population, SpurCourses.IDEA210, SpurCourses.CS201, SpurCourses.AMST101);
        SpurRegSim.findBest(39, courseMap, population, SpurCourses.IDEA210, SpurCourses.CS201, SpurCourses.ETST253);
        SpurRegSim.findBest(40, courseMap, population, SpurCourses.IDEA210, SpurCourses.CS201, SpurCourses.ETST240);
        SpurRegSim.findBest(41, courseMap, population, SpurCourses.IDEA210, SpurCourses.HIST15X, SpurCourses.AMST101);
        SpurRegSim.findBest(42, courseMap, population, SpurCourses.IDEA210, SpurCourses.HIST15X, SpurCourses.ETST253);
        SpurRegSim.findBest(43, courseMap, population, SpurCourses.IDEA210, SpurCourses.HIST15X, SpurCourses.ETST240);
        SpurRegSim.findBest(44, courseMap, population, SpurCourses.IDEA210, SpurCourses.AMST101, SpurCourses.ETST253);
        SpurRegSim.findBest(45, courseMap, population, SpurCourses.IDEA210, SpurCourses.AMST101, SpurCourses.ETST240);
        SpurRegSim.findBest(46, courseMap, population, SpurCourses.IDEA210, SpurCourses.ETST253, SpurCourses.ETST240);
        SpurRegSim.findBest(47, courseMap, population, SpurCourses.CS201, SpurCourses.HIST15X, SpurCourses.AMST101);
        SpurRegSim.findBest(48, courseMap, population, SpurCourses.CS201, SpurCourses.HIST15X, SpurCourses.ETST253);
        SpurRegSim.findBest(49, courseMap, population, SpurCourses.CS201, SpurCourses.HIST15X, SpurCourses.ETST240);
        SpurRegSim.findBest(50, courseMap, population, SpurCourses.CS201, SpurCourses.AMST101, SpurCourses.ETST253);
        SpurRegSim.findBest(51, courseMap, population, SpurCourses.CS201, SpurCourses.AMST101, SpurCourses.ETST240);
        SpurRegSim.findBest(52, courseMap, population, SpurCourses.CS201, SpurCourses.ETST253, SpurCourses.ETST240);
        SpurRegSim.findBest(53, courseMap, population, SpurCourses.HIST15X, SpurCourses.AMST101, SpurCourses.ETST253);
        SpurRegSim.findBest(54, courseMap, population, SpurCourses.HIST15X, SpurCourses.AMST101, SpurCourses.ETST240);
        SpurRegSim.findBest(55, courseMap, population, SpurCourses.HIST15X, SpurCourses.ETST253, SpurCourses.ETST240);
        SpurRegSim.findBest(56, courseMap, population, SpurCourses.AMST101, SpurCourses.ETST253, SpurCourses.ETST240);

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
