package dev.mathops.app.ops.snapin.messaging;

/**
 * Program-related situation that a user can be in, not specific to any course.
 */
public enum ESituationProgram {

    /** Student does not have prerequisites for any registered courses. */
    NO_PREREQ_FOR_ANY_COURSE,

    /** Student does not have any record of accessing website (starting course, trying User's). */
    NOT_ACCESSED_WEB_SITE,

    // NOTE: Student could do User's Exam before starting their first course, or they could start
    // a course then do the user's exam.

    /**
     * First course has not been started, but user's exam has been tried - student can pass the User's exam or start a
     * course.
     */
    COURSE_NOT_STARTED_USERS_ATTEMPTED,

    /**
     * First course has not been started, but user's exam has been passed - student should start their first course.
     */
    COURSE_NOT_STARTED_USERS_PASSED,

    /**
     * First course has been started, but User's exam has not been tried - students should try the User's exam.
     */
    COURSE_STARTED_USERS_NOT_TRIED,

    /**
     * First course has been started, and the User's exam has been tried - students should pass the User's exam.
     */
    COURSE_STARTED_USERS_ATTEMPTED,

    /** Student is working in courses - at least one course is open or available to start. */
    WORKING_IN_COURSES,

    /**
     * Student is blocked from working in any course because a course deadline has passed. NOTE: Student should still be
     * able to re-test in courses they had completed earlier.
     */
    BLOCKED,

    /** Student has finished all courses with A grades - nothing left to do. */
    FINISHED,
}
