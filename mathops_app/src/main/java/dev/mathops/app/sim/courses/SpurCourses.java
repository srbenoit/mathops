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

    public static final Course SEMINAR = new Course("SEMINAR", CRED1, 40, true);

    // Fall Courses

    public static final Course LIFE102 = new Course("LIFE 102", CRED4, 40, false);

    public static final Course MATH112 = new Course("MATH 112", CRED3, 40, false);

    public static final Course CS150B = new Course("CS 150B", CRED3, 40, false);

    public static final Course IDEA110 = new Course("IDEA 110", CRED3, 40, false);

    public static final Course HDFS101 = new Course("HDFS 101", CRED3, 40, false);

    public static final Course AGRI116 = new Course("AGRI 116", CRED3, 40, false); // Did not run in 2024

    public static final Course AB111 = new Course("AB 111", CRED3, 40, false); // Did not run in 2024

    public static final Course ERHS220 = new Course("ERHS 220", CRED3, 40, false);

    public static final Course POLS131 = new Course("POLS 131", CRED3, 40, false);

    public static final Course AREC222 = new Course("AREC 222", CRED3, 40, false);

    public static final Course SPCM100 = new Course("SPCM 100", CRED3, 40, false);

    public static final Course BZ101 = new Course("BZ 101", CRED3, 40, false);

    // Spring CoursesAGRI

    public static final Course CO150 = new Course("CO 150", CRED3, 24, false);

    public static final Course SOC220 = new Course("SOC 220", CRED3, 40, false);

    public static final Course LIFE103 = new Course("LIFE 103", CRED3, 40, false);

    public static final Course CHEM111 = new Course("CHEM 111/112", CRED3, 40, false);

    public static final Course IDEA210 = new Course("IDEA 210", CRED3, 40, false);

    public static final Course MIP101 = new Course("MIP 101", CRED3, 40, false);

    public static final Course CS201 = new Course("CS 201", CRED3, 40, false);

    public static final Course HISTORY = new Course("HISTORY", CRED3, 40, false);

    public static final Course IU173 = new Course("IU 173", CRED3, 24, false);

    public static final Course IU174 = new Course("IU 174", CRED3, 24, false);

    static {
        SEMINAR.addRoomType(ERoomUsage.CLASSROOM, 1, EAssignmentType.BLOCKS_OF_50, SpurRooms.CLASSROOMS);

        LIFE102.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        LIFE102.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, SpurRooms.LABS);
        MATH112.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CS150B.addRoomType(ERoomUsage.CLASSROOM, 2, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CS150B.addRoomType(ERoomUsage.CLASSROOM, 2, EAssignmentType.CONTIGUOUS, SpurRooms.CLASSROOMS);
        IDEA110.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        HDFS101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        AGRI116.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        AB111.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        ERHS220.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        POLS131.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        AREC222.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        SPCM100.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        BZ101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);

        LIFE103.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, SpurRooms.LABS);
        SEMINAR.addRoomType(ERoomUsage.CLASSROOM, 1, EAssignmentType.BLOCKS_OF_50, SpurRooms.CLASSROOMS);
        CO150.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        SOC220.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        LIFE103.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CHEM111.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CHEM111.addRoomType(ERoomUsage.RECITATION, 1, EAssignmentType.BLOCKS_OF_50, SpurRooms.CLASSROOMS);
        CHEM111.addRoomType(ERoomUsage.LAB, 3, EAssignmentType.CONTIGUOUS, SpurRooms.LABS);
        IDEA210.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        MIP101.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        CS201.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        HISTORY.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        IU173.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
        IU174.addRoomType(ERoomUsage.CLASSROOM, 3, EAssignmentType.BLOCKS_OF_50_OR_75, SpurRooms.CLASSROOMS);
    }
}
