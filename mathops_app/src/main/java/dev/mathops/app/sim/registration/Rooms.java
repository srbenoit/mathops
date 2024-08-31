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

        if (theRooms == null || theRooms.length == 0) {
            this.rooms = new ArrayList<>(0);
        } else {
            final List<Room> unmodifiable = List.of(theRooms);
            this.rooms = new ArrayList<>(unmodifiable);
            this.rooms.sort(null);
        }
    }

    /**
     * Gets the list of rooms.
     *
     * @return the list of rooms
     */
    List<Room> getRooms() {

        return new ArrayList<>(this.rooms);
    }

    /**
     * Calculates the total capacity of all rooms in this group.
     *
     * @return the total capacity
     */
    int totalCapacity() {

        int total = 0;

        for (final Room room : this.rooms) {
            total += room.getCapacity();
        }

        return total;
    }

    /**
     * Calculates the total number of free hours of all rooms in this group.
     *
     * @return the total number of hours free
     */
    int totalHoursFree() {

        int total = 0;

        for (final Room room : this.rooms) {
            total += room.getTotalHoursFree();
        }

        return total;
    }

    /**
     * Clears the lists of assigned sections for each room.
     */
    void reset() {

        for (final Room room : this.rooms) {
            room.clearAssignments();
        }
    }

    /**
     * Attempts to find a room that can handle a course.
     *
     * @param course the course
     * @return true if the course was assigned; false if not
     */
    public boolean canBeAssigned(final Course course) {

    }
}
