package dev.mathops.db.oldadmin;

import dev.mathops.commons.ui.ChangeUI;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.old.DbConnection;

import javax.swing.SwingUtilities;

/**
 * The main application.
 */
public final class JAdmin implements Runnable {

    /** The username to use. */
    private final String username;

    /** The password to use. */
    private final String password;

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param theUsername the username to use (null to prompt for user to enter a username)
     * @param thePassword the password to use (null to prompt for user to enter a password)
     */
    private JAdmin(final String theUsername, final String thePassword) {

        this.username = theUsername;
        this.password = thePassword;
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        new LoginDialog(this.username, this.password).display();
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        ChangeUI.changeUI();
        DbConnection.registerDrivers();

        String username = null;
        String password = null;

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

        SwingUtilities.invokeLater(new JAdmin(username, password));
    }
}
