package dev.mathops.web.site.html.lta;

/**
 * The state of a learning target assignment.
 */
public enum ELtaState {

    /** Initial - not yet generated. */
    INITIAL,

    /** Instructions. */
    INSTRUCTIONS,

    /** Interacting with item [current-item-index]. */
    ITEM_NN,

    /** Submit pressed while interacting with item [current-item-index]. */
    SUBMIT_NN,

    /** Assignment has been scored and recorded. */
    COMPLETED,

    /** Showing solutions for item [current-item-index]. */
    SOLUTION_NN
}
