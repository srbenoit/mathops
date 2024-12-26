package dev.mathops.app.sim.courses;

import dev.mathops.app.sim.rooms.SpurRooms;
import dev.mathops.app.sim.schedule.EAssignmentType;
import dev.mathops.app.sim.rooms.ERoomUsage;

/**
 * A container for the courses to be offered at Spur in Fall.
 */
public enum SpurCourses {
    ;

    /** A number of credits. */
    private static final int CRED1 = 1;

    /** A number of credits. */
    private static final int CRED3 = 3;

    /** A number of credits. */
    private static final int CRED4 = 4;

    // Courses offered every semester

    // Fall Courses

    public static final Course CO150 = new Course("CO 150", CRED3, 24, false);

    public static final Course PRECALC = new Course("PRECALC", CRED3, 40, false);

    public static final Course KEY175 = new Course("KEY 175", CRED1, 40, true);

    public static final Course LIFE102 = new Course("LIFE 102", CRED4, 40, false);

    public static final Course AB111 = new Course("AB 111", CRED3, 40, false); // Did not run in 2024

    public static final Course ERHS220 = new Course("ERHS 220", CRED3, 40, false);

    public static final Course CS150B = new Course("CS 150B", CRED3, 40, false);

    public static final Course SPCM100 = new Course("SPCM 100", CRED3, 40, false);

    public static final Course IDEA110 = new Course("IDEA 110", CRED3, 40, false);

    public static final Course ECON202 = new Course("ECON 202", CRED3, 40, false);

    public static final Course HDFS101 = new Course("HDFS 101", CRED3, 40, false);

    public static final Course ART100 = new Course("ART 100", CRED3, 40, false);

    // Spring Courses

    public static final Course MATH160 = new Course("MATH 160", CRED4, 40, false);

    public static final Course IU174 = new Course("IU 174", CRED3, 24, false);

    public static final Course SOC220 = new Course("SOC 220", CRED3, 40, false);

    public static final Course LIFE103 = new Course("LIFE 103", CRED3, 40, false);

    public static final Course CHEM111 = new Course("CHEM 111/112", CRED3, 40, false);

    public static final Course IDEA210 = new Course("IDEA 210", CRED3, 40, false);

    public static final Course CS201 = new Course("CS 201", CRED3, 40, false);

    public static final Course HIST15X = new Course("HIST 15x", CRED3, 40, false);

    public static final Course AMST101 = new Course("AMST 101", CRED3, 40, false);

    public static final Course ETST253 = new Course("ETST 253", CRED3, 40, false);

    public static final Course KEY192A = new Course("KEY 192A", CRED1, 40, true);

    public static final Course ETST240 = new Course("ETST 240", CRED3, 40, false);

    static {
        CO150.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        PRECALC.addRoomType(ERoomUsage.CLASSROOM, 4, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        KEY175.addRoomType(ERoomUsage.CLASSROOM, 1, EAssignmentType.BLOCKS_OF_50, SpurRooms.CLASSROOMS);
        LIFE102.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        LIFE102.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, SpurRooms.LABS);
        AB111.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        ERHS220.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CS150B.addRoomType(ERoomUsage.CLASSROOM, 2, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CS150B.addRoomType(ERoomUsage.CLASSROOM, 2, EAssignmentType.CONTIGUOUS, SpurRooms.CLASSROOMS);
        SPCM100.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        IDEA110.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        ECON202.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        HDFS101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        ART100.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);

        MATH160.addRoomType(ERoomUsage.CLASSROOM, 4, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        IU174.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        SOC220.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        LIFE103.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        LIFE103.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, SpurRooms.LABS);
        CHEM111.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CHEM111.addRoomType(ERoomUsage.RECITATION, 1, EAssignmentType.BLOCKS_OF_50, SpurRooms.CLASSROOMS);
        CHEM111.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, SpurRooms.LABS);
        IDEA210.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CS201.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        HIST15X.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        AMST101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        ETST253.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        KEY192A.addRoomType(ERoomUsage.CLASSROOM, 1, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        ETST240.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
    }
}
