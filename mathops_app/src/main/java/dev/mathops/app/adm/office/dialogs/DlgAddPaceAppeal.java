package dev.mathops.app.adm.office.dialogs;

import dev.mathops.app.JDateChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.app.adm.UserData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.SimpleBuilder;
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
import javax.swing.SwingConstants;
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
 * A dialog to add a row to "PACE_APPEALS" and (if relief is given) to "STMILESTONE" or "STU_STD_MILESTONE".
 */
public final class DlgAddPaceAppeal extends JFrame implements ActionListener, DocumentListener {

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

    /** The initial milestone this dialog was created to edit. */
    private final RawMilestone initialMilestone;

    /** The list of all milestones. */
    private final List<RawMilestone> allMilestones;

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

    /** The current milestone description (like "Unit 1 Review Exam" or "Unit 1, Standard 1.3 Mastery"). */
    private final JTextField overrideDescription;

    /** The current milestone date. */
    private final JTextField overrideOriginalDate;

    /** The current milestone date source (like "Original milestone" or "Milestone override"). */
    private final JTextField overrideOriginalSource;

    /** The new milestone date. */
    private final JDateChooser overrideNewDate;

    /** The new number of attempts allowed date. */
    private final JTextField overrideAttemptsAllowed;

    /**
     * Constructs a new {@code DlgAddPaceAppeal}.
     *
     * @param theCache            the data cache
     * @param theListener         the listener to be notified if an appeal record is added
     * @param theInitialMilestone the milestone for which an appeal is being added; {@code null} for a generic appeal
     *                            that could be for any milestone
     */
    public DlgAddPaceAppeal(final Cache theCache, final IPaceAppealsListener theListener,
                            final RawMilestone theInitialMilestone) {

        super(TITLE);
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.listener = theListener;
        this.initialMilestone = theInitialMilestone;

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

        this.statusLabel = new JLabel("Student is not enrolled in any paced courses");
        this.statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusLabel.setFont(Skin.BODY_12_FONT);

        this.paceField = new JTextField(2);
        this.paceField.setFont(Skin.BODY_12_FONT);
        this.paceField.setEditable(true);

        this.paceTrackField = new JTextField(2);
        this.paceTrackField.setFont(Skin.BODY_12_FONT);

        this.courseField = new JTextField(2);
        this.courseField.setFont(Skin.BODY_12_FONT);
        this.courseField.setEditable(true);

        this.unitField = new JTextField(2);
        this.unitField.setFont(Skin.BODY_12_FONT);
        this.unitField.setEditable(true);

        this.milestoneTypeDropdown = new JComboBox<>(MS_TYPES);
        this.milestoneTypeDropdown.setFont(Skin.BODY_12_FONT);
        this.milestoneTypeDropdown.setActionCommand(VALIDATE_CMD);

        this.reliefGiven = new JCheckBox("Relief Given");
        this.reliefGiven.setFont(Skin.BODY_12_FONT);
        this.reliefGiven.setActionCommand(VALIDATE_CMD);

        this.origDatePicker = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.origDatePicker.setFont(Skin.BODY_12_FONT);
        this.origDatePicker.setActionCommand(VALIDATE_CMD);

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

        paceAppeal.add(this.statusLabel, StackedBorderLayout.NORTH);

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
        flow8.add(this.origDatePicker);
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

        // Right side is "student milestone" or "student standard milestone" record, if applicable

        final JPanel milestoneUpdate = AdmPanelBase.makeOffWhitePanel(new StackedBorderLayout());

        final Border leftLine = BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY);
        final Border padLeftBottom = BorderFactory.createEmptyBorder(0, 10, 10, 0);
        final Border border1 = BorderFactory.createCompoundBorder(leftLine, padLeftBottom);
        milestoneUpdate.setBorder(border1);
        content.add(milestoneUpdate, StackedBorderLayout.WEST);

        final JLabel[] rightLabels = new JLabel[5];

        rightLabels[0] = new JLabel("Milestone: ");
        rightLabels[1] = new JLabel("Current Deadline: ");
        rightLabels[2] = new JLabel("Deadline Source: ");
        rightLabels[3] = new JLabel("New Deadline: ");
        rightLabels[4] = new JLabel("Attempts: ");
        for (final JLabel lbl : rightLabels) {
            lbl.setFont(Skin.BODY_12_FONT);
            lbl.setForeground(Skin.LABEL_COLOR);
        }
        UIUtilities.makeLabelsSameSizeRightAligned(rightLabels);

