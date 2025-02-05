package dev.mathops.db.cfg;

/**
 * A "site" object from the database configuration file.
 */
public final class Site {

    /** The owning web context. */
    public final WebContext owner;

    /** The site path. */
    public final String path;

    /** The profile. */
    public final Profile profile;

    /**
     * Constructs a new {@code Site}.
     *
     * @param theOwner   the owning web context
     * @param thePath    the site path
     * @param theProfile the profile
     * @throws IllegalArgumentException if the path or profile is null
     */
    Site(final WebContext theOwner, final String thePath, final Profile theProfile) {

        if (theOwner == null || thePath == null || thePath.isBlank() || theProfile == null) {
            final String msg = Res.get(Res.SITE_NULL_OWNER_PATH_PROFILE);
            throw new IllegalArgumentException(msg);
        }

        this.owner = theOwner;
        this.path = thePath;
        this.profile = theProfile;
    }

    /**
     * Gets the hostname for the site.
     *
     * @return the hostname
     */
    public String getHost() {

        return this.owner.host;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.owner.hashCode() + this.path.hashCode() + this.profile.hashCode();
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
        } else if (o instanceof final Site site) {
            equal = this.path.equals(site.path) && this.owner.equals(site.owner) && this.profile.equals(site.profile);
        } else {
            equal = false;
        }

        return equal;
    }
}
