package dev.mathops.app.adm.student;

/**
 * Possible status codes for a step within a course.
 */
public enum ECourseStepStatus {

    /** Not yet attempted, on time if done now. */
    NOT_YET_ATTEMPTED_ON_TIME,

    /** Not yet attempted, late. */
    NOT_YET_ATTEMPTED_LATE,

    /** Attempted, but not yet passed, on time if done now. */
    NOT_YET_PASSED_ON_TIME,

    /** Attempted, but not yet passed, on time if done now. */
    NOT_YET_PASSED_LATE,

    /** Passed on time. */
    PASSED_ON_TIME,

    /** Passed late. */
    PASSED_LATE,
}
