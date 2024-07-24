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
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

/**
 * A dialog to edit an existing row to "PACE_APPEALS" and (if relief is given) the related "STMILESTONE".
 */
final class DlgEditPaceAppeal extends JFrame implements ActionListener, ItemListener {

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The options with which to populate the milestone type dropdown. */
    private static final String[] MS_TYPES = {"Review Exam", "Final Exam", "Final +1"};

    /** The data cache. */
    private final Cache cache;

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
     * @param theCache the data cache
     * @param theOwner the owning panel to be refreshed if an appeal record is added
     */
    DlgEditPaceAppeal(final Cache theCache, final StuAppealsPanel theOwner) {

        super("Edit Pace Appeal");
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.owner = theOwner;

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
        this.appealDatePicker = new JDateChooser(today, holidays);
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

        this.origDatePicker = new JDateChooser(today, holidays);
        this.origDatePicker.setFont(Skin.MEDIUM_13_FONT);

        this.newDatePicker = new JDateChooser(today, holidays);
        this.newDatePicker.setFont(Skin.MEDIUM_13_FONT);

        this.attemptsAllowedField = new JTextField(2);
        this.attemptsAllowedField.setFont(Skin.MEDIUM_13_FONT);

        this.circumstancesField = new JTextArea(3, 30);
        this.circumstancesField.setFont(Skin.MEDIUM_13_FONT);
        this.circumstancesField.setBorder(this.attemptsAllowedField.getBorder());

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
     * @param fixed  the fixed data with logged-in user information
     * @param data   the student data
     * @param record the record to edit
     */
    void populateDisplay(final FixedData fixed, final StudentData data, final RawPaceAppeals record) {

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);

        this.interviewerField.setText(record.interviewer);
        this.appealDatePicker.setDate(record.appealDt);
        this.paceField.setText(record.pace == null ? CoreConstants.EMPTY : record.pace.toString());
        this.paceTrackField.setText(record.paceTrack);

        if (record.msNbr == null) {
            this.courseField.setText(CoreConstants.EMPTY);
            this.unitField.setText(CoreConstants.EMPTY);
        } else {
            final int nbr = record.msNbr.intValue();
            final int course = (nbr / 10) % 10;
            final int unit = nbr % 10;
            this.courseField.setText(Integer.toString(course));
            this.unitField.setText(Integer.toString(unit));
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

        this.origDatePicker.setDate(record.msDate);
        this.newDatePicker.setDate(record.newDeadlineDt);

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

            // TODO: validation

            // TODO: update

        } else if (CANCEL_CMD.equals(cmd)) {
            setVisible(false);
        }
    }

    /**
     * Called when the selected item changes in any dropdown.
     *
     * @param e the event to be processed
     */
    @Override
    public void itemStateChanged(final ItemEvent e) {

        // TODO:
    }
}

