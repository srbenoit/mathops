package dev.mathops.dbjobs.batch.daily;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.rawlogic.RawHoldTypeLogic;
import dev.mathops.db.rawlogic.RawResourceLogic;
import dev.mathops.db.rawlogic.RawStresourceLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawAdminHold;
import dev.mathops.db.rawrecord.RawHoldType;
import dev.mathops.db.rawrecord.RawResource;
import dev.mathops.db.rawrecord.RawStresource;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.db.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Scans registration data and applies relevant holds.
 */
public enum SetHolds {
    ;

    /** Hold ID indicating a rental calculator is overdue. */
    private static final String LATE_RENTAL_CALC_HOLD = "36";

    /** Flag to set batch into debug mode. */
    private static final boolean DEBUG = false;

    /** A commonly used string. */
    private static final String FOR_STUDENT = " for student ";

    /**
     * Runs the report, writing the result to a file.
     */
    public static void execute() {

        final ContextMap map = ContextMap.getDefaultInstance();
        final DbProfile profile = map.getCodeProfile(Contexts.BATCH_PATH);
        final DbContext ctx = profile.getDbContext(ESchemaUse.PRIMARY);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            try {
                final Cache cache = new Cache(profile, conn);
                setHolds(cache);
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Sets holds.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    static void setHolds(final Cache cache) throws SQLException {

        final LocalDate sysDate = LocalDate.now();
        final LocalDate endDate;
        String sev;
        int count = 0;

        final TermRec active = TermLogic.get(cache).queryActive(cache);

        endDate = active.endDate.minusDays(8L);

        final List<RawStresource> resources = RawStresourceLogic.INSTANCE.queryAll(cache);
        final Collection<String> students = new HashSet<>(10);

        // Add warning hold for students who have an unreturned rental calculator
        if (sysDate.equals(endDate)) {

            // Look up the severity of hold
            final RawHoldType type = RawHoldTypeLogic.query(cache, LATE_RENTAL_CALC_HOLD);
            sev = type == null ? "N" : type.sevAdminHold;

            if (!resources.isEmpty()) {

                for (final RawStresource test : resources) {
                    // Rental calculators all have resource IDs starting with "77"
                    if (test.resourceId.startsWith("77") && test.returnDt == null) {
                        students.add(test.stuId);
                    }
                }

                // TODO: Set the "times" field in the new hold from the stresource record

                for (final String student : students) {
                    if (RawAdminHoldLogic.query(cache, student, LATE_RENTAL_CALC_HOLD) == null) {
                        final RawAdminHold hold = new RawAdminHold(student, LATE_RENTAL_CALC_HOLD, sev,
                                Integer.valueOf(0), sysDate);

                        Log.info("  Adding hold " + LATE_RENTAL_CALC_HOLD + FOR_STUDENT + student);

                        if (!DEBUG) {
                            RawAdminHoldLogic.INSTANCE.insert(cache, hold);
                        }
                    } else {
                        Log.info("  Retaining existing hold " + LATE_RENTAL_CALC_HOLD + FOR_STUDENT + student);
                    }

                    ++count;
                }
            }
        }

        Log.info("  Number of rental calc holds that have been applied/retained: " + count);

        // Add hold for students who have an unreturned/overdue item

        count = 0;
        final Map<String, List<RawStresource>> lent = new HashMap<>(10);

        for (final RawStresource test : resources) {
            if (test.dueDt != null && test.dueDt.isBefore(sysDate) && test.returnDt == null) {
                final List<RawStresource> list = lent.computeIfAbsent(test.stuId, s -> new ArrayList<>(2));
                list.add(test);
            }
        }

        for (final Map.Entry<String, List<RawStresource>> entry : lent.entrySet()) {

            for (final RawStresource stres : entry.getValue()) {

                // Look up the hold associated with the resource
                final RawResource res = RawResourceLogic.query(cache, stres.resourceId);
                if (res == null) {
                    Log.info("  ** WARNING: Unrecognized resource ID '" + stres.resourceId + "' in STRESOURCE for "
                            + stres.stuId);
                    continue;
                }

                if (res.holdId != null) {
                    // TODO: Increment "times" field in stres and replace the "stres"
                    // object.

                    final RawHoldType type = RawHoldTypeLogic.query(cache, res.holdId);
                    sev = type == null ? "N" : type.sevAdminHold;

                    if (RawAdminHoldLogic.query(cache, stres.stuId, res.holdId) == null) {
                        final RawAdminHold hold = new RawAdminHold(stres.stuId,
                                res.holdId, sev, stres.timesDisplay, sysDate);

                        Log.info("  Adding hold " + res.holdId + FOR_STUDENT + stres.stuId);

                        if (!DEBUG) {
                            RawAdminHoldLogic.INSTANCE.insert(cache, hold);
                        }
                        ++count;
                    } else {
                        Log.info("  Retaining existing hold " + res.holdId + FOR_STUDENT + stres.stuId);
                    }
                }
            }
        }

        Log.info("  Number of holds for overdue items that have been applied: " + count);

        final List<RawAdminHold> allStudentHolds = RawAdminHoldLogic.INSTANCE.queryAll(cache);
        final Collection<String> withFatalHolds = new HashSet<>(allStudentHolds.size());
        final Collection<String> withNonfatalHolds = new HashSet<>(allStudentHolds.size());
        for (final RawAdminHold stuHold : allStudentHolds) {
            if ("F".equals(stuHold.sevAdminHold)) {
                withFatalHolds.add(stuHold.stuId);
            } else {
                withNonfatalHolds.add(stuHold.stuId);
            }
        }

        final List<RawStudent> allStudents = RawStudentLogic.INSTANCE.queryAll(cache);

        int numCleared = 0;
        int numFatalSet = 0;
        int numNonfatalSet = 0;
        int numFatalRetained = 0;
        int numNonfatalRetained = 0;

        for (final RawStudent test : allStudents) {
            if (test.sevAdminHold == null) {
                // Currently clear - see if there should be a hold
                if (withFatalHolds.contains(test.stuId)) {
                    // Need to add a "F" severity to STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, "F");
                    }
                    ++numFatalSet;
                } else if (withNonfatalHolds.contains(test.stuId)) {
                    // Need to add a "N" severity to STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, "N");
                    }
                    ++numNonfatalSet;
                }
            } else if ("F".equals(test.sevAdminHold)) {
                if (withFatalHolds.contains(test.stuId)) {
                    ++numFatalRetained;
                } else if (withNonfatalHolds.contains(test.stuId)) {
                    // Need to change severity from "F" to "N" in STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, "N");
                    }
                    ++numNonfatalSet;
                } else {
                    // Need to clear severity on STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, null);
                    }
                    ++numCleared;
                }
            } else // STUDENT record has "N"
                if (withFatalHolds.contains(test.stuId)) {
                    // Need to change severity from "N" to "F" in STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, "F");
                    }
                    ++numFatalSet;
                } else if (withNonfatalHolds.contains(test.stuId)) {
                    ++numNonfatalRetained;
                } else {
                    // Need to clear severity on STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, null);
                    }
                    ++numCleared;
                }
        }

        Log.info("  Students whose hold severity was cleared: " + numCleared);
        Log.info("  Students whose hold severity was set to 'F': " + numFatalSet);
        Log.info("  Students whose hold severity was set to 'N': " + numNonfatalSet);
        Log.info("  Students whose hold severity 'F' was retained: " + numFatalRetained);
        Log.info("  Students whose hold severity 'N' was retained: " + numNonfatalRetained);
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        execute();
    }
}
