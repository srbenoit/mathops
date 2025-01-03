package dev.mathops.app.course.visualizer;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.builder.SimpleBuilder;

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

    /** The content pane. */
    private final JPanel content;

    /** The status bar. */
    private final StatusBarPanel statusBar;

    /** The topics list panel */
    private final TopicListsPanel topicsList;

    /** The topic panel. */
    private final TopicPanel topic;

    /** A blank panel shown when no topic is selected. */
    private final JPanel blank;

    /** The currently-displaying topic panel. */
    private JPanel displaying;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theCourseDir the course directory
     */
    MainWindow(final File theCourseDir) {

        super();

        this.courseDir = theCourseDir;

        final String courseDirPath = theCourseDir.getAbsolutePath();
        final String title = SimpleBuilder.concat("Course Visualizer (", courseDirPath, ")");
        setTitle(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = new Dimension(screen.width / 2, screen.height * 2 / 3);
        this.content = new JPanel(new StackedBorderLayout(1, 1));

        final Color bg = this.content.getBackground();
        final int level = bg.getRed() + bg.getGreen() + bg.getBlue();
        final Color lineColor = level < 384 ? bg.brighter() : bg.darker();
        this.content.setBackground(lineColor);

        this.content.setPreferredSize(size);
        setContentPane(this.content);

        final Border topLine = BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor);
        this.content.setBorder(topLine);

        // Status bar along the bottom to display status information and messages
        this.statusBar = new StatusBarPanel(size, lineColor);
        this.content.add(this.statusBar, StackedBorderLayout.SOUTH);

        // The left-side will have two lists - the upper is the top-level directories (few), the lower is the topic
        // modules within the selected top-level directory.  Selecting a topic module will populate the main area.
        this.topicsList = new TopicListsPanel(theCourseDir, size, lineColor);
        this.content.add(this.topicsList, StackedBorderLayout.WEST);

        this.topic = new TopicPanel(size, lineColor);
        this.blank = new JPanel();

        this.content.add(this.blank, StackedBorderLayout.CENTER);
        this.displaying = this.blank;
    }

    /**
     * Initializes the window.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    public void init() {

        this.topicsList.init(this);
    }

    /**
     * Called by the topic list panel when the selected subject and/or topic module changes.
     *
     * @param subject     the selected subject ({@code null} if none selected)
     * @param topicModule the selected topic module ({@code null} if none selected)
     */
    void setSelection(final String subject, final String topicModule) {

        File target = null;
        boolean repaint = false;

        if (subject == null || topicModule == null) {
            if (this.displaying instanceof TopicPanel) {
                this.content.remove(this.displaying);
                this.content.add(this.blank, StackedBorderLayout.CENTER);
                this.displaying = this.blank;
                repaint = true;
            }
        } else {
            final File subjectDir = new File(this.courseDir, subject);
            if (subjectDir.exists() && subjectDir.isDirectory()) {
                final File moduleDir = new File(subjectDir, topicModule);
                if (moduleDir.exists() && moduleDir.isDirectory()) {
                    target = moduleDir;
                    if (!(this.displaying instanceof TopicPanel)) {
                        this.content.remove(this.displaying);
                        this.content.add(this.topic, StackedBorderLayout.CENTER);
                        this.displaying = this.topic;
                        repaint = true;
                    }
                } else if (this.displaying instanceof TopicPanel) {
                    this.content.remove(this.displaying);
                    this.content.add(this.blank, StackedBorderLayout.CENTER);
                    this.displaying = this.blank;
                    repaint = true;
                }
            } else if (this.displaying instanceof TopicPanel) {
                this.content.remove(this.displaying);
                this.content.add(this.blank, StackedBorderLayout.CENTER);
                this.displaying = this.blank;
                repaint = true;
            }
        }

        if (repaint) {
            this.content.invalidate();
            this.content.revalidate();
            this.content.repaint();
        }

        this.topic.refresh(target);
    }
}
