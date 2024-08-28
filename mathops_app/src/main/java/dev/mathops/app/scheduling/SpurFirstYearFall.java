package dev.mathops.app.scheduling;

import java.util.Arrays;
import java.util.List;

/**
 * A simulation of the Spur first-year Fall semester.
 */
final class SpurFirstYearFall {

    /** A class preferences key. */
    private static final String HEALTH_LIFE_FOOD = "A";

    /** A class preferences key. */
    private static final String LAND_PLANT_ANIMAL = "B";

    /** A class preferences key. */
    private static final String SCIENCE_ENGINEERING = "C";

    /** A class preferences key. */
    private static final String ENVIRONMENTAL_RES = "D";

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

        // Set up the preferences for each "exploratory studies" track

        final StudentClassPreferences prefs1 = new StudentClassPreferences(HEALTH_LIFE_FOOD, 13, 17);
        final StudentClassPreferences prefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        final StudentClassPreferences prefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        final StudentClassPreferences prefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);

        // Set up the student distribution

        final StudentDistribution distribution = new StudentDistribution();
        distribution.addGroup(prefs1, 0.411);
        distribution.addGroup(prefs2, 0.142);
        distribution.addGroup(prefs3, 0.265);
        distribution.addGroup(prefs4, 0.182);

        // Set up the available classrooms and labs

        final AvailableClassroom classroom1 = new AvailableClassroom(1, HOURS_PER_DAY, 40);
        final AvailableClassroom classroom2 = new AvailableClassroom(2, HOURS_PER_DAY, 40);
        final AvailableLab lab1 = new AvailableLab(100, HOURS_PER_DAY, 20);

        // Set up lists of classrooms and labs that are "compatible" with classes

        final List<AvailableClassroom> allClassrooms = Arrays.asList(classroom1, classroom2);
        final List<AvailableLab> allLabs = List.of(lab1);

        // Set up the offered course list

        final OfferedCourse LIFE102 = new OfferedCourse("LIFE 102", CRED3, 3, allClassrooms, 3, allLabs);
        final OfferedCourse MATH112 = new OfferedCourse("MATH 112", CRED4, 3, allClassrooms);
        final OfferedCourse SEMINAR = new OfferedCourse("SEMINAR", CRED1, 1, allClassrooms);
        final OfferedCourse CS150B = new OfferedCourse("CS 150B", CRED3, 3, allClassrooms);
        final OfferedCourse IDEA_110 = new OfferedCourse("IDEA 110", CRED3, 3, allClassrooms);
        final OfferedCourse HDFS_101 = new OfferedCourse("HDFS 101", CRED3, 3, allClassrooms);
        final OfferedCourse AGRI_116 = new OfferedCourse("AGRI 116", CRED3, 3, allClassrooms);
        final OfferedCourse AB_111 = new OfferedCourse("AB 111", CRED3, 3, allClassrooms);
        final OfferedCourse EHRS_220 = new OfferedCourse("EHRS 220", CRED3, 3, allClassrooms);
        final OfferedCourse POLS_131 = new OfferedCourse("POLS 131", CRED3, 3, allClassrooms);
        final OfferedCourse AREC_222 = new OfferedCourse("AREC 222", CRED3, 3, allClassrooms);
        final OfferedCourse SPCM_100 = new OfferedCourse("SPCM 100", CRED3, 3, allClassrooms);
        final OfferedCourse BZ_101 = new OfferedCourse("BZ 101", CRED3, 3, allClassrooms);

        // SIMULATION PART 1 - DETERMINE MAXIMUM POSSIBLE POPULATION SIZE

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
