package dev.mathops.app.adm.management.general;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.db.Cache;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A card with general system data.
 */
public class GeneralCard extends AdmPanelBase implements ActionListener {

    /** An action command. */
    private static final String REFRESH = "REFRESH";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4680085824616561014L;

    /** The panel that shows all configured facilities with their operating hours and closures. */
    private final FacilitiesPanel facilities;

    /**
     * Constructs a new {@code GeneralCard}.
     *
     * @param theCache the data cache
     */
    public GeneralCard(final Cache theCache) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_GREEN);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_GREEN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        panel.add(makeHeader("General Configuration", false), BorderLayout.NORTH);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Skin.BIG_BUTTON_16_FONT);
        tabs.setBackground(Skin.OFF_WHITE_GREEN);
        panel.add(tabs, BorderLayout.CENTER);

        this.facilities = new FacilitiesPanel(theCache);
        tabs.addTab("Facilities", this.facilities);
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
            refresh();
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
    public void refresh() {

        this.facilities.refreshStatus();
    }
}
