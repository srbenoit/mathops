package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A card panel that displays a map of the testing center, with usage of each station.
 */
class TestingMapCard extends AdminPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6444481681579133884L;

    /** An action command. */
    private static final String REFRESH = "REFRESH";

    /** The data cache. */
    private final Cache cache;

    /** The list of client computers to present. */
    private final List<RawClientPc> clients;

    /** The map. */
    private final TestingCenterMapPanel map;

    /**
     * Constructs a new {@code TestingMapCard}.
     *
     * @param theCache         the data cache
     */
    TestingMapCard(final Cache theCache) {

        super();

        this.cache = theCache;

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(getBackground());
        panel.setBorder(getBorder());

        setBackground(Skin.LT_GREEN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        final JPanel top = new JPanel(new StackedBorderLayout(10, 0));
        top.setBackground(Skin.OFF_WHITE_GREEN);
        top.add(makeHeader("Testing Center Map", false),
                StackedBorderLayout.WEST);

        final JButton refresh = new JButton("Refresh");
        refresh.setFont(Skin.BIG_BUTTON_16_FONT);
        refresh.addActionListener(this);
        refresh.setActionCommand(REFRESH);
        top.add(refresh, StackedBorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_GREEN);
        panel.add(center, BorderLayout.CENTER);

        this.clients = new ArrayList<>(100);
        loadClients();

        this.map = new TestingCenterMapPanel(this.clients);
        center.add(this.map, BorderLayout.CENTER);
    }

    /**
     * Called when the "Refresh" button is pressed.
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
     * Refreshes the card.
     */
    public void refresh() {

        loadClients();
        this.map.refresh();
    }

    /**
     * Loads the list of clients.
     */
    private void loadClients() {

        synchronized (this.clients) {
            this.clients.clear();

            try {
                final List<RawClientPc> stations = RawClientPcLogic.INSTANCE.queryAll(this.cache);

                for (final RawClientPc station : stations) {
                    final String center = station.testingCenterId;
                    if ("1".equals(center) || "4".equals(center)) {
                        this.clients.add(station);
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to query client PC records", ex);
            }
        }
    }
}
