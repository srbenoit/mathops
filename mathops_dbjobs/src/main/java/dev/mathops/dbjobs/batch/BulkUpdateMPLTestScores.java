package dev.mathops.dbjobs.batch;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.logic.mathplan.MathPlanLogic;
import dev.mathops.db.old.logic.mathplan.MathPlanPlacementStatus;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A job that searches for all students with a "WLCM5" MathPlan outcome, and then make sures that student has a
 * corresponding "MPL" test score in SORTEST.  If the SORTEST record is missing, a record is inserted.
 *
 * <p>
 * The logic for an individual student is as follows:
 * <pre>
 * IF
 *     the student has any responses to MATH PLAN records
 * THEN
 *     IF
 *         the student's Math Plan recommendation is any AUCC-1B course
 *     THEN
 *         the student should have a "MPL" test score of "1" to indicate Math Placement is not needed
 *     ELSE IF
 *         the student has completed the Math Placement tool
 *     THEN
 *         the student should have a "MPL" test score of "1" to indicate Math Placement is not needed
 *     ELSE IF
 *         the student has any MATH transfer credit that clears 1B or satisfies the prerequisite for MATH 117
 *     THEN
 *         the student should have a "MPL" test score of "1" to indicate Math Placement is not needed
 *     ELSE
 *         the student should have a "MPL" test score of "2" to indicate Math Placement is needed
 *     END IF
 * ELSE
 *     the student should not have any "MPL" test score at all, to indicate the Math Plan is not yet complete
 * END IF
 * </pre>
 */
public class BulkUpdateMPLTestScores {

    /** Debug flag - true to skip (but print) updates; false to actually perform updates. */
    private static final boolean DEBUG = false;

    /** The test code. */
    private static final String TEST_CODE = "MPL";

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The Primary database context. */
    private final DbContext primaryCtx;

    /** The live data database context. */
    private final DbContext liveCtx;

