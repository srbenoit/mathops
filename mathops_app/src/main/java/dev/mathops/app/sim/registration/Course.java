package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An offered course.
 */
final class Course implements Comparable<Course> {

    /** The unique course ID. */
    final String courseId;

    /** The number of credits. */
    final int numCredits;

    /** True if the class is mandatory for all students. */
    final boolean mandatory;

    /** The number of contact hours per week for each room usage. */
    private final Map<ERoomUsage, Integer> contactHoursByRoomType;

    /** The assignment type for each room usage. */
    private final Map<ERoomUsage, EAssignmentType> assignmentTypeByRoomType;

    /** The list of rooms that are compatible with a course's needs for each room usage. */
    private final Map<ERoomUsage, List<Room>> compatibleRoomsByRoomType;

    /** The total number of seats needed to accommodate a certain student population. */
    private int numSeatsNeeded = 0;

    /**
     * The list of sections assigned for this course (with a section for each required room usage for each instance of
     * the course offered).
     */
    private final Map<ERoomUsage, List<RoomAssignment>> roomAssignments;

    /**
     * Constructs an {@code Course} that has a lab component.
     *
     * @param theCourseId   the course ID
     * @param theNumCredits the number of credits
     * @param isMandatory   true if the course is mandatory for all students
     */
    Course(final String theCourseId, final int theNumCredits, final boolean isMandatory) {

        this.courseId = theCourseId;
        this.numCredits = theNumCredits;
        this.mandatory = isMandatory;

        this.contactHoursByRoomType = new EnumMap<>(ERoomUsage.class);
        this.assignmentTypeByRoomType = new EnumMap<>(ERoomUsage.class);
        this.compatibleRoomsByRoomType = new EnumMap<>(ERoomUsage.class);
        this.roomAssignments = new EnumMap<>(ERoomUsage.class);
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
                     final Room... rooms) {

        if (rooms == null || rooms.length == 0) {
            throw new IllegalArgumentException("List of compatible rooms must be nonempty");
        }

        final Integer contactHours = Integer.valueOf(contactHoursPerWeek);
        this.contactHoursByRoomType.put(usage, contactHours);

        this.assignmentTypeByRoomType.put(usage, assignmentType);

        final List<Room> roomsList = Arrays.asList(rooms);
        this.compatibleRoomsByRoomType.put(usage, roomsList);

        this.roomAssignments.computeIfAbsent(usage, key -> new ArrayList<>(10));
    }

    /**
     * Resets the number of seats needed in this class to zero.
     */
    void resetNumSeatsNeeded() {

        this.numSeatsNeeded = 0;
    }

    /**
     * Increments the number of seats needed in a course.
     */
    void incrementNumSeatsNeeded() {

        ++this.numSeatsNeeded;
    }

    /**
     * Gets the number of seats needed in this class.
     *
     * @return the number of seats needed
     */
    int getNumSeatsNeeded() {

        return this.numSeatsNeeded;
    }

    /**
     * Gets the set of room usages this course needs for delivery.
     *
     * @return the set of usages (an unmodifiable view)
     */
    Set<ERoomUsage> getUsages() {

        final Set<ERoomUsage> keys = this.contactHoursByRoomType.keySet();

        return Collections.unmodifiableSet(keys);
    }

    /**
     * Gets the number of weekly contact hours this room needed for a specified usage.
     *
     * @param usage the usage
     * @return the number of weekly contact hours
     */
    int getContactHours(final ERoomUsage usage) {

        final Integer value = this.contactHoursByRoomType.get(usage);

        return value == null ? 0 : value.intValue();
    }

    /**
     * Gets the assignment type for a specified usage.
     *
     * @param usage the usage
     * @return the assignment type
     */
    EAssignmentType getAssignmentType(final ERoomUsage usage) {

        return this.assignmentTypeByRoomType.get(usage);
    }

    /**
     * Tests whether a room is compatible for a specified use for this course.
     *
     * @param usage the usage
     * @param room  the room
     * @return true if the room is "compatible" with this course
     */
    boolean isRoomCompatible(final ERoomUsage usage, final Room room) {

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
    boolean areRoomsCompatible(final ERoomUsage usage, final Iterable<Room> rooms) {

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
     * Clears lists of room assignments.
     */
    void clearRoomAssignments() {

        for (final List<RoomAssignment> list : this.roomAssignments.values()) {
            list.clear();
        }
    }

    /**
     * Adds a room assignment for this course.  For each instance of the course to be taught, there should be a room
     * assignment for each required room usage.
     *
     * @param usage      the room usage
     * @param assignment the room assignment
     */
    void addRoomAssignment(final ERoomUsage usage, final RoomAssignment assignment) {

        final List<RoomAssignment> list = this.roomAssignments.computeIfAbsent(usage, key -> new ArrayList<>(10));

        list.add(assignment);
    }

//    /**
//     * Gets a copy of the list of room assignments for a specified usage in this course.
//     *
//     * @param usage the room usage
//     * @return the room assignments
//     */
//    List<RoomAssignment> getRoomAssignments(final ERoomUsage usage) {
//
//        final List<RoomAssignment> list = this.roomAssignments.get(usage);
//
//        return list == null ? null : new ArrayList<>(list);
//    }

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
}


