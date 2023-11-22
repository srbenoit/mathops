package dev.mathops.web.site.html;

/**
 * The state of a forced termination process.
 */
public enum EForceTerminateState {

    /** No forced termination in progress. */
    NONE,

    /** A forced abort without scoring has been requested. */
    ABORT_WITHOUT_SCORING_REQUESTED,

    /** A request to abort without scoring has been confirmed. */
    ABORT_WITHOUT_SCORING_CONFIRMED,

    /** A forced submit with scoring has been requested. */
    SUBMIT_AND_SCORE_REQUESTED,

    /** A request to submit and score has been confirmed. */
    SUBMIT_AND_SCORE_CONFIRMED
}
