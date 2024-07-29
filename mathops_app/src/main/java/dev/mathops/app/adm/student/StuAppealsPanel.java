package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.List;

/**
 * The "Appeals & Accommodations" panel of the admin system.
 */
final class StuAppealsPanel extends AdmPanelBase implements ActionListener {

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
    private final FixedData fixed;

    /** Flag indicating the logged-in user can see appeal details. */
    private final boolean canSeeDetails;

    /** Flag indicating the logged-in user can edit appeals. */
    private final boolean canEdit;

    /** A display for the student's time limit factor. */
    private final JTextField timelimitFactor;

    /** A display for the student's number of days of SDC extension. */
    private final JTextField extensionDays;

    /** The current student data. */
    private StudentData currentStudentData;

    /** The panel that displays appeals. */
    private JPanel appealsPanel;

    /** The list of currently-displayed appeals. */
    private List<RawPaceAppeals> appeals;

    /** The dialog to edit student accommodations. */
    private DlgEditAccommodations editAccommodationsDialog = null;

    /** The dialog to add new pace appeals. */
    private DlgAddPaceAppeal addPaceAppealDialog = null;

    /** The dialog to edit existing pace appeals. */
    private DlgEditPaceAppeal editPaceAppealDialog = null;

    /**
     * Constructs a new {@code StuAppealsPanel}.
     *
     * @param theCache the data cache
     * @param theFixed fixed data
     */
    StuAppealsPanel(final Cache theCache, final FixedData theFixed) {

        super();
        setBackground(Skin.WHITE);

        final Integer clearance = theFixed.getClearanceLevel("STU_DLINE");
        this.canEdit = clearance != null && clearance.intValue() <= 2;
        this.canSeeDetails = clearance != null && clearance.intValue() <= 3;

        this.cache = theCache;
        this.fixed = theFixed;

        final JPanel north = makeOffWhitePanel(new StackedBorderLayout(5, 5));
        north.setBackground(Skin.WHITE);
        north.setLayout(new BoxLayout(north, BoxLayout.PAGE_AXIS));
        add(north, StackedBorderLayout.NORTH);

        final JPanel headerFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 6));
        headerFlow.setBackground(Skin.WHITE);
        headerFlow.add(makeHeader("Accommodations and Appeals", false), BorderLayout.PAGE_START);
        north.add(headerFlow, StackedBorderLayout.NORTH);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 6));
        topFlow.setBackground(Skin.WHITE);
        final JLabel lbl1 = new JLabel("Time limit factor:");
        lbl1.setFont(Skin.MEDIUM_15_FONT);
        topFlow.add(lbl1);

        this.timelimitFactor = new JTextField(6);
        this.timelimitFactor.setEditable(false);
        this.timelimitFactor.setFont(Skin.MEDIUM_15_FONT);
        topFlow.add(this.timelimitFactor);

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
        if (canEdit) {
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
    void clearDisplay() {

        this.currentStudentData = null;

        this.timelimitFactor.setText(CoreConstants.EMPTY);
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
            this.timelimitFactor.setText(CoreConstants.EMPTY);
        } else {
            final String factorStr = factor.toString();
            this.timelimitFactor.setText(factorStr);
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
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
        row1.setBackground(Skin.WHITE);
        panel.add(row1, StackedBorderLayout.NORTH);
        final String dateString = TemporalUtils.FMT_MDY.format(appeal.appealDt);
        final String headerStr;
        if (appeal.interviewer == null) {
            headerStr = "Appeal on " + dateString + " (no interviewer)";
        } else {
            headerStr = "Appeal on " + dateString + " (entered by " + appeal.interviewer + ")";
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
            edit.setActionCommand(EDIT_APPEAL_CMD + index);
            edit.addActionListener(this);
            row1.add(edit);
        }

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
        row2.setBackground(Skin.WHITE);
        panel.add(row2, StackedBorderLayout.NORTH);

        final JLabel paceLbl = new JLabel("Pace: " + appeal.pace);
        paceLbl.setFont(Skin.MEDIUM_13_FONT);
        row2.add(paceLbl);

        final JLabel trackLbl = new JLabel("Track: " + appeal.paceTrack);
        trackLbl.setFont(Skin.MEDIUM_13_FONT);
        row2.add(trackLbl);

        if (appeal.msNbr != null) {
            final int number = appeal.msNbr.intValue();
            final int course = (number / 10) % 10;

            final JLabel courseLbl = new JLabel("Course: " + course);
            courseLbl.setFont(Skin.MEDIUM_13_FONT);
            row2.add(courseLbl);

            final int unit = number % 10;

            final String unitTypeStr;
            if ("RE".equals(appeal.msType)) {
                unitTypeStr = "Unit: " + unit + " (Review Exam)";
            } else if ("FE".equals(appeal.msType)) {
                unitTypeStr = "Unit: " + unit + " (Final Exam)";
            } else if ("F1".equals(appeal.msType)) {
                unitTypeStr = "Unit: " + unit + " (Final +1)";
            } else {
                unitTypeStr = "Unit: " + unit + " (Type " + appeal.msType + ")";
            }

            final JLabel unitTypeLbl = new JLabel(unitTypeStr);
            unitTypeLbl.setFont(Skin.MEDIUM_13_FONT);
            row2.add(unitTypeLbl);
        }

        final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
        row3.setBackground(Skin.WHITE);
        panel.add(row3, StackedBorderLayout.NORTH);

        final JLabel reliefLbl = new JLabel("Relief given: " + appeal.reliefGiven);
        reliefLbl.setFont(Skin.MEDIUM_13_FONT);
        row3.add(reliefLbl);

        if (appeal.msDate != null) {
            final JLabel origDateLbl = new JLabel("Orig. date: " + TemporalUtils.FMT_MDY.format(appeal.msDate));
            origDateLbl.setFont(Skin.MEDIUM_13_FONT);
            row3.add(origDateLbl);
        }

        if (appeal.newDeadlineDt != null) {
            final JLabel newDateLbl = new JLabel("New date: " + TemporalUtils.FMT_MDY.format(appeal.newDeadlineDt));
            newDateLbl.setFont(Skin.MEDIUM_13_FONT);
            row3.add(newDateLbl);
        }

        if (appeal.nbrAtmptsAllow != null) {
            final JLabel attemptsLbl = new JLabel("Attempts: " + appeal.nbrAtmptsAllow);
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
            if (appeal.circumstances != null) {
                final JLabel circLbl = new JLabel(appeal.circumstances);
                circLbl.setFont(Skin.MEDIUM_13_FONT);
                row4.add(circLbl);
            }

            final JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
            row5.setBackground(Skin.WHITE);
            panel.add(row5, StackedBorderLayout.NORTH);
            row5.add(lbl[1]);
            if (appeal.comment != null) {
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
            if (this.currentStudentData != null) {
                if (this.addPaceAppealDialog == null) {
                    this.addPaceAppealDialog = new DlgAddPaceAppeal(this.cache, this);
                }

                this.addPaceAppealDialog.populateDisplay(this.fixed, this.currentStudentData);
                this.addPaceAppealDialog.setVisible(true);
                this.addPaceAppealDialog.toFront();
            }
        } else if (EDIT_ACCOMMODATION_CMD.equals(cmd)) {
            if (this.currentStudentData != null) {
                if (this.editAccommodationsDialog == null) {
                    this.editAccommodationsDialog = new DlgEditAccommodations(this.cache, this);
                }

                this.editAccommodationsDialog.populateDisplay(this.currentStudentData);
                this.editAccommodationsDialog.setVisible(true);
                this.editAccommodationsDialog.toFront();
            }
        } else if (cmd != null && cmd.startsWith(EDIT_APPEAL_CMD)) {
            final String sub = cmd.substring(EDIT_APPEAL_CMD.length());
            try {
                final int index = Integer.parseInt(sub);
                if (index >= 0 && this.appeals != null && index < this.appeals.size()) {
                    final RawPaceAppeals appeal = this.appeals.get(index);

                    if (this.currentStudentData != null) {
                        if (this.editPaceAppealDialog == null) {
                            this.editPaceAppealDialog = new DlgEditPaceAppeal(this.cache, this);
                        }

                        this.editPaceAppealDialog.populateDisplay(this.fixed, this.currentStudentData, appeal);
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
