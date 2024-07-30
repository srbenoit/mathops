package dev.mathops.app.adm.office.registration;

import dev.mathops.app.JDateChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.office.student.IPaceAppealsListener;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawStmilestone;
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
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
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
import java.util.Objects;

/**
 * A dialog to edit an extension.  This edits the new deadline date and number of attempts values in the "stmilestone"
 * record, and tries to find the matching "pace_appeals" record to allow the user to edit that as well.  The deadline
 * dates in the two edits are synchronized.
 */
public final class DlgEditExtension extends JFrame implements ActionListener, DocumentListener {

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** An action command. */
    private static final String VALIDATE_CMD = "VALIDATE_CMD";

    /** The dialog title. */
    private static final String TITLE = "Edit Deadline Extension";

    /** The options with which to populate the milestone type dropdown. */
    private static final String[] MS_TYPES = {"Review Exam", "Final Exam", "Final +1"};

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey active;

    /** The owning panel to be refreshed if an appeal record is added. */
    private final IPaceAppealsListener listener;

    /** The current milestone. */
    private RawMilestone milestone;

    /** The current extension. */
    private RawStmilestone extension;

    /** The current appeal. */
    private RawPaceAppeals appeal;

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

    /** The original date. */
    private final JTextField origDate;

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
     * Constructs a new {@code DlgEditExtension}.
     *
     * @param theCache    the data cache
     * @param theListener the listener to be notified if an appeal record is added
     */
    DlgEditExtension(final Cache theCache, final IPaceAppealsListener theListener) {

        super(TITLE);
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.listener = theListener;

        TermKey activeKey = null;
        try {
            final SystemData systemData = theCache.getSystemData();
            final TermRec activeTerm = systemData.getActiveTerm();
            if (activeTerm != null) {
                activeKey = activeTerm.term;
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query milestones", ex);
        }
        this.active = activeKey;

        final JPanel content = AdmPanelBase.makeOffWhitePanel(new StackedBorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        content.setBorder(padding);
        setContentPane(content);

        // Left side is "pace appeals" record, right side will be new milestone, if applicable

        final JPanel paceAppeal = AdmPanelBase.makeOffWhitePanel(new StackedBorderLayout());
        content.add(paceAppeal, StackedBorderLayout.WEST);
        final Border padRightBottom = BorderFactory.createEmptyBorder(0, 0, 10, 10);
        paceAppeal.setBorder(padRightBottom);

        final JLabel[] leftLabels = new JLabel[12];

        leftLabels[0] = new JLabel("Student ID: ");
        leftLabels[1] = new JLabel("Student Name: ");
        leftLabels[2] = new JLabel("Interviewer: ");
        leftLabels[3] = new JLabel("Appeal Date: ");
        leftLabels[4] = new JLabel("Pace: ");
        leftLabels[5] = new JLabel("Pace Track: ");
        leftLabels[6] = new JLabel("Course: ");
        leftLabels[7] = new JLabel("Unit: ");
        leftLabels[8] = new JLabel("Milestone: ");
        leftLabels[9] = new JLabel("Original Date: ");
        leftLabels[10] = new JLabel("New Deadline: ");
        leftLabels[11] = new JLabel("Attempts: ");
        for (final JLabel lbl : leftLabels) {
            lbl.setFont(Skin.BODY_12_FONT);
            lbl.setForeground(Skin.LABEL_COLOR);
        }
        UIUtilities.makeLabelsSameSizeRightAligned(leftLabels);

        this.studentIdField = new JTextField(9);
        this.studentIdField.setFont(Skin.BODY_12_FONT);
        this.studentIdField.setEditable(false);

        this.studentNameField = new JTextField(20);
        this.studentNameField.setFont(Skin.BODY_12_FONT);
        this.studentNameField.setEditable(false);

        this.interviewerField = new JTextField(12);
        this.interviewerField.setFont(Skin.BODY_12_FONT);
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
        this.appealDatePicker = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.appealDatePicker.setFont(Skin.BODY_12_FONT);
        this.appealDatePicker.setActionCommand(VALIDATE_CMD);

        this.paceField = new JTextField(2);
        this.paceField.setFont(Skin.BODY_12_FONT);
        this.paceField.setEditable(false);

        this.paceTrackField = new JTextField(2);
        this.paceTrackField.setFont(Skin.BODY_12_FONT);
        this.paceTrackField.setEditable(false);

        this.courseField = new JTextField(2);
        this.courseField.setFont(Skin.BODY_12_FONT);
        this.courseField.setEditable(false);

        this.unitField = new JTextField(2);
        this.unitField.setFont(Skin.BODY_12_FONT);
        this.unitField.setEditable(false);

        this.milestoneTypeDropdown = new JComboBox<>(MS_TYPES);
        this.milestoneTypeDropdown.setFont(Skin.BODY_12_FONT);
        this.milestoneTypeDropdown.setActionCommand(VALIDATE_CMD);
        this.milestoneTypeDropdown.setEnabled(false);

        this.reliefGiven = new JCheckBox("Relief Given");
        this.reliefGiven.setFont(Skin.BODY_12_FONT);
        this.reliefGiven.setActionCommand(VALIDATE_CMD);

        this.origDate = new JTextField(2);
        this.origDate.setFont(Skin.BODY_12_FONT);
        this.origDate.setEditable(false);

        this.newDatePicker = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.newDatePicker.setFont(Skin.BODY_12_FONT);
        this.newDatePicker.setActionCommand(VALIDATE_CMD);

        this.attemptsAllowedField = new JTextField(2);
        this.attemptsAllowedField.setFont(Skin.BODY_12_FONT);
        this.attemptsAllowedField.setEditable(true);

        this.circumstancesField = new JTextArea(2, 30);
        this.circumstancesField.setFont(Skin.BODY_12_FONT);
        this.circumstancesField.setBorder(this.attemptsAllowedField.getBorder());
        this.circumstancesField.setEditable(true);

        this.commentField = new JTextArea(2, 30);
        this.commentField.setFont(Skin.BODY_12_FONT);
        this.commentField.setBorder(this.attemptsAllowedField.getBorder());

        this.applyButton = new JButton("Apply");
        this.applyButton.setFont(Skin.BUTTON_13_FONT);
        this.applyButton.setActionCommand(APPLY_CMD);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(Skin.BUTTON_13_FONT);
        cancelButton.setActionCommand(CANCEL_CMD);

        final JPanel flow1 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow1.add(leftLabels[0]);
        flow1.add(this.studentIdField);
        paceAppeal.add(flow1, StackedBorderLayout.NORTH);

        final JPanel flow2 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow2.add(leftLabels[1]);
        flow2.add(this.studentNameField);
        paceAppeal.add(flow2, StackedBorderLayout.NORTH);
        final JPanel flow3 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow3.add(leftLabels[2]);
        flow3.add(this.interviewerField);
        paceAppeal.add(flow3, StackedBorderLayout.NORTH);

        final JPanel flow4 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow4.add(leftLabels[3]);
        flow4.add(this.appealDatePicker);
        paceAppeal.add(flow4, StackedBorderLayout.NORTH);

        final JPanel flow5 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow5.add(leftLabels[4]);
        flow5.add(this.paceField);
        flow5.add(leftLabels[5]);
        flow5.add(this.paceTrackField);
        paceAppeal.add(flow5, StackedBorderLayout.NORTH);

        final JPanel flow6 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow6.add(leftLabels[6]);
        flow6.add(this.courseField);
        flow6.add(leftLabels[7]);
        flow6.add(this.unitField);
        paceAppeal.add(flow6, StackedBorderLayout.NORTH);

        final JPanel flow7 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow7.add(leftLabels[8]);
        flow7.add(this.milestoneTypeDropdown);
        flow7.add(new JLabel("      "));
        flow7.add(this.reliefGiven);
        paceAppeal.add(flow7, StackedBorderLayout.NORTH);

        final JPanel flow8 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow8.add(leftLabels[9]);
        flow8.add(this.origDate);
        paceAppeal.add(flow8, StackedBorderLayout.NORTH);

        final JPanel flow9 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow9.add(leftLabels[10]);
        flow9.add(this.newDatePicker);
        paceAppeal.add(flow9, StackedBorderLayout.NORTH);

        final JPanel flow10 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow10.add(leftLabels[11]);
        flow10.add(this.attemptsAllowedField);
        paceAppeal.add(flow10, StackedBorderLayout.NORTH);

        final JLabel circumstancesLbl = new JLabel("Circumstances:");
        circumstancesLbl.setFont(Skin.BODY_12_FONT);
        circumstancesLbl.setForeground(Skin.LABEL_COLOR);
        paceAppeal.add(circumstancesLbl, StackedBorderLayout.NORTH);
        paceAppeal.add(this.circumstancesField, StackedBorderLayout.NORTH);

        final JLabel commentLbl = new JLabel("Comment:");
        commentLbl.setFont(Skin.BODY_12_FONT);
        commentLbl.setForeground(Skin.LABEL_COLOR);
        paceAppeal.add(commentLbl, StackedBorderLayout.NORTH);
        paceAppeal.add(this.commentField, StackedBorderLayout.NORTH);

        // Buttons bar at the bottom

        final JPanel flow11 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.CENTER, 5, 3));
        final Border lineAbove = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY);
        final Border padAbove = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        final Border buttonBarBorder = BorderFactory.createCompoundBorder(lineAbove, padAbove);
        flow11.setBorder(buttonBarBorder);
        flow11.add(this.applyButton);
        flow11.add(cancelButton);
        content.add(flow11, StackedBorderLayout.SOUTH);

