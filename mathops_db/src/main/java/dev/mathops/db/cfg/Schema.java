package dev.mathops.db.cfg;

/**
 * A "schema" object from the database configuration file.
 */
public final class Schema {

    /** The data object. */
    public final Data data;

    /** The login object. */
    public final Login login;

    /**
     * Constructs a new {@code Schema}.
     *
     * @param theData  the login ID
     * @param theLogin the username
     * @throws IllegalArgumentException if the data or login ID is null
     */
    Schema(final Data theData, final Login theLogin) {

        if (theData == null || theLogin == null) {
            final String msg = Res.get(Res.SCHEMA_NULL_DATA_LOGIN);
            throw new IllegalArgumentException(msg);
        }

        this.data = theData;
        this.login = theLogin;
    }
}

