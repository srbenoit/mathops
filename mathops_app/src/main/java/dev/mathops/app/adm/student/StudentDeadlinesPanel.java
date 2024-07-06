package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.IZTableCommandListener;
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
import dev.mathops.db.old.rawrecord.RawStexam;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A panel that shows student deadlines.
 */
final class StudentDeadlinesPanel extends AdminPanelBase implements ActionListener, IZTableCommandListener<DeadlineListRow> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7434243694703706310L;

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** The data cache. */
    private final Cache cache;

    /** Fixed data. */
    private final FixedData fixed;

    /** The current student data. */
    private StudentData studentData;

    /** The row for which an appeal is being requested. */
    private DeadlineListRow currentRow;

    /** The deadlines table. */
    private final ZTableDeadlines deadlinesTable;

    /** A display for the student's pace. */
    private final JTextField paceDisplay;

    /** A display for the student's pace track. */
    private final JTextField paceTrackDisplay;

    /** An error message. */
    private final JLabel error;

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

    /**
     * Constructs a new {@code StudentDeadlinesPanel}.
     *
     * @param theCache the cache
     * @param theFixed the fixed data container
     */
    StudentDeadlinesPanel(final Cache theCache, final FixedData theFixed) {

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

        this.paceTrackDisplay = makeTextField(2);
        top.add(this.paceTrackDisplay);

        // Left side: Deadlines by registration, with 'appeal' option, if authorized
        final JPanel left = makeOffWhitePanel(new StackedBorderLayout(5, 5));
        left.setBackground(Skin.LIGHTEST);
        add(left, StackedBorderLayout.WEST);

        left.add(makeHeader("Deadlines", false), StackedBorderLayout.NORTH);

        this.deadlinesTable = new ZTableDeadlines(this, allowEdit);
        // this.deadlinesTable.setFillsViewportHeight(true);

        final JScrollPane deadlinesScroll = new JScrollPane(this.deadlinesTable);
        deadlinesScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        deadlinesScroll.getVerticalScrollBar().setUnitIncrement(10);
        deadlinesScroll.getVerticalScrollBar().setBlockIncrement(100);
        left.add(deadlinesScroll, StackedBorderLayout.NORTH);

        this.appealHeading = new JLabel("Appeal for:");
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
        this.applyBtn.addActionListener(this);

        // For some reason, text areas don't get borders by default...
        final Border border = this.interviewerField.getBorder();
        this.circumstancesArea.setBorder(border);
        this.commentsArea.setBorder(border);

        final JButton accommodationNotes = new JButton("Accommodation Notes");

        if (allowEdit) {
            // Center: detail fields for a deadline override
            final JPanel center = makeOffWhitePanel(new StackedBorderLayout(5, 5));
            center.setBackground(Skin.LIGHTEST);
            center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            add(center, StackedBorderLayout.CENTER);

            // Empty header to make spacing match left panel
            center.add(makeHeader(CoreConstants.SPC, false), StackedBorderLayout.NORTH);

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

            center.add(this.appealHeading, StackedBorderLayout.NORTH);

            final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row1.setBackground(Skin.LIGHTEST);
            row1.add(labels[0]);
            row1.add(this.interviewerField);
            center.add(row1, StackedBorderLayout.NORTH);

            final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row2.setBackground(Skin.LIGHTEST);
            row2.add(labels[1]);
            row2.add(this.appealDateField);
            center.add(row2, StackedBorderLayout.NORTH);

            final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row3.setBackground(Skin.LIGHTEST);
            row3.add(labels[2]);
            row3.add(this.reliefGiven);
            center.add(row3, StackedBorderLayout.NORTH);

            final JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row4.setBackground(Skin.LIGHTEST);
            row4.add(labels[3]);
            row4.add(this.newDeadlineField);
            center.add(row4, StackedBorderLayout.NORTH);

            final JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 1));
            row5.setBackground(Skin.LIGHTEST);
            row5.add(labels[4]);
            row5.add(this.nbrAttemptsField);
            center.add(row5, StackedBorderLayout.NORTH);

            final JLabel lbl5 = new JLabel("Circumstances:");
            lbl5.setFont(Skin.MEDIUM_15_FONT);
            center.add(lbl5, StackedBorderLayout.NORTH);
            center.add(this.circumstancesArea, StackedBorderLayout.NORTH);

            final JLabel lbl6 = new JLabel("Comments:");
            lbl6.setFont(Skin.MEDIUM_15_FONT);
            center.add(lbl6, StackedBorderLayout.NORTH);
            center.add(this.commentsArea, StackedBorderLayout.NORTH);

            final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            buttons.setBackground(Skin.LIGHTEST);
            this.applyBtn.setFont(Skin.MEDIUM_15_FONT);
            buttons.add(this.applyBtn);
            center.add(buttons, StackedBorderLayout.NORTH);

            final JPanel buttons2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            buttons2.setBackground(Skin.LIGHTEST);
            accommodationNotes.setFont(Skin.MEDIUM_15_FONT);
            buttons2.add(accommodationNotes);
            left.add(buttons2, StackedBorderLayout.SOUTH);
        }

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

        this.deadlinesTable.clear();
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
            if (stterm.pace == null) {
                this.paceDisplay.setText("?");
            } else {
                this.paceDisplay.setText(stterm.pace.toString());
            }
            this.paceTrackDisplay.setText(stterm.paceTrack);

            final Collection<RawStcourse> currentTermRegs = new ArrayList<>(data.studentCoursesPastAndCurrent);

            // Remove any not in the current term
            currentTermRegs.removeIf(next -> !next.termKey.equals(stterm.termKey));

            // Assign pace order if any regs do not yet have a pace order
            final List<RawStcourse> toassign = new ArrayList<>(currentTermRegs.size());
            final List<Integer> orders = new ArrayList<>(currentTermRegs.size());
            for (int i = 1; i <= currentTermRegs.size(); ++i) {
                orders.add(Integer.valueOf(i));
            }

            for (final RawStcourse reg : currentTermRegs) {
                final Integer order = reg.paceOrder;
                if (order == null) {
                    toassign.add(reg);
                } else if (order.intValue() >= currentTermRegs.size()) {
                    reg.paceOrder = null;
                    toassign.add(reg);
                } else {
                    orders.remove(order);
                }
            }

            if (!toassign.isEmpty()) {
                Collections.sort(toassign);
                for (final RawStcourse row : toassign) {
                    row.paceOrder = orders.removeFirst();
                }
            }
            toassign.clear();
            orders.clear();

            final List<RawStexam> exams = data.studentExams;

            final List<RawMilestone> milestones = data.milestones;
            final List<RawStmilestone> stmilestones = data.studentMilestones;
            final List<RawPaceAppeals> paceAppeals = data.paceAppeals;
            final List<DeadlineListRow> rows = new ArrayList<>(10);

            for (final RawMilestone ms : milestones) {
                RawStmilestone stms = null;
                for (final RawStmilestone test : stmilestones) {
                    if (test.paceTrack.equals(ms.paceTrack) && test.msNbr.equals(ms.msNbr)
                            && test.msType.equals(ms.msType)) {
                        stms = test;
                        break;
                    }
                }

                // Milestone number is [pace][order][unit]
                final int order = ms.msNbr.intValue() / 10 % 10;
                final int unit = ms.msNbr.intValue() % 10;
                final LocalDate newDate = stms == null ? null : stms.msDate;
                final LocalDate effDate = newDate == null ? ms.msDate : newDate;

                String course = null;
                for (final RawStcourse reg : currentTermRegs) {
                    if (reg.paceOrder != null && reg.paceOrder.intValue() == order) {
                        course = reg.course;
                    }
                }

                final String examType;
                if ("FE".equals(ms.msType) || "F1".equals(ms.msType)) {
                    examType = "F";
                } else {
                    examType = "R";
                }

                // See if the exam was completed
                LocalDate earliestCompletion = null;
                Boolean onTime = null;

                for (final RawStexam exam : exams) {
                    if (exam.course.equals(course) && exam.unit.intValue() == unit && exam.examType.equals(examType)
                            && "Y".equals(exam.passed)) {
                        if (earliestCompletion == null || exam.examDt.isBefore(earliestCompletion)) {
                            earliestCompletion = exam.examDt;
                        }
                    }
                }
                if (earliestCompletion != null) {
                    if ("F".equals(examType)) {
                        // If a Passed final is on record, assume it was on time.
                        onTime = Boolean.TRUE;
                    } else {
                        onTime = Boolean.valueOf(!earliestCompletion.isAfter(effDate));
                    }
                }

                RawPaceAppeals appeal = null;
                if (stterm.pace != null && stterm.paceTrack != null) {
                    for (final RawPaceAppeals test : paceAppeals) {
                        if (stterm.pace.equals(test.pace) && stterm.paceTrack.equals(test.paceTrack)
                                && ms.msNbr.equals(test.msNbr) && ms.msType.equals(test.msType)) {
                            appeal = test;
                            break;
                        }
                    }
                }

                rows.add(new DeadlineListRow(course, ms, stms, appeal, earliestCompletion, onTime));
            }

            this.deadlinesTable.setData(rows);
        }
    }

    /**
     * Called when a button is pressed within a row of a table.
     *
     * @param rowIndex the index of the row (where 0 is the first row below the header)
     * @param rowData  the record corresponding to the row
     * @param cmd      the action command associated with the button
     */
    @Override
    public void commandOnRow(final int rowIndex, final DeadlineListRow rowData, final String cmd) {

        if (ZTableDeadlines.CMD_APPEAL.equals(cmd)) {

            this.currentRow = rowData;

            final RawMilestone ms = rowData.milestoneRecord;

            this.appealHeading.setText("Appeal for " + rowData.course + " unit " + ms.getUnit() + ", " + ms.msType);

            this.interviewerField.setEnabled(true);
            this.appealDateField.setEnabled(true);
            this.reliefGiven.setEnabled(true);
            this.newDeadlineField.setEnabled(true);
            this.nbrAttemptsField.setEnabled(true);
            this.circumstancesArea.setEnabled(true);
            this.commentsArea.setEnabled(true);

            if (this.studentData != null && this.studentData.studentTerm != null) {
                this.applyBtn.setEnabled(true);

                this.interviewerField.setText(this.fixed.username);
                final LocalDate today = LocalDate.now();
                this.appealDateField.setText(TemporalUtils.FMT_MDY_COMPACT.format(today));
                final String deadlineStr;
                if (rowData.stmilestoneRecord == null) {
                    deadlineStr = TemporalUtils.FMT_MDY_COMPACT.format(rowData.milestoneRecord.msDate);
                } else {
                    deadlineStr = TemporalUtils.FMT_MDY_COMPACT.format(rowData.stmilestoneRecord.msDate);
                }
                this.newDeadlineField.setText(deadlineStr);
            }

        } else if (ZTableDeadlines.CMD_EDIT.equals(cmd)) {

            this.currentRow = rowData;

            final RawPaceAppeals appeal = rowData.paceAppealRecord;

            if (appeal == null) {
                this.appealHeading.setText("Appeal for:");
            } else {
                this.appealHeading.setText("Appeal for: " + rowData.course + " unit "
                        + rowData.milestoneRecord.getUnit() + " " + rowData.milestoneRecord.msType);

                this.interviewerField.setText(appeal.interviewer);
                this.appealDateField.setText(TemporalUtils.FMT_MDY.format(appeal.appealDt));
                this.reliefGiven.setSelected("Y".equals(appeal.reliefGiven));
                this.newDeadlineField.setText(TemporalUtils.FMT_MDY.format(appeal.newDeadlineDt));
                if (appeal.nbrAtmptsAllow == null) {
                    this.nbrAttemptsField.setText(CoreConstants.EMPTY);
                } else {
                    this.nbrAttemptsField.setText(appeal.nbrAtmptsAllow.toString());
                }
                this.circumstancesArea.setText(appeal.circumstances);
                this.commentsArea.setText(appeal.comment);
                this.applyBtn.setEnabled(true);
            }

            this.interviewerField.setEnabled(appeal != null);
            this.appealDateField.setEnabled(appeal != null);
            this.reliefGiven.setEnabled(appeal != null);
            this.newDeadlineField.setEnabled(appeal != null);
            this.nbrAttemptsField.setEnabled(appeal != null);
            this.circumstancesArea.setEnabled(appeal != null);
            this.commentsArea.setEnabled(appeal != null);
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
            if (this.studentData == null || this.studentData.studentTerm == null || this.currentRow == null) {
                JOptionPane.showMessageDialog(this, "Don't have enough student data to do an appeal...",
                        "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
            } else if (this.reliefGiven.isSelected()) {
                try {
                    applyAppealReliefGiven();
                } catch (final SQLException ex) {
                    Log.warning(ex);
                }
            } else {
                // Just documenting a request or an SDC accommodation
            }
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
            JOptionPane.showMessageDialog(this, "Interviewer field may not be empty.",
                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
        } else if (appealDateStr == null || appealDateStr.isBlank()) {
            JOptionPane.showMessageDialog(this, "Appeal date field may not be empty.",
                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
        } else if (newDeadlineStr == null || newDeadlineStr.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "If relief was given, new deadline date field may not be empty.",
                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
        } else {
            final LocalDate newDate = interpretDate(newDeadlineStr);
            if (newDate == null) {
                JOptionPane.showMessageDialog(this, "Unable to interpret new deadline date",
                        "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
            } else {
                final LocalDate appealDate = interpretDate(appealDateStr);

                if (appealDate == null) {
                    JOptionPane.showMessageDialog(this, "Unable to interpret appeal date",
                            "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                } else {
                    RawPaceAppeals appealRec = null;
                    RawStmilestone stmilestoneRec = null;

                    final TermRec active = this.cache.getSystemData().getActiveTerm();

                    if (attemptsStr == null || attemptsStr.isBlank()) {
                        appealRec = new RawPaceAppeals(active.term,
                                this.studentData.student.stuId, appealDate, "Y", this.studentData.studentTerm.pace,
                                this.studentData.studentTerm.paceTrack, this.currentRow.milestoneRecord.msNbr,
                                this.currentRow.milestoneRecord.msType, this.currentRow.milestoneRecord.msDate,
                                newDate, null, this.circumstancesArea.getText(), this.commentsArea.getText(),
                                interviewer);
                        stmilestoneRec = new RawStmilestone(active.term, this.studentData.student.stuId,
                                this.studentData.studentTerm.paceTrack, this.currentRow.milestoneRecord.msNbr,
                                this.currentRow.milestoneRecord.msType, newDate, null);
                    } else {
                        try {
                            final Integer attempts = Integer.valueOf(attemptsStr);

                            appealRec = new RawPaceAppeals(active.term,
                                    this.studentData.student.stuId, appealDate, "Y", this.studentData.studentTerm.pace,
                                    this.studentData.studentTerm.paceTrack, this.currentRow.milestoneRecord.msNbr,
                                    this.currentRow.milestoneRecord.msType, this.currentRow.milestoneRecord.msDate,
                                    newDate, attempts, this.circumstancesArea.getText(), this.commentsArea.getText(),
                                    interviewer);
                            stmilestoneRec = new RawStmilestone(active.term, this.studentData.student.stuId,
                                    this.studentData.studentTerm.paceTrack, this.currentRow.milestoneRecord.msNbr,
                                    this.currentRow.milestoneRecord.msType, newDate, attempts);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Unable to interpret number of attempts",
                                    "Deadline Appeal", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    if (appealRec != null && stmilestoneRec != null) {
                        try {
                            RawPaceAppealsLogic.INSTANCE.insert(this.cache, appealRec);
                            if (this.currentRow != null && this.currentRow.stmilestoneRecord != null) {
                                this.studentData.studentMilestones.remove(this.currentRow.stmilestoneRecord);
                                RawStmilestoneLogic.INSTANCE.delete(this.cache, this.currentRow.stmilestoneRecord);
                            }
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
     * @param dateString  the date string
     * @return the parsed date; null if unable to interpret
     */
    private static LocalDate interpretDate(final CharSequence dateString) {

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
