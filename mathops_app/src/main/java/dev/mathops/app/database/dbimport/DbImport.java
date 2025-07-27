package dev.mathops.app.database.dbimport;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.EDbUse;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Server;
import dev.mathops.text.builder.SimpleBuilder;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The database import application.
 */
public final class DbImport implements Runnable {

    /** The export directory from which to load data from Informix. */
    private File exportDir = null;

    /** The data loaded from the import directory. */
    private DataToImport data = null;

    /** Constructs a new {@code DbImport}. */
    private DbImport() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        this.exportDir = chooseExportDirectory();
        if (Objects.nonNull(this.exportDir)) {

            try {
                this.data = new DataToImport(this.exportDir);

                // This opens a dialog which (if the user activates [Ok]) will call {@code databaseSelected} to
                // actually create the database
                chooseTargetDatabase();
            } catch (final IllegalArgumentException ex) {
                Log.warning(ex);
                final String msg = ex.getMessage();
                JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Asks the user to select the export directory from which to import.
     *
     * @return the selected directory; {@code null} if the user chose "Cancel"
     */
    private static File chooseExportDirectory() {

        File result = null;

        File dir = PathList.getInstance().get(EPath.DB_PATH);
        if (dir == null) {
            final String home = System.getProperty("user.home");
            dir = new File(home);
        }
        final JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogTitle("Select Informix export directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            result = chooser.getSelectedFile();
            Log.info("Selected path: ", result.getAbsolutePath());
        }

        return result;
    }

    /**
     * Displays a dialog that lets the user select the PostgreSQL database into which to import.
     */
    private void chooseTargetDatabase() {

        DbConnection.registerDrivers();

        final DatabaseConfig databaseConfig = DatabaseConfig.getDefault();

        final List<Server> servers = databaseConfig.getServers();

        final List<Server> pgServers = new ArrayList<>(3);
        for (final Server server : servers) {
            if (server.type == EDbProduct.POSTGRESQL) {
                pgServers.add(server);
            }
        }

        if (pgServers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "There are no PostgreSQL servers configured in db-config.xml.",
                    "Select PostgreSQL Database", JOptionPane.ERROR_MESSAGE);
        } else {
            final PgDatabasePicker picker = new PgDatabasePicker(pgServers, this);
            UIUtilities.packAndCenter(picker);
            picker.setVisible(true);
        }
    }

    /**
     * Called by the database chooser dialog when the user has made a selection of the database into which to import.
     *
     * @param login      the selected login under which to perform the import
     * @param schema     the schema into which to import
     * @param dropTables true to drop existing tables in the database before importing
     */
    void databaseSelected(final Login login, final EDbUse schema, final boolean dropTables) {

        final DbConnection conn = login.checkOutConnection();

        try {
            final Connection jdbc = conn.getConnection();

            Log.info("Connected to PostgreSQL.");

            final String schemaName = schema == EDbUse.PRODUCTION ? "legacy"
                    : (schema == EDbUse.DEVELOPMENT ? "legacy_dev" : "legacy_test");

            final boolean schemaEmpty;
            if (dropTables) {
                Log.info("Dropping all existing tables in '", schemaName, "' schema.");
                dropExistingTables(jdbc, schemaName);
                schemaEmpty = true;
            } else {
                Log.info("Checking whether the '", schema, "' schema is empty...");
                schemaEmpty = isSchemaEmpty(jdbc, schemaName);
            }

            if (schemaEmpty) {
                Log.info("Proceeding with import");
                performImport(jdbc, schemaName);
            } else {
                Log.info("Schema not empty - unable to import.");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String[] msg = {"Unable to connect to PostgreSQL database:", ex.getMessage()};
            JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
        } finally {
            login.checkInConnection(conn);
        }
    }

    /**
     * Drops all existing tables in the selected schema.
     *
     * @param conn       the database connection
     * @param schemaName the schema name
     * @return SUCCESS if all tables were dropped
     */
    private ESuccessFailure dropExistingTables(final Connection conn, final String schemaName) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        for (final String view : this.data.synonyms.keySet()) {
            final String dropSql = "DROP VIEW IF EXISTS " + schemaName + "." + view;

            try (final Statement statement = conn.createStatement()) {
                statement.executeUpdate(dropSql);
            } catch (final SQLException ex) {
                Log.warning(ex);
                final String[] msg = {"Unable to drop '" + view + "' view", ex.getMessage()};
                JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                result = ESuccessFailure.FAILURE;
                break;
            }
        }

        if (result == ESuccessFailure.SUCCESS) {

            for (final TableDefinition table : this.data.tables) {
                final String dropSql = "DROP TABLE IF EXISTS " + schemaName + "." + table.tableName;

                try (final Statement statement = conn.createStatement()) {
                    statement.executeUpdate(dropSql);
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    final String[] msg = {"Unable to drop '" + table.tableName + "' table", ex.getMessage()};
                    JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                    result = ESuccessFailure.FAILURE;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Performs the import.
     *
     * @param conn       the database connection
     * @param schemaName the schema name
     */
    private void performImport(final Connection conn, final String schemaName) {

        boolean ok = true;

        for (final TableDefinition table : this.data.tables) {
            final String createSql = table.makeCreateSql(schemaName);

            try (final Statement statement = conn.createStatement()) {
                statement.executeUpdate(createSql);
            } catch (final SQLException ex) {
                Log.warning(ex);
                final String[] msg = {"Unable to create '" + table.tableName + "' table", ex.getMessage()};
                JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                ok = false;
                break;
            }
        }

        if (ok) {
            for (final Map.Entry<String, String> entry : this.data.synonyms.entrySet()) {
                final String viewName = entry.getKey();
                final String tableName = entry.getValue();

                final String createSql = SimpleBuilder.concat("CREATE VIEW ", schemaName, ".", viewName,
                        " AS SELECT * FROM ", schemaName, ".", tableName, ";");

                try (final Statement statement = conn.createStatement()) {
                    statement.executeUpdate(createSql);
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    final String[] msg = {"Unable to create '" + viewName + "' view", ex.getMessage()};
                    JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                    break;
                }
            }
        }

        try {
            conn.setAutoCommit(false);
        } catch (final SQLException ex) {
            final String[] msg = {"Unable to turn off autocommit on connection ", ex.getMessage()};
            JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
            ok = false;
        }

        if (ok) {
            for (final TableDefinition table : this.data.tables) {

                try (final Statement statement = conn.createStatement()) {
                    statement.executeUpdate("ALTER TABLE " + schemaName + "." + table.tableName + " SET UNLOGGED");
                    conn.setAutoCommit(false);
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    final String[] msg = {"Unable to set '" + table.tableName + "' table to unlogged", ex.getMessage()};
                    JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                    break;
                }

                final String insertSql = table.makeInsertPreparedStatementSql(schemaName);

                try (final PreparedStatement statement = conn.prepareStatement(insertSql)) {

                    final int numRows = table.data.size();

                    Log.fine("Inserting " + numRows + " rows into '", table.tableName, "'...");

                    for (int i = 0; i < numRows; ++i) {
                        final Object[] values = table.data.get(i);
                        final int numFields = table.fields.size();

                        if (values.length == numFields) {

                            int index = 0;
                            for (final Object value : values) {
                                final FieldDefinition field = table.fields.get(index);
                                ++index;

                                if ("desc".equals(field.fieldName)) {
                                    continue;
                                }

                                switch (value) {
                                    case null -> statement.setNull(index, field.type);
                                    case final Integer integerValue -> {
                                        final int primitive = integerValue.intValue();
                                        statement.setInt(index, primitive);
                                    }
                                    case final Long longValue -> {
                                        final long primitive = longValue.longValue();
                                        statement.setLong(index, primitive);
                                    }
                                    case final Double doubleValue -> {
                                        final double primitive = doubleValue.doubleValue();
                                        statement.setDouble(index, primitive);
                                    }
                                    case final LocalDate dateValue -> {
                                        final Date sqlDate = Date.valueOf(dateValue);
                                        statement.setDate(index, sqlDate);
                                    }
                                    case final LocalDateTime dateTimeValue -> {
                                        final Timestamp sqlTimestamp = Timestamp.valueOf(dateTimeValue);
                                        statement.setTimestamp(index, sqlTimestamp);
                                    }
                                    case final String stringValue -> {
                                        final int maxLen = field.length;
                                        int strLen = stringValue.getBytes(StandardCharsets.UTF_8).length;

                                        if (strLen > maxLen) {
                                            String truncated = stringValue;

                                            while (strLen > maxLen) {
                                                final int delta = strLen - maxLen;
                                                final int truncatedLen = truncated.length();
                                                truncated = truncated.substring(0, Math.max(0, truncatedLen - delta));
                                                strLen = truncated.getBytes(StandardCharsets.UTF_8).length;
                                            }

                                            Log.warning("Truncating '", stringValue, "' to '", truncated,
                                                    "' to fit field '", field.fieldName, "'");
                                            statement.setString(index, truncated);
                                        } else {
                                            statement.setString(index, stringValue);
                                        }
                                    }
                                    default -> {
                                        final String valueClassName = value.getClass().getName();
                                        Log.warning("Unexpected object type: ", valueClassName);
                                    }
                                }
                            }

                            statement.executeUpdate();
                        } else {
                            final String[] msg = {"Incorrect number of values in row " + i + " in input file"};
                            JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }

                    conn.commit();
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    final String[] msg = {"Unable to insert record into " + table.tableName, ex.getMessage()};
                    JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                    break;
                }

                try (final Statement statement = conn.createStatement()) {
                    statement.executeUpdate("ALTER TABLE " + schemaName + "." + table.tableName + " SET LOGGED");
                    conn.setAutoCommit(true);
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    final String[] msg = {"Unable to set '" + table.tableName + "' table to LOGGED", ex.getMessage()};
                    JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                    break;
                }
            }
        }

        // Create the indexes after the bulk load so they don't have to get tested on each new row

        if (ok) {
            for (final IndexDefinition index : this.data.indexes) {
                final String createSql = index.makeCreateSql(schemaName);

                try (final Statement statement = conn.createStatement()) {
                    statement.executeUpdate(createSql);
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    final String[] msg = {"Unable to create '" + index.tableName + "' index", ex.getMessage()};
                    JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                    break;
                }
            }
        }

        if (ok) {
            for (final UniqueIndexDefinition index : this.data.uniqueIndexes) {
                final String createSql = index.makeCreateSql(schemaName);

                try (final Statement statement = conn.createStatement()) {
                    statement.executeUpdate(createSql);
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    final String[] msg = {"Unable to create '" + index.tableName + "' unique index", ex.getMessage()};
                    JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                    break;
                }
            }
        }
    }

    /**
     * Tests whether the target schema is empty.
     *
     * @param conn       the database connection
     * @param schemaName the schema name
     * @return true if the schema is empty
     */
    private boolean isSchemaEmpty(final Connection conn, final String schemaName) {

        boolean isEmpty = false;

        final String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema=?";

        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schemaName);

            try (final ResultSet rs = ps.executeQuery()) {

                isEmpty = true;
                while (rs.next()) {
                    final String tableName = rs.getString(1);
                    Log.warning("Table '", tableName, "' was found in '", schemaName, "' schema.");
                    isEmpty = false;
                }

                if (!isEmpty) {
                    final String msg = "The '" + schemaName + "' schema is not empty - unable to import";
                    JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                final String[] msg = {"Unable to query tables in schema:", ex.getMessage()};
                JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String[] msg = {"Unable to prepare statement to query tables in schema:", ex.getMessage()};
            JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
        }

        return isEmpty;
    }

    /**
     * Launches the app.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();
        SwingUtilities.invokeLater(new DbImport());
    }
}
