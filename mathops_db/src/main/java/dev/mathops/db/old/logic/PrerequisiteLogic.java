package dev.mathops.db.old.logic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that tests whether a student has met prerequisites for a course.
 */
public final class PrerequisiteLogic {

    /** Flag to turn on debugging messages. */
    private static final boolean DEBUG = false;

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

    /** List of courses for which the student has satisfied the prerequisite through transfer credit. */
    private final List<String> satisfiedByTransfer;

    /** List of courses for which the student has earned credit. */
    private final List<String> creditFor;

    /** List of courses for which the student has transfer credit. */
    private final List<String> creditByTransfer;

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
        this.satisfiedByTransfer = new ArrayList<>(5);
        this.creditByTransfer = new ArrayList<>(5);

        final SystemData systemData = cache.getSystemData();

        final List<String> m117Prerequisites = systemData.getPrerequisitesByCourse(RawRecordConstants.M117);
        scanPrerequisites(cache, RawRecordConstants.M117, m117Prerequisites);
        final List<String> m118Prerequisites = systemData.getPrerequisitesByCourse(RawRecordConstants.M118);
        scanPrerequisites(cache, RawRecordConstants.M118, m118Prerequisites);
        final List<String> m124Prerequisites = systemData.getPrerequisitesByCourse(RawRecordConstants.M124);
        scanPrerequisites(cache, RawRecordConstants.M124, m124Prerequisites);
        final List<String> m125Prerequisites = systemData.getPrerequisitesByCourse(RawRecordConstants.M125);
        scanPrerequisites(cache, RawRecordConstants.M125, m125Prerequisites);
        final List<String> m126Prerequisites = systemData.getPrerequisitesByCourse(RawRecordConstants.M126);
        scanPrerequisites(cache, RawRecordConstants.M126, m126Prerequisites);

