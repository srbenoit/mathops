package dev.mathops.dbjobs.report.usage;

/**
 * A student-course record.
 */
final class StCourse {

    /** The student ID. */
    final String studentId;

    /** The course ID. */
    final String courseId;

    /**
     * Constructs a new {@code StCourse}.
     *
     * @param theStudentId the student ID
     * @param theCourseId  the course ID
     */
    StCourse(final String theStudentId, final String theCourseId) {

        this.studentId = theStudentId;
        this.courseId = theCourseId;
    }
}
