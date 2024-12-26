package dev.mathops.app.sim.students;

import dev.mathops.app.sim.courses.SpurCourses;

/**
 * The assumed distribution for the Spur first-year students in Fall.
 */
public enum SpurStudents {
    ;

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
        fallPrefs1.setPreference(SpurCourses.PRECALC, 0.9);
        fallPrefs1.setPreference(SpurCourses.CO150, 1.0);
        fallPrefs1.setPreference(SpurCourses.KEY175, 1.0);
        fallPrefs1.setPreference(SpurCourses.LIFE102, 0.9);
        fallPrefs1.setPreference(SpurCourses.AB111, 0.1);
        fallPrefs1.setPreference(SpurCourses.ERHS220, 0.1);
        fallPrefs1.setPreference(SpurCourses.CS150B, 0.25);
        fallPrefs1.setPreference(SpurCourses.SPCM100, 0.25);
        fallPrefs1.setPreference(SpurCourses.IDEA110, 0.25);
        fallPrefs1.setPreference(SpurCourses.ECON202, 0.3);
        fallPrefs1.setPreference(SpurCourses.HDFS101, 0.25);
        fallPrefs1.setPreference(SpurCourses.ART100, 0.1);

        final StudentClassPreferences fallPrefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        fallPrefs2.setPreference(SpurCourses.PRECALC, 0.9);
        fallPrefs2.setPreference(SpurCourses.CO150, 1.0);
        fallPrefs2.setPreference(SpurCourses.KEY175, 1.0);
        fallPrefs2.setPreference(SpurCourses.LIFE102, 0.8);
        fallPrefs2.setPreference(SpurCourses.AB111, 0.1);
        fallPrefs2.setPreference(SpurCourses.ERHS220, 0.1);
        fallPrefs2.setPreference(SpurCourses.CS150B, 0.25);
        fallPrefs2.setPreference(SpurCourses.SPCM100, 0.25);
        fallPrefs2.setPreference(SpurCourses.IDEA110, 0.2);
        fallPrefs2.setPreference(SpurCourses.ECON202, 0.4);
        fallPrefs2.setPreference(SpurCourses.HDFS101, 0.2);
        fallPrefs2.setPreference(SpurCourses.ART100, 0.1);

        final StudentClassPreferences fallPrefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        fallPrefs3.setPreference(SpurCourses.PRECALC, 0.9);
        fallPrefs3.setPreference(SpurCourses.CO150, 1.0);
        fallPrefs3.setPreference(SpurCourses.KEY175, 1.0);
        fallPrefs3.setPreference(SpurCourses.LIFE102, 0.9);
        fallPrefs3.setPreference(SpurCourses.AB111, 0.1);
        fallPrefs3.setPreference(SpurCourses.ERHS220, 0.1);
        fallPrefs3.setPreference(SpurCourses.CS150B, 0.7);
        fallPrefs3.setPreference(SpurCourses.SPCM100, 0.1);
        fallPrefs3.setPreference(SpurCourses.IDEA110, 0.25);
        fallPrefs3.setPreference(SpurCourses.ECON202, 0.25);
        fallPrefs3.setPreference(SpurCourses.HDFS101, 0.1);
        fallPrefs3.setPreference(SpurCourses.ART100, 0.25);

