package dev.mathops.app.ops.snapin;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.Serial;

/**
 * The base class for a dashboard panel.
 */
public abstract class AbstractDashboardPanel extends JPanel {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 635099250966445023L;

    /**
     * Constructs a new {@code AbstractDashboardPanel}.
     */
    protected AbstractDashboardPanel() {

        super();

        final Dimension size = new Dimension(300, 240);
        setPreferredSize(size);
        setMinimumSize(size);
        setBackground(Color.WHITE);
    }

    /**
     * Sets the preferred size of this component.
     *
     * @param preferredSize the new preferred size
     */
    public final void setPreferredSize(final Dimension preferredSize) {

        super.setPreferredSize(preferredSize);
    }

    /**
     * Sets the minimum size of this component.
     *
     * @param minimumSize the new minimum size
     */
    public final void setMinimumSize(final Dimension minimumSize) {

        super.setMinimumSize(minimumSize);
    }

    /**
     * Sets the background color of this component
     *
     * @param bg the new minimum size
     */
    public final void setBackground(final Color bg) {

        super.setBackground(bg);
    }

    /**
     * Called on a timer thread to periodically refresh displays.
     */
    public abstract void tick();
}
