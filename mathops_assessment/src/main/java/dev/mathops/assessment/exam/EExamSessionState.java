package dev.mathops.assessment.exam;

/**
 * Possible states for an exam session.
 */
public enum EExamSessionState {

    /** Items have been selected and ordered, template variables on selected items have values. */
    GENERATED(false, false, false),

    /**
     * User is interacting with the exam - navigating, reading instructions, or attempting item.
     */
    INTERACTING(false, false, false),

    /**
     * The user has submitted the exam for scoring, but scoring is not complete (this state allows for off-line scoring
     * of written responses).
     */
    SUBMITTED(false, false, false),

    /** Exam has been scored and recorded and is closed. */
    CLOSED(false, false, false),

    /**
     * User is reviewing exam after closure, with scores, correct/incorrect marks, and complete solutions shown.
     */
    REVIEW_WITH_SOLUTIONS(true, true, true);

    /** Flag indicating correctness of responses should be shown. */
    public final boolean showCorrectness;

    /** Flag indicating answers should be shown. */
    public final boolean showAnswers;

    /** Flag indicating solutions should be shown. */
    public final boolean showSolutions;

    /**
     * Constructs a new {@code EExamSessionState}.
     *
     * @param isShowCorrectness flag indicating correctness of responses should be shown
     * @param isShowAnswers     flag indicating answers should be shown
     * @param isShowSolutions   flag indicating solutions should be shown
     */
    EExamSessionState(final boolean isShowCorrectness, final boolean isShowAnswers,
                      final boolean isShowSolutions) {

        this.showCorrectness = isShowCorrectness;
        this.showAnswers = isShowAnswers;
        this.showSolutions = isShowSolutions;
    }
}
