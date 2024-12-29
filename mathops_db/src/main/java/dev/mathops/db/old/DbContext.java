package dev.mathops.db.old;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.SchemaConfig;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A schema and login configuration used to connect to a database, as well as a map from context to DbConnection for all
 * contexts that use this driver and schema.
 */
public final class DbContext {

    /** Number of pooled connections before warning issued. */
    private static final int POOL_WARN_SIZE = 30;

    /** Delay when checking out if no connections are checked in. */
    private static final long CHECKOUT_SLEEP = 100L;

    /** Commonly used string. */
    private static final String SLASH = CoreConstants.SLASH;

    /** The schema. */
    public final SchemaConfig schema;

    /** The login configuration. */
    public final LoginConfig loginConfig;

    /** Object on which to synchronize member access. */
    private final Object synch;

    /** The pool of database connections currently available. */
    private final Queue<DbConnection> available;

    /** The pool of database connections currently checked out. */
    private final Queue<DbConnection> checkedOut;

    /**
     * Constructs a new {@code DbContext}.
     *
     * @param theSchema      the schema
     * @param theLoginConfig the login configuration
     */
    public DbContext(final SchemaConfig theSchema, final LoginConfig theLoginConfig) {

        super();

        if (theSchema == null) {
            throw new IllegalArgumentException(Res.get(Res.DB_CTX_NULL_SCHEMA));
        }
        if (theLoginConfig == null) {
            throw new IllegalArgumentException(Res.get(Res.DB_CTX_NULL_DRIVER));
        }

        this.synch = new Object();

        this.schema = theSchema;
        this.loginConfig = theLoginConfig;
        this.available = new LinkedList<>();
        this.checkedOut = new LinkedList<>();
    }

    /**
     * Gets the schema configuration.
     *
     * @return the schema configuration
     */
    public SchemaConfig getSchema() {

        return this.schema;
    }

    /**
     * Gets the login configuration.
     *
     * @return the login configuration
     */
    public LoginConfig getLoginConfig() {

        return this.loginConfig;
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

        // If all connections are checked out pause a few milliseconds to see if one is checked in
        // (mitigates situation where a malicious connection sends requests very fast)
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
//                conn.setFactory(this.schema.getBuilder());
            } else {
                conn = this.available.poll();
            }
            this.checkedOut.add(conn);

            if (this.checkedOut.size() == POOL_WARN_SIZE) {
                Log.warning(Res.fmt(Res.DB_CTX_MANY_CONNECTIONS, this.schema.id,
                        this.loginConfig.id, Integer.toString(this.checkedOut.size())));
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
                Log.warning(Res.get(Res.DB_CTX_NOT_CHECKED_IN),
                        new IllegalStateException("Connection checked in that was not checked out"));
            }

            this.available.add(conn);
        }
    }

    /**
     * Tests whether this {@code DbContext} is equal to another object. To be equal, the other object must be a
     * {@code DbContext} and must represent the same host name and path.
     *
     * @param obj the object against which to test for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final DbContext test) {
            equal = test.schema.equals(this.schema) && test.loginConfig.equals(this.loginConfig);
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

        return this.schema.hashCode() | this.loginConfig.hashCode();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code.
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add(this.loginConfig.id, SLASH, this.schema.id);

        return builder.toString();
    }
}
