package dev.mathops.app.course;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.io.File;

/**
 * A panel that will present a tabbed pane with a tab for the Skills Review and one tab per standard.  Each tab will
 * display the data for that standard.
 */
final class StandardsPanel extends JPanel {

    /** The tabbed pane. */
    private final JTabbedPane tabs;

    /**
     * Constructs a new {@code StandardsPanel}.
     *
     * @param lineColor the color for line borders
     */
    StandardsPanel(final Color lineColor) {

        super(new StackedBorderLayout());

        final Border border = BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor);
        setBorder(border);

        this.tabs = new JTabbedPane();
        add(this.tabs, StackedBorderLayout.CENTER);
    }

    /**
     * Clears the panel, removing all tabs.
     */
    private void clear() {

        this.tabs.removeAll();
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
                // TODO: Make this a real panel and store it somewhere
                final JPanel panel = new JPanel();
                this.tabs.addTab("Skills Review", panel);
            }

            for (int i = 1; i < 30; ++i) {
                final String stdStr = Integer.toString(i);

                final String dirName = (10 + i) + "_standard_" + stdStr;
                final File standardDir = new File(dir, dirName);

                if (standardDir.exists()) {
                    // TODO: Make this a real panel and store it somewhere
                    final String title = "Standard " + stdStr;
                    final JPanel panel = new JPanel();
                    this.tabs.addTab(title, panel);
                }
            }
        }
    }
}
