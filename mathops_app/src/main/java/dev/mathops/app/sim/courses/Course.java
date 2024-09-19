package dev.mathops.app.sim.courses;

import dev.mathops.app.sim.schedule.EAssignmentType;
import dev.mathops.app.sim.rooms.ERoomUsage;
import dev.mathops.app.sim.rooms.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An offered course.
 */
public final class Course implements Comparable<Course> {

    /** The unique course ID. */
    public final String courseId;

    /** The number of credits. */
    public final int numCredits;

    /** The enrollment cap. */
    public final int enrollmentCap;

    /** True if the class is mandatory for all students. */
    public final boolean mandatory;

    /** The number of contact hours per week for each room usage. */
    private final Map<ERoomUsage, Integer> contactHoursByRoomType;

    /** The assignment type for each room usage. */
    private final Map<ERoomUsage, EAssignmentType> assignmentTypeByRoomType;

    /** The list of rooms that are compatible with a course's needs for each room usage. */
    private final Map<ERoomUsage, List<Room>> compatibleRoomsByRoomType;

    /** The total number of seats needed to accommodate a certain student population. */
    private int numSeatsNeeded = 0;

    /**
     * Constructs an {@code Course} that has a lab component.
     *
     * @param theCourseId      the course ID
     * @param theNumCredits    the number of credits
     * @param theEnrollmentCap the enrollment cap
     * @param isMandatory      true if the course is mandatory for all students
     */
    public Course(final String theCourseId, final int theNumCredits, final int theEnrollmentCap,
                  final boolean isMandatory) {

        this.courseId = theCourseId;
        this.numCredits = theNumCredits;
        this.enrollmentCap = theEnrollmentCap;
        this.mandatory = isMandatory;

        this.contactHoursByRoomType = new EnumMap<>(ERoomUsage.class);
        this.assignmentTypeByRoomType = new EnumMap<>(ERoomUsage.class);
        this.compatibleRoomsByRoomType = new EnumMap<>(ERoomUsage.class);
    }

    /**
     * Adds a room type to the course's room needs.
     *
     * @param usage               the room usage
     * @param contactHoursPerWeek the number of contact hours per week the course needs this room type
     * @param assignmentType      the assignment type
     * @param rooms               the list of rooms that are compatible with this course's needs for this room type
     */
    public void addRoomType(final ERoomUsage usage, final int contactHoursPerWeek, final EAssignmentType assignmentType,
                            final Room... rooms) {

        if (rooms == null || rooms.length == 0) {
            throw new IllegalArgumentException("List of compatible rooms must be nonempty");
        }

        final Integer contactHours = Integer.valueOf(contactHoursPerWeek);
        this.contactHoursByRoomType.put(usage, contactHours);

        this.assignmentTypeByRoomType.put(usage, assignmentType);

        final List<Room> roomsList = Arrays.asList(rooms);
        this.compatibleRoomsByRoomType.put(usage, roomsList);
    }

    /**
     * Adds a room type to the course's room needs.
     *
     * @param usage               the room usage
     * @param contactHoursPerWeek the number of contact hours per week the course needs this room type
     * @param assignmentType      the assignment type
     * @param rooms               the list of rooms that are compatible with this course's needs for this room type
     */
    void addRoomType(final ERoomUsage usage, final int contactHoursPerWeek, final EAssignmentType assignmentType,
                     final Collection<Room> rooms) {

        if (rooms == null || rooms.isEmpty()) {
            throw new IllegalArgumentException("List of compatible rooms must be nonempty");
        }

        final Integer contactHours = Integer.valueOf(contactHoursPerWeek);
        this.contactHoursByRoomType.put(usage, contactHours);

        this.assignmentTypeByRoomType.put(usage, assignmentType);

        final List<Room> roomsList = new ArrayList<>(rooms);
        this.compatibleRoomsByRoomType.put(usage, roomsList);
    }

    /**
     * Resets the number of seats needed in this class to zero.
     */
    public void resetNumSeatsNeeded() {

        this.numSeatsNeeded = 0;
    }

    /**
     * Sets the number of seats needed.
     *
     * @param newNumSeatsNeeded the new number of seats needed
     */
    public void setNumSeatsNeeded(final int newNumSeatsNeeded) {

        this.numSeatsNeeded = newNumSeatsNeeded;
    }

    /**
     * Increments the number of seats needed in a course.
     */
    public void incrementNumSeatsNeeded() {

        ++this.numSeatsNeeded;
    }

    /**
     * Gets the number of seats needed in this class.
     *
     * @return the number of seats needed
     */
    public int getNumSeatsNeeded() {

        return this.numSeatsNeeded;
    }

    /**
     * Gets the set of room usages this course needs for delivery.
     *
     * @return the set of usages (an unmodifiable view)
     */
    public Set<ERoomUsage> getUsages() {

        final Set<ERoomUsage> keys = this.contactHoursByRoomType.keySet();

        return Collections.unmodifiableSet(keys);
    }

    /**
     * Gets the number of weekly contact hours this room needed for a specified usage.
     *
     * @param usage the usage
     * @return the number of weekly contact hours
     */
    public int getContactHours(final ERoomUsage usage) {

        final Integer value = this.contactHoursByRoomType.get(usage);

        return value == null ? 0 : value.intValue();
    }

    /**
     * Gets the assignment type for a specified usage.
     *
     * @param usage the usage
     * @return the assignment type
     */
    public EAssignmentType getAssignmentType(final ERoomUsage usage) {

        return this.assignmentTypeByRoomType.get(usage);
    }

    /**
     * Tests whether a room is compatible for a specified use for this course.
     *
     * @param usage the usage
     * @param room  the room
     * @return true if the room is "compatible" with this course
     */
    public boolean isRoomCompatible(final ERoomUsage usage, final Room room) {

        final List<Room> compatibleRooms = this.compatibleRoomsByRoomType.get(usage);

        return compatibleRooms != null && compatibleRooms.contains(room);
    }

    /**
     * Tests whether all rooms in a set of rooms are compatible for a specified use for this course.
     *
     * @param usage the usage
     * @param rooms the rooms
     * @return true if all rooms in the group are "compatible" with this course
     */
    public boolean areRoomsCompatible(final ERoomUsage usage, final Iterable<Room> rooms) {

        boolean ok = true;

        for (final Room room : rooms) {
            if (!isRoomCompatible(usage, room)) {
                ok = false;
                break;
            }
        }

        return ok;
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.courseId.hashCode();
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
        } else if (obj instanceof final Course course) {
            equal = this.courseId.equals(course.courseId);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object to another for order.  Order comparisons are done on unique key strings.
     *
     * @param o the object to be compared
     * @return 0 if the argument is equal to this object; a value less than 0 if this object's key is lexicographically
     *         less than the argument's key; and a value greater than 0 if this object's key is lexicographically
     *         greater than the argument's key
     */
    @Override
    public int compareTo(final Course o) {

        return this.courseId.compareTo(o.courseId);
    }

    /**
     * Generates a string representation of the list of rooms.
     *
     * @return the string representation
     */
    public String toString() {

        return this.courseId;
    }
}


