package dev.mathops.app.sim.registration;

import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A simulation of the Spur first-year Spring semester.
 */
final class SpurFirstYearSpring {

    /** A class preferences key. */
    private static final String HEALTH_LIFE_FOOD = "HEALTH_LIFE_FOOD";

    /** A class preferences key. */
    private static final String LAND_PLANT_ANIMAL = "LAND_PLANT_ANIMAL";

    /** A class preferences key. */
    private static final String SCIENCE_ENGINEERING = "SCIENCE_ENGINEERING";

    /** A class preferences key. */
    private static final String ENVIRONMENTAL_RES = "ENVIRONMENTAL_RES";

    /** A number of credits. */
    private static final int CRED1 = 1;

    /** A number of credits. */
    private static final int CRED3 = 3;

    /** A number of credits. */
    private static final int CRED4 = 4;

    /** A number of hours the facility is open per day. */
    private static final int HOURS_PER_DAY = 9;

    /**
     * Constructs a new {@code SpurFirstYearSpring}.
     */
    private SpurFirstYearSpring() {

        // No action
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        // Set up the available classrooms and labs

        final Room classroom1 = new Room("Classroom 1", 40, HOURS_PER_DAY);
        final Room classroom2 = new Room("Classroom 2", 40, HOURS_PER_DAY);
//        final Room classroom3 = new Room("Classroom 3", 40, HOURS_PER_DAY);
        final Room[] classrooms = {classroom1, classroom2};

        final Room lab1 = new Room("Lab 1", 26, HOURS_PER_DAY);
        final Room[] labs = {lab1};

        final List<Room> rooms = List.of(classroom1, classroom2, lab1);

        // Set up the offered course list

        final Course SEMINAR = new Course("SEMINAR", CRED1, true);
        SEMINAR.addRoomType(ERoomUsage.CLASSROOM, 1, EAssignmentType.GROUPS_OF_2, classrooms);

        final Course CO150 = new Course("CO 150", CRED3, false);
        CO150.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);

        final Course SOC220 = new Course("SOC 220", CRED3, false);
        SOC220.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);

        final Course LIFE103 = new Course("LIFE 103", CRED3, false);
        LIFE103.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);
        LIFE103.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, labs);

        final Course CHEM111 = new Course("CHEM 111/112", CRED3, false);
        CHEM111.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);
        CHEM111.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, labs);

        final Course MIP101 = new Course("MIP 101", CRED3, false);
        MIP101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);

        final Course IDEA210 = new Course("IDEA 210", CRED3, false);
        MIP101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);

        final Course CS201 = new Course("CS 201", CRED3, false);
        MIP101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);

        final Course HISTORY = new Course("HISTORY", CRED3, false);
        MIP101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);

        final Course IU173 = new Course("IU 173", CRED3, false);
        MIP101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);

        final Course IU174 = new Course("IU 174", CRED3, false);
        MIP101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classrooms);

        final List<Course> immutableCourses = Arrays.asList(SEMINAR, CO150, SOC220, LIFE103, CHEM111, MIP101, IDEA210,
                CS201, HISTORY, IU173, IU174);
        final Collection<Course> courses = new ArrayList<>(immutableCourses);

        // Set up the preferences for each "exploratory studies" track

        final StudentClassPreferences prefs1 = new StudentClassPreferences(HEALTH_LIFE_FOOD, 13, 17);
        prefs1.setPreference(SEMINAR, 1.0);
        prefs1.setPreference(CO150, 1.0);
        prefs1.setPreference(SOC220, 0.4);
        prefs1.setPreference(LIFE103, 0.55);
        prefs1.setPreference(CHEM111, 0.9);
        prefs1.setPreference(MIP101, 0.25);
        prefs1.setPreference(IDEA210, 0.35);
        prefs1.setPreference(CS201, 0.25);
        prefs1.setPreference(HISTORY, 0.3);
        prefs1.setPreference(IU173, 0.5);
        prefs1.setPreference(IU174, 0.5);

        final StudentClassPreferences prefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        prefs2.setPreference(SEMINAR, 1.0);
        prefs2.setPreference(CO150, 1.0);
        prefs2.setPreference(SOC220, 0.2);
        prefs2.setPreference(LIFE103, 0.55);
        prefs2.setPreference(CHEM111, 0.9);
        prefs2.setPreference(MIP101, 0.3);
        prefs2.setPreference(IDEA210, 0.35);
        prefs2.setPreference(CS201, 0.25);
        prefs2.setPreference(HISTORY, 0.45);
        prefs2.setPreference(IU173, 0.5);
        prefs2.setPreference(IU174, 0.5);

        final StudentClassPreferences prefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        prefs3.setPreference(SEMINAR, 1.0);
        prefs3.setPreference(CO150, 1.0);
        prefs3.setPreference(SOC220, 0.25);
        prefs3.setPreference(LIFE103, 0.5);
        prefs3.setPreference(CHEM111, 0.9);
        prefs3.setPreference(MIP101, 0.1);
        prefs3.setPreference(IDEA210, 0.75);
        prefs3.setPreference(CS201, 0.40);
        prefs3.setPreference(HISTORY, 0.2);
        prefs3.setPreference(IU173, 0.5);
        prefs3.setPreference(IU174, 0.5);

        final StudentClassPreferences prefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);
        prefs4.setPreference(SEMINAR, 1.0);
        prefs4.setPreference(CO150, 1.0);
        prefs4.setPreference(SOC220, 0.4);
        prefs4.setPreference(LIFE103, 0.5);
        prefs4.setPreference(CHEM111, 0.9);
        prefs4.setPreference(MIP101, 0.2);
        prefs4.setPreference(IDEA210, 0.35);
        prefs4.setPreference(CS201, 0.25);
        prefs4.setPreference(HISTORY, 0.4);
        prefs4.setPreference(IU173, 0.5);
        prefs4.setPreference(IU174, 0.5);

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

//        final int hoursRemaining = ComputeSectionRoomAssignments.compute(courses, allClassrooms, allLabs);
//
//        if (hoursRemaining < 0) {
//            Log.warning("Unable to allocate course sections to classrooms and labs.");
//        } else {
//            Log.info("Sections have been allocated to classrooms and labs.");
//        }
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
