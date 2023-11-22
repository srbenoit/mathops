package dev.mathops.web.site.html.hw;

/**
 * The state of a homework assignment.
 */
public enum EHomeworkState {

    /** Initial - not yet generated. */
    INITIAL,

    /** Interacting - generated, but not yet completed. */
    INTERACTING,

    /** "You are correct" message, with link to next problem. */
    CORRECT_NEXT,

    /** "You are correct" message with link to submit. */
    CORRECT_SUBMIT,

    /** "You are incorrect" message. */
    INCORRECT_MSG,

    /** "You are incorrect" message with an option to show answers. */
    INCORRECT_SHOW_ANS,

    /** "You are incorrect" message with an option to show solutions. */
    INCORRECT_SHOW_SOL,

    /** Showing answers after an incorrect response. */
    SHOW_ANSWER,

    /** Showing solutions after an incorrect response. */
    SHOW_SOLUTION,

    /** Completed - recorded results. */
    COMPLETED
}
