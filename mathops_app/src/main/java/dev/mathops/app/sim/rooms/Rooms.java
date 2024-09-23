package dev.mathops.app.sim.rooms;

import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A container for a collection of rooms that can try to find assignments for specific classes from its set of rooms.
 */
public final class Rooms implements Comparable<Rooms> {

    /** The list of rooms (immutable). */
    private final List<Room> rooms;

    /**
     * Constructs a new {@code Rooms}.
     *
     * @param theRooms the rooms
     */
    public Rooms(final Room... theRooms) {

        if (theRooms == null || theRooms.length == 0) {
            this.rooms = new ArrayList<>(0);
        } else {
            final List<Room> unmodifiable = List.of(theRooms);
            this.rooms = new ArrayList<>(unmodifiable);
            this.rooms.sort(null);
        }
    }

    /**
     * Gets a copy of the list of rooms.
     *
     * @param list the list of rooms
     */
    public void getRooms(final Collection<? super Room> list) {

        list.addAll(this.rooms);
    }

    /**
     * Gets the list of rooms.
     *
     * @return the list of rooms
     */
    private List<Room> innerGetRooms() {

        return this.rooms;
    }

    /**
     * Calculates the total capacity of all rooms in this group.
     *
     * @return the total capacity
     */
    public int totalCapacity() {

        int total = 0;

        for (final Room room : this.rooms) {
            total += room.getCampusRoom().getCapacity();
        }

        return total;
    }

    /**
     * Calculates the total number of free hours of all rooms in this group.
     *
     * @return the total number of hours free
     */
    public int totalHoursFree() {

        int total = 0;

        for (final Room room : this.rooms) {
            total += room.getTotalBlocksFree();
        }

        return total;
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.rooms.hashCode();
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
        } else if (obj instanceof final Rooms other) {
            final List<Room> otherRooms = other.innerGetRooms();
            equal = this.rooms.equals(otherRooms);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object to another for order.  Ordering is based on total capacity.
     *
     * @param o the object to be compared
     * @return the value 0 if this object's capacity equals the other object's capacity a value less than 0 if this
     *         object's capacity is less than that of the other; and a value greater than 0 if this object's capacity is
     *         greater than that of the other
     */
    @Override
    public int compareTo(final Rooms o) {

        final int thisCapacity = totalCapacity();
        final int oCapacity = o.totalCapacity();

        return Integer.compare(thisCapacity, oCapacity);
    }

    /**
     * Generates a string representation of the list of rooms.
     *
     * @return the string representation
     */
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        final int count = this.rooms.size();
        builder.add("Room group {");
        final Room first = this.rooms.getFirst();
        builder.add(first);
        for (int i = 1; i < count; ++i) {
            builder.add(", ");
            final Room next = this.rooms.get(i);
            builder.add(next);
        }
        builder.add("}");

        return builder.toString();
    }
}
