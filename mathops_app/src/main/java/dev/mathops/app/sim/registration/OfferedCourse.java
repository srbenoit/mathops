package dev.mathops.app.sim.registration;

import java.util.ArrayList;
import java.util.List;

/**
 * An offered course.
 */
final class OfferedCourse implements Comparable<OfferedCourse> {

    /** The unique course ID. */
    final String courseId;

    /** The number of credits. */
    final int numCredits;

    /** The number of contact hours each week in a classroom. */
    final int classContactHours;

    /** The list of compatible classrooms. */
    final List<AvailableClassroom> compatibleClassrooms;

    /** The number of contact hours each week in a lab. */
    final int labContactHours;

    /** The list of compatible labs. */
    final List<AvailableLab> compatibleLabs;

    /** The total number of seats needed to accommodate a certain student population. */
    private int numSeatsNeeded;

    /**
     * Constructs an {@code OfferedCourse} that has a lab component.
     *
     * @param theCourseId             the course ID
     * @param theNumCredits           the number of credits
     * @param theClassContactHours    the number of class contact hours
     * @param theCompatibleClassrooms the list of compatible classrooms
     * @param theLabContactHours      the number of lab contact hours
     * @param theCompatibleLabs       the list of compatible labs
     */
    OfferedCourse(final String theCourseId, final int theNumCredits, final int theClassContactHours,
                  final List<AvailableClassroom> theCompatibleClassrooms, final int theLabContactHours,
                  final List<AvailableLab> theCompatibleLabs) {

        this.courseId = theCourseId;
        this.numCredits = theNumCredits;
        this.classContactHours = theClassContactHours;
        this.compatibleClassrooms = new ArrayList<>(theCompatibleClassrooms);
        this.labContactHours = theLabContactHours;
        this.compatibleLabs = new ArrayList<>(theCompatibleLabs);
    }

    /**
     * Constructs an {@code OfferedCourse} with no lab component.
     *
     * @param theCourseId             the course ID
     * @param theNumCredits           the number of credits
     * @param theClassContactHours    the number of class contact hours
     * @param theCompatibleClassrooms the list of compatible classrooms
     */
    OfferedCourse(final String theCourseId, final int theNumCredits, final int theClassContactHours,
                  final List<AvailableClassroom> theCompatibleClassrooms) {

        this.courseId = theCourseId;
        this.numCredits = theNumCredits;
        this.classContactHours = theClassContactHours;
        this.compatibleClassrooms = new ArrayList<>(theCompatibleClassrooms);
        this.labContactHours = 0;
        this.compatibleLabs = new ArrayList<>(0);
    }

    /**
     * Sets the number of seats needed in this class.
     *
     * @param theNumSeatsNeeded the number of seats needed
     */
    void setNumSeatsNeeded(final int theNumSeatsNeeded) {

        this.numSeatsNeeded = theNumSeatsNeeded;
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
     * Tests whether a classroom is compatible with the course.
     *
     * @param classroom the classroom
     * @return true if the classroom is "compatible" with this course
     */
    boolean isClassroomCompatible(final AvailableClassroom classroom) {

        return this.compatibleClassrooms.contains(classroom);
    }

    /**
     * Tests whether a all classrooms in a group are compatible with the course.
     *
     * @param group the classroom group
     * @return true if all rooms in the classroom group are "compatible" with this course
     */
    boolean isClassroomGroupCompatible(final AvailableClassroomGroup group) {

        boolean compatible = true;

        final List<AvailableClassroom> classrooms = group.getClassrooms();

        for (final AvailableClassroom classroom : classrooms) {
            if (!isClassroomCompatible(classroom)) {
                compatible = false;
                break;
            }
        }

        return compatible;
    }

    /**
     * Tests whether a lab is compatible with the course.
     *
     * @param lab the lab
     * @return true if the lab is "compatible" with this course
     */
    boolean isLabCompatible(final AvailableLab lab) {

        return this.compatibleLabs.contains(lab);
    }

    /**
     * Tests whether a all labs in a group are compatible with the course.
     *
     * @param group the lab group
     * @return true if all rooms in the lab group are "compatible" with this course
     */
    boolean isLabGroupCompatible(final AvailableLabGroup group) {

        boolean compatible = true;

        final List<AvailableLab> labs = group.getLabs();

        for (final AvailableLab lab : labs) {
            if (!isLabCompatible(lab)) {
                compatible = false;
                break;
            }
        }

        return compatible;
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
        } else if (obj instanceof final OfferedCourse course) {
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
    public int compareTo(final OfferedCourse o) {

        return this.courseId.compareTo(o.courseId);
    }
}


