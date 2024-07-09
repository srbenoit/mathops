package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

/**
 * A panel that shows student deadlines.
 */
final class StuDeadlinesPanel extends AdminPanelBase implements ActionListener {

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
    private final FixedData fixed;

    /** The current student data. */
    private StudentData studentData = null;

    /** A display for the student's pace. */
    private final JTextField paceDisplay;

    /** A display for the student's pace track. */
    private final JTextField paceTrackDisplay;

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

    /**
     * Constructs a new {@code StuDeadlinesPanel}.
     *
     * @param theCache the cache
     * @param theFixed the fixed data container
     */
    StuDeadlinesPanel(final Cache theCache, final FixedData theFixed) {

        super();

        this.cache = theCache;
        this.fixed = theFixed;
        setBackground(Skin.LIGHTEST);

        final Integer permission = theFixed.getClearanceLevel("STU_DLINE");
        final boolean allowEdit = permission != null && permission.intValue() < 3;

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

        add(makeHeader("Deadlines", false), StackedBorderLayout.NORTH);

        this.paceTrackDisplay = makeTextField(2);
        top.add(this.paceTrackDisplay);

        this.deadlinesGrid = new DeadlinesGrid();

        // Left side: Deadlines by registration, with 'appeal' option, if authorized

        final JPanel left = new JPanel(new StackedBorderLayout(5, 5));
        left.setBackground(Skin.LIGHTEST);

        final JScrollPane deadlinesScroll = new JScrollPane(this.deadlinesGrid);
        deadlinesScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        deadlinesScroll.getVerticalScrollBar().setUnitIncrement(10);
        deadlinesScroll.getVerticalScrollBar().setBlockIncrement(100);
        left.add(deadlinesScroll, StackedBorderLayout.NORTH);

        add(left, StackedBorderLayout.WEST);

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

        if (allowEdit) {
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
    private void clearDisplay() {

        this.deadlinesGrid.clearDisplay();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        this.studentData = data;

        final RawStterm stterm = data.studentTerm;

        if (stterm != null) {
            this.paceDisplay.setText(stterm.pace == null ? "?" : stterm.pace.toString());
            this.paceTrackDisplay.setText(stterm.paceTrack);
            this.deadlinesGrid.populateDisplay(data, this);
        }
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
            doAdd(cmd);
        } else if (cmd.startsWith("EDIT") && cmd.length() > 6) {
            doEdit(cmd);
        }
    }

    /**
     * Performs the "Apply" action.
     */
    private void doApply() {

        if (this.studentData == null || this.studentData.studentTerm == null) {
            JOptionPane.showMessageDialog(this, "Don't have enough student data to do an appeal...",
                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
        } else if (this.reliefGiven.isSelected()) {
            try {
                applyAppealReliefGiven();
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        } else {
            // TODO: Just documenting a request or an SDC accommodation
        }
    }

    /**
     * Performs the "Cancel" action.
     */
    private void doCancel() {

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

        invalidate();
        revalidate();
    }

    /**
     * Performs the "Add" action.
     *
     * @param cmd the action command, known to have length greater than 5, whose format follows the form "ADDRE432" to
     *            add a RE milestone override for MS number 432
     */
    private void doAdd(final String cmd) {

        final String nbr = cmd.substring(5);

        try {
            final int nbrValue = Integer.parseInt(nbr);

            final String type = cmd.substring(3, 5);
            RawMilestone ms = null;
            for (final RawMilestone test : this.studentData.milestones) {
                if (test.msNbr.intValue() == nbrValue && test.msType.equals(type)) {
                    ms = test;
                    break;
                }
            }

            if (ms != null) {
                final int order = (nbrValue / 10) % 10;
                final int unit = nbrValue % 10;
                final String typeStr = ms.getTypeString();

                final RawStcourse reg = this.studentData.pacedRegistrations.get(order - 1);

                this.appealHeading.setText("Appeal for: " + reg.course + " Unit " + unit + " " + typeStr);
                this.interviewerField.setText(this.fixed.username);
                final LocalDate now = LocalDate.now();
                final String nowStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(now);
                this.appealDateField.setText(nowStr);
                this.reliefGiven.setSelected(false);
                this.newDeadlineField.setText(CoreConstants.EMPTY);
                this.nbrAttemptsField.setText(CoreConstants.EMPTY);
                this.circumstancesArea.setText(CoreConstants.EMPTY);
                this.commentsArea.setText(CoreConstants.EMPTY);
                this.appealForm.setVisible(true);

                this.reliefGiven.setEnabled(true);
                this.nbrAttemptsField.setEnabled(true);
                this.newDeadlineField.setEnabled(true);
                this.circumstancesArea.setEnabled(true);
                this.commentsArea.setEnabled(true);
                this.applyBtn.setEnabled(true);

                invalidate();
                revalidate();
            }
        } catch (final NumberFormatException ex) {
            Log.warning("Invalid milestone number (", nbr, ")", ex);
        }
    }

    /**
     * Performs the "Edit" action.
     *
     * @param cmd the action command, known to have length greater than 5, whose format follows the form "EDITRE432.2"
     *            to edit the [2] index RE milestone override for MS number 432
     */
    private void doEdit(final String cmd) {

        final int dot = cmd.indexOf('.');
        if (dot > 6) {
            final String nbr = cmd.substring(6, dot);
            final String index = cmd.substring(dot + 1);

            try {
                final int nbrValue = Integer.parseInt(nbr);
                final int indexValue = Integer.parseInt(index);

                final String type = cmd.substring(4, 6);

                Log.info("Request to EDIT extension [" + index + "] for MS (" + type + ") ", nbr);

                RawMilestone ms = null;
                for (final RawMilestone test : this.studentData.milestones) {
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
                    for (final RawStmilestone test : this.studentData.studentMilestones) {
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
                        final String track = this.studentData.studentTerm.paceTrack;

                        RawPaceAppeals appeal = null;
                        for (final RawPaceAppeals test : this.studentData.paceAppeals) {
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
                            final int order = (nbrValue / 10) % 10;
                            final int unit = nbrValue % 10;
                            final String typeStr = ms.getTypeString();

                            final RawStcourse reg = this.studentData.pacedRegistrations.get(order - 1);

                            this.appealHeading.setText("Appeal for: " + reg.course + " Unit " + unit + " " + typeStr);

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
                            this.commentsArea.setText(appeal.comment == null ? CoreConstants.EMPTY : appeal.comment);
                            this.appealForm.setVisible(true);

                            this.reliefGiven.setEnabled(true);
                            this.nbrAttemptsField.setEnabled(true);
                            this.newDeadlineField.setEnabled(true);
                            this.circumstancesArea.setEnabled(true);
                            this.commentsArea.setEnabled(true);
                            this.applyBtn.setEnabled(true);

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

    /**
     * Applies relief for an appeal.
     *
     * @throws SQLException if there is an error accessing the database
     */
    private void applyAppealReliefGiven() throws SQLException {

        final String interviewer = this.interviewerField.getText();
        final String appealDateStr = this.appealDateField.getText();
        final String newDeadlineStr = this.newDeadlineField.getText();
        final String attemptsStr = this.nbrAttemptsField.getText();

        if (interviewer == null || interviewer.isBlank()) {
            JOptionPane.showMessageDialog(this, "Interviewer field may not be empty.", "Deadline Appeal",
                    JOptionPane.ERROR_MESSAGE);
        } else if (appealDateStr == null || appealDateStr.isBlank()) {
            JOptionPane.showMessageDialog(this, "Appeal date field may not be empty.", "Deadline Appeal",
                    JOptionPane.ERROR_MESSAGE);
        } else if (newDeadlineStr == null || newDeadlineStr.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "If relief was given, new deadline date field may not be empty.", "Deadline Appeal",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            final LocalDate newDate = interpretDate(newDeadlineStr.trim());
            if (newDate == null) {
                JOptionPane.showMessageDialog(this, "Unable to interpret new deadline date", "Deadline Appeal",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                final LocalDate appealDate = interpretDate(appealDateStr);

                if (appealDate == null) {
                    JOptionPane.showMessageDialog(this, "Unable to interpret appeal date", "Deadline Appeal",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    RawPaceAppeals appealRec = null;
                    RawStmilestone stmilestoneRec = null;

                    final TermRec active = this.cache.getSystemData().getActiveTerm();

                    if (attemptsStr == null || attemptsStr.isBlank()) {
//                        appealRec = new RawPaceAppeals(active.term,
//                                this.studentData.student.stuId, appealDate, "Y", this.studentData.studentTerm.pace,
//                                this.studentData.studentTerm.paceTrack, this.currentRow.milestoneRecord.msNbr,
//                                this.currentRow.milestoneRecord.msType, this.currentRow.milestoneRecord.msDate,
//                                newDate, null, this.circumstancesArea.getText(), this.commentsArea.getText(),
//                                interviewer);
//                        // FIXME: Use correct extension type
//                        stmilestoneRec = new RawStmilestone(active.term, this.studentData.student.stuId,
//                                this.studentData.studentTerm.paceTrack, this.currentRow.milestoneRecord.msNbr,
//                                this.currentRow.milestoneRecord.msType, newDate, null, "ACC");
                    } else {
                        try {
                            final Integer attempts = Integer.valueOf(attemptsStr);

//                            appealRec = new RawPaceAppeals(active.term,
//                                    this.studentData.student.stuId, appealDate, "Y", this.studentData.studentTerm
//                                    .pace,
//                                    this.studentData.studentTerm.paceTrack, this.currentRow.milestoneRecord.msNbr,
//                                    this.currentRow.milestoneRecord.msType, this.currentRow.milestoneRecord.msDate,
//                                    newDate, attempts, this.circumstancesArea.getText(), this.commentsArea.getText(),
//                                    interviewer);
//                            // FIXME: Use correct extension type
//                            stmilestoneRec = new RawStmilestone(active.term, this.studentData.student.stuId,
//                                    this.studentData.studentTerm.paceTrack, this.currentRow.milestoneRecord.msNbr,
//                                    this.currentRow.milestoneRecord.msType, newDate, attempts, "ACC");
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Unable to interpret number of attempts",
                                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    if (appealRec != null && stmilestoneRec != null) {
                        try {
                            RawPaceAppealsLogic.INSTANCE.insert(this.cache, appealRec);
                            RawStmilestoneLogic.INSTANCE.insert(this.cache, stmilestoneRec);

                            this.appealHeading.setText(CoreConstants.EMPTY);
                            this.interviewerField.setText(CoreConstants.EMPTY);
                            this.appealDateField.setText(CoreConstants.EMPTY);
                            this.reliefGiven.setSelected(false);
                            this.newDeadlineField.setText(CoreConstants.EMPTY);
                            this.nbrAttemptsField.setText(CoreConstants.EMPTY);
                            this.circumstancesArea.setText(CoreConstants.EMPTY);
                            this.commentsArea.setText(CoreConstants.EMPTY);
                            this.applyBtn.setEnabled(false);

                            // Refresh
                            this.studentData.studentMilestones.add(stmilestoneRec);
                            populateDisplay(this.studentData);
                        } catch (final SQLException ex) {
                            JOptionPane.showMessageDialog(this, "Failed to insert appeal: " + ex.getMessage(),
                                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Attempts to interpret a date string
     *
     * @param dateString the date string
     * @return the parsed date; null if unable to interpret
     */
    private static LocalDate interpretDate(final String dateString) {

        LocalDate date = null;
        TemporalAccessor newDate = null;

        try {
            newDate = TemporalUtils.FMT_MDY.parse(dateString);
        } catch (final DateTimeParseException ex1) {
            try {
                newDate = TemporalUtils.FMT_MDY_COMPACT.parse(dateString);
            } catch (final DateTimeParseException ex2) {
                try {
                    newDate = TemporalUtils.FMT_INFORMIX.parse(dateString);
                } catch (final DateTimeParseException ex3) {
                    if (dateString.length() == 6) {
                        // Try Informix format MMDDYY, like 123199
                        try {
                            final int value = Integer.parseInt(dateString);
                            final int month = value / 10000;
                            final int day = (value / 100) % 100;
                            final int year = 2000 + (value % 100);

                            newDate = LocalDate.of(year, month, day);
                        } catch (final NumberFormatException | DateTimeException ex) {
                            // No action
                        }
                    }
                }
            }
        }

        if (newDate != null) {
            final int day = newDate.get(ChronoField.DAY_OF_MONTH);
            final int month = newDate.get(ChronoField.MONTH_OF_YEAR);
            final int year = newDate.get(ChronoField.YEAR);
            date = LocalDate.of(year, month, day);
        }

        return date;
    }
}
