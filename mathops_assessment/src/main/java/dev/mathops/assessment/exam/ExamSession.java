package dev.mathops.assessment.exam;

import java.util.ArrayList;
import java.util.List;

/**
 * An exam session, which provides the public interface used to access an exam. This interface should not change when
 * the structure of the exam object changes, although constructors may change.
 */
public final class ExamSession {

    /** The session state. */
    private EExamSessionState state;

    /** The exam object. */
    private final ExamObj exam;

    /** Session listeners. */
    private final List<IExamSessionListener> listeners;

    /**
     * Constructs a new {@code ExamSession}.
     *
     * @param theState the current state of this session
     * @param theExam  the exam (which stores currently active section/problem, user answers, timestamps of creation,
     *                 realization, presentation, and completion, and flags to indicate whether the exam is being taken
     *                 remotely and whether it was proctored
     */
    public ExamSession(final EExamSessionState theState, final ExamObj theExam) {

        this.state = theState;
        this.exam = theExam;
        this.listeners = new ArrayList<>(2);
    }

    /**
     * Gets the exam session state.
     *
     * @return the state
     */
    public EExamSessionState getState() {

        synchronized (this) {
            return this.state;
        }
    }

    /**
     * Sets the exam session state.
     *
     * @param theState the new state
     */
    public void setState(final EExamSessionState theState) {

        synchronized (this) {
            this.state = theState;
            fireExamSessionStateChanged(theState);
        }
    }

    /**
     * Gets the exam model.
     *
     * @return the exam model.
     */
    public ExamObj getExam() {

        return this.exam;
    }

    /**
     * Adds a listener to receive event notifications.
     *
     * @param theListener the listener to add
     */
    public void addListener(final IExamSessionListener theListener) {

        synchronized (this) {
            this.listeners.add(theListener);
        }
    }

    /**
     * Sends a notification to all registered listeners that the exam session state has changed.
     *
     * @param newState the new state
     */
    private void fireExamSessionStateChanged(final EExamSessionState newState) {

        synchronized (this) {
            for (final IExamSessionListener listener : this.listeners) {
                listener.examSessionStateChanged(newState);
            }
        }
    }
}
