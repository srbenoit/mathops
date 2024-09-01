package dev.mathops.app.sim.registration;

import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A simulation of the Spur first-year Fall semester.
 */
final class SpurFirstYearFall {

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
     * Constructs a new {@code SpurFirstYearProgram}.
     */
    private SpurFirstYearFall() {

        // No action
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        // Set up the available classrooms and labs

        final Room classroom1 = new Room("Classroom 1", 40, HOURS_PER_DAY);
        final Room classroom2 = new Room("Classroom 2", 40, HOURS_PER_DAY);
        final Room classroom3 = new Room("Classroom 3", 40, HOURS_PER_DAY);
        final Room lab1 = new Room("Lab 1", 26, HOURS_PER_DAY);
        final List<Room> rooms = List.of(classroom1, classroom2, classroom3, lab1);

        // Set up the offered course list

        final Course SEMINAR = new Course("SEMINAR", CRED1, true);
        SEMINAR.addRoomType(ERoomUsage.CLASSROOM, 1, EAssignmentType.GROUPS_OF_2, classroom1, classroom2, classroom3);

        final Course LIFE102 = new Course("LIFE 102", CRED3, false);
        LIFE102.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);
        LIFE102.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, lab1);

        final Course MATH112 = new Course("MATH 112", CRED4, false);
        MATH112.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course CS150B = new Course("CS 150B", CRED3, false);
        CS150B.addRoomType(ERoomUsage.CLASSROOM, 2, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);
        LIFE102.addRoomType(ERoomUsage.CLASSROOM, 2, EAssignmentType.CONTIGUOUS, classroom1, classroom2, classroom3);

        final Course IDEA110 = new Course("IDEA 110", CRED3, false);
        IDEA110.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course HDFS101 = new Course("HDFS 101", CRED3, false);
        HDFS101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course AGRI116 = new Course("AGRI 116", CRED3, false);
        AGRI116.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course AB111 = new Course("AB 111", CRED3, false);
        AB111.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course EHRS220 = new Course("EHRS 220", CRED3, false);
        EHRS220.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course POLS131 = new Course("POLS 131", CRED3, false);
        POLS131.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course AREC222 = new Course("AREC 222", CRED3, false);
        AREC222.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course SPCM100 = new Course("SPCM 100", CRED3, false);
        SPCM100.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final Course BZ101 = new Course("BZ 101", CRED3, false);
        BZ101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.GROUPS_OF_2_OR_3, classroom1, classroom2, classroom3);

        final List<Course> immutableCourses = Arrays.asList(LIFE102, MATH112, SEMINAR, CS150B, IDEA110,
                HDFS101, AGRI116, AB111, EHRS220, POLS131, AREC222, SPCM100, BZ101);
        final Collection<Course> courses = new ArrayList<>(immutableCourses);

        // Set up the preferences for each "exploratory studies" track

        final StudentClassPreferences prefs1 = new StudentClassPreferences(HEALTH_LIFE_FOOD, 13, 17);
        prefs1.setPreference(SEMINAR, 1.0);
        prefs1.setPreference(MATH112, 1.0);
        prefs1.setPreference(AGRI116, 0.3);
        prefs1.setPreference(AREC222, 0.3);
        prefs1.setPreference(POLS131, 0.1);
        prefs1.setPreference(AB111, 0.1);
        prefs1.setPreference(BZ101, 0.2);
        prefs1.setPreference(LIFE102, 0.9);
        prefs1.setPreference(EHRS220, 0.1);
        prefs1.setPreference(SPCM100, 0.25);
        prefs1.setPreference(CS150B, 0.25);
        prefs1.setPreference(IDEA110, 0.25);
        prefs1.setPreference(HDFS101, 0.25);

        final StudentClassPreferences prefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        prefs2.setPreference(SEMINAR, 1.0);
        prefs2.setPreference(MATH112, 1.0);
        prefs2.setPreference(AGRI116, 0.4);
        prefs2.setPreference(AREC222, 0.4);
        prefs2.setPreference(POLS131, 0.1);
        prefs2.setPreference(AB111, 0.1);
        prefs2.setPreference(BZ101, 0.2);
        prefs2.setPreference(LIFE102, 0.8);
        prefs2.setPreference(EHRS220, 0.1);
        prefs2.setPreference(SPCM100, 0.25);
        prefs2.setPreference(CS150B, 0.25);
        prefs2.setPreference(IDEA110, 0.2);
        prefs2.setPreference(HDFS101, 0.2);

        final StudentClassPreferences prefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        prefs3.setPreference(SEMINAR, 1.0);
        prefs3.setPreference(MATH112, 1.0);
        prefs3.setPreference(AGRI116, 0.25);
        prefs3.setPreference(AREC222, 0.25);
        prefs3.setPreference(POLS131, 0.25);
        prefs3.setPreference(AB111, 0.1);
        prefs3.setPreference(BZ101, 0.1);
        prefs3.setPreference(LIFE102, 0.9);
        prefs3.setPreference(EHRS220, 0.1);
        prefs3.setPreference(SPCM100, 0.1);
        prefs3.setPreference(CS150B, 0.7);
        prefs3.setPreference(IDEA110, 0.25);
        prefs3.setPreference(HDFS101, 0.1);

        final StudentClassPreferences prefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);
        prefs4.setPreference(SEMINAR, 1.0);
        prefs4.setPreference(MATH112, 1.0);
        prefs4.setPreference(AGRI116, 0.4);
        prefs4.setPreference(AREC222, 0.4);
        prefs4.setPreference(POLS131, 0.1);
        prefs4.setPreference(AB111, 0.2);
        prefs4.setPreference(BZ101, 0.1);
        prefs4.setPreference(LIFE102, 0.7);
        prefs4.setPreference(EHRS220, 0.2);
        prefs4.setPreference(SPCM100, 0.25);
        prefs4.setPreference(CS150B, 0.25);
        prefs4.setPreference(IDEA110, 0.2);
        prefs4.setPreference(HDFS101, 0.2);

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
