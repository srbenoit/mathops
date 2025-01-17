package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;

/**
 * A base class for panels that are displayed when something is selected in the tree view.
 */
class PanelBase extends JPanel {

    /** A larger font for headings. */
    final Font largerFont;

    /** A bold version of the larger font for headings. */
    final Font largerBoldFont;

    /** An accent color for text to which we want to draw attention. */
    final Color accentColor;

    /**
     * Constructs a new {@code PanelBase}.
     */
    PanelBase() {

        super(new StackedBorderLayout());

        final JLabel label = new JLabel(" ");

        final Font font = label.getFont();
        final int fontSize = font.getSize();
        this.largerFont = font.deriveFont((float) fontSize * 1.3f);
        this.largerBoldFont = this.largerFont.deriveFont(Font.BOLD);

        final Color origColor = label.getForeground();
        final int origRed = origColor.getRed();
        final int origGreen = origColor.getGreen();
        final int origBlue = origColor.getBlue();
        final int newBlue = origBlue < 128 ? (origBlue + 255) / 2 : (origBlue * 2 / 3);
        this.accentColor = new Color(origRed, origGreen, newBlue);
    }

    /**
     * Creates a title label (in the larger font) and a heading label (in the larger bold font, and the accent color).
     * Both are then added to a target {@code JPanel} (assumed to have a flow layout), and then the heading label is
     * returned so its contents can be updated.
     *
     * @param titleText the title text
     * @param target    the target {@code JPanel} to which to add the title label and heading label
     * @return the heading label
     */
    final JLabel makeHeadingLabel(final String titleText, final JPanel target) {

        final JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(this.largerFont);

        final JLabel headingLabel = new JLabel(" ");
        headingLabel.setFont(this.largerBoldFont);
        headingLabel.setForeground(this.accentColor);

        target.add(titleLabel);
        target.add(headingLabel);

        return headingLabel;
    }
}
