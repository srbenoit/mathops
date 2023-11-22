package dev.mathops.web.site.html.pastexam;

/**
 * The state of a review exam (state may also include an item index).
 */
public enum EPastExamState {

    /** Initial - not yet generated. */
    INITIAL,

    /** Unable to load exam. */
    CANT_LOAD_EXAM,

    /** Displaying instructions. */
    INSTRUCTIONS,

    /** Displaying with item [current-item-index]. */
    ITEM_NN
}
