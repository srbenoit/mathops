package dev.mathops.db.old.logic.mathplan.data;

import dev.mathops.db.old.rawrecord.RawCourse;

/**
 * Information on a single course in a course sequence.
 */
public final class CourseInfo {

    /** The course record. */
    public final RawCourse course;

    /** Flag indicating course was added to satisfy a prerequisite, not from a major/program. */
    final boolean addedAsPrerequisite;

    /** The required grade to satisfy prerequisites of down-stream courses. */
    Float requiredGrade = null;

    /** The earned grade. */
    Float earnedGrade = null;

    /** The course status. */
    public ECourseStatus status;

    /**
     * Constructs a new {@code CourseInfo}.
     *
     * @param theCourse       the course record
     * @param isAddedAsPrereq flag indicating course was added to satisfy a prerequisite, not from a major/program
     */
    public CourseInfo(final RawCourse theCourse, final boolean isAddedAsPrereq) {

        this.course = theCourse;
        this.addedAsPrerequisite = isAddedAsPrereq;
        this.status = ECourseStatus.NONE;
    }
}
