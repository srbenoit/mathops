package dev.mathops.session.sitelogic.mathplan.data;

import dev.mathops.db.old.rawrecord.RawCourse;

/**
 * Information on a single course in a course sequence.
 */
public final class CourseInfo {

    /** The course record. */
    public final RawCourse course;

    /** Flag indicating course was added to satisfy a prereq, not from a major/program. */
    final boolean addedAsPrereq;

    /** The required grade to satisfy prerequisites of down-stream courses. */
    Float requiredGrade;

    /** The earned grade. */
    Float earnedGrade;

    /** The course status. */
    public ECourseStatus status;

    /**
     * Constructs a new {@code CourseInfo}.
     *
     * @param theCourse       the course record
     * @param isAddedAsPrereq flag indicating course was added to satisfy a prerequisite, not from a major/program
     */
    CourseInfo(final RawCourse theCourse, final boolean isAddedAsPrereq) {

        this.course = theCourse;
        this.addedAsPrereq = isAddedAsPrereq;
        this.status = ECourseStatus.NONE;
    }
}
