package dev.mathops.db.cfg;

/**
 * A "facet" object from the database configuration file.  A "facet" configures the login to use to access some schema
 * within the profile.
 */
public final class Facet {

    /** The data object. */
    public final Data data;

    /** The login object. */
    public final Login login;

    /**
     * Constructs a new {@code Facet}.
     *
     * @param theData  the login ID
     * @param theLogin the username
     * @throws IllegalArgumentException if the data or login ID is null
     */
    public Facet(final Data theData, final Login theLogin) {

        if (theData == null || theLogin == null) {
            final String msg = Res.get(Res.SCHEMA_NULL_DATA_LOGIN);
            throw new IllegalArgumentException(msg);
        }

        this.data = theData;
        this.login = theLogin;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.data.hashCode() + this.login.hashCode();
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
        } else if (o instanceof final Facet facet) {
            equal = this.data.equals(facet.data) && this.login.equals(facet.login);
        } else {
            equal = false;
        }

        return equal;
    }
}