        final StudentClassPreferences fallPrefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);
        fallPrefs4.setPreference(SpurCourses.PRECALC, 0.9);
        fallPrefs4.setPreference(SpurCourses.CO150, 1.0);
        fallPrefs4.setPreference(SpurCourses.KEY175, 1.0);
        fallPrefs4.setPreference(SpurCourses.LIFE102, 0.7);
        fallPrefs4.setPreference(SpurCourses.AB111, 0.2);
        fallPrefs4.setPreference(SpurCourses.ERHS220, 0.2);
        fallPrefs4.setPreference(SpurCourses.CS150B, 0.25);
        fallPrefs4.setPreference(SpurCourses.SPCM100, 0.25);
        fallPrefs4.setPreference(SpurCourses.IDEA110, 0.2);
        fallPrefs4.setPreference(SpurCourses.ECON202, 0.4);
        fallPrefs4.setPreference(SpurCourses.HDFS101, 0.2);
        fallPrefs4.setPreference(SpurCourses.ART100, 0.1);

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
        springPrefs1.setPreference(SpurCourses.MATH160, 0.4);
        springPrefs1.setPreference(SpurCourses.IU174, 0.5);
        springPrefs1.setPreference(SpurCourses.SOC220, 0.4);
        springPrefs1.setPreference(SpurCourses.LIFE103, 0.55);
        springPrefs1.setPreference(SpurCourses.CHEM111, 0.9);
        springPrefs1.setPreference(SpurCourses.IDEA210, 0.35);
        springPrefs1.setPreference(SpurCourses.CS201, 0.25);
        springPrefs1.setPreference(SpurCourses.HIST15X, 0.3);
        springPrefs1.setPreference(SpurCourses.AMST101, 0.25);
        springPrefs1.setPreference(SpurCourses.ETST253, 0.25);
        springPrefs1.setPreference(SpurCourses.ETST240, 0.25);
        springPrefs1.setPreference(SpurCourses.KEY192A, 1.0);

        final StudentClassPreferences springPrefs2 = new StudentClassPreferences(LAND_PLANT_ANIMAL, 13, 17);
        springPrefs2.setPreference(SpurCourses.MATH160, 0.4);
        springPrefs2.setPreference(SpurCourses.IU174, 0.5);
        springPrefs2.setPreference(SpurCourses.SOC220, 0.2);
        springPrefs2.setPreference(SpurCourses.LIFE103, 0.55);
        springPrefs2.setPreference(SpurCourses.CHEM111, 0.9);
        springPrefs2.setPreference(SpurCourses.IDEA210, 0.35);
        springPrefs2.setPreference(SpurCourses.CS201, 0.25);
        springPrefs2.setPreference(SpurCourses.HIST15X, 0.45);
        springPrefs2.setPreference(SpurCourses.AMST101, 0.3);
        springPrefs2.setPreference(SpurCourses.ETST253, 0.25);
        springPrefs2.setPreference(SpurCourses.ETST240, 0.25);
        springPrefs2.setPreference(SpurCourses.KEY192A, 1.0);

        final StudentClassPreferences springPrefs3 = new StudentClassPreferences(SCIENCE_ENGINEERING, 13, 17);
        springPrefs3.setPreference(SpurCourses.MATH160, 0.4);
        springPrefs3.setPreference(SpurCourses.IU174, 0.5);
        springPrefs3.setPreference(SpurCourses.SOC220, 0.25);
        springPrefs3.setPreference(SpurCourses.LIFE103, 0.5);
        springPrefs3.setPreference(SpurCourses.CHEM111, 0.9);
        springPrefs3.setPreference(SpurCourses.IDEA210, 0.75);
        springPrefs3.setPreference(SpurCourses.CS201, 0.40);
        springPrefs3.setPreference(SpurCourses.HIST15X, 0.2);
        springPrefs3.setPreference(SpurCourses.AMST101, 0.1);
        springPrefs3.setPreference(SpurCourses.ETST253, 0.25);
        springPrefs3.setPreference(SpurCourses.ETST240, 0.25);
        springPrefs3.setPreference(SpurCourses.KEY192A, 1.0);

        final StudentClassPreferences springPrefs4 = new StudentClassPreferences(ENVIRONMENTAL_RES, 13, 17);
        springPrefs4.setPreference(SpurCourses.MATH160, 0.4);
        springPrefs4.setPreference(SpurCourses.IU174, 0.5);
        springPrefs4.setPreference(SpurCourses.SOC220, 0.4);
        springPrefs4.setPreference(SpurCourses.LIFE103, 0.5);
        springPrefs4.setPreference(SpurCourses.CHEM111, 0.9);
        springPrefs4.setPreference(SpurCourses.IDEA210, 0.35);
        springPrefs4.setPreference(SpurCourses.CS201, 0.25);
        springPrefs4.setPreference(SpurCourses.HIST15X, 0.4);
        springPrefs4.setPreference(SpurCourses.AMST101, 0.2);
        springPrefs4.setPreference(SpurCourses.ETST253, 0.25);
        springPrefs4.setPreference(SpurCourses.ETST240, 0.25);
        springPrefs4.setPreference(SpurCourses.KEY192A, 1.0);

        final StudentDistribution distribution = new StudentDistribution();
        distribution.addGroup(springPrefs1, 0.411);
        distribution.addGroup(springPrefs2, 0.142);
        distribution.addGroup(springPrefs3, 0.265);
        distribution.addGroup(springPrefs4, 0.182);

        return distribution;
    }
}
