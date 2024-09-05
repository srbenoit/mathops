package dev.mathops.app.sim.courses;

import dev.mathops.app.sim.registration.EnrollingStudent;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An offered section of a course.
 */
public final class OfferedSection {

    /** The course offering. */
    private final OfferedCourse offeredCourse;

    /** The days the section meets. */
    private final List<OfferedSectionMeetingTime> meetingTimes;

    /** The total number of seats available in the section. */
    private final int totalSeats;

    /** The list of students enrolled in the section. */
    private final List<EnrollingStudent> enrolledStudents;

    /** A list to accumulate enrollment sizes over several trials. */
    private final List<Integer> enrollmentSizeHistory;

    /** Collisions during registration (map from colliding course to number of collisions). */
    private final Map<OfferedSection, Integer> collisions;

    /**
     * Constructs a new {@code OfferedSection}.
     *
     * @param theOfferedCourse the offered course
     * @param theTotalSeats    the total number of seats
     * @param theMeetingTimes  the list of offered times
     */
    public OfferedSection(final OfferedCourse theOfferedCourse, final int theTotalSeats,
                          final OfferedSectionMeetingTime... theMeetingTimes) {

        if (theOfferedCourse == null) {
            throw new IllegalArgumentException("Owning offered course may not be null");
        }
        if (theMeetingTimes == null || theMeetingTimes.length == 0) {
            throw new IllegalArgumentException("Section has no meeting times");
        }

        this.offeredCourse = theOfferedCourse;
        this.totalSeats = theTotalSeats;

        this.meetingTimes = new ArrayList<>(theMeetingTimes.length);
        for (final OfferedSectionMeetingTime meetingTime : theMeetingTimes) {
            if (meetingTime == null) {
                throw new IllegalArgumentException("Meeting time may not be null");
            }
            this.meetingTimes.add(meetingTime);
        }

        this.enrolledStudents = new ArrayList<>(theTotalSeats);
        this.enrollmentSizeHistory = new ArrayList<>(100);

        this.collisions = new HashMap<>(10);
    }

    /**
     * Clears the list of enrolled students.
     */
    public void clearEnrolledStudents() {

        this.enrolledStudents.clear();
    }

    /**
     * Captures the enrollment count in a history record.
     */
    public void captureEnrollment() {

        final int count = this.enrolledStudents.size();
        this.enrollmentSizeHistory.add(Integer.valueOf(count));
    }

    /**
     * Gets the average enrollment over all captured enrollment counts.
     *
     * @return the average enrollment
     */
    public int averageEnrollment() {

        final int numHistory = this.enrollmentSizeHistory.size();

        // Bias the number so truncation later actually performs rounding.
        int total = numHistory / 2;

        for (final Integer count : this.enrollmentSizeHistory) {
            total += count.intValue();
        }

        return total / numHistory;
    }

    /**
     * Clears the collision counts from a registration cycle.
     */
    public void clearCollisions() {

        this.collisions.clear();
    }

    /**
     * Records a collision in which this course could not be selected due to an enrollment in another section.
     *
     * @param collidingSection the colliding section
     */
    public void recordCollision(final OfferedSection collidingSection) {

        final Integer existing = this.collisions.get(collidingSection);
        final int newCount = existing == null ? 1 : existing.intValue() + 1;
        this.collisions.put(collidingSection, Integer.valueOf(newCount));
    }

    /**
     * Gets a copy of the record of collisions during registration.
     *
     * @return the collisions record (a map from colliding course to number of collisions)
     */
    public Map<OfferedSection, Integer> getCollisions() {

        return new HashMap<>(this.collisions);
    }

    /**
     * Tests whether this section has a time conflict with another section.
     *
     * @param other the other section
     * @return true of the two sections conflict
     */
    public boolean hasConflict(final OfferedSection other) {

        boolean conflict = false;

        for (final OfferedSectionMeetingTime myTime : this.meetingTimes) {
            for (final OfferedSectionMeetingTime otherTime : other.getMeetingTimes()) {
                if (myTime.hasConflict(otherTime)) {
                    conflict = true;
                    break;
                }
            }
        }

        return conflict;
    }

    /**
     * Gets the course.
     *
     * @return the course
     */
    public OfferedCourse getOfferedCourse() {

        return this.offeredCourse;
    }

    /**
     * Gets a copy of the meeting times for this section.
     *
     * @return the meeting times
     */
    public List<OfferedSectionMeetingTime> getMeetingTimes() {

        return new ArrayList<>(this.meetingTimes);
    }

    /**
     * Gets the total number of seats.
     *
     * @return the number of seats
     */
    public int getTotalSeats() {

        return this.totalSeats;
    }

    /**
     * Gets the number of students enrolled.
     *
     * @return the number of students enrolled
     */
    public int getEnrollment() {

        return this.enrolledStudents.size();
    }

    /**
     * Gets the number of seats remaining.
     *
     * @return the number of seats remaining
     */
    public int getSeatsRemaining() {

        return this.totalSeats - this.enrolledStudents.size();
    }

    /**
     * Gets a copy of the list of enrolled students.
     *
     * @return the list of enrolled students
     */
    public List<EnrollingStudent> getEnrolledStudents() {

        return new ArrayList<>(this.enrolledStudents);
    }

    /**
     * Adds an enrolled student/
     *
     * @param toAdd the student to add
     */
    public void addEnrolledStudent(final EnrollingStudent toAdd) {

        this.enrolledStudents.add(toAdd);
    }

    /**
     * Generates a string representation of the object.
     *
     * @return the string representation
     */
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        final Course course = this.offeredCourse.getCourse();

        builder.add(course.courseId, " ", this.meetingTimes);

        return builder.toString();
    }
}
