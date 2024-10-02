package dev.mathops.app.sim.rooms;

/**
 * A set of rooms.  A simulation is run against a selected set of rooms.
 */
public final class RoomSet implements Comparable<RoomSet> {

    /** The set name (unique among defined sets). */
    private final String name;

    /** The table model that stores the rooms. */
    private final RoomSetTableModel tableModel;

    /**
     * Constructs a new {@code RoomSet}.
     *
     * @param theName    the set name
     * @param owningList the owning list model to be notified when room set data has changed
     */
    RoomSet(final String theName, final RoomSetsListModel owningList) {

        if (theName == null || theName.isBlank()) {
            throw new IllegalArgumentException("Room set name may not be null or blank");
        }

        this.name = theName;
        this.tableModel = new RoomSetTableModel(owningList);
    }

    /**
     * Gets the set name.
     *
     * @return the set name
     */
    public String getName() {

        return this.name;
    }

    /**
     * Gets the table models containing the rooms.
     *
     * @return the table model
     */
    RoomSetTableModel getTableModel() {

        return this.tableModel;
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.name.hashCode();
    }

    /**
     * Tests whether this object is equal to another.  Equality of this class is tested only on equality of the unique
     * configuration name.
     *
     * @param obj the other object
     * @return true if this object is equal
     */
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RoomSet room) {
            final String objId = room.getName();
            equal = this.name.equals(objId);
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
    public int compareTo(final RoomSet o) {

        final String otherName = o.getName();

        return this.name.compareTo(otherName);
    }

    /**
     * Generates a string representation of the list of rooms.
     *
     * @return the string representation
     */
    public String toString() {

        // This representation will be used in Swing components whose model contains objects of this type (meaning
        // the string representation should be just the configuration mame)

        return this.name;
    }
}
