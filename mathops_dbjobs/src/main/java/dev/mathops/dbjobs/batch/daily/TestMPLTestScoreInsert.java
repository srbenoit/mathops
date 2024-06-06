package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tests the insertion of an MPL test score in BANTEST, and the ability to then query for that score.
 */
public final class TestMPLTestScoreInsert {

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The primary database context. */
    private final DbContext primaryCtx;

    /** The live database context. */
    private final DbContext liveCtx;

    /**
     * Constructs a new {@code TestMPLTestScoreInsert}.
     */
    private TestMPLTestScoreInsert() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile("ifxtest");
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

        if (this.primaryCtx == null || this.liveCtx == null) {
            report.add("Unable to create database contexts.");
        } else {
            try {
                final DbConnection liveConn = this.liveCtx.checkOutConnection();
                final LocalDateTime now = LocalDateTime.now();
                final Integer pidm = Integer.valueOf(12366515);
                final String score = "2";

//                    837311273   12421243   1
//                    836927778   12366515   2

                try {
//                    final RawMpscorequeue toInsert = new RawMpscorequeue(pidm, "MPL", now, score);
//                    if (RawMpscorequeueLogic.insertSORTEST(liveConn, toInsert)) {
//                        report.add("Insert of test score succeeded.");

                        final List<RawMpscorequeue> allScores = RawMpscorequeueLogic.querySORTESTByStudent(liveConn,
                                pidm);
                        boolean success = true;

                        int count = 0;
                        for (final RawMpscorequeue row : allScores) {
                            Log.info(row);

                            if ("MPL".equals(row.testCode)) {
                                ++count;

                                if (!score.equals(row.testScore)) {
                                    report.add("ERROR: Found a 'MPL' test score row but status was '" + row.testScore
                                            + "' rather than '" + score + "'.");
                                    success = false;
                                }
                            }
                        }
                        if (count == 0) {
                            report.add("ERROR: No 'MPL' test score row found on query.");
                            success = false;
                        } else if (count > 1) {
                            report.add("ERROR: Multiple 'MPL' test score rows found on query.");
                            success = false;
                        }

                        if (success) {
                            report.add("Insert of test score succeeded.");
                        }

//                    } else {
//                        report.add("Insert of test score FAILED.");
//                    }
                } finally {
                    this.liveCtx.checkInConnection(liveConn);
                }
            } catch (final SQLException ex) {
                report.add("*** Exception while exercising test score creation in Banner: " + ex.getMessage());
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        for (final String rep : report) {
            htm.addln(rep);
        }

        return htm.toString();
    }



    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final TestMPLTestScoreInsert job = new TestMPLTestScoreInsert();

        Log.fine(job.execute());
    }
}
