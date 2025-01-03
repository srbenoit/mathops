package dev.mathops.app.course.visualizer;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.builder.SimpleBuilder;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.io.File;
import java.util.Arrays;

/**
 * A panel that will present a tabbed pane with a tab for the Skills Review and one tab per standard.  Each tab will
 * display the data for that standard.
 */
final class StandardsPanel extends JPanel {

    /** The maximum number of standards we can display. */
    private static final int MAX_STANDARDS = 30;

    /** The line color. */
    private final Color lineColor;

    /** The tabbed pane. */
    private final JTabbedPane tabs;

    /** The displayed Skills Review panel. */
    private SkillsReviewPanel skillsReview;

    /** The displayed set of standards. */
    private final StandardPanel[] standards;

    /**
     * Constructs a new {@code StandardsPanel}.
     *
     * @param theLineColor the color for line borders
     */
    StandardsPanel(final Color theLineColor) {

        super(new StackedBorderLayout());

        this.lineColor = theLineColor;

        final Border border = BorderFactory.createMatteBorder(1, 0, 0, 0, theLineColor);
        setBorder(border);

        this.tabs = new JTabbedPane();
        add(this.tabs, StackedBorderLayout.CENTER);

        this.standards = new StandardPanel[MAX_STANDARDS];
    }

    /**
     * Clears the panel, removing all tabs.
     */
    private void clear() {

        this.tabs.removeAll();

        this.skillsReview = null;
        Arrays.fill(this.standards, null);

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Refreshes the contents of the panel.
     *
     * @param dir the directory that should contain a "10_skills_review" subdirectory and a series of subdirectories of
     *            the form "11_standard_1", "12_standard_2", etc.
     */
    void refresh(final File dir) {

        clear();

        if (dir != null) {
            final File skillsReviewDir = new File(dir, "10_skills_review");
            if (skillsReviewDir.exists()) {
                this.skillsReview = new SkillsReviewPanel(this.lineColor);
                this.tabs.addTab("Skills Review", this.skillsReview);
            }

            for (int i = 1; i < MAX_STANDARDS; ++i) {
                final String stdStr = Integer.toString(i);

                final String dirName = (10 + i) + "_standard_" + stdStr;
                final File standardDir = new File(dir, dirName);

                if (standardDir.exists()) {
                    final String title = SimpleBuilder.concat("Standard ", stdStr);
                    this.standards[i] = new StandardPanel(this.lineColor);
                    this.tabs.addTab(title, this.standards[i]);
                }
            }
        }
    }
}
