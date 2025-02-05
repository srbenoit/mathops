package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawrecord.RawPendingExam;

import java.sql.SQLException;
import java.util.List;

/**
 * A scheduled (cron) job that runs at 11pm each day to delete any "pending_exam" records that were left.
 */
public final class CleanPending {

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code CleanPending}.
     */
    public CleanPending() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     */
    public void execute() {

        if (this.profile == null) {
            Log.warning("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(this.profile);

            Log.info("Running CLEAN_PENDING job");
            try {
                final List<RawPendingExam> all = RawPendingExamLogic.queryAll(cache);
                for (final RawPendingExam row : all) {
                    Log.info("    CLEAN_PENDING deleting row for student ", row.stuId);
                    RawPendingExamLogic.delete(cache, row);
                }
            } catch (final SQLException ex) {
                Log.warning("EXCEPTION: " + ex.getMessage());
            }
        }
    }
}
