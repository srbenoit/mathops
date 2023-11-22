package dev.mathops.app.canvas;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;

/**
 * A Swing application to connect to Canvas and exercise its API functions, presenting results to the user.
 */
final class CanvasExerciser {

    /**
     * Constructs a new {@code CanvasExerciser}.
     */
    private CanvasExerciser() {

        // No action
    }

    /**
     * Launches the application and creates the login window.
     *
     * <pre>
     * --host canvas-host-URL --token access-token
     * </pre>
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();

        String host = null;
        String token = null;

        if (args != null) {
            final int len = args.length;
            for (int i = 0; i < len - 1; ++i) {
                if ("--host".equals(args[i])
                        && !args[i + 1].startsWith("--")) {
                    host = args[i + 1];
                } else if ("--token".equals(args[i])
                        && !args[i + 1].startsWith("--")) {
                    token = args[i + 1];
                }
            }
        }

        SwingUtilities.invokeLater(new LoginWindow(host, token));
    }
}
