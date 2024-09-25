package dev.mathops.app.sim.students;

import dev.mathops.app.sim.courses.SpurCourses;

/**
 * The assumed distribution for the Spur first-year students in Fall.
 */
public enum SpurStudents {
    ;

    /** Flag to control whether MATH 112 is included in Fall (if false, it is offered in Summer). */
    private static final boolean INCLUDE_MATH = false;

    /** Flag to control whether ERHS 220 is included in Fall. */
    private static final boolean INCLUDE_ERHS = true;

    /** Flag to control whether POLS 131 is included in Fall. */
    private static final boolean INCLUDE_POLS = true;

    /** Flag to control whether CO 150 is included in Fall (if false, it is offered in Spring). */
    public static final boolean INCLUDE_FA_CO150 = false;

    /** A class preferences key. */
    private static final String HEALTH_LIFE_FOOD = "HEALTH_LIFE_FOOD";

    /** A class preferences key. */
    private static final String LAND_PLANT_ANIMAL = "LAND_PLANT_ANIMAL";

    /** A class preferences key. */
    private static final String SCIENCE_ENGINEERING = "SCIENCE_ENGINEERING";

    /** A class preferences key. */
    private static final String ENVIRONMENTAL_RES = "ENVIRONMENTAL_RES";

    /** The student preference distribution for Fall. */
    public static final StudentDistribution SPUR_FALL_DISTRIBUTION;

    /** The student preference distribution for Spring. */
    public static final StudentDistribution SPUR_SPRING_DISTRIBUTION;

    static {
        SPUR_FALL_DISTRIBUTION = makeFallDistribution();
        SPUR_SPRING_DISTRIBUTION = makeSpringDistribution();
    }

