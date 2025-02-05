package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * A scheduled (cron) job that runs at 11pm each day to return the testing center to its "default" starting
 * configuration for the next day. It does this by "closing" blocks of PCs, but does not "open" any PCs that are not
 * already open (so a PC that is malfunctioning can be closed and will not be reopened by this batch).
 */
public final class PcCleanup {

    /** The database profile through which to access the database. */
    private final Profile profile;

    /** The station numbers to close. */
    private static final List<String> toReset = Arrays.asList("7", "8", "9", "10", "11", "12", "13", "14",
            "15", "16", "17", "18", "85", "86", "87", "88", "90", "91", "94", "95", "97", "98", "99",
            "100", "3", "4", "33", "34", "35", "36", "83", "84", "89", "92", "93", "96");

    /**
     * Constructs a new {@code PcCleanup}.
     */
    public PcCleanup() {

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
            Log.info("Running PC_CLEANUP job");

            final Cache cache = new Cache(this.profile);

            try {
                exec(cache);
            } catch (final SQLException ex) {
                Log.warning("EXCEPTION: " + ex.getMessage());
            }
        }
    }

    /**
     * Executes batch operations.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void exec(final Cache cache) throws SQLException {

        final List<RawClientPc> mainCenter = //
                RawClientPcLogic.queryByTestingCenter(cache, "1");

        final List<RawClientPc> quietTesting = //
                RawClientPcLogic.queryByTestingCenter(cache, "4");

        for (final RawClientPc row : mainCenter) {
            final String computerId = row.computerId;
            final String station = row.stationNbr;

            if (toReset.contains(station) && "O".equals(row.pcUsage)) {
                Log.info("    PC_CLEANUP resetting station " + station);
                RawClientPcLogic.updatePcUsage(cache, row.computerId, "P");
            }

            RawClientPcLogic.updatePowerOnDue(cache, computerId, null);
            RawClientPcLogic.updateLastPing(cache, computerId, null);
            RawClientPcLogic.updatePowerStatus(cache, computerId, RawClientPc.POWER_OFF);
        }

        for (final RawClientPc row : quietTesting) {
            final String computerId = row.computerId;
            RawClientPcLogic.updatePowerOnDue(cache, computerId, null);
            RawClientPcLogic.updateLastPing(cache, computerId, null);
            RawClientPcLogic.updatePowerStatus(cache, computerId, RawClientPc.POWER_OFF);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final PcCleanup job = new PcCleanup();

        job.execute();
    }
}
