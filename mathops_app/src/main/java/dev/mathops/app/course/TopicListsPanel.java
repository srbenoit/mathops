package dev.mathops.app.course;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.io.File;

final class TopicListsPanel extends JPanel {

    /** The course directory. */
    private final File courseDir;

    /**
     * Constructs a new {@code TopicListsPanel}.
     *
     * @param theCourseDir the course directory
     * @param ownerSize    the preferred size of the panel that will contain this panel
     */
    TopicListsPanel(final File theCourseDir, final Dimension ownerSize) {

        super(new StackedBorderLayout());

        this.courseDir = theCourseDir;
    }
}
