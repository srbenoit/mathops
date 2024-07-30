package dev.mathops.app.adm.office.registration;

import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A panel that presents student deadlines in a grid.
 */
final class DeadlinesGrid extends JPanel {

    /** The layout. */
    private final GroupLayout layout;

    /** True if the current user is allowed to edit deadlines. */
    private final boolean editAllowed;

    /**
     * Constructs a new {@code DeadlinesGrid}.
     *
     * @param isEditAllowed true if the current user is allowed to edit deadlines
     */
    DeadlinesGrid(final boolean isEditAllowed) {

        super();

        setBackground(Skin.WHITE);

        this.layout = new GroupLayout(this);
        this.layout.setAutoCreateGaps(true);
        this.layout.setAutoCreateContainerGaps(false);
        setLayout(this.layout);

        this.editAllowed = isEditAllowed;
    }

    /**
     * Populates the grid for a selected student.
     *
     * @param data     the student data
     * @param listener the listener to receive ActionEvents when buttons in the grid are activated
     */
    void populateDisplay(final StudentData data, final ActionListener listener) {

        clearDisplay();

        Log.info("Deadlines grid is populating with " + data.studentMilestones.size() + " milestone overrides");

        final GroupLayout.ParallelGroup hGroup = this.layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        final GroupLayout.SequentialGroup vGroup = this.layout.createSequentialGroup();

        final RawStterm stterm = data.studentTerm;
        final String paceStr = Integer.toString(stterm.pace);

        for (final RawStcourse reg : data.pacedRegistrations) {

            final String courseName = reg.course.startsWith("M ") ? reg.course.replace("M ", "MATH ") : reg.course;
            final String paceOrderStr = Integer.toString(reg.paceOrder);

            final JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
            header.setBackground(Skin.OFF_WHITE_BLUE);

            final JLabel courseNameLbl = new JLabel(courseName);
            courseNameLbl.setFont(Skin.MEDIUM_HEADER_15_FONT);
            courseNameLbl.setForeground(Skin.LABEL_COLOR);
            header.add(courseNameLbl);

            final String labelText;
            if ("G".equals(reg.openStatus)) {
                labelText = "(Course " + paceOrderStr + " of " + paceStr + ", Forfeit [G])";
            } else if ("N".equals(reg.openStatus)) {
                labelText = "(Course " + paceOrderStr + " of " + paceStr + ", Forfeit [N])";
            } else if ("Y".equals(reg.openStatus)) {
                if ("Y".equals(reg.completed)) {
                    labelText = "(Course " + paceOrderStr + " of " + paceStr + ", Completed)";
                } else {
                    labelText = "(Course " + paceOrderStr + " of " + paceStr + ", Open)";
                }
            } else {
                labelText = "(Course " + paceOrderStr + " of " + paceStr + ", Not started)";
            }

            final JLabel paceOrderLbl = new JLabel(labelText);
            paceOrderLbl.setFont(Skin.MEDIUM_15_FONT);
            paceOrderLbl.setForeground(Skin.LABEL_COLOR);
            header.add(paceOrderLbl);

            hGroup.addComponent(header);
            vGroup.addComponent(header);

            final JPanel deadlineGrid = makeDeadlineGrid(data, reg, listener);

            hGroup.addComponent(deadlineGrid);
            vGroup.addComponent(deadlineGrid);
        }

        this.layout.setHorizontalGroup(hGroup);
        this.layout.setVerticalGroup(vGroup);
    }

