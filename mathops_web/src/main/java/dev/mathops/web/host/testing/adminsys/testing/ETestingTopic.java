package dev.mathops.web.host.testing.adminsys.testing;

/**
 * Topics in the testing management website.
 */
public enum ETestingTopic {

    /** Powering testing stations on or off. */
    POWER_ON_OFF("Power Stations On/Off", "power.html"),

    /** Web servers. */
    ENABLE_DISABLE("Enable/Disable Stations", "enable.html"),

    /** Issue Exam. */
    ISSUE("Issue Exam", "issue.html"),

    /** Cancel exam. */
    CANCEL("Cancel Exam", "cancel.html");

    /** The button label. */
    private final String label;

    /** The page URL. */
    private final String url;

    /**
     * Constructs a new {@code ETestingTopic}.
     *
     * @param theLabel the button label
     * @param theUrl   the page URL
     */
    ETestingTopic(final String theLabel, final String theUrl) {

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
