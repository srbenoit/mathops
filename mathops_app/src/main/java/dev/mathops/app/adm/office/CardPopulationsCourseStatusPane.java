package dev.mathops.app.adm.office;

import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.impl.RawSttermLogic;
import dev.mathops.db.schema.legacy.rec.RawMilestone;
import dev.mathops.db.schema.legacy.rec.RawStterm;
import dev.mathops.db.schema.main.rec.TermRec;
import dev.mathops.db.field.TermKey;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The "By course status" pane within the card that selects a student population. This pane allows the user to select by
 * number of enrollments, pace track, and course status.
 */
final class CardPopulationsCourseStatusPane extends JPanel implements ActionListener {

    /** Button action command. */
    private static final String BY_STATUS_CMD = "BY_STATUS";

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -593777255738938860L;

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey activeKey;

    /** The "search" button. */
    private final JButton searchBtn;

    /** A progress bar to show progress of population query. */
    private final JProgressBar progress;

    /** Map from pace to track to index to checkbox that enables that block of milestones. */
    private final Map<Integer, Map<String, Map<Integer, JCheckBox>>> frontBoxes;

    /** Map from pace to track to milestone to checkbox. */
    private final Map<Integer, Map<String, Map<Integer, Map<ECourseMilestone, JCheckBox>>>> boxes;

    /**
     * Constructs a new {@code CardPopulationsCourseStatusPane}.
     *
     * @param theCache the data cache
     */
    CardPopulationsCourseStatusPane(final Cache theCache) {

        super(new BorderLayout());

        this.cache = theCache;

        final SystemData systemData = theCache.getSystemData();
        TermRec active;
        try {
            active = systemData.getActiveTerm();
        } catch (final SQLException ex) {
            active = null;
        }

        this.activeKey = active == null ? null : active.term;
        this.frontBoxes = new HashMap<>(5);
        this.boxes = new HashMap<>(5);

        // Determine all pace track combinations, store milestones, so we can highlight deadline
        // status of review/final exams.

        final Map<Integer, Set<String>> paceTracks = new TreeMap<>();
        final List<RawMilestone> milestones;

        try {
            milestones = systemData.getMilestones(this.activeKey);

            for (final RawMilestone ms : milestones) {
                final Set<String> tracks = paceTracks.computeIfAbsent(ms.pace, k -> new TreeSet<>());
                tracks.add(ms.paceTrack);
                // Log.info("Adding pace " + ms.pace + " track " + ms.paceTrack);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query active term and milestone data.", ex);
        }

        final Border etched = BorderFactory.createEtchedBorder();
        final Border pad5 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(etched, pad5);
        setBorder(newBorder);
        setBackground(Skin.WHITE);

        final JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Skin.WHITE);
        add(center, BorderLayout.CENTER);

        final JPanel centerNorth = new JPanel();
        final LayoutManager centerBox = new BoxLayout(centerNorth, BoxLayout.PAGE_AXIS);
        centerNorth.setLayout(centerBox);
        centerNorth.setBackground(Skin.WHITE);
        center.add(centerNorth, BorderLayout.PAGE_START);

        final MatteBorder lineBelow = BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM);

        // Loop through paces, then within each, loop through tracks, and build UI elements
        for (final Map.Entry<Integer, Set<String>> paceEntry : paceTracks.entrySet()) {
            final Integer pace = paceEntry.getKey();
            final int paceInt = pace.intValue();

            final JLabel pace1Lbl = new JLabel(pace + " Course Pace:");
            pace1Lbl.setFont(Skin.BOLD_12_FONT);
            final JPanel paceFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
            paceFlow.setBorder(lineBelow);
            paceFlow.setBackground(Skin.WHITE);
            paceFlow.add(pace1Lbl);
            centerNorth.add(paceFlow);

            for (final String track : paceEntry.getValue()) {
                for (int index = 1; index <= paceInt; ++index) {
                    final String label = track + index + ":";
                    final JPanel trackPane = makeCourseStatusPane(label, pace, track, Integer.valueOf(index));

                    if (index == paceInt) {
                        trackPane.setBorder(lineBelow);
                    }
                    centerNorth.add(trackPane);
                }
            }
        }

        //

        final JPanel bottom = new JPanel(new BorderLayout());
        add(bottom, BorderLayout.PAGE_END);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.setBackground(Color.WHITE);
        this.searchBtn = new JButton("Search...");
        this.searchBtn.addActionListener(this);
        this.searchBtn.setActionCommand(BY_STATUS_CMD);
        buttons.add(this.searchBtn);
        bottom.add(buttons, BorderLayout.PAGE_START);

