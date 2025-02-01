package dev.mathops.db.old.logic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for logic relating to a student's challenge status and eligibility to take challenge exams. This object
 * does not provide eligibility for the math placement tool or to access tutorials or take tutorial exams.
 *
 * <p>
 * There is one challenge exam per precalculus course. Their IDs are:
 *
 * <ul>
 * <li>MC117 - MATH 117 Challenge Exam
 * <li>MC118 - MATH 118 Challenge Exam
 * <li>MC124 - MATH 124 Challenge Exam
 * <li>MC125 - MATH 125 Challenge Exam
 * <li>MC126 - MATH 126 Challenge Exam
 * </ul>
 *
 * <p>
 * A student is eligible for a challenge exam if all the following conditions are true:
 * <ul>
 * <li>The student has never attempted the challenge exam before (one attempt per exam).
 * <li>The student is not currently enrolled in the course or completing an Incomplete from a prior
 * term.
 * <li>The student has met the prerequisites for the course.
 * </ul>
 * <p>
 * Eligibility does not depend on application term or membership in any special student categories.
 */
public final class ChallengeExamLogic {

    /** The ID for the MATH 117 challenge exam. */
    public static final String M117_CHALLENGE_EXAM_ID = "MC117";

    /** The ID for the MATH 118 challenge exam. */
    public static final String M118_CHALLENGE_EXAM_ID = "MC118";

    /** The ID for the MATH 124 challenge exam. */
    public static final String M124_CHALLENGE_EXAM_ID = "MC124";

    /** The ID for the MATH 125 challenge exam. */
    public static final String M125_CHALLENGE_EXAM_ID = "MC125";

    /** The ID for the MATH 126 challenge exam. */
    public static final String M126_CHALLENGE_EXAM_ID = "MC126";

    /** The active precalculus registrations for the student. */
    private final List<RawStcourse> activeRegs;

    /** The list of all challenge attempts on the student's record. */
    private final List<RawStchallenge> allChallengeAttempts;

    /** Prerequisite-checking logic. */
    private final PrerequisiteLogic prereqLogic;

    /** Map from course ID to status container for each course's challenge exam. */
    private final Map<String, ChallengeExamStatus> status;

    /**
     * Constructs a new {@code ChallengeLogic}.
     *
     * @param cache        the data cache
     * @param theStudentId the student ID
     * @throws SQLException if there is an error accessing the database
     */
    public ChallengeExamLogic(final Cache cache, final String theStudentId) throws SQLException {

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }

        final TermRec active = cache.getSystemData().getActiveTerm();

        this.activeRegs = RawStcourseLogic.getActiveForStudent(cache, theStudentId, active.term);

        this.allChallengeAttempts = RawStchallengeLogic.queryByStudent(cache, theStudentId);

        this.prereqLogic = new PrerequisiteLogic(cache, theStudentId);

        this.status = new HashMap<>(5);

        computeStatus(RawRecordConstants.M117);
        computeStatus(RawRecordConstants.M118);
        computeStatus(RawRecordConstants.M124);
        computeStatus(RawRecordConstants.M125);
        computeStatus(RawRecordConstants.M126);
    }

    /**
     * Computes the status for a particular course.
     *
     * @param theCourseId the course ID
     */
    private void computeStatus(final String theCourseId) {

        final String examId = getExamId(theCourseId);

        if (examId != null) {
            String available = null;
            String reason = null;

            // See if the challenge exam has been attempted previously
            LocalDate attempted = null;
            for (final RawStchallenge test : this.allChallengeAttempts) {
                if (examId.equals(test.version)) {
                    attempted = test.examDt;
                    break;
                }
            }

            if (attempted == null) {
                // See if currently enrolled
                boolean enrolled = false;
                for (final RawStcourse reg : this.activeRegs) {
                    if (theCourseId.equals(reg.course)) {
                        enrolled = true;
                        break;
                    }
                }

                if (enrolled) {
                    reason = "Course challenge exam may not be taken while enrolled in the course.";
                } else if (this.prereqLogic.hasSatisfiedPrerequisitesFor(theCourseId)) {
                    available = examId;
                } else {
                    reason = "In order to take Challenge Exam, you must have satisfied the "
                            + "prerequisites for the course.";
                }
            } else {
                reason = "Challenge exam was already taken on "
                        + TemporalUtils.FMT_MDY.format(attempted) + CoreConstants.DOT;
            }

            this.status.put(theCourseId, new ChallengeExamStatus(available, reason));
        }
    }

    /**
     * Gets the challenge exam ID associated with a course.
     *
     * @param theCourseId the course ID
     * @return the challenge exam ID
     */
    private static String getExamId(final String theCourseId) {

        String examId = null;

        if (RawRecordConstants.M117.equals(theCourseId) || RawRecordConstants.MATH117.equals(theCourseId)) {
            examId = M117_CHALLENGE_EXAM_ID;
        } else if (RawRecordConstants.M118.equals(theCourseId) || RawRecordConstants.MATH118.equals(theCourseId)) {
            examId = M118_CHALLENGE_EXAM_ID;
        } else if (RawRecordConstants.M124.equals(theCourseId) || RawRecordConstants.MATH124.equals(theCourseId)) {
            examId = M124_CHALLENGE_EXAM_ID;
        } else if (RawRecordConstants.M125.equals(theCourseId) || RawRecordConstants.MATH125.equals(theCourseId)) {
            examId = M125_CHALLENGE_EXAM_ID;
        } else if (RawRecordConstants.M126.equals(theCourseId) || RawRecordConstants.MATH126.equals(theCourseId)) {
            examId = M126_CHALLENGE_EXAM_ID;
        }

        return examId;
    }

    /**
     * Gets the status data container for a particular course.
     *
     * @param courseId the course ID
     * @return the status data container if found; {@code null} if no status available for the specified course
     */
    public ChallengeExamStatus getStatus(final String courseId) {

        return this.status.get(courseId);
    }

