package dev.mathops.app.sim.registration.rooms;

import java.util.ArrayList;
import java.util.List;

/**
 * A model of a room in which classes or labs can be scheduled.
 */
public final class Room implements Comparable<Room> {

    /** The unique classroom ID. */
    final int id;

    /** The seating capacity. */
    final int capacity;

    /** The number of hours per day the classroom is available. */
    final int hoursPerDay;

    /**
     * The list of "assignments" currently active, including the number of hours per week, and whether it needs to be
     * contiguous.
     */
    private final List<RoomAssignment> assignments;

    /**
     * A grid of 25-minute blocks available to schedule classes or labs.  A "1 contact hour" block would consume two of
     * these (adjacent).  An "N contact hour contiguous" block would consume 2N of these (contiguous). An "N contact
     * hour" block will consume 2N of these, but they could be arranged in pairs or threes over multiple days.  Each
     * entry in this grid is the "assignment number" that is allocated to those blocks.  This class is free to move the
     * blocks associated with an "assignment number" around to try to accommodate requests for new assignments, but can
     * (on request) provide the current set of blocks occupied by any assignment.
     */
    private final int[][] timeBlockGrid;

    /**
     * Constructs a new {@code Room}.
     *
     * @param theId          the unique classroom ID
     * @param theCapacity    the seating capacity
     * @param theHoursPerDay the number of hours per day the classroom is available
     */
    Room(final int theId, final int theCapacity, final int theHoursPerDay) {

        if (theCapacity < 1) {
            throw new IllegalArgumentException("Room capacity may not be less than 1");
        }
        if (theHoursPerDay < 1) {
            throw new IllegalArgumentException("Number of hours per day may not be less than 1");
        }

        this.id = theId;
        this.capacity = theCapacity;
        this.hoursPerDay = theHoursPerDay;

        this.assignments = new ArrayList<>(100);
        this.timeBlockGrid = new int[5][theHoursPerDay * 2];
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.id;
    }

    /**
     * Tests whether this object is equal to another.  Equality of this class is tested only on equality of the unique
     * course ID.
     *
     * @param obj the other object
     * @return true if this object is equal
     */
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final AvailableClassroomOld classroom) {
            equal = this.id == classroom.id;
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object to another for order.
     *
     * @param o the object to be compared
     * @return the value 0 if this object's capacity equals the other object's capacity a value less than 0 if this
     *         object's capacity is less than that of the other; and a value greater than 0 if this object's capacity is
     *         greater than that of the other
     */
    @Override
    public int compareTo(final Room o) {

        return Integer.compare(this.capacity, o.capacity);
    }
}
