package dev.mathops.db.old.logic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rec.MasteryAttemptQaRec;
import dev.mathops.db.old.rec.MasteryAttemptRec;
import dev.mathops.db.old.rec.MasteryExamRec;
import dev.mathops.db.old.reclogic.MasteryAttemptQaLogic;
import dev.mathops.db.old.reclogic.MasteryExamLogic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for the student's work record in a course based on mastery of standards.
 */
public final class StandardsMasteryLogic {

    /** Homeworks the student has taken. */
    private final List<RawSthomework> stHomeworks;

    /** Mastery exams associated with the course. */
    private final List<MasteryExamRec> masteryExams;

    /** Map from mastery exam ID to attempts the student has taken. */
    private final Map<String, List<MasteryAttemptRec>> masteryAttempts;

    /** Map from mastery exam ID to per-question results on exams. */
    private final Map<String, List<MasteryAttemptQaRec>> masteryAttemptQuestions;

    /** Map from mastery exam ID to Boolean "mastered" indicator. */
    private final Map<String, Boolean> mastered;

    /**
     * Constructs a new {@code StandardsMasteryLogic}.
     *
     * @param studentData the student data object
     * @param courseId    the course ID
     * @throws SQLException of there is an error accessing the database
     */
    public StandardsMasteryLogic(final StudentData studentData, final String courseId) throws SQLException {

        this.stHomeworks = studentData.getStudentHomeworkForCourse(courseId);

        final Cache cache = studentData.getCache();

        this.masteryExams = MasteryExamLogic.get(cache).queryActiveByCourse(cache, courseId);
        final int numExams = this.masteryExams.size();

        this.masteryAttempts = new HashMap<>(numExams);
        this.masteryAttemptQuestions = new HashMap<>(numExams);
        this.mastered = new HashMap<>(numExams);

        for (final MasteryExamRec exam : this.masteryExams) {
            final List<MasteryAttemptRec> attempts = studentData.getMasteryAttemptsByExamId(exam.examId);

            this.masteryAttempts.put(exam.examId, attempts);
            final int numAttempts = attempts.size();

            final List<MasteryAttemptQaRec> qaList = new ArrayList<>(numAttempts << 1);
            this.masteryAttemptQuestions.put(exam.examId, qaList);

            boolean isMastered = false;
            for (final MasteryAttemptRec attempt : attempts) {

                if ("Y".equals(attempt.passed)) {
                    isMastered = true;
                }

                final List<MasteryAttemptQaRec> qa = MasteryAttemptQaLogic.get(cache).queryByAttempt(cache,
                        attempt.serialNbr, attempt.examId);
                qaList.addAll(qa);
            }

            final Boolean masteredObj = Boolean.valueOf(isMastered);
            this.mastered.put(exam.examId, masteredObj);
        }
    }

    /**
     * Tests whether the student has mastered enough standards in each half of the course to pass the course.
     *
     * @return true if the student has mastered enough standards
     */
    public boolean areEnoughStandardsMasteredToPassCourse() {

        int numberMasteredFirstHalf = 0;
        int numberMasteredSecondHalf = 0;

        // FIXME: Move the number of units into data to eliminate hardcoded cutoff between halves

        for (final MasteryExamRec masteryExam : this.masteryExams) {
            final Boolean masteredFlag = this.mastered.get(masteryExam.examId);

            if (Boolean.TRUE.equals(masteredFlag)) {
                if (masteryExam.unit.intValue() < 5) {
                    ++numberMasteredFirstHalf;
                } else {
                    ++numberMasteredSecondHalf;
                }
            }
        }

        // FIXME: Move the number needed per half into data

        return numberMasteredFirstHalf >= 10 && numberMasteredSecondHalf >= 10;
    }

    /**
     * Returns the total number of standards.
     *
     * @return the number of standards
     */
    public int countTotalStandards() {

        return this.masteryExams.size();
    }

