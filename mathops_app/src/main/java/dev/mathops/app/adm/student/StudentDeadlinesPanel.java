package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.rawrecord.RawMilestone;
import dev.mathops.db.rawrecord.RawPaceAppeals;
import dev.mathops.db.rawrecord.RawStcourse;
import dev.mathops.db.rawrecord.RawStexam;
import dev.mathops.db.rawrecord.RawStmilestone;
import dev.mathops.db.rawrecord.RawStterm;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A panel that shows student deadlines.
 */
/* default */ final class StudentDeadlinesPanel extends AdminPanelBase
        implements IZTableCommandListener<DeadlineListRow> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7434243694703706310L;

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

    /** The "Apply" button. */
    private final JButton applyBtn;

    /** The "Accommodation Notes" button. */
    private final JButton accommodationNotes;

    /**
     * Constructs a new {@code StudentDeadlinesPanel}.
     *
     * @param fixed            the fixed data container
     */
    StudentDeadlinesPanel(final FixedData fixed) {

        super();

        setBackground(Skin.LIGHTEST);

        final Integer permission = fixed.getClearanceLevel("STU_DLINE");
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
        this.accommodationNotes = new JButton("Accommodation Notes");

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
            this.accommodationNotes.setFont(Skin.MEDIUM_15_FONT);
            buttons2.add(this.accommodationNotes);
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
                    row.paceOrder = orders.remove(0);
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
                if ("F1".equals(ms.msType)) {
                    continue;
                }

                RawStmilestone stms = null;
                for (final RawStmilestone test : stmilestones) {
                    if (test.paceTrack.equals(ms.paceTrack) && test.msNbr.equals(ms.msNbr)
                            && test.msType.equals(ms.msType)) {
                        stms = test;
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

            final RawMilestone ms = rowData.milestoneRecord;

            this.appealHeading.setText("Appeal for " + rowData.course + " unit " + ms.getUnit() + ", " + ms.msType);

            this.interviewerField.setEnabled(true);
            this.appealDateField.setEnabled(true);
            this.reliefGiven.setEnabled(true);
            this.newDeadlineField.setEnabled(true);
            this.nbrAttemptsField.setEnabled(true);
            this.circumstancesArea.setEnabled(true);
            this.commentsArea.setEnabled(true);

        } else if (ZTableDeadlines.CMD_EDIT.equals(cmd)) {

            final RawPaceAppeals appeal = rowData.paceAppealRecord;

            if (appeal == null) {
                this.appealHeading.setText("Appeal for:");
            } else {
                this.appealHeading.setText("Appeal for: ?");

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
}
