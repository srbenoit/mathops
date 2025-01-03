package dev.mathops.app.course.visualizer;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;

/**
 * A panel to show a summary of the types of files found in a "60_handouts" directory.
 */
final class HandoutsListPanel extends JPanel {

    /**
     * Constructs a new {@code HandoutsListPanel}.
     *
     * @param heading   the panel heading
     * @param lineColor the color for line borders
     */
    HandoutsListPanel(final String heading, final Color lineColor) {

        super(new StackedBorderLayout());

        final Border border = BorderFactory.createMatteBorder(0, 0, 0, 1, lineColor);
        setBorder(border);

        final JLabel headingLbl = new JLabel(heading);
        final JPanel headingLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        headingLine.add(headingLbl);
        add(headingLine, StackedBorderLayout.NORTH);

    }

    /**
     * Clears all fields in the panel.
     *
     * @param titleText text to display in the "title" field
     */
    private void clear(final String titleText) {

        // TODO:
    }

    /**
     * Refreshes the contents of the panel.
     *
     * @param dir the directory that should contain an "02_handouts" subdirectory
     */
    void refresh(final File dir) {

        if (dir == null) {
            clear(CoreConstants.SPC);
        } else {
            final File lessonsDir = new File(dir, "60_handouts");

            if (lessonsDir.exists()) {
                // TODO:
            } else {
                clear("(No handouts found)");
            }
        }
    }
}
