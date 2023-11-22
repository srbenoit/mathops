package dev.mathops.app.problem;

/**
 * A listener interface to be implemented by classes that want to receive notifications that a student has entered or
 * cleared an answer on a problem.
 */
public interface AnswerListener {

    /**
     * Record a student's answer.
     *
     * @param answer a list of answer objects, whose type depends on the type of problem for which the answer is being
     *               submitted; the answers will be passed directly into the {@code PresentedProblem} object
     */
    void recordAnswer(Object[] answer);

    /**
     * Clear a student's answer.
     */
    void clearAnswer();
}
