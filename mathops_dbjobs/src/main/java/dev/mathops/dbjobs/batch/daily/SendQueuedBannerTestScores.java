package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
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
    private final Profile profile;

    /**
     * Constructs a new {@code SendQueuedBannerTestScores}.
     */
    public SendQueuedBannerTestScores() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.profile == null) {
            report.add("Unable to create database profile.");
        } else if (LogicUtils.isBannerDown()) {
            report.add("BANNER currently down; skipping queued test score scan");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                final Login liveLogin = this.profile.getLogin(ESchema.LIVE);
                final DbConnection liveConn = liveLogin.checkOutConnection();

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
                    liveLogin.checkInConnection(liveConn);
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

        DbConnection.registerDrivers();
        final SendQueuedBannerTestScores job = new SendQueuedBannerTestScores();

        Log.fine(job.execute());
    }
}
