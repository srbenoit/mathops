package dev.mathops.db.generalized.connection;

/**
 * A base class for generalized connections to the database.
 */
public abstract class AbstractGeneralConnection {

    /** The connection's context. */
    private final EConnectionContext context;

    /**
     * Constructs a new {@code AbstractConnection}.
     *
     * @param theContext the connection's context
     */
    protected AbstractGeneralConnection(final EConnectionContext theContext) {

        if (theContext == null) {
            throw new IllegalArgumentException("Connection context may not be null");
        }

        this.context = theContext;
    }

    /**
     * Gets the connection's context.
     *
     * @return the context
     */
    public final EConnectionContext getContext() {

        return this.context;
    }

    /**
     * Gets the database product name.  For a JDBC connection, this can come from database metadata.
     *
     * @return the database product name
     */
    public abstract String getDatabaseProductName();
}
