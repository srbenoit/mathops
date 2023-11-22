package dev.mathops.app.ops.snapin.messaging;

/**
 * A time period within a course, relative to course deadlines.
 */
public enum ECourseTimePeriod {

    /** Before R1 deadline becomes "close". */
    WELL_BEFORE_R1_DUE,

    /** R1 deadline is "close" (includes day R1 is due). */
    R1_DUE_SOON,

    /** After R1 deadline, but before R2 deadline becomes "close". */
    WELL_BEFORE_R2_DUE,

    /** R2 deadline is "close" (includes day R2 is due). */
    R2_DUE_SOON,

    /** After R2 deadline, but before R3 deadline becomes "close". */
    WELL_BEFORE_R3_DUE,

    /** R3 deadline is "close" (includes day R3 is due). */
    R3_DUE_SOON,

    /** After R3 deadline, but before R4 deadline becomes "close". */
    WELL_BEFORE_R4_DUE,

    /** R4 deadline is "close" (includes day R4 is due). */
    R4_DUE_SOON,

    /** After R4 deadline, but before Final exam deadline becomes "close". */
    WELL_BEFORE_FIN_DUE,

    /** Final Exam deadline is "close" (includes day Final is due). */
    FIN_DUE_SOON,

    /** Final exam deadline has passed, but student has last try. */
    LAST_TRY_AVAILABLE,

    /** Final was passed on time, course remains open for re-testing. */
    RETESTING,

    /** Final exam not passed on time, student is locked out of course. */
    LOCKED_OUT,
}
