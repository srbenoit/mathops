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
 * The logic for an individual student (based on the most recent completion of the Math Plan) is as follows:
 * <pre>
 * IF
 *     the student has a response with version='WLCM5' and survey_nbr=2 and stu_answer='Y'
 * THEN
 *     the student should have a "MPL" test score of "2" to indicate Math Placement is indicated by the Math Plan
 * ELSE IF
 *     the student has a response with version='WLCM5' and survey_nbr=1 and stu_answer='Y'
 * THEN
 *     the student should have a "MPL" test score of "1" to indicate Math Placement is not indicated by the Math Plan
 * ELSE
 *     the student should not have a "MPL" test score at all
 * END IF
 * </pre>
 */
public enum BulkUpdateMPLTestScores {
    ;

    /** Debug flag - true to skip (but print) updates; false to actually perform updates. */
    private static final boolean DEBUG = true;

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
        final Map<String, RawStmathplan> latest2 = new HashMap<>(25000);

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
                } else if (TWO.equals(row.surveyNbr)) {
                    final RawStmathplan existing2 = latest2.get(row.stuId);
                    if (existing2 == null) {
                        latest2.put(row.stuId, row);
                    } else {
                        final LocalDateTime existingWhen = existing2.getWhen();
                        if (existingWhen == null || existingWhen.isBefore(when)) {
                            latest2.put(row.stuId, row);
                        }
                    }
                }
            }
        }

        final int size1 = latest1.size();
        final String size1Str = Integer.toString(size1);
        Log.fine("    Found ", size1Str, " 'WLCM5' question 1 responses");

        final int size2 = latest2.size();
        final String size2Str = Integer.toString(size2);
        Log.fine("    Found ", size2Str, " 'WLCM5' question 2 responses");

        final Collection<String> stuIds = new HashSet<>(25000);
        final Set<String> keys1 = latest1.keySet();
        final Set<String> keys2 = latest2.keySet();
        stuIds.addAll(keys1);
        stuIds.addAll(keys2);

        final int sizeAll = stuIds.size();
        final String sizeAllStr = Integer.toString(sizeAll);
        Log.fine("    Found ", sizeAllStr, " distinct students with responses");

        // Compare results with SORTEST table
        Log.fine(CoreConstants.EMPTY);
        Log.fine("Scanning SORTEST table...");

        final DbConnection liveConn = liveCtx.checkOutConnection();
        try {
            int count1 = 0;
            int count2 = 0;
            for (final String stuId : stuIds) {
                final RawStudent student = RawStudentLogic.query(cache, stuId, false);

                if (student == null) {
                    Log.fine("   ERROR: Student ", stuId, " not found!");
                } else {
                    final List<RawMpscorequeue> existing = RawMpscorequeueLogic.querySORTESTByStudent(liveConn,
                            student.pidm);

                    RawMpscorequeue mostRecent = null;
                    for (final RawMpscorequeue test : existing) {
                        if ("MPL".equals(test.testCode)) {
                            if (mostRecent == null || mostRecent.testDate.isBefore(test.testDate)) {
                                mostRecent = test;
                            }
                        }
                    }

                    if (latest2.containsKey(stuId)) {
                        // Student should have a "2" MPL score
                        if (mostRecent == null || !"2".equals(mostRecent.testScore)) {
                            Log.fine("   Need to insert MPL=2 test score for ", stuId);
                            ++count2;
                        }
                    } else if (latest1.containsKey(stuId)) {
                        // Student should have a "1" MPL score
                        if (mostRecent == null || !"1".equals(mostRecent.testScore)) {
                            Log.fine("   Need to insert MPL=1 test score for ", stuId);
                            ++count1;
                        }
                    }
                }
            }

            Log.fine("    Found", Integer.toString(count1), " to update to score 1");
            Log.fine("    Found", Integer.toString(count2), " to update to score 2");
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
