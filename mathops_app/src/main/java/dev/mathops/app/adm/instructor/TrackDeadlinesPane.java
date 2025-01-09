package dev.mathops.app.adm.instructor;

import dev.mathops.app.JDateChooser;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCampusCalendarLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.mathops.app.adm.AdmPanelBase.makeLabelMedium;

/**
 * A panel to display milestones for a pace track.
 */
final class TrackDeadlinesPane extends JPanel implements ActionListener {

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** The data cache. */
    private final Cache cache;

    /** The list of holidays. */
    private final List<LocalDate> holidays;

    /** The original milestones, to allow up to detect which ones the user has changed. */
    private final Map<Integer, RawMilestone> original;

    /** The date choosers associated with each milestone. */
    private final Map<Integer, JDateChooser> choosers;

    /** The "changed" label associated with each milestone. */
    private final Map<Integer, JLabel> changedLabel;

    /**
     * Constructs a new {@code TrackDeadlinesPane}.
     *
     * @param theCache the cache
     * @param thePace  the pace
     * @param theTrack the track
     */
    TrackDeadlinesPane(final Cache theCache, final int thePace, final String theTrack) {

        super(new StackedBorderLayout());

        this.cache = theCache;

        this.holidays = new ArrayList<>(10);
        this.original = new HashMap<>(30);
        this.choosers = new HashMap<>(30);
        this.changedLabel = new HashMap<>(30);

        setBackground(Skin.WHITE);
        final Border etched = BorderFactory.createEtchedBorder();
        setBorder(etched);

        try {
            final TermRec active = TermLogic.get(theCache).queryActive(theCache);
            if (active == null) {
                final JPanel errorFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 9, 6));
                errorFlow.setBackground(Skin.WHITE);
                final JLabel errorLbl = new JLabel("Unable to look up the active term.");
                errorFlow.add(errorLbl);
                add(errorFlow, StackedBorderLayout.NORTH);
            } else {

                final List<RawCampusCalendar> calenderRows = RawCampusCalendarLogic.queryAll(theCache);
                for (final RawCampusCalendar cal : calenderRows) {
                    if (RawCampusCalendar.DT_DESC_HOLIDAY.equals(cal.dtDesc)) {
                        this.holidays.add(cal.campusDt);
                    }
                }

                final List<RawMilestone> milestones = RawMilestoneLogic.getAllMilestones(theCache, active.term, thePace,
                        theTrack);
                milestones.sort(null);

                final JPanel north = new JPanel(new StackedBorderLayout());
                add(north, StackedBorderLayout.NORTH);

                final String headingStr = thePace + "-course Pace, Track " + theTrack;
                final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));

                final JLabel headingLbl = makeLabelMedium(headingStr);
                flow.add(headingLbl);
                north.add(flow, StackedBorderLayout.WEST);

                final JButton applyBtn = new JButton("Apply Changes");
                applyBtn.setActionCommand(APPLY_CMD);
                applyBtn.addActionListener(this);
                final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
                buttonFlow.add(applyBtn);
                north.add(buttonFlow, StackedBorderLayout.EAST);

                buildPanelContent(milestones);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            final JPanel errorFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 9, 6));
            errorFlow.setBackground(Skin.WHITE);
            final String exMsg = ex.getLocalizedMessage();
            final JLabel errorLbl = new JLabel("Failed to query data: " + exMsg);
            errorFlow.add(errorLbl);
            add(errorFlow, StackedBorderLayout.NORTH);
        }
    }

    /**
     * Builds the panel content based on a list of active (non-dropped) registrations.
     *
     * @param milestones the list of milestones
     */
    private void buildPanelContent(final List<RawMilestone> milestones) {

        final JPanel center = new JPanel(new StackedBorderLayout());
        center.setBackground(Skin.WHITE);

        final JLabel[] labels = new JLabel[6];

        int currentCourse = -1;

        final Border topLine = BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.MEDIUM);

        for (final RawMilestone ms : milestones) {

            this.original.put(ms.msNbr, ms);

            final int course = ms.getIndex();
            if (course != currentCourse) {
                final String courseStr = "Course " + course;
                final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 6));
                flow.setBackground(Skin.WHITE);
                flow.setBorder(topLine);

                final JLabel courseLabel = makeLabelMedium(courseStr);
                flow.add(courseLabel);
                center.add(flow, StackedBorderLayout.NORTH);

                currentCourse = course;

                labels[0] = new JLabel("Unit 1 Review:");
                labels[1] = new JLabel("Unit 2 Review:");
                labels[2] = new JLabel("Unit 3 Review:");
                labels[3] = new JLabel("Unit 4 Review:");
                labels[4] = new JLabel("Final Exam:");
                labels[5] = new JLabel("Final Exam +1:");

                UIUtilities.makeLabelsSameSizeRightAligned(labels);
            }

            final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 3));
            flow.setBackground(Skin.WHITE);

            final Font font = getFont();
            final JDateChooser dateChooser = new JDateChooser(ms.msDate, this.holidays, font);
            dateChooser.setActionCommand(ms.msNbr.toString());
            dateChooser.addActionListener(this);

            this.choosers.put(ms.msNbr, dateChooser);

            if ("RE".equals(ms.msType)) {
                final int unit = ms.getUnit();
                if (unit >= 1 && unit <= 4) {
                    flow.add(labels[unit - 1]);
                }
                flow.add(dateChooser);
            } else if ("FE".equals(ms.msType)) {
                flow.add(labels[4]);
                flow.add(dateChooser);
            } else if ("F1".equals(ms.msType)) {
                flow.add(labels[5]);
                flow.add(dateChooser);

                final int tries = ms.nbrAtmptsAllow == null ? 0 : ms.nbrAtmptsAllow.intValue();
                if (tries == 1) {
                    final JLabel triesLabel = new JLabel("(1 attempt allowed)");
                    flow.add(triesLabel);
                } else {
                    final String triesStr = Integer.toString(tries);
                    final JLabel triesLabel = new JLabel("(" + triesStr + " attempts allowed)");
                    flow.add(triesLabel);
                }
            }

            final JLabel changed = new JLabel(CoreConstants.SPC);
            changed.setForeground(Skin.ERROR_COLOR);
            this.changedLabel.put(ms.msNbr, changed);

            flow.add(changed);

            center.add(flow, StackedBorderLayout.NORTH);
        }

        final JScrollPane scroll = new JScrollPane(center);
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        add(scroll, StackedBorderLayout.CENTER);
    }

    /**
     * Called when a date chooser changes a date.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (APPLY_CMD.equals(cmd)) {
            doApply();
        } else {
            try {
                final Integer msNbr = Integer.valueOf(cmd);
                final JDateChooser chooser = this.choosers.get(msNbr);
                final RawMilestone orig = this.original.get(msNbr);
                final JLabel changeLabel = this.changedLabel.get(msNbr);

                if (chooser != null && orig != null && changeLabel != null) {
                    if (chooser.getCurrentDate().equals(orig.msDate)) {
                        changeLabel.setText(CoreConstants.SPC);
                    } else {
                        final String origDate = TemporalUtils.FMT_MDY.format(orig.msDate);
                        changeLabel.setText("(changed from " + origDate + ")");
                    }
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Unable to interpret action command: ", cmd);
            }
        }
    }

    /**
     * Performs the "apply" operation...
     */
    private void doApply() {

        for (final Map.Entry<Integer, RawMilestone> entry : this.original.entrySet()) {
            final Integer msNbr = entry.getKey();
            final RawMilestone origValue = entry.getValue();

            final JDateChooser dateChooser = this.choosers.get(msNbr);
            if (dateChooser != null) {
                final LocalDate newDate = dateChooser.getCurrentDate();
                if (!newDate.equals(origValue.msDate)) {
                    Log.info("Updating milestone ", msNbr);

                    try {
                        if (RawMilestoneLogic.updateMsDate(this.cache, origValue, newDate)) {
                            origValue.msDate = newDate;
                            final JLabel lbl = this.changedLabel.get(msNbr);
                            if (lbl != null) {
                                lbl.setText(CoreConstants.SPC);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Unable to update milestone date.",
                                    "Update Milestone Date", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                        final String[] msg = {"Unable to update milestone date.", ex.getLocalizedMessage()};
                        JOptionPane.showMessageDialog(this, msg, "Update Milestone Date", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}