        this.appealDatePicker.addActionListener(this);
        this.paceField.getDocument().addDocumentListener(this);
        this.courseField.getDocument().addDocumentListener(this);
        this.unitField.getDocument().addDocumentListener(this);
        this.milestoneTypeDropdown.addActionListener(this);
        this.reliefGiven.addActionListener(this);
        this.newDatePicker.addActionListener(this);
        this.attemptsAllowedField.getDocument().addDocumentListener(this);
        this.circumstancesField.getDocument().addDocumentListener(this);
        this.applyButton.addActionListener(this);
        cancelButton.addActionListener(this);

        //

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
     * @param userData     the user data
     * @param data         the student data
     * @param theMilestone the milestone
     * @param theExtension the extension record being edited
     * @param theAppeal    the associated pace appeal, {@code null} of there is no matching appeal record
     */
    public void populateDisplay(final UserData userData, final StudentData data, final RawMilestone theMilestone,
                                final RawStmilestone theExtension, final RawPaceAppeals theAppeal) {

        this.milestone = theMilestone;
        this.extension = theExtension;
        this.appeal = theAppeal;

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);

        final LocalDate today = LocalDate.now();
        this.interviewerField.setText(userData.username);
        this.appealDatePicker.setCurrentDate(today);

        final int pace = theExtension.getPace();
        final String paceStr = Integer.toString(pace);
        this.paceField.setText(paceStr);

