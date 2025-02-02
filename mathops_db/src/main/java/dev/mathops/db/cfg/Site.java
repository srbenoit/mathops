package dev.mathops.db.cfg;

/**
 * A "site" object from the database configuration file.
 */
public final class Site {

    /** The site path. */
    public final String path;

    /** The profile. */
    public final Profile profile;

    /**
     * Constructs a new {@code Site}.
     *
     * @param thePath    the site path
     * @param theProfile the profile
     * @throws IllegalArgumentException if the path or profile is null
     */
    Site(final String thePath, final Profile theProfile) {

        if (thePath == null || thePath.isBlank() || theProfile == null) {
            final String msg = Res.get(Res.SITE_NULL_PATH_PROFILE);
            throw new IllegalArgumentException(msg);
        }

        this.path = thePath;
        this.profile = theProfile;
    }
}
