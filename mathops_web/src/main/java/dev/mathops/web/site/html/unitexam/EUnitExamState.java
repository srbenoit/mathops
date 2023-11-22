package dev.mathops.web.site.html.unitexam;

/**
 * The state of a unit exam (state may also include an item index).
 */
public enum EUnitExamState {

    /** Initial - not yet generated. */
    INITIAL,

    /** Instructions. */
    INSTRUCTIONS,

    /** Interacting with item [current-item-index]. */
    ITEM_NN,

    /** Submit pressed while interacting with item [current-item-index]. */
    SUBMIT_NN,

    /** Exam has been scored and recorded. */
    COMPLETED,

    /** Showing solutions for item [current-item-index]. */
    SOLUTION_NN
}
