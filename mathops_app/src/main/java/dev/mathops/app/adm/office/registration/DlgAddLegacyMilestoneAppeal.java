package dev.mathops.app.adm.office.registration;

import dev.mathops.app.ui.JDateChooser;
import dev.mathops.app.ui.JDateTimeChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.office.student.IAppealsListener;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawMilestoneAppealLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawMilestoneAppeal;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.SimpleBuilder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A dialog to add a row to "MILESTONE_APPEAL" that adjusts a student deadline in a legacy course, and the corresponding
 * "STMILESTONE" record.
 */
public final class DlgAddLegacyMilestoneAppeal extends JFrame implements ActionListener, DocumentListener {

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** An action command. */
    private static final String VALIDATE_CMD = "VALIDATE_CMD";

    /** The dialog title. */
    private static final String TITLE = "Add Milestone Appeal (Legacy Course)";

    /** The options with which to populate the milestone type dropdown. */
    private static final String[] MS_TYPES = {"Review Exam", "Final Exam", "Final +1"};

    /** The options with which to populate the milestone type dropdown. */
    private static final String[] APPEAL_TYPES = {"Accommodation", "University-Excused Absence", "Medical",
            "Family Emergency", "Very Close to Finishing", "Free Extension", "Automatically-Applied Extension",
            "Other"};

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey active;

    /** The initial milestone this dialog was created to edit. */
    private RawMilestone milestone;

    /** The list of all milestones. */
    private final List<RawMilestone> allMilestones;

    /** The owning panel to be refreshed if an appeal record is added. */
    private final IAppealsListener listener;

    /** The field for the student ID. */
    private final JTextField studentIdField;

    /** The field for the student Name. */
    private final JTextField studentNameField;

    /** The interviewer login name. */
    private final JTextField interviewerField;

    /** The appeal type chooser. */
    private final JComboBox<String> appealTypeDropdown;

    /** The appeal date/time. */
    private final JDateTimeChooser appealDateTimePicker;

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

