package dev.mathops.app.ops.snapin;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.io.Serial;

/**
 * The base class for a fullscreen panel.
 */
public abstract class AbstractFullPanel extends JPanel {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -3333584278600456357L;

    /**
     * Constructs a new {@code AbstractFullPanel}.
     */
    protected AbstractFullPanel() {

        super(new StackedBorderLayout());
    }

    /**
     * Called on a timer thread to periodically refresh displays.
     */
    public abstract void tick();
}
