package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serial;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A card panel that allows an administrator to cancel an exam that's in the "LOGIN" phase (often due to the wrong exam
 * being issued).
 */
class TestingCancelCard extends AdminPanelBase implements ActionListener, FocusListener {

    /** An action command. */
    private static final String STU = "STU";

    /** An action command. */
    private static final String CANCEL = "CANCEL";

    /** An action command. */
    private static final String RESET = "RESET";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6444481681579133884L;

    /** The data cache. */
    private final Cache cache;

    /** The student ID field. */
    private final JTextField studentIdField;

    /** The student name display field. */
    private final JLabel studentNameDisplay;

    /** The student status display field. */
    private final JLabel studentStatusDisplay;

    /** The "cancel" button. */
    private final JButton cancelButton;

    /** The client PC record being canceled. */
    private RawClientPc found;

    /**
     * Constructs a new {@code TestingCancelCard}.
     *
     * @param theCache         the data cache
     */
    TestingCancelCard(final Cache theCache) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_MAGENTA);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_CYAN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;

        panel.add(makeHeader("Cancel Exam", false), BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_MAGENTA);
        panel.add(center, BorderLayout.CENTER);

        // Top row: Student ID entry
        final JPanel north = new JPanel(new BorderLayout(0, 0));
        north.setBackground(Skin.OFF_WHITE_MAGENTA);

        // Bottom: Checkbox to allow exams to be issued for which student is not eligible
        final JPanel south = new JPanel(new BorderLayout(10, 10));
        south.setBackground(Skin.OFF_WHITE_MAGENTA);

        center.add(north, BorderLayout.NORTH);
        center.add(south, BorderLayout.CENTER);

        //

        final JLabel studentIdLabel = new JLabel("Student ID:");
        studentIdLabel.setFont(Skin.MEDIUM_HEADER_15_FONT);
        studentIdLabel.setForeground(Skin.LABEL_COLOR);
        studentIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        this.studentIdField = new JTextField(12);
        this.studentIdField.setBackground(Skin.FIELD_BG);
        this.studentIdField.setActionCommand(STU);
        this.studentIdField.addActionListener(this);
        this.studentIdField.addFocusListener(this);

        this.studentNameDisplay = new JLabel(CoreConstants.SPC);
        this.studentNameDisplay.setFont(Skin.MEDIUM_HEADER_15_FONT);
        this.studentNameDisplay.setForeground(Skin.ERROR_COLOR);
        final Dimension pref = this.studentIdField.getPreferredSize();
        this.studentNameDisplay.setPreferredSize(new Dimension(pref.width * 3, pref.height));

        final JPanel studentFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        studentFlow.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        studentFlow.setBackground(Skin.OFF_WHITE_MAGENTA);
        studentFlow.add(studentIdLabel);
        studentFlow.add(this.studentIdField);
        studentFlow.add(new JLabel("     "));
        studentFlow.add(this.studentNameDisplay);

        north.add(studentFlow, BorderLayout.NORTH);

        this.studentStatusDisplay = new JLabel(CoreConstants.SPC);
        this.studentStatusDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        this.studentStatusDisplay.setFont(Skin.MEDIUM_15_FONT);
        north.add(this.studentStatusDisplay, BorderLayout.SOUTH);

        final JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPane.setBackground(Skin.OFF_WHITE_MAGENTA);
        this.cancelButton = new JButton("Cancel Exam");
        this.cancelButton.setFont(Skin.BIG_BUTTON_16_FONT);
        this.cancelButton.setActionCommand(CANCEL);
        this.cancelButton.addActionListener(this);
        this.cancelButton.setEnabled(false);
        buttonsPane.add(this.cancelButton);

        final JButton resetButton = new JButton("Start Over");
        resetButton.setFont(Skin.BIG_BUTTON_16_FONT);
        resetButton.setActionCommand(RESET);
        resetButton.addActionListener(this);
        buttonsPane.add(resetButton);

        south.add(buttonsPane, BorderLayout.NORTH);
    }

    /**
     * Called when the "Loan" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        Log.info("Action, command is ", cmd);

        if (STU.equals(cmd)) {
            processStudentId();
        } else if (CANCEL.equals(cmd)) {
            doCancelExam();
        } else if (RESET.equals(cmd)) {
            reset();
        }
    }

    /**
     * Called when a text field gains focus.
     *
     * @param e the focus event
     */
    @Override
    public void focusGained(final FocusEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when a text field loses focus.
     *
     * @param e the focus event
     */
    @Override
    public void focusLost(final FocusEvent e) {

        if (e.getComponent() == this.studentIdField) {
            if (this.studentIdField.getText().isEmpty()) {
                this.studentIdField.requestFocus();
            } else {
                processStudentId();
            }
        }
    }

    /**
     * Sets focus.
     */
    public void focus() {

        this.studentIdField.requestFocus();
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    public void reset() {

        this.studentIdField.setText(CoreConstants.EMPTY);
        this.studentIdField.setBackground(Skin.FIELD_BG);

        this.studentNameDisplay.setText(CoreConstants.SPC);
        this.studentStatusDisplay.setText(CoreConstants.SPC);
        this.studentIdField.requestFocus();

        this.cancelButton.setEnabled(false);
    }

    /**
     * Called when a student ID is entered and "Return" is pressed in that field (typically, by the bar code scanner
     * reading a student ID card).
     */
    private void processStudentId() {

        this.studentNameDisplay.setText(CoreConstants.SPC);

        final String stuId = this.studentIdField.getText();

        final String cleanStu = stuId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                .replace(CoreConstants.DASH, CoreConstants.EMPTY);

        final String foundFirstName;
        final String foundLastName;
        final String foundPrefName;
        final String sql1 = "SELECT first_name, last_name, pref_name "
                + "FROM student WHERE stu_id=?";

        this.studentStatusDisplay.setText(CoreConstants.SPC);

        try (final PreparedStatement ps = this.cache.conn.prepareStatement(sql1)) {
            ps.setString(1, cleanStu);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    foundFirstName = rs.getString(1);
                    foundLastName = rs.getString(2);
                    foundPrefName = rs.getString(3);
                    this.studentIdField.setBackground(Skin.FIELD_BG);

                    final String first =
                            foundFirstName == null ? CoreConstants.EMPTY : foundFirstName.trim();
                    final String last =
                            foundLastName == null ? CoreConstants.EMPTY : foundLastName.trim();
                    final String pref =
                            foundPrefName == null ? CoreConstants.EMPTY : foundPrefName.trim();

                    if (pref.isEmpty() || pref.equals(first)) {
                        this.studentNameDisplay.setText(first + CoreConstants.SPC + last);
                    } else {
                        this.studentNameDisplay.setText(first + CoreConstants.SPC + last //
                                + " (" + pref + ")");
                    }

                    checkForUnstartedExams(cleanStu);
                } else {
                    this.studentStatusDisplay.setText("Student not found.");
                    this.studentIdField.setBackground(Skin.FIELD_ERROR_BG);
                }
            }
        } catch (final SQLException ex) {
            if (ex.getMessage() == null) {
                this.studentStatusDisplay
                        .setText("Error querying student table: " + ex.getClass().getSimpleName());
            } else {
                this.studentStatusDisplay
                        .setText("Error querying student table: " + ex.getMessage());
            }
        }
    }

    /**
     * Checks for client PC records in the "student Login" state for the selected student.
     *
     * @param stuId the student ID
     */
    private void checkForUnstartedExams(final String stuId) {

        this.cancelButton.setEnabled(false);
        this.found = null;

        try {
            final List<RawClientPc> records = RawClientPcLogic.INSTANCE.queryAll(this.cache);

            for (final RawClientPc record : records) {
                if (stuId.equals(record.currentStuId)
                        && (RawClientPc.STATUS_AWAIT_STUDENT.equals(record.currentStatus))) {
                    this.found = record;
                    break;
                }
            }

            if (this.found == null) {
                this.studentStatusDisplay
                        .setText("No testing stations found awaiting student login.");
            } else {
                this.studentStatusDisplay
                        .setText("FOUND: Testing station " + this.found.stationNbr + ", "
                                + this.found.currentCourse + ", unit " + this.found.currentUnit + " exam.");
                this.cancelButton.setEnabled(true);
                this.cancelButton.requestFocus();
            }
        } catch (final SQLException ex) {
            this.studentStatusDisplay.setText("Error querying client PC table: " + ex.getMessage());
        }
    }

    /**
     * Cancels the exam.
     */
    private void doCancelExam() {

        if (this.found == null) {
            this.studentStatusDisplay.setText("No exam to cancel!");
        } else {
            try {
                final RawClientPc record =
                        RawClientPcLogic.query(this.cache, this.found.computerId);

                if (record != null
                        && (RawClientPc.STATUS_AWAIT_STUDENT.equals(record.currentStatus))) {

                    if (RawClientPcLogic.updateAllCurrent(this.cache, this.found.computerId,
                            RawClientPc.STATUS_LOCKED, null, null, null, null)) {
                        this.studentStatusDisplay.setText("Exam Canceled.");
                    } else {
                        this.studentStatusDisplay.setText("Error while canceling exam!");
                    }
                } else {
                    this.studentStatusDisplay
                            .setText("Testing station is no longer in the login state.");
                }
            } catch (final SQLException ex) {
                this.studentStatusDisplay
                        .setText("Error checking client PC table: " + ex.getMessage());
            }

            this.found = null;
        }

        this.cancelButton.setEnabled(false);
    }
}
