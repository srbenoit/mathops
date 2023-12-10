package dev.mathops.db;

/**
 * A base class for generalized connections to the database.
 */
public abstract class AbstractGeneralConnection {

    /**
     * Constructs a new {@code AbstractConnection}.
     */
    protected AbstractGeneralConnection() {

        // No action
    }

    /**
     * Gets the database product name.  For a JDBC connection, this can come from database metadata.
     *
     * @return the database product name
     */
    public abstract String getDatabaseProductName();
}
