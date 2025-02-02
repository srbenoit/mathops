package dev.mathops.db.cfg;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A "login" object from the database configuration file.
 */
public final class Login {

    /** The database that owns the login. */
    public final Database database;

    /** The login ID. */
    public final String id;

    /** The username. */
    public final String user;

    /** The password (mutable - user can enter if not initially provided). */
    public String password;

    /**
     * Constructs a new {@code Login}.
     *
     * @param theDatabase the database that owns the login
     * @param theId       the login ID
     * @param theUser     the username
     * @param thePassword the password
     * @throws IllegalArgumentException if the ID or username is null
     */
    Login(final Database theDatabase, final String theId, final String theUser, final String thePassword) {

        if (theDatabase == null || theId == null || theId.isBlank() || theUser == null) {
            final String msg = Res.get(Res.LOGIN_NULL_DB_ID_USER);
            throw new IllegalArgumentException(msg);
        }

        this.database = theDatabase;
        this.id = theId;
        this.user = theUser;
        this.password = thePassword;
    }

    /**
     * Sets the password for subsequent connections to the database.
     *
     * @param thePassword the password
     */
    public void setPassword(final String thePassword) {

        this.password = thePassword;
    }

    /**
     * Creates a new JDBC connection using this configuration.
     *
     * @param thePassword the password for this connection (not stored by this call)
     * @return the new connection
     * @throws SQLException if the connection could not be opened
     */
    public Connection openConnection(final String thePassword) throws SQLException {

        return this.database.openConnection(this.user, thePassword);
    }

    /**
     * Creates a new JDBC connection using this configuration.
     *
     * @return the new connection
     * @throws SQLException if the connection could not be opened
     */
    public Connection openConnection() throws SQLException {

        return this.database.openConnection(this.user, this.password);
    }
}
