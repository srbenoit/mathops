package dev.mathops.db.cfg;

/**
 * A "code-context" object from the database configuration file.
 */
public final class CodeContext {

    /** The context ID. */
    public final String id;

    /** The profile. */
    public final Profile profile;

    /**
     * Constructs a new {@code CodeContext}.
     *
     * @param theId      the context ID
     * @param theProfile the profile
     * @throws IllegalArgumentException if the context ID or profile is null
     */
    CodeContext(final String theId, final Profile theProfile) {

        if (theId == null || theId.isBlank() || theProfile == null) {
            final String msg = Res.get(Res.CODE_CONTEXT_NULL_ID_PROFILE);
            throw new IllegalArgumentException(msg);
        }

        this.id = theId;
        this.profile = theProfile;
    }
}
