package dev.mathops.app.adm.office.dialogs;

import dev.mathops.app.JDateChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A dialog to edit an existing row to "PACE_APPEALS" and (if relief is given) the related "STMILESTONE".
 */
public final class DlgEditPaceAppeal extends JFrame implements ActionListener, ItemListener {

    /** The dialog title. */
    private static final String TITLE = "Edit Pace Appeal";

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The options with which to populate the milestone type dropdown. */
    private static final String[] MS_TYPES = {"Review Exam", "Final Exam", "Final +1"};

    /** The data cache. */
    private final Cache cache;

    /** The record being edited. */
    private RawPaceAppeals currentRecord;

    /** The owning panel to be refreshed if an appeal record is added. */
    private final IPaceAppealsListener listener;

    /** The field for the student ID. */
    private final JTextField studentIdField;

    /** The field for the student Name. */
    private final JTextField studentNameField;

    /** The interviewer login name. */
    private final JTextField interviewerField;

    /** The appeal date. */
    private final JDateChooser appealDatePicker;

    /** The field for the student pace. */
    private final JTextField paceField;

    /** The field for the student pace track. */
    private final JTextField paceTrackField;

    /** The field for the course (from 1 to pace). */
    private final JTextField courseField;

    /** The field for the unit. */
    private final JTextField unitField;

    /** The milestone type chooser. */
    private final JComboBox<String> milestoneTypeDropdown;

    /** The checkbox to indicate relief was given. */
    private final JCheckBox reliefGiven;

    /** The original milestone date. */
    private final JDateChooser origDatePicker;

    /** The new milestone date. */
    private final JDateChooser newDatePicker;

    /** The number of attempts allowed. */
    private final JTextField attemptsAllowedField;

    /** The circumstances. */
    private final JTextArea circumstancesField;

    /** The comment. */
    private final JTextArea commentField;

    /** The "Apply" button". */
    private final JButton applyButton;

