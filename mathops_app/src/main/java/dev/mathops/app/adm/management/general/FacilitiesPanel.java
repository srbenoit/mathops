package dev.mathops.app.adm.management.general;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.rec.main.FacilityRec;
import dev.mathops.db.reclogic.main.FacilityLogic;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

/**
 * A panel that supports management of special student populations.
 */
final class FacilitiesPanel extends AdmPanelBase implements ActionListener {

    /** An action command. */
    private static final String CMD_ADD_FACILITY = "ADD_FACILITY";

    /** The data cache. */
    private final Cache cache;

    /** The model backing the facility list. */
    private final DefaultListModel<String> facilityListModel;

    /** The list of all facilities. */
    private final JList<String> facilityList;

    /** A dialog to add a facility. */
    private AddFacilityDialog addDialog = null;

    /**
     * Constructs a new {@code FacilitiesPanel}.
     *
     * @param theCache the data cache
     */
    FacilitiesPanel(final Cache theCache) {

        super();

        setBackground(Skin.LIGHTEST);

        this.cache = theCache;

        // Left side: List with the set of all facilities, and a button to add a new one.

        final JPanel col1 = makeOffWhitePanel(new BorderLayout(5, 5));
        col1.setBackground(Skin.LIGHTEST);

        add(col1, StackedBorderLayout.WEST);

        col1.add(makeLabelMedium2("Defined Facilities"), BorderLayout.PAGE_START);

        this.facilityListModel = new DefaultListModel<>();
        this.facilityList = new JList<>(this.facilityListModel);
        final JScrollPane scroll = new JScrollPane(this.facilityList);
        col1.add(scroll, BorderLayout.CENTER);

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JButton addFacility = new JButton("Create new facility");
        addFacility.setActionCommand(CMD_ADD_FACILITY);
        buttonFlow.add(addFacility);
        col1.add(buttonFlow, BorderLayout.PAGE_END);

        // Center: Tabs with [Facility Information], [Hours of Operation], [Closures]

        final JTabbedPane tabs = new JTabbedPane();
        add(tabs, StackedBorderLayout.CENTER);

        addFacility.addActionListener(this);
        refreshStatus();
    }

    /**
     * Refreshes the billing status display.
     */
    void refreshStatus() {

        final FacilityLogic logic = FacilityLogic.get(this.cache);
        try {
            final List<FacilityRec> allRows = logic.queryAll(this.cache);

            this.facilityListModel.removeAllElements();
            for (final FacilityRec rec : allRows) {
                this.facilityListModel.addElement(rec.facility);
            }

        } catch (final SQLException ex) {
            Log.warning("Failed to query existing facilities", ex);
        }
    }

    /**
     * Called when an action is invoked by a button press.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CMD_ADD_FACILITY.equals(cmd)) {
            Log.info("Adding facility");

            if (this.addDialog == null) {
                this.addDialog = new AddFacilityDialog(this);
            }
            this.addDialog.setVisible(true);
            this.addDialog.toFront();
        }
    }

    /**
     * Adds a facility.
     *
     * @param rec the facility record to add
     * @return null if successful, an error message if unsuccessful
     */
    String[] addFacility(final FacilityRec rec) {

        String[] error = null;

        try {
            if (FacilityLogic.get(this.cache).insert(this.cache, rec)) {
                refreshStatus();
            } else {
                error = new String[]{"Failed to insert new facility record."};
            }
        } catch (final SQLException ex) {
            error = new String[]{"Failed to insert new facility record.", ex.getLocalizedMessage()};
        }

        return error;
    }
}
