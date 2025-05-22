package dev.mathops.db.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A "web-context" object from the database configuration file.
 */
public final class WebContext {

    /** The profile ID. */
    public final String host;

    /** The list of sites. */
    private final Map<String, Site> sites;

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
        this.sites = new HashMap<>(5);
    }

    /**
     * Gets the list of sites.
     *
     * @return the lists of sites
     */
    public List<String> getSites() {

        final Set<String> keys = this.sites.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Adds a site.
     *
     * @param site the site to add
     */
    void addSite(final Site site) {

        this.sites.put(site.path, site);
    }

    /**
     * Gets the {@code Site} with a specified path.
     *
     * @param path the path
     * @return the site
     */
    public Site getSite(final String path) {

        return this.sites.get(path);
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.host.hashCode();
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param o the other object
     * @return true if the objects are equal
     */
    public boolean equals(final Object o) {

        final boolean equal;

        if (o == this) {
            equal = true;
        } else if (o instanceof final WebContext webContext) {
            equal = this.host.equals(webContext.host);
        } else {
            equal = false;
        }

        return equal;
    }
}
