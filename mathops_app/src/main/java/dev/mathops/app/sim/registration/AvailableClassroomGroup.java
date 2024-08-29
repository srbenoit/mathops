package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.List;

/**
 * A possible group of multiple classrooms. This class implements {@code Comparable&lt;AvailableClassroomGroup&gt;},
 * with the ordering based on total capacity of all classrooms in the group.
 */
final class AvailableClassroomGroup implements Comparable<AvailableClassroomGroup> {

    /** The classrooms in the group (at least 2). */
    final List<AvailableClassroom> classrooms;

    /** The total seating capacity. */
    final int totalCapacity;

    /**
     * Constructs a new {@code AvailableClassroomGroup}.
     *
     * @param theClassrooms the classrooms in the group
     */
    AvailableClassroomGroup(final AvailableClassroom... theClassrooms) {

        if (theClassrooms == null || theClassrooms.length < 2) {
            throw new IllegalArgumentException("There must be at least 2 classrooms specified for a group");
        }

        int total = 0;

        this.classrooms = new ArrayList<>(theClassrooms.length);
        for (final AvailableClassroom classroom : theClassrooms) {
            if (classroom == null) {
                throw new IllegalArgumentException("Classroom may not be null");
            }
            this.classrooms.add(classroom);
            total += classroom.capacity;
        }

        this.totalCapacity = total;
    }

    /**
     * Gets a copy of the list of available classrooms.
     *
     * @return the available classrooms
     */
    List<AvailableClassroom> getClassrooms() {

        return new ArrayList<>(this.classrooms);
    }

    /**
     * Gets the smallest number of hours remaining in a week for any classroom in this group.
     *
     * @return the number of hours for which all classrooms are available in a week
     */
    int getHoursRemainingInWeek() {

        int hours = Integer.MAX_VALUE;

        for (final AvailableClassroom classroom : this.classrooms) {
            final int remaining = classroom.getHoursRemainingInWeek();
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
    public int compareTo(final AvailableClassroomGroup o) {

        return Integer.compare(this.totalCapacity, o.totalCapacity);
    }
}

