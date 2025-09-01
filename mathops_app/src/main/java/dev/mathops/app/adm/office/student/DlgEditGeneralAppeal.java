package dev.mathops.app.adm.office.student;

import dev.mathops.app.ui.JDateTimeChooser;
import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawMilestoneAppealLogic;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.schema.legacy.RawCampusCalendar;
import dev.mathops.db.schema.legacy.RawMilestoneAppeal;
import dev.mathops.db.schema.legacy.RawPaceAppeals;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;

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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A dialog to edit a row in either the "PACE_APPEALS" or the "MILESTONE_APPEAL" table that simply documents an
 * accommodation or situation, but does not adjust a student deadline.  This is identical to the dialog to add an
 * appeal, except that the appeal date/time is disabled, since that is part of the primary key (with student id).
 */
public final class DlgEditGeneralAppeal extends JFrame implements ActionListener, DocumentListener {

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
    private static final String TITLE = "Edit General Appeal";

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey active;

    /** The {@code RawPaceAppeals} record being edited. */
    private RawPaceAppeals currentPaceAppeal;

    /** The {@code RawMilestoneAppeal} record being edited. */
    private RawMilestoneAppeal currentMilestoneAppeal;

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
    DlgEditGeneralAppeal(final Cache theCache, final IAppealsListener theListener) {

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
        this.appealDateTimePicker.setEnabled(false);

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
                        DlgEditGeneralAppeal.this.circumstancesField.transferFocusBackward();
                    } else {
                        DlgEditGeneralAppeal.this.circumstancesField.transferFocus();
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
                        DlgEditGeneralAppeal.this.commentField.transferFocusBackward();
                    } else {
                        DlgEditGeneralAppeal.this.commentField.transferFocus();
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
     * Populates all displayed fields for a selected student and a provided appeal.
     *
     * @param data       the student data
     * @param paceAppeal the pace appeal to edit
     */
    public void populateDisplay(final StudentData data, final RawPaceAppeals paceAppeal) {

        this.currentPaceAppeal = paceAppeal;
        this.currentMilestoneAppeal = null;

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);
        this.interviewerField.setText(paceAppeal.interviewer);

        final LocalDateTime dateTime = LocalDateTime.of(paceAppeal.appealDt, LocalTime.of(12, 0));
        this.appealDateTimePicker.setCurrentDateTime(dateTime);
        this.appealTypeDropdown.setEnabled(false);

        final String circ = paceAppeal.circumstances == null ? CoreConstants.EMPTY : paceAppeal.circumstances;
        this.circumstancesField.setText(circ);

        final String comment = paceAppeal.comment == null ? CoreConstants.EMPTY : paceAppeal.comment;
        this.commentField.setText(comment);

        this.applyButton.setEnabled(false);
    }

    /**
     * Populates all displayed fields for a selected student and a provided appeal.
     *
     * @param data            the student data
     * @param milestoneAppeal the milestone appeal to edit
     */
    public void populateDisplay(final StudentData data, final RawMilestoneAppeal milestoneAppeal) {

        this.currentPaceAppeal = null;
        this.currentMilestoneAppeal = milestoneAppeal;

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);
        this.interviewerField.setText(milestoneAppeal.interviewer);

        this.appealDateTimePicker.setCurrentDateTime(milestoneAppeal.appealDateTime);

        switch (milestoneAppeal.appealType) {
            case RawMilestoneAppeal.APPEAL_TYPE_ACC -> this.appealTypeDropdown.setSelectedIndex(0);
            case RawMilestoneAppeal.APPEAL_TYPE_EXC -> this.appealTypeDropdown.setSelectedIndex(1);
            case RawMilestoneAppeal.APPEAL_TYPE_MED -> this.appealTypeDropdown.setSelectedIndex(2);
            case RawMilestoneAppeal.APPEAL_TYPE_FAM -> this.appealTypeDropdown.setSelectedIndex(3);
            case null, default -> this.appealTypeDropdown.setSelectedIndex(4);
        }
        this.appealTypeDropdown.setEnabled(true);

        final String circ = milestoneAppeal.circumstances == null ? CoreConstants.EMPTY :
                milestoneAppeal.circumstances.trim();
        this.circumstancesField.setText(circ);

