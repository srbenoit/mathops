package dev.mathops.assessment.problem;

/**
 * Problem modes, to control generation of HTML, LaTeX, etc.
 */
public enum EProblemMode {

    /** Question only - no answer or solution. */
    QUESTION_ONLY,

    /** The question plus the correct answer, but no complete solution. */
    QUESTION_WITH_ANSWER,

    /** The question plus the correct answer and complete solution. */
    QUESTION_WITH_SOLUTION
}