    /**
     * Creates a panel that displays all deadlines for a single course.
     *
     * @param data     the student data
     * @param reg      the registration record (the pace order controls which deadlines are used)
     * @param listener the listener to receive ActionEvents when buttons in the grid are activated
     * @return the deadline grid
     */
    private JPanel makeDeadlineGrid(final StudentData data, final RawStcourse reg, final ActionListener listener) {

        final RawStterm stterm = data.studentTerm;
        final Integer pace = stterm.pace;
        final String track = stterm.paceTrack;

        final JPanel grid = new JPanel();
        grid.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        grid.setBackground(Skin.WHITE);

        final GroupLayout gridLayout = new GroupLayout(grid);
        gridLayout.setLayoutStyle(new DeadlinesLayoutStyle());
        gridLayout.setAutoCreateGaps(true);
        grid.setLayout(gridLayout);

        final GroupLayout.SequentialGroup hGroup = gridLayout.createSequentialGroup();
        final GroupLayout.SequentialGroup vGroup = gridLayout.createSequentialGroup();

        // Create column headings
        final Border underline = BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.DARK);
        final JLabel unitHeading = new JLabel("Unit:");
        unitHeading.setFont(Skin.MEDIUM_15_FONT);
        unitHeading.setBorder(underline);
        final JLabel milestoneHeading = new JLabel("Milestone:");
        milestoneHeading.setFont(Skin.MEDIUM_15_FONT);
        milestoneHeading.setBorder(underline);
        final JLabel origDateHeading = new JLabel("Orig. Date:");
        origDateHeading.setFont(Skin.MEDIUM_15_FONT);
        origDateHeading.setBorder(underline);
        final JLabel extensionsHeading = new JLabel("Extensions:");
        extensionsHeading.setFont(Skin.MEDIUM_15_FONT);
        extensionsHeading.setBorder(underline);
        final JLabel completedHeading = new JLabel("Completed:");
        completedHeading.setFont(Skin.MEDIUM_15_FONT);
        completedHeading.setBorder(underline);
        final JLabel onTimeHeading = new JLabel("On-Time:");
        onTimeHeading.setFont(Skin.MEDIUM_15_FONT);
        onTimeHeading.setBorder(underline);

        final GroupLayout.ParallelGroup col1 = gridLayout.createParallelGroup();
        col1.addComponent(unitHeading);

        final GroupLayout.ParallelGroup col2 = gridLayout.createParallelGroup();
        col2.addComponent(milestoneHeading);

        final GroupLayout.ParallelGroup col3 = gridLayout.createParallelGroup();
        col3.addComponent(origDateHeading);

        final GroupLayout.ParallelGroup col4 = gridLayout.createParallelGroup();
        col4.addComponent(extensionsHeading);

        final GroupLayout.ParallelGroup col5 = gridLayout.createParallelGroup();
        col5.addComponent(completedHeading);

        final GroupLayout.ParallelGroup col6 = gridLayout.createParallelGroup();
        col6.addComponent(onTimeHeading);

        final GroupLayout.ParallelGroup headerRow = gridLayout.createParallelGroup();
        headerRow.addComponent(unitHeading).addComponent(milestoneHeading).addComponent(origDateHeading)
                .addComponent(extensionsHeading).addComponent(completedHeading).addComponent(onTimeHeading);
        vGroup.addGroup(headerRow);

        final List<RawStexam> exams = data.studentExams;
        final List<RawMilestone> milestones = data.milestones;
        final List<RawStmilestone> stmilestones = data.studentMilestones;
        final List<RawPaceAppeals> paceAppeals = data.paceAppeals;

        final int regIndex = reg.paceOrder.intValue();

        final List<RawStmilestone> stmsList = new ArrayList<>(3);
        final List<RawPaceAppeals> appealsList = new ArrayList<>(3);

