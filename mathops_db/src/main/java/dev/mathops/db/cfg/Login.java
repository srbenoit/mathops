package dev.mathops.db.cfg;

import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A "login" object from the database configuration file.
 */
public final class Login {

    /** Number of pooled connections before warning issued. */
    private static final int POOL_WARN_SIZE = 30;

    /** Delay when checking out if no connections are checked in. */
    private static final long CHECKOUT_SLEEP = 100L;

    /** The database that owns the login. */
    public final Database database;

    /** The login ID. */
    public final String id;

    /** The username. */
    public final String user;

    /** The password (mutable - user can enter if not initially provided). */
    public String password;

    /** Object on which to synchronize member access. */
    private final Object synch;

    /** The pool of database connections currently available. */
    private final Queue<DbConnection> available;

    /** The pool of database connections currently checked out. */
    private final Queue<DbConnection> checkedOut;

    /**
     * Constructs a new {@code Login}.
     *
     * @param theDatabase the database that owns the login
     * @param theId       the login ID
     * @param theUser     the username
     * @param thePassword the password
     * @throws IllegalArgumentException if the ID or username is null
     */
    public Login(final Database theDatabase, final String theId, final String theUser, final String thePassword) {

        if (theDatabase == null || theId == null || theId.isBlank() || theUser == null) {
            final String msg = Res.get(Res.LOGIN_NULL_DB_ID_USER);
            throw new IllegalArgumentException(msg);
        }

        this.database = theDatabase;
        this.id = theId;
        this.user = theUser;
        this.password = thePassword;

        this.synch = new Object();
        this.available = new LinkedList<>();
        this.checkedOut = new LinkedList<>();
    }

    /**
     * Creates a new login with all the settings of this object but with a new username and password.  This is used when
     * the username and/or password are entered at runtime by the user.
     *
     * @param theUser     the username
     * @param thePassword the password
     * @return the new login
     */
    public Login newLogin(final String theUser, final String thePassword) {

        return new Login(this.database, this.id, theUser, thePassword);
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

    /**
     * Checks out a database connection, creating a new one if there are none available.
     *
     * @return the connection
     */
    public DbConnection checkOutConnection() {

        final DbConnection conn;
        final boolean empty;

        synchronized (this.synch) {
            empty = this.available.isEmpty();
        }

        // If all connections are checked out pause a few milliseconds to see if one is checked in (mitigates situation
        // where a malicious connection sends requests very fast)
        if (empty) {
            try {
                Thread.sleep(CHECKOUT_SLEEP);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized (this.synch) {
            if (this.available.isEmpty()) {
                conn = new DbConnection(this);
            } else {
                conn = this.available.poll();
            }
            this.checkedOut.add(conn);

            final int numCheckedOut = this.checkedOut.size();
            if (numCheckedOut == POOL_WARN_SIZE) {
                final String numCheckedOutStr = Integer.toString(numCheckedOut);
                final String msg = Res.fmt(Res.LOGIN_MANY_CONNECTIONS, this.id, numCheckedOutStr);
                Log.warning(msg);
            }
        }

        return conn;
    }

    /**
     * Checks a database connection back in.
     *
     * @param conn the connection to check in
     */
    public void checkInConnection(final DbConnection conn) {

        synchronized (this.synch) {
            if (!this.checkedOut.remove(conn)) {
                final String msg = Res.get(Res.LOGIN_NOT_CHECKED_IN);
                // Include an exception so we get a stack trace to find where this occurred.
                Log.warning(new IllegalStateException(msg));
            }

            this.available.add(conn);
        }
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.database.hashCode() + this.id.hashCode() + this.user.hashCode();
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
        } else if (o instanceof final Login login) {
            equal = this.id.equals(login.id) && this.user.equals(login.user) && this.database.equals(login.database);
        } else {
            equal = false;
        }

        return equal;
    }
}
