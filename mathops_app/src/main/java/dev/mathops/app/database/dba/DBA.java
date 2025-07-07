package dev.mathops.app.database.dba;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;

import javax.swing.SwingUtilities;

/**
 * The main DBA program.
 */
public final class DBA implements Runnable {

    /** Constructs a new {@code DBA}. */
    private DBA() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        DbConnection.registerDrivers();

        final DatabaseConfig databaseConfig = DatabaseConfig.getDefault();
        final DBAWindow window = new DBAWindow(databaseConfig);

        UIUtilities.packAndCenter(window);
        window.setVisible(true);
    }

    /**
     * Launches the app.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

//        FlatLightLaf.setup();
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(new DBA());
    }
}

