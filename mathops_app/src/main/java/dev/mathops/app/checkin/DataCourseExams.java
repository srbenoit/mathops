package dev.mathops.app.checkin;

import dev.mathops.text.builder.SimpleBuilder;

/**
 * A container for the set of exams associated with a single course, along with the student's status.
 *
 * <p>
 * A student may be only in either an OLD or a NEW version of a Precalculus course.  For the OLD versions, the course
 * has four Unit exams and a Final exam.  For the NEW versions, the course has a single "Mastery Exam" that contains
 * some collection of standards that will be grouped into that exam.
 *
 * <p>
 * Every course also has a "Challenge Exam" whose eligibility is stored in this class as well.
 */
public final class DataCourseExams {

    /** The course numbers. */
    public final CourseNumbers courseNumbers;

    /** True if the student is registered in the OLD version of the course. */
    public boolean registeredInOld = false;

    /** True if the student is registered in the NEW version of the course. */
    public boolean registeredInNew = false;

    /** The OLD course Unit 1 exam status, or {@code null} if not an OLD course (has old course ID). */
    public DataExamStatus unit1Exam = null;

    /** The OLD course Unit 2 exam status, or {@code null} if not an OLD course (has old course ID). */
    public DataExamStatus unit2Exam = null;

    /** The OLD course Unit 3 exam status, or {@code null} if not an OLD course (has old course ID). */
    public DataExamStatus unit3Exam = null;

    /** The OLD course Unit 4 exam status, or {@code null} if not an OLD course (has old course ID). */
    public DataExamStatus unit4Exam = null;

    /** The OLD course Final Exam status, or {@code null} if not an OLD course (has old course ID). */
    public DataExamStatus finalExam = null;

    /** The Mastery Exam status, or {@code null} if not a NEW course (has new course ID and unit 0) */
    public DataExamStatus masteryExam = null;

    /** The Challenge Exam status (has challenge exam course ID and unit 0). */
    public DataExamStatus challengeExam = null;

    /**
     * Constructs a new {@code DataCourseExams}.
     *
     * @param theCourseNumbers the course numbers
     */
    DataCourseExams(final CourseNumbers theCourseNumbers) {

        this.courseNumbers = theCourseNumbers;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String regOldStr = Boolean.toString(this.registeredInOld);
        final String regNewStr = Boolean.toString(this.registeredInNew);

        return SimpleBuilder.concat("DataCourseExams{courseNumbers='", this.courseNumbers, "', registeredInOld=",
                regOldStr, "', registeredInNew=", regNewStr, ", unit1Exam=", this.unit1Exam, ", unit2Exam=",
                this.unit2Exam, ", unit3Exam=", this.unit3Exam, ", unit4Exam=", this.unit4Exam, ", finalExam=",
                this.finalExam, ", masteryExam=", this.masteryExam, ", challengeExam=", this.challengeExam, "}");
    }
}
