package dev.mathops.web.site.admin.genadmin;

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

    // Subtopics of Automation

    /** Automation - Task List Management. */
    AUTO_BOT("Student Bots", "automation_bot.html");

    /** The button label. */
    public final String label;

    /** The page URL. */
    /* default */ final String url;

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
