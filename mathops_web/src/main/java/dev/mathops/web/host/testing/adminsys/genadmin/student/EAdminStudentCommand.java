package dev.mathops.web.host.testing.adminsys.genadmin.student;

/**
 * Commands that can be issued within a student view.
 */
enum EAdminStudentCommand {

    /** General student information. */
    STUDENT_INFO("Student", "student_info.html"),

    /** Student placement status. */
    PLACEMENT("Placement", "student_placement.html"),

    /** Student course registrations. */
    REGISTRATIONS("Registrations", "student_course_status.html"),

    /** Activity. */
    ACTIVITY("Activity", "student_course_activity.html"),

    /** Math plan status. */
    MATH_PLAN("Math Plan", "student_math_plan.html");

    /** The button label. */
    final String label;

    /** The page URL. */
    final String url;

    /**
     * Constructs a new {@code EAdminStudentCommand}.
     *
     * @param theLabel the button label
     * @param theUrl   the page URL
     */
    EAdminStudentCommand(final String theLabel, final String theUrl) {

        this.label = theLabel;
        this.url = theUrl;
    }
}
