package dev.mathops.app.adm;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LoggingSubsystem;
import dev.mathops.db.DbConnection;

import javax.swing.SwingUtilities;

/**
 * A launcher class that executes the admin app (by creating its login window).
 */
final class AdminApp {

    /**
     * Constructs a new {@code AdminApp}.
     */
    private AdminApp() {

        // No action
    }

    /**
     * Launches the application and creates the login window.
     *
     * <pre>
     * --username foo --password bar
     * </pre>
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();
        DbConnection.registerDrivers();

        String username = null;
        String password = null;

        LoggingSubsystem.getSettings().setLogToFiles(false);
        LoggingSubsystem.getSettings().setLogToConsole(true);
        Log.getWriter().startList(1000);

        if (args != null) {
            final int len = args.length;
            for (int i = 0; i < len - 1; ++i) {
                if ("--username".equals(args[i]) && !args[i + 1].startsWith("--")) {
                    username = args[i + 1];
                } else if ("--password".equals(args[i]) && !args[i + 1].startsWith("--")) {
                    password = args[i + 1];
                }
            }
        }

        SwingUtilities.invokeLater(new LoginWindow(username, password));
    }
}
