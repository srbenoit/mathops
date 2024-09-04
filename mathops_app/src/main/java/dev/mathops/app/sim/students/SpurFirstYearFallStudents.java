package dev.mathops.app.sim.students;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.commons.log.Log;

import java.util.Map;

/**
 * The assumed distribution for the Spur first-year students in Fall.
 */
final class SpurFirstYearFallStudents {

    /** Flag to control whether MATH 112 is included in Fall (if false, it is offered in Summer). */
    private static final boolean INCLUDE_MATH = false;

    /** Flag to control whether EHRS 220 is included in Fall (if false, it is offered in Summer). */
    private static final boolean INCLUDE_EHRS = false;

    /** Flag to control whether POLS 131 is included in Fall (if false, it is offered in Summer). */
    private static final boolean INCLUDE_POLS = false;

    /** A class preferences key. */
    private static final String HEALTH_LIFE_FOOD = "HEALTH_LIFE_FOOD";

    /** A class preferences key. */
    private static final String LAND_PLANT_ANIMAL = "LAND_PLANT_ANIMAL";

    /** A class preferences key. */
    private static final String SCIENCE_ENGINEERING = "SCIENCE_ENGINEERING";

    /** A class preferences key. */
    private static final String ENVIRONMENTAL_RES = "ENVIRONMENTAL_RES";


    /**
     * Constructs a new {@code SpurFirstYearFallStudents}.
     */
    private SpurFirstYearFallStudents() {

        // No action
    }

    /**
     * Runs the simulation.
     */
    private static void runSimulation() {

        final StudentClassPreferences prefs1 = new StudentClassPreferences(HEALTH_LIFE_FOOD, 13, 17);
        prefs1.setPreference(SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            prefs1.setPreference(MATH112, 1.0);
        }
        prefs1.setPreference(AGRI116, 0.3);
        prefs1.setPreference(AREC222, 0.3);
        if (INCLUDE_POLS) {
            prefs1.setPreference(POLS131, 0.1);
        }
        prefs1.setPreference(AB111, 0.1);
        prefs1.setPreference(BZ101, 0.2);
        prefs1.setPreference(LIFE102, 0.9);
        if (INCLUDE_EHRS) {
            prefs1.setPreference(EHRS220, 0.1);
        }
        prefs1.setPreference(SPCM100, 0.25);
        prefs1.setPreference(CS150B, 0.25);
        prefs1.setPreference(IDEA110, 0.25);
        prefs1.setPreference(HDFS101, 0.25);

        final StudentClassPreferences prefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        prefs2.setPreference(SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            prefs2.setPreference(MATH112, 1.0);
        }
        prefs2.setPreference(AGRI116, 0.4);
        prefs2.setPreference(AREC222, 0.4);
        if (INCLUDE_POLS) {
            prefs2.setPreference(POLS131, 0.1);
        }
        prefs2.setPreference(AB111, 0.1);
        prefs2.setPreference(BZ101, 0.2);
        prefs2.setPreference(LIFE102, 0.8);
        if (INCLUDE_EHRS) {
            prefs2.setPreference(EHRS220, 0.1);
        }
        prefs2.setPreference(SPCM100, 0.25);
        prefs2.setPreference(CS150B, 0.25);
        prefs2.setPreference(IDEA110, 0.2);
        prefs2.setPreference(HDFS101, 0.2);

        final StudentClassPreferences prefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        prefs3.setPreference(SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            prefs3.setPreference(MATH112, 1.0);
        }
        prefs3.setPreference(AGRI116, 0.25);
        prefs3.setPreference(AREC222, 0.25);
        if (INCLUDE_POLS) {
            prefs3.setPreference(POLS131, 0.25);
        }
        prefs3.setPreference(AB111, 0.1);
        prefs3.setPreference(BZ101, 0.1);
        prefs3.setPreference(LIFE102, 0.9);
        if (INCLUDE_EHRS) {
            prefs3.setPreference(EHRS220, 0.1);
        }
        prefs3.setPreference(SPCM100, 0.1);
        prefs3.setPreference(CS150B, 0.7);
        prefs3.setPreference(IDEA110, 0.25);
        prefs3.setPreference(HDFS101, 0.1);

        final StudentClassPreferences prefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);
        prefs4.setPreference(SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            prefs4.setPreference(MATH112, 1.0);
        }
        prefs4.setPreference(AGRI116, 0.4);
        prefs4.setPreference(AREC222, 0.4);
        if (INCLUDE_POLS) {
            prefs4.setPreference(POLS131, 0.1);
        }
        prefs4.setPreference(AB111, 0.2);
        prefs4.setPreference(BZ101, 0.1);
        prefs4.setPreference(LIFE102, 0.7);
        if (INCLUDE_EHRS) {
            prefs4.setPreference(EHRS220, 0.2);
        }
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
