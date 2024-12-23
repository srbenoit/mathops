package dev.mathops.app.course;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

/**
 * The main window.
 */
public final class MainWindow extends JFrame {

    /** The course directory. */
    private final File courseDir;

    /** The status bar. */
    private final StatusBarPanel statusBar;

    /** The topics list panel */
    private final TopicListsPanel topicsList;

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

        final Color bg = getBackground();
        final int level = bg.getRed() + bg.getGreen() + bg.getBlue();
        final Color lineColor = level < 384 ? bg.brighter() : bg.darker();

        final Border topLine = BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor);
        content.setBorder(topLine);

        // Status bar along the bottom to display status information and messages
        this.statusBar = new StatusBarPanel(size);
        content.add(this.statusBar, StackedBorderLayout.SOUTH);

        // The left-side will have two lists - the upper is the top-level directories (few), the lower is the topic
        // modules within the selected top-level directory.  Selecting a topic module will populate the main area.
        this.topicsList = new TopicListsPanel(theCourseDir, size);
        this.topicsList.init();
        content.add(this.topicsList, StackedBorderLayout.WEST);
    }
}
