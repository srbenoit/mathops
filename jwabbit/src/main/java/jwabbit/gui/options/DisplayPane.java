package jwabbit.gui.options;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.Serial;

/**
 * A pane within the options dialog to simulate a display under various conditions.
 */
final class DisplayPane extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8962657737327164059L;

    /**
     * Constructs a new {@code DisplayPane}.
     */
    DisplayPane() {

        super();

        setPreferredSize(new Dimension(192, 128));
        setBackground(new Color(0x9e, 0xab, 0x88));
    }
}
