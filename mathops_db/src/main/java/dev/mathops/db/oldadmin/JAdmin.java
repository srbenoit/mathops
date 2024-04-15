package dev.mathops.db.oldadmin;

import dev.mathops.commons.ui.UIUtilities;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * The main application.
 */
public class JAdmin implements Runnable{

    /**
     * Private constructor to prevent direct instantiation.
     */
    private JAdmin() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        final JFrame frame = new JFrame("MATH ADMIN");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        final Console console = new Console(120, 60);
        frame.setContentPane(console);

        UIUtilities.packAndCenter(frame);
        frame.setVisible(true);
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        SwingUtilities.invokeLater(new JAdmin());
    }
}
