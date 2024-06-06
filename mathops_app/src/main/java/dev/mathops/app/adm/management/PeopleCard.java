package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.db.logic.Cache;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A card with people-related data.
 */
/* default */ class PeopleCard extends AdminPanelBase implements ActionListener {

    /** An action command. */
    private static final String REFRESH = "REFRESH";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2097566677702003943L;

    /**
     * Constructs a new {@code PeopleCard}.
     *
     * @param theCache the data cache
     */
    PeopleCard(final Cache theCache) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_BROWN);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_BROWN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        panel.add(makeHeader("People", false), BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_BROWN);
        panel.add(center, BorderLayout.CENTER);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Skin.BIG_BUTTON_16_FONT);
        tabs.setBackground(Skin.OFF_WHITE_RED);
        panel.add(tabs, BorderLayout.CENTER);

        final SpecialStudentsPanel specialStudents = new SpecialStudentsPanel(theCache);
        tabs.addTab("Special Students", specialStudents);
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
    /* default */ void focus() {

        // No action
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    /* default */ void reset() {

        // TODO:
    }

    /**
     * Called when the "Refresh" button is pressed.
     */
    private void processRefresh() {

        reset();
    }
}
