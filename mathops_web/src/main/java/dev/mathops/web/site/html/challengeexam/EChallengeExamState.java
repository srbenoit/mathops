package dev.mathops.web.site.html.challengeexam;

/**
 * The state of a challenge exam (state may also include an item index).
 */
public enum EChallengeExamState {

    /** Initial - not yet generated. */
    INITIAL,

    /** Error or ineligible to start exam. */
    ERROR,

    /** Instructions. */
    INSTRUCTIONS,

    /** Interacting with item [current-item-index]. */
    ITEM_NN,

    /** Submit pressed while interacting with item [current-item-index]. */
    SUBMIT_NN,

    /** Exam has been scored and recorded. */
    COMPLETED
}
