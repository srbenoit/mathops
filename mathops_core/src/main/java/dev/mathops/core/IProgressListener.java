package dev.mathops.core;

/**
 * A listener for progress updates.
 */
@FunctionalInterface
public interface IProgressListener {

    /**
     * Indicates progress.
     *
     * @param description    a description of the current operation
     * @param stepsCompleted the number of steps completed
     * @param totalSteps     the total number of steps
     */
    void progress(final String description, int stepsCompleted, int totalSteps);
}
