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

    /** True if the class is mandatory for all students. */
    final boolean mandatory;

    /** The number of contact hours each week in a classroom. */
    final int classContactHours;

    /** The list of compatible classrooms. */
    private final List<AvailableClassroom> compatibleClassrooms;

    /** The number of contact hours each week in a lab. */
    final int labContactHours;

    /** The list of compatible labs. */
    private final List<AvailableLab> compatibleLabs;

    /** The total number of seats needed to accommodate a certain student population. */
    private int numSeatsNeeded = 0;

    /** The list of assigned classroom sections. */
    private final List<AssignedSection> assignedClassSections;

    /** The list of assigned lab sections. */
    private final List<AssignedSection> assignedLabSections;

    /**
     * Constructs an {@code OfferedCourse} that has a lab component.
     *
     * @param theCourseId             the course ID
     * @param theNumCredits           the number of credits
     * @param isMandatory             true if the course is mandatory for all students
     * @param theClassContactHours    the number of class contact hours
     * @param theCompatibleClassrooms the list of compatible classrooms
     * @param theLabContactHours      the number of lab contact hours
     * @param theCompatibleLabs       the list of compatible labs
     */
    OfferedCourse(final String theCourseId, final int theNumCredits, final boolean isMandatory,
                  final int theClassContactHours, final List<AvailableClassroom> theCompatibleClassrooms,
                  final int theLabContactHours, final List<AvailableLab> theCompatibleLabs) {

        this.courseId = theCourseId;
        this.numCredits = theNumCredits;
        this.mandatory = isMandatory;
        this.classContactHours = theClassContactHours;
        this.compatibleClassrooms = new ArrayList<>(theCompatibleClassrooms);
        this.labContactHours = theLabContactHours;
        this.compatibleLabs = new ArrayList<>(theCompatibleLabs);

        this.assignedClassSections = new ArrayList<>(5);
        this.assignedLabSections = new ArrayList<>(5);
    }

    /**
     * Constructs an {@code OfferedCourse} with no lab component.
     *
     * @param theCourseId             the course ID
     * @param theNumCredits           the number of credits
     * @param isMandatory             true if the course is mandatory for all students
     * @param theClassContactHours    the number of class contact hours
     * @param theCompatibleClassrooms the list of compatible classrooms
     */
    OfferedCourse(final String theCourseId, final int theNumCredits, final boolean isMandatory,
                  final int theClassContactHours, final List<AvailableClassroom> theCompatibleClassrooms) {

        this.courseId = theCourseId;
        this.numCredits = theNumCredits;
        this.mandatory = isMandatory;
        this.classContactHours = theClassContactHours;
        this.compatibleClassrooms = new ArrayList<>(theCompatibleClassrooms);
        this.labContactHours = 0;
        this.compatibleLabs = new ArrayList<>(0);

        this.assignedClassSections = new ArrayList<>(5);
        this.assignedLabSections = new ArrayList<>(0);
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
     * Clears lists of assigned classroom and lab sections.
     */
    void clearAssignedSections() {

        this.assignedClassSections.clear();
        this.assignedLabSections.clear();
    }

    /**
     * Adds an assigned class section to this course.
     *
     * @param section the assigned section
     */
    void addAssignedClassSection(final AssignedSection section) {

        this.assignedClassSections.add(section);
    }

    /**
     * Adds an assigned lab section to this course.
     *
     * @param section the assigned section
     */
    void addAssignedLabSection(final AssignedSection section) {

        this.assignedClassSections.add(section);
    }

    /**
     * Gets a copy of the list of assigned class sections for this course.
     *
     * @return the assigned class sections
     */
    List<AssignedSection> getClassSections() {

        return new ArrayList<>(this.assignedClassSections);
    }

    /**
     * Gets a copy of the list of assigned lab sections for this course.
     *
     * @return the assigned lab sections
     */
    List<AssignedSection> getLabSections() {

        return new ArrayList<>(this.assignedLabSections);
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