    /**
     * Constructs a new {@code DlgEditPaceAppeal}.
     *
     * @param theCache    the data cache
     * @param theListener the listener to be notified if an appeal record is added
     */
    public DlgEditPaceAppeal(final Cache theCache, final IPaceAppealsListener theListener) {

        super(TITLE);
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.listener = theListener;

        final JPanel content = AdmPanelBase.makeOffWhitePanel(new StackedBorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        content.setBorder(padding);
        setContentPane(content);

        final JLabel[] labels = new JLabel[12];

        labels[0] = new JLabel("Student ID: ");
        labels[1] = new JLabel("Student Name: ");
        labels[2] = new JLabel("Interviewer: ");
        labels[3] = new JLabel("Appeal Date: ");
        labels[4] = new JLabel("Pace: ");
        labels[5] = new JLabel("Pace Track: ");
        labels[6] = new JLabel("Course: ");
        labels[7] = new JLabel("Unit: ");
        labels[8] = new JLabel("Milestone: ");
        labels[9] = new JLabel("Original Date: ");
        labels[10] = new JLabel("New Deadline: ");
        labels[11] = new JLabel("Attempts: ");
        for (final JLabel lbl : labels) {
            lbl.setFont(Skin.MEDIUM_13_FONT);
            lbl.setForeground(Skin.LABEL_COLOR);
        }
        UIUtilities.makeLabelsSameSizeRightAligned(labels);

        this.studentIdField = new JTextField(9);
        this.studentIdField.setFont(Skin.MEDIUM_13_FONT);
        this.studentIdField.setEditable(false);

        this.studentNameField = new JTextField(20);
        this.studentNameField.setFont(Skin.MEDIUM_13_FONT);
        this.studentNameField.setEditable(false);

        this.interviewerField = new JTextField(12);
        this.interviewerField.setFont(Skin.MEDIUM_13_FONT);
        this.interviewerField.setEditable(true);

        final List<LocalDate> holidays = new ArrayList<>(10);

        try {
            final List<RawCampusCalendar> allHolidays = this.cache.getSystemData().getCampusCalendarsByType(
                    RawCampusCalendar.DT_DESC_HOLIDAY);
            for (final RawCampusCalendar holiday : allHolidays) {
                holidays.add(holiday.campusDt);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query holidays.");
        }

        final LocalDate today = LocalDate.now();
        this.appealDatePicker = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.appealDatePicker.setFont(Skin.MEDIUM_13_FONT);

        this.paceField = new JTextField(2);
        this.paceField.setFont(Skin.MEDIUM_13_FONT);

        this.paceTrackField = new JTextField(2);
        this.paceTrackField.setFont(Skin.MEDIUM_13_FONT);

        this.courseField = new JTextField(2);
        this.courseField.setFont(Skin.MEDIUM_13_FONT);

        this.unitField = new JTextField(2);
        this.unitField.setFont(Skin.MEDIUM_13_FONT);

        this.milestoneTypeDropdown = new JComboBox<>(MS_TYPES);
        this.milestoneTypeDropdown.setFont(Skin.MEDIUM_13_FONT);
        this.milestoneTypeDropdown.addItemListener(this);

        this.reliefGiven = new JCheckBox("Relief Given");
        this.reliefGiven.setFont(Skin.MEDIUM_13_FONT);

        this.origDatePicker = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.origDatePicker.setFont(Skin.MEDIUM_13_FONT);

        this.newDatePicker = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.newDatePicker.setFont(Skin.MEDIUM_13_FONT);

        this.attemptsAllowedField = new JTextField(2);
        this.attemptsAllowedField.setFont(Skin.MEDIUM_13_FONT);

        this.circumstancesField = new JTextArea(2, 30);
        this.circumstancesField.setFont(Skin.MEDIUM_13_FONT);
        this.circumstancesField.setBorder(this.attemptsAllowedField.getBorder());

        this.commentField = new JTextArea(2, 30);
        this.commentField.setFont(Skin.MEDIUM_13_FONT);
        this.commentField.setBorder(this.attemptsAllowedField.getBorder());

        this.applyButton = new JButton("Apply");
        this.applyButton.setFont(Skin.BUTTON_13_FONT);
        this.applyButton.setActionCommand(APPLY_CMD);
        this.applyButton.addActionListener(this);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(Skin.BUTTON_13_FONT);
        cancelButton.setActionCommand(CANCEL_CMD);
        cancelButton.addActionListener(this);

        final JPanel flow1 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow1.add(labels[0]);
        flow1.add(this.studentIdField);
        content.add(flow1, StackedBorderLayout.NORTH);

        final JPanel flow2 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow2.add(labels[1]);
        flow2.add(this.studentNameField);
        content.add(flow2, StackedBorderLayout.NORTH);
        final JPanel flow3 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow3.add(labels[2]);
        flow3.add(this.interviewerField);
        content.add(flow3, StackedBorderLayout.NORTH);

        final JPanel flow4 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow4.add(labels[3]);
        flow4.add(this.appealDatePicker);
        content.add(flow4, StackedBorderLayout.NORTH);

        final JPanel flow5 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow5.add(labels[4]);
        flow5.add(this.paceField);
        flow5.add(labels[5]);
        flow5.add(this.paceTrackField);
        content.add(flow5, StackedBorderLayout.NORTH);

        final JPanel flow6 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow6.add(labels[6]);
        flow6.add(this.courseField);
        flow6.add(labels[7]);
        flow6.add(this.unitField);
        content.add(flow6, StackedBorderLayout.NORTH);

        final JPanel flow7 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow7.add(labels[8]);
        flow7.add(this.milestoneTypeDropdown);
        flow7.add(new JLabel("      "));
        flow7.add(this.reliefGiven);
        content.add(flow7, StackedBorderLayout.NORTH);

        final JPanel flow8 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow8.add(labels[9]);
        flow8.add(this.origDatePicker);
        content.add(flow8, StackedBorderLayout.NORTH);

        final JPanel flow9 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow9.add(labels[10]);
        flow9.add(this.newDatePicker);
        content.add(flow9, StackedBorderLayout.NORTH);

        final JPanel flow10 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow10.add(labels[11]);
        flow10.add(this.attemptsAllowedField);
        content.add(flow10, StackedBorderLayout.NORTH);

        final JLabel circumstancesLbl = new JLabel("Circumstances:");
        circumstancesLbl.setFont(Skin.MEDIUM_13_FONT);
        circumstancesLbl.setForeground(Skin.LABEL_COLOR);
        content.add(circumstancesLbl, StackedBorderLayout.NORTH);
        content.add(this.circumstancesField, StackedBorderLayout.NORTH);

        final JLabel commentLbl = new JLabel("Comment:");
        commentLbl.setFont(Skin.MEDIUM_13_FONT);
        commentLbl.setForeground(Skin.LABEL_COLOR);
        content.add(commentLbl, StackedBorderLayout.NORTH);
        content.add(this.commentField, StackedBorderLayout.NORTH);

        final JPanel flow11 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        final Border padTop = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        flow11.setBorder(padTop);
        flow11.add(this.applyButton);
        flow11.add(cancelButton);
        content.add(flow11, StackedBorderLayout.NORTH);

        pack();
        final Dimension size = getSize();

        Container parent = theListener.getParent();
        while (parent != null) {
            if (parent instanceof final JFrame owningFrame) {
                final Rectangle bounds = owningFrame.getBounds();
                final int cx = bounds.x + (bounds.width / 2);
                final int cy = bounds.y + (bounds.height / 2);

                setLocation(cx - size.width / 2, cy - size.height / 2);
                break;
            }
            parent = parent.getParent();
        }
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data   the student data
     * @param record the record to edit
     */
    public void populateDisplay(final StudentData data, final RawPaceAppeals record) {

        this.currentRecord = record;

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);

        this.interviewerField.setText(record.interviewer);
        this.appealDatePicker.setCurrentDate(record.appealDt);
        this.paceField.setText(record.pace == null ? CoreConstants.EMPTY : record.pace.toString());
        this.paceTrackField.setText(record.paceTrack);

        if (record.msNbr == null) {
            this.courseField.setText(CoreConstants.EMPTY);
            this.unitField.setText(CoreConstants.EMPTY);
        } else {
            final int nbr = record.msNbr.intValue();
            final int course = (nbr / 10) % 10;
            final int unit = nbr % 10;
            final String courseStr = Integer.toString(course);
            this.courseField.setText(courseStr);
            final String unitStr = Integer.toString(unit);
            this.unitField.setText(unitStr);
        }

        if ("RE".equals(record.msType)) {
            this.milestoneTypeDropdown.setSelectedIndex(0);
        } else if ("FE".equals(record.msType)) {
            this.milestoneTypeDropdown.setSelectedIndex(1);
        } else if ("F1".equals(record.msType)) {
            this.milestoneTypeDropdown.setSelectedIndex(2);
        } else {
            this.milestoneTypeDropdown.setSelectedIndex(-1);
        }

        this.reliefGiven.setSelected("Y".equals(record.reliefGiven));

        this.origDatePicker.setCurrentDate(record.msDate);
        this.newDatePicker.setCurrentDate(record.newDeadlineDt);

        if (record.nbrAtmptsAllow == null) {
            this.attemptsAllowedField.setText(CoreConstants.EMPTY);
        } else {
            this.attemptsAllowedField.setText(Integer.toString(record.nbrAtmptsAllow));
        }

        this.circumstancesField.setText(record.circumstances);
        this.commentField.setText(record.comment);

        this.applyButton.setEnabled(true);
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (APPLY_CMD.equals(cmd)) {
            final String error = validateFields();
            if (error == null) {
                final String[] errors = doUpdateAppeal();
                if (errors == null) {
                    this.listener.updateAppeals();
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, errors, TITLE, JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, error, TITLE, JOptionPane.ERROR_MESSAGE);
            }

        } else if (CANCEL_CMD.equals(cmd)) {
            setVisible(false);
        }
    }

    /**
     * Validates inputs.
     *
     * @return null if inputs are valid, an error message if not
     */
    private String validateFields() {

        Log.info("Validating...");

        String error = null;

        if (this.appealDatePicker.getCurrentDate() == null) {
            error = "Appeal date must be set.";
        }

        boolean hasPace = false;
        int paceInt = 0;
        try {
            final String paceText = this.paceField.getText();
            paceInt = Integer.parseInt(paceText);
            hasPace = true;
        } catch (final NumberFormatException ex) {
            error = "Invalid Pace.";
        }

        int courseInt = 0;
        try {
            final String courseText = this.courseField.getText();
            courseInt = Integer.parseInt(courseText);

            if (courseInt < 0 || (hasPace && courseInt > paceInt)) {
                error = "Course number must fall within pace.";
            }
        } catch (final NumberFormatException ex) {
            error = "Invalid Course number.";
        }

        int unitInt = 0;
        try {
            final String unitText = this.unitField.getText();
            unitInt = Integer.parseInt(unitText);

            if (unitInt < 0 || unitInt > 5) {
                error = "Unit number not in valid range.";
            }
        } catch (final NumberFormatException ex) {
            error = "Invalid Course number.";
        }

        final int msIndex = this.milestoneTypeDropdown.getSelectedIndex();
        if (msIndex == -1) {
            error = "A milestone type must be selected.";
        }

        if (this.origDatePicker.getCurrentDate() == null) {
            error = "Original deadline date may not be null.";
        } else if (this.interviewerField.getText() == null
                || this.interviewerField.getText().isBlank()) {
            error = "Interviewer may not be blank.";
        } else if (this.circumstancesField.getText() == null
                || this.circumstancesField.getText().isBlank()) {
            error = "Circumstances field may not be blank.";
        }

        if (error == null) {
            final String attemptsText = this.attemptsAllowedField.getText();
            if (attemptsText != null && !attemptsText.isBlank()) {
                try {
                    Integer.parseInt(attemptsText);
                } catch (final NumberFormatException ex) {
                    error = "Invalid number of attempts.";
                }
            }
        }

        return error;
    }

    /**
     * Attempts to insert the new pace appeal record.
     *
     * @return null if insert succeeded; an error message if not
     */
    private String[] doUpdateAppeal() {

        String error[] = null;

        Log.info("doUpdateAppeals()");

        if (this.currentRecord == null) {
            error = new String[]{"No current record to edit."};
        } else {
            try {
                final LocalDate appealDate = this.appealDatePicker.getCurrentDate();
                final String relief = this.reliefGiven.isSelected() ? "Y" : "N";

                final String paceText = this.paceField.getText();
                final Integer paceInt = Integer.valueOf(paceText);

                final String paceTrack = this.paceTrackField.getText();

                final String courseText = this.courseField.getText();
                final Integer courseInt = Integer.valueOf(courseText);

                final String unitText = this.unitField.getText();
                final Integer unitInt = Integer.valueOf(unitText);

                final int number = paceInt.intValue() * 100 + courseInt.intValue() * 10 + unitInt.intValue();
                final Integer msNbr = Integer.valueOf(number);

                final String msType;
                if (this.milestoneTypeDropdown.getSelectedIndex() == 0) {
                    msType = "RE";
                } else if (this.milestoneTypeDropdown.getSelectedIndex() == 1) {
                    msType = "FE";
                } else if (this.milestoneTypeDropdown.getSelectedIndex() == 2) {
                    msType = "F1";
                } else {
                    msType = null;
                }

                final LocalDate msDate = this.origDatePicker.getCurrentDate();
                final LocalDate newDate = this.newDatePicker.getCurrentDate();

                final String attemptsText = this.attemptsAllowedField.getText();
                final Integer numAttempts = attemptsText == null || attemptsText.isBlank() ? null
                        : Integer.valueOf(attemptsText);

                final String circumstances = this.circumstancesField.getText();
                final String comment = this.commentField.getText();
                final String interviewer = this.interviewerField.getText();

                final boolean changed = !(Objects.equals(this.currentRecord.appealDt, appealDate)
                        && Objects.equals(this.currentRecord.reliefGiven, relief)
                        && Objects.equals(this.currentRecord.pace, paceInt)
                        && Objects.equals(this.currentRecord.paceTrack, paceTrack)
                        && Objects.equals(this.currentRecord.msNbr, msNbr)
                        && Objects.equals(this.currentRecord.msType, msType)
                        && Objects.equals(this.currentRecord.msDate, msDate)
                        && Objects.equals(this.currentRecord.newDeadlineDt, newDate)
                        && Objects.equals(this.currentRecord.nbrAtmptsAllow, numAttempts)
                        && Objects.equals(this.currentRecord.circumstances, circumstances)
                        && Objects.equals(this.currentRecord.comment, comment)
                        && Objects.equals(this.currentRecord.interviewer, interviewer));

                if (changed) {
                    final RawPaceAppeals newRecord = new RawPaceAppeals(this.currentRecord.termKey,
                            this.currentRecord.stuId, appealDate, relief, paceInt, paceTrack, msNbr, msType,
                            msDate, newDate, numAttempts, circumstances, comment, interviewer);

                    Log.info("Replacing appeal record");
                    RawPaceAppealsLogic.INSTANCE.delete(this.cache, this.currentRecord);
                    RawPaceAppealsLogic.INSTANCE.insert(this.cache, newRecord);
                } else {
                    Log.info("Nothing changed - skipping update");
                }
            } catch (final NumberFormatException ex) {
                error = new String[]{"Invalid Course number."};
            } catch (final SQLException ex) {
                error = new String[]{"There was an error updating the record.", ex.getLocalizedMessage()};
            }
        }

        return error;
    }

    /**
     * Called when the selected item changes in any dropdown.
     *
     * @param e the event to be processed
     */
    @Override
    public void itemStateChanged(final ItemEvent e) {

        final String error = validateFields();
        this.applyButton.setEnabled(error == null);
    }
}

