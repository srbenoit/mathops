package dev.mathops.app.scheduling;

/**
 * A simulation of the Spur first-year Fall semester.
 */
class SpurFirstYearFall {

    /** A section number. */
    private static final int SECT1 = 1;

    /** A section number. */
    private static final int SECT2 = 2;

    /** A section number. */
    private static final int SECT3 = 3;

    /** A number of credits. */
    private static final int CRED1 = 1;

    /** A number of credits. */
    private static final int CRED3 = 3;

    /** A number of credits. */
    private static final int CRED4 = 4;

    /** A number of credits. */
    private static final int CRED5 = 5;

    /** An hour block. */
    private static final int HR1 = 1;

    /** An hour block. */
    private static final int HR2 = 2;

    /** An hour block. */
    private static final int HR3 = 3;

    /** An hour block. */
    private static final int HR4 = 4;

    /** An hour block. */
    private static final int HR5 = 5;

    /** An hour block. */
    private static final int HR6 = 6;

    /** An hour block. */
    private static final int HR7 = 7;

    /** An hour block. */
    private static final int HR8 = 8;

    /** An hour block. */
    private static final int HR9 = 9;

    /**
     * Constructs a new {@code SpurFirstYearProgram}.
     */
    SpurFirstYearFall() {

    }

    /**
     * Runs the simulation.
     */
    void runSimulation() {

        final RegistrationSimulation regSim = new RegistrationSimulation();

        final OfferedSection LIFE102_1 = new OfferedSection("LIFE 102", SECT1, 40, CRED3, OfferedSection.MWF, HR3,
                OfferedSection.WEDNESDAY, HR4, 3);
        final OfferedSection LIFE102_2 = new OfferedSection("LIFE 102", SECT2, 40, CRED3, OfferedSection.MWF, HR7,
                OfferedSection.FRIDAY, HR4, 3);
        final OfferedSection LIFE102_3 = new OfferedSection("LIFE 102", SECT3, 40, CRED3, OfferedSection.TR, HR3,
                OfferedSection.THURSDAY, HR1, 2);

        final OfferedSection MATH112_1 = new OfferedSection("MATH 112", SECT1, 40, CRED3, OfferedSection.MWF, HR2);
        final OfferedSection MATH112_2 = new OfferedSection("MATH 112", SECT2, 40, CRED3, OfferedSection.MWF, HR8);
        final OfferedSection MATH112_3 = new OfferedSection("MATH 112", SECT3, 40, CRED3, OfferedSection.TR, HR4);

        final OfferedSection SEMINAR_1 = new OfferedSection("SEMINAR", SECT1, 40, CRED1, OfferedSection.MONDAY, HR4);
        final OfferedSection SEMINAR_2 = new OfferedSection("SEMINAR", SECT2, 40, CRED1, OfferedSection.MONDAY, HR5);
        final OfferedSection SEMINAR_3 = new OfferedSection("SEMINAR", SECT3, 40, CRED1, OfferedSection.MONDAY, HR6);

        final OfferedSection CS150B_1 = new OfferedSection("CS 150B", SECT1, 40, CRED3, OfferedSection.MWF, HR2);
        final OfferedSection CS150B_2 = new OfferedSection("CS 150B", SECT2, 40, CRED3, OfferedSection.TR, HR5);

        final OfferedSection IDEA_110_1 = new OfferedSection("IDEA 110", SECT1, 40, CRED3, OfferedSection.MWF, HR1);
        final OfferedSection HDFS_101_1 = new OfferedSection("HDFS 101", SECT1, 40, CRED3, OfferedSection.MWF, HR9);
        final OfferedSection AGRI_116_1 = new OfferedSection("AGRI 116", SECT1, 40, CRED3, OfferedSection.MWF, HR3);
        final OfferedSection AB_111_1 = new OfferedSection("AB 111", SECT1, 40, CRED3, OfferedSection.MWF, HR4);
        final OfferedSection EHRS_220_1 = new OfferedSection("EHRS 220", SECT1, 40, CRED3, OfferedSection.MWF, HR5);
        final OfferedSection POLS_131_1 = new OfferedSection("POLS 131", SECT1, 40, CRED3, OfferedSection.MWF, HR6);
        final OfferedSection AREC_222_1 = new OfferedSection("AREC 222", SECT1, 40, CRED3, OfferedSection.MWF, HR7);
        final OfferedSection SPCM_100_1 = new OfferedSection("SPCM 100", SECT1, 40, CRED3, OfferedSection.MWF, HR8);
        final OfferedSection BZ_101_1 = new OfferedSection("BZ 101", SECT1, 40, CRED3, OfferedSection.MWF, HR9);

        regSim.addSections(LIFE102_1, LIFE102_2, LIFE102_3, MATH112_1, MATH112_2, MATH112_3, SEMINAR_1, SEMINAR_2,
                SEMINAR_3, CS150B_1, CS150B_2, IDEA_110_1, HDFS_101_1, AGRI_116_1, AB_111_1, EHRS_220_1, POLS_131_1,
                AREC_222_1, SPCM_100_1, BZ_101_1);

        final ClassPreferences prefsHealthLifeFood = new ClassPreferences();
        final ClassPreferences prefsLandPlantAnimal = new ClassPreferences();
        final ClassPreferences prefsScienceEngineering = new ClassPreferences();
        final ClassPreferences prefsEnvironmental = new ClassPreferences();
        int stuId = 1;

        for (int i = 0; i < 49; ++i) {
            final EnrollingStudent stu = new EnrollingStudent(stuId, 13, 17, prefsHealthLifeFood);
            regSim.addStudents(stu);
            ++stuId;
        }
        for (int i = 0; i < 17; ++i) {
            final EnrollingStudent stu = new EnrollingStudent(stuId, 13, 17, prefsLandPlantAnimal);
            regSim.addStudents(stu);
            ++stuId;
        }
        for (int i = 0; i < 32; ++i) {
            final EnrollingStudent stu = new EnrollingStudent(stuId, 13, 17, prefsScienceEngineering);
            regSim.addStudents(stu);
            ++stuId;
        }
        for (int i = 0; i < 22; ++i) {
            final EnrollingStudent stu = new EnrollingStudent(stuId, 13, 17, prefsEnvironmental);
            regSim.addStudents(stu);
            ++stuId;
        }
        
        regSim.runRegistration();
    }

    /**
     * Main method to execute the simulation.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        new SpurFirstYearFall().runSimulation();
    }
}
