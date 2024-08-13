package dev.mathops.web.site.admin.genadmin;

/**
 * Topics in the administrative website.
 */
public enum EAdminTopic {

    /** Student status. */
    STUDENT_STATUS("Student Status", "student.html"),

    /** Monitor System. */
    MONITOR_SYSTEM("Monitor System", "monitor.html"),

    /** Test student. */
    TEST_STUDENTS("Test Student", "test_student.html"),

    /** Utilities. */
    UTILITIES("Utilities", "utilities.html"),

    /** Server administration. */
    SERVER_ADMIN("Server <br class='hidebelow800'>Administration", "server_admin.html"),

    /** Database administration. */
    DB_ADMIN("Database <br class='hidebelow800'>Administration", "db_admin.html"),

    /** Web Site administration. */
    SITE_ADMIN("Web Site <br class='hidebelow800'>Administration", "site_admin.html"),

    /** Logic testing. */
    LOGIC_TESTING("Logic Testing", "logic_testing.html");

    /** The button label. */
    final String label;

    /** The page URL. */
    final String url;

    /**
     * Constructs a new {@code EAdminTopic}.
     *
     * @param theLabel the button label
     * @param theUrl   the page URL
     */
    EAdminTopic(final String theLabel, final String theUrl) {

        this.label = theLabel;
        this.url = theUrl;
    }
}
