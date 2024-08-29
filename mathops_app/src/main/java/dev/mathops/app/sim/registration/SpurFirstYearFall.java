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

        final AvailableClassroom classroom1 = new AvailableClassroom(1, HOURS_PER_DAY, 40);
        final AvailableClassroom classroom2 = new AvailableClassroom(2, HOURS_PER_DAY, 40);
        final AvailableLab lab1 = new AvailableLab(100, HOURS_PER_DAY, 26);

        // Set up lists of classrooms and labs that are "compatible" with classes

        final List<AvailableClassroom> immutableClassrooms = Arrays.asList(classroom1, classroom2);
        final List<AvailableClassroom> allClassrooms = new ArrayList<>(immutableClassrooms);

        final List<AvailableLab> immutableLabs = List.of(lab1);
        final List<AvailableLab> allLabs = new ArrayList<>(immutableLabs);

        // Set up the offered course list

        final OfferedCourse SEMINAR = new OfferedCourse("SEMINAR", CRED1, true, 1, allClassrooms);
        final OfferedCourse LIFE102 = new OfferedCourse("LIFE 102", CRED3, false, 3, allClassrooms, 3, allLabs);
        final OfferedCourse MATH112 = new OfferedCourse("MATH 112", CRED4, false, 3, allClassrooms);
        final OfferedCourse CS150B = new OfferedCourse("CS 150B", CRED3, false, 3, allClassrooms);
        final OfferedCourse IDEA110 = new OfferedCourse("IDEA 110", CRED3, false, 3, allClassrooms);
        final OfferedCourse HDFS101 = new OfferedCourse("HDFS 101", CRED3, false, 3, allClassrooms);
        final OfferedCourse AGRI116 = new OfferedCourse("AGRI 116", CRED3, false, 3, allClassrooms);
        final OfferedCourse AB111 = new OfferedCourse("AB 111", CRED3, false, 3, allClassrooms);
        final OfferedCourse EHRS220 = new OfferedCourse("EHRS 220", CRED3, false, 3, allClassrooms);
        final OfferedCourse POLS131 = new OfferedCourse("POLS 131", CRED3, false, 3, allClassrooms);
        final OfferedCourse AREC222 = new OfferedCourse("AREC 222", CRED3, false, 3, allClassrooms);
        final OfferedCourse SPCM100 = new OfferedCourse("SPCM 100", CRED3, false, 3, allClassrooms);
        final OfferedCourse BZ101 = new OfferedCourse("BZ 101", CRED3, false, 3, allClassrooms);
        final List<OfferedCourse> immutableCourses = Arrays.asList(LIFE102, MATH112, SEMINAR, CS150B, IDEA110,
                HDFS101, AGRI116, AB111, EHRS220, POLS131, AREC222, SPCM100, BZ101);
        final Collection<OfferedCourse> courses = new ArrayList<>(immutableCourses);

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

        final int maxPopulation = ComputePopulationSize.compute(courses, distribution, allClassrooms, allLabs);
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
