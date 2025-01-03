package dev.mathops.app.course.visualizer;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

/**
 * The central panel that presents the selected topic module.
 */
final class TopicPanel extends JPanel {

    /** A panel to present the topic module metadata. */
    private final TopicMetadataPanel metadataPanel;

    /** The handouts panel for the topic. */
    private final HandoutsListPanel topicHandoutsPanel;

    /** The start lessons panel for the topic. */
    private final LessonsListPanel startLessonsPanel;

    /** The standards panel for the topic. */
    private final StandardsPanel standardsPanel;

    /** The end lessons panel for the topic. */
    private final LessonsListPanel endLessonsPanel;

    /**
     * Constructs a new {@code TopicPanel}.
     *
     * @param ownerSize the preferred size of the panel that will contain this panel
     * @param lineColor the color for line borders
     */
    TopicPanel(final Dimension ownerSize, final Color lineColor) {

        super(new StackedBorderLayout(1, 1));
        setBackground(lineColor);

        final Dimension mySize = new Dimension(ownerSize.width - 244, ownerSize.height - 26);
        setPreferredSize(mySize);

        this.metadataPanel = new TopicMetadataPanel(lineColor);
        this.metadataPanel.init();
        this.topicHandoutsPanel = new HandoutsListPanel("Module-Level Handouts:", lineColor);
        this.startLessonsPanel = new LessonsListPanel("Introductory Lessons:", 1, "intro", lineColor);
        this.standardsPanel = new StandardsPanel(lineColor);
        this.endLessonsPanel = new LessonsListPanel("Concluding Lessons:", 91, "conclusion", lineColor);

        final JPanel topRow = new JPanel(new StackedBorderLayout(1, 1));
        topRow.setBackground(lineColor);
        topRow.add(this.metadataPanel, StackedBorderLayout.WEST);
        topRow.add(this.topicHandoutsPanel, StackedBorderLayout.CENTER);
        add(topRow, StackedBorderLayout.NORTH);

        add(this.startLessonsPanel, StackedBorderLayout.NORTH);
        add(this.standardsPanel, StackedBorderLayout.CENTER);
        add(this.endLessonsPanel, StackedBorderLayout.SOUTH);
    }

    /**
     * Refreshes the contents of the panel based on the contents of a topic module directory.
     *
     * @param topicModuleDir the topic module directory ({@code null} if none selected)
     */
    void refresh(final File topicModuleDir) {

        this.metadataPanel.refresh(topicModuleDir);
        this.topicHandoutsPanel.refresh(topicModuleDir);
        this.startLessonsPanel.refresh(topicModuleDir);
        this.standardsPanel.refresh(topicModuleDir);
        this.endLessonsPanel.refresh(topicModuleDir);
    }
}
