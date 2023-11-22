package dev.mathops.web.site.course;

/**
 * The phases of a schedule which must be completed in order with respect to courses that count toward a student's
 * pace.
 */
enum ESchedulePhase {

    /** Incompletes from prior terms that have been completed. */
    inc_completed,

    /** Incompletes from prior terms that are currently open. */
    inc_open,

    /** Incompletes from prior terms that are not yet open. */
    inc_unopened,

    /** Current term courses that have been completed. */
    completed,

    /** Current term courses that are currently open. */
    open
}
