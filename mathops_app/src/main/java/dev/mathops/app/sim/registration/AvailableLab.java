package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.List;

/**
 * An available lab.  This class implements {@code Comparable&lt;AvailableLab&gt;}, with the ordering based on room
 * capacity (smaller capacities sort before larger capacities).
 */
final class AvailableLab implements Comparable<AvailableLab> {

    /** The lab ID. */
    final int id;

    /** The number of hours per day the lab is available. */
    final int hoursPerDay;

    /** The seating capacity. */
    final int capacity;

    /** The number of hours remaining in a week. */
    int hoursRemainingInWeek;

    /** The sections assigned to this lab. */
    private final List<AssignedSection> assignedSections;

    /**
     * Constructs a new {@code AvailableLab}.
     *
     * @param theId          the lab ID
     * @param theHoursPerDay the number of hours per day the lab is available
     * @param theCapacity    the seating capacity
     */
    AvailableLab(final int theId, final int theHoursPerDay, final int theCapacity) {

        this.id = theId;
        this.hoursPerDay = theHoursPerDay;
        this.capacity = theCapacity;

        this.assignedSections = new ArrayList<>(theHoursPerDay);
    }

    /**
     * Sets the number of hours of availability remaining per week for this lab.
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
     * Gets the number of hours of availability remaining per week for this lab.
     *
     * @return the number of hours
     */
    int getHoursRemainingInWeek() {

        return this.hoursRemainingInWeek;
    }

    /**
     * Clears the list of sections assigned to this lab.
     */
    void clearAssignedSections() {

        this.assignedSections.clear();
    }

    /**
     * Gets the list of sections assigned to this lab.
     *
     * @return the list of assigned sections
     */
    List<AssignedSection> getAssignedSections() {

        return new ArrayList<>(this.assignedSections);
    }

    /**
     * Adds an assigned section to this lab.
     *
     * @param section the section
     */
    public void addAssignedSection(final AssignedSection section) {

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
        } else if (obj instanceof final AvailableLab lab) {
            equal = this.id == lab.id;
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
    public int compareTo(final AvailableLab o) {

        return Integer.compare(this.capacity, o.capacity);
    }
}

