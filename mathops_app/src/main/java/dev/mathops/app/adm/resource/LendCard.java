package dev.mathops.app.adm.resource;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawResourceLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStresourceLogic;
import dev.mathops.db.old.rawrecord.RawResource;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStresource;
import dev.mathops.db.rec.TermRec;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * A card panel to lend a resource.
 */
final class LendCard extends AdmPanelBase implements ActionListener, FocusListener {

    /** An action command. */
    private static final String STU = "STU";

    /** An action command. */
    private static final String RES = "RES";

    /** An action command. */
    private static final String LEND = "LEND";

    /** An action command. */
    private static final String CANCEL = "CANCEL";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1993186332487407571L;

    /** The data cache. */
    private final Cache cache;

    /** The owning resource pane. */
    private final TopPanelResource owner;

    /** The student ID field. */
    private final JTextField studentIdField;

    /** The cleaned student ID. */
    private String cleanedStudentId;

    /** The student name display field. */
    private final JLabel studentNameDisplay;

    /** The resource ID field. */
    private final JTextField resourceIdField;

    /** The type of JLabel scanned. */
    private final JLabel resourceTypeDisplay;

    /** The number of days allowed on the loan. */
    private int days;

    /** The lend button. */
    private final JButton lendBtn;

    /** The panel that holds the error message. */
    private final JPanel errorPane;

    /** An error message. */
    private final JLabel error1;

    /** An error message. */
    private final JLabel error2;

    /** A checkbox to allow bypass of course registration requirement for calculators. */
    private final JCheckBox bypassRegRequirement;

