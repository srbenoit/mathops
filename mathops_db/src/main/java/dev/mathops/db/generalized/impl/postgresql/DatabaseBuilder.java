package dev.mathops.db.generalized.impl.postgresql;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.DbaLoginConfig;
import dev.mathops.db.generalized.connection.AbstractGeneralConnection;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.generalized.connection.JdbcGeneralConnection;
import dev.mathops.db.config.LoginConfig;
import dev.mathops.db.config.ServerConfig;
import dev.mathops.db.generalized.impl.DatabaseValidationResult;
import dev.mathops.db.generalized.impl.EValidationStatus;
import dev.mathops.db.tables.primary.AllTables;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class that uses a DBA profile and login to connect to a PostgreSQL database, then verifies that all schemas and
 * tables needed by a collection of table definitions are present and correct.  If any database objects are missing or
 * incorrect, this object attempts to build / repair the database.
 */
public class DatabaseBuilder {

    /** The server configuration, which should have a DBA login conmfigured. */
    private final ServerConfig server;

    /** The DBA password. */
    private final String dbaPassword;

    /** The collection of all tables in the primary schema. */
    private final AllTables allTables;

    /**
     * Constructs a new {@code DatabaseBuilder}.
     *
     * @param theServer      the server configuration
     * @param theAllTables   the list of all tables in the primary schema
     * @param theDbaPassword the DBA password
     */
    public DatabaseBuilder(final ServerConfig theServer, final AllTables theAllTables,
                           final String theDbaPassword) {

        if (theServer == null) {
            throw new IllegalArgumentException("Server may not be null");
        }
        if (theAllTables == null) {
            throw new IllegalArgumentException("Tables list may not be null");
        }
        if (theDbaPassword == null) {
            throw new IllegalArgumentException("DBA password may not be null");
        }

        if (theServer.dbaLogin == null) {
            throw new IllegalArgumentException("Server has no DBA login configured.");
        }

        if (theServer.type != EDbInstallationType.POSTGRESQL) {
            throw new IllegalArgumentException("Server specified by DBA profile login is not a PostgreSQL server");
        }

        this.server = theServer;
        this.allTables = theAllTables;
        this.dbaPassword = theDbaPassword;
    }

    /**
     * Attempts to validate a database.  This involves connecting to a PostgreSQL server using a DBA login profile,
     * then verifying that all schemas and tables needed are present and correct.  If any database objects are missing
     * or incorrect, an attempt is made to build / repair those objects.
     *
     * <p>
     * This method is idempotent: executing multiple times should have the same effect as executing once, unless some
     * underlying database object was externally modified between executions.
     *
     * @return the results of validation
     */
    public final DatabaseValidationResult validateDatabase() {

        final List<String> errors = new ArrayList<>(10);
        final List<String> actionsTaken = new ArrayList<>(10);

        final DbaLoginConfig dbaLogin = this.server.dbaLogin;

        try {
            final AbstractGeneralConnection conn = dbaLogin.openConnection(this.server, this.dbaPassword);
            if (conn instanceof final JdbcGeneralConnection jdbcConn) {
                validateDatabaseJdbc(jdbcConn, errors, actionsTaken);
            } else {
                errors.add("Connection to PostgreSQL server did not generate JDBC connection");
            }
        } catch (final SQLException ex) {
            errors.add(SimpleBuilder.concat("Failed to connect to PostgreSQL server using DBA login: ",
                    ex.getLocalizedMessage()));
        }

        final EValidationStatus status = errors.isEmpty() ? EValidationStatus.VALID : EValidationStatus.INVALID;

        return new DatabaseValidationResult(status, errors, actionsTaken);
    }

    /**
     * Attempts to validate the database given a JDBC connection.
     *
     * @param conn the JDBC connection
     * @param errors a list to which to add errors
     */
    private void validateDatabaseJdbc(final JdbcGeneralConnection conn, final List<String> errors,
                                      final List<String> actionsTaken) {

        if (validateRoles(conn, errors, actionsTaken) && validateMathDatabase(conn, errors, actionsTaken)) {

        }
    }

