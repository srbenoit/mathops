package dev.mathops.app.eos;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * An application to automate end-of-semester tasks.
 */
final class EndOfSemesterApp implements Runnable, ActionListener {

    /** Saves the state of the process to a file. */
    private static final String SAVE_CMD = "SAVE";

    /** Loads the state of the process from a file. */
    private static final String LOAD_CMD = "LOAD";

    /** The list of steps. */
    private final List<AbstractStep> steps;

    /**
     * Constructs a new {@code EndOfSemesterApp}.
     */
    private EndOfSemesterApp() {

        this.steps = new ArrayList<>(100);
    }

    /**
     * Constructs the UI on the AWT event thread.
     */
    @Override
    public void run() {

        final Class<?> cls = getClass();
        final Image expandImg = FileLoader.loadFileAsImage(cls, "expand.png", true);
        final Image collapseImg = FileLoader.loadFileAsImage(cls, "collapse.png", true);

        final JFrame frame = new JFrame("End-of-Semester Processing");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = screen.width * 3 / 4;
        final int h = screen.height * 3 / 4;

        final JPanel content = new JPanel(new StackedBorderLayout());
        content.setPreferredSize(new Dimension(w, h));
        frame.setContentPane(content);

        final StepList stepList = new StepList(expandImg, collapseImg);
        final JScrollPane stepListScroll = new JScrollPane(stepList);
        content.add(stepListScroll, StackedBorderLayout.CENTER);

        final StepDisplay status = new StepDisplay(h - 20, w / 2);
        content.add(status, StackedBorderLayout.EAST);

        final String section1Heading = "Phase 1: Review and Prepare";

        final S100CheckForNonReturnedResources step100 = new S100CheckForNonReturnedResources(stepList, status);
        this.steps.add(step100);
        stepList.addStep(section1Heading, step100);

        final S101EditNextTermDataFiles step101 = new S101EditNextTermDataFiles(stepList, status);
        this.steps.add(step101);
        stepList.addStep(section1Heading, step101);

        final String section5Heading = "Phase 5: Some other stuff...";

        final S500CreateArchiveTables step500 = new S500CreateArchiveTables(stepList, status, null);
        this.steps.add(step500);
        stepList.addStep(section5Heading, step500);

        final S501ArchiveData step501 = new S501ArchiveData(stepList, status, null, null);
        this.steps.add(step501);
        stepList.addStep(section5Heading, step501);

        final JButton saveBtn = new JButton("Save progress...");
        saveBtn.setActionCommand(SAVE_CMD);
        saveBtn.addActionListener(this);
        final JButton loadBtn = new JButton("Load saved progress...");
        loadBtn.setActionCommand(LOAD_CMD);
        loadBtn.addActionListener(this);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 10));
        buttons.add(saveBtn);
        buttons.add(loadBtn);
        content.add(buttons, StackedBorderLayout.SOUTH);

        frame.pack();
        final Dimension size = frame.getSize();

        frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 3);
        frame.setVisible(true);
    }

    /**
     * Called when the "Save" or "Load" buttons are activated.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (SAVE_CMD.equals(cmd)) {
            Log.info("Save");

            // For each step, we create a JSON object with the step number, finished status, report, and notes.

        } else if (LOAD_CMD.equals(cmd)) {
            Log.info("Load");
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

        SwingUtilities.invokeLater(new EndOfSemesterApp());
    }
}
