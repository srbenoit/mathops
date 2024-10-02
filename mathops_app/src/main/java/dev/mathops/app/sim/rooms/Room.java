package dev.mathops.app.sim.rooms;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An immutable model of a room in which sections can be scheduled.
 */
public final class Room implements Comparable<Room> {

    /** The unique room ID. */
    private final String id;

    /** The seating capacity. */
    private final int capacity;

    /**
     * Constructs a new {@code Room}.
     *
     * @param theId       the unique room ID
     * @param theCapacity the seating capacity
     */
    public Room(final String theId, final int theCapacity) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Room ID may not be null or blank");
        }
        if (theCapacity < 1) {
            throw new IllegalArgumentException("Room capacity may not be less than 1");
        }

        this.id = theId;
        this.capacity = theCapacity;
    }

    /**
     * Gets the unique room ID.
     *
     * @return the room ID
     */
    public String getId() {

        return this.id;
    }

    /**
     * Gets the seating capacity.
     *
     * @return the seating capacity
     */
    public int getCapacity() {

        return this.capacity;
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.id.hashCode();
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
        } else if (obj instanceof final Room room) {
            final String objId = room.getId();
            equal = this.id.equals(objId);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object to another for order.  Ordering is based on room capacity.
     *
     * @param o the object to be compared
     * @return the value 0 if this object's capacity equals the other object's capacity a value less than 0 if this
     *         object's capacity is less than that of the other; and a value greater than 0 if this object's capacity is
     *         greater than that of the other
     */
    @Override
    public int compareTo(final Room o) {

        final int cap = o.getCapacity();

        return Integer.compare(this.capacity, cap);
    }

    /**
     * Generates a string representation of the list of rooms.
     *
     * @return the string representation
     */
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add(this.id);
        builder.add(" (cap=");
        builder.add(this.capacity);
        builder.add(')');

        return builder.toString();
    }
}
