package dev.mathops.app.eos;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
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

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = screen.width * 2 / 3;
        final int h = screen.height * 2 / 3;

        final JPanel content = new JPanel(new BorderLayout());
        content.setPreferredSize(new Dimension(w, h));
        frame.setContentPane(content);

        final JPanel taskList = new JPanel(new StackedBorderLayout());
        final JScrollPane taskListScroll = new JScrollPane(taskList);
        content.add(taskListScroll, BorderLayout.CENTER);

        final JEditorPane report = new JEditorPane();
        report.setPreferredSize(new Dimension(w / 2, h - 20));
        report.setEditable(false);
        report.setBackground(Color.WHITE);
        report.setContentType("text/html");
        report.setText("<H2>Report</H2>");

        final JScrollPane reportScroll = new JScrollPane(report);
        content.add(reportScroll, BorderLayout.LINE_END);

        final S500CreateArchiveTables step500 = new S500CreateArchiveTables(null);
        final StepPanel panel500 = step500.getPanel();
        taskList.add(panel500, StackedBorderLayout.NORTH);

        final S501ArchiveData step501 = new S501ArchiveData(null, null);
        final StepPanel panel501 = step501.getPanel();
        taskList.add(panel501, StackedBorderLayout.NORTH);

        frame.pack();
        final Dimension size = frame.getSize();

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

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(new EndOfSemesterApp());
    }
}
