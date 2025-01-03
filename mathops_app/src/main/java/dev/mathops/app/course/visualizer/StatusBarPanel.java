package dev.mathops.app.course.visualizer;

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
     * @param ownerSize the preferred size of the panel that will contain this panel
     */
    StatusBarPanel(final Dimension ownerSize, final Color lineColor) {

        super(new StackedBorderLayout());

        final Dimension pref = new Dimension(ownerSize.width, 24);
        setPreferredSize(pref);

        final Border padding = BorderFactory.createEmptyBorder(6, 6, 6, 6);
        setBorder(padding);
    }
}
