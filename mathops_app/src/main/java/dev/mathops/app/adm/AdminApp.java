package dev.mathops.app.adm;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LoggingSubsystem;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.cfg.ContextMap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;

/**
 * A launcher class that executes the admin app (by creating its login window).
 */
final class AdminApp implements Runnable {

    /** The login username. */
    private final String username;

    /** The login password. */
    private final String password;

    /**
     * Constructs a new {@code AdminApp}.
     */
    private AdminApp(final String theUsername, final String thePassword) {

        this.username = theUsername;
        this.password = thePassword;
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        final String path = System.getProperty("user.dir");
        final File dir = new File(path);
        final File cfgFile = new File(dir, "db-config.xml");

        ContextMap contextMap;
        if (cfgFile.exists()) {
            try {
                contextMap = ContextMap.load(dir);
            } catch (final ParsingException ex) {
                contextMap = ContextMap.getDefaultInstance();
                Log.warning(ex);
            }
        } else {
            contextMap = ContextMap.getDefaultInstance();
        }

        if (contextMap == null) {
            JOptionPane.showMessageDialog(null, "Failed to load database configuration");
        } else {
            final LoginWindow login = new LoginWindow(contextMap, this.username, this.password);
            login.setVisible(true);
        }
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

        SwingUtilities.invokeLater(new AdminApp(username, password));
    }
}