        for (final RawMilestone ms : milestones) {
            if (ms.pace.equals(pace) && ms.paceTrack.equals(track) && ms.getIndex() == regIndex) {
                // This is a milestone we want to display...

                final int unit = ms.getUnit();

                final GroupLayout.ParallelGroup milestoneRow = gridLayout.createParallelGroup();

                final JLabel unitLbl = new JLabel("    " + unit);
                unitLbl.setFont(Skin.MEDIUM_13_FONT);
                unitLbl.setForeground(Skin.LABEL_COLOR2);
                milestoneRow.addComponent(unitLbl);
                col1.addComponent(unitLbl);

                final String typeStr = ms.getTypeString();

                final JLabel msTypeLbl = new JLabel(typeStr);
                msTypeLbl.setFont(Skin.MEDIUM_13_FONT);
                msTypeLbl.setForeground(Skin.LABEL_COLOR2);
                milestoneRow.addComponent(msTypeLbl);
                col2.addComponent(msTypeLbl);

                final String origDtStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(ms.msDate);
                final JLabel origDtLbl = new JLabel(origDtStr);
                origDtLbl.setFont(Skin.MEDIUM_13_FONT);
                origDtLbl.setForeground(Skin.LABEL_COLOR2);
                milestoneRow.addComponent(origDtLbl);
                col3.addComponent(origDtLbl);

                // Collect student milestones and pace appeals attached to this milestone, find the currently effective
                // deadline date
                stmsList.clear();
                appealsList.clear();

                LocalDate effDate = ms.msDate;
                for (final RawStmilestone test : stmilestones) {
                    if (test.paceTrack.equals(track) && test.msNbr.equals(ms.msNbr) && test.msType.equals(ms
                            .msType)) {
                        stmsList.add(test);

                        RawPaceAppeals found = null;
                        for (final RawPaceAppeals testAppeal : paceAppeals) {
                            if (testAppeal.paceTrack.equals(track) && testAppeal.msNbr.equals(test.msNbr)
                                    && testAppeal.newDeadlineDt.equals(test.msDate)
                                    && Objects.equals(testAppeal.nbrAtmptsAllow, test.nbrAtmptsAllow)) {
                                found = testAppeal;
                                break;
                            }
                        }

                        appealsList.add(found);

                        if (test.msDate.isAfter(effDate)) {
                            effDate = test.msDate;
                        }
                    }
                }

                final JPanel extensions = new JPanel(new StackedBorderLayout());
                extensions.setBackground(Skin.WHITE);

                final int count = stmsList.size();
                for (int i = 0; i < count; ++i) {
                    final RawStmilestone stms = stmsList.get(i);
                    final RawPaceAppeals appeal = appealsList.get(i);

                    final String toolText;
                    if (appeal == null) {
                        toolText = "No pace appeal record found.";
                    } else {
                        final StringBuilder builder = new StringBuilder(500);

                        final String appealDateStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(appeal.appealDt);
                        builder.append("Appeal date: ");
                        builder.append(appealDateStr);

                        builder.append(CoreConstants.CRLF);
                        builder.append("Relief given: ");
                        builder.append(appeal.reliefGiven);

                        if (appeal.circumstances != null) {
                            builder.append(CoreConstants.CRLF);
                            builder.append("Circumstances: ");
                            builder.append(appeal.circumstances);
                        }

                        if (appeal.comment != null) {
                            builder.append(CoreConstants.CRLF);
                            builder.append("Comment: ");
                            builder.append(appeal.comment);
                        }

                        if (appeal.interviewer != null) {
                            builder.append(CoreConstants.CRLF);
                            builder.append("Interviewer: ");
                            builder.append(appeal.interviewer);
                        }

                        toolText = builder.toString();
                    }

                    final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
                    flow.setBackground(Skin.WHITE);

                    final String newDateStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(stms.msDate);

                    if (this.editAllowed) {
                        final JButton editExtension = new JButton(newDateStr);
                        editExtension.setActionCommand("EDIT" + ms.msType + ms.msNbr + "." + i);
                        editExtension.addActionListener(listener);
                        editExtension.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.DARK));
                        editExtension.setFont(Skin.BUTTON_13_FONT);
                        editExtension.setForeground(Skin.LABEL_COLOR3);
                        editExtension.setToolTipText(toolText);
                        flow.add(editExtension);
                    } else {
                        final JLabel lbl = new JLabel(newDateStr);
                        lbl.setForeground(Skin.LABEL_COLOR3);
                        flow.add(lbl);
                    }

                    extensions.add(flow, StackedBorderLayout.NORTH);
                }