        final String comment = milestoneAppeal.comment == null ? CoreConstants.EMPTY : milestoneAppeal.comment.trim();
        this.commentField.setText(comment);

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
            processChange();
        } else if (APPLY_CMD.equals(cmd)) {
            final String error = validateFields();
            if (error == null) {
                final boolean changed = hasChanged();
                if (changed) {
                    final String[] result = doUpdateAppeal();
                    if (result == null) {
                        setVisible(false);
                    } else {
                        JOptionPane.showMessageDialog(this, result, TITLE, JOptionPane.ERROR_MESSAGE);
                    }
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

        if (this.interviewerField.getText() == null || this.interviewerField.getText().isBlank()) {
            error = "Interviewer may not be blank.";
        } else if (this.circumstancesField.getText() == null || this.circumstancesField.getText().isBlank()) {
            error = "Circumstances field may not be blank.";
        }

        return error;
    }

    /**
     * Tests whether any fields have changed from the original record being edited.
     *
     * @return true if at least one field has changed
     */
    private boolean hasChanged() {

        boolean changed = false;

        final String newCirc = this.circumstancesField.getText().trim();
        final String newComment = this.commentField.getText().trim();
        final String newInterviewer = this.interviewerField.getText().trim();

        final int index = this.appealTypeDropdown.getSelectedIndex();
        final String newAppealType;
        if (index == 0) {
            newAppealType = RawMilestoneAppeal.APPEAL_TYPE_ACC;
        } else if (index == 1) {
            newAppealType = RawMilestoneAppeal.APPEAL_TYPE_EXC;
        } else if (index == 2) {
            newAppealType = RawMilestoneAppeal.APPEAL_TYPE_MED;
        } else if (index == 3) {
            newAppealType = RawMilestoneAppeal.APPEAL_TYPE_FAM;
        } else {
            newAppealType = RawMilestoneAppeal.APPEAL_TYPE_OTH;
        }

        if (Objects.nonNull(this.currentPaceAppeal)) {
            final String oldCirc = this.currentPaceAppeal.circumstances == null ? CoreConstants.EMPTY :
                    this.currentPaceAppeal.circumstances.trim();
            if (newCirc.equals(oldCirc)) {
                final String oldComment = this.currentPaceAppeal.comment == null ? CoreConstants.EMPTY :
                        this.currentPaceAppeal.comment.trim();
                if (newComment.equals(oldComment)) {
                    final String oldInterviewer = this.currentPaceAppeal.interviewer == null ? CoreConstants.EMPTY :
                            this.currentPaceAppeal.interviewer.trim();
                    changed = !newInterviewer.equals(oldInterviewer);
                } else {
                    changed = true;
                }
            } else {
                changed = true;
            }
        } else if (Objects.nonNull(this.currentMilestoneAppeal)) {
            final String oldCirc = this.currentMilestoneAppeal.circumstances == null ? CoreConstants.EMPTY :
                    this.currentMilestoneAppeal.circumstances.trim();
            if (newCirc.equals(oldCirc)) {
                final String oldComment = this.currentMilestoneAppeal.comment == null ? CoreConstants.EMPTY :
                        this.currentMilestoneAppeal.comment.trim();
                if (newComment.equals(oldComment)) {
                    final String oldInterviewer = this.currentMilestoneAppeal.interviewer == null ?
                            CoreConstants.EMPTY :
                            this.currentMilestoneAppeal.interviewer.trim();
                    if (newInterviewer.equals(oldInterviewer)) {
                        changed = !newAppealType.equals(this.currentMilestoneAppeal.appealType);
                    } else {
                        changed = true;
                    }
                } else {
                    changed = true;
                }
            } else {
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Attempts to update the current appeal record.
     *
     * @return null if update succeeded; an error message if not
     */
    private String[] doUpdateAppeal() {

        String[] error = null;

        try {
            final String circumstances = this.circumstancesField.getText();
            final String comment = this.commentField.getText();
            final String interviewer = this.interviewerField.getText();

            if (Objects.nonNull(this.currentPaceAppeal)) {

                this.currentPaceAppeal.interviewer = interviewer;
                this.currentPaceAppeal.circumstances = circumstances;
                this.currentPaceAppeal.comment = comment;

                if (RawPaceAppealsLogic.update(this.cache, this.currentPaceAppeal)) {
                    if (this.listener != null) {
                        this.listener.updateAppeals();
                    }
                } else {
                    error = new String[]{"Unable to update pace appeal record."};
                }
            } else if (Objects.nonNull(this.currentMilestoneAppeal)) {

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

                this.currentMilestoneAppeal.interviewer = interviewer;
                this.currentMilestoneAppeal.circumstances = circumstances;
                this.currentMilestoneAppeal.comment = comment;
                this.currentMilestoneAppeal.appealType = appealType;

                if (RawMilestoneAppealLogic.update(this.cache, this.currentMilestoneAppeal)) {
                    if (this.listener != null) {
                        this.listener.updateAppeals();
                    }
                } else {
                    error = new String[]{"Unable to update milestone appeal record."};
                }
            } else {
                error = new String[]{"There is no current record being edited."};
            }
        } catch (final SQLException ex) {
            error = new String[]{"There was an error updating the record.", ex.getLocalizedMessage()};
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

        processChange();
    }

    /**
     * Called when content is removed from a text field.
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {

        processChange();
    }

    /**
     * Called when text field content is updated.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {

        processChange();
    }

    /**
     * Processes some change in input values;
     */
    private void processChange() {

        final String error = validateFields();
        final boolean changed = hasChanged();
        this.applyButton.setEnabled(error == null && changed);
    }
}