//    /**
//     * Main method to exercise the logic object.
//     *
//     * @param args command-line arguments
//     */
//     public static void main(final String... args) {
//
//     final ContextMap map = ContextMap.getDefaultInstance();
//     DbConnection.registerDrivers();
//
//     final DbProfile dbProfile =
//     map.getWebSiteProfile(Contexts.PLACEMENT_HOST, Contexts.ROOT_PATH).getDbProfile();
//     final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
//
//     try {
//     final DbConnection conn = ctx.checkOutConnection();
//     final Cache cache = new Cache(dbProfile, conn);
//
//     try {
//     ChallengeExamLogic logic = new ChallengeExamLogic(cache, dbProfile, //
//     "999011121");
//
//     Log.fine("Student who has taken unproctored 'POOOO', ",
//     "has 2 proctored attempts remaining:");
//     mainPrintResult(logic);
//
//     logic = new ChallengeExamLogic(cache, dbProfile, "999011122");
//
//     Log.fine(CoreConstants.EMPTY);
//     Log.fine("Student who has taken proctored, ",
//     "has unproctored and proctored remaining:");
//     mainPrintResult(logic);
//
//     logic = new ChallengeExamLogic(cache, dbProfile, "999033121");
//
//     Log.fine(CoreConstants.EMPTY);
//     Log.fine("Student who has taken 2 proctored, ",
//     "has no attempts remaining:");
//     mainPrintResult(logic);
//
//     logic = new ChallengeExamLogic(cache, dbProfile, "833165649");
//
//     Log.fine(CoreConstants.EMPTY);
//     Log.fine("Test student with course credit:");
//     mainPrintResult(logic);
//     } finally {
//     ctx.checkInConnection(conn);
//     }
//     } catch (SQLException ex) {
//     Log.warning(ex);
//     }
//     }

//    /**
//     * Prints results of one test.
//     *
//     * @param logic the logic object
//     */
//     private static void mainPrintResult(final ChallengeExamLogic logic) {
//
//     Log.fine(" MATH 117:");
//     mainPrintStatus(logic.getStatus(RawRecordConstants.M117));
//
//     Log.fine(" MATH 118:");
//     mainPrintStatus(logic.getStatus(RawRecordConstants.M118));
//
//     Log.fine(" MATH 124:");
//     mainPrintStatus(logic.getStatus(RawRecordConstants.M124));
//
//     Log.fine(" MATH 125:");
//     mainPrintStatus(logic.getStatus(RawRecordConstants.M125));
//
//     Log.fine(" MATH 126:");
//     mainPrintStatus(logic.getStatus(RawRecordConstants.M126));
//     }

//    /**
//     * Prints the contents of a <code>ChallengeExamStatus</code>.
//     *
//     * @param status the <code>ChallengeExamStatus</code> whose contents to print
//     */
//     private static void mainPrintStatus(final ChallengeExamStatus status) {
//
//     if (status.availableExamId != null) {
//     Log.fine(" Version available : " + status.availableExamId);
//     }
//     if (status.reasonUnavailable != null) {
//     Log.fine(" Why Unavailable : ", status.reasonUnavailable);
//     }
//     }
}
