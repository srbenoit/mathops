package dev.mathops.db.logic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.svc.term.TermRec;

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
    private final PrerequisiteLogic prerequisiteLogic;

    /** Map from course ID to status container for each course's challenge exam. */
    private final Map<String, ChallengeExamStatus> status;

    /**
     * Constructs a new {@code ChallengeLogic}.
     *
     * @param data the student data object
     * @throws SQLException if there is an error accessing the database
     */
    public ChallengeExamLogic(final StudentData data) throws SQLException {

        if (data == null) {
            throw new IllegalArgumentException("Student data may not be null");
        }

        final TermRec active = data.getSystemData().getActiveTerm();
        this.activeRegs = data.getActiveRegistrations(active.term);
        this.allChallengeAttempts = data.getChallengeExams();
        this.prerequisiteLogic = new PrerequisiteLogic(data);

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
                } else if (this.prerequisiteLogic.hasSatisfiedPrereqsFor(theCourseId)) {
                    available = examId;
                } else {
                    reason = "To take Challenge Exam, you must have satisfied the prerequisites for the course.";
                }
            } else {
                reason = "Challenge exam was already taken on " + TemporalUtils.FMT_MDY.format(attempted)
                        + CoreConstants.DOT;
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

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("ChallengeExamLogic{activeRegs=", this.activeRegs, ", allChallengeAttempts=",
                this.allChallengeAttempts, ", prerequisiteLogic=", this.prerequisiteLogic, ", status=", this.status,
                "}");
    }
}
