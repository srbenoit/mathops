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
 * A simulation of the Spur first-year Spring semester.
 */
final class SpurFirstYearSpring {

    /** The population size. */
    private static final int POPULATION_SIZE = 120;

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
    private static final String CLASSROOM_1 = SpurRooms.CLASSROOM_1.getId();

    /** A classroom ID. */
    private static final String CLASSROOM_2 = SpurRooms.CLASSROOM_2.getId();

    /** A classroom ID. */
    private static final String LAB_1 = SpurRooms.LAB_1.getId();

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

        final Map<Course, OfferedCourse> offeredCourses = new HashMap<>(20);

        // SEMINAR

        final OfferedCourse courseSeminar = new OfferedCourse(SpurCourses.SEMINAR);
        offeredCourses.put(SpurCourses.SEMINAR, courseSeminar);

        final Collection<OfferedSection> sectionsSeminar = new ArrayList<>(4);
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.M, CLASSROOM_2,
                        LocalTime.of(15, 0), LocalTime.of(15, 50))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.M, CLASSROOM_2,
                        LocalTime.of(16, 0), LocalTime.of(16, 50))));
        sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.T, CLASSROOM_2,
                        LocalTime.of(8, 30), LocalTime.of(9, 20))));
        if (POPULATION_SIZE > 120) {
            sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                    new OfferedSectionMeetingTime(CLASS, EMeetingDays.R, CLASSROOM_2,
                            LocalTime.of(8, 30), LocalTime.of(9, 20))));
            if (POPULATION_SIZE > 160) {
                sectionsSeminar.add(new OfferedSection(courseSeminar, CLASSROOM_SIZE,
                        new OfferedSectionMeetingTime(CLASS, EMeetingDays.F, CLASSROOM_2,
                                LocalTime.of(16, 0), LocalTime.of(16, 50))));
            }
        }
        courseSeminar.addSectionsList(sectionsSeminar);

        // LIFE 103

        final OfferedCourse courseLife103 = new OfferedCourse(SpurCourses.LIFE103);
        offeredCourses.put(SpurCourses.LIFE103, courseLife103);

        final Collection<OfferedSection> sectionsLife103Class = new ArrayList<>(4);
        sectionsLife103Class.add(new OfferedSection(courseLife103, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        sectionsLife103Class.add(new OfferedSection(courseLife103, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        courseLife103.addSectionsList(sectionsLife103Class);

        final Collection<OfferedSection> sectionsLife103Lab = new ArrayList<>(4);
        sectionsLife103Lab.add(new OfferedSection(courseLife103, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(8, 0), LocalTime.of(10, 45))));
        sectionsLife103Lab.add(new OfferedSection(courseLife103, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(8, 0), LocalTime.of(10, 45))));
        sectionsLife103Lab.add(new OfferedSection(courseLife103, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.F, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(13, 45))));
        if (POPULATION_SIZE > 120) {
            sectionsLife103Lab.add(new OfferedSection(courseLife103, LAB_SIZE,
                    new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                            LocalTime.of(14, 0), LocalTime.of(16, 45))));
        }
        courseLife103.addSectionsList(sectionsLife103Lab);

        // CHEM 111

        final OfferedCourse courseChem111 = new OfferedCourse(SpurCourses.CHEM111);
        offeredCourses.put(SpurCourses.CHEM111, courseChem111);

        final Collection<OfferedSection> sectionsChem111Class = new ArrayList<>(4);
        sectionsChem111Class.add(new OfferedSection(courseChem111, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(11, 50))));
        sectionsChem111Class.add(new OfferedSection(courseChem111, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        sectionsChem111Class.add(new OfferedSection(courseChem111, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(13, 0), LocalTime.of(13, 50))));
        courseChem111.addSectionsList(sectionsChem111Class);

        final Collection<OfferedSection> sectionsChem111Recitation = new ArrayList<>(2);
        sectionsChem111Recitation.add(new OfferedSection(courseChem111, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.W, CLASSROOM_2,
                        LocalTime.of(15, 0), LocalTime.of(15, 50))));
        sectionsChem111Recitation.add(new OfferedSection(courseChem111, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.W, CLASSROOM_2,
                        LocalTime.of(16, 0), LocalTime.of(16, 50))));
        sectionsChem111Recitation.add(new OfferedSection(courseChem111, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(RECITATION, EMeetingDays.F, CLASSROOM_2,
                        LocalTime.of(15, 0), LocalTime.of(15, 50))));
        courseChem111.addSectionsList(sectionsChem111Recitation);

        final Collection<OfferedSection> sectionsChem111Lab = new ArrayList<>(2);
        sectionsChem111Lab.add(new OfferedSection(courseChem111, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(13, 45))));
        sectionsChem111Lab.add(new OfferedSection(courseChem111, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.R, LAB_1,
                        LocalTime.of(11, 0), LocalTime.of(13, 45))));
        sectionsChem111Lab.add(new OfferedSection(courseChem111, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.M, LAB_1,
                        LocalTime.of(14, 0), LocalTime.of(16, 45))));
        sectionsChem111Lab.add(new OfferedSection(courseChem111, LAB_SIZE,
                new OfferedSectionMeetingTime(LAB, EMeetingDays.T, LAB_1,
                        LocalTime.of(14, 0), LocalTime.of(16, 45))));
        courseChem111.addSectionsList(sectionsChem111Lab);

        // SOC 220

        final OfferedCourse courseSoc220 = new OfferedCourse(SpurCourses.SOC220);
        offeredCourses.put(SpurCourses.SOC220, courseSoc220);

        final Collection<OfferedSection> sectionsSoc220Class = new ArrayList<>(2);
        sectionsSoc220Class.add(new OfferedSection(courseSoc220, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(10, 0), LocalTime.of(10, 50))));
        sectionsSoc220Class.add(new OfferedSection(courseSoc220, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(12, 0), LocalTime.of(12, 50))));
        courseSoc220.addSectionsList(sectionsSoc220Class);

        // CS 201

        final OfferedCourse courseCs201 = new OfferedCourse(SpurCourses.CS201);
        offeredCourses.put(SpurCourses.CS201, courseCs201);

        final Collection<OfferedSection> sectionsCs201Class = new ArrayList<>(2);
        sectionsCs201Class.add(new OfferedSection(courseCs201, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(13, 0), LocalTime.of(13, 50))));
        sectionsCs201Class.add(new OfferedSection(courseCs201, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(14, 0), LocalTime.of(14, 50))));
        courseCs201.addSectionsList(sectionsCs201Class);

        // HISTORY

        final OfferedCourse courseHistory = new OfferedCourse(SpurCourses.HISTORY);
        offeredCourses.put(SpurCourses.HISTORY, courseHistory);

        final Collection<OfferedSection> sectionsHistoryClass = new ArrayList<>(2);
        sectionsHistoryClass.add(new OfferedSection(courseHistory, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsHistoryClass.add(new OfferedSection(courseHistory, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(14, 00), LocalTime.of(15, 15))));
        courseHistory.addSectionsList(sectionsHistoryClass);

        // IDEA 210

        final OfferedCourse courseIdea210 = new OfferedCourse(SpurCourses.IDEA210);
        offeredCourses.put(SpurCourses.IDEA210, courseIdea210);

        final Collection<OfferedSection> sectionsIdea210Class = new ArrayList<>(2);
        sectionsIdea210Class.add(new OfferedSection(courseIdea210, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(12, 30), LocalTime.of(13, 45))));
        sectionsIdea210Class.add(new OfferedSection(courseIdea210, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(15, 30), LocalTime.of(16, 45))));
        courseIdea210.addSectionsList(sectionsIdea210Class);

        // IU 173

        final OfferedCourse courseIu173 = new OfferedCourse(SpurCourses.IU173);
        offeredCourses.put(SpurCourses.IU173, courseIu173);

        final Collection<OfferedSection> sectionsIu173Class = new ArrayList<>(2);
        sectionsIu173Class.add(new OfferedSection(courseIu173, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(9, 30), LocalTime.of(10, 45))));
        sectionsIu173Class.add(new OfferedSection(courseIu173, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(12, 30), LocalTime.of(13, 45))));
        sectionsIu173Class.add(new OfferedSection(courseIu173, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(15, 30), LocalTime.of(16, 45))));
        sectionsIu173Class.add(new OfferedSection(courseIu173, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_2,
                        LocalTime.of(14, 0), LocalTime.of(14, 50))));
        courseIu173.addSectionsList(sectionsIu173Class);

        // IU 174

        final OfferedCourse courseIu174 = new OfferedCourse(SpurCourses.IU174);
        offeredCourses.put(SpurCourses.IU174, courseIu174);

        final Collection<OfferedSection> sectionsIu174Class = new ArrayList<>(2);
        sectionsIu174Class.add(new OfferedSection(courseIu174, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(11, 0), LocalTime.of(12, 15))));
        sectionsIu174Class.add(new OfferedSection(courseIu174, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_1,
                        LocalTime.of(14, 0), LocalTime.of(15, 15))));
        sectionsIu174Class.add(new OfferedSection(courseIu174, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.MWF, CLASSROOM_1,
                        LocalTime.of(9, 0), LocalTime.of(9, 50))));
        courseIu174.addSectionsList(sectionsIu174Class);

        // MIP 101

        final OfferedCourse courseMip101 = new OfferedCourse(SpurCourses.MIP101);
        offeredCourses.put(SpurCourses.MIP101, courseMip101);

        final Collection<OfferedSection> sectionsMip101lass = new ArrayList<>(2);
        sectionsMip101lass.add(new OfferedSection(courseMip101, CLASSROOM_SIZE,
                new OfferedSectionMeetingTime(CLASS, EMeetingDays.TR, CLASSROOM_2,
                        LocalTime.of(9, 30), LocalTime.of(10, 45))));
        courseMip101.addSectionsList(sectionsMip101lass);

        // Generate the students who will register

        final StudentPopulation population = new StudentPopulation(SpurStudents.SPUR_SPRING_DISTRIBUTION,
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
