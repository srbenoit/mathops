package dev.mathops.app.database.dbimport;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.log.Log;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.io.File;

/**
 * The database import application.
 */
public final class DbImport implements Runnable {

    /** Constructs a new {@code DbImport}. */
    private DbImport() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        // Identify the location of the export folder on the local system
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
            final File chosenDir = chooser.getSelectedFile();
            Log.info("Selected path: ", chosenDir.getAbsolutePath());




            // Gather credentials to connect to the PostgreSQL instance and connect

            // Verify the "archive" schema exists and that there are no tables in it (drop needs to get done elsewhere)

            // Perform the import...
        }
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
