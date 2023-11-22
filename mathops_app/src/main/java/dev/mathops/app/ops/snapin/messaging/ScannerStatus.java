package dev.mathops.app.ops.snapin.messaging;

/**
 * Progress status.
 */
public final class ScannerStatus {

    /** The number of steps completed. */
    public final int stepsCompleted;

    /** The total number of steps. */
    public final int totalSteps;

    /** The description, to use as progress bar text. */
    public final String description;

    /**
     * Constructs a new {@code ScannerStatus}.
     *
     * @param theStepsCompleted the number of steps completed
     * @param theTotalSteps     the total number of steps
     * @param theDescription    the description
     */
    public ScannerStatus(final int theStepsCompleted, final int theTotalSteps, final String theDescription) {

        this.stepsCompleted = theStepsCompleted;
        this.totalSteps = theTotalSteps;
        this.description = theDescription;
    }
}
