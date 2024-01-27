package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawrecord.RawPendingExam;

import java.sql.SQLException;
import java.util.List;

/**
 * A scheduled (cron) job that runs at 11pm each day to delete any "pending_exam" records that were left.
 */
public final class CleanPending {

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The Primary database context. */
    private final DbContext primaryCtx;

    /**
     * Constructs a new {@code CleanPending}.
     */
    public CleanPending() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
    }

    /**
     * Executes the job.
     */
    public void execute() {

        if (this.dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else if (this.primaryCtx == null) {
            Log.warning("Unable to create PRIMARY database context.");
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);
                try {
                    Log.info("Running CLEAN_PENDING job");
                    final List<RawPendingExam> all = RawPendingExamLogic.INSTANCE.queryAll(cache);
                    for (final RawPendingExam row : all) {
                        Log.info("    CLEAN_PENDING deleting row for student ", row.stuId);
                        RawPendingExamLogic.INSTANCE.delete(cache, row);
                    }
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning("EXCEPTION: " + ex.getMessage());
            }
        }
    }
}
