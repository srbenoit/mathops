package dev.mathops.app.adm.student;

import dev.mathops.app.JDateChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

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
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog to add a row to "PACE_APPEALS" and (if relief is given) to "STMILESTONE".
 */
final class DlgAddPaceAppeal extends JFrame implements ActionListener, DocumentListener {

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** An action command. */
    private static final String VALIDATE_CMD = "VALIDATE_CMD";

    /** The dialog title. */
    private static final String TITLE = "Add Pace Appeal";

    /** The options with which to populate the milestone type dropdown. */
    private static final String[] MS_TYPES = {"Review Exam", "Final Exam", "Final +1"};

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey active;

    /** The list of all milestones. */
    private final List<RawMilestone> allMilestones;

    /** The owning panel to be refreshed if an appeal record is added. */
    private final StuAppealsPanel owner;

    /** The field for the student ID. */
    private final JTextField studentIdField;

    /** The field for the student Name. */
    private final JTextField studentNameField;

    /** The interviewer login name. */
    private final JTextField interviewerField;

    /** The appeal date. */
    private final JDateChooser appealDatePicker;

    /** A label in which to show the student's status. */
    private final JLabel statusLabel;

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
     * Constructs a new {@code DlgAddPaceAppeal}.
     *
     * @param theCache the data cache
     * @param theOwner the owning panel to be refreshed if an appeal record is added
     */
    DlgAddPaceAppeal(final Cache theCache, final StuAppealsPanel theOwner) {

        super(TITLE);
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.owner = theOwner;

        this.allMilestones = new ArrayList<>(50);
        TermKey activeKey = null;
        try {
            final SystemData systemData = theCache.getSystemData();
            final TermRec activeTerm = systemData.getActiveTerm();
            if (activeTerm != null) {
                activeKey = activeTerm.term;
                final List<RawMilestone> termMilestones = systemData.getMilestones(activeKey);
                this.allMilestones.addAll(termMilestones);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query milestones", ex);
        }
        this.active = activeKey;

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
        this.interviewerField.getDocument().addDocumentListener(this);

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
        this.appealDatePicker = new JDateChooser(today, holidays);
        this.appealDatePicker.setFont(Skin.MEDIUM_13_FONT);
        this.appealDatePicker.setActionCommand(VALIDATE_CMD);
        this.appealDatePicker.addActionListener(this);

        this.statusLabel = new JLabel("Student is not enrolled in any paced courses");
        this.statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusLabel.setFont(Skin.MEDIUM_13_FONT);

        this.paceField = new JTextField(2);
        this.paceField.setFont(Skin.MEDIUM_13_FONT);
        this.paceField.setEditable(true);
        this.paceField.getDocument().addDocumentListener(this);

        this.paceTrackField = new JTextField(2);
        this.paceTrackField.setFont(Skin.MEDIUM_13_FONT);

        this.courseField = new JTextField(2);
        this.courseField.setFont(Skin.MEDIUM_13_FONT);
        this.courseField.setEditable(true);
        this.courseField.getDocument().addDocumentListener(this);

        this.unitField = new JTextField(2);
        this.unitField.setFont(Skin.MEDIUM_13_FONT);
        this.unitField.setEditable(true);
        this.unitField.getDocument().addDocumentListener(this);

        this.milestoneTypeDropdown = new JComboBox<>(MS_TYPES);
        this.milestoneTypeDropdown.setFont(Skin.MEDIUM_13_FONT);
        this.milestoneTypeDropdown.setActionCommand(VALIDATE_CMD);
        this.milestoneTypeDropdown.addActionListener(this);

        this.reliefGiven = new JCheckBox("Relief Given");
        this.reliefGiven.setFont(Skin.MEDIUM_13_FONT);

        this.origDatePicker = new JDateChooser(today, holidays);
        this.origDatePicker.setFont(Skin.MEDIUM_13_FONT);
        this.origDatePicker.setActionCommand(VALIDATE_CMD);
        this.origDatePicker.addActionListener(this);

        this.newDatePicker = new JDateChooser(today, holidays);
        this.newDatePicker.setFont(Skin.MEDIUM_13_FONT);
        this.newDatePicker.setActionCommand(VALIDATE_CMD);
        this.newDatePicker.addActionListener(this);

        this.attemptsAllowedField = new JTextField(2);
        this.attemptsAllowedField.setFont(Skin.MEDIUM_13_FONT);
        this.attemptsAllowedField.setEditable(true);
        this.attemptsAllowedField.getDocument().addDocumentListener(this);

        this.circumstancesField = new JTextArea(3, 30);
        this.circumstancesField.setFont(Skin.MEDIUM_13_FONT);
        this.circumstancesField.setBorder(this.attemptsAllowedField.getBorder());
        this.circumstancesField.setEditable(true);
        this.circumstancesField.getDocument().addDocumentListener(this);

        this.commentField = new JTextArea(3, 30);
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

        final JPanel flow1 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow1.add(labels[0]);
        flow1.add(this.studentIdField);
        content.add(flow1, StackedBorderLayout.NORTH);

        final JPanel flow2 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow2.add(labels[1]);
        flow2.add(this.studentNameField);
        content.add(flow2, StackedBorderLayout.NORTH);
        final JPanel flow3 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow3.add(labels[2]);
        flow3.add(this.interviewerField);
        content.add(flow3, StackedBorderLayout.NORTH);

        final JPanel flow4 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow4.add(labels[3]);
        flow4.add(this.appealDatePicker);
        content.add(flow4, StackedBorderLayout.NORTH);

        content.add(this.statusLabel, StackedBorderLayout.NORTH);

        final JPanel flow5 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow5.add(labels[4]);
        flow5.add(this.paceField);
        flow5.add(labels[5]);
        flow5.add(this.paceTrackField);
        content.add(flow5, StackedBorderLayout.NORTH);

        final JPanel flow6 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow6.add(labels[6]);
        flow6.add(this.courseField);
        flow6.add(labels[7]);
        flow6.add(this.unitField);
        content.add(flow6, StackedBorderLayout.NORTH);

        final JPanel flow7 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow7.add(labels[8]);
        flow7.add(this.milestoneTypeDropdown);
        flow7.add(new JLabel("      "));
        flow7.add(this.reliefGiven);
        content.add(flow7, StackedBorderLayout.NORTH);

        final JPanel flow8 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow8.add(labels[9]);
        flow8.add(this.origDatePicker);
        content.add(flow8, StackedBorderLayout.NORTH);

        final JPanel flow9 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow9.add(labels[10]);
        flow9.add(this.newDatePicker);
        content.add(flow9, StackedBorderLayout.NORTH);

        final JPanel flow10 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
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
        flow11.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        flow11.add(this.applyButton);
        flow11.add(cancelButton);
        content.add(flow11, StackedBorderLayout.NORTH);

        pack();
        final Dimension size = getSize();

        Container parent = theOwner.getParent();
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
     * @param fixed the fixed data with logged-in user information
     * @param data  the student data
     */
    void populateDisplay(final FixedData fixed, final StudentData data) {

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);

        final LocalDate today = LocalDate.now();

        this.interviewerField.setText(fixed.username);
        this.appealDatePicker.setDate(today);

        if (data.studentTerm == null) {
            this.statusLabel.setText("Student is not enrolled in any paced courses");
            this.paceField.setText("0");
            this.paceTrackField.setText(CoreConstants.EMPTY);
            this.courseField.setText("0");
            this.unitField.setText("0");
            this.milestoneTypeDropdown.setSelectedIndex(0);
            this.origDatePicker.setDate(today);
            this.newDatePicker.setDate(today);
        } else {
            this.statusLabel.setText("Student is not enrolled in any paced courses");

            this.paceField.setText(data.studentTerm.pace == null ? CoreConstants.EMPTY :
                    data.studentTerm.pace.toString());
            this.paceTrackField.setText(data.studentTerm.paceTrack);
            this.courseField.setText(CoreConstants.EMPTY);
            this.unitField.setText(CoreConstants.EMPTY);
            this.milestoneTypeDropdown.setSelectedIndex(-1);
            this.origDatePicker.setDate(null);
            this.newDatePicker.setDate(null);
        }

        this.reliefGiven.setSelected(false);
        this.attemptsAllowedField.setText(CoreConstants.EMPTY);
        this.circumstancesField.setText(CoreConstants.EMPTY);
        this.commentField.setText(CoreConstants.EMPTY);

        this.applyButton.setEnabled(false);
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (VALIDATE_CMD.equals(cmd)) {
            final String error = validateFields();
            this.applyButton.setEnabled(error == null);
        } else if (APPLY_CMD.equals(cmd)) {
            final String error = validateFields();
            if (error == null) {
                final String[] errors = doInsert();
                if (errors == null) {
                    this.owner.updateAppeals();
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

        String error = null;

        if (this.appealDatePicker.getDate() == null) {
            error = "Appeal date must be set.";
        } else {
            try {
                final String paceText = this.paceField.getText();
                final int paceInt = Integer.parseInt(paceText);

                try {
                    final String courseText = this.courseField.getText();
                    final int courseInt = Integer.parseInt(courseText);

                    if (courseInt < 0 || courseInt > paceInt) {
                        error = "Course number must fall within pace.";
                    } else {
                        final String unitText = this.unitField.getText();
                        final int unitInt = Integer.parseInt(unitText);

                        if (unitInt < 0 || unitInt > 5) {
                            error = "Unit number not in valid range.";
                        } else if (this.milestoneTypeDropdown.getSelectedIndex() == -1) {
                            error = "A milestone type must be selected.";
                        } else if (this.origDatePicker.getDate() == null) {
                            error = "Original deadline date may not be null.";
                        } else if (this.interviewerField.getText() == null
                                || this.interviewerField.getText().isBlank()) {
                            error = "Interviewer may not be blank.";
                        } else if (this.circumstancesField.getText() == null
                                || this.circumstancesField.getText().isBlank()) {
                            error = "Circumstances field may not be blank.";
                        }
                    }
                } catch (final NumberFormatException ex) {
                    error = "Invalid Course number.";
                }
            } catch (final NumberFormatException ex) {
                error = "Invalid Pace.";
            }
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
    private String[] doInsert() {

        String error[] = null;

        try {
            final TermRec activeTerm = this.cache.getSystemData().getActiveTerm();
            final TermKey active = activeTerm == null ? null : activeTerm.term;

            final String stuId = this.studentIdField.getText();

            final LocalDate appealDate = this.appealDatePicker.getDate();

            final String relief = this.reliefGiven.isSelected() ? "Y" : "N";

            final String paceText = this.paceField.getText();
            final Integer paceInt = Integer.valueOf(paceText);

            final String paceTrack = this.paceTrackField.getText();

            final String courseText = this.courseField.getText();
            final Integer courseInt = Integer.valueOf(courseText);

            final String unitText = this.unitField.getText();
            final Integer unitInt = Integer.valueOf(unitText);

            final Integer msNbr;
            if (paceInt == null || courseInt == null || unitInt == null) {
                msNbr = null;
            } else {
                final int number = paceInt.intValue() * 100 + courseInt.intValue() * 10 + unitInt.intValue();
                msNbr = Integer.valueOf(number);
            }

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

            final LocalDate msDate = this.origDatePicker.getDate();
            final LocalDate newDate = this.newDatePicker.getDate();

            final String attemptsText = this.attemptsAllowedField.getText();
            final Integer numAttempts = attemptsText == null || attemptsText.isBlank() ? null
                    : Integer.valueOf(attemptsText);

            final String circumstances = this.circumstancesField.getText();
            final String comment = this.commentField.getText();
            final String interviewer = this.interviewerField.getText();

            final RawPaceAppeals newRecord = new RawPaceAppeals(active, stuId, appealDate, relief, paceInt, paceTrack,
                    msNbr, msType, msDate, newDate, numAttempts, circumstances, comment, interviewer);

            RawPaceAppealsLogic.INSTANCE.insert(this.cache, newRecord);

            // TODO: If relief was given, also update STMILESTONE

        } catch (final NumberFormatException ex) {
            error = new String[]{"Invalid Course number."};
        } catch (final SQLException ex) {
            error = new String[]{"There was an error inserting the new record.", ex.getLocalizedMessage()};
        }

        return error;
    }

    /**
     * Called when content is inserted into a text field.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {

        final String error = validateFields();
        this.applyButton.setEnabled(error == null);
    }

    /**
     * Called when content is removed from a text field.
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {

        final String error = validateFields();
        this.applyButton.setEnabled(error == null);
    }

    /**
     * Called when text field content is updated.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {

        final String error = validateFields();
        this.applyButton.setEnabled(error == null);
    }
}

