package dev.mathops.app.sim.courses;

import dev.mathops.app.sim.registration.EnrollingStudent;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

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
