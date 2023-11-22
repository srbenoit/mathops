package dev.mathops.app.eos;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * An application to automate end-of-semester tasks.
 */
final class EndOfSemesterApp implements Runnable {

    /**
     * Constructs a new {@code EndOfSemesterApp}.
     */
    private EndOfSemesterApp() {

        // No action
    }

    /**
     * Constructs the UI on the AWT event thread.
     */
    @Override
    public void run() {

        final JFrame frame = new JFrame("End-of-Semester Processing");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        content.setPreferredSize(new Dimension(1024, 768));
        frame.setContentPane(content);

        final PanelChecklist checklist = new PanelChecklist();
        final JScrollPane checklistScroll = new JScrollPane(checklist);
        content.add(checklistScroll, BorderLayout.CENTER);

        frame.pack();
        final Dimension size = frame.getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 3);
        frame.setVisible(true);
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

        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(new EndOfSemesterApp());
    }
}