    /**
     * Constructs a new {@code LendCard}.
     *
     * @param theCache the data cache
     * @param fixed    the fixed data
     */
    LendCard(final Cache theCache, final UserData fixed, final TopPanelResource theOwner) {

        super();

        this.owner = theOwner;

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(getBackground());
        panel.setBorder(getBorder());

        setBackground(Skin.LT_GREEN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;

        panel.add(makeHeader("Lend Item", false), BorderLayout.PAGE_START);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_GREEN);
        panel.add(center, BorderLayout.CENTER);

        // Student ID entry row (label and text field)
        // Name of selected student
        // Resource ID entry row (label and text field)
        // Type of selected resource
        // [Lend] and [Cancel] buttons
        // Error messages

        // In this panel's "north" position:
        // inner1 (header in N, errors in S, inner2 in center)
        // inner2 (student entry in N, [lend] in S, inner3 in center)
        // inner3 (stu name in N, resource type in S, resource entry in center)

        final JPanel inner1 = new JPanel(new BorderLayout(10, 10));
        inner1.setBackground(Skin.OFF_WHITE_GREEN);
        final JPanel inner2 = new JPanel(new BorderLayout(10, 10));
        inner2.setBackground(Skin.OFF_WHITE_GREEN);
        final JPanel inner3 = new JPanel(new BorderLayout(10, 10));
        inner3.setBackground(Skin.OFF_WHITE_GREEN);

        inner1.add(inner2, BorderLayout.CENTER);
        inner2.add(inner3, BorderLayout.CENTER);
        center.add(inner1, BorderLayout.PAGE_START);

        //

        final JLabel studentIdLabel = new JLabel("Student ID:");
        studentIdLabel.setFont(Skin.MEDIUM_HEADER_18_FONT);
        studentIdLabel.setForeground(Skin.LABEL_COLOR);
        studentIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        final Dimension d1 = studentIdLabel.getPreferredSize();

        final JLabel resourceIdLabel = new JLabel("Resource ID:");
        resourceIdLabel.setFont(Skin.MEDIUM_HEADER_18_FONT);
        resourceIdLabel.setForeground(Skin.LABEL_COLOR);
        resourceIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        final Dimension d2 = resourceIdLabel.getPreferredSize();

        final JLabel spacer1 = new JLabel(CoreConstants.SPC);
        final JLabel spacer2 = new JLabel(CoreConstants.SPC);

        final Dimension dMax = new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
        studentIdLabel.setPreferredSize(dMax);
        resourceIdLabel.setPreferredSize(dMax);
        spacer1.setPreferredSize(dMax);
        spacer2.setPreferredSize(dMax);

        this.studentIdField = new JTextField(15);
        this.studentIdField.setFont(Skin.MONO_16_FONT);
        this.studentIdField.setBackground(Skin.FIELD_BG);
        this.studentIdField.setActionCommand(STU);
        this.studentIdField.addActionListener(this);
        this.studentIdField.addFocusListener(this);
        final JPanel studentFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        studentFlow.setBackground(Skin.OFF_WHITE_GREEN);
        studentFlow.add(studentIdLabel);
        studentFlow.add(this.studentIdField);
        inner2.add(studentFlow, BorderLayout.PAGE_START);

        //

        this.studentNameDisplay = new JLabel(CoreConstants.SPC);
        this.studentNameDisplay.setFont(Skin.MEDIUM_HEADER_18_FONT);
        this.studentNameDisplay.setForeground(Skin.ERROR_COLOR);
        this.studentNameDisplay.setPreferredSize(this.studentIdField.getPreferredSize());
        final JPanel studentNameFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        studentNameFlow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        studentNameFlow.setBackground(Skin.OFF_WHITE_GREEN);
        studentNameFlow.add(spacer1);
        studentNameFlow.add(this.studentNameDisplay);
        inner3.add(studentNameFlow, BorderLayout.PAGE_START);

        this.resourceIdField = new JTextField(15);
        this.resourceIdField.setFont(Skin.MONO_16_FONT);
        this.resourceIdField.setBackground(Skin.FIELD_BG);
        this.resourceIdField.setEnabled(false);
        this.resourceIdField.setActionCommand(RES);
        this.resourceIdField.addActionListener(this);
        this.resourceIdField.addFocusListener(this);
        final JPanel resourceFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        resourceFlow.setBackground(Skin.OFF_WHITE_GREEN);
        resourceFlow.add(resourceIdLabel);
        resourceFlow.add(this.resourceIdField);
        inner3.add(resourceFlow, BorderLayout.CENTER);

        this.resourceTypeDisplay = new JLabel(CoreConstants.SPC);
        this.resourceTypeDisplay.setIconTextGap(20);
        this.resourceTypeDisplay.setFont(Skin.MEDIUM_HEADER_18_FONT);
        this.resourceTypeDisplay.setForeground(Skin.ERROR_COLOR);
        final JPanel resourceTypeFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        resourceTypeFlow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        resourceTypeFlow.setBackground(Skin.OFF_WHITE_GREEN);
        resourceTypeFlow.add(spacer2);
        resourceTypeFlow.add(this.resourceTypeDisplay);
        inner3.add(resourceTypeFlow, BorderLayout.PAGE_END);

        //

        final JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPane.setBackground(Skin.OFF_WHITE_GREEN);
        this.lendBtn = new JButton("Lend");
        this.lendBtn.setFont(Skin.BIG_BUTTON_20_FONT);
        this.lendBtn.setActionCommand(LEND);
        this.lendBtn.addActionListener(this);
        this.lendBtn.setEnabled(false);
        final JButton cancelBtn = new JButton("Start Over");
        cancelBtn.setFont(Skin.BIG_BUTTON_20_FONT);
        cancelBtn.setActionCommand(CANCEL);
        cancelBtn.addActionListener(this);
        buttonsPane.add(this.lendBtn);
        buttonsPane.add(cancelBtn);
        inner2.add(buttonsPane, BorderLayout.PAGE_END);

        //

        this.errorPane = new JPanel(new BorderLayout());
        this.errorPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        this.errorPane.setBackground(Skin.OFF_WHITE_GREEN);
        inner1.add(this.errorPane, BorderLayout.PAGE_END);

        this.error1 = new JLabel(CoreConstants.SPC);
        this.error1.setFont(Skin.MEDIUM_18_FONT);
        this.error1.setHorizontalAlignment(SwingConstants.CENTER);
        this.error1.setForeground(Skin.ERROR_COLOR);

        this.error2 = new JLabel(CoreConstants.SPC);
        this.error2.setFont(Skin.MEDIUM_18_FONT);
        this.error2.setHorizontalAlignment(SwingConstants.CENTER);
        this.error2.setForeground(Skin.ERROR_COLOR);

        this.errorPane.add(this.error1, BorderLayout.PAGE_START);
        this.errorPane.add(this.error2, BorderLayout.PAGE_END);

        this.bypassRegRequirement = new JCheckBox();

        final Integer loanClear = fixed.getClearanceLevel("RES_LOAN");
        if (loanClear != null && loanClear.intValue() < 3) {
            // User has permissions - allow them to override registration requirements

            final JPanel checkboxRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            checkboxRow.setBackground(Skin.OFF_WHITE_GREEN);
            checkboxRow.add(this.bypassRegRequirement);

            final JLabel lbl =
                    new JLabel("Allow issuing calculator without active registration.");
            lbl.setFont(Skin.MEDIUM_HEADER_15_FONT);

            checkboxRow.add(lbl);
            center.add(checkboxRow, BorderLayout.PAGE_END);
        }
    }

