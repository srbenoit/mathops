package dev.mathops.app.checkin;

import dev.mathops.db.logic.challenge.ChallengeExamLogic;
import dev.mathops.db.schema.RawRecordConstants;

import java.util.List;

/**
 * A record to store the various course/exam numbers associated with a course.
 */
public record CourseNumbers(String oldCourseId, String newCourseId, String challengeId, String tutorialId) {

    /** A course. */
    public static final CourseNumbers MATH117 = new CourseNumbers(RawRecordConstants.M117, RawRecordConstants.MATH117,
            ChallengeExamLogic.M117_CHALLENGE_EXAM_ID, RawRecordConstants.M1170);

    /** A course. */
    public static final CourseNumbers MATH118 = new CourseNumbers(RawRecordConstants.M118, RawRecordConstants.MATH118,
            ChallengeExamLogic.M118_CHALLENGE_EXAM_ID, RawRecordConstants.M1180);

    /** A course. */
    public static final CourseNumbers MATH124 = new CourseNumbers(RawRecordConstants.M124, RawRecordConstants.MATH124,
            ChallengeExamLogic.M124_CHALLENGE_EXAM_ID, RawRecordConstants.M1240);

    /** A course. */
    public static final CourseNumbers MATH125 = new CourseNumbers(RawRecordConstants.M125, RawRecordConstants.MATH125,
                      ChallengeExamLogic.M125_CHALLENGE_EXAM_ID, RawRecordConstants.M1250);

    /** A course. */
    public static final CourseNumbers MATH126 = new CourseNumbers(RawRecordConstants.M126, RawRecordConstants.MATH126,
            ChallengeExamLogic.M126_CHALLENGE_EXAM_ID, RawRecordConstants.M1260);


    /** The list of courses to consider with their IDs. */
    public static final List<CourseNumbers> COURSES = List.of(MATH117, MATH118, MATH124, MATH125, MATH126);

    /** The number of courses. */
    public static final int NUM_COURSES = COURSES.size();

    /**
     * Tests whether a given course ID matches either the old or the new course ID of this course.
     *
     * @param courseId the course ID to test
     * @return {@code true} if {@code courseId} matches either the old or new course ID
     */
    boolean isMatching(final String courseId) {

        return courseId != null && (courseId.equals(this.oldCourseId) || courseId.equals(this.newCourseId));
    }

    /**
     * Tests whether a given course ID matches the NEW course ID of this course.
     *
     * @param courseId the course ID to test
     * @return {@code true} if {@code courseId} matches the new course ID
     */
    boolean isNew(final String courseId) {

        return courseId != null && courseId.equals(this.newCourseId);
    }

    /**
     * Tests whether a given course ID matches the OLD course ID of this course.
     *
     * @param courseId the course ID to test
     * @return {@code true} if {@code courseId} matches the old course ID
     */
    boolean isOld(final String courseId) {

        return courseId != null && courseId.equals(this.oldCourseId);
    }
}

