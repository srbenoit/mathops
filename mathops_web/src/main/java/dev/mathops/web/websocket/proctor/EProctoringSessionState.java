package dev.mathops.web.websocket.proctor;

/**
 * A proctoring session state.
 */
/* default */ enum EProctoringSessionState {

    /** Awaiting the student photo. */
    AWAITING_STUDENT_PHOTO,

    /** Awaiting the student ID. */
    AWAITING_STUDENT_ID,

    /** Scanning environment. */
    ENVIRONMENT,

    /** Presenting instructions to student. */
    SHOWING_INSTRUCTIONS,

    /** Student is in the assessment. */
    ASSESSMENT,

    /** Assessment is finished. */
    FINISHED
}
