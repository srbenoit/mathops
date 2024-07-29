package dev.mathops.app.adm.office.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * The "Accommodations & Appeals" panel of the admin system.
 *
 * <p>
 * This panel allows the user to view or (with sufficient permissions) to add/edit accommodations and pace appeals.
 * Accommodations such as extended time on exams and allowed days of extension are stored in the STUDENT table, and can
 * be updated through this panel (typically based on an accommodation letter).
 *
 * <p>
 * For pace appeals, there can be many records in PACE_APPEALS for a student - that table is essentially a log of all
 * appeals made and their outcomes, including what adjustments (if any) were made at the time to the student's deadline.
 * The STMILESTONE or STU_STD_MILESTONE tables store the current student deadline, if it has been overridden.  There
 * should be only one record in either of these tables for a specific milestone, and that record will be effective even
 * if its deadline date precedes the "ordinary" deadline date for a course.
 *
 * <p>
 * When a user adds or edits a pace appeal record, they have the option to update the student's deadline in the
 * milestone tables as well.
 */
public final class StuAppealsPanel extends AdmPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4908492242412815193L;

    /** An action command. */
    private static final String ADD_APPEAL_CMD = "ADD_APPEAL";

    /** An action command. */
    private static final String EDIT_ACCOMMODATION_CMD = "EDIT_ACC";

    /** An action command. */
    private static final String EDIT_APPEAL_CMD = "EDIT_APPEAL";

    /** The data cache. */
    private final Cache cache;

    /** The fixed data. */
    private final UserData fixed;

    /** Flag indicating the logged-in user can see appeal details. */
    private final boolean canSeeDetails;

    /** Flag indicating the logged-in user can edit appeals. */
    private final boolean canEdit;

    /** A display for the student's time limit factor. */
    private final JTextField timeLimitFactor;

    /** A display for the student's number of days of SDC extension. */
    private final JTextField extensionDays;

    /** The current student data. */
    private StudentData currentStudentData = null;

    /** The panel that displays appeals. */
    private final JPanel appealsPanel;

    /** The list of currently-displayed appeals. */
    private List<RawPaceAppeals> appeals = null;

    /** The dialog to edit student accommodations. */
    private DlgEditAccommodations editAccommodationsDialog = null;

    /** The dialog to add new pace appeals. */
    private DlgAddPaceAppeal addPaceAppealDialog = null;

    /** The dialog to edit existing pace appeals. */
    private DlgEditPaceAppeal editPaceAppealDialog = null;

    /**
     * Constructs a new {@code StuAppealsPanel}.
     *
     * @param theCache    the data cache
     * @param theUserData user data
     */
    public StuAppealsPanel(final Cache theCache, final UserData theUserData) {

        super();
        setBackground(Skin.WHITE);

        final Integer clearance = theUserData.getClearanceLevel("STU_DLINE");
        this.canEdit = clearance != null && clearance.intValue() <= 2;
        this.canSeeDetails = clearance != null && clearance.intValue() <= 3;

        this.cache = theCache;
        this.fixed = theUserData;

        final JPanel north = makeOffWhitePanel(new StackedBorderLayout(5, 5));
        north.setBackground(Skin.WHITE);
        north.setLayout(new BoxLayout(north, BoxLayout.PAGE_AXIS));
        add(north, StackedBorderLayout.NORTH);

        final JPanel headerFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 6));
        headerFlow.setBackground(Skin.WHITE);
        final JLabel header = makeHeader("Accommodations and Appeals", false);
        headerFlow.add(header, BorderLayout.PAGE_START);
        north.add(headerFlow, StackedBorderLayout.NORTH);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 6));
        topFlow.setBackground(Skin.WHITE);
        final JLabel lbl1 = new JLabel("Time limit factor:");
        lbl1.setFont(Skin.MEDIUM_15_FONT);
        topFlow.add(lbl1);

        this.timeLimitFactor = new JTextField(6);
        this.timeLimitFactor.setEditable(false);
        this.timeLimitFactor.setFont(Skin.MEDIUM_15_FONT);
        topFlow.add(this.timeLimitFactor);

        final JLabel lbl2 = new JLabel("      Days of Extension Allowed:");
        lbl2.setFont(Skin.MEDIUM_15_FONT);
        topFlow.add(lbl2);

        this.extensionDays = new JTextField(6);
        this.extensionDays.setEditable(false);
        this.extensionDays.setFont(Skin.MEDIUM_15_FONT);
        topFlow.add(this.extensionDays);

        // If this user has permission to add/edit appeals, provide a button to edit time-limit and extensions
        if (this.canEdit) {
            final JLabel gap = new JLabel("      ");
            gap.setFont(Skin.MEDIUM_15_FONT);
            topFlow.add(gap);

            final JButton editAccommodations = new JButton("Edit Accommodations");
            editAccommodations.setFont(Skin.BUTTON_13_FONT);
            editAccommodations.setActionCommand(EDIT_ACCOMMODATION_CMD);
            editAccommodations.addActionListener(this);
            topFlow.add(editAccommodations);
        }

        north.add(topFlow, StackedBorderLayout.NORTH);

        this.appealsPanel = new JPanel(new StackedBorderLayout());
        this.appealsPanel.setBackground(Skin.WHITE);

        final JScrollPane appealsScroll = new JScrollPane(this.appealsPanel);
        appealsScroll.getVerticalScrollBar().setUnitIncrement(20);
        appealsScroll.getVerticalScrollBar().setBlockIncrement(100);
        add(appealsScroll, StackedBorderLayout.CENTER);

        // If this user has permission to add/edit appeals, provide a button to do so
        if (this.canEdit) {
            final JPanel south = makeOffWhitePanel(new StackedBorderLayout(5, 5));
            south.setBackground(Skin.WHITE);
            south.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 6));
            add(south, StackedBorderLayout.SOUTH);

            final JButton add = new JButton("Add Appeal...");
            add.setFont(Skin.BUTTON_13_FONT);
            add.setActionCommand(ADD_APPEAL_CMD);
            add.addActionListener(this);
            south.add(add);
        }
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

        this.currentStudentData = null;

        this.timeLimitFactor.setText(CoreConstants.EMPTY);
        this.extensionDays.setText(CoreConstants.EMPTY);
        this.appealsPanel.removeAll();
        this.appealsPanel.invalidate();
        this.appealsPanel.revalidate();

        if (this.editAccommodationsDialog != null) {
            this.editAccommodationsDialog.setVisible(false);
            this.editAccommodationsDialog.dispose();
            this.editAccommodationsDialog = null;
        }

        if (this.addPaceAppealDialog != null) {
            this.addPaceAppealDialog.setVisible(false);
            this.addPaceAppealDialog.dispose();
            this.addPaceAppealDialog = null;
        }

        if (this.editPaceAppealDialog != null) {
            this.editPaceAppealDialog.setVisible(false);
            this.editPaceAppealDialog.dispose();
            this.editPaceAppealDialog = null;
        }
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        this.currentStudentData = data;

        final Float factor = data.student.timelimitFactor;
        if (factor == null) {
            this.timeLimitFactor.setText(CoreConstants.EMPTY);
        } else {
            final String factorStr = factor.toString();
            this.timeLimitFactor.setText(factorStr);
        }

        final Integer extDays = data.student.extensionDays;
        if (extDays == null) {
            this.extensionDays.setText(CoreConstants.EMPTY);
        } else {
            final String extDaysStr = extDays.toString();
            this.extensionDays.setText(extDaysStr);
        }

        this.appealsPanel.removeAll();
        try {
            this.appeals = RawPaceAppealsLogic.queryByStudent(this.cache, data.student.stuId);
            this.appeals.sort(null);

            int index = 0;
            for (final RawPaceAppeals appeal : this.appeals) {
                final JPanel appealPanel = makeAppealPanel(appeal, index);
                this.appealsPanel.add(appealPanel, StackedBorderLayout.NORTH);
                ++index;
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            JOptionPane.showMessageDialog(this, "Failed to query appeals.", "Accommodationa & Appeals",
                    JOptionPane.ERROR_MESSAGE);
        }
        this.appealsPanel.invalidate();
        this.appealsPanel.revalidate();
    }

    /**
     * Creates a panel that displays a pace appeal.
     *
     * @param appeal the appeal
     * @param index  the index of the appeal
     * @return the panel
     */
    private JPanel makeAppealPanel(final RawPaceAppeals appeal, final int index) {

        final JPanel panel = new JPanel(new StackedBorderLayout());
        panel.setBackground(Skin.WHITE);
        final MatteBorder lineBelow = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);
        panel.setBorder(lineBelow);

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
        row1.setBackground(Skin.WHITE);
        panel.add(row1, StackedBorderLayout.NORTH);
        final String dateString = TemporalUtils.FMT_MDY.format(appeal.appealDt);
        final String headerStr;
        if (appeal.interviewer == null) {
            headerStr = SimpleBuilder.concat("Appeal on ", dateString, " (no interviewer)");
        } else {
            headerStr = SimpleBuilder.concat("Appeal on ", dateString, " (entered by ", appeal.interviewer, ")");
        }
        final JLabel lbl11 = new JLabel(headerStr);
        lbl11.setFont(Skin.MEDIUM_HEADER_15_FONT);
        row1.add(lbl11);

        if (this.canEdit) {
            final JLabel gap = new JLabel("      ");
            gap.setFont(Skin.MEDIUM_15_FONT);
            row1.add(gap);

            final JButton edit = new JButton("Edit Appeal");
            edit.setFont(Skin.BUTTON_13_FONT);
            final String indexStr = Integer.toString(index);
            final String cmd = SimpleBuilder.concat(EDIT_APPEAL_CMD, indexStr);
            edit.setActionCommand(cmd);
            edit.addActionListener(this);
            row1.add(edit);
        }

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
        row2.setBackground(Skin.WHITE);
        panel.add(row2, StackedBorderLayout.NORTH);

        final String paceLblStr = SimpleBuilder.concat("Pace: ", appeal.pace);
        final JLabel paceLbl = new JLabel(paceLblStr);
        paceLbl.setFont(Skin.MEDIUM_13_FONT);
        row2.add(paceLbl);

        final String trackLblStr = SimpleBuilder.concat("Track: ", appeal.pace);
        final JLabel trackLbl = new JLabel(trackLblStr);
        trackLbl.setFont(Skin.MEDIUM_13_FONT);
        row2.add(trackLbl);

        if (Objects.nonNull(appeal.msNbr)) {
            final int number = appeal.msNbr.intValue();
            final int course = (number / 10) % 10;

            final String courseStr = Integer.toString(course);
            final String courseLblStr = SimpleBuilder.concat("Course: ", courseStr);
            final JLabel courseLbl = new JLabel(courseLblStr);
            courseLbl.setFont(Skin.MEDIUM_13_FONT);
            row2.add(courseLbl);

            final int unit = number % 10;
            final String unitStr = Integer.toString(unit);

            final String unitTypeStr;
            if ("RE".equals(appeal.msType)) {
                unitTypeStr = SimpleBuilder.concat("Unit: ", unitStr, " (Review Exam)");
            } else if ("FE".equals(appeal.msType)) {
                unitTypeStr = SimpleBuilder.concat("Unit: ", unitStr, " (Final Exam)");
            } else if ("F1".equals(appeal.msType)) {
                unitTypeStr = SimpleBuilder.concat("Unit: ", unitStr, " (Final +1)");
            } else {
                unitTypeStr = SimpleBuilder.concat("Unit: ", unitStr, " (Type ", appeal.msType, ")");
            }

            final JLabel unitTypeLbl = new JLabel(unitTypeStr);
            unitTypeLbl.setFont(Skin.MEDIUM_13_FONT);
            row2.add(unitTypeLbl);
        }

        final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
        row3.setBackground(Skin.WHITE);
        panel.add(row3, StackedBorderLayout.NORTH);

        final String reliefLblStr = SimpleBuilder.concat("Relief given: ", appeal.reliefGiven);
        final JLabel reliefLbl = new JLabel(reliefLblStr);
        reliefLbl.setFont(Skin.MEDIUM_13_FONT);
        row3.add(reliefLbl);

        if (Objects.nonNull(appeal.msDate)) {
            final String dateStr = TemporalUtils.FMT_MDY.format(appeal.msDate);
            final String origDateLblStr = SimpleBuilder.concat("Orig. date: ", dateStr);
            final JLabel origDateLbl = new JLabel(origDateLblStr);
            origDateLbl.setFont(Skin.MEDIUM_13_FONT);
            row3.add(origDateLbl);
        }

        if (Objects.nonNull(appeal.newDeadlineDt)) {
            final String dateStr = TemporalUtils.FMT_MDY.format(appeal.newDeadlineDt);
            final String newDateLblStr = SimpleBuilder.concat("New date: ", dateStr);
            final JLabel newDateLbl = new JLabel(newDateLblStr);
            newDateLbl.setFont(Skin.MEDIUM_13_FONT);
            row3.add(newDateLbl);
        }

        if (Objects.nonNull(appeal.nbrAtmptsAllow)) {
            final String attemptsLblStr = SimpleBuilder.concat("Attempts: ", appeal.nbrAtmptsAllow);
            final JLabel attemptsLbl = new JLabel(attemptsLblStr);
            attemptsLbl.setFont(Skin.MEDIUM_13_FONT);
            row3.add(attemptsLbl);
        }

        if (this.canSeeDetails) {
            final JLabel[] lbl = new JLabel[2];
            lbl[0] = new JLabel("Circumstances:");
            lbl[0].setFont(Skin.MEDIUM_13_FONT);
            lbl[1] = new JLabel("Comment:");
            lbl[1].setFont(Skin.MEDIUM_13_FONT);
            UIUtilities.makeLabelsSameSizeRightAligned(lbl);

            final JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
            row4.setBackground(Skin.WHITE);
            panel.add(row4, StackedBorderLayout.NORTH);
            row4.add(lbl[0]);
            if (Objects.nonNull(appeal.circumstances)) {
                final JLabel circLbl = new JLabel(appeal.circumstances);
                circLbl.setFont(Skin.MEDIUM_13_FONT);
                row4.add(circLbl);
            }

            final JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
            row5.setBackground(Skin.WHITE);
            panel.add(row5, StackedBorderLayout.NORTH);
            row5.add(lbl[1]);
            if (Objects.nonNull(appeal.comment)) {
                final JLabel commentLbl = new JLabel(appeal.comment);
                commentLbl.setFont(Skin.MEDIUM_13_FONT);
                row5.add(commentLbl);
            }
        }

        return panel;
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (ADD_APPEAL_CMD.equals(cmd)) {
            if (Objects.nonNull(this.currentStudentData)) {
                if (this.addPaceAppealDialog == null) {
                    this.addPaceAppealDialog = new DlgAddPaceAppeal(this.cache, this);
                }

                this.addPaceAppealDialog.populateDisplay(this.fixed, this.currentStudentData);
                this.addPaceAppealDialog.setVisible(true);
                this.addPaceAppealDialog.toFront();
            }
        } else if (EDIT_ACCOMMODATION_CMD.equals(cmd)) {
            if (Objects.nonNull(this.currentStudentData)) {
                if (this.editAccommodationsDialog == null) {
                    this.editAccommodationsDialog = new DlgEditAccommodations(this.cache, this);
                }

                this.editAccommodationsDialog.populateDisplay(this.currentStudentData);
                this.editAccommodationsDialog.setVisible(true);
                this.editAccommodationsDialog.toFront();
            }
        } else if (cmd != null && cmd.startsWith(EDIT_APPEAL_CMD)) {
            final int cmdLen = EDIT_APPEAL_CMD.length();
            final String sub = cmd.substring(cmdLen);
            try {
                final int index = Integer.parseInt(sub);
                if (index >= 0 && Objects.nonNull(this.appeals) && index < this.appeals.size()) {
                    final RawPaceAppeals appeal = this.appeals.get(index);

                    if (Objects.nonNull(this.currentStudentData)) {
                        if (this.editPaceAppealDialog == null) {
                            this.editPaceAppealDialog = new DlgEditPaceAppeal(this.cache, this);
                        }

                        this.editPaceAppealDialog.populateDisplay(this.currentStudentData, appeal);
                        this.editPaceAppealDialog.setVisible(true);
                        this.editPaceAppealDialog.toFront();
                    }
                } else {
                    Log.warning("Command referenced invalid appeal: ", cmd);
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Failed to interpret command: ", cmd);
            }
        }
    }

    /**
     * Called by the dialog that edits accommodations when an edit is applied.
     */
    void updateAccommodations() {

        if (this.currentStudentData != null) {
            this.currentStudentData.updateStudent(this.cache);
            populateDisplay(this.currentStudentData);
        }
    }

    /**
     * Called by the dialog that edits accommodations when an edit is applied.
     */
    void updateAppeals() {

        if (this.currentStudentData != null) {
            this.currentStudentData.updatePaceAppeals(this.cache);
            populateDisplay(this.currentStudentData);
        }
    }
}
