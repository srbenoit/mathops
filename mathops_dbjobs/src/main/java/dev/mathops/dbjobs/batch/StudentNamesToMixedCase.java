package dev.mathops.dbjobs.batch;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.ifaces.ILiveStudent;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.LiveStudent;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * A utility class that scans the student table, and for each student found, queries BANNER for the student's name. If
 * found, the name is updated to the mixed-case name from BANNER rather than the all-caps name we have had historically.
 * This is to provide more natural displays in web pages and for automatic email/message generation.
 *
 * <p>
 * This also checks the "preferred first name" from Banner and updates in local data if different.
 */
final class StudentNamesToMixedCase {

    /** When true, does not update database - just logs what would be updated. */
    private static final boolean DEBUG = false;

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The primary database context. */
    private final DbContext primaryCtx;

    /** The live data database context. */
    private final DbContext liveCtx;

    /**
     * Constructs a new {@code StudentNamesToMixedCase}.
     */
    private StudentNamesToMixedCase() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        this.liveCtx = this.dbProfile.getDbContext(ESchemaUse.LIVE);
    }

    /**
     * Executes the job.
     */
    private void execute() {

        if (this.dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else if (this.primaryCtx == null) {
            Log.warning("Unable to create PRIMARY database context.");
        } else if (this.liveCtx == null) {
            Log.warning("Unable to create LIVE database context.");
        } else {
            try {
                final DbConnection conn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);

                try {
                    final DbConnection bannerConn = this.liveCtx.checkOutConnection();
                    try {
                        exec(cache, bannerConn);
                    } finally {
                        this.liveCtx.checkInConnection(bannerConn);
                    }

                } catch (final SQLException ex) {
                    Log.warning("Failed to connect to LIVE database.", ex);
                } finally {
                    this.primaryCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to connect to PRIMARY database.", ex);
            }
        }
    }

    /**
     * Executes logic once database connections have been established.
     *
     * @param cache      the data cache
     * @param bannerConn the connection to the BANNER database
     * @throws SQLException if there is an error accessing either database
     */
    private static void exec(final Cache cache, final DbConnection bannerConn) throws SQLException {

        final List<RawStudent> allStudents = RawStudentLogic.INSTANCE.queryAll(cache);
        final int numStudents = allStudents.size();

        if (numStudents > 0) {
            Log.info("Loaded " + numStudents + " students");

            final ILiveStudent impl1 = bannerConn.getImplementation(ILiveStudent.class);

            int onStudent = 0;
            for (final RawStudent student : allStudents) {
                ++onStudent;
                if ((onStudent % 100) == 0) {
                    Log.fine("-> Processing student " + onStudent + " out of " + numStudents);
                }
                final String stuId = student.stuId;

                if ("888888888".equals(stuId)) {
                    continue;
                }

                try {
                    final List<LiveStudent> liveList = impl1.query(bannerConn, stuId);

                    if (liveList.isEmpty()) {
                        Log.warning("No live record for student ", student.stuId);
                    } else {
                        final LiveStudent live = liveList.get(0);

                        if (!Objects.equals(student.firstName, live.firstName) ||
                                !Objects.equals(student.lastName, live.lastName) ||
                                !Objects.equals(student.prefName, live.prefFirstName)) {

                            Log.fine("    ", student.stuId, " - Existing: ", student.firstName, " (", student.prefName,
                                    ") ", student.lastName, "  New: ", live.firstName, " (", live.prefFirstName, ") "
                                    , live.lastName);

                            if (!DEBUG) {
                                RawStudentLogic.updateName(cache, student.stuId, live.lastName, live.firstName,
                                        live.prefFirstName, student.middleInitial);
                            }
                        }

                        if (!(Objects.equals(live.collegeCode, student.college)
                                && Objects.equals(live.departmentCode, student.dept)
                                && Objects.equals(live.programCode, student.programCode)
                                && Objects.equals(live.minorCode, student.minor))) {

                            Log.fine("    Update student ", student.stuId, " College [", student.college,
                                    "->", live.collegeCode, "] Dept [", student.dept, "->", live.departmentCode,
                                    "] Program [", student.programCode, "->", live.programCode, "] Minor [",
                                    student.minor, "->", live.minorCode, "]");

                            if (!DEBUG) {
                                RawStudentLogic.updateProgram(cache, student.stuId, live.collegeCode,
                                        live.departmentCode, live.programCode, live.minorCode);
                            }
                        }
                    }
                } catch (final SQLException ex) {
                    Log.warning("Failed to query student from LIVE database.", ex);
                }
            }
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final StudentNamesToMixedCase job = new StudentNamesToMixedCase();

        job.execute();
    }
}