    /**
     * Constructs the Fall semester student preferences distribution.
     *
     * @return the Fall distribution
     */
    static StudentDistribution makeFallDistribution() {

        final StudentClassPreferences fallPrefs1 = new StudentClassPreferences(HEALTH_LIFE_FOOD, 13, 17);
        fallPrefs1.setPreference(SpurCourses.SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            fallPrefs1.setPreference(SpurCourses.MATH112, 1.0);
        }
        fallPrefs1.setPreference(SpurCourses.AGRI116, 0.3);
        fallPrefs1.setPreference(SpurCourses.AREC222, 0.3);
        if (INCLUDE_POLS) {
            fallPrefs1.setPreference(SpurCourses.POLS131, 0.1);
        }
        fallPrefs1.setPreference(SpurCourses.AB111, 0.1);
        fallPrefs1.setPreference(SpurCourses.BZ101, 0.2);
        fallPrefs1.setPreference(SpurCourses.LIFE102, 0.9);
        if (INCLUDE_ERHS) {
            fallPrefs1.setPreference(SpurCourses.ERHS220, 0.1);
        }
        fallPrefs1.setPreference(SpurCourses.SPCM100, 0.25);
        fallPrefs1.setPreference(SpurCourses.CS150B, 0.25);
        fallPrefs1.setPreference(SpurCourses.IDEA110, 0.25);
        fallPrefs1.setPreference(SpurCourses.HDFS101, 0.25);
        if (INCLUDE_FA_CO150) {
            fallPrefs1.setPreference(SpurCourses.CO150, 1.0);
        }

        final StudentClassPreferences fallPrefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        fallPrefs2.setPreference(SpurCourses.SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            fallPrefs2.setPreference(SpurCourses.MATH112, 1.0);
        }
        fallPrefs2.setPreference(SpurCourses.AGRI116, 0.4);
        fallPrefs2.setPreference(SpurCourses.AREC222, 0.4);
        if (INCLUDE_POLS) {
            fallPrefs2.setPreference(SpurCourses.POLS131, 0.1);
        }
        fallPrefs2.setPreference(SpurCourses.AB111, 0.1);
        fallPrefs2.setPreference(SpurCourses.BZ101, 0.2);
        fallPrefs2.setPreference(SpurCourses.LIFE102, 0.8);
        if (INCLUDE_ERHS) {
            fallPrefs2.setPreference(SpurCourses.ERHS220, 0.1);
        }
        fallPrefs2.setPreference(SpurCourses.SPCM100, 0.25);
        fallPrefs2.setPreference(SpurCourses.CS150B, 0.25);
        fallPrefs2.setPreference(SpurCourses.IDEA110, 0.2);
        fallPrefs2.setPreference(SpurCourses.HDFS101, 0.2);
        if (INCLUDE_FA_CO150) {
            fallPrefs2.setPreference(SpurCourses.CO150, 1.0);
        }

        final StudentClassPreferences fallPrefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        fallPrefs3.setPreference(SpurCourses.SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            fallPrefs3.setPreference(SpurCourses.MATH112, 1.0);
        }
        fallPrefs3.setPreference(SpurCourses.AGRI116, 0.25);
        fallPrefs3.setPreference(SpurCourses.AREC222, 0.25);
        if (INCLUDE_POLS) {
            fallPrefs3.setPreference(SpurCourses.POLS131, 0.25);
        }
        fallPrefs3.setPreference(SpurCourses.AB111, 0.1);
        fallPrefs3.setPreference(SpurCourses.BZ101, 0.1);
        fallPrefs3.setPreference(SpurCourses.LIFE102, 0.9);
        if (INCLUDE_ERHS) {
            fallPrefs3.setPreference(SpurCourses.ERHS220, 0.1);
        }
        fallPrefs3.setPreference(SpurCourses.SPCM100, 0.1);
        fallPrefs3.setPreference(SpurCourses.CS150B, 0.7);
        fallPrefs3.setPreference(SpurCourses.IDEA110, 0.25);
        fallPrefs3.setPreference(SpurCourses.HDFS101, 0.1);
        if (INCLUDE_FA_CO150) {
            fallPrefs3.setPreference(SpurCourses.CO150, 1.0);
        }

        final StudentClassPreferences fallPrefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);
        fallPrefs4.setPreference(SpurCourses.SEMINAR, 1.0);
        if (INCLUDE_MATH) {
            fallPrefs4.setPreference(SpurCourses.MATH112, 1.0);
        }
        fallPrefs4.setPreference(SpurCourses.AGRI116, 0.4);
        fallPrefs4.setPreference(SpurCourses.AREC222, 0.4);
        if (INCLUDE_POLS) {
            fallPrefs4.setPreference(SpurCourses.POLS131, 0.1);
        }
        fallPrefs4.setPreference(SpurCourses.AB111, 0.2);
        fallPrefs4.setPreference(SpurCourses.BZ101, 0.1);
        fallPrefs4.setPreference(SpurCourses.LIFE102, 0.7);
        if (INCLUDE_ERHS) {
            fallPrefs4.setPreference(SpurCourses.ERHS220, 0.2);
        }
        fallPrefs4.setPreference(SpurCourses.SPCM100, 0.25);
        fallPrefs4.setPreference(SpurCourses.CS150B, 0.25);
        fallPrefs4.setPreference(SpurCourses.IDEA110, 0.2);
        fallPrefs4.setPreference(SpurCourses.HDFS101, 0.2);
        if (INCLUDE_FA_CO150) {
            fallPrefs4.setPreference(SpurCourses.CO150, 1.0);
        }

        final StudentDistribution distribution = new StudentDistribution();
        distribution.addGroup(fallPrefs1, 0.411);
        distribution.addGroup(fallPrefs2, 0.142);
        distribution.addGroup(fallPrefs3, 0.265);
        distribution.addGroup(fallPrefs4, 0.182);

