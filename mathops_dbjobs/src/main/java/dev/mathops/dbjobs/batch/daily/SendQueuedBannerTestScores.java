package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.LogicUtils;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Scans all active registrations in the database, and all existing STTERM records, and scans for any STTERM records
 * that are inconsistent with the registration data.
 */
public final class SendQueuedBannerTestScores {

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The primary database context. */
    private final DbContext primaryCtx;

    /** The live database context. */
    private final DbContext liveCtx;

    /**
     * Constructs a new {@code SendQueuedBannerTestScores}.
     */
    public SendQueuedBannerTestScores() {

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

        if (this.primaryCtx == null || this.liveCtx == null) {
            report.add("Unable to create database contexts.");
        } else if (LogicUtils.isBannerDown()) {
            report.add("BANNER currently down; skipping queued test score scan");
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);

                try {
                    final DbConnection liveConn = this.liveCtx.checkOutConnection();

                    try {
                        final List<RawMpscorequeue> queued = RawMpscorequeueLogic.queryAll(cache);
                        report.add("Number of queued test scores: " + queued.size());

                        for (final RawMpscorequeue toProcess : queued) {
                            report.add("Posting queued test score to BANNER: [" + toProcess + "]");

                            if (RawMpscorequeueLogic.insertSORTEST(liveConn, toProcess)) {
                                RawMpscorequeueLogic.delete(cache, toProcess);
                            } else {
                                report.add("Failed to post queued test score to BANNER");
                            }
                        }
                    } finally {
                        this.liveCtx.checkInConnection(liveConn);
                    }
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                report.add("*** Exception while sending queued scores to Banner: " + ex.getMessage());
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
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final SendQueuedBannerTestScores job = new SendQueuedBannerTestScores();

        Log.fine(job.execute());
    }
}