        this.paceTrackField.setText(theExtension.paceTrack);

        final int courseIndex = theExtension.getIndex();
        final String courseIndexStr = Integer.toString(courseIndex);
        this.courseField.setText(courseIndexStr);

        final int unit = theExtension.getUnit();
        final String unitStr = Integer.toString(unit);
        this.unitField.setText(unitStr);

        if ("RE".equals(theExtension.msType)) {
            this.milestoneTypeDropdown.setSelectedIndex(0);
        } else if ("FE".equals(theExtension.msType)) {
            this.milestoneTypeDropdown.setSelectedIndex(1);
        } else if ("F1".equals(theExtension.msType)) {
            this.milestoneTypeDropdown.setSelectedIndex(2);
        } else {
            this.milestoneTypeDropdown.setSelectedIndex(-1);
        }

        final LocalDate orig;
        if (theAppeal == null) {
            orig = theMilestone.msDate;
        } else {
            orig = theAppeal.msDate;
        }
        final String origStr = TemporalUtils.FMT_MDY.format(orig);
        this.origDate.setText(origStr);

        this.newDatePicker.setCurrentDate(theExtension.msDate);

        if (theExtension.nbrAtmptsAllow == null) {
            this.attemptsAllowedField.setText(CoreConstants.EMPTY);
        } else {
            final String attemptsStr = theExtension.nbrAtmptsAllow.toString();
            this.unitField.setText(attemptsStr);
        }

