package dev.mathops.app.eos;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Image;
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
        final StepExecutable panel100 = step100.getPanel();
        stepList.addStep(section1Heading, panel100);

        final S101EditNextTermDataFiles step101 = new S101EditNextTermDataFiles(stepList, status);
        final StepManual panel101 = step101.getPanel();
        stepList.addStep(section1Heading, panel101);

        final String section5Heading = "Phase 5: Some other stuff...";

        final S500CreateArchiveTables step500 = new S500CreateArchiveTables(stepList, status, null);
        final StepExecutable panel500 = step500.getPanel();
        stepList.addStep(section5Heading, panel500);

        final S501ArchiveData step501 = new S501ArchiveData(stepList, status, null, null);
        final StepExecutable panel501 = step501.getPanel();
        stepList.addStep(section5Heading, panel501);

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
