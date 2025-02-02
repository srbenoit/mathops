package dev.mathops.db.old.cfg;

import dev.mathops.commons.CoreConstants;

/**
 * Represents a website (define db by host and path), with a selected database profile.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;web host='...'&gt;
 *   &lt;site path='...' profile='...'&gt;
 * &lt;/profile&gt;
 * </pre>
 */
@Deprecated
public final class WebSiteProfile {

    /** The host name. */
    public final String host;

    /** The path. */
    public final String path;

    /** The database profile. */
    public final DbProfile dbProfile;

    /**
     * Constructs a new {@code WebSiteProfile}.
     *
     * @param theHost    the host
     * @param thePath    the path
     * @param theProfile the database profile
     * @throws IllegalArgumentException if any argument is null
     */
    WebSiteProfile(final String theHost, final String thePath, final DbProfile theProfile) {

        if (theHost == null) {
            throw new IllegalArgumentException("Host may not be null");
        }
        if (thePath == null) {
            throw new IllegalArgumentException("Path may not be null");
        }
        if (theProfile == null) {
            throw new IllegalArgumentException("Profile may not be null");
        }

        this.host = theHost;
        this.path = thePath;
        this.dbProfile = theProfile;
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

        if (obj instanceof final WebSiteProfile test) {
            equal = test.host.equals(this.host) && test.path.equals(this.path)
                    && test.dbProfile.equals(this.dbProfile);
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

        return this.host.hashCode() + this.path.hashCode() + this.dbProfile.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.host + CoreConstants.SLASH + this.path + " (" + this.dbProfile.id + ")";
    }
}
