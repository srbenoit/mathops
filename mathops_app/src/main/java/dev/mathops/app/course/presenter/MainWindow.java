package dev.mathops.app.course.presenter;

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
import java.util.Collection;

/**
 * The main window.
 */
public final class MainWindow extends JFrame {

    /** A character used in numbered directory names. */
    private static final char UNDERSCORE = '_';

    /** The threshold to consider a color as "light". */
    private static final int BRIGHTNESS_THRESHOLD = 384;

    /** The course directory. */
    private final File courseDir;

    /** The content pane. */
    private final JPanel content;

    /** The resource pane. */
    private DirectoryTreePane resourcePane;

    /** The media pane. */
    private MediaPane mediaPane;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theCourseDir the course directory
     */
    MainWindow(final File theCourseDir) {

        super();

        this.courseDir = theCourseDir;

        final String courseDirPath = theCourseDir.getAbsolutePath();
        final String title = Res.fmt(Res.PRESENTER_TITLE, courseDirPath);
        setTitle(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = new Dimension(screen.width / 2, screen.height * 2 / 3);
        this.content = new JPanel(new StackedBorderLayout(1, 1));

        final Color bg = this.content.getBackground();
        final int level = bg.getRed() + bg.getGreen() + bg.getBlue();
        final Color lineColor = level < BRIGHTNESS_THRESHOLD ? bg.brighter() : bg.darker();
        this.content.setBackground(lineColor);

        this.content.setPreferredSize(size);
        setContentPane(this.content);

        final Border topLine = BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor);
        this.content.setBorder(topLine);
    }

    /**
     * Initializes the window.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    public void init() {

        final JPanel west = new JPanel(new StackedBorderLayout(1, 1));
        this.content.add(west, StackedBorderLayout.WEST);

        final TopicTreePane topicTree = new TopicTreePane(this, this.courseDir);
        topicTree.init();
        west.add(topicTree, StackedBorderLayout.NORTH);

        this.resourcePane = new DirectoryTreePane(this, this.courseDir);
        this.resourcePane.init();
        west.add(this.resourcePane, StackedBorderLayout.CENTER);

        this.mediaPane = new MediaPane(this, this.courseDir);
        this.mediaPane.init();
        add(this.mediaPane, StackedBorderLayout.CENTER);
    }

    /**
     * Called when a topic is selected in the topic tree pane.
     *
     * @param subject the selected subject (a numbered directory name; null if none selected)
     * @param topic   the selected topic (a numbered directory name; null if none selected)
     */
    void topicSelected(final String subject, final String topic) {

        this.resourcePane.processSelection(subject, topic);
    }

    /**
     * Called when a directory is selected in the directory tree pane.
     *
     * @param selection the list of directory names below the course directory
     */
    void dirSelected(final Collection<String> selection) {

        this.mediaPane.processSelection(selection);
    }

    /**
     * Tests whether a filename is in numbered form, like "15_something"
     *
     * @param filename the filename
     * @return true if the name is in numbered forms
     */
    static boolean isNumberedFilename(final String filename) {

        boolean isNumbered = false;

        if (filename.length() > 3) {
            if ((int) filename.charAt(2) == (int) UNDERSCORE) {
                final char ch0 = filename.charAt(0);
                final char ch1 = filename.charAt(1);
                isNumbered = Character.isDigit(ch0) && Character.isDigit(ch1);
            }
        }

        return isNumbered;
    }

    /**
     * Generates an accent color (lighter if the background is "light", darker if the background is "dark").
     *
     * @param bg the current background
     * @return the accent color
     */
    static Color getAccent(final Color bg) {

        final int level = bg.getRed() + bg.getBlue() + bg.getGreen();

        return level > BRIGHTNESS_THRESHOLD ? bg.brighter() : bg.darker();
    }
}
