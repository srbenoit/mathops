package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.db.old.Cache;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.io.Serial;

/**
 * A card with placement-related data.
 */
/* default */ class PlacementCard extends AdminPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4649552398988781982L;

    /** The panel that shows billing status and allows billing batches to be run. */
    private final PlacementBillingPanel billing;

    /** The panel that shows a report of recent placement activity. */
    private final PlacementReportPanel report;

    /**
     * Constructs a new {@code PlacementCard}.
     *
     * @param theCache         the data cache
     */
    PlacementCard(final Cache theCache) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_LIME);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_LIME);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        panel.add(makeHeader("Placement", false), BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_LIME);
        panel.add(center, BorderLayout.CENTER);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Skin.BIG_BUTTON_16_FONT);
        tabs.setBackground(Skin.OFF_WHITE_RED);
        panel.add(tabs, BorderLayout.CENTER);

        this.billing = new PlacementBillingPanel(theCache);
        tabs.addTab("Billing", this.billing);

        this.report = new PlacementReportPanel(theCache);
        tabs.addTab("Report", this.report);
    }

    /**
     * Refreshes the card's display.
     */
    public void refresh() {

        this.billing.refreshStatus();
        this.report.refreshStatus();
    }
}