    /**
     * Returns the number of standards the student has mastered.
     *
     * @return the number of standards mastered
     */
    public int countCompleteStandards() {

        int completed = 0;

        for (final Boolean flag : this.mastered.values()) {
            if (Boolean.TRUE.equals(flag)) {
                ++completed;
            }
        }

        return completed;
    }

    /**
     * Tests whether the standard associated with a mastery exam has been mastered.
     *
     * @param examId the mastery exam ID
     * @return true if the student has mastered the standard
     */
    public boolean hasMasteredStandard(final String examId) {

        boolean isMastered = false;

        final List<MasteryAttemptRec> attempts = this.masteryAttempts.get(examId);
        for (final MasteryAttemptRec attempt : attempts) {
            if ("Y".equals(attempt.passed)) {
                isMastered = true;
                break;
            }
        }

        return isMastered;
    }

    /**
     * Checks which questions have already been answered twice correctly on an exam.
     *
     * @param examId the mastery exam ID
     * @return 0 if neither question was answered correctly; 1 if question 1 has been answered correctly twice, but not
     *         question 2; 2 if question 2 was answered correctly twice, but not question 1; 3 if both have been
     *         answered correctly twice (this number is the bit-wise combination of 0x01 representing question 1, and
     *         0x02 representing question2)
     */
    public int whichQuestionsPassedTwice(final String examId) {

        int numQuestion1Correct = 0;
        int numQuestion2Correct = 0;

        final List<MasteryAttemptQaRec> allQa = this.masteryAttemptQuestions.get(examId);
        for (final MasteryAttemptQaRec qa : allQa) {
            if ("Y".equals(qa.correct)) {
                final int q = qa.questionNbr.intValue();

                if (q == 1) {
                    ++numQuestion1Correct;
                } else if (q == 2) {
                    ++numQuestion2Correct;
                }
            }
        }

        int result = 0;

        if (numQuestion1Correct >= 2) {
            result += 0x01;
        }
        if (numQuestion2Correct >= 2) {
            result += 0x02;
        }

        return result;
    }

    /**
     * Tests whether the student has passed the "standard assignment" to enable mastery of a standard.
     *
     * @param unit      the unit (typically 1 through 8 for a course)
     * @param objective the objective (typically 1 through 3)
     * @return true if the student has passed a homework of type "ST" for the indicated unit and objective
     */
    public boolean hasPassedStandardAssignment(final int unit, final int objective) {

        boolean passed = false;

        for (final RawSthomework hw : this.stHomeworks) {
            if (hw.unit.intValue() == unit && hw.objective.intValue() == objective && "Y".equals(hw.passed)) {
                passed = true;
                break;
            }
        }

        return passed;
    }

    /**
     * Returns the number of standards for which the student is eligible to demonstrate mastery.
     *
     * @return the number of standards
     */
    public int countAvailableStandards() {

        int available = 0;

        for (final MasteryExamRec exam : this.masteryExams) {
            final int unit = exam.unit.intValue();
            final int obj = exam.objective.intValue();
            if (hasPassedStandardAssignment(unit, obj) && !hasMasteredStandard(exam.examId)) {
                ++available;
            }
        }

        return available;
    }

    /**
     * Assembles a list of the mastery exams for which the student is eligible to attempt mastery.
     *
     * @return a list of the mastery exams for which the student is eligible
     */
    public List<MasteryExamRec> gatherEligibleStandards() {

        final List<MasteryExamRec> available = new ArrayList<>(10);

        for (final MasteryExamRec exam : this.masteryExams) {
            final int unit = exam.unit.intValue();
            final int obj = exam.objective.intValue();
            if (hasPassedStandardAssignment(unit, obj) && !hasMasteredStandard(exam.examId)) {
                available.add(exam);
            }
        }

        return available;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("StandardsMasteryLogic{stHomeworks=", this.stHomeworks,
                ", masteryExams=", this.masteryExams, ", masteryAttempts=", this.masteryAttempts, "}");
    }
}