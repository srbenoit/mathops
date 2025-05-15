package dev.mathops.dbjobs.eos.rollover;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.dbjobs.EDebugMode;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Performs the roll-over process.
 */
public class PerformRollover implements Runnable {

    /** Flag to run in "debug" mode which prints changes that would be performed rather than performing any changes. */
    private static final EDebugMode DEBUG_MODE = EDebugMode.DEBUG;

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code PerformRollover}.
     *
     * @param theCache the data cache
     */
    private PerformRollover(final Cache theCache) {

        this.cache = theCache;
    }

    /**
     * Runs the process.
     */
    public void run() {

        final TermLogic termLogic = TermLogic.get(this.cache);

        try {
            final TermRec activeTerm = termLogic.queryActive(this.cache);
            final TermRec nextTerm = termLogic.queryNext(this.cache);
            final TermRec priorTerm = termLogic.queryPrior(this.cache);

            if (activeTerm == null) {
                Log.warning("Unable to query the active term");
            } else if (nextTerm == null) {
                Log.warning("Unable to query the next term");
            } else if (priorTerm == null) {
                Log.warning("Unable to query the prior term");
            } else {
                Log.info("Active term: ", activeTerm.term.longString);
                Log.info("Next term: ", nextTerm.term.longString);
                Log.info("Prior term: ", priorTerm.term.longString);

                doRollover(activeTerm, nextTerm, priorTerm);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query the active, next, or prior term.", ex);
        }
    }

    /**
     * Performs rollover tasks.
     *
     * @param activeTerm the active term
     * @param nextTerm   the next term
     * @param priorTerm  the prior term
     */
    private void doRollover(final TermRec activeTerm, final TermRec nextTerm, final TermRec priorTerm) {

        if (rolloverHolds() == ESuccessFailure.SUCCESS
            && rolloverBogusMapping(activeTerm) == ESuccessFailure.SUCCESS
            && rolloverCalcs() == ESuccessFailure.SUCCESS
            && rolloverChallengeFee() == ESuccessFailure.SUCCESS
        ) {
            Log.warning("Rollover process completed successfully");
        } else {
            Log.warning("Rollover process terminated with an error.");

        }
    }

    /**
     * Deletes administrative holds except hold 05 (Deferred F) and hold 06 (Discretionary hold) or for any hold whose
     * hold number starts with "4" (overdue resources lends).
     *
     * @return success or failure
     */
    private ESuccessFailure rolloverHolds() {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        try {
            final List<RawAdminHold> holds = RawAdminHoldLogic.queryAll(this.cache);

            int count = 0;
            for (final RawAdminHold hold : holds) {
                final String holdId = hold.holdId;
                // FIXME: This should be a field on the "admin_hold" record - retain across terms?
                if ("06".equals(holdId) || holdId.startsWith("4")) {
                    continue;
                }

                if (DEBUG_MODE == EDebugMode.DEBUG) {
                    Log.info("> Deleting hold ", holdId, " for ", hold.stuId);
                } else {
                    RawAdminHoldLogic.delete(this.cache, hold);
                }
                ++count;
            }
            if (count == 0) {
                Log.info("> There were no administrative holds to delete.");
            }
        } catch (final SQLException ex) {
            Log.warning("> Failed to clean administrative holds.", ex);
            result = ESuccessFailure.FAILURE;
        }

        return result;
    }

    /**
     * Deletes bogus_mapping rows that are more than 6 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure rolloverBogusMapping(final TermRec activeTerm) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        final int deleteYear = activeTerm.term.shortYear.intValue() - 6;
        final String deleteYearStr = Integer.toString(deleteYear);

        final String sql = SimpleBuilder.concat("DELETE FROM bogus_mapping WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        if (DEBUG_MODE == EDebugMode.DEBUG) {
            Log.info("> ", sql);
        } else {
            try {
                final DbConnection conn = this.cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    conn.commit();
                } finally {
                    Cache.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning("> Failed to clean bogus_mapping holds.", ex);
                result = ESuccessFailure.FAILURE;
            }
        }

        return result;
    }

    /**
     * Deletes all calcs rows.
     *
     * @return success or failure
     */
    private ESuccessFailure rolloverCalcs() {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        if (DEBUG_MODE == EDebugMode.DEBUG) {
            Log.info("> DELETE FROM calcs");
        } else {
            try {
                final DbConnection conn = this.cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM calcs");
                    conn.commit();
                } finally {
                    Cache.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning("> Failed to clean calcs holds.", ex);
                result = ESuccessFailure.FAILURE;
            }
        }

        return result;
    }

    /**
     * Deletes all challenge_fee rows.  This deletes rows thar are 15 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure rolloverChallengeFee(final TermRec activeTerm) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        if (DEBUG_MODE == EDebugMode.DEBUG) {
            Log.info("> DELETE FROM challenge_fee WHERE bill_dt < start date from term 15 years ago");
        } else {
            try {



//                DELETE FROM challenge_fee
//                WHERE bill_dt <(SELECT start_dt FROM term
//                WHERE term_yr = (cterm.term_yr - 15)
//                AND term = cterm.term)



                final DbConnection conn = this.cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM calcs");
                    conn.commit();
                } finally {
                    Cache.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning("> Failed to clean calcs holds.", ex);
                result = ESuccessFailure.FAILURE;
            }
        }

        return result;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        final Runnable obj = new PerformRollover(cache);
        obj.run();
    }
}