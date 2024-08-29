package dev.mathops.app.sim.registration.rooms;

import java.util.ArrayList;
import java.util.List;

/**
 * A possible group of multiple labs. This class implements {@code Comparable&lt;AvailableLabGroup&gt;}, with the
 * ordering based on total capacity of all labs in the group.
 */
public final class AvailableLabGroup implements Comparable<AvailableLabGroup> {

    /** The labs in the group (at least 2). */
    final List<AvailableLab> labs;

    /** The total seating capacity. */
    public final int totalCapacity;

    /**
     * Constructs a new {@code AvailableLabGroup}.
     *
     * @param theLabs the labs in the group
     */
    public AvailableLabGroup(final AvailableLab... theLabs) {

        if (theLabs == null || theLabs.length < 2) {
            throw new IllegalArgumentException("There must be at least 2 labs specified for a group");
        }

        int total = 0;

        this.labs = new ArrayList<>(theLabs.length);
        for (final AvailableLab lab : theLabs) {
            if (lab == null) {
                throw new IllegalArgumentException("Lab may not be null");
            }
            this.labs.add(lab);
            total += lab.capacity;
        }

        this.totalCapacity = total;
    }

    /**
     * Gets a copy of the list of available labs.
     *
     * @return the available labs
     */
    public List<AvailableLab> getLabs() {

        return new ArrayList<>(this.labs);
    }

    /**
     * Gets the smallest number of hours remaining in a week for any lab in this group.
     *
     * @return the number of hours for which all labs are available in a week
     */
    public int getHoursRemainingInWeek() {

        int hours = Integer.MAX_VALUE;

        for (final AvailableLab lab : this.labs) {
            final int remaining = lab.getHoursRemainingInWeek();
            hours = Math.min(hours, remaining);
        }

        return hours;
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
    public int compareTo(final AvailableLabGroup o) {

        return Integer.compare(this.totalCapacity, o.totalCapacity);
    }
}

