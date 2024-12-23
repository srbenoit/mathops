package dev.mathops.app.course;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

/**
 * The main window.
 */
public final class MainWindow extends JFrame {

    /** The course directory. */
    private final File courseDir;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theCourseDir the course directory
     */
    MainWindow(final File theCourseDir) {

        super();

        this.courseDir = theCourseDir;

        final String title = "Course Visualizer (" + theCourseDir.getAbsolutePath() + ")";
        setTitle(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = new Dimension(screen.width / 2, screen.height * 2 / 3);
        final JPanel content = new JPanel(new StackedBorderLayout());
        content.setPreferredSize(size);
        setContentPane(content);

        // The left-side will have two lists - the upper is the top-level directories (few), the lower is the topic
        // modules within the selected top-level directory.  Selecting a topic module will populate the main area.
        final JPanel left = new TopicListsPanel(theCourseDir, size);
        content.add(left, StackedBorderLayout.WEST);
    }
}
