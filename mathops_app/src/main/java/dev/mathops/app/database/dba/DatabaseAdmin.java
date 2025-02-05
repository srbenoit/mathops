package dev.mathops.app.database.dba;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;

import javax.swing.SwingUtilities;

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

        final DatabaseConfig databaseConfig = DatabaseConfig.getDefault();

        final DatabasePicker picker = new DatabasePicker(databaseConfig);
        picker.init();
        UIUtilities.packAndCenter(picker);
        picker.setVisible(true);
    }

    /**
     * Launches the app.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();
//        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(new DatabaseAdmin());
    }
}

