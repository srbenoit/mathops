package dev.mathops.dbjobs.report.analytics;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStqaLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A program that takes a look at final exams for students who have not passed the course, where the final exam score
 * was 14 or 15, and where the student had gotten one or more of the missed items correct on an earlier final exam
 * attempt.
 */
final class FinalExamsToReexamine {

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code UrgencyHistoryAnalysis}.
     *
     * @param theCache the data cache
     */
    private FinalExamsToReexamine(final Cache theCache) {

        this.cache = theCache;
    }

    /**
     * Executes the job.
     */
    private void calculate() {

        // Fetch all registrations with open_status 'Y' and completed 'N'
        try {
            final TermRec active = this.cache.getSystemData().getActiveTerm();
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(this.cache, active.term, false, false);

            allRegs.removeIf(row -> (!"Y".equals(row.openStatus) || !"N".equals(row.completed)));

            Log.info("Found " + allRegs.size() + " registrations to examine...");

            for (final RawStcourse reg : allRegs) {
                checkReg(reg);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Checks a single registration.
     *
     * @param reg the registration to check
     * @throws SQLException if there is an error accessing the database
     */
    private void checkReg(final RawStcourse reg) throws SQLException {

        // Query all exams for the student and filter down to just final exams
        final List<RawStexam> allFinals = RawStexamLogic.queryByStudentCourse(this.cache, reg.stuId, reg.course, true);

        allFinals.removeIf(stexam -> !"F".equals(stexam.examType));

        boolean scored14OnFinal = false;
        boolean finalNotPassed = true;

        final List<RawStexam> toReview = new ArrayList<>(5);
        final Map<Long, List<RawStqa>> answers = new HashMap<>(allFinals.size());

        for (final RawStexam stexam : allFinals) {
            if ("F".equals(stexam.examType)) {

                if ((stexam.examScore.intValue() >= 14) && "N".equals(stexam.passed)) {
                    toReview.add(stexam);
                    scored14OnFinal = true;
                }
                if ("Y".equals(stexam.passed)) {
                    finalNotPassed = false;
                }
            }

            final List<RawStqa> qa = RawStqaLogic.queryBySerial(this.cache, stexam.serialNbr);
            answers.put(stexam.serialNbr, qa);
        }

        if (scored14OnFinal && finalNotPassed) {
            Log.fine("Student " + reg.stuId + " in course " + reg.course + " has " + toReview.size()
                     + " exams to review:");

            // Map from item number incorrect to number of times that item was answered correctly.
            final Map<Integer, Integer> incorrect = new HashMap<>(6);
            final HtmlBuilder htm = new HtmlBuilder(100);

            for (final RawStexam review : toReview) {
                final Long serial = review.serialNbr;

                // Find the answers that were marked wrong on this exam...
                final List<RawStqa> qa = answers.get(serial);
                incorrect.clear();

                for (final RawStqa item : qa) {
                    if ("N".equals(item.ansCorrect)) {
                        incorrect.put(item.questionNbr, Integer.valueOf(0));
                    }
                }

                // For each of these, see if they had answered that item correctly before

                for (final Map.Entry<Integer, Integer> entry : incorrect.entrySet()) {

                    for (final RawStexam stexam : allFinals) {
                        final List<RawStqa> ans = answers.get(stexam.serialNbr);

                        for (final RawStqa test : ans) {
                            if (test.questionNbr.equals(entry.getKey()) && "Y".equals(test.ansCorrect)) {

                                entry.setValue(Integer.valueOf(entry.getValue().intValue() + 1));
                            }
                        }
                    }
                }

                htm.reset();
                htm.addln("    Serial number ", serial, " score was ", review.examScore, ":");

                int count = 0;
                for (final Map.Entry<Integer, Integer> entry : incorrect.entrySet()) {
                    if (entry.getValue().intValue() > 0) {
                        htm.addln("        Item ", entry.getKey(), " was answered correctly ",
                                entry.getValue(), " times elsewhere.");
                        ++count;
                    }
                }

                if ((review.examScore.intValue() + count) >= 16) {
                    Log.fine(htm.toString());
                }
            }
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);

        if (profile == null) {
            Log.warning("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(profile);
            Log.info("Connected to " + profile.id);

            final FinalExamsToReexamine obj = new FinalExamsToReexamine(cache);
            obj.calculate();
        }
    }
}
