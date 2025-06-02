package dev.mathops.web.host.testing.adminsys.genadmin;

/**
 * Topics in the administrative website.
 */
public enum EAdmSubtopic {

    // Subtopics of Server Administration

    /** Server Administration - Sessions. */
    SRV_SESSIONS("Sessions", "srvadm_sessions.html"),

    /** Server Administration - Maintenance. */
    SRV_MAINTENANCE("Maintenance", "srvadm_maintenance.html"),

    /** Server Administration - Control. */
    SRV_CONTROL("Control", "srvadm_control.html"),

    /** Server Administration - Diagnostics. */
    SRV_DIAGNOSTICS("Diagnostics", "srvadm_diagnostics.html"),

    // Subtopics of Database Administration

    /** Database Administration - Contexts. */
    DB_CONTEXTS("Contexts", "dbadm_contexts.html"),

    /** Database Administration - Batch. */
    DB_BATCH("Batch Jobs", "dbadm_batch.html"),

    /** Database Administration - Reports. */
    DB_REPORTS("Reports", "dbadm_reports.html"),

    /** Database Administration - Queries. */
    DB_QUERIES("Queries", "dbadm_queries.html"),

    /** Database Administration - Metadata. */
    DB_META("Metadata", "dbadm_metadata.html"),

    /** Database Administration - PROD views. */
    DB_PROD_VIEWS("Views", "dbadm_prod_views.html"),

    // Subtopics of Logic Testing

    /** Logic Testing - Registrations. */
    LOGIC_REGISTRATIONS("Registrations", "logic_registrations.html"),

    /** Logic Testing - Prerequisites. */
    LOGIC_PREREQUISITES("Prerequisites", "logic_prerequisites.html"),

    /** Logic Testing - Calendar. */
    LOGIC_CALENDAR("Calendar", "logic_calendar.html"),

    /** Logic Testing - Milestones. */
    LOGIC_MILESTONES("Milestones", "logic_milestones.html");

    /** The button label. */
    public final String label;

    /** The page URL. */
    final String url;

    /**
     * Constructs a new {@code EAdmSubtopic}.
     *
     * @param theLabel the button label
     * @param theUrl   the page URL
     */
    EAdmSubtopic(final String theLabel, final String theUrl) {

        this.label = theLabel;
        this.url = theUrl;
    }
}
