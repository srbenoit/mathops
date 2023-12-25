package dev.mathops.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;

import java.util.Map;

/**
 * An immutable representation of a site (as specified by an absolute path) within a web context, which specifies the
 * data profile to use for that site.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;site path='...' profile='...'/&gt;
 * </pre>
 */
public final class CfgSite implements Comparable<CfgSite> {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "site";

    /** The ID attribute. */
    private static final String PATH_ATTR = "path";

    /** The ID attribute. */
    private static final String PROFILE_ATTR = "profile";

    /** The path */
    public final String path;

    /** The data profile. */
    public final CfgDataProfile profile;

    /**
     * Constructs a new {@code Site}.
     *
     * @param thePath    the path
     * @param theProfile a data profile
     * @throws IllegalArgumentException if either argument is null
     */
    public CfgSite(final String thePath, final CfgDataProfile theProfile) {

        if (thePath == null || thePath.isBlank()) {
            throw new IllegalArgumentException("Path may not be null or blank");
        }
        if (theProfile == null) {
            throw new IllegalArgumentException("Data profile may not be null");
        }

        this.path = thePath;
        this.profile = theProfile;
    }


    /**
     * Constructs a new {@code Site} from its XML representation.
     *
     * @param theProfileMap a map from string data profile ID to data profile
     * @param theElem       the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    CfgSite(final Map<String, CfgDataProfile> theProfileMap, final EmptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {
            this.path = theElem.getRequiredStringAttr(PATH_ATTR);
            if (this.path.isBlank()) {
                throw new IllegalArgumentException("Path may not be blank");
            }

            final String profileId = theElem.getRequiredStringAttr(PROFILE_ATTR);

            this.profile = theProfileMap.get(profileId);
            if (this.profile == null) {
                final String msg = Res.fmt(Res.SITE_BAD_PROFILE, profileId, this.path);
                throw new ParsingException(theElem, msg);
            }
        } else {
            final String msg = Res.get(Res.SITE_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
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

        if (obj instanceof final CfgSite test) {
            equal = test.path.equals(this.path) && test.profile.equals(this.profile);
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

        return this.path.hashCode() + this.profile.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("Site{path=", this.path, ",profile=", this.profile.id, "}");
    }

    /**
     * Compares this site to another for order. Order is based on path.
     *
     * @param o the other site to which to compare
     */
    @Override
    public int compareTo(final CfgSite o) {

        return this.path.compareTo(o.path);
    }
}
