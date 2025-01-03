package dev.mathops.app.course.visualizer;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;

/**
 * A panel that will present the content of the Skills Review.
 */
final class SkillsReviewPanel extends JPanel {

    /** The handouts panel for the Skills Review. */
    private final HandoutsListPanel topicHandoutsPanel;

    /** The start lessons panel for the topic. */
    private final LessonsListPanel startLessonsPanel;

    /**
     * Constructs a new {@code StandardPanel}.
     *
     * @param lineColor the color for line borders
     */
    SkillsReviewPanel(final Color lineColor) {

        super(new StackedBorderLayout(1, 1));
        setBackground(lineColor);

        final Border border = BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor);
        setBorder(border);

        this.topicHandoutsPanel = new HandoutsListPanel("Skills Review Handouts:", lineColor);
        this.startLessonsPanel = new LessonsListPanel("Introductory Lessons:", 1, "intro", lineColor);

        add(this.topicHandoutsPanel, StackedBorderLayout.NORTH);
        add(this.startLessonsPanel, StackedBorderLayout.NORTH);

        // TODO: Examples

        // TODO: Items

        // TODO: Assessments
    }
}
