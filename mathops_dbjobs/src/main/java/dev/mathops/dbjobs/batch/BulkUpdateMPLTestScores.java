package dev.mathops.dbjobs.batch;

import dev.mathops.commons.CoreConstants;
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
public enum BulkUpdateMPLTestScores {
    ;

    /** Debug flag - true to skip (but print) updates; false to actually perform updates. */
    private static final boolean DEBUG = false;

    /** The test code. */
    private static final String TEST_CODE = "MPL";

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /** A commonly used integer. */
    private static final Integer TWO = Integer.valueOf(2);

    /**
     * Executes the job.
     *
     * @param cache the data cache
     * @param liveCtx the "LIVE" database context
     * @throws SQLException if there is an error accessing the database
     */
    public static void execute(final Cache cache, final DbContext liveCtx) throws SQLException {

        // Determine the list of students who should have MPL test scores of some kind
        Log.fine("Scanning student MathPlan status...");

        final List<RawStmathplan> allStMathPlan = RawStmathplanLogic.INSTANCE.queryAll(cache);
        final int size = allStMathPlan.size();
        final String sizeStr = Integer.toString(size);
        Log.fine("    Found ", sizeStr, " MathPlan responses");

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
        Log.fine("    Found ", size1Str, " 'WLCM5' question 1 responses");

        final Collection<String> stuIds = new HashSet<>(25000);
        final Set<String> keys1 = latest1.keySet();
        stuIds.addAll(keys1);

        final int sizeAll = stuIds.size();
        final String sizeAllStr = Integer.toString(sizeAll);
        Log.fine("    Found ", sizeAllStr, " distinct students with responses");

        // Compare results with SORTEST table
        Log.fine(CoreConstants.EMPTY);
        Log.fine("Scanning SORTEST table...");

        final DbConnection liveConn = liveCtx.checkOutConnection();
        final LocalDateTime now = LocalDateTime.now();
        try {
            int count1 = 0;
            int count2 = 0;
            int already1 = 0;
            int already2 = 0;
            for (final String stuId : stuIds) {

                RawStudent student = RawStudentLogic.query(cache, stuId, false);
                if (student == null) {
                    Log.fine("   WARNING: Student ", stuId, " needed to be retrieved");
                    student = RawStudentLogic.query(cache, stuId, true);
                }

                if (student == null) {
                    Log.fine("   ERROR: Student ", stuId, " not found!");
                } else if (student.pidm == null) {
                    Log.fine("   ERROR: Student ", stuId, " has no PIDM!");
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
                            Log.warning("Student ", stuId, " who has not completed MathPlan has a MPL score of ",
                                    mostRecent.testScore);
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
                            Log.fine("   Need to insert MPL=", wantValue, " test score for ", stuId);
                        } else {
                            Log.fine("   Inserting MPL=", wantValue, " test score for ", stuId);
                            final RawMpscorequeue toInsert = new RawMpscorequeue(student.pidm, TEST_CODE, now,
                                    wantValue);
                            if (!RawMpscorequeueLogic.insertSORTEST(liveConn, toInsert)) {
                                Log.fine("   ERROR: Failed to insert MPL=", wantValue, " test score for ", stuId);
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
            Log.fine("    Found ", count1Str, " to update to score 1");

            final String count2Str = Integer.toString(count2);
            Log.fine("    Found ", count2Str, " to update to score 2");

            final String already1Str = Integer.toString(already1);
            Log.fine("    Found ", already1Str, " already with score 1");

            final String already2Str = Integer.toString(already2);
            Log.fine("    Found ", already2Str, " already with score 2");
        } finally {
            liveCtx.checkInConnection(liveConn);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        final DbProfile dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        final DbContext liveCtx = dbProfile.getDbContext(ESchemaUse.LIVE);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                execute(cache, liveCtx);
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

    }
}
