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

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.id.hashCode() + this.profile.hashCode();
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
        } else if (o instanceof final CodeContext codeContext) {
            equal = this.id.equals(codeContext.id) && this.profile.equals(codeContext.profile);
        } else {
            equal = false;
        }

        return equal;
    }
}