    /** The prior milestone date. */
    private final JDateChooser priorDatePicker;

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
     * Constructs a new {@code DlgAddLegacyMilestoneAppeal}.
     *
     * @param theCache    the data cache
     * @param theListener the listener to be notified if an appeal record is added
     */
    public DlgAddLegacyMilestoneAppeal(final Cache theCache, final IAppealsListener theListener) {

        super(TITLE);
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.listener = theListener;

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

        final JLabel[] leftLabels = new JLabel[13];

        leftLabels[0] = new JLabel("Student ID: ");
        leftLabels[1] = new JLabel("Student Name: ");
        leftLabels[2] = new JLabel("Interviewer: ");
        leftLabels[3] = new JLabel("Appeal Type: ");
        leftLabels[4] = new JLabel("Appeal Date/Time: ");
        leftLabels[5] = new JLabel("Pace: ");
        leftLabels[6] = new JLabel("Pace Track: ");
        leftLabels[7] = new JLabel("Course: ");
        leftLabels[8] = new JLabel("Unit: ");
        leftLabels[9] = new JLabel("Milestone: ");
        leftLabels[10] = new JLabel("Prior Deadline: ");
        leftLabels[11] = new JLabel("New Deadline: ");
        leftLabels[12] = new JLabel("Attempts: ");
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

        this.appealTypeDropdown = new JComboBox<>(APPEAL_TYPES);
        this.appealTypeDropdown.setFont(Skin.BODY_12_FONT);
        this.appealTypeDropdown.setActionCommand(VALIDATE_CMD);

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

        final LocalDateTime now = LocalDateTime.now();
        this.appealDateTimePicker = new JDateTimeChooser(now, holidays, Skin.BODY_12_FONT, SwingConstants.VERTICAL);
        this.appealDateTimePicker.setFont(Skin.BODY_12_FONT);
        this.appealDateTimePicker.setActionCommand(VALIDATE_CMD);
        final Color bg = content.getBackground();
        this.appealDateTimePicker.setBackground(bg);

        this.statusLabel = new JLabel("Student is not enrolled in any paced courses");
        this.statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusLabel.setFont(Skin.BODY_12_FONT);

        this.paceField = new JTextField(2);
        this.paceField.setFont(Skin.BODY_12_FONT);
        this.paceField.setEnabled(false);

        this.paceTrackField = new JTextField(2);
        this.paceTrackField.setFont(Skin.BODY_12_FONT);
        this.paceTrackField.setEnabled(false);

        this.courseField = new JTextField(2);
        this.courseField.setFont(Skin.BODY_12_FONT);
        this.courseField.setEnabled(false);

        this.unitField = new JTextField(2);
        this.unitField.setFont(Skin.BODY_12_FONT);
        this.unitField.setEnabled(false);

        this.milestoneTypeDropdown = new JComboBox<>(MS_TYPES);
        this.milestoneTypeDropdown.setFont(Skin.BODY_12_FONT);
        this.milestoneTypeDropdown.setActionCommand(VALIDATE_CMD);
        this.milestoneTypeDropdown.setEnabled(false);

        final LocalDate today = now.toLocalDate();

        this.priorDatePicker = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.priorDatePicker.setFont(Skin.BODY_12_FONT);
        this.priorDatePicker.setActionCommand(VALIDATE_CMD);
        this.priorDatePicker.setEnabled(false);

        this.newDatePicker = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.newDatePicker.setFont(Skin.BODY_12_FONT);
        this.newDatePicker.setActionCommand(VALIDATE_CMD);

        this.attemptsAllowedField = new JTextField(2);
        this.attemptsAllowedField.setFont(Skin.BODY_12_FONT);

        this.circumstancesField = new JTextArea(2, 30);
        this.circumstancesField.setFont(Skin.BODY_12_FONT);
        this.circumstancesField.setBorder(this.attemptsAllowedField.getBorder());

        this.circumstancesField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    if (e.getModifiersEx() > 0) {
                        DlgAddLegacyMilestoneAppeal.this.circumstancesField.transferFocusBackward();
                    } else {
                        DlgAddLegacyMilestoneAppeal.this.circumstancesField.transferFocus();
                    }
                    e.consume();
                }
            }
        });

        this.commentField = new JTextArea(2, 30);
        this.commentField.setFont(Skin.BODY_12_FONT);
        this.commentField.setBorder(this.attemptsAllowedField.getBorder());

        this.commentField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    if (e.getModifiersEx() > 0) {
                        DlgAddLegacyMilestoneAppeal.this.commentField.transferFocusBackward();
                    } else {
                        DlgAddLegacyMilestoneAppeal.this.commentField.transferFocus();
                    }
                    e.consume();
                }
            }
        });

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
        flow4.add(this.appealTypeDropdown);
        paceAppeal.add(flow4, StackedBorderLayout.NORTH);

        final JPanel flow5 = AdmPanelBase.makeOffWhitePanel(new BorderLayout(5, 0));
        flow5.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        final JPanel flow5a = AdmPanelBase.makeOffWhitePanel(new BorderLayout());
        flow5a.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        flow5a.add(leftLabels[4], BorderLayout.PAGE_START);
        flow5.add(flow5a, BorderLayout.LINE_START);
        flow5.add(this.appealDateTimePicker, BorderLayout.CENTER);
        paceAppeal.add(flow5, StackedBorderLayout.NORTH);

        paceAppeal.add(this.statusLabel, StackedBorderLayout.NORTH);

        final JPanel flow6 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow6.add(leftLabels[5]);
        flow6.add(this.paceField);
        flow6.add(leftLabels[6]);
        flow6.add(this.paceTrackField);
        flow6.add(leftLabels[7]);
        flow6.add(this.courseField);
        paceAppeal.add(flow6, StackedBorderLayout.NORTH);

        final JPanel flow7 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow7.add(leftLabels[8]);
        flow7.add(this.unitField);
        paceAppeal.add(flow7, StackedBorderLayout.NORTH);

        final JPanel flow8 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow8.add(leftLabels[9]);
        flow8.add(this.milestoneTypeDropdown);
        paceAppeal.add(flow8, StackedBorderLayout.NORTH);

        final JPanel flow9 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow9.add(leftLabels[10]);
        flow9.add(this.priorDatePicker);
        paceAppeal.add(flow9, StackedBorderLayout.NORTH);

        final JPanel flow10 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow10.add(leftLabels[11]);
        flow10.add(this.newDatePicker);
        paceAppeal.add(flow10, StackedBorderLayout.NORTH);

        final JPanel flow11 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow11.add(leftLabels[12]);
        flow11.add(this.attemptsAllowedField);
        paceAppeal.add(flow11, StackedBorderLayout.NORTH);

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

        // Right side is "student milestone" or "student standard milestone" record, as applicable

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
        this.overrideNewDate.setEnabled(false);
        this.overrideAttemptsAllowed = new JTextField(2);
        this.overrideAttemptsAllowed.setEditable(false);

        final JPanel flow31 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        final JLabel topLbl = new JLabel("Student Milestone Update:");
        topLbl.setFont(Skin.MEDIUM_15_FONT);
        flow31.add(topLbl);
        milestoneUpdate.add(flow31, StackedBorderLayout.NORTH);

        final JPanel flow32 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow32.add(rightLabels[0]);
        flow32.add(this.overrideDescription);
        milestoneUpdate.add(flow32, StackedBorderLayout.NORTH);

        final JPanel flow33 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow33.add(rightLabels[1]);
        flow33.add(this.overrideOriginalDate);
        milestoneUpdate.add(flow33, StackedBorderLayout.NORTH);

        final JPanel flow34 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow34.add(rightLabels[2]);
        flow34.add(this.overrideOriginalSource);
        milestoneUpdate.add(flow34, StackedBorderLayout.NORTH);

        final JPanel flow35 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow35.add(rightLabels[3]);
        flow35.add(this.overrideNewDate);
        milestoneUpdate.add(flow35, StackedBorderLayout.NORTH);

        final JPanel flow36 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow36.add(rightLabels[4]);
        flow36.add(this.overrideAttemptsAllowed);
        milestoneUpdate.add(flow36, StackedBorderLayout.NORTH);

        // Buttons bar at the bottom

        final JPanel flow21 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.CENTER, 5, 3));
        final Border lineAbove = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY);
        final Border padAbove = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        final Border buttonBarBorder = BorderFactory.createCompoundBorder(lineAbove, padAbove);
        flow21.setBorder(buttonBarBorder);
        flow21.add(this.applyButton);
        flow21.add(cancelButton);
        content.add(flow21, StackedBorderLayout.SOUTH);

        if (Objects.nonNull(this.milestone)) {
            final String paceStr = this.milestone.pace.toString();
            this.paceField.setText(paceStr);
            this.paceField.setEditable(false);

            this.paceTrackField.setText(this.milestone.paceTrack);
            this.paceTrackField.setEditable(false);

            final int courseIndex = this.milestone.getIndex();
            final String courseIndexStr = Integer.toString(courseIndex);
            this.courseField.setText(courseIndexStr);
            this.courseField.setEditable(false);

            final int unit = this.milestone.getUnit();
            final String unitStr = Integer.toString(unit);
            this.unitField.setText(unitStr);
            this.unitField.setEditable(false);

            if ("RE".equals(this.milestone.msType)) {
                this.milestoneTypeDropdown.setSelectedIndex(0);
            } else if ("FE".equals(this.milestone.msType)) {
                this.milestoneTypeDropdown.setSelectedIndex(1);
            } else if ("F1".equals(this.milestone.msType)) {
                this.milestoneTypeDropdown.setSelectedIndex(2);
            }
            this.milestoneTypeDropdown.setEnabled(false);

            this.priorDatePicker.setCurrentDate(this.milestone.msDate);
            this.priorDatePicker.setEnabled(false);
        }

        this.appealDateTimePicker.addActionListener(this);
        this.paceField.getDocument().addDocumentListener(this);
        this.paceTrackField.getDocument().addDocumentListener(this);
        this.appealTypeDropdown.addActionListener(this);
        this.courseField.getDocument().addDocumentListener(this);
        this.unitField.getDocument().addDocumentListener(this);
        this.milestoneTypeDropdown.addActionListener(this);
        this.priorDatePicker.addActionListener(this);
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
     * @param theMilestone the milestone for which an appeal is being added
     */
    public void populateDisplay(final UserData userData, final StudentData data, final RawMilestone theMilestone) {

        this.milestone = theMilestone;

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);

        final LocalDateTime now = LocalDateTime.now();
        final LocalDate today = now.toLocalDate();

        this.interviewerField.setText(userData.username);
        this.appealDateTimePicker.setCurrentDateTime(now);

        if (data.studentTerm == null) {
            this.statusLabel.setText("Student is not enrolled in any paced courses");
            this.newDatePicker.setCurrentDate(today);
        } else {
            this.statusLabel.setText(CoreConstants.SPC);
            this.newDatePicker.setCurrentDate(null);
        }

        if (theMilestone == null) {
            if (data.studentTerm == null) {
                this.paceField.setText("0");
                this.paceTrackField.setText(CoreConstants.EMPTY);
                this.courseField.setText("0");
                this.unitField.setText("0");
                this.milestoneTypeDropdown.setSelectedIndex(0);
                this.priorDatePicker.setCurrentDate(today);
            } else {
                this.paceField.setText(data.studentTerm.pace == null ? CoreConstants.EMPTY :
                        data.studentTerm.pace.toString());
                this.paceTrackField.setText(data.studentTerm.paceTrack);
                this.courseField.setText(CoreConstants.EMPTY);
                this.unitField.setText(CoreConstants.EMPTY);
                this.milestoneTypeDropdown.setSelectedIndex(-1);
                this.priorDatePicker.setCurrentDate(null);
            }
        } else {
            final String paceStr = theMilestone.pace.toString();
            this.paceField.setText(paceStr);
            this.paceTrackField.setText(theMilestone.paceTrack);
            final String courseStr = Integer.toString(theMilestone.getIndex());
            this.courseField.setText(courseStr);
            final int unit = theMilestone.getUnit();
            final String unitStr = Integer.toString(unit);
            this.unitField.setText(unitStr);
            switch (theMilestone.msType) {
                case "RE" -> this.milestoneTypeDropdown.setSelectedIndex(0);
                case "FE" -> this.milestoneTypeDropdown.setSelectedIndex(1);
                case "F1" -> {
                    this.milestoneTypeDropdown.setSelectedIndex(2);
                    this.attemptsAllowedField.setText("1");
                }
                case null, default -> this.milestoneTypeDropdown.setSelectedIndex(-1);
            }
            this.priorDatePicker.setCurrentDate(theMilestone.msDate);
        }

        final boolean isF1 = "F1".equals(this.milestone.msType);
        this.attemptsAllowedField.setEnabled(isF1);
        this.attemptsAllowedField.setText(isF1 ? "1" : CoreConstants.EMPTY);

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

        if (this.appealDateTimePicker.getCurrentDateTime() == null) {
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
        final boolean hasTrack = track != null && track.length() == 1;

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

            try {
                final String attemptsText = this.attemptsAllowedField.getText();
                if (attemptsText == null || attemptsText.isBlank()) {
                    error = "Number of attempts is required.";
                } else if (Integer.parseInt(attemptsText) < 1) {
                    error = "Invalid number of attempts.";
                }
            } catch (final NumberFormatException ex) {
                error = "Invalid number of attempts.";
            }
        }

        if (this.priorDatePicker.getCurrentDate() == null) {
            error = "Original deadline date may not be null.";
        } else if (this.interviewerField.getText() == null || this.interviewerField.getText().isBlank()) {
            error = "Interviewer may not be blank.";
        } else if (this.circumstancesField.getText() == null || this.circumstancesField.getText().isBlank()) {
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
                    this.overrideDescription.setText(msDesc);

                    final LocalDate effectiveDate;
                    if (stms == null) {
                        effectiveDate = ms.msDate;
                        this.overrideOriginalSource.setText("Original milestone");
                    } else {
                        effectiveDate = stms.msDate;
                        this.overrideOriginalSource.setText("Milestone override");
                    }
                    final String dateString = TemporalUtils.FMT_MDY.format(effectiveDate);
                    if (this.priorDatePicker.getCurrentDate() == null) {
                        this.priorDatePicker.setCurrentDate(effectiveDate);
                    }
                    this.overrideOriginalDate.setText(dateString);
                    this.overrideNewDate.setCurrentDate(newDate);

                    final String attemptsStr = this.attemptsAllowedField.getText();
                    if (!(attemptsStr == null || attemptsStr.isBlank())) {
                        try {
                            Integer.parseInt(attemptsStr);
                            this.overrideAttemptsAllowed.setText(attemptsStr);
                        } catch (final NumberFormatException ex) {
                            // No action
                        }
                    }
                }
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

        String[] error = null;

        try {
            final String stuId = this.studentIdField.getText();

            final LocalDateTime appealDateTime = this.appealDateTimePicker.getCurrentDateTime();

            final String typeCode;
            final int appealTypeIndex = this.appealTypeDropdown.getSelectedIndex();
            if (appealTypeIndex == 0) {
                typeCode = RawMilestoneAppeal.APPEAL_TYPE_ACC;
            } else if (appealTypeIndex == 1) {
                typeCode = RawMilestoneAppeal.APPEAL_TYPE_EXC;
            } else if (appealTypeIndex == 2) {
                typeCode = RawMilestoneAppeal.APPEAL_TYPE_MED;
            } else if (appealTypeIndex == 3) {
                typeCode = RawMilestoneAppeal.APPEAL_TYPE_FAM;
            } else if (appealTypeIndex == 4) {
                typeCode = RawMilestoneAppeal.APPEAL_TYPE_FIN;
            } else if (appealTypeIndex == 5) {
                typeCode = RawMilestoneAppeal.APPEAL_TYPE_REQ;
            } else if (appealTypeIndex == 6) {
                typeCode = RawMilestoneAppeal.APPEAL_TYPE_AUT;
            } else {
                typeCode = RawMilestoneAppeal.APPEAL_TYPE_OTH;
            }

            final String paceText = this.paceField.getText();
            final Integer paceInt = Integer.valueOf(paceText);

            final String paceTrack = this.paceTrackField.getText();

            final String courseText = this.courseField.getText();
            final int courseInt = Integer.parseInt(courseText);

            final String unitText = this.unitField.getText();
            final int unitInt = Integer.parseInt(unitText);

            final int number = paceInt.intValue() * 100 + courseInt * 10 + unitInt;
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

            final LocalDate priorDate = this.priorDatePicker.getCurrentDate();
            final LocalDate newDate = this.newDatePicker.getCurrentDate();

            final String attemptsText = this.attemptsAllowedField.getText();
            final Integer numAttempts = attemptsText == null || attemptsText.isBlank() ? null
                    : Integer.valueOf(attemptsText);

            final String circumstances = this.circumstancesField.getText();
            final String comment = this.commentField.getText();
            final String interviewer = this.interviewerField.getText();

            final RawMilestoneAppeal newRecord = new RawMilestoneAppeal(this.active, stuId, appealDateTime,
                    typeCode, paceInt, paceTrack, msNbr, msType, priorDate, newDate, numAttempts, circumstances,
                    comment, interviewer);

            RawMilestoneAppealLogic.insert(this.cache, newRecord);

            // If relief was given, also insert or update STMILESTONE
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
                RawStmilestoneLogic.insert(this.cache, newRow);
            } else if (!(stms.msDate.equals(newDate) && stms.nbrAtmptsAllow.equals(numAttempts))) {
                stms.msDate = newDate;
                stms.nbrAtmptsAllow = numAttempts;
                RawStmilestoneLogic.update(this.cache, stms);
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