        this.overrideDescription = new JTextField(20);
        this.overrideDescription.setEditable(false);
        this.overrideOriginalDate = new JTextField(20);
        this.overrideOriginalDate.setEditable(false);
        this.overrideOriginalSource = new JTextField(20);
        this.overrideOriginalSource.setEditable(false);
        this.overrideNewDate = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.overrideNewDate.setFont(Skin.BODY_12_FONT);
        this.overrideAttemptsAllowed = new JTextField(2);

        final JPanel flow21 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        final JLabel topLbl = new JLabel("Student Milestone Update:");
        topLbl.setFont(Skin.MEDIUM_15_FONT);
        flow21.add(topLbl);
        milestoneUpdate.add(flow21, StackedBorderLayout.NORTH);

        final JPanel flow22 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow22.add(rightLabels[0]);
        flow22.add(this.overrideDescription);
        milestoneUpdate.add(flow22, StackedBorderLayout.NORTH);

        final JPanel flow23 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow23.add(rightLabels[1]);
        flow23.add(this.overrideOriginalDate);
        milestoneUpdate.add(flow23, StackedBorderLayout.NORTH);

        final JPanel flow24 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow24.add(rightLabels[2]);
        flow24.add(this.overrideOriginalSource);
        milestoneUpdate.add(flow24, StackedBorderLayout.NORTH);

        final JPanel flow25 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow25.add(rightLabels[3]);
        flow25.add(this.overrideNewDate);
        milestoneUpdate.add(flow25, StackedBorderLayout.NORTH);

        final JPanel flow26 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow26.add(rightLabels[4]);
        flow26.add(this.overrideAttemptsAllowed);
        milestoneUpdate.add(flow26, StackedBorderLayout.NORTH);

        // Buttons bar at the bottom