        return distribution;
    }

    /**
     * Constructs the Spring semester student preferences distribution.
     *
     * @return the Spring distribution
     */
    static StudentDistribution makeSpringDistribution() {

        final StudentClassPreferences springPrefs1 = new StudentClassPreferences(HEALTH_LIFE_FOOD, 13, 17);
        springPrefs1.setPreference(SpurCourses.SEMINAR, 1.0);
        if (!INCLUDE_FA_CO150) {
            springPrefs1.setPreference(SpurCourses.CO150, 1.0);
        }
        springPrefs1.setPreference(SpurCourses.SOC220, 0.4);
        springPrefs1.setPreference(SpurCourses.LIFE103, 0.55);
        springPrefs1.setPreference(SpurCourses.CHEM111, 0.9);
        springPrefs1.setPreference(SpurCourses.MIP101, 0.25);
        springPrefs1.setPreference(SpurCourses.IDEA210, 0.35);
        springPrefs1.setPreference(SpurCourses.CS201, 0.25);
        springPrefs1.setPreference(SpurCourses.HISTORY, 0.3);
        springPrefs1.setPreference(SpurCourses.IU173, 0.5);
        springPrefs1.setPreference(SpurCourses.IU174, 0.5);

        final StudentClassPreferences springPrefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        springPrefs2.setPreference(SpurCourses.SEMINAR, 1.0);
        if (!INCLUDE_FA_CO150) {
            springPrefs2.setPreference(SpurCourses.CO150, 1.0);
        }
        springPrefs2.setPreference(SpurCourses.SOC220, 0.2);
        springPrefs2.setPreference(SpurCourses.LIFE103, 0.55);
        springPrefs2.setPreference(SpurCourses.CHEM111, 0.9);
        springPrefs2.setPreference(SpurCourses.MIP101, 0.3);
        springPrefs2.setPreference(SpurCourses.IDEA210, 0.35);
        springPrefs2.setPreference(SpurCourses.CS201, 0.25);
        springPrefs2.setPreference(SpurCourses.HISTORY, 0.45);
        springPrefs2.setPreference(SpurCourses.IU173, 0.5);
        springPrefs2.setPreference(SpurCourses.IU174, 0.5);

        final StudentClassPreferences springPrefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        springPrefs3.setPreference(SpurCourses.SEMINAR, 1.0);
        if (!INCLUDE_FA_CO150) {
            springPrefs3.setPreference(SpurCourses.CO150, 1.0);
        }
        springPrefs3.setPreference(SpurCourses.SOC220, 0.25);
        springPrefs3.setPreference(SpurCourses.LIFE103, 0.5);
        springPrefs3.setPreference(SpurCourses.CHEM111, 0.9);
        springPrefs3.setPreference(SpurCourses.MIP101, 0.1);
        springPrefs3.setPreference(SpurCourses.IDEA210, 0.75);
        springPrefs3.setPreference(SpurCourses.CS201, 0.40);
        springPrefs3.setPreference(SpurCourses.HISTORY, 0.2);
        springPrefs3.setPreference(SpurCourses.IU173, 0.5);
        springPrefs3.setPreference(SpurCourses.IU174, 0.5);

        final StudentClassPreferences springPrefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);
        springPrefs4.setPreference(SpurCourses.SEMINAR, 1.0);
        if (!INCLUDE_FA_CO150) {
            springPrefs4.setPreference(SpurCourses.CO150, 1.0);
        }
        springPrefs4.setPreference(SpurCourses.SOC220, 0.4);
        springPrefs4.setPreference(SpurCourses.LIFE103, 0.5);
        springPrefs4.setPreference(SpurCourses.CHEM111, 0.9);
        springPrefs4.setPreference(SpurCourses.MIP101, 0.2);
        springPrefs4.setPreference(SpurCourses.IDEA210, 0.35);
        springPrefs4.setPreference(SpurCourses.CS201, 0.25);
        springPrefs4.setPreference(SpurCourses.HISTORY, 0.4);
        springPrefs4.setPreference(SpurCourses.IU173, 0.5);
        springPrefs4.setPreference(SpurCourses.IU174, 0.5);

        final StudentDistribution distribution = new StudentDistribution();
        distribution.addGroup(springPrefs1, 0.411);
        distribution.addGroup(springPrefs2, 0.142);
        distribution.addGroup(springPrefs3, 0.265);
        distribution.addGroup(springPrefs4, 0.182);

        return distribution;
    }

}
