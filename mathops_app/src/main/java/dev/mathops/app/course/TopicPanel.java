package dev.mathops.app.course;

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

    /** The lessons panel for the topic. */
    private final LessonsListPanel topicLessonsPanel;

    /** The handouts panel for the topic. */
    private final HandoutsListPanel topicHandoutsPanel;

    /** The standards panel for the topic. */
    private final StandardsPanel standardsPanel;

    /**
     * Constructs a new {@code TopicPanel}.
     *
     * @param ownerSize the preferred size of the panel that will contain this panel
     * @param lineColor the color for line borders
     */
    TopicPanel(final Dimension ownerSize, final Color lineColor) {

        super(new StackedBorderLayout());

        final Dimension mySize = new Dimension(ownerSize.width - 244, ownerSize.height - 26);
        setPreferredSize(mySize);

        this.metadataPanel = new TopicMetadataPanel(lineColor);
        this.metadataPanel.init();
        this.topicLessonsPanel = new LessonsListPanel("Module-Level Lessons:", lineColor);
        this.topicHandoutsPanel = new HandoutsListPanel("Module-Level Handouts:", lineColor);
        this.standardsPanel = new StandardsPanel(lineColor);

        final JPanel topRow = new JPanel(new StackedBorderLayout());
        add(topRow, StackedBorderLayout.NORTH);
        topRow.add(this.metadataPanel, StackedBorderLayout.WEST);
        topRow.add(this.topicLessonsPanel, StackedBorderLayout.WEST);
        topRow.add(this.topicHandoutsPanel, StackedBorderLayout.WEST);
        add(this.standardsPanel, StackedBorderLayout.CENTER);
    }

    /**
     * Refreshes the contents of the panel based on the contents of a topic module directory.
     *
     * @param topicModuleDir the topic module directory ({@code null} if none selected)
     */
    void refresh(final File topicModuleDir) {

        this.metadataPanel.refresh(topicModuleDir);
        this.topicLessonsPanel.refresh(topicModuleDir);
        this.topicHandoutsPanel.refresh(topicModuleDir);
        this.standardsPanel.refresh(topicModuleDir);
    }
}
