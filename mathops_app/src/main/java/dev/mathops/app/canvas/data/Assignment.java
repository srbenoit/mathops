package dev.mathops.app.canvas.data;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * The definition of a single assignment, as returned by {@code /api/v1/courses/:course_id/assignments}. The format of
 * the response is shown below (although not all fields are captured in the Java object):
 *
 * <pre>
 * { "id": 2109845,
 *   "name": "Assignment Name",
 *   "description": "HTML Description",
 *   "created_at": "2023-03-24T14:57:09Z",
 *   "updated_at": "2023-03-24T14:57:09Z",
 *   "due_at": null,
 *   "lock_at": null,
 *   "unlock_at": null,
 *   "has_overrides": false,
 *   "course_id": 164819.0,
 *   "html_url": "https://colostate.instructure.com/courses/164819/assignments/2109845",
 *   "submissions_download_url": "https://colostate.instructure.com/courses/164819/assignments/2109845/submissions?zip=1",
 *   "assignment_group_id": 507568.0,
 *   "due_date_required": false,
 *   "max_name_length": 255.0,
 *   "grade_group_students_individually": false,
 *   "peer_reviews": false,
 *   "automatic_peer_reviews": false,
 *   "anonymous_peer_reviews": false,
 *   "intra_group_peer_reviews": false,
 *   "group_category_id": null,
 *   "needs_grading_count": 0.0,
 *   "position": 1.0,
 *   "post_to_sis": false,
 *   "integration_id": null,
 *   "integration_data": { },
 *   "points_possible": 10.0,
 *   "submission_types": ["external_tool"],
 *   "has_submitted_submissions": false,
 *   "grading_type": "points",
 *   "grading_standard_id": null,
 *   "workflow_state": "unpublished",
 *   "published": false,
 *   "unpublishable": true,
 *   "only_visible_to_overrides": false,
 *   "locked_for_user": false,
 *   "omit_from_final_grade": false,
 *   "moderated_grading": false,
 *   "grader_count": 0.0,
 *   "final_grader_id": null,
 *   "grader_comments_visible_to_graders": true,
 *   "graders_anonymous_to_graders": false,
 *   "grader_names_visible_to_final_grader": true,
 *   "graded_submissions_exist": false,
 *   "anonymous_grading": false,
 *   "anonymous_instructor_annotations": false,
 *   "allowed_attempts": -1.0,
 *   "post_manually": false,
 *   "annotatable_attachment_id": null,
 *   "anonymize_students": false,
 *   "require_lockdown_browser": false,
 *   "important_dates": false,
 *   "muted": true,
 *   "url": "https://colostate.instructure.com/api/v1/courses/164819/external_tools/sessionless_launch?assignment_id=2109845&launch_type=assessment",
 *   "secure_params": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsdGlfYXNzaWdubWVudF9pZCI6IjYyODlmNGUzLWY0ODEtNDlkYy04YzQ1LTQ1ZThlMzk1YmNkOSIsImx0aV9hc3NpZ25tZW50X2Rlc2NyaXB0aW9uIjoiXHUwMDNjcFx1MDAzZVRoaXMgaXMgYSB0ZXN0IGFzc2lnbm1lbnQgdG8gd29yayBvbiBDYW52YXMgaW50ZWdlcmF0aW9uLlx1MDAzYy9wXHUwMDNlIn0.2jn5vvJbdSvasqRxYB4IvOkGGARnQgl_Kj4lQ8au_c8",
 *   "is_quiz_assignment": false,
 *   "in_closed_grading_period": false,
 *   "lti_context_id": "6289f4e3-f481-49dc-8c45-45e8e395bcd9",
 *   "sis_assignment_id": null,
 *   "original_quiz_id": null,
 *   "original_lti_resource_link_id": null,
 *   "original_course_id": null,
 *   "original_assignment_id": null,
 *   "original_assignment_name": null,
 *   "can_duplicate": false
 *   "overrides": [(see AssignmentOverride class)}
 * </pre>
 */
public final class Assignment {

    /** The assignment ID. */
    private final Long id;

    /** The assignment name. */
    private final String name;

    /** The assignment HTML description. */
    private final String description;

    /** The date the assignment was created. */
    private final ZonedDateTime createdAt;

    /** The date the assignment was last updated. */
    private final ZonedDateTime updatedAt;

    /** The date the assignment is due. */
    private final ZonedDateTime dueAt;

    /** The date the assignment is locked. */
    private final ZonedDateTime lockAt;

    /** The date the assignment is unlocked. */
    private final ZonedDateTime unlockAt;

    /** The course ID. */
    private final Long courseId;

    /** The assignment group ID. */
    private final Long assignmentGroupId;

    /** The sorting position of the assignment in the group. */
    private final Long position;

    /** The possible points on the assignment. */
    private final Double pointsPossible;

    /**
     * The allowed types of submission (each is 'discussion_topic', 'online_quiz', 'on_paper', 'none', 'external_tool',
     * 'online_text_entry', 'online_url', 'online_upload', 'media_recording', or 'student_annotation').
     */
    private final String[] submissionTypes;

    /** The possible grading types (one of 'pass_fail', 'percent', 'letter_grade', 'gpa_scale', 'points'). */
    private final String gradingType;

    /** The grading standard ID. */
    private final Long gradingStandardId;

    /** The workflow state (one of 'unpublished', 'published'). */
    private final String workflowState;

    /** The only-visible-to-overrides flag. */
    private final Boolean onlyVisibleToOverrides;

    /** True to omit from student's final grade. */
    private final Boolean omitFromFinalGrade;

    /** The number of attempts allowed (-1 if unlimited). */
    private final Long allowedAttempts;

    /** The overrides. */
    private final List<AssignmentOverride> overrides;

    /**
     * Constructs a new {@code Assignment} from the JSON response from the server.
     *
     * @param json the parsed JSON response
     */
    public Assignment(final JSONObject json) {

        final Double idDbl = json.getNumberProperty("id");
        this.id = idDbl == null ? null : Long.valueOf(idDbl.longValue());

        this.name = json.getStringProperty("name");
        this.description = json.getStringProperty("description");

        final String createdAtStr = json.getStringProperty("created_at");
        if (createdAtStr == null) {
            this.createdAt = null;
        } else {
            ZonedDateTime parsed = null;
            try {
                parsed = ZonedDateTime.parse(createdAtStr);
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid date/time value: ", createdAtStr, ex);
            }
            this.createdAt = parsed;
        }

        final String updatedAtStr = json.getStringProperty("updated_at");
        if (updatedAtStr == null) {
            this.updatedAt = null;
        } else {
            ZonedDateTime parsed = null;
            try {
                parsed = ZonedDateTime.parse(updatedAtStr);
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid date/time value: ", updatedAtStr, ex);
            }
            this.updatedAt = parsed;
        }

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

        final String lockAtStr = json.getStringProperty("lock_at");
        if (lockAtStr == null) {
            this.lockAt = null;
        } else {
            ZonedDateTime parsed = null;
            try {
                parsed = ZonedDateTime.parse(lockAtStr);
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid date/time value: ", lockAtStr, ex);
            }
            this.lockAt = parsed;
        }

        final String unlockAtStr = json.getStringProperty("unlock_at");
        if (unlockAtStr == null) {
            this.unlockAt = null;
        } else {
            ZonedDateTime parsed = null;
            try {
                parsed = ZonedDateTime.parse(unlockAtStr);
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid date/time value: ", unlockAtStr, ex);
            }
            this.unlockAt = parsed;
        }

        final Double courseIdDbl = json.getNumberProperty("course_id");
        this.courseId = courseIdDbl == null ? null : Long.valueOf(courseIdDbl.longValue());

        final Double assignmentGroupIdDbl = json.getNumberProperty("assignment_group_id");
        this.assignmentGroupId = assignmentGroupIdDbl == null ? null : Long.valueOf(assignmentGroupIdDbl.longValue());

        final Double positionDbl = json.getNumberProperty("position");
        this.position = positionDbl == null ? null : Long.valueOf(positionDbl.longValue());

        this.pointsPossible = json.getNumberProperty("points_possible");

        final Object submissionTypesObj = json.getProperty("submission_types");
        if (submissionTypesObj instanceof final String str) {
            this.submissionTypes = new String[1];
            this.submissionTypes[0] = str;
        } else if (submissionTypesObj instanceof final Object[] strArray) {
            final int count = strArray.length;
            this.submissionTypes = new String[count];
            for (int i = 0; i < count; ++i) {
                if (strArray[i] instanceof final String str) {
                    this.submissionTypes[i] = str;
                }
            }
        } else {
            this.submissionTypes = null;
        }

        this.gradingType = json.getStringProperty("grading_type");

        final Double gradingStandardIdDbl = json.getNumberProperty("grading_standard_id");
        this.gradingStandardId = gradingStandardIdDbl == null ? null : Long.valueOf(gradingStandardIdDbl.longValue());

        this.workflowState = json.getStringProperty("workflow_state");

        final String onlyVisOverridesStr = json.getStringProperty("only_visible_to_overrides");
        if ("true".equalsIgnoreCase(onlyVisOverridesStr)) {
            this.onlyVisibleToOverrides = Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(onlyVisOverridesStr)) {
            this.onlyVisibleToOverrides = Boolean.FALSE;
        } else {
            this.onlyVisibleToOverrides = null;
        }

        final String omitFromGradeStr = json.getStringProperty("omit_from_final_grade");
        if ("true".equalsIgnoreCase(omitFromGradeStr)) {
            this.omitFromFinalGrade = Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(omitFromGradeStr)) {
            this.omitFromFinalGrade = Boolean.FALSE;
        } else {
            this.omitFromFinalGrade = null;
        }

        final Double allowedAttemptsDbl = json.getNumberProperty("allowed_attempts");
        this.allowedAttempts = allowedAttemptsDbl == null ? null : Long.valueOf(allowedAttemptsDbl.longValue());

        this.overrides = new ArrayList<>(10);

        final Object overridesObj = json.getProperty("overrides");
        if (overridesObj instanceof final Object[] objArray) {
            for (final Object obj : objArray) {
                if (obj instanceof final JSONObject jsonObj) {
                    this.overrides.add(new AssignmentOverride(jsonObj));
                }
            }
        }
    }
}
