package dev.mathops.app.adm.office.placement;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.AlignedFlowLayout;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.types.EMathPlanStatus;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.SimpleBuilder;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serial;
import java.sql.SQLException;
import java.util.List;

/**
 * A panel that shows student Math Plan and RamReady status.
 */
public class PlacementMathPlanPanel extends AdmPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7990489786189966963L;

    /** A table that shows all Math Plan responses. */
    private final JTableMathPlanResponses mathPlanResponsesTable;

    /** Scroll pane for math plan responses. */
    private final JScrollPane mathPlanScroll;

    /** The RamReady "check math plan" status. */
    private final JTextField checkMathPlanStatus;

    /** The RamReady "check math plan" message. */
    private final JTextArea checkMathPlanMessage;

    /** The RamReady "check math placement" status. */
    private final JTextField checkMathPlacementStatus;

    /** The RamReady "check math placement" message. */
    private final JTextArea checkMathPlacementMessage;

    /** An error message. */
    private final JLabel error;

    /**
     * Constructs a new {@code StudentMathPlanPanel}.
     */
    public PlacementMathPlanPanel() {

        super();
        setBackground(Skin.WHITE);

        this.error = makeError();
        add(this.error, BorderLayout.SOUTH);

        final JPanel pane1 = makeOffWhitePanel(new StackedBorderLayout());
        pane1.setBackground(Skin.WHITE);
        pane1.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(pane1, StackedBorderLayout.CENTER);

        final JPanel pane2 = makeOffWhitePanel(new StackedBorderLayout());
        pane2.setBackground(Skin.WHITE);
        pane2.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(pane2, StackedBorderLayout.SOUTH);

        //

        final JLabel mathPlanHeader = makeHeader("Math Plan Responses", false);
        pane1.add(mathPlanHeader, BorderLayout.NORTH);

        this.mathPlanResponsesTable = new JTableMathPlanResponses();
        this.mathPlanResponsesTable.setFillsViewportHeight(true);

        this.mathPlanScroll = new JScrollPane(this.mathPlanResponsesTable);
        this.mathPlanScroll.setPreferredSize(
                this.mathPlanResponsesTable.getPreferredScrollSize(this.mathPlanScroll, 3));

        pane1.add(this.mathPlanScroll, BorderLayout.WEST);

        //

        final JLabel ramReadyHeader = makeHeader("RamReady Status", true);
        pane2.add(ramReadyHeader, BorderLayout.NORTH);

        final JLabel[] labels = new JLabel[4];
        labels[0] = new JLabel("    Status:");
        labels[1] = new JLabel("    Message:");
        labels[2] = new JLabel("    Status:");
        labels[3] = new JLabel("    Message:");

        labels[0].setFont(Skin.MEDIUM_15_FONT);
        labels[1].setFont(Skin.MEDIUM_15_FONT);
        labels[2].setFont(Skin.MEDIUM_15_FONT);
        labels[3].setFont(Skin.MEDIUM_15_FONT);

        UIUtilities.makeLabelsSameSizeRightAligned(labels);
        labels[1].setVerticalTextPosition(SwingConstants.TOP);
        labels[3].setVerticalTextPosition(SwingConstants.TOP);

        final JPanel flow2a = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow2a.setBackground(Skin.WHITE);
        final JLabel header1 = new JLabel("CheckMathPlan results:");
        header1.setFont(Skin.MEDIUM_HEADER_15_FONT);
        flow2a.add(header1);
        pane2.add(flow2a, StackedBorderLayout.NORTH);

        final JPanel flow2b = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow2b.setBackground(Skin.WHITE);
        flow2b.add(labels[0]);
        this.checkMathPlanStatus = new JTextField(20);
        this.checkMathPlanStatus.setFont(Skin.MEDIUM_15_FONT);
        flow2b.add(this.checkMathPlanStatus);
        pane2.add(flow2b, StackedBorderLayout.NORTH);

        final JPanel flow2c =
                new JPanel(new AlignedFlowLayout(AlignedFlowLayout.LEFT, 6, 3, AlignedFlowLayout.TOP));
        flow2c.setBackground(Skin.WHITE);
        flow2c.add(labels[1]);
        this.checkMathPlanMessage = new JTextArea(3, 60);
        this.checkMathPlanMessage.setLineWrap(true);
        this.checkMathPlanMessage.setWrapStyleWord(true);
        this.checkMathPlanMessage.setFont(Skin.MEDIUM_15_FONT);
        this.checkMathPlanMessage.setBorder(this.checkMathPlanStatus.getBorder());
        flow2c.add(this.checkMathPlanMessage);
        pane2.add(flow2c, StackedBorderLayout.NORTH);

        final JPanel flow2d = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow2d.setBackground(Skin.WHITE);
        final JLabel header2 = new JLabel("CheckMathPlacement results:");
        header2.setFont(Skin.MEDIUM_HEADER_15_FONT);
        flow2d.add(header2);
        pane2.add(flow2d, StackedBorderLayout.NORTH);

        final JPanel flow2e = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 3));
        flow2e.setBackground(Skin.WHITE);
        flow2e.add(labels[2]);
        this.checkMathPlacementStatus = new JTextField(20);
        this.checkMathPlacementStatus.setFont(Skin.MEDIUM_15_FONT);
        flow2e.add(this.checkMathPlacementStatus);
        pane2.add(flow2e, StackedBorderLayout.NORTH);

        final JPanel flow2f =
                new JPanel(new AlignedFlowLayout(AlignedFlowLayout.LEFT, 6, 3, AlignedFlowLayout.TOP));
        flow2f.setBackground(Skin.WHITE);
        flow2f.add(labels[3]);
        this.checkMathPlacementMessage = new JTextArea(3, 60);
        this.checkMathPlacementMessage.setLineWrap(true);
        this.checkMathPlacementMessage.setWrapStyleWord(true);
        this.checkMathPlacementMessage.setFont(Skin.MEDIUM_15_FONT);
        this.checkMathPlacementMessage.setBorder(this.checkMathPlacementStatus.getBorder());
        flow2f.add(this.checkMathPlacementMessage);
        pane2.add(flow2f, StackedBorderLayout.NORTH);
    }

    /**
     * Sets the selected student data.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     */
    public void setSelectedStudent(final Cache cache, final String studentId) {

        this.error.setText(CoreConstants.SPC);
        clearDisplay();

        if (studentId == null) {
            clearDisplay();
        } else {
            populateDisplay(cache, studentId);
        }

        final Dimension prefScrollSize = this.mathPlanResponsesTable.getPreferredScrollSize(this.mathPlanScroll, 3);
        this.mathPlanScroll.setPreferredSize(prefScrollSize);
    }

    /**
     * Clears all displayed fields.
     */
    public void clearDisplay() {

        this.mathPlanResponsesTable.clear();
        this.checkMathPlanStatus.setText(CoreConstants.EMPTY);
        this.checkMathPlanMessage.setText(CoreConstants.EMPTY);
        this.checkMathPlacementStatus.setText(CoreConstants.EMPTY);
        this.checkMathPlacementMessage.setText(CoreConstants.EMPTY);
        this.error.setText(CoreConstants.EMPTY);
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     */
    private void populateDisplay(final Cache cache, final String studentId) {

        try {
            final List<RawStmathplan> records = RawStmathplanLogic.queryByStudent(cache, studentId);
            this.mathPlanResponsesTable.addData(records, 3);
        } catch (final SQLException ex) {
            this.error.setText("Failed to query 'stmathplan': " + ex.getMessage());
        }

        try {
            final RawStudent stu = RawStudentLogic.query(cache, studentId, false);

            if (stu == null) {
                this.error.setText("Student '" + studentId + "' not found");
            } else {
                final Integer pidm = stu.pidm;

                if (pidm == null) {
                    this.checkMathPlanStatus.setText("NOT STARTED");
                    this.checkMathPlanMessage.setText("Mathematics Plan status is not available.");

                    this.checkMathPlacementStatus.setText("NOT STARTED");
                    this.checkMathPlacementMessage.setText("Math Placement status not available.");

                    this.error.setText("Student '" + studentId + "' has no PIDM");
                } else {
                    final EMathPlanStatus status = MathPlanLogic.getStatus(cache, studentId);
                    if (status == EMathPlanStatus.NOT_STARTED) {
                        this.checkMathPlanStatus.setText("NOT STARTED");
                        this.checkMathPlanMessage.setText(SimpleBuilder.concat(
                                "Create my Personalized Mathematics Plan\r\n",
                                "All majors at CSU include at least one quantitative reasoning ",
                                "course (for a total of three credits) to graduate. Create your ",
                                "Personalized Mathematics Plan to view the math or statistics ",
                                "course(s) for your major (or majors of interest) and to determine ",
                                "whether you should complete the Math Placement process before Ram ",
                                "Orientation."));
                    } else {
                        this.checkMathPlanStatus.setText("COMPLETED");
                        this.checkMathPlanMessage.setText(SimpleBuilder.concat("Review my Mathematics Plan."));
                    }

                    final List<RawStmpe> placement = cache.getStudent(studentId).getLegalPlacementAttempts();
                    if (placement.isEmpty()) {
                        this.checkMathPlacementStatus.setText("NOT STARTED");
                        this.checkMathPlacementMessage.setText(SimpleBuilder.concat(
                                "Complete the Math Placement Process\r\n",
                                "Based on your personalized Mathematics Plan, you should complete ",
                                "the Math Placement process before Ram Orientation."));
                    } else {
                        this.checkMathPlacementStatus.setText("COMPLETED");
                        this.checkMathPlacementMessage.setText("Review my Math Placement results");
                    }
                }
            }
        } catch (final SQLException ex) {
            this.error.setText("Failed to query 'student': " + ex.getMessage());
        }

        final int prefWidth = this.mathPlanResponsesTable.getPreferredSize().width
                              + this.mathPlanScroll.getVerticalScrollBar().getPreferredSize().width + 10;

        this.mathPlanScroll.setPreferredSize(new Dimension(prefWidth, Integer.MAX_VALUE));

        invalidate();
        revalidate();
        repaint();
    }
}
