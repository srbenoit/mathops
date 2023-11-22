package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.app.ops.snapin.AbstractDashboardPanel;

import java.io.Serial;

/**
 * A dashboard panel for this snap-in.
 */
public final class MessagingDashboard extends AbstractDashboardPanel {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -559743567383076662L;

    /**
     * Constructs a new {@code MessagingDashboard}.
     */
    MessagingDashboard() {

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
