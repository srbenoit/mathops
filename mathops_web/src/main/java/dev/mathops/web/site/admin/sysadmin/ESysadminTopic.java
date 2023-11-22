package dev.mathops.web.site.admin.sysadmin;

/**
 * Topics in the system administrative website.
 */
public enum ESysadminTopic {

    /** Database servers. */
    DB_SERVERS("Database Servers", "db.html"),

    /** Web servers. */
    WEB_SERVERS("Web Servers", "web_servers.html"),

    /** Media servers */
    MEDIA_SERVERS("Media Servers", "media_servers.html"),

    /** TURN servers */
    TURN_SERVERS("TURN Servers", "turn_servers.html");

    /** The button label. */
    private final String label;

    /** The page URL. */
    private final String url;

    /**
     * Constructs a new {@code ESysadminTopic}.
     *
     * @param theLabel the button label
     * @param theUrl   the page URL
     */
    ESysadminTopic(final String theLabel, final String theUrl) {

        this.label = theLabel;
        this.url = theUrl;
    }

    /**
     * Gets the button label.
     *
     * @return the button label
     */
    public String getLabel() {

        return this.label;
    }

    /**
     * Gets the page URL.
     *
     * @return the page URL
     */
    public String getUrl() {

        return this.url;
    }
}
