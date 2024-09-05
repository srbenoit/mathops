package dev.mathops.app.sim.courses;

import java.time.LocalTime;

/**
 * A weekly meeting time for an offered section of a course.  Courses may have any number of meeting times.
 *
 * @param label          a label, like "Class", "Recitation", "Lab", etc. to distinguish from other meeting times.
 * @param theMeetingDays the days of the week for this meeting time
 * @param theRoomId      the room ID
 * @param theStartTime   the start time
 * @param theEndTime     the end time
 */
public record OfferedSectionMeetingTime(String label, EMeetingDays theMeetingDays, String theRoomId,
                                        LocalTime theStartTime, LocalTime theEndTime) {

}
