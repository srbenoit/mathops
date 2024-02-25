package dev.mathops.web.site.html.item;

/**
 * The state of an inline item.
 */
public enum EItemState {

    /** Initial - not yet generated. */
    INITIAL,

    /** Interacting - generated, but not yet completed - inputs and submit button are enabled. */
    INTERACTING,

    /** Interacting, with hint shown - inputs and submit button are enabled. */
    INTERACTING_HINTS,

    /** "You are correct" message, with possible link to try another revision. */
    CORRECT,

    /** "You are correct" with answers and possible link to try another revision. */
    CORRECT_SHOW_ANSWER,

    /** "You are correct" with solution and possible link to try another revision. */
    CORRECT_SHOW_SOLUTION,

    /** "You are incorrect" message; if more attempts, inputs and submit button are enabled. */
    INCORRECT,

    /** "You are incorrect" with answers; if more attempts, inputs and submit button are enabled. */
    INCORRECT_SHOW_ANSWER,

    /** "You are incorrect" with solution; if more attempts, inputs and submit button are enabled. */
    INCORRECT_SHOW_SOLUTION,
}
