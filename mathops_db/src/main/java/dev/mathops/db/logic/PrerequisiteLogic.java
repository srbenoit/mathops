package dev.mathops.db.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.rawlogic.RawPrereqLogic;
import dev.mathops.db.rawlogic.RawStcourseLogic;
import dev.mathops.db.rawrecord.RawFfrTrns;
import dev.mathops.db.rawrecord.RawMpeCredit;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.db.rawrecord.RawStcourse;
import dev.mathops.db.svc.term.TermLogic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that tests whether a student has met prerequisites for a course.
 */
public final class PrerequisiteLogic {

    /** The student ID. */
    private final String studentId;

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
     * @param cache        the data cache
     * @param theStudentId the student ID
     * @throws SQLException if there is an error accessing the database
     */
    public PrerequisiteLogic(final Cache cache, final String theStudentId) throws SQLException {

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }

        this.studentId = theStudentId;

        this.allPlacementCredit = RawMpeCreditLogic.queryByStudent(cache, theStudentId);

        this.allHistory = RawStcourseLogic.getHistory(cache, theStudentId);
        this.allCompletions = RawStcourseLogic.getAllPriorCompleted(cache, theStudentId);
        this.allTransfer = RawFfrTrnsLogic.queryByStudent(cache, theStudentId);

        this.satisfied = new ArrayList<>(5);

        if (checkPrerequisites(cache, RawRecordConstants.M117,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M117))) {
            this.satisfied.add(RawRecordConstants.M117);
        }
        if (checkPrerequisites(cache, RawRecordConstants.M118,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M118))) {
            this.satisfied.add(RawRecordConstants.M118);
        }
        if (checkPrerequisites(cache, RawRecordConstants.M124,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M124))) {
            this.satisfied.add(RawRecordConstants.M124);
        }
        if (checkPrerequisites(cache, RawRecordConstants.M125,
                RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M125))) {
            this.satisfied.add(RawRecordConstants.M125);
        }
        if (checkPrerequisites(cache, RawRecordConstants.M126,
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
     * @param cache           the data cache
     * @param courseId        the course ID
     * @param prereqCourseIds the list of courses which can satisfy the prerequisites for the course
     * @return True if the prerequisite is satisfied; False if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean checkPrerequisites(final Cache cache, final String courseId,
                                       final Iterable<String> prereqCourseIds) throws SQLException {

        boolean prereqSatisfied = false;

        // Scan for STCOURSE records marked as "prereq_satisfied = 'Y'", even records that have
        // been dropped.

        for (final RawStcourse test : this.allHistory) {
            if (test.course.equals(courseId) && ("Y".equals(test.prereqSatis) || "P".equals(test.prereqSatis))) {
                prereqSatisfied = true;
                break;
            }
        }

        // If not previously satisfied, test for data that indicates satisfied.
        if (!prereqSatisfied) {

            outer:
            for (final String preq : prereqCourseIds) {

                // Test for a previously completed course or prerequisite course
                for (final RawStcourse complete : this.allCompletions) {
                    if (courseId.equals(complete.course) || preq.equals(complete.course)) {
                        prereqSatisfied = true;
                        break outer;
                    }
                }

                // Test for placement credit in the course or a prerequisite course
                for (final RawMpeCredit cred : this.allPlacementCredit) {
                    if (courseId.equals(cred.course) || preq.equals(cred.course)) {
                        prereqSatisfied = true;
                        break outer;
                    }
                }

                // Search for transfer credit in course or a prerequisite course
                for (final RawFfrTrns xfer : this.allTransfer) {
                    if (courseId.equals(xfer.course) || preq.equals(xfer.course)) {

                        prereqSatisfied = true;
                        break outer;
                    }
                }
            }
        }

        // Special-case handling - section 801/401/809 students can start the course without
        // prereqs, but they get a longer Skills Review (ideally, we would only afford this to
        // non-degree-seeking DCE students, but we tend not to get that data from CSU Online

        if (!prereqSatisfied && RawRecordConstants.M117.equals(courseId)) {

            final List<RawStcourse> allCurrent = RawStcourseLogic.getActiveForStudent(cache, this.studentId,
                    TermLogic.get(cache).queryActive(cache).term);

            String sect = null;
            for (final RawStcourse test : allCurrent) {
                if (RawRecordConstants.M117.equals(test.course)) {
                    sect = test.sect;
                    break;
                }
            }

            if ("801".equals(sect) || "809".equals(sect)) {
                prereqSatisfied = true;
            }
        }

        return prereqSatisfied;
    }

    /**
     * Tests whether a student has earned credit in a course.
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