        final List<String> math125Prerequisites = systemData.getPrerequisitesByCourse(RawRecordConstants.MATH125);
        scanPrerequisites(cache, RawRecordConstants.MATH125, math125Prerequisites);
        final List<String> math126Prerequisites = systemData.getPrerequisitesByCourse(RawRecordConstants.MATH126);
        scanPrerequisites(cache, RawRecordConstants.MATH126, math126Prerequisites);

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
        if (checkCredit(RawRecordConstants.MATH125)) {
            this.creditFor.add(RawRecordConstants.M125);
        }
        if (checkCredit(RawRecordConstants.MATH126)) {
            this.creditFor.add(RawRecordConstants.M126);
        }
    }

    /**
     * Tests whether the student has satisfied the prerequisites for a given course.
     *
     * @param theCourseId the course ID
     * @return true if the student has satisfied the prerequisites for the course
     */
    public boolean hasSatisfiedPrerequisitesFor(final String theCourseId) {

        return this.satisfied.contains(theCourseId);
    }

    /**
     * Tests whether the student has satisfied the prerequisites for a given course through transfer credit.
     *
     * @param theCourseId the course ID
     * @return true if the student has satisfied the prerequisites for the course with transfer credit
     */
    public boolean hasSatisfiedPrerequisitesByTransferFor(final String theCourseId) {

        return this.satisfiedByTransfer.contains(theCourseId);
    }

    /**
     * Tests whether the student has transfer credit for a course but not CSU credit.
     *
     * @param theCourseId the course ID
     * @return true if the student has credit for the course by transfer
     */
    boolean isCreditByTransferFor(final String theCourseId) {

        return this.creditByTransfer.contains(theCourseId);
    }

    /**
     * Tests whether the student has credit for a given course (by having taken the course, earned placement credit, or
     * by transfer credit).
     *
     * @param theCourseId the course ID
     * @return true if the student has credit for the course
     */
    boolean hasCreditFor(final String theCourseId) {

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
     * @throws SQLException if there is an error accessing the database
     */
    private void scanPrerequisites(final Cache cache, final String courseId,
                                   final Iterable<String> prereqCourseIds) throws SQLException {

        boolean prereqSatisfied = false;

        // Scan for STCOURSE records marked as "prereq_satisfied = 'Y'", even records that have been dropped.

        for (final RawStcourse test : this.allHistory) {
            if (test.course.equals(courseId) && "Y".equals(test.prereqSatis)) {
                prereqSatisfied = true;
                if (DEBUG) {
                    Log.info("* Satisfied prerequisite for ", courseId,
                            " because past registration shows prerequisites satisfied");
                }
                break;
            }
        }

        // If not previously satisfied, test for data that indicates satisfied.
        if (!prereqSatisfied) {
            outer:
            for (final String preq : prereqCourseIds) {
                // Test for a previously completed course or prerequisite course
                for (final RawStcourse complete : this.allCompletions) {
                    if (preq.equals(complete.course)) {
                        prereqSatisfied = true;
                        if (DEBUG) {
                            Log.info("* Satisfied prerequisite for ", courseId,
                                    " because course was completed in the past");
                        }
                        break outer;
                    }
                }

                // Test for placement credit in the course or a prerequisite course
                for (final RawMpeCredit cred : this.allPlacementCredit) {
                    if (preq.equals(cred.course)) {
                        prereqSatisfied = true;
                        if (DEBUG) {
                            Log.info("* Satisfied prerequisite for ", courseId,
                                    " through placement credit for ", cred.course);
                        }
                        break outer;
                    }
                }

                // Search for transfer credit in course or a prerequisite course
                for (final RawFfrTrns xfer : this.allTransfer) {
                    if (preq.equals(xfer.course)) {
                        this.satisfiedByTransfer.add(courseId);
                        if (DEBUG) {
                            Log.info("* Satisfied prerequisite for ", courseId,
                                    " through transfer credit for ", xfer.course);
                        }
                        prereqSatisfied = true;
                        break outer;
                    }
                }
            }
        }

        // Special-case handling - section 801/809 students can start MATH 117 without prerequisites, but they
        // get a longer Skills Review (ideally, we would only afford this to non-degree-seeking DCE students, but we
        // tend not to get that data from CSU Online

        if (!prereqSatisfied && RawRecordConstants.M117.equals(courseId)) {

            final TermRec activeTerm = cache.getSystemData().getActiveTerm();
            final List<RawStcourse> allCurrent = RawStcourseLogic.getActiveForStudent(cache, this.studentId,
                    activeTerm.term);

            String sect = null;
            for (final RawStcourse test : allCurrent) {
                if (RawRecordConstants.M117.equals(test.course)) {
                    sect = test.sect;
                    break;
                }
            }

            if ("801".equals(sect) || "809".equals(sect)) {
                if (DEBUG) {
                    Log.info("* Satisfied prerequisite because registered in 80X section of 117");
                }
                prereqSatisfied = true;
            }
        }

        if (prereqSatisfied) {
            this.satisfied.add(courseId);
        }
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
                        if (!this.creditByTransfer.contains(courseId)) {
                            this.creditByTransfer.add(courseId);
                        }
                        hasCredit = true;
                        break;
                    }
                }
            }
        }

        return hasCredit;
    }

    /**
     * Main method to exercise the logic object.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getWebProfile(Contexts.PLACEMENT_HOST, Contexts.ROOT_PATH);
        final Cache cache = new Cache(profile);

        try {
            final PrerequisiteLogic prereq = new PrerequisiteLogic(cache, "837069087");

            Log.fine("Student: ", prereq.studentId);

            Log.fine(" OK for 117: " + prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M117)
                     + "; by transfer: " + prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M117));
            Log.fine(" OK for 118: " + prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M118)
                     + "; by transfer: " + prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M118));
            Log.fine(" OK for 124: " + prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M124)
                     + "; by transfer: " + prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M124));
            Log.fine(" OK for 125: " + prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M125)
                     + "; by transfer: " + prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M125));
            Log.fine(" OK for 126: " + prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M126)
                     + "; by transfer: " + prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M126));

            Log.fine(" OK for MATH 125: " + prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.MATH125)
                     + "; by transfer: " + prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.MATH125));
            Log.fine(" OK for MATH 126: " + prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.MATH126)
                     + "; by transfer: " + prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.MATH126));

            Log.fine(" Credit for 117: " + prereq.hasCreditFor(RawRecordConstants.M117));
            Log.fine(" Credit for 118: " + prereq.hasCreditFor(RawRecordConstants.M118));
            Log.fine(" Credit for 124: " + prereq.hasCreditFor(RawRecordConstants.M124));
            Log.fine(" Credit for 125: " + prereq.hasCreditFor(RawRecordConstants.M125));
            Log.fine(" Credit for 126: " + prereq.hasCreditFor(RawRecordConstants.M126));
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}
