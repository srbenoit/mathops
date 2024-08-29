package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.List;

/**
 * An available classroom.  This class implements {@code Comparable&lt;AvailableClassroom&gt;}, with the ordering based
 * on room capacity (smaller capacities sort before larger capacities).
 */
final class AvailableClassroom implements Comparable<AvailableClassroom> {

    /** The classroom ID. */
    final int id;

    /** The number of hours per day the classroom is available. */
    final int hoursPerDay;

    /** The seating capacity. */
    final int capacity;

    /** The number of hours remaining in a week. */
    private int hoursRemainingInWeek = 0;

    /** The sections assigned to this classroom. */
    private final List<AssignedSection> assignedSections;

    /**
     * Constructs a new {@code AvailableClassroom}.
     *
     * @param theId          the classroom ID
     * @param theHoursPerDay the number of hours per day the classroom is available
     * @param theCapacity    the seating capacity
     */
    AvailableClassroom(final int theId, final int theHoursPerDay, final int theCapacity) {

        this.id = theId;
        this.hoursPerDay = theHoursPerDay;
        this.capacity = theCapacity;

        this.assignedSections = new ArrayList<>(theHoursPerDay);
    }

    /**
     * Sets the number of hours of availability remaining per week for this classroom.
     *
     * @param theHoursRemainingInWeek the new number of hours
     */
    void setHoursRemainingInWeek(final int theHoursRemainingInWeek) {

        this.hoursRemainingInWeek = theHoursRemainingInWeek;
    }

    /**
     * Decreases the number of hours remaining in a week by a set amount.
     *
     * @param delta the number of hours by which to reduce hours remaining
     */
    void decreaseHoursRemaining(final int delta) {

        this.hoursRemainingInWeek -= delta;
    }

    /**
     * Gets the number of hours of availability remaining per week for this classroom.
     *
     * @return the number of hours
     */
    int getHoursRemainingInWeek() {

        return this.hoursRemainingInWeek;
    }

    /**
     * Clears the list of sections assigned to this classroom.
     */
    void clearAssignedSections() {

        this.assignedSections.clear();
    }

    /**
     * Gets the list of sections assigned to this classroom.
     *
     * @return the list of assigned sections
     */
    List<AssignedSection> getAssignedSections() {

        return new ArrayList<>(this.assignedSections);
    }

    /**
     * Adds an assigned section to this classroom.
     *
     * @param section the section
     */
    void addAssignedSection(final AssignedSection section) {

        this.assignedSections.add(section);
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
        } else if (obj instanceof final AvailableClassroom classroom) {
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
    public int compareTo(final AvailableClassroom o) {

        return Integer.compare(this.capacity, o.capacity);
    }
}

