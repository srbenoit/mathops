
package dev.mathops.app.adm.resource;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.logic.Cache;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serial;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A card panel to check a student's loan (historical and current).
 */
/* default */ class StudentLoanHistoryCard extends AdminPanelBase
        implements ActionListener, FocusListener {

    /** An action command. */
    private static final String STU = "STU";

    /** An action command. */
    private static final String DONE = "DONE";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1071515079233201482L;

    /** The data cache. */
    private final Cache cache;

    /** The student ID field. */
    private final JTextField studentIdField;

    /** The student name display field. */
    private final JLabel studentNameDisplay;

    /** The history table. */
    private final JTableLoanHistory table;

    /** The done button. */
    private final JButton doneBtn;

    /** An error message. */
    private final JLabel error1;

    /** An error message. */
    private final JLabel error2;

    /**
     * Constructs a new {@code StudentLoanCard}.
     *
     * @param theCache         the data cache
     */
    /* default */ StudentLoanHistoryCard(final Cache theCache, final Object theRenderingHint) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_CYAN);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_CYAN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;

        panel.add(makeHeader("Student Resource Loan History", false),
                BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_CYAN);
        panel.add(center, BorderLayout.CENTER);

        // "Student Loan History" header
        // Student ID entry row (label and text field)
        // Name of selected student
        // Table of loan records
        // [Done] button
        // Error messages

        // In this panel's "center" position:
        // inner1 (header in N, errors in S, inner2 in center)
        // inner2 (student entry in N, [Done] in S, inner3 in center)
        // inner3 (stu name in N, resource list in center)

        final JPanel inner1 = new JPanel(new BorderLayout(10, 10));
        inner1.setBackground(Skin.OFF_WHITE_CYAN);
        final JPanel inner2 = new JPanel(new BorderLayout(10, 10));
        inner2.setBackground(Skin.OFF_WHITE_CYAN);
        final JPanel inner3 = new JPanel(new BorderLayout(10, 10));
        inner3.setBackground(Skin.OFF_WHITE_CYAN);

        inner1.add(inner2, BorderLayout.CENTER);
        inner2.add(inner3, BorderLayout.CENTER);
        center.add(inner1, BorderLayout.CENTER);

        //

        final JLabel studentIdLabel = new JLabel("Student ID:");
        studentIdLabel.setFont(Skin.MEDIUM_HEADER_18_FONT);
        studentIdLabel.setForeground(Skin.LABEL_COLOR);
        studentIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        final JLabel spacer1 = new JLabel(CoreConstants.SPC);

        spacer1.setPreferredSize(studentIdLabel.getPreferredSize());

        this.studentIdField = new JTextField(15);
        this.studentIdField.setFont(Skin.MONO_16_FONT);
        this.studentIdField.setBackground(Skin.FIELD_BG);
        this.studentIdField.setActionCommand(STU);
        this.studentIdField.addActionListener(this);
        this.studentIdField.addFocusListener(this);
        final JPanel studentFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        studentFlow.setBackground(Skin.OFF_WHITE_CYAN);
        studentFlow.add(studentIdLabel);
        studentFlow.add(this.studentIdField);
        inner2.add(studentFlow, BorderLayout.NORTH);

        //

        this.studentNameDisplay = new JLabel(CoreConstants.SPC);
        this.studentNameDisplay.setFont(Skin.MEDIUM_HEADER_18_FONT);
        this.studentNameDisplay.setForeground(Skin.ERROR_COLOR);
        this.studentNameDisplay.setPreferredSize(this.studentIdField.getPreferredSize());
        final JPanel studentNameFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        studentNameFlow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        studentNameFlow.setBackground(Skin.OFF_WHITE_CYAN);
        studentNameFlow.add(spacer1);
        studentNameFlow.add(this.studentNameDisplay);
        inner3.add(studentNameFlow, BorderLayout.NORTH);

        //

        this.table = new JTableLoanHistory();
        final JScrollPane scroll = new JScrollPane(this.table);
        scroll.getViewport().setBackground(Skin.WHITE);
        scroll.setBackground(Skin.WHITE);
        inner3.add(scroll, BorderLayout.CENTER);

        scroll.setPreferredSize(this.table.getPreferredScrollSize(scroll, 3));

        //

        final JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPane.setBackground(Skin.OFF_WHITE_CYAN);
        this.doneBtn = new JButton("Done");
        this.doneBtn.setFont(Skin.BIG_BUTTON_16_FONT);
        this.doneBtn.setActionCommand(DONE);
        this.doneBtn.addActionListener(this);
        buttonsPane.add(this.doneBtn);
        inner2.add(buttonsPane, BorderLayout.SOUTH);

        //

        final JPanel errorPane = new JPanel(new BorderLayout());
        errorPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        errorPane.setBackground(Skin.OFF_WHITE_CYAN);
        inner1.add(errorPane, BorderLayout.SOUTH);

        this.error1 = new JLabel(CoreConstants.SPC);
        this.error1.setFont(Skin.MEDIUM_18_FONT);
        this.error1.setHorizontalAlignment(SwingConstants.CENTER);
        this.error1.setForeground(Skin.ERROR_COLOR);

        this.error2 = new JLabel(CoreConstants.SPC);
        this.error2.setFont(Skin.MEDIUM_18_FONT);
        this.error2.setHorizontalAlignment(SwingConstants.CENTER);
        this.error2.setForeground(Skin.ERROR_COLOR);

        errorPane.add(this.error1, BorderLayout.NORTH);
        errorPane.add(this.error2, BorderLayout.SOUTH);
    }

    /**
     * Called when the "Loan" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (STU.equals(cmd)) {
            processStudentId();
        } else if (DONE.equals(cmd)) {
            processDone();
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

        processStudentId();
    }

    /**
     * Sets focus.
     */
    /* default */ void focus() {

        this.studentIdField.requestFocus();
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    /* default */ void reset() {

        this.studentIdField.setText(CoreConstants.EMPTY);
        this.studentIdField.setBackground(Skin.FIELD_BG);

        this.studentNameDisplay.setText(CoreConstants.SPC);

        this.table.clear();

        this.error1.setText(CoreConstants.SPC);
        this.error2.setText(CoreConstants.SPC);

        this.studentIdField.requestFocus();

        getRootPane().setDefaultButton(this.doneBtn);
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
        String foundLastName = null;
        final String sql1 = "SELECT first_name, last_name "
                + "FROM student WHERE stu_id=?";

        try (final PreparedStatement ps = this.cache.conn.prepareStatement(sql1)) {
            ps.setString(1, cleanStu);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    foundFirstName = rs.getString(1);
                    foundLastName = rs.getString(2);
                    this.studentIdField.setBackground(Skin.FIELD_BG);

                    final String first =
                            foundFirstName == null ? CoreConstants.EMPTY : foundFirstName.trim();
                    final String last =
                            foundLastName == null ? CoreConstants.EMPTY : foundLastName.trim();

                    this.studentNameDisplay.setText(first + CoreConstants.SPC + last);

                } else {
                    this.error1.setText("Student not found.");
                    this.error2.setText(CoreConstants.SPC);
                    this.studentIdField.setBackground(Skin.FIELD_ERROR_BG);
                }
            }
        } catch (final SQLException ex) {
            this.error1.setText("Error querying student table:");
            if (ex.getMessage() == null) {
                this.error2.setText(ex.getClass().getSimpleName());
            } else {
                this.error2.setText(ex.getMessage());
            }
        }

        if (foundLastName != null) {

            // Get all resource records and build map from resource ID to resource type
            final Map<String, String> resourceMap = new HashMap<>(100);

            try (final Statement stmt = this.cache.conn.createStatement()) {
                try (final ResultSet rs = stmt.executeQuery(//
                        "SELECT resource_id, resource_type FROM resource")) {
                    while (rs.next()) {
                        resourceMap.put(rs.getString(1), rs.getString(2));
                    }
                }
            } catch (final SQLException ex) {
                this.error1.setText("Error querying resource table:");
                if (ex.getMessage() == null) {
                    this.error2.setText(ex.getClass().getSimpleName());
                } else {
                    this.error2.setText(ex.getMessage());
                }
            }

            if (!resourceMap.isEmpty()) {
                final String sql3 = "SELECT resource_id,loan_dt,start_time,due_dt, "
                        + "return_dt,finish_time FROM stresource WHERE stu_id=? "
                        + "ORDER BY loan_dt, start_time";

                final List<StudentResourceLoanRow> records = new ArrayList<>(10);
                try (final PreparedStatement ps2 = this.cache.conn.prepareStatement(sql3)) {
                    ps2.setString(1, cleanStu);
                    try (final ResultSet rs = ps2.executeQuery()) {
                        while (rs.next()) {
                            final String resId = rs.getString(1);
                            final Date loanDt = rs.getDate(2);
                            final int loanTime = rs.getInt(3);
                            final Date dueDt = rs.getDate(4);
                            final Date returnDt = rs.getDate(5);
                            final int returnTime = rs.getInt(6);
                            final String type = resourceMap.get(resId);

                            final int lh = loanTime / 60;
                            final int lm = loanTime % 60;
                            final LocalDateTime lent = loanDt == null ? null
                                    : LocalDateTime.of(loanDt.toLocalDate(), LocalTime.of(lh, lm));

                            final LocalDate due = dueDt == null ? null : dueDt.toLocalDate();

                            final int rh = returnTime / 60;
                            final int rm = returnTime % 60;
                            final LocalDateTime returned = returnDt == null ? null
                                    : LocalDateTime.of(returnDt.toLocalDate(), LocalTime.of(rh, rm));

                            records
                                    .add(new StudentResourceLoanRow(resId, lent, due, returned, type));
                        }

                        this.table.clear();
                        this.table.addData(records, 2);
                    }
                } catch (final SQLException ex) {
                    this.error1.setText("Error querying stresource table:");
                    if (ex.getMessage() == null) {
                        this.error2.setText(ex.getClass().getSimpleName());
                    } else {
                        this.error2.setText(ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Called when the "Done" button is pressed.
     */
    private void processDone() {

        reset();
    }
}
