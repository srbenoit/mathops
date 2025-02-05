package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Sets open status to 'N' for any in-progress Incompletes where i_deadline_dt < TODAY (and open status is not already
 * 'N').
 */
public enum CloseIncompletes {
    ;

    /** Flag to set batch into debug mode. */
    private static final boolean DEBUG = false;

    /**
     * Executes the job.
     */
    public static void execute() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        Log.info("Running CLOSE_INCOMPLETES job");

        try {
            final LocalDate tomorrow = LocalDate.now().plusDays(1L);

            final List<RawStcourse> inc = RawStcourseLogic.queryOpenIncompletes(cache);
            for (final RawStcourse row : inc) {
                if ("Y".equals(row.openStatus) && row.iDeadlineDt != null && row.iDeadlineDt.isBefore(tomorrow)) {

                    Log.info("    CLOSE_INCOMPLETES setting open_status to 'N'");

                    if (!DEBUG) {
                        RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, row.stuId, row.course,
                                row.sect, row.termKey, "N", row.finalClassRoll, row.lastClassRollDt);
                    }

                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        execute();
    }
}