                if (this.editAllowed) {
                    final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
                    flow.setBackground(Skin.WHITE);
                    final JButton addExtension = new JButton("Appeal...");
                    addExtension.setActionCommand("ADD" + ms.msType + ms.msNbr);
                    addExtension.addActionListener(listener);
                    addExtension.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.DARK));
                    addExtension.setFont(Skin.BUTTON_13_FONT);
                    flow.add(addExtension);

                    extensions.add(flow, StackedBorderLayout.NORTH);
                }

                milestoneRow.addComponent(extensions);
                col4.addComponent(extensions);

                // See if the student has passed the associated exam
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
                    if (exam.course.equals(reg.course) && exam.unit.intValue() == unit && exam.examType.equals
                            (examType)
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

                final String completionStr = earliestCompletion == null ? CoreConstants.SPC :
                        TemporalUtils.FMT_MDY_COMPACT_FIXED.format(earliestCompletion);
                final JLabel completionLbl = new JLabel(completionStr);
                completionLbl.setFont(Skin.MEDIUM_13_FONT);
                completionLbl.setForeground(Skin.LABEL_COLOR2);

                milestoneRow.addComponent(completionLbl);
                col5.addComponent(completionLbl);

                final String onTimeStr = onTime == null ? CoreConstants.SPC : onTime.booleanValue() ? "Y" : "N";
                final JLabel onTimeLbl = new JLabel(onTimeStr);
                onTimeLbl.setFont(Skin.MEDIUM_13_FONT);
                onTimeLbl.setForeground(Skin.LABEL_COLOR2);

                milestoneRow.addComponent(onTimeLbl);
                col6.addComponent(onTimeLbl);

                vGroup.addGroup(milestoneRow);
            }
        }

        hGroup.addGroup(col1).addGroup(col2).addGroup(col3).addGroup(col4).addGroup(col5).addGroup(col6);
        gridLayout.setHorizontalGroup(hGroup);
        gridLayout.setVerticalGroup(vGroup);

        return grid;
    }

    /**
     * Populates the grid for a selected student.
     */
    public void clearDisplay() {

        removeAll();
    }

    /**
     * Displays an indication that the student is not registered for any courses that would count toward their paced
     * deadlines this term.
     */
    void indicateNoCourses() {

        clearDisplay();

        final GroupLayout.ParallelGroup hGroup = this.layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        final GroupLayout.SequentialGroup vGroup = this.layout.createSequentialGroup();

        final JPanel messages = new JPanel(new StackedBorderLayout());
        messages.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        final JLabel msg1 = new JLabel("This student is not registered for any courses that count");
        msg1.setFont(Skin.MEDIUM_15_FONT);
        msg1.setBackground(Skin.LIGHTEST);
        msg1.setForeground(Skin.DARK);
        messages.add(msg1, StackedBorderLayout.NORTH);

        final JLabel msg2 = new JLabel("toward their pace in this term.");
        msg2.setFont(Skin.MEDIUM_15_FONT);
        msg2.setBackground(Skin.LIGHTEST);
        msg2.setForeground(Skin.DARK);
        messages.add(msg2, StackedBorderLayout.NORTH);

        hGroup.addComponent(messages);
        vGroup.addComponent(messages);

        this.layout.setHorizontalGroup(hGroup);
        this.layout.setVerticalGroup(vGroup);
    }

    /**
     * The layout style for the deadline grid.
     */
    static class DeadlinesLayoutStyle extends LayoutStyle {

        /**
         * Constructs a new {@code DeadlinesLayoutStyle}.
         */
        DeadlinesLayoutStyle() {
            super();
        }

        /**
         * Gets the preferred gap between components.
         *
         * @param component1 the {@code JComponent} {@code component2} is being placed relative to
         * @param component2 the {@code JComponent} being placed
         * @param type       how the two components are being placed
         * @param position   the position {@code component2} is being placed relative to {@code component1}; one of
         *                   {@code SwingConstants.NORTH}, {@code SwingConstants.SOUTH}, {@code SwingConstants.EAST} or
         *                   {@code SwingConstants.WEST}
         * @param parent     the parent of {@code component2}; this may differ from the actual parent and it may be
         *                   {@code null}
         * @return the gap
         */
        @Override
        public final int getPreferredGap(final JComponent component1, final JComponent component2,
                                         final ComponentPlacement type, final int position, final Container parent) {

            return position == SwingConstants.EAST || position == SwingConstants.WEST ? 10 : 2;
        }

        /**
         * Gets the preferred gap between a component and the edge of the container.
         *
         * @param component the {@code JComponent} being positioned
         * @param position  the position {@code component} is being placed relative to its parent; one of
         *                  {@code SwingConstants.NORTH}, {@code SwingConstants.SOUTH}, {@code SwingConstants.EAST} or
         *                  {@code SwingConstants.WEST}
         * @param parent    the parent of {@code component}; this may differ from the actual parent and may be
         *                  {@code null}
         * @return the gap
         */
        @Override
        public final int getContainerGap(final JComponent component, final int position, final Container parent) {

            return position == SwingConstants.EAST || position == SwingConstants.WEST ? 6 : 2;
        }
    }
}
