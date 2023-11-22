package dev.mathops.app.ops.snapin.messaging;

/**
 * The student's situation within a particular course - each student will have a situation in each of their registered
 * courses.
 */
public enum ESituationCourse {

    /** Open status is "G" (forfeit of a late course). */
    FORFEIT_LAST,

    /** Open status is "N" (forfeit of an early course). */
    CLOSED_FIRST,

    /** Open status is null, prerequisites have been met. */
    NOT_YET_OPENED_HAS_PREREQ,

    /** Open status is null, prerequisites have not been met. */
    NOT_YET_OPENED_NO_PREREQ,

    /** SR has not been passed. */
    SR_NOT_PASSED,

    /** SR has been passed, R1 has not been passed. */
    R1_NOT_PASSED,

    /** R1 has been passed, U1 has not been passed. */
    U1_NOT_PASSED,

    /** U1 has been passed, R2 has not been passed. */
    R2_NOT_PASSED,

    /** R2 has been passed, U2 has not been passed. */
    U2_NOT_PASSED,

    /** U2 has been passed, R3 has not been passed. */
    R3_NOT_PASSED,

    /** R3 has been passed, U3 has not been passed. */
    U3_NOT_PASSED,

    /** U3 has been passed, R4 has not been passed. */
    R4_NOT_PASSED,

    /** R4 has been passed, U4 has not been passed. */
    U4_NOT_PASSED,

    /** U4 has been passed, FIN has not been passed. */
    FIN_NOT_PASSED,

    /** FIN has been passed, re-testing could improve grade. */
    FIN_PASSED_GRADE_CAN_IMPROVE,

    /** FIN has been passed, re-testing would not improve grade. */
    FINISHED,

    /** Locked out because final deadline has passed. */
    LOCKED_OUT,
}
