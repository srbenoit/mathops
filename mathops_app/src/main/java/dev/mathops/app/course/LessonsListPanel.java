package dev.mathops.app.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;

/**
 * A panel to show a list of all lessons in a directory (used for both topic modules, skills reviews, and standards).
 */
final class LessonsListPanel extends JPanel {

    /**
     * Constructs a new {@code LessonsListPanel}.
     *
     * @param heading the panel heading
     * @param lineColor the color for line borders
     */
    LessonsListPanel(final String heading, final Color lineColor) {

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
     * @param dir the directory that should contain an "01_lessons" subdirectory
     */
    void refresh(final File dir) {

        if (dir == null) {
            clear(CoreConstants.SPC);
        } else {
            final File lessonsDir = new File(dir, "01_lessons");

            if (lessonsDir.exists()) {
                // TODO:
            } else {
                clear("(No lessons found)");
            }
        }
    }
}
