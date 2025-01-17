package dev.mathops.app.database.dba;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.cfg.ContextMap;

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

        final ContextMap map = ContextMap.getDefaultInstance();

        final DatabasePicker picker = new DatabasePicker(map);
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

