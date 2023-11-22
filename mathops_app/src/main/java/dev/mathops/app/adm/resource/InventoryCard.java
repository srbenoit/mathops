package dev.mathops.app.adm.resource;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A card panel to manage inventory.
 */
/* default */ class InventoryCard extends AdminPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5961620463532971934L;

    /**
     * Constructs a new {@code InventoryCard}.
     */
    /* default */ InventoryCard() {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_BLUE);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_BLUE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        panel.add(makeHeader("Resource Inventory", false), BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_BLUE);
        panel.add(center, BorderLayout.CENTER);
    }

    /**
     * Called when the "Loan" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        // TODO:
    }

    /**
     * Sets focus.
     */
    /* default */ void focus() {

        // No action
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    /* default */ void reset() {

        // No action
    }
}
