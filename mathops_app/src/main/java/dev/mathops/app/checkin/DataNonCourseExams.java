package dev.mathops.app.checkin;

import dev.mathops.core.builder.SimpleBuilder;

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
final class DataNonCourseExams {

    /** The ELM exam (stored with unit 0). */
    DataExamStatus elmExam = null;

    /** The Precalculus tutorial exam to place out of MATH 117 (stored with unit 4). */
    DataExamStatus precalc117 = null;

    /** The Precalculus tutorial exam to place out of MATH 118 (stored with unit 4). */
    DataExamStatus precalc118 = null;

    /** The Precalculus tutorial exam to place out of MATH 124 (stored with unit 4). */
    DataExamStatus precalc124 = null;

    /** The Precalculus tutorial exam to place out of MATH 125 (stored with unit 4). */
    DataExamStatus precalc125 = null;

    /** The Precalculus tutorial exam to place out of MATH 126 (stored with unit 4). */
    DataExamStatus precalc126 = null;

    /** The User's exam (stored with unit 0). */
    DataExamStatus usersExam = null;

    /** The Math Placement Tool (stored with unit 1). */
    DataExamStatus placement = null;

    /**
     * Constructs a new {@code DataNonCourseExams}.
     */
    DataNonCourseExams() {

        // No action
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("DataNonCourseExams{elmExam='", this.elmExam, "', precalc117=", this.precalc117,
                ", precalc118=", this.precalc118, ", precalc124=", this.precalc124, ", precalc125=", this.precalc125,
                ", precalc126=", this.precalc126, ", usersExam=", this.usersExam, ", placement=", this.placement, "}");
    }
}
