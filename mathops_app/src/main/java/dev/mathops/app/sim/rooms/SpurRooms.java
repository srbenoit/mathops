package dev.mathops.app.sim.rooms;

import java.util.Collection;
import java.util.List;

/**
 * A container for the rooms available at Spur.
 */
public enum SpurRooms {
    ;

    /** A number of blocks the facility is open per day on MWF. */
    private static final int BLOCKS_PER_DAY_MWF = 9;

    /** A number of blocks the facility is open per day on TR. */
    private static final int BLOCKS_PER_DAY_TR = 6;

    /** Classroom 1. */
    public static final Room CLASSROOM_1 = new Room("Classroom 1", 40, BLOCKS_PER_DAY_MWF, BLOCKS_PER_DAY_TR);

    /** Classroom 2. */
    public static final Room CLASSROOM_2 = new Room("Classroom 2", 40, BLOCKS_PER_DAY_MWF, BLOCKS_PER_DAY_TR);

    /** Lab 1. */
    public static final Room LAB_1 = new Room("Lab 1", 26, BLOCKS_PER_DAY_MWF, BLOCKS_PER_DAY_TR);

    /** The collection of classrooms. */
    public static final Collection<Room> CLASSROOMS = List.of(CLASSROOM_1, CLASSROOM_2);

    /** The collection of labs. */
    public static final Collection<Room> LABS = List.of(LAB_1);
}
