package dev.mathops.app.adm.office;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.rec.RawCsection;
import dev.mathops.db.schema.main.rec.TermRec;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A card to display when the user selects the "Populations" option. This card includes a tabbed pane with ways to
 * select a population.
 */
final class CardPopulations extends AdmPanelBase implements ActionListener {

    /** Button action command. */
    private static final String BYCSECT_CMD = "BYCSECT";

    /** Button action command. */
    private static final String CSECT_CMD = "CSECT";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

    /** The data cache. */
    private final Cache cache;

    /** A button to search by course/section. */
    private JButton courseSectionBtn = null;

    /** Map from course to map from section to checkbox to search for that section's students. */
    private final Map<String, Map<String, JCheckBox>> courseSectionCheckboxes;

    /**
     * Constructs a new {@code CardPopulations}.
     *
     * @param theCache the data cache
     */
    CardPopulations(final Cache theCache) {

        super();

        this.courseSectionCheckboxes = new HashMap<>(10);

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_CYAN);
        final Border myBorder = getBorder();
        panel.setBorder(myBorder);

        setBackground(Skin.LT_CYAN);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final Border pad3 = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(bevel, pad3);
        setBorder(newBorder);
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;

        final JLabel header = makeHeader("Select a population...", false);
        panel.add(header, BorderLayout.PAGE_START);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);
        panel.add(tabs, BorderLayout.CENTER);

        final JPanel byCoursePanel = makeByCoursePanel();
        tabs.addTab("By Course and Section", byCoursePanel);

        final JPanel byCourseStatusPanel = new CardPopulationsCourseStatusPane(theCache);
        tabs.addTab("By Course Status", byCourseStatusPanel);

        // By special student type...

        // By placement status...

        // Others? Holds? Registration issues? Prerequisite issues? DCE? By advisor? By major?

    }

    /**
     * Creates the panel with controls to select a population by course, section and open status.
     *
     * @return the panel
     */
    private JPanel makeByCoursePanel() {

        final JPanel panel = new JPanel(new BorderLayout());
        final Border etched = BorderFactory.createEtchedBorder();
        final Border pad5 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        final CompoundBorder border = BorderFactory.createCompoundBorder(etched, pad5);
        panel.setBorder(border);
        panel.setBackground(Skin.WHITE);

        final SystemData systemData = this.cache.getSystemData();

        try {
            final TermRec active = systemData.getActiveTerm();
            final List<RawCsection> sects = systemData.getCourseSections(active.term);

            final Iterator<RawCsection> iter = sects.iterator();
            final Map<String, List<String>> csections = new TreeMap<>();
            while (iter.hasNext()) {
                final RawCsection row = iter.next();
                if ("Y".equals(row.bogus)) {
                    continue;
                }

                final List<String> list = csections.computeIfAbsent(row.course, s -> new ArrayList<>(10));
                list.add(row.sect);
            }

            final int count = csections.size();
            final JPanel gridPane = new JPanel(new GridLayout(0, count, 5, 5));
            gridPane.setBackground(Skin.WHITE);
            panel.add(gridPane, BorderLayout.PAGE_START);

            for (final Map.Entry<String, List<String>> entry : csections.entrySet()) {
                final String labelStr = entry.getKey().replace("M ", "MATH ");
                final JLabel lbl = new JLabel(labelStr);
                final MatteBorder lineBelow = BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM);
                lbl.setBorder(lineBelow);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(Skin.BIG_BUTTON_16_FONT);
                gridPane.add(lbl);

                final List<String> entryValue = entry.getValue();
                Collections.sort(entryValue);
            }

            int row = 0;
            boolean found = true;
            while (found) {
                found = false;
                for (final Map.Entry<String, List<String>> entry : csections.entrySet()) {
                    final List<String> list = entry.getValue();
                    if (list.size() > row) {
                        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
                        flow.setBackground(Skin.WHITE);
                        final JCheckBox check = new JCheckBox();
                        check.setActionCommand(CSECT_CMD);
                        check.addActionListener(this);

                        final String key = entry.getKey();
                        final Map<String, JCheckBox> map = this.courseSectionCheckboxes.computeIfAbsent(key,
                                s -> new HashMap<>(10));

                        final String listItem = list.get(row);
                        map.put(listItem, check);

                        flow.add(check);
                        final JLabel rowLbl = new JLabel(listItem);
                        flow.add(rowLbl);
                        found = true;
                        gridPane.add(flow);
                    } else {
                        gridPane.add(new JLabel(CoreConstants.EMPTY));
                    }
                }

                ++row;
            }

            final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttons.setBackground(Color.WHITE);
            this.courseSectionBtn = new JButton("Search...");
            this.courseSectionBtn.addActionListener(this);
            this.courseSectionBtn.setActionCommand(BYCSECT_CMD);
            this.courseSectionBtn.setEnabled(false);
            buttons.add(this.courseSectionBtn);
            panel.add(buttons, BorderLayout.PAGE_END);
        } catch (final SQLException ex) {
            final String exMsg = ex.getMessage();
            final JLabel msgLbl = new JLabel("Unable to query Course Section list: " + exMsg);
            panel.add(msgLbl, BorderLayout.CENTER);
        }

        return panel;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    void focus() {

        // TODO:
    }

    /**
     * Clears the display - this makes sure any open dialogs are closed so the app can close.
     */
    void clearDisplay() {

        // No action
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CSECT_CMD.equals(cmd)) {
            final Collection<String> courses = new ArrayList<>(10);
            final Collection<String> sects = new ArrayList<>(10);
            gatherSelectedCSects(courses, sects);
            final boolean hasItems = !courses.isEmpty();
            this.courseSectionBtn.setEnabled(hasItems);
        } else if (BYCSECT_CMD.equals(cmd)) {
            final List<String> courses = new ArrayList<>(10);
            final List<String> sects = new ArrayList<>(10);
            gatherSelectedCSects(courses, sects);

            if (courses.isEmpty()) {
                // Button was incorrectly enabled when no checkboxes were selected
                this.courseSectionBtn.setEnabled(false);
            } else {
                Log.info("Search by course/sect:");
                final int size = courses.size();
                for (int i = 0; i < size; ++i) {
                    Log.info("    ", courses.get(i), "/", sects.get(i));
                }
            }
        }
    }

    /**
     * Scans for the list of selected course/sections. On return, the provided lists will each have had the same number
     * of records (possibly 0) added to them with the course id and section number of each selected course/section.
     *
     * @param courses a list to which to add courses
     * @param sects   a list to which to add sections
     */
    private void gatherSelectedCSects(final Collection<? super String> courses,
                                      final Collection<? super String> sects) {

        for (final Map.Entry<String, Map<String, JCheckBox>> entry1 : this.courseSectionCheckboxes.entrySet()) {
            for (final Map.Entry<String, JCheckBox> entry2 : entry1.getValue().entrySet()) {
                if (entry2.getValue().isSelected()) {
                    final String key1 = entry1.getKey();
                    final String key2 = entry2.getKey();
                    courses.add(key1);
                    sects.add(key2);
                }
            }
        }
    }
}
