package dev.mathops.app.checkin;

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
final class CheckInDataCourseExams {

    /** The course ID (begins with "M " for OLD courses, and with "MATH " for NEW courses). */
    final String courseId;

    /** The OLD course Unit 1 exam status ({@code null} if not an OLD course). */
    ExamStatus unit1Exam;

    /** The OLD course Unit 2 exam status ({@code null} if not an OLD course). */
    ExamStatus unit2Exam;

    /** The OLD course Unit 3 exam status ({@code null} if not an OLD course). */
    ExamStatus unit3Exam;

    /** The OLD course Unit 4 exam status ({@code null} if not an OLD course). */
    ExamStatus unit4Exam;

    /** The OLD course Final Exam status ({@code null} if not an OLD course). */
    ExamStatus finalExam;

    /** The Mastery Exam status ({@code null} if not a NEW course). */
    ExamStatus masteryExam;

    /** The Challenge Exam status. */
    ExamStatus challengeExam;

    /**
     * Constructs a new {@code CheckInDataCourseExams}.
     *
     * @param theCourseId the course ID (begins with "M " for OLD courses, and with "MATH " for NEW courses)
     */
    CheckInDataCourseExams(final String theCourseId) {

        this.courseId = theCourseId;
    }
}
