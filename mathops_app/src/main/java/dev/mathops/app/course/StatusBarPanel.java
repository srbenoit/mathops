package dev.mathops.app.course;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;

/**
 * A panel shown along the bottom edge to display status or messages.
 */
final class StatusBarPanel extends JPanel {

    /**
     * Constructs a new {@code StatusBarPanel}.
     *
     * @param ownerSize the preferred size opf the panel that will contain this panel
     */
    StatusBarPanel(final Dimension ownerSize) {

        super(new StackedBorderLayout());

        final Dimension pref = new Dimension(ownerSize.width, 24);
        setPreferredSize(pref);

        final Color bg = getBackground();
        final int level = bg.getRed() + bg.getGreen() + bg.getBlue();
        final Color lineColor = level < 384 ? bg.brighter() : bg.darker();

        final Border padding = BorderFactory.createEmptyBorder(6, 6, 6, 6);
        final Border line = BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor);
        final Border border = BorderFactory.createCompoundBorder(line, padding);
        setBorder(border);
    }
}
