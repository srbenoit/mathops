package dev.mathops.app.adm.office.placement;

import dev.mathops.app.ui.JDateChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawFfrTrns;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
 * A dialog to add a row to "FFR_TRNS".
 */
final class DlgAddTransfer extends JFrame implements ActionListener, ItemListener {

    /** The course IDs with which to pre-populate the course ID dropdown. */
    private static final String[] COURSE_IDS = {"MATH 1++1B", "MATH 2++1B", "M 002", "M 055", "M 099", "M 100C",
            "M 101", "M 105", "M 117", "M 118", "M 124", "M 125", "M 126", "M 120", "M 127", "M 141", "M 155", "M 156",
            "M 160", "M 161", "M 229", "M 255", "M 261", "M 340", "M 369", "STAT 100", "STAT 201", "STAT 204"};

    /** The options with which to populate the type dropdown. */
    private static final String[] TRANSFER_OR_CHALLENGE = {"Transfer Credit", "Credit By Exam"};

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The data cache. */
    private final Cache cache;

    /** The owning student courses panel to be refreshed if a transfer record is added. */
    private final PlacementTransferPanel owner;

    /** The field for the student ID. */
    private final JTextField studentIdField;

    /** The field for the student Name. */
    private final JTextField studentNameField;

    /** The dropdown for the course ID (the user can enter a course ID as well). */
    private final JComboBox<String> courseIdDropdown;

    /** The exam date field. */
    private final JDateChooser examDate;

    /** The dropdown for the credit type (transfer or challenge) */
    private final JComboBox<String> typeDropdown;

    /** The field for the grade. */
    private final JTextField gradeField;

    /** The "Apply" button". */
    private final JButton applyButton;

    /**
     * Constructs a new {@code DlgAddTransfer}.
     *
     * @param theCache the data cache
     * @param theOwner the owning student courses panel to be refreshed if a transfer record is added
     */
    DlgAddTransfer(final Cache theCache, final PlacementTransferPanel theOwner) {

        super("Add Transfer Credit");
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.owner = theOwner;

        final JPanel content = AdmPanelBase.makeOffWhitePanel(new StackedBorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        content.setBorder(padding);
        setContentPane(content);

        final JLabel[] labels = new JLabel[6];

        labels[0] = new JLabel("Student ID: ");
        labels[1] = new JLabel("Student Name: ");
        labels[2] = new JLabel("Course ID: ");
        labels[3] = new JLabel("Date: ");
        labels[4] = new JLabel("Type: ");
        labels[5] = new JLabel("Grade: ");
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

        this.courseIdDropdown = new JComboBox<>(COURSE_IDS);
        this.courseIdDropdown.setFont(Skin.MEDIUM_13_FONT);
        this.courseIdDropdown.setEditable(true);
        this.courseIdDropdown.setSelectedIndex(-1);
        this.courseIdDropdown.addItemListener(this);

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
        this.examDate = new JDateChooser(today, holidays, Skin.BODY_12_FONT);
        this.examDate.setFont(Skin.MEDIUM_13_FONT);

        this.typeDropdown = new JComboBox<>(TRANSFER_OR_CHALLENGE);
        this.typeDropdown.setFont(Skin.MEDIUM_13_FONT);
        this.typeDropdown.setEditable(true);
        this.typeDropdown.addItemListener(this);

        this.gradeField = new JTextField(3);
        this.gradeField.setFont(Skin.MEDIUM_13_FONT);

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
        flow3.add(this.courseIdDropdown);
        content.add(flow3, StackedBorderLayout.NORTH);

        final JPanel flow4 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow4.add(labels[3]);
        flow4.add(this.examDate);
        content.add(flow4, StackedBorderLayout.NORTH);

        final JPanel flow5 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow5.add(labels[4]);
        flow5.add(this.typeDropdown);
        content.add(flow5, StackedBorderLayout.NORTH);

        final JPanel flow6 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow6.add(labels[5]);
        flow6.add(this.gradeField);
        content.add(flow6, StackedBorderLayout.NORTH);

        this.applyButton = new JButton("Apply");
        this.applyButton.setFont(Skin.BUTTON_13_FONT);
        this.applyButton.setActionCommand(APPLY_CMD);
        this.applyButton.addActionListener(this);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(Skin.BUTTON_13_FONT);
        cancelButton.setActionCommand(CANCEL_CMD);
        cancelButton.addActionListener(this);

        final JPanel flow7 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        flow7.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        flow7.add(this.applyButton);
        flow7.add(cancelButton);
        content.add(flow7, StackedBorderLayout.NORTH);

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
     * @param data the student data
     */
    void populateDisplay(final StudentData data) {

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);

        this.courseIdDropdown.setSelectedIndex(-1);
        this.examDate.setCurrentDate(LocalDate.now());
        this.typeDropdown.setSelectedIndex(0);
        this.gradeField.setText("");

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

        if (APPLY_CMD.equals(cmd)) {

            final String stuId = this.studentIdField.getText();
            final Object course = this.courseIdDropdown.getSelectedItem();
            final LocalDate date = this.examDate.getCurrentDate();
            final Object type = this.typeDropdown.getSelectedItem();
            final String gradeStr = this.gradeField.getText();
            final String grade = gradeStr == null || gradeStr.isBlank() ? null :
                    (gradeStr.length() > 2 ? gradeStr.substring(0, 2) : gradeStr);

            if (stuId == null || course == null || date == null || type == null) {
                Log.warning("Apply button was enabled when it should not have been");
                this.applyButton.setEnabled(false);
            } else if (course instanceof final String courseStr) {
                if (type instanceof final String typeStr) {
                    final String examPlaced = typeStr.startsWith("Transfer") ? "T" : "C";
                    final RawFfrTrns newRecord = new RawFfrTrns(stuId, courseStr, examPlaced, date, null, grade);

                    try {
                        RawFfrTrnsLogic.insert(this.cache, newRecord);
                        setVisible(false);
                        this.owner.updateTransferCreditList();
                    } catch (final SQLException ex) {
                        final String[] msg = {"Failed to insert FFR_TRNS record", ex.getMessage()};
                        Log.warning(ex);
                        JOptionPane.showMessageDialog(this, msg, "Add Transfer Credit", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid type", "Add Transfer Credit",
                            JOptionPane.ERROR_MESSAGE);
                    this.applyButton.setEnabled(false);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid course ID", "Add Transfer Credit",
                        JOptionPane.ERROR_MESSAGE);
                this.applyButton.setEnabled(false);
            }
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

        final Object selectedCourse = this.courseIdDropdown.getSelectedItem();
        final Object selectedType = this.typeDropdown.getSelectedItem();
        final Object date = this.examDate.getCurrentDate();

        this.applyButton.setEnabled(selectedCourse != null && selectedType != null && date != null);
    }
}

