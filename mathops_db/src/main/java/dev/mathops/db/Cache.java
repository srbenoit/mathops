package dev.mathops.db;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.util.HashMap;
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
    public final Profile profile;

    /** The single system data instance shared by all student data instances. */
    private final SystemData systemData;

    /** The single main data instance shared by all student data instances. */
    private final MainData mainData;

    /** The single term data instance shared by all student data instances. */
    private final TermData termData;

    /** Student data for the logged-in user; null when there is no logged-in user */
    private StudentData loggedInUser = null;

    /** Student data for the user as whom the logged-in user is acting; null if they are not acting. */
    private StudentData actAsUser = null;

    /** A map from student ID to student data container for "students of interest" in context. */
    private final Map<String, StudentData> studentData;

    /**
     * Constructs a new {@code Cache}.
     *
     * @param theProfile the database profile that was used to create the cache (this can provide access to other schema
     *                   contexts than the PRIMARY context used here)
     */
    public Cache(final Profile theProfile) {

        this.profile = theProfile;
        this.systemData = new SystemData(this);
        this.mainData = new MainData(this);
        this.termData = new TermData(this);
        this.studentData = new HashMap<>(4);
    }

    /**
     * Checks out a connection for this cache that will access a particular schema.
     *
     * @param whichSchema the schema
     * @return the connection for the specified schema
     */
    public DbConnection checkOutConnection(final ESchema whichSchema) {

        DbConnection conn = null;

        final Login login = this.profile.getLogin(whichSchema);
        if (login != null) {
            conn = login.checkOutConnection();
        }

        return conn;
    }

    /**
     * Checks in a connection that was previously checked out with {@code checkOutConnection}.
     *
     * @param connection the connection
     */
    public static void checkInConnection(final DbConnection connection) {

        connection.login.checkInConnection(connection);
    }

    /**
     * Gets the database profile that was used to create this cache.
     *
     * @return the database profile
     */
    public Profile getProfile() {

        return this.profile;
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
     * Gets the main data object.
     *
     * @return the main data object
     */
    public MainData getMainData() {

        return this.mainData;
    }

    /**
     * Gets the term data object.
     *
     * @return the term data object
     */
    public TermData getTermData() {

        return this.termData;
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
     * Gets the student data object for the "effective" user, which is either the logged-in user, or the user as whom
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

        return this.studentData.computeIfAbsent(studentId, key -> new StudentData(this, studentRecord));
    }

    /**
     * Gets the prefix for a specified schema.
     *
     * @param which the schema
     * @return the prefix (null if none)
     */
    public String getSchemaPrefix(final ESchema which) {

        final Facet facet = this.profile.getFacet(which);

        return facet == null ? null : facet.data.prefix;
    }
}
