package dev.mathops.db.oldadmin;

import dev.mathops.commons.ui.UIUtilities;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * The main application.
 */
public final class JAdmin implements Runnable{

    /** The console. */
    private Console console = null;

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

        this.console = new Console(100, 40);
        frame.setContentPane(this.console);

        this.console.clear();
        this.console.print("MAIN ADMIN", 0, 0);
        this.console.reverse(5, 0, 5);
        this.console.commit();

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
