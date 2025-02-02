package dev.mathops.db.cfg;

import java.util.ArrayList;
import java.util.List;

/**
 * A "web-context" object from the database configuration file.
 */
public final class WebContext {

    /** The profile ID. */
    public final String host;

    /** The list of sites. */
    private final List<Site> sites;

    /**
     * Constructs a new {@code Profile}.
     *
     * @param theHost the host
     */
    WebContext(final String theHost) {

        if (theHost == null || theHost.isBlank()) {
            final String msg = Res.get(Res.WEB_CONTEXT_NULL_HOST);
            throw new IllegalArgumentException(msg);
        }

        this.host = theHost;
        this.sites = new ArrayList<>(5);
    }

    /**
     * Gets the list of sites.
     *
     * @return the list of sites
     */
    List<Site> getSites() {

        return this.sites;
    }
}
