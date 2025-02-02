package dev.mathops.db;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbConfig;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.ServerConfig;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A container for cached data associated with a single database connection, which is typically a load of a web page or
 * the execution of a batch job or report.
 *
 * <p>
 * Data is often used more than once during construction of a response or execution of a job, and this class prevents
 * doing the same query multiple times (with possibly inconsistent results), while at the same time avoiding bulk
 * queries of all possible data without regard to the particular response being created.
 *
 * <p>
 * This object must be created and accessed by a single thread (typical for a servlet request for a web page) since it
 * has public data members and no synchronization.
 *
 * <p>
 * Rather than try to accommodate every possible query and its results, this class simply provides a generic map from a
 * {@code String} key to {@code RawRecordBase} to store single-record results, and a second map from {@code String} key
 * to {@code List<? extends RawRecordBase>} to store list results.
 *
 * <p>
 * It then becomes essential to uniquely name each possible query result (where the name must include the unique keys
 * used in a query).
 */
public final class Cache {

    /** A guest User ID. */
    private static final String GUEST = "GUEST";

    /** The database profile that was used to create the cache. */
    public final DbProfile dbProfile;

    /** The database connection to use for all cached data queries. */
    public final DbConnection conn;

    /** The main schema name. */
    public final String mainSchemaName;

    /** The term schema name. */
    public final String termSchemaName;

    /** The single system data instance shared by all student data instances. */
    private final SystemData systemData;

    /** Student data for the logged-in user; null when there is no logged-in user */
    private StudentData loggedInUser = null;

    /** Student data for the user as whom the logged-in user is acting; null if they are not acting. */
    private StudentData actAsUser = null;

    /** A map from student ID to student data container for "students of interest" in context. */
    private final Map<String, StudentData> studentData;

    /**
     * Constructs a new {@code Cache}.
     *
     * @param theDbProfile the database profile that was used to create the cache (this can provide access to other
     *                     schema contexts than the PRIMARY context used here)
     * @param theConn      the database connection to use for all cached data queries
     * @throws SQLException if there is an error accessing the database
     */
    public Cache(final DbProfile theDbProfile, final DbConnection theConn) throws SQLException {

        if (theConn.dbContext.schema.use != ESchemaUse.PRIMARY) {
            throw new IllegalArgumentException("Cache must be used with primary schema connection");
        }

        this.dbProfile = theDbProfile;
        this.conn = theConn;

        // Attempt to determine the schema name for term-related queries

        final DbConfig db = theDbProfile.getDbContext(ESchemaUse.PRIMARY).loginConfig.db;

        final EDbProduct type = db.server.type;
        if (type == EDbProduct.INFORMIX) {
            this.mainSchemaName = "math";
            this.termSchemaName = "math";
        } else if (type == EDbProduct.POSTGRESQL) {
            if (db.use == EDbUse.PROD) {
                this.mainSchemaName = "legacy";
                final TermRec active = TermLogic.Postgres.INSTANCE.queryActive(this);
                if (active == null) {
                    throw new IllegalArgumentException("No active TermRec found");
                }
                this.termSchemaName = active.term.shortString.toLowerCase(Locale.ROOT);
                Log.info("Using the '", this.termSchemaName, "' schema for term data");
            } else if (db.use == EDbUse.DEV) {
                this.mainSchemaName = "legacy_dev";
                this.termSchemaName = "term_d";
            } else {
                this.mainSchemaName = "legacy_test";
                this.termSchemaName = "term_t";
            }
        } else {
            throw new IllegalArgumentException("No TERM implementation for " + type + " database");
        }

        this.systemData = new SystemData(this);
        this.studentData = new HashMap<>(4);
    }

    /**
     * Gets the database profile that was used to create this cache.
     *
     * @return the database profile
     */
    public DbProfile getDbProfile() {

        return this.dbProfile;
    }

    /**
     * Gets the system data object.
     *
     * @return the system data object
     */
    public SystemData getSystemData() {

        return this.systemData;
    }

    /**
     * Sets the student data object for the logged-in user.
     *
     * @param studentId the student ID of the new logged-in user
     * @return the student data object for the logged-in user
     */
    public StudentData setLoggedInUser(final String studentId) {

        if (GUEST.equals(studentId)) {
            final RawStudent stu = RawStudentLogic.makeFakeStudent(GUEST, CoreConstants.EMPTY, GUEST);
            this.loggedInUser = new StudentData(this, stu);
        } else {
            if (this.loggedInUser == null || !this.loggedInUser.getStudentId().equals(studentId)) {
                this.loggedInUser = new StudentData(this, studentId, ELiveRefreshes.IF_MISSING);
            }
        }

        return this.loggedInUser;
    }

    /**
     * Sets the student data object for the user as whom the logged-in user is acting.  This also clears the "act as"
     * user, if present.
     *
     * @param newActAsUser the student data for the new "acting-as" user
     */
    public void setLoggedInUser(final StudentData newActAsUser) {

        this.loggedInUser = newActAsUser;
        this.actAsUser = null;
    }

    /**
     * Gets the student data object for the logged-in user.
     *
     * @return the student data object for the logged-in user
     */
    public StudentData getLoggedInUser() {

        return this.loggedInUser;
    }

    /**
     * Sets the student data object for the user as whom the logged-in user is acting.
     *
     * @param studentId the student ID of the new "acting-as" user
     * @return the student data object for the "acting-as" user
     */
    public StudentData setActAsUser(final String studentId) {

        if (this.actAsUser == null || !this.actAsUser.getStudentId().equals(studentId)) {
            this.actAsUser = new StudentData(this, studentId, ELiveRefreshes.IF_MISSING);
        }

        return this.actAsUser;
    }

    /**
     * Sets the student data object for the user as whom the logged-in user is acting.
     *
     * @param newActAsUser the student data for the new "acting-as" user
     */
    public void setActAsUser(final StudentData newActAsUser) {

        this.actAsUser = newActAsUser;
    }

    /**
     * Gets the student data object for the user as whom the logged-in user is acting.
     *
     * @return the student data object for the user as whom is being acted
     */
    public StudentData getActAsUser() {

        return this.actAsUser;
    }

    /**
     * Gets the student data object for the "effective" user, which is either the logged in user, or the user as whom
     * that logged-in user is currently acting if they are acting.
     *
     * @return the student data object for the effective user
     */
    public StudentData getEffectiveUser() {

        return this.actAsUser == null ? this.loggedInUser : this.actAsUser;
    }

    /**
     * Gets the data object for a student with a specified ID, creating a new {@code StudentData} object for that
     * student if one does not already exist.
     *
     * @param studentId the student ID
     * @return the student data object for the effective user
     */
    public StudentData getStudent(final String studentId) {

        return this.studentData.computeIfAbsent(studentId,
                key -> new StudentData(this, key, ELiveRefreshes.IF_MISSING));
    }

    /**
     * Gets the data object for a student with a specified student record, creating a new {@code StudentData} object for
     * that student if one does not already exist.
     *
     * @param studentRecord the student record
     * @return the student data object for the effective user
     */
    public StudentData getStudent(final RawStudent studentRecord) {

        final String studentId = studentRecord.stuId;

        return this.studentData.computeIfAbsent(studentId,
                key -> new StudentData(this, studentRecord));
    }

    /**
     * Tests whether this cache is connected to a PostgreSQL database.
     *
     * @return true if PostgreSQL
     */
    public boolean isPostgreSQL() {

        final DbContext context = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
        final LoginConfig login = context.getLoginConfig();
        final DbConfig db = login.db;
        final ServerConfig server = db.server;

        return server.type == EDbProduct.POSTGRESQL;
    }
}