        this.progress = new JProgressBar(0, 100);
        bottom.add(this.progress, BorderLayout.PAGE_END);
    }

    /**
     * Creates a pane with checkboxes to select a student's status in a single course.
     *
     * @param label       the label to prefix the row
     * @param pace        the pace
     * @param track       the pace track
     * @param courseIndex the course index (1-based)
     * @return the panel
     */
    private JPanel makeCourseStatusPane(final String label, final Integer pace, final String track,
                                        final Integer courseIndex) {

        final JPanel pane = new JPanel(new BorderLayout());
        pane.setBackground(Skin.WHITE);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        topFlow.setBackground(Color.WHITE);
        pane.add(topFlow, BorderLayout.PAGE_START);

        final JCheckBox check = new JCheckBox(label);
        check.setFont(Skin.MONO_12_FONT);
        check.setActionCommand("CHECK_" + pace + "." + track + "-" + courseIndex);
        check.addActionListener(this);
        topFlow.add(check);

        // Add the checkbox at the front of the row to member data

        final Map<String, Map<Integer, JCheckBox>> frontPaceMap =
                this.frontBoxes.computeIfAbsent(pace, k -> new HashMap<>(10));
        final Map<Integer, JCheckBox> frontTrackMap = frontPaceMap.computeIfAbsent(track, s -> new HashMap<>(10));
        frontTrackMap.put(courseIndex, check);

        // Add a map to store the checkboxes for each milestone

        final Map<String, Map<Integer, Map<ECourseMilestone, JCheckBox>>> paceMap
                = this.boxes.computeIfAbsent(pace, k -> new HashMap<>(6));

        final Map<Integer, Map<ECourseMilestone, JCheckBox>> trackMap
                = paceMap.computeIfAbsent(track, s -> new HashMap<>(6));

        final Map<ECourseMilestone, JCheckBox> indexMap
                = trackMap.computeIfAbsent(courseIndex, k -> new EnumMap<>(ECourseMilestone.class));

        // Create the checkboxes

        final Font boldFont = check.getFont().deriveFont(Font.BOLD);

        for (final ECourseMilestone status : ECourseMilestone.values()) {
            final JCheckBox chk = new JCheckBox(status.label);

            chk.setEnabled(false);
            if (status.hasDueDate) {
                chk.setFont(boldFont);
            }
            topFlow.add(chk);

            indexMap.put(status, chk);
        }

        return pane;
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (BY_STATUS_CMD.equals(cmd)) {
            final Map<Integer, Map<String, Map<Integer, Set<ECourseMilestone>>>> picked = getPicked();

            if (!picked.isEmpty()) {
                this.searchBtn.setEnabled(false);

                final DoQuery theQuery = new DoQuery(picked, this.cache, this.activeKey);

                theQuery.addPropertyChangeListener(evt -> {
                    final Integer prog = (Integer) evt.getNewValue();

                    final String propertyName = evt.getPropertyName();
                    if ("progress".equals(propertyName)) {
                        final int progValue = prog.intValue();
                        this.progress.setValue(progValue);
                    }

                    if (theQuery.isDone()) {
                        this.searchBtn.setEnabled(true);
                    }
                });

                theQuery.execute();
            }

        } else if (cmd.startsWith("CHECK_")) {
            handleCheckAction(cmd);
        }
    }

    /**
     * Constructs a map with the user's selections for query.
     *
     * @return a map from pace to track to course index to the set of selected statuses
     */
    private Map<Integer, Map<String, Map<Integer, Set<ECourseMilestone>>>> getPicked() {

        final Map<Integer, Map<String, Map<Integer, Set<ECourseMilestone>>>> result = new HashMap<>(5);

        // Loop through all paces

        for (final Map.Entry<Integer, Map<String, Map<Integer, JCheckBox>>> paceMap : this.frontBoxes.entrySet()) {

            final Integer pace = paceMap.getKey();

            // Loop through all tracks within the pace

            for (final Map.Entry<String, Map<Integer, JCheckBox>> trackMap : paceMap.getValue().entrySet()) {

                final String track = trackMap.getKey();

                // Loop through all course indexes within the pace and track

                for (final Map.Entry<Integer, JCheckBox> indexMap : trackMap.getValue().entrySet()) {
                    if (indexMap.getValue().isSelected()) {
                        final Integer courseIndex = indexMap.getKey();
                        accumulatePicked(pace, track, courseIndex, result);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Accumulates the set of selected milestone statuses for a pace/track/index combination.
     *
     * @param pace        the pace
     * @param track       the pace track
     * @param courseIndex the course index
     * @param result      the map to which to add the result
     */
    private void accumulatePicked(final Integer pace, final String track, final Integer courseIndex,
                                  final Map<? super Integer, Map<String, Map<Integer, Set<ECourseMilestone>>>> result) {

        final Map<String, Map<Integer, Map<ECourseMilestone, JCheckBox>>> paceMap = this.boxes.get(pace);

        final Map<Integer, Map<ECourseMilestone, JCheckBox>> trackMap = paceMap.get(track);

        final Map<ECourseMilestone, JCheckBox> indexMap = trackMap.get(courseIndex);

        for (final Map.Entry<ECourseMilestone, JCheckBox> entry : indexMap.entrySet()) {

            if (entry.getValue().isSelected()) {

                final Map<String, Map<Integer, Set<ECourseMilestone>>> map1
                        = result.computeIfAbsent(pace, object -> new HashMap<>(5));

                final Map<Integer, Set<ECourseMilestone>> map2
                        = map1.computeIfAbsent(track, s -> new HashMap<>(5));

                final Set<ECourseMilestone> set
                        = map2.computeIfAbsent(courseIndex, i -> EnumSet.noneOf(ECourseMilestone.class));

                final ECourseMilestone key = entry.getKey();
                set.add(key);
            }
        }
    }

    /**
     * Handles an action generated by a checkbox.
     *
     * @param cmd the action command
     */
    private void handleCheckAction(final String cmd) {

        final int dot = cmd.indexOf('.');
        final int dash = cmd.lastIndexOf('-');
        if (dot > 0 && dash > dot) {
            try {
                final String preDot = cmd.substring(6, dot);
                final Integer pace = Integer.valueOf(preDot);
                final String track = cmd.substring(dot + 1, dash);
                final String postDash = cmd.substring(dash + 1);
                final Integer index = Integer.valueOf(postDash);

                final Map<String, Map<Integer, JCheckBox>> paceMap1 = this.frontBoxes.get(pace);
                final Map<String, Map<Integer, Map<ECourseMilestone, JCheckBox>>> paceMap2 = this.boxes.get(pace);

                if (paceMap1 == null || paceMap2 == null) {
                    Log.warning("Can't find maps for pace ", pace);
                } else {
                    final Map<Integer, JCheckBox> trackMap1 = paceMap1.get(track);
                    final Map<Integer, Map<ECourseMilestone, JCheckBox>> trackMap2 = paceMap2.get(track);

                    if (trackMap1 == null || trackMap2 == null) {
                        Log.warning("Can't find maps for pace ", pace, " track ", track);
                    } else {
                        final JCheckBox frontBox = trackMap1.get(index);
                        final Map<ECourseMilestone, JCheckBox> milestones = trackMap2.get(index);

                        if (frontBox == null || milestones == null) {
                            Log.warning("Can't find front box / milestones for index ", index);
                        } else {
                            final boolean checked = frontBox.isSelected();
                            for (final JCheckBox chk : milestones.values()) {
                                chk.setEnabled(checked);
                            }
                        }
                    }
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid action command: ", cmd, ex);
            }
        }
    }

    /**
     * A {@code SwingWorker} that performs the query in the background.
     */
    private static final class DoQuery extends SwingWorker<String, Object> {

        /** The picked population. */
        private final Map<Integer, Map<String, Map<Integer, Set<ECourseMilestone>>>> picked;

        /** The data cache. */
        private final Cache cache;

        /** The active term. */
        private final TermKey activeKey;

        /**
         * Constructs a new {@code DoQuery}.
         *
         * @param thePicked    the picked statuses to include (a map from pace to track to course index to a set of
         *                     statuses to include)
         * @param theCache     the data cache
         * @param theActiveKey the active term key
         */
        private DoQuery(final Map<Integer, Map<String, Map<Integer, Set<ECourseMilestone>>>> thePicked,
                        final Cache theCache, final TermKey theActiveKey) {

            super();

            this.picked = thePicked;
            this.cache = theCache;
            this.activeKey = theActiveKey;
        }

        /**
         * Performs the query in the background.
         */
        @Override
        public String doInBackground() {

            // Determine the list of students to examine

            final Map<RawStterm, Map<Integer, Set<ECourseMilestone>>> toProcess = new HashMap<>(1000);

            try {
                final List<RawStterm> stterms = RawSttermLogic.queryAllByTerm(this.cache, this.activeKey);

                // Determine which students to include based on pace and pace/track checkboxes

                for (final Map.Entry<Integer, Map<String, Map<Integer, Set<ECourseMilestone>>>> paceMap : this.picked
                        .entrySet()) {

                    final Integer pace = paceMap.getKey();

                    for (final Map.Entry<String, Map<Integer, Set<ECourseMilestone>>> trackMap : paceMap
                            .getValue().entrySet()) {

                        final String track = trackMap.getKey();

                        for (final RawStterm test : stterms) {
                            if (pace.equals(test.pace) && track.equals(test.paceTrack)) {
                                final Map<Integer, Set<ECourseMilestone>> value = trackMap.getValue();
                                toProcess.put(test, value);
                            }
                        }
                    }
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }

            setProgress(5);

            // For each of those students, see whether they are in the selected

            final int count = toProcess.size();
            int pos = 0;
            for (final Map.Entry<RawStterm, Map<Integer, Set<ECourseMilestone>>> entry : toProcess.entrySet()) {
                final RawStterm key = entry.getKey();
                final Map<Integer, Set<ECourseMilestone>> value = entry.getValue();
                processStudent(key, value);
                setProgress(5 + 95 * pos / count);
                ++pos;
            }

            return CoreConstants.EMPTY;
        }

        /**
         * Processes a single student.
         *
         * @param stterm       the student term record
         * @param milestoneMap a map from course index to set of milestones of interest
         */
        private void processStudent(final RawStterm stterm, final Map<Integer, Set<ECourseMilestone>> milestoneMap) {

            // TODO:
        }

        /**
         * Called when the process completes.
         */
        @Override
        protected void done() {

            setProgress(100);
        }
    }
}
