package dev.mathops.app.adm.office.registration;

import dev.mathops.app.JDateChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.app.adm.office.ISemesterCalendarPaneListener;
import dev.mathops.app.adm.office.dialogs.DlgAddPaceAppeal;
import dev.mathops.app.adm.office.dialogs.DlgEditPaceAppeal;
import dev.mathops.app.adm.office.dialogs.IPaceAppealsListener;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.svc.term.TermRec;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A panel that shows student deadlines.
 */
public final class CourseDeadlinesPanel extends AdmPanelBase implements ActionListener,
        ISemesterCalendarPaneListener, IPaceAppealsListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7434243694703706310L;

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The data cache. */
    private final Cache cache;

    /** The fixed data. */
    private final UserData fixed;

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

    /** An error message. */
    private final JLabel error;

    /** The panel that contains the appeal entry form. */
    private final JPanel appealForm;

    /** A heading for the appeal. */
    private final JLabel appealHeading;

    /** The "interviewer" field. */
    private final JTextField interviewerField;

    /** The "appeal date" field. */
    private final JTextField appealDateField;

    /** The "relief given" checkbox. */
    private final JCheckBox reliefGiven;

    /** The "new deadline" field. */
    private final JTextField newDeadlineField;

    /** The "# attempts" field. */
    private final JTextField nbrAttemptsField;

    /** The "circumstances" text area. */
    private final JTextArea circumstancesArea;

    /** The "comments" text area. */
    private final JTextArea commentsArea;

    /** The apply button. */
    private final JButton applyBtn;

    /** The cancel button. */
    private final JButton cancelBtn;

    /** The calendar display. */
    final SemesterCalendarPane calendar;

    /** The milestone record currently being edited. */
    private RawStmilestone editMilestone;

    /** The appeal record currently being edited. */
    private RawPaceAppeals editAppeal;

    /** The milestone for which an appeal is being added. */
    private RawMilestone addMilestone;

    /** The dialog to add new pace appeals. */
    private DlgAddPaceAppeal addPaceAppealDialog = null;

    /** The dialog to edit existing pace appeals. */
    private DlgEditPaceAppeal editPaceAppealDialog = null;

    /**
     * Constructs a new {@code StuDeadlinesPanel}.
     *
     * @param theCache the cache
     * @param theFixed the fixed data container
     */
    public CourseDeadlinesPanel(final Cache theCache, final UserData theFixed) {

        super();

        this.cache = theCache;
        this.fixed = theFixed;
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

        this.appealHeading = new JLabel("Appeal for:");
        this.appealHeading.setFont(Skin.MEDIUM_15_FONT);
        this.appealHeading.setForeground(Skin.LABEL_COLOR);

        this.interviewerField = new JTextField(15);
        this.interviewerField.setEnabled(false);
        this.appealDateField = new JTextField(15);
        this.appealDateField.setEnabled(false);
        this.reliefGiven = new JCheckBox();
        this.reliefGiven.setEnabled(false);
        this.newDeadlineField = new JTextField(15);
        this.newDeadlineField.setEnabled(false);
        this.nbrAttemptsField = new JTextField(15);
        this.nbrAttemptsField.setEnabled(false);
        this.circumstancesArea = new JTextArea(3, 20);
        this.circumstancesArea.setEnabled(false);
        this.commentsArea = new JTextArea(3, 20);
        this.commentsArea.setEnabled(false);
        this.applyBtn = new JButton("Apply");
        this.applyBtn.setEnabled(false);
        this.applyBtn.setActionCommand(APPLY_CMD);
        this.applyBtn.setFont(Skin.MEDIUM_15_FONT);
        this.applyBtn.addActionListener(this);
        this.cancelBtn = new JButton("Cancel");
        this.cancelBtn.setActionCommand(CANCEL_CMD);
        this.cancelBtn.setFont(Skin.MEDIUM_15_FONT);
        this.cancelBtn.addActionListener(this);

        // For some reason, text areas don't get borders by default...
        final Border border = this.interviewerField.getBorder();
        this.circumstancesArea.setBorder(border);
        this.commentsArea.setBorder(border);

        this.appealForm = makeOffWhitePanel(new StackedBorderLayout(5, 5));
        this.appealForm.setBackground(Skin.LIGHTEST);
        this.appealForm.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        add(this.appealForm, StackedBorderLayout.WEST);

        if (editAllowed) {
            // Center: detail fields for a deadline override

            final JLabel[] labels = new JLabel[5];
            labels[0] = new JLabel("Interviewer:");
            labels[1] = new JLabel("Appeal Date:");
            labels[2] = new JLabel("Relief Given:");
            labels[3] = new JLabel("New Deadline:");
            labels[4] = new JLabel("# Attempts:");
            int maxW = 0;
            int maxH = 0;
            for (final JLabel lbl : labels) {
                lbl.setFont(Skin.MEDIUM_15_FONT);
                final Dimension pref = lbl.getPreferredSize();
                maxW = Math.max(maxW, pref.width);
                maxH = Math.max(maxH, pref.height);
            }
            final Dimension newPref = new Dimension(maxW, maxH);
            for (final JLabel lbl : labels) {
                lbl.setPreferredSize(newPref);
            }

            this.appealForm.add(this.appealHeading, StackedBorderLayout.NORTH);

            final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row1.setBackground(Skin.LIGHTEST);
            row1.add(labels[0]);
            row1.add(this.interviewerField);
            this.appealForm.add(row1, StackedBorderLayout.NORTH);

            final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row2.setBackground(Skin.LIGHTEST);
            row2.add(labels[1]);
            row2.add(this.appealDateField);
            this.appealForm.add(row2, StackedBorderLayout.NORTH);

            final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row3.setBackground(Skin.LIGHTEST);
            row3.add(labels[2]);
            row3.add(this.reliefGiven);
            this.appealForm.add(row3, StackedBorderLayout.NORTH);

            final JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row4.setBackground(Skin.LIGHTEST);
            row4.add(labels[3]);
            row4.add(this.newDeadlineField);
            this.appealForm.add(row4, StackedBorderLayout.NORTH);

            final JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row5.setBackground(Skin.LIGHTEST);
            row5.add(labels[4]);
            row5.add(this.nbrAttemptsField);
            this.appealForm.add(row5, StackedBorderLayout.NORTH);

            final JLabel lbl5 = new JLabel("Circumstances:");
            lbl5.setFont(Skin.MEDIUM_15_FONT);
            this.appealForm.add(lbl5, StackedBorderLayout.NORTH);
            this.appealForm.add(this.circumstancesArea, StackedBorderLayout.NORTH);

            final JLabel lbl6 = new JLabel("Comments:");
            lbl6.setFont(Skin.MEDIUM_15_FONT);
            this.appealForm.add(lbl6, StackedBorderLayout.NORTH);
            this.appealForm.add(this.commentsArea, StackedBorderLayout.NORTH);

            final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            buttons.setBackground(Skin.LIGHTEST);
            buttons.add(this.applyBtn);
            buttons.add(this.cancelBtn);
            this.appealForm.add(buttons, StackedBorderLayout.NORTH);
        }
        this.appealForm.setVisible(false);

        // Bottom: error message space
        this.error = makeError();
        add(this.error, StackedBorderLayout.SOUTH);
    }

    /**
     * Sets the selected student data.
     *
     * @param data the selected student data
     */
    public void setSelectedStudent(final StudentData data) {

        this.error.setText(CoreConstants.SPC);
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

        clearAndHideForm();
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (APPLY_CMD.equals(cmd)) {
            doApply();
        } else if (CANCEL_CMD.equals(cmd)) {
            doCancel();
        } else if (cmd.startsWith("ADD") && cmd.length() > 5) {
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

        final Integer permission = this.fixed.getClearanceLevel("STU_DLINE");
        final boolean allowEdit = permission != null && permission.intValue() < 3;

        if (allowEdit) {

            final String nbr = cmd.substring(5);

            this.editAppeal = null;
            this.editMilestone = null;
            this.addMilestone = null;

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

                    if (Objects.nonNull(this.currentStudentData)) {
                        if (this.addPaceAppealDialog == null) {
                            this.addPaceAppealDialog = new DlgAddPaceAppeal(this.cache, this, ms);
                        }

                        this.addPaceAppealDialog.populateDisplay(this.fixed, this.currentStudentData);
                        this.addPaceAppealDialog.setVisible(true);
                        this.addPaceAppealDialog.toFront();
                    }

                    this.addMilestone = ms;

                    final int order = (nbrValue / 10) % 10;
                    final int unit = nbrValue % 10;
                    final String typeStr = ms.getTypeString();

                    final RawStcourse reg = this.currentStudentData.pacedRegistrations.get(order - 1);

                    this.appealHeading.setText("New Appeal for: " + reg.course + " Unit " + unit + " " + typeStr);
                    this.interviewerField.setText(this.fixed.username);
                    final LocalDate now = LocalDate.now();
                    final String nowStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(now);
                    this.appealDateField.setText(nowStr);
                    this.reliefGiven.setSelected(false);
                    this.newDeadlineField.setText(CoreConstants.EMPTY);
                    this.nbrAttemptsField.setText(CoreConstants.EMPTY);
                    this.circumstancesArea.setText(CoreConstants.EMPTY);
                    this.commentsArea.setText(CoreConstants.EMPTY);

                    this.reliefGiven.setEnabled(true);
                    this.nbrAttemptsField.setEnabled(true);
                    this.newDeadlineField.setEnabled(true);
                    this.circumstancesArea.setEnabled(true);
                    this.commentsArea.setEnabled(true);
                    this.applyBtn.setEnabled(true);

                    this.appealForm.setVisible(true);
                    this.calendar.setListener(this);

                    invalidate();
                    revalidate();
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

        final Integer permission = this.fixed.getClearanceLevel("STU_DLINE");
        final boolean allowEdit = permission != null && permission.intValue() < 3;

        if (allowEdit) {
            this.editAppeal = null;
            this.editMilestone = null;
            this.addMilestone = null;

            final int dot = cmd.indexOf('.');
            if (dot > 6) {
                final String nbr = cmd.substring(6, dot);
                final String index = cmd.substring(dot + 1);

                try {
                    final int nbrValue = Integer.parseInt(nbr);
                    final int indexValue = Integer.parseInt(index);

                    final String type = cmd.substring(4, 6);

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

                            RawPaceAppeals appeal = null;
                            for (final RawPaceAppeals test : this.currentStudentData.paceAppeals) {
                                if (test.paceTrack.equals(track) && test.msNbr.equals(stms.msNbr)
                                        && test.newDeadlineDt.equals(stms.msDate)
                                        && Objects.equals(test.nbrAtmptsAllow, stms.nbrAtmptsAllow)) {
                                    appeal = test;
                                    break;
                                }
                            }

                            if (appeal == null) {
                                Log.warning("Unable to find pace appeal associated with appeal being edited.");
                            } else {
                                this.editMilestone = stms;
                                this.editAppeal = appeal;

                                final int order = (nbrValue / 10) % 10;
                                final int unit = nbrValue % 10;
                                final String typeStr = ms.getTypeString();

                                final RawStcourse reg = this.currentStudentData.pacedRegistrations.get(order - 1);

                                this.appealHeading.setText("Edit Appeal for: " + reg.course + " Unit " + unit + " " + typeStr);

                                this.interviewerField.setText(appeal.interviewer);
                                final String dtStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(appeal.appealDt);
                                this.appealDateField.setText(dtStr);
                                this.reliefGiven.setSelected("Y".equals(appeal.reliefGiven));

                                if (!stms.msDate.equals(appeal.newDeadlineDt)) {
                                    Log.warning("Date in appeal record does not match date in StMilestone record!");
                                }

                                final String msDtStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(stms.msDate);
                                this.newDeadlineField.setText(msDtStr);
                                this.nbrAttemptsField.setText(stms.nbrAtmptsAllow == null ? CoreConstants.EMPTY :
                                        Integer.toString(stms.nbrAtmptsAllow));
                                this.circumstancesArea.setText(appeal.circumstances == null ? CoreConstants.EMPTY :
                                        appeal.circumstances);
                                this.commentsArea.setText(appeal.comment == null ? CoreConstants.EMPTY :
                                        appeal.comment);

                                this.reliefGiven.setEnabled(true);
                                this.nbrAttemptsField.setEnabled(true);
                                this.newDeadlineField.setEnabled(true);
                                this.circumstancesArea.setEnabled(true);
                                this.commentsArea.setEnabled(true);
                                this.applyBtn.setEnabled(true);

                                this.appealForm.setVisible(true);
                                this.calendar.setListener(this);

                                invalidate();
                                revalidate();
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
     * Performs the "Apply" action.
     */
    private void doApply() {

        if (this.currentStudentData == null || this.currentStudentData.studentTerm == null) {
            JOptionPane.showMessageDialog(this, "Don't have enough student data to do an appeal...",
                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
        } else if (this.editAppeal == null || this.editMilestone == null) {
            doAddApply();
        } else {
            doEditApply();
        }
    }

    /**
     * Performs the "Apply" action when adding a new appeal.
     */
    private void doAddApply() {

        this.newDeadlineField.setBackground(Skin.FIELD_BG);
        this.nbrAttemptsField.setBackground(Skin.FIELD_BG);

        final LocalDate newDate = extractNewDate();

        boolean ok = newDate != null;

        Integer newAttempts = null;

        if (ok) {
            final String attemptsStr = this.nbrAttemptsField.getText();
            if (attemptsStr == null || attemptsStr.isBlank()) {
                if ("F1".equals(this.addMilestone.msType)) {
                    this.nbrAttemptsField.setBackground(Skin.FIELD_ERROR_BG);
                    JOptionPane.showMessageDialog(this,
                            "Number of attempts allowed must be specified for 'Final +1' milestone.",
                            "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                }
            } else {
                try {
                    newAttempts = Integer.valueOf(attemptsStr.trim());
                } catch (final NumberFormatException ex) {
                    this.nbrAttemptsField.setBackground(Skin.FIELD_ERROR_BG);
                    JOptionPane.showMessageDialog(this, "Unable to interpret number of attempts allowed.",
                            "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                }
            }
        }

        TermRec active = null;

        try {
            active = this.cache.getSystemData().getActiveTerm();
        } catch (final SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to query for active term.",
                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
        }

        if (ok) {
            if (active == null) {
                JOptionPane.showMessageDialog(this, "Unable to query active term.",
                        "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
            } else if (this.addMilestone == null) {
                JOptionPane.showMessageDialog(this, "Unable to determine milestone to appeal.",
                        "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
            } else {
                final String newRelief = this.reliefGiven.isSelected() ? "Y" : "N";
                final LocalDate appealDate = LocalDate.now();

                final RawPaceAppeals appealRec = new RawPaceAppeals(active.term,
                        this.currentStudentData.student.stuId, appealDate, newRelief,
                        this.currentStudentData.studentTerm.pace,
                        this.currentStudentData.studentTerm.paceTrack, this.addMilestone.msNbr,
                        this.addMilestone.msType,
                        this.addMilestone.msDate, newDate, newAttempts, this.circumstancesArea.getText(),
                        this.commentsArea.getText(), this.interviewerField.getText());

                final RawStmilestone stmilestoneRec = new RawStmilestone(active.term,
                        this.currentStudentData.student.stuId,
                        this.currentStudentData.studentTerm.paceTrack, this.addMilestone.msNbr,
                        this.addMilestone.msType, newDate, newAttempts);

                try {
                    RawPaceAppealsLogic.INSTANCE.insert(this.cache, appealRec);
                    RawStmilestoneLogic.INSTANCE.insert(this.cache, stmilestoneRec);
                    clearAndHideForm();

                    // Refresh
                    this.currentStudentData.studentMilestones.add(stmilestoneRec);
                    this.currentStudentData.paceAppeals.add(appealRec);
                    populateDisplay(this.currentStudentData);
                } catch (final SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Failed to insert appeal: " + ex.getMessage(),
                            "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Called by the dialog that edits accommodations when an edit is applied.
     */
    @Override
    public void updateAppeals() {

        if (this.currentStudentData != null) {

            Log.info("Updating appeals.");

            this.currentStudentData.updatePaceAppeals(this.cache);

            Log.info("There are now " + this.currentStudentData.paceAppeals.size() + " pace appeals and "
                    + this.currentStudentData.studentMilestones.size() + " student milestones");

            populateDisplay(this.currentStudentData);
        }
    }

    /**
     * Performs the "Apply" action when editing an existing appeal.
     */
    private void doEditApply() {

        this.newDeadlineField.setBackground(Skin.FIELD_BG);
        this.nbrAttemptsField.setBackground(Skin.FIELD_BG);

        final LocalDate newDate = extractNewDate();

        boolean ok = newDate != null;

        Integer newAttempts = null;

        if (ok) {
            final String attemptsStr = this.nbrAttemptsField.getText();
            if (attemptsStr == null || attemptsStr.isBlank()) {
                if ("F1".equals(this.editMilestone.msType)) {
                    this.nbrAttemptsField.setBackground(Skin.FIELD_ERROR_BG);
                    JOptionPane.showMessageDialog(this,
                            "Number of attempts allowed must be specified for a 'Final +1' milestone.",
                            "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                }
            } else {
                try {
                    newAttempts = Integer.valueOf(attemptsStr.trim());
                    if (newAttempts.intValue() < 1) {
                        this.nbrAttemptsField.setBackground(Skin.FIELD_ERROR_BG);
                        JOptionPane.showMessageDialog(this, "Number of attempts must be at least 1.",
                                "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                        ok = false;
                    }
                } catch (final NumberFormatException ex) {
                    this.nbrAttemptsField.setBackground(Skin.FIELD_ERROR_BG);
                    JOptionPane.showMessageDialog(this, "Unable to interpret number of attempts allowed.",
                            "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                }
            }
        }

        if (ok) {
            final String newCircumstances = this.circumstancesArea.getText();
            final String newComment = this.commentsArea.getText();

            if (Objects.equals(newDate, this.editMilestone.msDate)
                    && Objects.equals(newAttempts, this.editMilestone.nbrAtmptsAllow)) {
                Log.warning("Apply on an edit with no changes to STMILESTONE row.");
            } else {
                this.editMilestone.msDate = newDate;
                this.editMilestone.nbrAtmptsAllow = newAttempts;
                try {
                    RawStmilestoneLogic.update(this.cache, this.editMilestone);
                } catch (final SQLException ex) {
                    final String[] msg = {"Failed to update student milestone record: ", ex.getMessage()};
                    JOptionPane.showMessageDialog(this, msg, "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                }
            }

            if (ok) {
                final String newRelief = this.reliefGiven.isSelected() ? "Y" : "N";

                if (Objects.equals(newRelief, this.editAppeal.reliefGiven)
                        && Objects.equals(newDate, this.editAppeal.newDeadlineDt)
                        && Objects.equals(newAttempts, this.editAppeal.nbrAtmptsAllow)
                        && Objects.equals(newCircumstances, this.editAppeal.circumstances)
                        && Objects.equals(newComment, this.editAppeal.comment)) {
                    Log.warning("Apply on an edit with no changes to PACE_APPEALS row.");
                } else {
                    try {
                        this.editAppeal.reliefGiven = newRelief;
                        this.editAppeal.newDeadlineDt = newDate;
                        this.editAppeal.nbrAtmptsAllow = newAttempts;
                        this.editAppeal.circumstances = newCircumstances;
                        this.editAppeal.comment = newComment;

                        RawPaceAppealsLogic.update(this.cache, this.editAppeal);
                    } catch (final SQLException ex) {
                        final String[] msg = {"Failed to update pace appeal record: ", ex.getMessage()};
                        JOptionPane.showMessageDialog(this, msg, "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                        ok = false;
                    }
                }
            }
        }

        if (ok) {
            clearAndHideForm();
            this.populateDisplay(this.currentStudentData);
        }
    }

    /**
     * Extracts the new deadline date from the text field, displaying a warning dialog if the date was not specified or
     * is not valid.
     *
     * @return the extracted date
     */
    private LocalDate extractNewDate() {

        LocalDate newDate = null;

        final String newDateStr = this.newDeadlineField.getText();
        if (newDateStr == null || newDateStr.isBlank()) {
            this.newDeadlineField.setBackground(Skin.FIELD_ERROR_BG);
            if (this.reliefGiven.isSelected()) {
                JOptionPane.showMessageDialog(this, "A deadline is required.", "Deadline Appeal",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "A deadline is required, even if relief was not given.",
                        "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            newDate = JDateChooser.interpretDate(newDateStr.trim());
            if (newDate == null) {
                this.newDeadlineField.setBackground(Skin.FIELD_ERROR_BG);
                JOptionPane.showMessageDialog(this, "Unable to interpret new deadline date.", "Deadline Appeal",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        return newDate;
    }

    /**
     * Performs the "Cancel" action.
     */
    private void doCancel() {

        clearAndHideForm();
    }

    /**
     * Clears and hides the appeal form.
     */
    private void clearAndHideForm() {

        this.editMilestone = null;
        this.editAppeal = null;
        this.addMilestone = null;

        this.appealHeading.setText(CoreConstants.EMPTY);
        this.interviewerField.setText(CoreConstants.EMPTY);
        this.appealDateField.setText(CoreConstants.EMPTY);
        this.reliefGiven.setSelected(false);
        this.nbrAttemptsField.setText(CoreConstants.EMPTY);
        this.circumstancesArea.setText(CoreConstants.EMPTY);
        this.commentsArea.setText(CoreConstants.EMPTY);

        this.reliefGiven.setEnabled(false);
        this.nbrAttemptsField.setEnabled(false);
        this.newDeadlineField.setEnabled(false);
        this.circumstancesArea.setEnabled(false);
        this.commentsArea.setEnabled(false);
        this.applyBtn.setEnabled(false);

        this.appealForm.setVisible(false);
        this.calendar.setListener(null);

        invalidate();
        revalidate();
    }

    /**
     * Called when the user selects a date from the calendar panel.
     *
     * @param date the selected date
     */
    @Override
    public void dateSelected(final LocalDate date) {

        final String dateStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(date);
        this.newDeadlineField.setText(dateStr);
    }
}
