package dev.mathops.app.assessment.qualitycontrol;

/**
 * A progress update.
 */
final class ProgressUpdate {

    /** The percentage finished. */
    final float percentDone;

    /** The current step. */
    final String onStep;

    /** The report contents. */
    final String report;

    /**
     * Constructs a new progress update.
     *
     * @param thePercentDone the percentage finished
     * @param theOnStep      the current step
     * @param theReport      the report contents
     */
    ProgressUpdate(final float thePercentDone, final String theOnStep, final String theReport) {

        this.percentDone = thePercentDone;
        this.onStep = theOnStep;
        this.report = theReport;
    }
}
