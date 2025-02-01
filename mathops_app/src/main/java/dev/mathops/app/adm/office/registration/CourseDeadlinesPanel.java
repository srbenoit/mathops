package dev.mathops.app.adm.office.registration;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.app.adm.office.student.IAppealsListener;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawMilestoneAppeal;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.rec.StandardMilestoneRec;
import dev.mathops.db.rec.StudentStandardMilestoneRec;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Objects;

/**
 * A panel that shows student deadlines.
 */
public final class CourseDeadlinesPanel extends AdmPanelBase implements ActionListener, IAppealsListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7434243694703706310L;

    /** The data cache. */
    private final Cache cache;

    /** The fixed data. */
    private final UserData userData;

    /** The current student data. */
    private StudentData currentStudentData = null;

    /** A display for the student's pace. */
    private final JTextField paceDisplay;

    /** A display for the student's pace track. */
    private final JTextField paceTrackDisplay;

    /** A display for the student's number of accommodation extension days. */
    private final JTextField extDaysDisplay;

    /** A panel that will be populated with deadlines. */
    private final DeadlinesGrid deadlinesGrid;

    /** The calendar display. */
    private final SemesterCalendarPane calendar;

    /** The milestone record currently being edited. */
    private RawStmilestone editMilestone = null;

    /** The standard milestone record currently being edited. */
    private StandardMilestoneRec editStdMilestone = null;

    /** The pace appeal record currently being edited. */
    private RawPaceAppeals editPaceAppeal = null;

    /** The milestone appeal record currently being edited. */
    private RawMilestoneAppeal editMilestoneAppeal = null;

    /** The milestone for which an appeal is being added. */
    private RawMilestone addMilestone = null;

    /** The standard milestone for which an appeal is being added. */
    private StandardMilestoneRec addStdMilestone = null;

    /** The dialog to add new milestone appeals. */
    private DlgAddLegacyMilestoneAppeal addMilestoneAppealDialog = null;

    /** The dialog to edit an existing legacy milestone appeal. */
    private DlgEditLegacyMilestoneAppeal editLegacyMilestoneAppealDialog = null;

    /** The dialog to edit an existing standard milestone appeal. */
    private DlgEditStandardMilestoneAppeal editStandardMilestoneAppealDialog = null;

    /**
     * Constructs a new {@code StuDeadlinesPanel}.
     *
     * @param theCache the cache
     * @param theFixed the fixed data container
     */
    public CourseDeadlinesPanel(final Cache theCache, final UserData theFixed) {

        super();

        this.cache = theCache;
        this.userData = theFixed;
        setBackground(Skin.LIGHTEST);

        final Integer permission = theFixed.getClearanceLevel("STU_DLINE");
        final boolean editAllowed = permission != null && permission.intValue() < 3;

        // Top - student's pace and pace track
        final JPanel top = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        top.setBackground(Skin.LIGHTEST);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));
        add(top, StackedBorderLayout.NORTH);

        final JLabel paceHeader = makeLabel("Pace:");
        paceHeader.setFont(Skin.MEDIUM_15_FONT);
        top.add(paceHeader);

        this.paceDisplay = makeTextField(2);
        top.add(this.paceDisplay);

        final JLabel trackHeader = makeLabel("Pace Track:");
        trackHeader.setFont(Skin.MEDIUM_15_FONT);
        trackHeader.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        top.add(trackHeader);

        this.paceTrackDisplay = makeTextField(2);
        top.add(this.paceTrackDisplay);

        final JLabel sdcDaysHeader = makeLabel("SDC Extension Days:");
        sdcDaysHeader.setFont(Skin.MEDIUM_15_FONT);
        sdcDaysHeader.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        top.add(sdcDaysHeader);

        this.extDaysDisplay = makeTextField(2);
        top.add(this.extDaysDisplay);

        add(makeHeader("Deadlines", false), StackedBorderLayout.NORTH);

        this.deadlinesGrid = new DeadlinesGrid(editAllowed);

        // Left side: Deadlines by registration, with 'appeal' option, if authorized

        final JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBackground(Skin.LIGHTEST);

        final JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setBackground(Skin.LIGHTEST);
        inner.add(this.deadlinesGrid, BorderLayout.NORTH);

        final JScrollPane deadlinesScroll = new JScrollPane(inner);
        deadlinesScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        deadlinesScroll.getVerticalScrollBar().setUnitIncrement(10);
        deadlinesScroll.getVerticalScrollBar().setBlockIncrement(100);
        left.add(deadlinesScroll, StackedBorderLayout.CENTER);

        add(left, StackedBorderLayout.WEST);

        this.calendar = new SemesterCalendarPane(this.cache);
        add(this.calendar, StackedBorderLayout.WEST);
    }

    /**
     * Sets the selected student data.
     *
     * @param data the selected student data
     */
    public void setSelectedStudent(final StudentData data) {

        clearDisplay();

        if (data != null) {
            populateDisplay(data);
        }
    }

    /**
     * Clears all displayed fields.
     */
    public void clearDisplay() {

        this.deadlinesGrid.clearDisplay();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        this.calendar.initialize();

        this.currentStudentData = data;

        final RawStterm stterm = data.studentTerm;

        if (data.pacedRegistrations.isEmpty()) {
            this.deadlinesGrid.indicateNoCourses();
        } else {
            if (data.student.extensionDays == null) {
                this.extDaysDisplay.setText(CoreConstants.EMPTY);
            } else {
                this.extDaysDisplay.setText(data.student.extensionDays.toString());
            }

            if (stterm == null) {
                this.deadlinesGrid.clearDisplay();
            } else {
                this.paceDisplay.setText(stterm.pace == null ? "?" : stterm.pace.toString());
                this.paceTrackDisplay.setText(stterm.paceTrack);

                this.deadlinesGrid.populateDisplay(data, this);
            }
        }

        this.deadlinesGrid.invalidate();
        this.deadlinesGrid.revalidate();
        repaint();
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (cmd.startsWith("ADD") && cmd.length() > 5) {
            initiateAdd(cmd);
        } else if (cmd.startsWith("EDIT") && cmd.length() > 6) {
            initiateEdit(cmd);
        }
    }

    /**
     * Performs the "Add" action.
     *
     * @param cmd the action command, known to have length greater than 5, whose format follows the form "ADDRE432" to
     *            add a RE milestone override for MS number 432
     */
    private void initiateAdd(final String cmd) {

        final Integer permission = this.userData.getClearanceLevel("STU_DLINE");
        final boolean allowEdit = permission != null && permission.intValue() < 3;

        if (allowEdit) {
            final String nbr = cmd.substring(5);

            this.editPaceAppeal = null;
            this.editMilestoneAppeal = null;
            this.editMilestone = null;
            this.editStdMilestone = null;
            this.addMilestone = null;
            this.addStdMilestone = null;

            try {
                final int nbrValue = Integer.parseInt(nbr);

                final String type = cmd.substring(3, 5);
                RawMilestone ms = null;
                for (final RawMilestone test : this.currentStudentData.milestones) {
                    if (test.msNbr.intValue() == nbrValue && test.msType.equals(type)) {
                        ms = test;
                        break;
                    }
                }

                if (ms != null) {
                    if (this.addMilestoneAppealDialog == null) {
                        this.addMilestoneAppealDialog = new DlgAddLegacyMilestoneAppeal(this.cache, this);
                    }

                    Log.info("Adding appeal for ", ms);

                    this.addMilestoneAppealDialog.populateDisplay(this.userData, this.currentStudentData, ms);

                    final Point loc = getLocationOnScreen();
                    final Dimension size = getSize();
                    final int cx = loc.x + size.width / 2;
                    final int cy = loc.y + size.height / 2;
                    final Dimension dialogSize = this.addMilestoneAppealDialog.getSize();

                    this.addMilestoneAppealDialog.setLocation(cx - dialogSize.width / 2, cy - dialogSize.height / 2);
                    this.addMilestoneAppealDialog.setVisible(true);
                    this.addMilestoneAppealDialog.toFront();

                    this.addMilestone = ms;
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid milestone number (", nbr, ")", ex);
            }
        }
    }

    /**
     * Performs the "Edit" action.
     *
     * @param cmd the action command, known to have length greater than 5, whose format follows the form "EDITRE432.2"
     *            to edit the [2] index RE milestone override for MS number 432
     */
    private void initiateEdit(final String cmd) {

        final Integer permission = this.userData.getClearanceLevel("STU_DLINE");
        final boolean allowEdit = permission != null && permission.intValue() < 3;

        if (allowEdit) {
            this.editPaceAppeal = null;
            this.editMilestoneAppeal = null;
            this.editMilestone = null;
            this.editStdMilestone = null;
            this.addMilestone = null;
            this.addStdMilestone = null;

            final int dot = cmd.indexOf('.');
            if (dot > 6) {
                final String nbr = cmd.substring(6, dot);
                final String index = cmd.substring(dot + 1);

                try {
                    final int nbrValue = Integer.parseInt(nbr);
                    final int indexValue = Integer.parseInt(index);

                    final String type = cmd.substring(4, 6);

                    Log.info("Edit of number ", nbrValue, " index ", indexValue, " type ", type);

                    RawMilestone ms = null;
                    for (final RawMilestone test : this.currentStudentData.milestones) {
                        if (test.msNbr.intValue() == nbrValue && test.msType.equals(type)) {
                            ms = test;
                            break;
                        }
                    }

                    if (ms == null) {
                        Log.warning("Unable to find milestone associated with appeal being edited.");
                    } else {
                        RawStmilestone stms = null;
                        int i = 0;
                        for (final RawStmilestone test : this.currentStudentData.studentMilestones) {
                            if (test.msNbr.intValue() == nbrValue && test.msType.equals(type)) {
                                if (i == indexValue) {
                                    stms = test;
                                    break;
                                }
                                ++i;
                            }
                        }

                        if (stms == null) {
                            Log.warning("Unable to find student milestone associated with appeal being edited.");
                        } else {
                            final String track = this.currentStudentData.studentTerm.paceTrack;

                            RawPaceAppeals paceAppeal = null;
                            for (final RawPaceAppeals test : this.currentStudentData.paceAppeals) {
                                if (Objects.equals(test.paceTrack, track) && test.msNbr.equals(stms.msNbr)
                                    && Objects.equals(test.newDeadlineDt, stms.msDate)
                                    && Objects.equals(test.nbrAtmptsAllow, stms.nbrAtmptsAllow)) {
                                    paceAppeal = test;
                                    break;
                                }
                            }

                            if (paceAppeal == null) {

                                RawMilestoneAppeal msAppeal = null;
                                for (final RawMilestoneAppeal test : this.currentStudentData.milestoneAppeals) {
                                    if (Objects.equals(test.paceTrack, track) && test.msNbr.equals(stms.msNbr)
                                        && Objects.equals(test.newMsDt, stms.msDate)
                                        && Objects.equals(test.attemptsAllowed, stms.nbrAtmptsAllow)) {
                                        msAppeal = test;
                                        break;
                                    }
                                }

                                if (msAppeal == null) {
                                    Log.warning("Unable to find pace or milestone appeal associated with appeal being ",
                                            "edited.");
                                } else {
                                    final int number = msAppeal.msNbr.intValue();

                                    if (number > 999) {
                                        // This is a standards-based milestone (pace/course/unit/objective)
                                    } else {
                                        // This is a legacy milestone (pace/course/unit)
                                        showLegacyEditDialog(stms, msAppeal);
                                    }
                                }
                            } else {
                                final int number = paceAppeal.msNbr.intValue();

                                if (number > 999) {
                                    // This is a standards-based milestone (pace/course/unit/objective)
                                } else {
                                    // This is a legacy milestone (pace/course/unit)
                                    showLegacyEditDialog(stms, paceAppeal);
                                }
                            }
                        }
                    }
                } catch (final NumberFormatException ex) {
                    Log.warning("Invalid milestone number (", nbr, ")", ex);
                }
            } else {
                Log.warning("Invalid milestone request (", cmd, ")");
            }
        }
    }

    /**
     * Presents the dialog to edit an existing pace appeal and extension in a legacy course.
     *
     * @param stms       the student milestone record
     * @param paceAppeal the pace appeal record
     */
    private void showLegacyEditDialog(final RawStmilestone stms, final RawPaceAppeals paceAppeal) {

        // This is a legacy milestone (pace/course/unit)
        if (this.editLegacyMilestoneAppealDialog == null) {
            this.editLegacyMilestoneAppealDialog = new DlgEditLegacyMilestoneAppeal(this.cache, this);
        }

        this.editLegacyMilestoneAppealDialog.populateDisplay(this.userData, this.currentStudentData, paceAppeal, stms);

        final Point loc = getLocationOnScreen();
        final Dimension size = getSize();
        final int cx = loc.x + size.width / 2;
        final int cy = loc.y + size.height / 2;
        final Dimension dialogSize = this.editLegacyMilestoneAppealDialog.getSize();

        this.editLegacyMilestoneAppealDialog.setLocation(cx - dialogSize.width / 2, cy - dialogSize.height / 2);
        this.editLegacyMilestoneAppealDialog.setVisible(true);
        this.editLegacyMilestoneAppealDialog.toFront();

        this.editMilestone = stms;
        this.editMilestoneAppeal = null;
        this.editPaceAppeal = paceAppeal;
    }

    /**
     * Presents the dialog to edit an existing milestone appeal and extension in a legacy course.
     *
     * @param stms     the student milestone record
     * @param msAppeal the milestone appeal record
     */
    private void showLegacyEditDialog(final RawStmilestone stms, final RawMilestoneAppeal msAppeal) {

        // This is a legacy milestone (pace/course/unit)
        if (this.editLegacyMilestoneAppealDialog == null) {
            this.editLegacyMilestoneAppealDialog = new DlgEditLegacyMilestoneAppeal(this.cache, this);
        }

        this.editLegacyMilestoneAppealDialog.populateDisplay(this.userData, this.currentStudentData, msAppeal, stms);

        final Point loc = getLocationOnScreen();
        final Dimension size = getSize();
        final int cx = loc.x + size.width / 2;
        final int cy = loc.y + size.height / 2;
        final Dimension dialogSize = this.editLegacyMilestoneAppealDialog.getSize();

        this.editLegacyMilestoneAppealDialog.setLocation(cx - dialogSize.width / 2,
                cy - dialogSize.height / 2);
        this.editLegacyMilestoneAppealDialog.setVisible(true);
        this.editLegacyMilestoneAppealDialog.toFront();

        this.addMilestone = null;
        this.addStdMilestone = null;
        this.editMilestone = stms;
        this.editStdMilestone = null;
        this.editMilestoneAppeal = msAppeal;
        this.editPaceAppeal = null;
    }

    /**
     * Presents the dialog to edit an existing pace appeal and extension in a legacy course.
     *
     * @param ms         the milestone record
     * @param stms       the student milestone record
     * @param paceAppeal the pace appeal record
     */
    private void showStandardEditDialog(final StandardMilestoneRec ms, final StudentStandardMilestoneRec stms,
                                        final RawPaceAppeals paceAppeal) {

        // This is a legacy milestone (pace/course/unit)
        if (this.editStandardMilestoneAppealDialog == null) {
            this.editStandardMilestoneAppealDialog = new DlgEditStandardMilestoneAppeal(this.cache, this);
        }

        this.editStandardMilestoneAppealDialog.populateDisplay(this.userData,
                this.currentStudentData, ms, stms, paceAppeal);

        final Point loc = getLocationOnScreen();
        final Dimension size = getSize();
        final int cx = loc.x + size.width / 2;
        final int cy = loc.y + size.height / 2;
        final Dimension dialogSize = this.editStandardMilestoneAppealDialog.getSize();

        this.editStandardMilestoneAppealDialog.setLocation(cx - dialogSize.width / 2,
                cy - dialogSize.height / 2);
        this.editStandardMilestoneAppealDialog.setVisible(true);
        this.editStandardMilestoneAppealDialog.toFront();

        this.addMilestone = null;
        this.addStdMilestone = null;
        this.editMilestone = null;
        this.editStdMilestone = ms;
        this.editMilestoneAppeal = null;
        this.editPaceAppeal = paceAppeal;
    }

    /**
     * Presents the dialog to edit an existing milestone appeal and extension in a legacy course.
     *
     * @param ms       the milestone record
     * @param stms     the student milestone record
     * @param msAppeal the milestone appeal record
     */
    private void showStandardEditDialog(final StandardMilestoneRec ms, final StudentStandardMilestoneRec stms,
                                        final RawMilestoneAppeal msAppeal) {

        // This is a legacy milestone (pace/course/unit)
        if (this.editStandardMilestoneAppealDialog == null) {
            this.editStandardMilestoneAppealDialog = new DlgEditStandardMilestoneAppeal(this.cache, this);
        }

        this.editStandardMilestoneAppealDialog.populateDisplay(this.userData, this.currentStudentData, ms, stms,
                msAppeal);

        final Point loc = getLocationOnScreen();
        final Dimension size = getSize();
        final int cx = loc.x + size.width / 2;
        final int cy = loc.y + size.height / 2;
        final Dimension dialogSize = this.editStandardMilestoneAppealDialog.getSize();

        this.editStandardMilestoneAppealDialog.setLocation(cx - dialogSize.width / 2, cy - dialogSize.height / 2);
        this.editStandardMilestoneAppealDialog.setVisible(true);
        this.editStandardMilestoneAppealDialog.toFront();

        this.addMilestone = null;
        this.addStdMilestone = null;
        this.editMilestone = null;
        this.editStdMilestone = ms;
        this.editMilestoneAppeal = msAppeal;
        this.editPaceAppeal = null;
    }

    /**
     * Called by the dialog that edits accommodations when an edit is applied.
     */
    @Override
    public void updateAppeals() {

        if (this.currentStudentData != null) {
            this.currentStudentData.updatePaceAppeals(this.cache);
            populateDisplay(this.currentStudentData);
        }
    }
}
