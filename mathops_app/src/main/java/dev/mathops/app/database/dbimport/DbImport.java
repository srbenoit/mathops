package dev.mathops.app.database.dbimport;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.ServerConfig;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

        final ContextMap map = ContextMap.getDefaultInstance();
        final ServerConfig[] servers = map.getServers();

        final List<ServerConfig> pgServers = new ArrayList<>(3);
        for (final ServerConfig server : servers) {
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
     * @param login  the selected login under which to perform the import
     * @param schema the schema into which to import
     */
    void databaseSelected(final LoginConfig login, final EDbUse schema) {

        try (final Connection conn = login.openConnection()) {
            Log.info("Connected to PostgreSQL - checking wither the '", schema, "' schema is empty...");

            final String schemaName = schema == EDbUse.PROD ? "legacy"
                    : (schema == EDbUse.DEV ? "legacy_dev" : "legacy_test");

            if (isSchemaEmpty(conn, schemaName)) {
                Log.info("Schema is empty - proceeding with import");

                // TODO:
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String[] msg = {"Unable to connect to PostgreSQL database:", ex.getMessage()};
            JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
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