    /**
     * Constructs a new {@code BulkUpdateMPLTestScores}.
     */
    public BulkUpdateMPLTestScores() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        this.liveCtx = this.dbProfile.getDbContext(ESchemaUse.LIVE);
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.dbProfile == null) {
            final String msg = "Unable to create production context.";
            Log.warning(msg);
            report.add(msg);
        } else if (this.primaryCtx == null) {
            final String msg = "Unable to create PRIMARY database context.";
            Log.warning(msg);
            report.add(msg);
        } else if (this.liveCtx == null) {
            final String msg = "Unable to create LIVE database context.";
            Log.warning(msg);
            report.add(msg);
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);
                try {
                    execute(cache, report);
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                final String msg = HtmlBuilder.concat("EXCEPTION: ", ex.getMessage());
                Log.warning(msg);
                report.add(msg);
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        htm.addln("<pre>");
        for (final String rep : report) {
            htm.addln(rep);
        }
        htm.addln("</pre>");

        return htm.toString();
    }

    /**
     * Executes the job.
     *
     * @param cache the data cache
     * @param report a list of strings to which to add report output lines
     * @throws SQLException if there is an error accessing the database
     */
    public void execute(final Cache cache, final Collection<? super String> report) throws SQLException {

        // Determine the list of students who should have MPL test scores of some kind
        final String msg1 = "Scanning student MathPlan status...";
        Log.fine(msg1);
        report.add(msg1);

        final List<RawStmathplan> allStMathPlan = RawStmathplanLogic.INSTANCE.queryAll(cache);
        final int size = allStMathPlan.size();
        final String sizeStr = Integer.toString(size);

        final String msg2 = HtmlBuilder.concat("    Found ", sizeStr, " MathPlan responses");
        report.add(msg2);
        Log.fine(msg2);

        final Map<String, RawStmathplan> latest1 = new HashMap<>(25000);

        // Find the most recent "WLCM5" rows
        for (final RawStmathplan row : allStMathPlan) {
            if ("WLCM5".equals(row.version) && "Y".equals(row.stuAnswer)) {
                final LocalDateTime when = row.getWhen();

                if (ONE.equals(row.surveyNbr)) {
                    final RawStmathplan existing1 = latest1.get(row.stuId);
                    if (existing1 == null) {
                        latest1.put(row.stuId, row);
                    } else {
                        final LocalDateTime existingWhen = existing1.getWhen();
                        if (existingWhen == null || existingWhen.isBefore(when)) {
                            latest1.put(row.stuId, row);
                        }
                    }
                }
            }
        }

        final int size1 = latest1.size();
        final String size1Str = Integer.toString(size1);
        final String msg3 = HtmlBuilder.concat("    Found ", size1Str, " 'WLCM5' question 1 responses");
        Log.fine(msg3);
        report.add(msg3);

        final Collection<String> stuIds = new HashSet<>(25000);
        final Set<String> keys1 = latest1.keySet();
        stuIds.addAll(keys1);

        final int sizeAll = stuIds.size();
        final String sizeAllStr = Integer.toString(sizeAll);
        final String msg4 = HtmlBuilder.concat("    Found ", sizeAllStr, " distinct students with responses");
        Log.fine(msg4);
        report.add(msg4);

        // Compare results with SORTEST table
        Log.fine(CoreConstants.EMPTY);
        final String msg5 = "Scanning SORTEST table...";
        Log.fine(msg5);
        report.add(msg5);

        final DbConnection liveConn = this.liveCtx.checkOutConnection();
        final LocalDateTime now = LocalDateTime.now();
        try {
            int count1 = 0;
            int count2 = 0;
            int already1 = 0;
            int already2 = 0;
            for (final String stuId : stuIds) {

                RawStudent student = RawStudentLogic.query(cache, stuId, false);
                if (student == null) {
                    final String msg = HtmlBuilder.concat("   WARNING: Student ", stuId, " needed to be retrieved");
                    Log.fine(msg);
                    report.add(msg);
                    student = RawStudentLogic.query(cache, stuId, true);
                }

                if (student == null) {
                    final String msg = HtmlBuilder.concat("   ERROR: Student ", stuId, " not found!");
                    Log.warning(msg);
                    report.add(msg);
                } else if (student.pidm == null) {
                    final String msg = HtmlBuilder.concat("   ERROR: Student ", stuId, " has no PIDM!");
                    Log.warning(msg);
                    report.add(msg);
                } else {
                    final List<RawMpscorequeue> existing = RawMpscorequeueLogic.querySORTESTByStudent(liveConn,
                            student.pidm);

                    RawMpscorequeue mostRecent = null;
                    for (final RawMpscorequeue test : existing) {
                        if (TEST_CODE.equals(test.testCode)) {
                            if (mostRecent == null || mostRecent.testDate.isBefore(test.testDate)) {
                                mostRecent = test;
                            }
                        }
                    }

                    final MathPlanPlacementStatus status = MathPlanLogic.getMathPlacementStatus(cache, stuId);

                    String wantValue = null;
                    if (latest1.containsKey(stuId)) {
                        if (status.isPlacementComplete) {
                            wantValue = "1";
                        } else if (status.isPlacementNeeded) {
                            wantValue = "2";
                        } else {
                            wantValue = "1";
                        }
                    }

                    boolean doInsert = false;
                    if (wantValue == null) {
                        if (mostRecent != null) {
                            final String msg = HtmlBuilder.concat("Student ", stuId,
                                    " who has not completed MathPlan has a MPL score of ", mostRecent.testScore);
                            Log.warning(msg);
                            report.add(msg);
                        }
                    } else if (mostRecent == null) {
                        // Insert the new score
                        doInsert = true;
                    } else if (!wantValue.equals(mostRecent.testScore)) {
                        // Score has changed - insert a new score
                        doInsert = true;
                    }

                    if (doInsert) {
                        // Score has changed - insert a new score
                        if (DEBUG) {
                            final String msg = HtmlBuilder.concat("   Need to insert MPL=", wantValue,
                                    " test score for ", stuId);
                            Log.fine(msg);
                            report.add(msg);
                        } else {
                            final String msg = HtmlBuilder.concat("   Inserting MPL=", wantValue, " test score for ",
                                    stuId);
                            Log.fine(msg);
                            report.add(msg);

                            final RawMpscorequeue toInsert = new RawMpscorequeue(student.pidm, TEST_CODE, now,
                                    wantValue);
                            if (!RawMpscorequeueLogic.insertSORTEST(liveConn, toInsert)) {
                                final String msg6 = HtmlBuilder.concat("   ERROR: Failed to insert MPL=", wantValue,
                                        " test score for ", stuId);
                                Log.warning(msg6);
                                report.add(msg6);
                            }
                        }
                        if ("2".equals(wantValue)) {
                            ++count2;
                        } else{
                            ++count1;
                        }
                    }
                }
            }

            final String count1Str = Integer.toString(count1);
            final String msg6 = HtmlBuilder.concat("    Found ", count1Str, " to update to score 1");
            Log.fine(msg6);
            report.add(msg6);

            final String count2Str = Integer.toString(count2);
            final String msg7 = HtmlBuilder.concat("    Found ", count2Str, " to update to score 2");
            Log.fine(msg7);
            report.add(msg7);

            final String already1Str = Integer.toString(already1);
            final String msg8 = HtmlBuilder.concat("    Found ", already1Str, " already with score 1");
            Log.fine(msg8);
            report.add(msg8);

            final String already2Str = Integer.toString(already2);
            final String msg9 = HtmlBuilder.concat("    Found ", already2Str, " already with score 2");
            Log.fine(msg9);
            report.add(msg9);
        } finally {
            this.liveCtx.checkInConnection(liveConn);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final BulkUpdateMPLTestScores job = new BulkUpdateMPLTestScores();

        Log.fine(job.execute());
    }
}
