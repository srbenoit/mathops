package dev.mathops.app.sim.courses;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;

import java.time.LocalTime;

/**
 * A weekly meeting time for an offered section of a course.  Courses may have any number of meeting times.
 *
 * @param label       a label, like "Class", "Recitation", "Lab", etc. to distinguish from other meeting times.
 * @param meetingDays the days of the week for this meeting time
 * @param roomId      the room ID
 * @param startTime   the start time
 * @param endTime     the end time
 */
public record OfferedSectionMeetingTime(String label, EMeetingDays meetingDays, String roomId,
                                        LocalTime startTime, LocalTime endTime) {

    /**
     * Tests whether this meeting time has a time conflict with another meeting time.
     *
     * @param other the other section
     * @return true of the two meeting times conflict
     */
    boolean hasConflict(final OfferedSectionMeetingTime other) {

        boolean conflict = false;

        final LocalTime otherStart = other.startTime();
        if (otherStart.isBefore(this.endTime)) {
            // Other time starts before we end, so a conflict is possible

            final LocalTime otherEnd = other.endTime();
            if (otherEnd.isAfter(this.startTime)) {
                // Other time starts before we end and ends after we start, so the times DO conflict

                final EMeetingDays otherDays = other.meetingDays();

                conflict = (this.meetingDays.includesMonday() && otherDays.includesMonday())
                           || (this.meetingDays.includesTuesday() && otherDays.includesTuesday())
                           || (this.meetingDays.includesWednesday() && otherDays.includesWednesday())
                           || (this.meetingDays.includesThursday() && otherDays.includesThursday())
                           || (this.meetingDays.includesFriday() && otherDays.includesFriday());
            }
        }

        return conflict;
    }

    /**
     * Generates a string representation of the object.
     *
     * @return the string representation
     */
    public String toString() {

        final String meetingDaysName = this.meetingDays.name();
        final String startTimeStr = TemporalUtils.FMT_HM_A.format(this.startTime);
        final String endTimeStr = TemporalUtils.FMT_HM_A.format(this.endTime);

        return SimpleBuilder.concat(this.label, " on ", meetingDaysName, " in ", this.roomId, " from ", startTimeStr,
                " to ", endTimeStr);
    }
}
