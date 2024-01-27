package dev.mathops.db.config;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable representation of a web context (as specified by a web server host), which defines a list of sites
 * within that web context.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;web-context host='...'&gt;
 *   ... zero or more &lt;site&gt; child elements ...
 * &lt;/web&gt;
 * </pre>
 */
public final class CfgWebContext implements Comparable<CfgWebContext> {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "web-context";

    /** The ID attribute. */
    private static final String HOST_ATTR = "host";

    /** An empty array used when converting collections of sites to arrays. */
    private static final CfgSite[] EMPTY_SITE_ARRAY = new CfgSite[0];

    /** The web server host. */
    public final String host;

    /** A map from site path to site configuration. */
    private final Map<String, CfgSite> sites;

    /**
     * Constructs a new {@code WebContext}.
     *
     * @param theHost  the host
     * @param theSites a map from site path name to site configuration
     * @throws IllegalArgumentException if any argument is null
     */
    public CfgWebContext(final String theHost, final Collection<CfgSite> theSites) {

        if (theHost == null || theHost.isBlank()) {
            throw new IllegalArgumentException("Host may not be null or blank");
        }

        this.host = theHost;

        if (theSites == null || theSites.isEmpty()) {
            this.sites = new HashMap<>(0);
        } else {
            final int count = theSites.size();
            this.sites = new HashMap<>(count);
            for (final CfgSite site : theSites) {
                if (site != null) {
                    if (this.sites.containsKey(site.path)) {
                        throw new IllegalArgumentException("Duplicate path in child sites collection");
                    }
                    this.sites.put(site.path, site);
                }
            }
        }
    }

    /**
     * Constructs a new {@code WebContext} from its XML representation.
     *
     * @param theProfileMap a map from string data profile ID to data profile
     * @param theElem       the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    CfgWebContext(final Map<String, CfgDataProfile> theProfileMap, final NonemptyElement theElem)
            throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {
            this.host = theElem.getRequiredStringAttr(HOST_ATTR);
            if (this.host.isBlank()) {
                throw new IllegalArgumentException("Host may not be blank");
            }

            final List<IElement> childElements = theElem.getElementChildrenAsList();
            final int count = childElements.size();
            this.sites = new HashMap<>(count);

            for (final IElement child : childElements) {
                final String childTag = child.getTagName();

                if (child instanceof final EmptyElement childElem) {
                    if (CfgSite.ELEM_TAG.equals(childTag)) {
                        final CfgSite site = new CfgSite(theProfileMap, childElem);
                        if (this.sites.containsKey(site.path)) {
                            throw new IllegalArgumentException("Duplicate path in child sites collection");
                        }
                        this.sites.put(site.path, site);
                    } else {
                        Log.warning("Unexpected <", childTag, "> child of <web-context> element.");
                    }
                } else {
                    Log.warning("Unexpected <", childTag, "> child of <web-context> element.");
                }
            }
        } else {
            final String msg = Res.get(Res.PROF_CFG_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
    }

    /**
     * Gets of schema logins in the schema login map.
     *
     * @return the array of schema login configurations
     */
    public CfgSite[] getSites() {

        return this.sites.values().toArray(EMPTY_SITE_ARRAY);
    }

    /**
     * Gets the site configuration for a specified path.
     *
     * @param path the path
     * @return the site configuration; {@code null} if none is configured for the specified path)
     */
    public CfgSite getSite(final String path) {

        return this.sites.get(path);
    }

    /**
     * Tests whether this {@code ServerConfig} is equal to another object. To be equal, the other object must be a
     * {@code ServerConfig} and must have the same type, host, port, and name.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final CfgWebContext test) {
            equal = test.host.equals(this.host) && test.sites.equals(this.sites);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {

        return this.host.hashCode() + this.sites.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("WebContext{host='", this.host, "', sites=[", this.sites, "]}");
    }

    /**
     * Compares this web context to another for order. Order is based on host.
     *
     * @param o the other web context to which to compare
     */
    @Override
    public int compareTo(final CfgWebContext o) {

        return this.host.compareTo(o.host);
    }
}
