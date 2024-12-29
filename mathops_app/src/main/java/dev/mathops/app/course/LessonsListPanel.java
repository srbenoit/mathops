package dev.mathops.app.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;

/**
 * A panel to show a list of all lessons in a directory (used for both topic modules, skills reviews, and standards).
 */
final class LessonsListPanel extends JPanel {

    /** A character used in subject and module directory names. */
    private static final int UNDERSCORE = '_';

    /** A panel to hold the list of lessons. */
    private final JPanel lessonList;

    /**
     * Constructs a new {@code LessonsListPanel}.
     *
     * @param heading   the panel heading
     * @param lineColor the color for line borders
     */
    LessonsListPanel(final String heading, final Color lineColor) {

        super(new StackedBorderLayout());

        final Border padding = BorderFactory.createEmptyBorder(3, 6, 3, 6);
        setBorder(padding);

        final JLabel headingLbl = new JLabel(heading);
        final Border labelPad = BorderFactory.createEmptyBorder(0, 0, 3, 0);
        headingLbl.setBorder(labelPad);
        add(headingLbl, StackedBorderLayout.NORTH);

        final JTextArea testArea = new JTextArea();
        final Color testBg = testArea.getBackground();

        this.lessonList = new JPanel(new StackedBorderLayout());
        final Border etched = BorderFactory.createEtchedBorder();
        this.lessonList.setBorder(etched);
        this.lessonList.setBackground(testBg);
        add(this.lessonList, StackedBorderLayout.CENTER);
    }

    /**
     * Clears all fields in the panel.
     *
     * @param message text to display
     */
    private void clear(final String message) {

        this.lessonList.removeAll();

        final JLabel lbl = new JLabel(message);
        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        final Color bg = this.lessonList.getBackground();
        flow.setBackground(bg);
        flow.add(lbl);
        this.lessonList.add(flow, StackedBorderLayout.NORTH);

        this.lessonList.invalidate();
        this.lessonList.revalidate();
        this.lessonList.repaint();
    }

    /**
     * Refreshes the contents of the panel.
     *
     * @param dir the directory that should contain an "01_lessons" subdirectory
     */
    void refresh(final File dir) {

        if (dir == null || !dir.exists()) {
            clear(CoreConstants.SPC);
        } else {
            final File[] files = dir.listFiles();

            if (files == null) {
                clear("(No lessons found)");
            } else {
                this.lessonList.removeAll();
                int numFound = 0;
                for (final File file : files) {
                    if (file.isDirectory()) {
                        final String name = file.getName();
                        ++numFound;

                        final JLabel lbl = new JLabel(name);
                        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
                        flow.add(lbl);
                        this.lessonList.add(flow, StackedBorderLayout.NORTH);
                    }
                }

                if (numFound == 0) {
                    clear("(No lessons found)");
                } else {
                    this.lessonList.invalidate();
                    this.lessonList.revalidate();
                    this.lessonList.repaint();
                }
            }
        }
    }
}