    /**
     * Called when the "Lend" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (STU.equals(cmd)) {
            processStudentId();
        } else if (RES.equals(cmd)) {
            processResourceId();
        } else if (LEND.equals(cmd)) {
            processLend();
        } else if (CANCEL.equals(cmd)) {
            processCancel();
        }
    }

    /**
     * Called when a text field gains focus.
     *
     * @param e the focus event
     */
    @Override
    public void focusGained(final FocusEvent e) {

        // No action
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
                this.resourceIdField.setEnabled(false);
                this.lendBtn.setEnabled(false);
                this.studentIdField.requestFocus();
            } else {
                processStudentId();
            }
        } else if (e.getComponent() == this.resourceIdField) {
            if (this.resourceIdField.getText().isEmpty() && !this.studentIdField.getText().isEmpty()) {
                this.lendBtn.setEnabled(false);
                this.resourceIdField.requestFocus();
            } else {
                processResourceId();
            }
        }
    }

    /**
     * Sets focus.
     */
    void focus() {

        if (this.lendBtn.isEnabled()) {
            this.lendBtn.requestFocus();
        } else if (this.resourceIdField.isEnabled()) {
            this.resourceIdField.requestFocus();
        } else {
            this.studentIdField.requestFocus();
        }
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    void reset() {

        this.studentIdField.setText(CoreConstants.EMPTY);
        this.studentIdField.setBackground(Skin.FIELD_BG);

        this.studentNameDisplay.setText(CoreConstants.SPC);

        this.resourceIdField.setText(CoreConstants.EMPTY);
        this.resourceIdField.setBackground(Skin.FIELD_BG);
        this.resourceIdField.setEnabled(false);

        this.resourceTypeDisplay.setIcon(null);
        this.resourceTypeDisplay.setText(CoreConstants.SPC);

        this.days = 0;

        this.lendBtn.setEnabled(false);

        this.error1.setText(CoreConstants.SPC);
        this.error2.setText(CoreConstants.SPC);

        this.errorPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        this.errorPane.setBackground(Skin.OFF_WHITE_GREEN);

        getRootPane().setDefaultButton(this.lendBtn);

        this.studentIdField.requestFocus();
    }

    /**
     * Called when a student ID is entered and "Return" is pressed in that field (typically, by the bar code scanner
     * reading a student ID card).
     */
    private void processStudentId() {

        this.cleanedStudentId = null;
        this.studentNameDisplay.setText(CoreConstants.SPC);

        final String stuId = this.studentIdField.getText();

        if ("R".equals(stuId) || "r".equals(stuId)) {
            this.owner.goToReturn();
        } else if ("L".equals(stuId) || "l".equals(stuId)) {
            // No action
            this.studentIdField.setText(CoreConstants.EMPTY);
        } else {
            final String cleanStu = stuId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);

            final String foundFirstName;
            final String foundLastName;
            final String sql1 = "SELECT first_name, last_name FROM student WHERE stu_id=?";

            final DbConnection conn = this.cache.checkOutConnection(ESchema.LEGACY);

            try (final PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, cleanStu);
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        foundFirstName = rs.getString(1);
                        foundLastName = rs.getString(2);
                        this.studentIdField.setBackground(Skin.FIELD_BG);

                        this.cleanedStudentId = cleanStu;

                        final String first = foundFirstName == null ? CoreConstants.EMPTY : foundFirstName.trim();
                        final String last = foundLastName == null ? CoreConstants.EMPTY : foundLastName.trim();

                        this.studentNameDisplay.setText(first + CoreConstants.SPC + last);

                        this.resourceIdField.setEnabled(true);
                        this.resourceIdField.requestFocus();
                    } else {
                        this.error1.setText("Student not found.");
                        this.error2.setText(CoreConstants.SPC);

                        this.errorPane.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.RED, 2),
                                BorderFactory.createEmptyBorder(20, 0, 0, 0)));
                        this.errorPane.setBackground(Skin.OFF_WHITE_RED);

                        this.studentIdField.setBackground(Skin.FIELD_ERROR_BG);
                        this.resourceIdField.setEnabled(false);
                        this.lendBtn.setEnabled(false);
                    }
                }
            } catch (final SQLException ex) {
                this.error1.setText("Error querying student table:");
                if (ex.getMessage() == null) {
                    this.error2.setText(ex.getClass().getSimpleName());
                } else {
                    this.error2.setText(ex.getMessage());
                }

                this.errorPane.setBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 2),
                                BorderFactory.createEmptyBorder(20, 0, 0, 0)));
                this.errorPane.setBackground(Skin.OFF_WHITE_RED);
            } finally {
                Cache.checkInConnection(conn);
            }
        }
    }

    /**
     * Called when a resource ID is entered and "Return" is pressed in that field (typically, by the bar code scanner
     * reading a resource barcode).
     */
    private void processResourceId() {

        final String resId = this.resourceIdField.getText();

        if (resId == null || resId.isEmpty()) {
            this.resourceIdField.setBackground(Skin.FIELD_ERROR_BG);
        } else {
            // Load the resource record
            final String cleanRes = resId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);
            final String foundType;

            final String sql2 = "SELECT resource_type, days_allowed FROM resource WHERE resource_id=?";

            final DbConnection conn = this.cache.checkOutConnection(ESchema.LEGACY);

            try (final PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setString(1, cleanRes);

                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        foundType = rs.getString(1);
                        this.days = rs.getInt(2);

                        BufferedImage icon = null;
                        final String type;

                        switch (foundType) {
                            case RawResource.TYPE_INHOUSE_CALC -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "TI84-icon.png", false);
                                type = "TI-84 calculator";
                            }
                            case RawResource.TYPE_OFFICE_CALC -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "TI84-icon.png", false);
                                type = "Office TI-84 calculator";
                            }
                            case RawResource.TYPE_RENTAL_CALC -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "TI84-icon.png", false);
                                type = "*** RENTAL *** TI-84 calculator (SHOULD NOT LEND)";
                            }
                            case RawResource.TYPE_RENTAL_MANUAL -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "TI84-book-icon.png", false);
                                type = "TI-84 calculator manual";
                            }
                            case RawResource.TYPE_INHOUSE_IPAD -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "ipad-icon.png", false);
                                type = "iPad tablet";
                            }
                            case RawResource.TYPE_INHOUSE_NOTEBOOK -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "laptop-icon.png", false);
                                type = "Windows notebook";
                            }
                            case RawResource.TYPE_INHOUSE_TEXT -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "textbook-icon.png", false);
                                type = "In-house textbook";
                            }
                            case RawResource.TYPE_OVERNIGHT_TEXT -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "textbook-icon.png", false);
                                type = "Overnight textbook";
                            }
                            case RawResource.TYPE_INHOUSE_HEADSET -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "headphones-icon.png", false);
                                type = "Headphones";
                            }
                            case RawResource.TYPE_INHOUSE_LOCK -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "lock-icon.png", false);
                                type = "Padlock";
                            }
                            case RawResource.TYPE_TUTOR_TABLET -> {
                                icon = FileLoader.loadFileAsImage(LendCard.class, "tablet-icon.png", false);
                                type = "Tutor Tablet";
                            }
                            case null, default -> type = "*** Unknown resource type ***";
                        }

                        if (icon == null) {
                            this.resourceTypeDisplay.setIcon(null);
                        } else {
                            this.resourceTypeDisplay.setIcon(new ImageIcon(icon));
                        }
                        this.resourceTypeDisplay.setText(type);

                        this.resourceIdField.setBackground(Skin.FIELD_BG);

                        this.lendBtn.setEnabled(true);
                        this.error1.setText(CoreConstants.SPC);
                        this.error2.setText(CoreConstants.SPC);
                        this.errorPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                        this.errorPane.setBackground(Skin.OFF_WHITE_GREEN);
                        this.lendBtn.requestFocus();
                    } else {
                        this.error1.setText("Resource not found.");
                        this.error2.setText(CoreConstants.SPC);
                        this.resourceIdField.setBackground(Skin.FIELD_ERROR_BG);
                        this.lendBtn.setEnabled(false);

                        this.errorPane.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.RED, 2),
                                BorderFactory.createEmptyBorder(20, 0, 0, 0)));
                        this.errorPane.setBackground(Skin.OFF_WHITE_RED);
                    }
                }
            } catch (final SQLException ex) {
                this.error1.setText("Error querying resource table:");
                if (ex.getMessage() == null) {
                    this.error2.setText(ex.getClass().getSimpleName());
                } else {
                    this.error2.setText(ex.getMessage());
                }

                this.errorPane.setBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 2),
                                BorderFactory.createEmptyBorder(20, 0, 0, 0)));
                this.errorPane.setBackground(Skin.OFF_WHITE_RED);
            } finally {
                Cache.checkInConnection(conn);
            }
        }
    }

    /**
     * Called when the "Lend" button is pressed.
     */
    private void processLend() {

        final String resId = this.resourceIdField.getText();
        final String cleanRes = resId == null ? CoreConstants.EMPTY
                : resId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                .replace(CoreConstants.DASH, CoreConstants.EMPTY);

        // See if the resource is already checked out
        final String sql3 = "SELECT stu_id FROM stresource WHERE resource_id=? AND return_dt IS NULL";

        String foundStu = null;
        final DbConnection conn = this.cache.checkOutConnection(ESchema.LEGACY);

        try (final PreparedStatement ps = conn.prepareStatement(sql3)) {
            ps.setString(1, cleanRes);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    foundStu = rs.getString(1);
                }
            }
        } catch (final SQLException ex) {
            this.error1.setText("Error querying stresource table:");
            if (ex.getMessage() == null) {
                this.error2.setText(ex.getClass().getSimpleName());
            } else {
                this.error2.setText(ex.getMessage());
            }

            this.errorPane.setBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 2),
                            BorderFactory.createEmptyBorder(20, 0, 0, 0)));
            this.errorPane.setBackground(Skin.OFF_WHITE_RED);
        } finally {
            Cache.checkInConnection(conn);
        }

        if (foundStu == null) {
            // Check eligibility for lend...
            final String whyNotEligible = testEligibleForLend(this.cleanedStudentId, cleanRes);

            if (whyNotEligible == null) {
                final LocalDate today = LocalDate.now();
                final LocalDate due = today.plusDays(this.days);

                final LocalTime now = LocalTime.now();
                final int start = now.getHour() * 60 + now.getMinute();

                final RawStresource record = new RawStresource(this.cleanedStudentId, cleanRes, today,
                        Integer.valueOf(start), due, null, null, Integer.valueOf(0), today);

                try {
                    if (RawStresourceLogic.insert(this.cache, record)) {
                        JOptionPane.showMessageDialog(this, "Loan has been recorded.");
                        reset();
                    } else {
                        this.error1.setText("Error inserting into stresource table:");
                        this.error2.setText(CoreConstants.SPC);

                        this.errorPane.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.RED, 2),
                                BorderFactory.createEmptyBorder(20, 0, 0, 0)));
                        this.errorPane.setBackground(Skin.OFF_WHITE_RED);
                    }
                } catch (final SQLException ex) {
                    this.error1.setText("Error inserting into stresource table:");
                    if (ex.getMessage() == null) {
                        this.error2.setText(ex.getClass().getSimpleName());
                    } else {
                        this.error2.setText(ex.getMessage());
                    }

                    this.errorPane.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.RED, 2),
                            BorderFactory.createEmptyBorder(20, 0, 0, 0)));
                    this.errorPane.setBackground(Skin.OFF_WHITE_RED);
                }
            } else {
                this.error1.setText(whyNotEligible);

                this.errorPane.setBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 2),
                                BorderFactory.createEmptyBorder(20, 0, 0, 0)));
                this.errorPane.setBackground(Skin.OFF_WHITE_RED);
            }
        } else if (foundStu.equals(this.cleanedStudentId)) {
            // Nothing to do...
            JOptionPane.showMessageDialog(this, "Item is already checked out to this student.");
            reset();
        } else {
            this.error1.setText("Resource is checked out to a different student.");
            this.error2.setText(CoreConstants.SPC);

            // TODO: A way to check back in, and immediately check out to new student
            //  (which may remove a hold placed on the old student's account)

            this.errorPane.setBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 2),
                            BorderFactory.createEmptyBorder(20, 0, 0, 0)));
            this.errorPane.setBackground(Skin.OFF_WHITE_RED);
        }
    }

    /**
     * Tests whether a student is eligible to be lent a resource.
     *
     * @param stuId the student ID
     * @param resId the resource ID
     * @return {@code null} if the student is eligible for the loan, and if not, a string with a message to display to
     *         the staff member explaining why not
     */
    private String testEligibleForLend(final String stuId, final String resId) {

        String whyNotEligible = null;

        try {
            final RawResource resource = RawResourceLogic.query(this.cache, resId);

            if (RawResource.TYPE_TUTOR_TABLET.equals(resource.resourceType)) {
                // Tutor tablets are available only to people in SpecialStus as "TUTOR" or "ADMIN"
                final boolean ok = RawSpecialStusLogic.isSpecialType(this.cache, stuId, LocalDate.now(),
                        RawSpecialStus.TUTOR, RawSpecialStus.ADMIN);

                if (!ok) {
                    whyNotEligible = "Available only to Precalculus Center Learning Assistants.";
                }
            } else if (RawResource.TYPE_INHOUSE_CALC.equals(resource.resourceType)
                       || RawResource.TYPE_INHOUSE_NOTEBOOK.equals(resource.resourceType)
                       || RawResource.TYPE_INHOUSE_IPAD.equals(resource.resourceType)) {

                // In-house calculators, iPads, notebooks only issued to students with enrollment

                if (!this.bypassRegRequirement.isSelected()) {
                    final SystemData systemData = this.cache.getSystemData();
                    final TermRec active = systemData.getActiveTerm();
                    final List<RawStcourse> regs = RawStcourseLogic.getActiveForStudent(this.cache, stuId, active.term);

                    // NOTE: This should include up registrations in MATH 120 - they can check out resources
                    if (regs.isEmpty()) {
                        whyNotEligible = "Available only to students enrolled in Precalculus courses.";
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            whyNotEligible = "Unable to query resource with the specified ID.";
        }

        return whyNotEligible;
    }

    /**
     * Called when the "Start Over" button is pressed.
     */
    private void processCancel() {

        reset();
    }
}
