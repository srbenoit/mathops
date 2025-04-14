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

    /** The number of errors so far. */
    final int errorsSoFar;

    /**
     * Constructs a new progress update.
     *
     * @param thePercentDone the percentage finished
     * @param theOnStep      the current step
     * @param theReport      the report contents
     * @param theReport      the report contents
     * @param theErrorsSoFar the number of errors so far
     */
    ProgressUpdate(final float thePercentDone, final String theOnStep, final String theReport,
                   final int theErrorsSoFar) {

        this.percentDone = thePercentDone;
        this.onStep = theOnStep;
        this.report = theReport;
        this.errorsSoFar = theErrorsSoFar;
    }
}
