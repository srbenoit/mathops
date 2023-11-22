package dev.mathops.assessment.exam;

/**
 * A listener interface that can receive event notifications from an exam session.
 */
@FunctionalInterface
public interface IExamSessionListener {

    /**
     * Fired when the exam session state changes.
     *
     * @param newState the new state
     */
    void examSessionStateChanged(EExamSessionState newState);
}
