package dev.mathops.app.adm.office.student;

import dev.mathops.app.ui.JDateTimeChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.app.adm.UserData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.schema.legacy.impl.RawMilestoneAppealLogic;
import dev.mathops.db.schema.legacy.rec.RawCampusCalendar;
import dev.mathops.db.schema.legacy.rec.RawMilestoneAppeal;
import dev.mathops.db.schema.main.rec.TermRec;
import dev.mathops.db.field.TermKey;

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

/**
 * A dialog to add a row to "MILESTONE_APPEAL" that simply documents an accommodation or situation, but does not adjust
 * a student deadline.
 */
public final class DlgAddGeneralAppeal extends JFrame implements ActionListener, DocumentListener {

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** An action command. */
    private static final String VALIDATE_CMD = "VALIDATE_CMD";

    /** The options with which to populate the milestone type dropdown. */
    private static final String[] APPEAL_TYPES = {"Accommodation", "University-Excused Absence", "Medical",
            "Family Emergency", "Other"};

    /** The dialog title. */
    private static final String TITLE = "Add General Appeal";

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey active;

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

    /** The circumstances. */
    private final JTextArea circumstancesField;

    /** The comment. */
    private final JTextArea commentField;

    /** The "Apply" button". */
    private final JButton applyButton;

    /**
     * Constructs a new {@code DlgAddGeneralAppeal}.
     *
     * @param theCache    the data cache
     * @param theListener the listener to be notified if an appeal record is added
     */
    DlgAddGeneralAppeal(final Cache theCache, final IAppealsListener theListener) {

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
            Log.warning("Failed to query active term", ex);
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

        final JLabel[] leftLabels = new JLabel[5];

        leftLabels[0] = new JLabel("Student ID: ");
        leftLabels[1] = new JLabel("Student Name: ");
        leftLabels[2] = new JLabel("Interviewer: ");
        leftLabels[3] = new JLabel("Appeal Type: ");
        leftLabels[4] = new JLabel("Appeal Date/Time: ");
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

        final Border border = this.interviewerField.getBorder();

        this.circumstancesField = new JTextArea(2, 30);
        this.circumstancesField.setFont(Skin.BODY_12_FONT);
        this.circumstancesField.setBorder(border);
        this.circumstancesField.setEditable(true);

        this.circumstancesField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    if (e.getModifiersEx() > 0) {
                        DlgAddGeneralAppeal.this.circumstancesField.transferFocusBackward();
                    } else {
                        DlgAddGeneralAppeal.this.circumstancesField.transferFocus();
                    }
                    e.consume();
                }
            }
        });

        this.commentField = new JTextArea(2, 30);
        this.commentField.setFont(Skin.BODY_12_FONT);
        this.commentField.setBorder(border);

        this.commentField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    if (e.getModifiersEx() > 0) {
                        DlgAddGeneralAppeal.this.commentField.transferFocusBackward();
                    } else {
                        DlgAddGeneralAppeal.this.commentField.transferFocus();
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
        final JPanel flow4a = AdmPanelBase.makeOffWhitePanel(new BorderLayout());
        flow4a.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        flow4a.add(leftLabels[4], BorderLayout.PAGE_START);
        flow5.add(flow4a, BorderLayout.LINE_START);
        flow5.add(this.appealDateTimePicker, BorderLayout.CENTER);
        paceAppeal.add(flow5, StackedBorderLayout.NORTH);

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

        this.appealTypeDropdown.addActionListener(this);
        this.appealDateTimePicker.addActionListener(this);
        this.circumstancesField.getDocument().addDocumentListener(this);
        this.applyButton.addActionListener(this);
        cancelButton.addActionListener(this);

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
     * @param userData the user data
     * @param data     the student data
     */
    public void populateDisplay(final UserData userData, final StudentData data) {

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);
        this.interviewerField.setText(userData.username);

        final LocalDateTime now = LocalDateTime.now();

        this.appealDateTimePicker.setCurrentDateTime(now);
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
            error = "Appeal date/time must be set.";
        }

        if (this.interviewerField.getText() == null || this.interviewerField.getText().isBlank()) {
            error = "Interviewer may not be blank.";
        } else if (this.circumstancesField.getText() == null || this.circumstancesField.getText().isBlank()) {
            error = "Circumstances field may not be blank.";
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

            final int typeIndex = this.appealTypeDropdown.getSelectedIndex();
            final String appealType;
            if (typeIndex == 0) {
                appealType = RawMilestoneAppeal.APPEAL_TYPE_ACC;
            } else if (typeIndex == 1) {
                appealType = RawMilestoneAppeal.APPEAL_TYPE_EXC;
            } else if (typeIndex == 2) {
                appealType = RawMilestoneAppeal.APPEAL_TYPE_MED;
            } else if (typeIndex == 3) {
                appealType = RawMilestoneAppeal.APPEAL_TYPE_FAM;
            } else {
                appealType = RawMilestoneAppeal.APPEAL_TYPE_OTH;
            }

            final LocalDateTime appealDateTime = this.appealDateTimePicker.getCurrentDateTime();

            final String circumstances = this.circumstancesField.getText();
            final String comment = this.commentField.getText();
            final String interviewer = this.interviewerField.getText();

            // TODO: Make sure the { stu_id, appeal_date_time } is UNIQUE - this is the primary key for records.

            final RawMilestoneAppeal newRecord = new RawMilestoneAppeal(this.active, stuId, appealDateTime, appealType,
                    null, null, null, null, null, null, null, circumstances, comment, interviewer);
            RawMilestoneAppealLogic.insert(this.cache, newRecord);

            if (this.listener != null) {
                this.listener.updateAppeals();
            }
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

