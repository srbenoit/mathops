package dev.mathops.web.site;

/**
 * Supported types of proctoring.
 */
public enum EProctoringType {

    /** No proctoring. */
    NONE,

    /** Proctoring within the departmental testing center. */
    DEPT_TESTING_CENTER,

    /** Proctoring within the University testing center. */
    UNIV_TESTING_CENTER,

    /** Proctoring within the Student Disability testing center. */
    SDC_TESTING_CENTER,

    /** Online proctoring through ProctorU. */
    PROCTORU,

    /** Online proctoring through Honorlock. */
    HONORLOCK,

    /** Online proctoring through Respondus. */
    RESPONDUS,

    /** Online proctoring through RamWork. */
    RAMWORK,

    /** Online proctoring by departmental staff. */
    STAFF_PROCTORING
}
