package dev.mathops.db.old.logic;

import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawlogic.RawPrereqLogic;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that tests whether a student has met prerequisites for a course.
 */
public final class PrerequisiteLogic {

    /** The list of student placement credit. */
    private final List<RawMpeCredit> allPlacementCredit;

    /** The list of all transfer credits on the student's record. */
    private final List<RawFfrTrns> allTransfer;

    /** The set of courses the student has completed previously. */
    private final List<RawStcourse> allCompletions;

    /** The complete set of courses the student has ever taken. */
    private final List<RawStcourse> allHistory;

    /** List of courses for which the student has satisfied the prerequisite. */
    private final List<String> satisfied;

    /** List of courses for which the student has earned credit. */
    private final List<String> creditFor;

    /**
     * Constructs a new {@code PrerequisiteLogic}.
     *
     * @param data the student data object
     * @throws SQLException if there is an error accessing the database
     */
    public PrerequisiteLogic(final StudentData data) throws SQLException {

        if (data == null) {
            throw new IllegalArgumentException("Student data object may not be null");
        }

        this.allPlacementCredit = data.getPlacementCredit();
        this.allHistory = data.getRegistrations();
        this.allCompletions = data.getCompletedRegistrations();
        this.allTransfer = data.getTransferCredit();

        final Cache cache = data.getCache();

        this.satisfied = new ArrayList<>(5);

        if (checkPrerequisites(RawRecordConstants.M117,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M117))) {
            this.satisfied.add(RawRecordConstants.M117);
        }
        if (checkPrerequisites(RawRecordConstants.M118,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M118))) {
            this.satisfied.add(RawRecordConstants.M118);
        }
        if (checkPrerequisites(RawRecordConstants.M124,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M124))) {
            this.satisfied.add(RawRecordConstants.M124);
        }
        if (checkPrerequisites(RawRecordConstants.M125,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M125))) {
            this.satisfied.add(RawRecordConstants.M125);
        }
        if (checkPrerequisites(RawRecordConstants.M126,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M126))) {
            this.satisfied.add(RawRecordConstants.M126);
        }

        this.creditFor = new ArrayList<>(5);
        if (checkCredit(RawRecordConstants.M117)) {
            this.creditFor.add(RawRecordConstants.M117);
        }
        if (checkCredit(RawRecordConstants.M118)) {
            this.creditFor.add(RawRecordConstants.M118);
        }
        if (checkCredit(RawRecordConstants.M124)) {
            this.creditFor.add(RawRecordConstants.M124);
        }
        if (checkCredit(RawRecordConstants.M125)) {
            this.creditFor.add(RawRecordConstants.M125);
        }
        if (checkCredit(RawRecordConstants.M126)) {
            this.creditFor.add(RawRecordConstants.M126);
        }
    }

    /**
     * Tests whether the student has satisfied the prerequisites for a given course.
     *
     * @param theCourseId the course ID
     * @return true if the student has satisfied the prerequisites for the course
     */
    public boolean hasSatisfiedPrereqsFor(final String theCourseId) {

        return this.satisfied.contains(theCourseId);
    }

    /**
     * Tests whether the student has credit for a given course (by having taken the course, earned placement credit, or
     * by transfer credit).
     *
     * @param theCourseId the course ID
     * @return true if the student has credit for the course
     */
    public boolean hasCreditFor(final String theCourseId) {

        return this.creditFor.contains(theCourseId);
    }

    /**
     * Compares the existing student records to a list of new student records. For any new records not in the existing
     * collection, a new student record is created. For all new records that exist already, the existing record is
     * tested for changes and updated if needed.
     *
     * @param courseId        the course ID
     * @param prereqCourseIds the list of courses which can satisfy the prerequisites for the course
     * @return True if the prerequisite is satisfied; False if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkPrerequisites(final String courseId, final Iterable<String> prereqCourseIds) {

        boolean prereqSatisfied = false;

        // Scan for registrations that indicate prerequisite is satisfied (this allows manual settings of that flag to
        // persistently clear prerequisites in future terms)
        for (final RawStcourse test : this.allHistory) {
            if (test.course.equals(courseId) && ("Y".equals(test.prereqSatis) || "P".equals(test.prereqSatis))) {
                prereqSatisfied = true;
                break;
            }
        }

        // If not previously satisfied, test for data that indicates it has been satisfied
        if (!prereqSatisfied) {
            for (final String preq : prereqCourseIds) {
                if (checkCredit(preq)) {
                    prereqSatisfied = true;
                    break;
                }
            }
        }

        return prereqSatisfied;
    }

    /**
     * Tests whether a student has earned credit in a course, has transfer credit for the course, or has placed out of
     * the course.
     *
     * @param courseId the course ID
     * @return true if the student has earned credit
     */
    private boolean checkCredit(final String courseId) {

        boolean hasCredit = false;

        for (final RawStcourse complete : this.allCompletions) {
            if (courseId.equals(complete.course)) {
                hasCredit = true;
                break;
            }
        }

        if (!hasCredit) {
            for (final RawMpeCredit cred : this.allPlacementCredit) {
                if (courseId.equals(cred.course)) {
                    hasCredit = true;
                    break;
                }
            }

            if (!hasCredit) {
                for (final RawFfrTrns xfer : this.allTransfer) {
                    if (courseId.equals(xfer.course)) {
                        hasCredit = true;
                        break;
                    }
                }
            }
        }

        return hasCredit;
    }

    // /**
    // * Main method to exercise the logic object.
    // *
    // * @param args command-line arguments
    // */
    // public static void main(final String... args) {
    //
    // final ContextMap map = ContextMap.getDefaultInstance();
    // DbConnection.registerDrivers();
    //
    // final DbProfile dbProfile =
    // map.getWebSiteProfile(Contexts.PLACEMENT_HOST, Contexts.ROOT_PATH).dbProfile;
    // final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
    //
    // try {
    // final DbConnection conn = ctx.checkOutConnection();
    // final Cache cache = new Cache(dbProfile, conn);
    //
    // try {
    // final PrerequisiteLogic prereq = //
    // new PrerequisiteLogic(cache, "833365051");
    //
    // Log.fine("Student: ", prereq.studentId);
    //
    // Log.fine(" OK for 117: " + prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M117));
    // // $NON-NLS-1$
    // Log.fine(" OK for 118: " + prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M118));
    // // $NON-NLS-1$
    // Log.fine(" OK for 124: " + prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M124));
    // // $NON-NLS-1$
    // Log.fine(" OK for 125: " + prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M125));
    // // $NON-NLS-1$
    // Log.fine(" OK for 126: " + prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M126));
    // // $NON-NLS-1$
    // } finally {
    // ctx.checkInConnection(conn);
    // }
    // } catch (SQLException ex) {
    // Log.warning(ex);
    // }
    // }
}