        if (theAppeal == null) {
            this.appealDatePicker.setCurrentDate(today);
            this.appealDatePicker.setEnabled(true);
            this.reliefGiven.setSelected(true);
            this.circumstancesField.setText(CoreConstants.EMPTY);
            this.commentField.setText(CoreConstants.EMPTY);
            this.interviewerField.setText(userData.username);
        } else {
            this.appealDatePicker.setCurrentDate(theAppeal.appealDt);
            this.appealDatePicker.setEnabled(false);
            final boolean wasReliefGiven = "Y".equals(theAppeal.reliefGiven);
            this.reliefGiven.setSelected(wasReliefGiven);
            this.circumstancesField.setText(theAppeal.circumstances);
            this.commentField.setText(theAppeal.comment);
            this.interviewerField.setText(theAppeal.interviewer);
        }

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
                final String[] errors = doApply();
                if (errors == null) {
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, errors, TITLE, JOptionPane.ERROR_MESSAGE);
                }
                this.listener.updateAppeals();
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

        if (this.newDatePicker.getCurrentDate() == null) {
            error = "Deadline date may not be null.";
        }

        if (this.milestoneTypeDropdown.getSelectedIndex() == 2) {
            final String attemptsStr = this.attemptsAllowedField.getText();
            if (attemptsStr == null || attemptsStr.isBlank()) {
                error = "Number of attempts required for F1 milestone.";
            } else {
                try {
                    Integer.parseInt(attemptsStr);
                } catch (final NumberFormatException ex) {
                    error = "Invalid number of attempts.";
                }
            }
        }

        final String interviewer = this.interviewerField.getText();
        if (interviewer == null || interviewer.isBlank()) {
            error = "Interviewer may not be blank.";
        }

        return error;
    }

    /**
     * Attempts to insert the new pace appeal record.
     *
     * @return null if insert succeeded; an error message if not
     */
    private String[] doApply() {

        String error[] = null;

        if (this.milestone == null || this.extension == null) {
            error = new String[]{"No current extension to edit."};
        } else {
            try {
                final String stuId = this.studentIdField.getText();

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

                final LocalDate newDate = this.newDatePicker.getCurrentDate();

                final String attemptsText = this.attemptsAllowedField.getText();
                final Integer numAttempts = attemptsText == null || attemptsText.isBlank() ? null
                        : Integer.valueOf(attemptsText);

                final String circumstances = this.circumstancesField.getText();
                final String comment = this.commentField.getText();
                final String interviewer = this.interviewerField.getText();

                if (!(Objects.equals(newDate, this.extension.msDate)
                        && Objects.equals(numAttempts, this.extension.nbrAtmptsAllow))) {

                    // The "STMILESTONE" record needs to be updated.
                    this.extension.msDate = newDate;
                    this.extension.nbrAtmptsAllow = numAttempts;

                    if (!RawStmilestoneLogic.update(this.cache, this.extension)) {
                        error = new String[]{"Update of student milestone record failed."};
                    }
                }

                if (this.appeal == null) {
                    final RawPaceAppeals newAppeal = new RawPaceAppeals(this.active, stuId, appealDate, relief,
                            this.milestone.pace, this.milestone.paceTrack, this.milestone.msNbr, this.milestone.msType,
                            this.milestone.msDate, newDate, numAttempts, circumstances, comment, interviewer);

                    RawPaceAppealsLogic.INSTANCE.insert(this.cache, newAppeal);

                } else if (!(Objects.equals(interviewer, this.appeal.interviewer)
                        && Objects.equals(circumstances, this.appeal.circumstances)
                        && Objects.equals(comment, this.appeal.comment)
                        && Objects.equals(newDate, this.appeal.newDeadlineDt)
                        && Objects.equals(numAttempts, this.appeal.nbrAtmptsAllow))) {

                    this.appeal.interviewer = interviewer;
                    this.appeal.circumstances = circumstances;
                    this.appeal.comment = comment;
                    this.appeal.newDeadlineDt = newDate;
                    this.appeal.nbrAtmptsAllow = numAttempts;

                    RawPaceAppealsLogic.update(this.cache, this.appeal);
                }

            } catch (final NumberFormatException ex) {
                error = new String[]{"Invalid Course number."};
            } catch (final SQLException ex) {
                error = new String[]{"There was an error inserting the new record.", ex.getLocalizedMessage()};
            }
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

