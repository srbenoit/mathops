package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A card with resource-related data.
 */
class ResourceCard extends AdmPanelBase implements ActionListener {

    /** An action command. */
    private static final String REFRESH = "REFRESH";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6473834022787197770L;

    /**
     * Constructs a new {@code ResourceCard}.
     */
    ResourceCard() {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_BLUE);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_BLUE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        panel.add(makeHeader("Resources", false), BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_BLUE);
        panel.add(center, BorderLayout.CENTER);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (REFRESH.equals(cmd)) {
            processRefresh();
        }
    }

    /**
     * Sets focus.
     */
    void focus() {

        // No action
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    void reset() {

        // TODO:
    }

    /**
     * Called when the "Refresh" button is pressed.
     */
    private void processRefresh() {

        reset();
    }
}
