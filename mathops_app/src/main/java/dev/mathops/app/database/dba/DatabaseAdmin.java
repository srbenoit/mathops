package dev.mathops.app.database.dba;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.ServerConfig;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * The main database administration program.
 */
public final class DatabaseAdmin implements Runnable {

    /** Constructs a new {@code DatabaseAdmin}. */
    private DatabaseAdmin() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

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
            final PgDatabasePicker picker = new PgDatabasePicker(pgServers);
            picker.init();
            UIUtilities.packAndCenter(picker);
            picker.setVisible(true);
        }
    }

    /**
     * Launches the app.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

//        FlatLightLaf.setup();
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(new DatabaseAdmin());
    }
}

