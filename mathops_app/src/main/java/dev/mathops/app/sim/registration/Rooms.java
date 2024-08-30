package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for a collection of rooms that can try to find assignments for specific classes from its set of rooms.
 */
final class Rooms {

    /** The list of rooms (immutable). */
    private final List<Room> rooms;

    /**
     * Constructs a new {@code Rooms}.
     *
     * @param theRooms the rooms
     */
    Rooms(final Room... theRooms) {

        this.rooms = theRooms == null ? new ArrayList<>(0) : List.of(theRooms);
    }

    /**
     * Attempts to find a room that can handle a course.
     *
     * @param course the course
     * @return true if the course was assigned; false if not
     */
    public boolean canBeAssigned(final OfferedCourse course) {

    }
}
