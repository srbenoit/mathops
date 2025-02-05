package dev.mathops.db;

import dev.mathops.commons.log.Log;
import dev.mathops.db.cfg.Login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A database connection definition, consisting of a {@code DriverConfig} object that can open and close JDBC
 * connections to the underlying database, a {@code Schema} object that has a factory to create implementations for each
 * model type, and a JDBC connection with a timeout that represents the currently active connection.
 * <p>
 * This object will be used by multiple threads, in general, but its methods are not synchronized, since a client
 * program will typically need to call several methods on this object, and it maintains state (a JDBC connection).
 * Therefore, all clients that use this class should perform atomic database operations within synchronized blocks. This
 * implies that {@code getConnection} should be used on entry to the synchronized block. For example:
 *
 * <pre>
 * DbConnection dbConn = ContextMap.getInstance().getConnection(&quot;myContext&quot;);
 * synchronized (dbConn) {
 *     Connection c = dbConn.getConnection();
 *
 *     try (Statement s = c.createStatement();
 *          ResultSet rs = s.executeQuery(&quot;SELECT * FROM foo&quot;)) {
 *
 *         while (rs.next()) {
 *             constructModelFromResultSet(r);
 *         }
 *     } catch (DbException e) {
 *         // Handle the exception
 *     } finally {
 *         dbConn.releaseConnection();
 *     }
 * }
 * </pre>
 */
public final class DbConnection {

    /** Object on which to synchronize registration of drivers. */
    private static final Object REGISTER_SYNCH = new Object();

    /** Flag indicating drivers have been registered. */
    private static boolean unregistered = true;

    /** The login this connection uses. */
    final Login login;

    /** The currently active JDBC connection. */
    private Connection jdbcConnection;

    /**
     * Constructs a new {@code DbConnection}.
     *
     * @param theLogin the login context this connection uses
     */
    public DbConnection(final Login theLogin) {

        this.login = theLogin;
    }

    /**
     * Gets the type of database product to which this object is connected.
     *
     * @return the database product
     */
    public EDbProduct getProduct() {

        return this.login.database.server.type;
    }

    /**
     * Registers the JDBC drivers needed by the database package.
     */
    public static void registerDrivers() {

        synchronized (REGISTER_SYNCH) {
            if (unregistered) {
                try {
                    final String msg = Res.get(Res.DB_CONN_REG_IFX);
                    Log.info(msg);
                    Class.forName("com.informix.jdbc.IfxDriver");
                } catch (final ClassNotFoundException ex) {
                    final String msg2 = Res.get(Res.DB_CONN_REG_IFX_FAIL);
                    Log.warning(msg2, ex);
                }

                try {
                    final String msg = Res.get(Res.DB_CONN_REG_ORA);
                    Log.info(msg);
                    Class.forName("oracle.jdbc.OracleDriver");
                } catch (final ClassNotFoundException ex) {
                    final String msg2 = Res.get(Res.DB_CONN_REG_ORA_FAIL);
                    Log.warning(msg2, ex);
                }

                try {
                    final String msg = Res.get(Res.DB_CONN_REG_PG);
                    Log.info(msg);
                    Class.forName("org.postgresql.Driver");
                } catch (final ClassNotFoundException ex) {
                    final String msg2 = Res.get(Res.DB_CONN_REG_PG_FAIL);
                    Log.warning(msg2, ex);
                }

                unregistered = false;
            }
        }
    }

    /**
     * Gets the currently active JDBC connection, creating a new connection to the database if the active connection has
     * not yet been created or has been closed or has timed out. This action resets the timeout on the connection.
     *
     * @return the {@code Connection}
     * @throws SQLException if there is an error connecting to the server
     */
    public Connection getConnection() throws SQLException {

        if (this.jdbcConnection == null || this.jdbcConnection.isClosed()) {

            this.jdbcConnection = this.login.openConnection();
            this.jdbcConnection.setAutoCommit(false);
        }

        return this.jdbcConnection;
    }

    /**
     * Creates a statement. If the JDBC connection is not yet open, this method opens it.
     *
     * @return the prepared statement
     * @throws SQLException if the statement could not be prepared
     */
    public Statement createStatement() throws SQLException {

        final Connection connection = getConnection();
        return connection.createStatement();
    }

    /**
     * Prepares a statement. If the JDBC connection is not yet open, this method opens it.
     *
     * <p>
     * This method may (at some future time) cache prepared statements, keyed on the SQL string. When that happens,
     * calls to this method should NOT be managed with "try-with-resource" blocks that close the prepared statements.
     *
     * @param sql the statement SQL
     * @return the prepared statement
     * @throws SQLException if the statement could not be prepared
     */
    public PreparedStatement prepareStatement(final String sql)
            throws SQLException {

        final Connection connection = getConnection();
        return connection.prepareStatement(sql);
    }

    /**
     * Commits the transaction.
     *
     * @throws SQLException if the commit failed
     */
    public void commit() throws SQLException {

        final Connection connection = getConnection();
        connection.commit();
    }

    /**
     * Rolls back the transaction.
     *
     * @throws SQLException if the commit failed
     */
    public void rollback() throws SQLException {

        final Connection connection = getConnection();
        connection.rollback();
    }
}
