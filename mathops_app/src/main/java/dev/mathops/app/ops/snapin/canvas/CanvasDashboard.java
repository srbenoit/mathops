package dev.mathops.app.ops.snapin.canvas;

import dev.mathops.app.ops.snapin.AbstractDashboardPanel;

import java.io.Serial;

/**
 * A dashboard panel for this snap-in.
 */
public final class CanvasDashboard extends AbstractDashboardPanel {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -7837612231769055569L;

    /**
     * Constructs a new {@code CanvasDashboard}.
     */
    CanvasDashboard() {

        super();
    }

    /**
     * Called on a timer thread to periodically refresh displays.
     */
    @Override
    public void tick() {

        // No action
    }
}