        final JPanel flow11 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.CENTER, 5, 3));
        final Border lineAbove = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY);
        final Border padAbove = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        final Border buttonBarBorder = BorderFactory.createCompoundBorder(lineAbove, padAbove);
        flow11.setBorder(buttonBarBorder);
        flow11.add(this.applyButton);
        flow11.add(cancelButton);
        content.add(flow11, StackedBorderLayout.SOUTH);

        if (Objects.nonNull(theInitialMilestone)) {
            final String paceStr = theInitialMilestone.pace.toString();
            this.paceField.setText(paceStr);
            this.paceField.setEditable(false);

            this.paceTrackField.setText(theInitialMilestone.paceTrack);
            this.paceTrackField.setEditable(false);

            final int courseIndex = theInitialMilestone.getIndex();
            final String courseIndexStr = Integer.toString(courseIndex);
            this.courseField.setText(courseIndexStr);
            this.courseField.setEditable(false);

            final int unit = theInitialMilestone.getUnit();
            final String unitStr = Integer.toString(unit);
            this.unitField.setText(unitStr);
            this.unitField.setEditable(false);

            if ("RE".equals(theInitialMilestone.msType)) {
                this.milestoneTypeDropdown.setSelectedIndex(0);
            } else if ("FE".equals(theInitialMilestone.msType)) {
                this.milestoneTypeDropdown.setSelectedIndex(1);
            } else if ("F1".equals(theInitialMilestone.msType)) {
                this.milestoneTypeDropdown.setSelectedIndex(2);
            }
            this.milestoneTypeDropdown.setEnabled(false);

            this.origDatePicker.setCurrentDate(theInitialMilestone.msDate);
            this.origDatePicker.setEnabled(false);
        }

        this.appealDatePicker.addActionListener(this);
        this.paceField.getDocument().addDocumentListener(this);
        this.courseField.getDocument().addDocumentListener(this);
        this.unitField.getDocument().addDocumentListener(this);
        this.milestoneTypeDropdown.addActionListener(this);
        this.reliefGiven.addActionListener(this);
        this.origDatePicker.addActionListener(this);
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
     * @param fixed the fixed data with logged-in user information
     * @param data  the student data
     */
    public void populateDisplay(final UserData fixed, final StudentData data) {

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);

        final LocalDate today = LocalDate.now();

        this.interviewerField.setText(fixed.username);
        this.appealDatePicker.setCurrentDate(today);

        if (data.studentTerm == null) {
            this.statusLabel.setText("Student is not enrolled in any paced courses");
            this.newDatePicker.setCurrentDate(today);
        } else {
            this.statusLabel.setText(CoreConstants.SPC);
            this.newDatePicker.setCurrentDate(null);
        }

        if (this.initialMilestone == null) {
            if (data.studentTerm == null) {
                this.paceField.setText("0");
                this.paceTrackField.setText(CoreConstants.EMPTY);
                this.courseField.setText("0");
                this.unitField.setText("0");
                this.milestoneTypeDropdown.setSelectedIndex(0);
                this.origDatePicker.setCurrentDate(today);
            } else {
                this.paceField.setText(data.studentTerm.pace == null ? CoreConstants.EMPTY :
                        data.studentTerm.pace.toString());
                this.paceTrackField.setText(data.studentTerm.paceTrack);
                this.courseField.setText(CoreConstants.EMPTY);
                this.unitField.setText(CoreConstants.EMPTY);
                this.milestoneTypeDropdown.setSelectedIndex(-1);
                this.origDatePicker.setCurrentDate(null);
            }
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
                final String[] errors = doInsertAppeal();
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

        final String track = this.paceTrackField.getText();
        boolean hasTrack = track != null && track.length() == 1;

        boolean hasCourse = false;
        int courseInt = 0;
        try {
            final String courseText = this.courseField.getText();
            courseInt = Integer.parseInt(courseText);

            if (courseInt < 0 || (hasPace && courseInt > paceInt)) {
                error = "Course number must fall within pace.";
            } else {
                hasCourse = true;
            }
        } catch (final NumberFormatException ex) {
            error = "Invalid Course number.";
        }

        boolean hasUnit = false;
        int unitInt = 0;
        try {
            final String unitText = this.unitField.getText();
            unitInt = Integer.parseInt(unitText);

            if (unitInt < 0 || unitInt > 5) {
                error = "Unit number not in valid range.";
            } else {
                hasUnit = true;
            }
        } catch (final NumberFormatException ex) {
            error = "Invalid Course number.";
        }

        String msType = null;
        final int msIndex = this.milestoneTypeDropdown.getSelectedIndex();
        if (msIndex == -1) {
            error = "A milestone type must be selected.";
        } else if (msIndex == 0) {
            msType = RawMilestone.UNIT_REVIEW_EXAM;
        } else if (msIndex == 1) {
            msType = RawMilestone.FINAL_EXAM;
        } else if (msIndex == 2) {
            msType = RawMilestone.FINAL_LAST_TRY;
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

        this.overrideDescription.setText(CoreConstants.EMPTY);
        this.overrideOriginalDate.setText(CoreConstants.EMPTY);
        this.overrideOriginalSource.setText(CoreConstants.EMPTY);
        this.overrideNewDate.setCurrentDate(null);
        this.overrideAttemptsAllowed.setText(CoreConstants.EMPTY);

        if (hasPace && hasTrack && hasCourse && hasUnit && msType != null) {
            final SystemData systemData = this.cache.getSystemData();

            try {
                final List<RawMilestone> milestones = systemData.getMilestones(this.active, Integer.valueOf(paceInt),
                        track);
                final List<RawStmilestone> stmilestones = RawStmilestoneLogic.getStudentMilestones(this.cache,
                        this.active, track, this.studentIdField.getText());

                final int msNumber = paceInt * 100 + courseInt * 10 + unitInt;
                final Integer msNbr = Integer.valueOf(msNumber);

                RawMilestone ms = null;
                for (final RawMilestone test : milestones) {
                    if (test.msNbr.equals(msNbr) && test.msType.equals(msType)) {
                        ms = test;
                        break;
                    }
                }

                RawStmilestone stms = null;
                for (final RawStmilestone test : stmilestones) {
                    if (test.msNbr.equals(msNbr) && test.msType.equals(msType)) {
                        stms = test;
                        break;
                    }
                }

                final boolean enableMilestone = this.reliefGiven.isSelected();
                final LocalDate newDate = this.newDatePicker.getCurrentDate();

                if (ms == null) {
                    Log.warning("Unable to find milestone record.");
                } else {
                    final String unitStr = Integer.toString(unitInt);
                    final String msDesc;
                    if (msIndex == 0) {
                        msDesc = SimpleBuilder.concat("Unit ", unitStr, " Review Exam");
                    } else if (msIndex == 1) {
                        msDesc = "Final Exam";
                    } else if (msIndex == 2) {
                        msDesc = "Final Exam +1 Attempt";
                    } else {
                        msDesc = CoreConstants.EMPTY;
                    }
                    if (enableMilestone) {
                        this.overrideDescription.setText(msDesc);
                    }

                    final LocalDate effectiveDate;
                    if (stms == null) {
                        effectiveDate = ms.msDate;
                        if (enableMilestone) {
                            this.overrideOriginalSource.setText("Original milestone");
                        }
                    } else {
                        effectiveDate = stms.msDate;
                        if (enableMilestone) {
                            this.overrideOriginalSource.setText("Milestone override");
                        }
                    }
                    final String dateString = TemporalUtils.FMT_MDY.format(effectiveDate);
                    if (this.origDatePicker.getCurrentDate() == null) {
                        this.origDatePicker.setCurrentDate(effectiveDate);
                    }
                    if (enableMilestone) {
                        this.overrideOriginalDate.setText(dateString);
                    }

                    if (enableMilestone) {
                        this.overrideNewDate.setCurrentDate(newDate);
                    }

                    final String attemptsStr = this.attemptsAllowedField.getText();
                    if (!(attemptsStr == null || attemptsStr.isBlank())) {
                        try {
                            Integer.parseInt(attemptsStr);
                            if (enableMilestone) {
                                this.overrideAttemptsAllowed.setText(attemptsStr);
                            }
                        } catch (final NumberFormatException ex) {
                            // No action
                        }
                    }
                }

                this.overrideDescription.setEnabled(enableMilestone);
                this.overrideOriginalDate.setEnabled(enableMilestone);
                this.overrideOriginalSource.setEnabled(enableMilestone);
                this.overrideNewDate.setEnabled(enableMilestone);
                this.overrideAttemptsAllowed.setEnabled(enableMilestone);
            } catch (final SQLException ex) {
                Log.warning("Failed to query milestones.", ex);
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
    private String[] doInsertAppeal() {

        String error[] = null;

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

            final LocalDate msDate = this.origDatePicker.getCurrentDate();
            final LocalDate newDate = this.newDatePicker.getCurrentDate();

            final String attemptsText = this.attemptsAllowedField.getText();
            final Integer numAttempts = attemptsText == null || attemptsText.isBlank() ? null
                    : Integer.valueOf(attemptsText);

            final String circumstances = this.circumstancesField.getText();
            final String comment = this.commentField.getText();
            final String interviewer = this.interviewerField.getText();

            final RawPaceAppeals newRecord = new RawPaceAppeals(this.active, stuId, appealDate, relief, paceInt,
                    paceTrack, msNbr, msType, msDate, newDate, numAttempts, circumstances, comment, interviewer);

            RawPaceAppealsLogic.INSTANCE.insert(this.cache, newRecord);

            // If relief was given, also insert or update STMILESTONE
            if (this.reliefGiven.isSelected()) {
                final List<RawStmilestone> stmilestones = RawStmilestoneLogic.getStudentMilestones(this.cache,
                        this.active, paceTrack, stuId);

                RawStmilestone stms = null;
                for (final RawStmilestone test : stmilestones) {
                    if (test.msNbr.equals(msNbr) && test.msType.equals(msType)) {
                        stms = test;
                        break;
                    }
                }

                if (stms == null) {
                    final RawStmilestone newRow = new RawStmilestone(this.active, stuId, paceTrack,
                            msNbr, msType, newDate, numAttempts);
                    RawStmilestoneLogic.INSTANCE.insert(this.cache, newRow);
                } else if (!(stms.msDate.equals(newDate) && stms.nbrAtmptsAllow.equals(numAttempts))) {
                    stms.msDate = newDate;
                    stms.nbrAtmptsAllow = numAttempts;
                    RawStmilestoneLogic.update(this.cache, stms);
                }
            }

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

