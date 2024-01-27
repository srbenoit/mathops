package dev.mathops.app.canvas.data;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.json.JSONObject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * An override due date for an assignment for a student. The format of the response is shown below:
 *
 * <pre>
 * { "id": 4,                                  // the ID of the assignment override
 *   "assignment_id": 123,                     // the ID of the assignment the override applies to
 *   "student_ids": [1, 2, 3],                 // the IDs of the override's target students (if
 *                                             //  the override targets an ad-hoc set of students)
 *   "group_id": 2,                            // the ID of the override's target group (if the
 *                                             //  override targets a group and the assignment is
 *                                             //  a group assignment)
 *   "course_section_id": 1,                   // the ID of the override's target section (if the
 *                                             //  override targets a section)
 *   "title": "an assignment override",        // the title of the override
 *   "due_at": "2012-07-01T23:59:00-06:00",    // the overridden due at (if due_at is overridden)
 *   "all_day": true,                          // the overridden all day flag
 *   "all_day_date": "2012-07-01",             // the overridden all day date
 *   "unlock_at": "2012-07-01T23:59:00-06:00", // the overridden unlock at
 *   "lock_at": "2012-07-01T23:59:00-06:00"}   // the overridden lock at
 * </pre>
 */
final class AssignmentOverride {

    /** The override ID. */
    private final Long overrideId;

    /** The ID of the assignment to which this override applies. */
    private final Long assignmentId;

    /** The list of student IDs to which this override applies. */
    private final Long[] studentIds;

    /** The group ID to which this override applies. */
    private final Long groupId;

    /** The course section ID to which this override applies. */
    private final Long courseSectionId;

    /** The override title. */
    private final String title;

    /** The overridden due date. */
    private final ZonedDateTime dueAt;

    /** The override all-day flag. */
    private final Boolean allDay;

    /** The overridden all-day date. */
    private final ZonedDateTime allDayDate;

    /** The overridden unlock date. */
    private final ZonedDateTime unlockAt;

    /** The overridden lock date. */
    private final ZonedDateTime lockAt;

    /**
     * Constructs a new {@code AssignmentOverride} from the JSON response from the server.
     *
     * @param json the parsed JSON response
     */
    AssignmentOverride(final JSONObject json) {

        final Double idDbl = json.getNumberProperty("id");
        this.overrideId = idDbl == null ? null : Long.valueOf(idDbl.longValue());

        final Double assignmentIdDbl = json.getNumberProperty("assignment_id");
        this.assignmentId = assignmentIdDbl == null ? null : Long.valueOf(assignmentIdDbl.longValue());

        final Object studentIdsObj = json.getProperty("student_ids");
        if (studentIdsObj instanceof final Number num) {
            this.studentIds = new Long[1];
            this.studentIds[0] = Long.valueOf(num.longValue());
        } else if (studentIdsObj instanceof final Object[] numArray) {
            final int count = numArray.length;
            this.studentIds = new Long[count];
            for (int i = 0; i < count; ++i) {
                if (numArray[i] instanceof final Number num) {
                    this.studentIds[i] = Long.valueOf(num.longValue());
                }
            }
        } else {
            this.studentIds = null;
        }

        final Double groupIdDbl = json.getNumberProperty("group_id");
        this.groupId = groupIdDbl == null ? null : Long.valueOf(groupIdDbl.longValue());

        final Double courseSectionIdDbl = json.getNumberProperty("course_section_id");
        this.courseSectionId = courseSectionIdDbl == null ? null : Long.valueOf(courseSectionIdDbl.longValue());

        this.title = json.getStringProperty("title");

        final String dueAtStr = json.getStringProperty("due_at");
        if (dueAtStr == null) {
            this.dueAt = null;
        } else {
            ZonedDateTime parsed = null;
            try {
                parsed = ZonedDateTime.parse(dueAtStr);
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid date/time value: ", dueAtStr, ex);
            }
            this.dueAt = parsed;
        }

        final String allDayStr = json.getStringProperty("all_day");
        if ("true".equalsIgnoreCase(allDayStr)) {
            this.allDay = Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(allDayStr)) {
            this.allDay = Boolean.FALSE;
        } else {
            this.allDay = null;
        }

        final String allDayDateStr = json.getStringProperty("all_day_date");
        if (allDayDateStr == null) {
            this.allDayDate = null;
        } else {
            ZonedDateTime parsed = null;
            if (dueAtStr != null) {
                try {
                    parsed = ZonedDateTime.parse(dueAtStr);
                } catch (final DateTimeParseException ex) {
                    Log.warning("Invalid date/time value: ", dueAtStr, ex);
                }
            }
            this.allDayDate = parsed;
        }

        final String unlockDateStr = json.getStringProperty("unlock_at");
        if (unlockDateStr == null) {
            this.unlockAt = null;
        } else {
            ZonedDateTime parsed = null;
            if (dueAtStr != null) {
                try {
                    parsed = ZonedDateTime.parse(dueAtStr);
                } catch (final DateTimeParseException ex) {
                    Log.warning("Invalid date/time value: ", dueAtStr, ex);
                }
            }
            this.unlockAt = parsed;
        }

        final String lockDateStr = json.getStringProperty("lock_at");
        if (lockDateStr == null) {
            this.lockAt = null;
        } else {
            ZonedDateTime parsed = null;
            if (dueAtStr != null) {
                try {
                    parsed = ZonedDateTime.parse(dueAtStr);
                } catch (final DateTimeParseException ex) {
                    Log.warning("Invalid date/time value: ", dueAtStr, ex);
                }
            }
            this.lockAt = parsed;
        }
    }
}
