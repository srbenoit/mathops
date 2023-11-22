package dev.mathops.web.site.html.placementexam;

/**
 * The state of a placement exam (state may also include an item index).
 */
public enum EPlacementExamState {

    /** Initial - not yet generated. */
    INITIAL,

    /** Error or ineligible to start exam. */
    ERROR,

    /** Profile questions. */
    PROFILE,

    /** Instructions. */
    INSTRUCTIONS,

    /** Interacting with item [current-item-index]. */
    ITEM_NN,

    /** Submit pressed while interacting with item [current-item-index]. */
    SUBMIT_NN,

    /** Exam has been scored and recorded. */
    COMPLETED
}