    /**
     * Attempts to validate that all roles configured for the server attached to the DBA login exist.
     *
     * @param conn         the JDBC connection
     * @param errors       a list to which to add errors
     * @param actionsTaken a list of actions this method took to update the database
     * @return true if all requires roles exist at the end of this method's execution
     */
    private boolean validateRoles(final JdbcGeneralConnection conn, final List<String> errors,
                                  final List<String> actionsTaken) {

        final Connection jdbc = conn.getConnection();
        boolean ok = true;

        // Query for all roles defined
        final Collection<String> allExistingRoles = new ArrayList<>(10);
        final String sql = "SELECT rolename FROM pg_roles;";

        try (final Statement stmt = jdbc.createStatement()) {

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String name = rs.getString("rolename");
                    allExistingRoles.add(name);
                }
            } catch (final SQLException ex) {
                errors.add(SimpleBuilder.concat("Failed to query for existing roles: ", ex.getLocalizedMessage()));
                ok = false;
            }

            if (ok) {
                // Determine all roles needed
                final List<LoginConfig> logins = this.server.getLogins();

                for (final LoginConfig login : logins) {
                    if (!allExistingRoles.contains(login.user)) {
                        final String sql2 = SimpleBuilder.concat("CREATE ROLE ", login.user,
                                "LOGIN CREATEDB CREATEROLE PASSWORD '", login.password, "'");
                        try{
                            final int numRows = stmt.executeUpdate(sql2);
                            if (numRows == 1) {
                                actionsTaken.add(SimpleBuilder.concat("Created '", login.user, "' role."));
                            } else {
                                errors.add(SimpleBuilder.concat("Failed to create '", login.user, "' role."));
                            }
                        } catch (final SQLException ex) {
                            errors.add(SimpleBuilder.concat("Failed to create '", login.user, "' role: ",
                                    ex.getLocalizedMessage()));
                            ok = false;
                        }
                    }
                }
            }
        } catch (final SQLException ex) {
            errors.add(SimpleBuilder.concat("Failed to create database statement: ", ex.getLocalizedMessage()));
            ok = false;
        }

        return ok;
    }

    /**
     * Attempts to validate that the "math" database exists, creating it if not.
     *
     * @param conn         the JDBC connection
     * @param errors       a list to which to add errors
     * @param actionsTaken a list of actions this method took to update the database
     * @return true if the database exists after this method executes
     */
    private boolean validateMathDatabase(final JdbcGeneralConnection conn, final Collection<? super String> errors,
                                         final Collection<? super String> actionsTaken) {

        final Connection jdbc = conn.getConnection();
        boolean ok = true;

        boolean tryToCreate = false;

        try {
            final DatabaseMetaData data = jdbc.getMetaData();

            try (final ResultSet rs = data.getCatalogs()) {
                tryToCreate = true;
                while (rs.next()) {
                    final String catalogName = rs.getString("TABLE_CAT");
                    if ("math".equals(catalogName)) {
                        tryToCreate = false;
                        break;
                    }
                }
            }
        } catch (final SQLException ex) {
            errors.add(SimpleBuilder.concat("Failed to retrieve database catalogs: ", ex.getLocalizedMessage()));
            ok = false;
        }

        if (ok && tryToCreate) {
            final String sql = "CREATE DATABASE math OWNER math";

            try (final Statement stmt = jdbc.createStatement()) {
                try{
                    final int numRows = stmt.executeUpdate(sql);
                    if (numRows == 1) {
                        actionsTaken.add("Created 'math' database.");
                    } else {
                        errors.add("Failed to create 'math' database.");
                    }
                } catch (final SQLException ex) {
                    errors.add(SimpleBuilder.concat("Failed to create 'math' database: ", ex.getLocalizedMessage()));
                    ok = false;
                }
            } catch (final SQLException ex) {
                errors.add(SimpleBuilder.concat("Failed to create database statement: ", ex.getLocalizedMessage()));
                ok = false;
            }
        }

        return ok;
    }
}
