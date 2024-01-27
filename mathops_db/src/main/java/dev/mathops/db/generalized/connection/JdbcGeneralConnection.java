package dev.mathops.db.generalized.connection;

import dev.mathops.commons.log.Log;
import dev.mathops.db.config.EDbUse;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A base class for generalized connections to the database.
 */
public final class JdbcGeneralConnection extends AbstractGeneralConnection {

    /** The encapsulated JDBC connection. */
    private final Connection connection;

    /**
     * Constructs a new {@code JdbcGeneralConnection}.
     *
     * @param theContext the connection's context
     * @param theConnection the underlying JDBC connection
     */
    public JdbcGeneralConnection(final EDbUse theContext, final Connection theConnection) {

        super(theContext);

        if (theConnection == null) {
            throw new IllegalArgumentException("JDBC connection may not be null");
        }

        this.connection = theConnection;
    }

    /**
     * Gets the database product name.  For a JDBC connection, this can come from database metadata.
     *
     * @return the database product name
     */
    public String getDatabaseProductName() {

        String result;

        try {
            result = this.connection.getMetaData().getDatabaseProductName();
        } catch (final SQLException ex) {
            Log.warning(ex);
            result = "UNKNOWN";
        }

        return result;
    }

    /**
     * Gets the JDBC connection.
     *
     * @return the JDBC connection
     */
    public Connection getConnection() {

        return this.connection;
    }
}
