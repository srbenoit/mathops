package dev.mathops.dbjobs.report.analytics.irdata;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An examination of longitudinal performance of students based on placement results, precalculus course outcomes.
 */
final class AnalyzeLongitudinalPerformance {

    /** Extract data. */
    private final Data data;

    /**
     * Constructs a new {@code AnalyzeLongitudinalPerformance}.
     */
    private AnalyzeLongitudinalPerformance() {

        this.data = new Data();
    }

    /**
     * Executes the job.
     *
     * @return a report
     */
    private String execute() {

        final Collection<String> report = new ArrayList<>(50);

        if (this.data.isLoadSuccessful(report)) {

            report.add(CoreConstants.EMPTY);
            identifyTrajectories(report);

            // TODO:
        }

        final int numReportLines = report.size();
        final HtmlBuilder htm = new HtmlBuilder(numReportLines * 40);
        for (final String rep : report) {
            htm.addln(rep);
        }

        return htm.toString();
    }

    /**
     * Categories students based on the "trajectory" of their time at CSU.  A "trajectory" is a unique combination of
     * the tuples of {termSeq, censusClass, censusFlag, deceased, eotFlag, graduated} over the sequence of terms.
     *
     * @param report a collection to which to add report lines
     */
    private void identifyTrajectories( final Collection<? super String> report) {

        final Map<String, Map<Integer, TermRecord>> termData = this.data.getTermData();

        final Set<String> distinctTrajectories = new HashSet<>(100);

        for (final Map.Entry<String, Map<Integer, TermRecord>> entry : termData.entrySet()) {
            final Map<Integer, TermRecord> termRecords = entry.getValue();
            final String trajectoryString = makeTrajectoryString(termRecords);

            if (!distinctTrajectories.contains(trajectoryString)) {
                Log.fine(trajectoryString);
                distinctTrajectories.add(trajectoryString);
            }
        }

        report.add("Discovered " + distinctTrajectories.size() + " distinct trajectories");
    }

    /**
     * Makes a trajectory string based on a list of term records.
     *
     * @param termRecords the (sorted) map from term sequence to term data
     * @return the trajectory string
     */
    private static String makeTrajectoryString(final Map<Integer, TermRecord> termRecords) {

        final HtmlBuilder htm = new HtmlBuilder(40);

        for (final TermRecord rec : termRecords.values()) {
            htm.add(rec.termSeq());
            htm.add(Boolean.TRUE.equals(rec.graduated()) ? "Y" : "N");
        }
        return htm.toString();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "LongitudinalPerformance";
    }

    /**
     * Main method to run the job.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final AnalyzeLongitudinalPerformance job = new AnalyzeLongitudinalPerformance();

        final String report = job.execute();

        Log.fine(CoreConstants.EMPTY);
        Log.fine(CoreConstants.EMPTY);
        Log.fine(report);
        Log.fine(CoreConstants.EMPTY);
        Log.fine(CoreConstants.EMPTY);
    }
}
