package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * A panel that supports management of special student populations.
 */
final class GeneralFacilitiesPanel extends AdmPanelBase {

    /** The data cache. */
    private final Cache cache;

    /** The list of all facilities. */
    private final JList<String> facilityList;

    /**
     * Constructs a new {@code GeneralFacilitiesPanel}.
     *
     * @param theCache the data cache
     */
    GeneralFacilitiesPanel(final Cache theCache) {

        super();

        setBackground(Skin.LIGHTEST);

        this.cache = theCache;

        // Left side: List with the set of all facilities, and a button to add a new one.

        final JPanel col1 = makeOffWhitePanel(new BorderLayout(5, 5));
        col1.setBackground(Skin.LIGHTEST);

        add(col1, StackedBorderLayout.WEST);

        col1.add(makeHeader("Facilities", false), BorderLayout.PAGE_START);

        this.facilityList = new JList<>();
        col1.add(this.facilityList, BorderLayout.CENTER);

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JButton addFacility = new JButton("Create new facility");
        buttonFlow.add(addFacility);
        col1.add(buttonFlow, BorderLayout.PAGE_END);

        // Center: Tabs with [Facility Information], [Hours of Operation], [Closures]

        final JTabbedPane tabs = new JTabbedPane();
        add(tabs, StackedBorderLayout.CENTER);
    }

    /**
     * Refreshes the billing status display.
     */
    public void refreshStatus() {

        // TODO
    }
}
