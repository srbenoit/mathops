package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawCsectionLogic;
import dev.mathops.db.rawrecord.RawCsection;
import dev.mathops.db.svc.term.TermLogic;
import dev.mathops.db.svc.term.TermRec;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
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
final class CardPopulations extends AdminPanelBase implements ActionListener {

    /** Button action command. */
    private static final String BYCSECT_CMD = "BYCSECT";

    /** Button action command. */
    private static final String CSECT_CMD = "CSECT";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

    /** The owning admin pane. */
    private final TopPanelStudent owner;

    /** The data cache. */
    private final Cache cache;

    /** The fixed data. */
    private final FixedData fixed;

    /** A button to search by course/section. */
    private JButton cstatusBtn;

    /** Map from course to map from section to checkbox to search for that section's students. */
    private final Map<String, Map<String, JCheckBox>> csectCheckboxes;

    /**
     * Constructs a new {@code CardPopulations}.
     *
     * @param theOwner         the owning top-level student panel
     * @param theCache         the data cache
     * @param theFixed         the fixed data
     */
    CardPopulations(final TopPanelStudent theOwner, final Cache theCache, final FixedData theFixed) {

        super();

        this.csectCheckboxes = new HashMap<>(10);

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_CYAN);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_CYAN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.owner = theOwner;
        this.cache = theCache;
        this.fixed = theFixed;

        panel.add(makeHeader("Select a population...", false), BorderLayout.PAGE_START);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);
        panel.add(tabs, BorderLayout.CENTER);

        final JPanel byCoursePanel = makeByCoursePanel(theCache, theFixed);
        tabs.addTab("By Course and Section", byCoursePanel);

        final JPanel byCourseStatusPanel =
                new CardPopulationsCourseStatusPane(theOwner, theCache, theFixed);
        tabs.addTab("By Course Status", byCourseStatusPanel);

        // By special student type...

        // By placement status...

        // Others? Holds? Registration issues? Prerequisite issues? DCE? By advisor? By major?

    }

    /**
     * Creates the panel with controls to select a population by course, section and open status.
     *
     * @param theCache         the data cache
     * @param theFixed         the fixed data
     * @return the panel
     */
    private JPanel makeByCoursePanel(final Cache theCache, final FixedData theFixed) {

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.setBackground(Skin.WHITE);

        try {
            final TermRec active = TermLogic.get(this.cache).queryActive(theCache);
            final List<RawCsection> sects = RawCsectionLogic.queryByTerm(theCache, active.term);

            final Iterator<RawCsection> iter = sects.iterator();
            final Map<String, List<String>> csections = new TreeMap<>();
            while (iter.hasNext()) {
                final RawCsection row = iter.next();
                if ("Y".equals(row.bogus)) {
                    continue;
                }

                final List<String> list = csections.computeIfAbsent(row.course, k -> new ArrayList<>(10));
                list.add(row.sect);
            }

            final JPanel gridPane = new JPanel(new GridLayout(0, csections.size(), 5, 5));
            gridPane.setBackground(Skin.WHITE);
            panel.add(gridPane, BorderLayout.PAGE_START);

            for (final Map.Entry<String, List<String>> entry : csections.entrySet()) {
                final JLabel lbl = new JLabel(entry.getKey().replace("M ", "MATH "));
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(Skin.BIG_BUTTON_16_FONT);
                gridPane.add(lbl);

                Collections.sort(entry.getValue());
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

                        final Map<String, JCheckBox> map = this.csectCheckboxes.computeIfAbsent(entry.getKey(),
                                s -> new HashMap<>(10));
                        map.put(list.get(row), check);

                        flow.add(check);
                        final JLabel rowLbl = new JLabel(list.get(row));
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
            this.cstatusBtn = new JButton("Search...");
            this.cstatusBtn.addActionListener(this);
            this.cstatusBtn.setActionCommand(BYCSECT_CMD);
            this.cstatusBtn.setEnabled(false);
            buttons.add(this.cstatusBtn);
            panel.add(buttons, BorderLayout.PAGE_END);
        } catch (final SQLException ex) {
            final JLabel msgLbl = new JLabel("Unable to query Course Section list: " + ex.getMessage());
            panel.add(msgLbl, BorderLayout.CENTER);
        }

        return panel;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        // TODO:
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
            this.cstatusBtn.setEnabled(!courses.isEmpty());
        } else if (BYCSECT_CMD.equals(cmd)) {
            final List<String> courses = new ArrayList<>(10);
            final List<String> sects = new ArrayList<>(10);
            gatherSelectedCSects(courses, sects);

            if (courses.isEmpty()) {
                // Button was incorrectly enabled when no checkboxes were selected
                this.cstatusBtn.setEnabled(false);
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

        for (final Map.Entry<String, Map<String, JCheckBox>> entry1 : this.csectCheckboxes.entrySet()) {
            for (final Map.Entry<String, JCheckBox> entry2 : entry1.getValue().entrySet()) {
                if (entry2.getValue().isSelected()) {
                    courses.add(entry1.getKey());
                    sects.add(entry2.getKey());
                }
            }
        }
    }
}
