package dev.mathops.app.ops;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.db.DbConnection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.prefs.Preferences;

/**
 * A launcher class that executes the operations app (by creating its login window).
 */
final class Operations {

    /** The key for the access token in system preferences. */
    static final String ACCESS_TOKEN_KEY = "canvastoken";

    /**
     * Constructs a new {@code Operations}.
     */
    private Operations() {

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

        final Preferences prefs = Preferences.userNodeForPackage(Operations.class);
        String token = prefs.get(ACCESS_TOKEN_KEY, null);
        boolean badToken = token == null || token.isBlank();

        if (badToken) {
            final String[] message = {"This program integrates with the Canvas LMS.",
                    "To do this, it requires an 'access token'.",
                    " ",
                    "Log in to your Canvas LMS, and click the [Account] icon on the left side.",
                    "Under 'Approved Integrations' you will need a 'User-Generated' access token.",
                    " ",
                    "If such a token already exists, open it and [Regenerate Token].",
                    "If not, create a new User-Defined token.",
                    " ",
                    "Paste the generated token below."};
            final String enteredToken = JOptionPane.showInputDialog(null, message, "Canvas LMS Integration",
                    javax.swing.JOptionPane.QUESTION_MESSAGE);

            badToken = enteredToken == null || enteredToken.isBlank();
            if (!badToken) {
                prefs.put(ACCESS_TOKEN_KEY, enteredToken);
                token = enteredToken;
            }
        }

        if (!badToken) {
            String username = null;
            String password = null;

            if (args != null) {
                final int numArgs = args.length;
                for (int i = 0; i < numArgs - 1; ++i) {
                    if ("--username".equals(args[i])
                        && !args[i + 1].startsWith("--")) {
                        username = args[i + 1];
                    } else if ("--password".equals(args[i])
                               && !args[i + 1].startsWith("--")) {
                        password = args[i + 1];
                    }
                }
            }

            SwingUtilities.invokeLater(new LoginWindow(username, password, token));
        }
    }
}
