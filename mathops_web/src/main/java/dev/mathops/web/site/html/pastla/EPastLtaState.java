package dev.mathops.web.site.html.pastla;

/**
 * The state of a past learning target assignment.
 */
public enum EPastLtaState {

    /** Initial - not yet generated. */
    INITIAL,

    /** Unable to load exam. */
    CANT_LOAD_EXAM,

    /** Displaying instructions. */
    INSTRUCTIONS,

    /** Displaying with item [current-item-index]. */
    ITEM_NN
}
