
package dev.mathops.app.ops.snapin.canvas;

/**
 * Progress status.
 */
final class SyncherStatus {

    /** The number of steps completed. */
    final int stepsCompleted;

    /** The total number of steps. */
    final int totalSteps;

    /** The description, to use as progress bar text. */
    final String description;

    /**
     * Constructs a new {@code SyncherStatus}.
     *
     * @param theStepsCompleted the number of steps completed
     * @param theTotalSteps     the total number of steps
     * @param theDescription    the description
     */
    SyncherStatus(final int theStepsCompleted, final int theTotalSteps, final String theDescription) {

        this.stepsCompleted = theStepsCompleted;
        this.totalSteps = theTotalSteps;
        this.description = theDescription;
    }
}
