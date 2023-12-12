package dev.mathops.app.db;

import com.formdev.flatlaf.FlatDarkLaf;
import dev.mathops.app.db.ui.MainWindow;
import dev.mathops.core.builder.SimpleBuilder;

import javax.swing.SwingUtilities;

/**
 * An application to configure and manage databases.
 *
 * <p>
 * This application loads the "db_config.xml" file and presents the contents in a form-based editor.  For every server
 * with a "DBA" login configured, the application supports the creation or validation of that database  For every
 * defined login, the application can test that login's access and permissions.
 *
 * <p>
 * The application has export/backup and import/restore features.  Data from any database can be exported to a backup
 * and restored to any other.  Single tables can also be exported and imported.
 *
 * <p>
 * Raw table contents can be visualized, sorted, filtered, updated, and deleted, and new data can be inserted.
 */
public final class DatabaseManager implements Runnable {

    /** The main window.*/
    private MainWindow mainWindow = null;

    /**
     * Constructs a new {@code DatabaseManager}.
     */
    private DatabaseManager() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        this.mainWindow = new MainWindow();
        this.mainWindow.init();

        this.mainWindow.setVisible(true);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("DatabaseManager{mainWindow=", this.mainWindow, "}");
    }

    /**
     * Main method to launch the application.
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(new DatabaseManager());
    }
}
