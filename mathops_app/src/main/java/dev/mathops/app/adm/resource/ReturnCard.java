package dev.mathops.app.adm.resource;

import dev.mathops.app.AppFileLoader;
import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.rawlogic.RawResourceLogic;
import dev.mathops.db.rawlogic.RawStresourceLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawAdminHold;
import dev.mathops.db.rawrecord.RawResource;
import dev.mathops.db.rawrecord.RawStresource;
import dev.mathops.db.rawrecord.RawStudent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A card panel to return a resource.
 */
final class ReturnCard extends AdminPanelBase implements ActionListener {

    /** An action command. */
    private static final String RES = "RES";

    /** An action command. */
    private static final String DONE = "DONE";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6363976238042003285L;

    /** The data cache. */
    private final Cache cache;

    /** The resource ID field. */
    private final JTextField resourceIdField;

    /** The type of resource scanned. */
    private final JLabel resourceTypeDisplay;

    /** The student name display field. */
    private final JLabel studentNameDisplay;

    /** The done button. */
    private final JButton doneBtn;

    /** An error message. */
    private final JLabel error1;

    /** An error message. */
    private final JLabel error2;

    /**
     * Constructs a new {@code ReturnCard}.
     *
     * @param theCache         the data cache
     */
    ReturnCard(final Cache theCache) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_RED);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_RED);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;

        panel.add(makeHeader("Return Item", false), BorderLayout.PAGE_START);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_RED);
        panel.add(center, BorderLayout.CENTER);

        // Resource ID entry row (label and text field)
        // Type of selected resource
        // Name of student who had item checked out
        // [Done] button
        // Error messages

        // In this panel's "north" position:
        // inner1 (header in N, errors in S, inner2 in center)
        // inner2 (resource entry in N, [Done] in S, inner3 in center)
        // inner3 (resource type in N, student name in S)

        final JPanel inner1 = new JPanel(new BorderLayout(10, 10));
        inner1.setBackground(Skin.OFF_WHITE_RED);
        final JPanel inner2 = new JPanel(new BorderLayout(10, 10));
        inner2.setBackground(Skin.OFF_WHITE_RED);
        final JPanel inner3 = new JPanel(new BorderLayout(10, 10));
        inner3.setBackground(Skin.OFF_WHITE_RED);

        inner1.add(inner2, BorderLayout.CENTER);
        inner2.add(inner3, BorderLayout.CENTER);
        center.add(inner1, BorderLayout.PAGE_START);

        //

        final JLabel resourceIdLabel = new JLabel("Resource ID:");
        resourceIdLabel.setFont(Skin.MEDIUM_HEADER_18_FONT);
        resourceIdLabel.setForeground(Skin.LABEL_COLOR);
        resourceIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        final JLabel spacer1 = new JLabel(CoreConstants.SPC);
        spacer1.setPreferredSize(resourceIdLabel.getPreferredSize());

        final JLabel spacer2 = new JLabel(CoreConstants.SPC);
        spacer2.setPreferredSize(resourceIdLabel.getPreferredSize());

        this.resourceIdField = new JTextField(15);
        this.resourceIdField.setFont(Skin.MONO_16_FONT);
        this.resourceIdField.setBackground(Skin.FIELD_BG);
        this.resourceIdField.setActionCommand(RES);
        this.resourceIdField.addActionListener(this);
        final JPanel resourceFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        resourceFlow.setBackground(Skin.OFF_WHITE_RED);
        resourceFlow.add(resourceIdLabel);
        resourceFlow.add(this.resourceIdField);
        inner2.add(resourceFlow, BorderLayout.PAGE_START);

        this.resourceTypeDisplay = new JLabel(CoreConstants.SPC);
        this.resourceTypeDisplay.setIconTextGap(20);
        this.resourceTypeDisplay.setFont(Skin.MEDIUM_HEADER_18_FONT);
        this.resourceTypeDisplay.setForeground(Skin.ERROR_COLOR);
        final JPanel resourceTypeFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        resourceTypeFlow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        resourceTypeFlow.setBackground(Skin.OFF_WHITE_RED);
        resourceTypeFlow.add(spacer1);
        resourceTypeFlow.add(this.resourceTypeDisplay);
        inner3.add(resourceTypeFlow, BorderLayout.PAGE_START);

        //

        this.studentNameDisplay = new JLabel(CoreConstants.SPC);
        this.studentNameDisplay.setFont(Skin.MEDIUM_HEADER_18_FONT);
        this.studentNameDisplay.setForeground(Skin.ERROR_COLOR);
        final JPanel studentNameFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        studentNameFlow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        studentNameFlow.setBackground(Skin.OFF_WHITE_RED);
        studentNameFlow.add(spacer2);
        studentNameFlow.add(this.studentNameDisplay);
        inner3.add(studentNameFlow, BorderLayout.PAGE_END);

        //

        final JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPane.setBackground(Skin.OFF_WHITE_RED);
        this.doneBtn = new JButton("Done");
        this.doneBtn.setFont(Skin.BIG_BUTTON_20_FONT);
        this.doneBtn.setActionCommand(DONE);
        this.doneBtn.addActionListener(this);
        buttonsPane.add(this.doneBtn);
        inner2.add(buttonsPane, BorderLayout.PAGE_END);

        //

        final JPanel errorPane = new JPanel(new BorderLayout());
        errorPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        errorPane.setBackground(Skin.OFF_WHITE_RED);
        inner1.add(errorPane, BorderLayout.PAGE_END);

        this.error1 = new JLabel(CoreConstants.SPC);
        this.error1.setFont(Skin.MEDIUM_18_FONT);
        this.error1.setHorizontalAlignment(SwingConstants.CENTER);
        this.error1.setForeground(Skin.ERROR_COLOR);

        this.error2 = new JLabel(CoreConstants.SPC);
        this.error2.setFont(Skin.MEDIUM_18_FONT);
        this.error2.setHorizontalAlignment(SwingConstants.CENTER);
        this.error2.setForeground(Skin.ERROR_COLOR);

        errorPane.add(this.error1, BorderLayout.PAGE_START);
        errorPane.add(this.error2, BorderLayout.PAGE_END);
    }

    /**
     * Called when the "Loan" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (RES.equals(cmd)) {
            processReturn();
        } else if (DONE.equals(cmd)) {
            processDone();
        }
    }

    /**
     * Sets focus.
     */
    void focus() {

        this.resourceIdField.requestFocus();
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    void reset() {

        this.resourceIdField.setText(CoreConstants.EMPTY);
        this.resourceIdField.setBackground(Skin.FIELD_BG);

        this.resourceTypeDisplay.setIcon(null);
        this.resourceTypeDisplay.setText(CoreConstants.SPC);

        this.studentNameDisplay.setText(CoreConstants.SPC);

        this.error1.setText(CoreConstants.SPC);
        this.error2.setText(CoreConstants.SPC);

        this.resourceIdField.requestFocus();

        getRootPane().setDefaultButton(this.doneBtn);
    }

    /**
     * Called when a resource ID is entered and "Return" is pressed in that field (typically, by the bar code scanner
     * reading a resource barcode).
     */
    private void processReturn() {

        this.error1.setText(CoreConstants.SPC);
        this.error2.setText(CoreConstants.SPC);

        final String resId = this.resourceIdField.getText();

        if (resId == null || resId.isEmpty()) {
            this.resourceIdField.setBackground(Skin.FIELD_ERROR_BG);
        } else {
            final String cleanRes = resId.trim().replace(CoreConstants.SPC, CoreConstants.EMPTY)
                    .replace(CoreConstants.DASH, CoreConstants.EMPTY);

            final String sql1 = "SELECT resource_type FROM resource WHERE resource_id=?";

            String foundType = null;
            try (final PreparedStatement ps = this.cache.conn.prepareStatement(sql1)) {
                ps.setString(1, cleanRes);

                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        foundType = rs.getString(1);

                        BufferedImage icon = null;
                        final String type;

                        if (RawResource.TYPE_INHOUSE_CALC.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "TI84-icon.png", false);
                            type = "TI-84 calculator";
                        } else if (RawResource.TYPE_OFFICE_CALC.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "TI84-icon.png", false);
                            type = "Office TI-84 calculator";
                        } else if (RawResource.TYPE_RENTAL_CALC.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "TI84-icon.png", false);
                            type = "*** RENTAL *** TI-84 calculator (SHOULD NOT LEND)";
                        } else if (RawResource.TYPE_RENTAL_MANUAL.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "TI84-book-icon.png", false);
                            type = "TI-84 calculator manual";
                        } else if (RawResource.TYPE_INHOUSE_IPAD.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "ipad-icon.png", false);
                            type = "iPad tablet";
                        } else if (RawResource.TYPE_INHOUSE_NOTEBOOK.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "laptop-icon.png", false);
                            type = "Windows notebook";
                        } else if (RawResource.TYPE_INHOUSE_TEXT.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "textbook-icon.png", false);
                            type = "In-house textbook";
                        } else if (RawResource.TYPE_OVERNIGHT_TEXT.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "textbook-icon.png", false);
                            type = "Overnight textbook";
                        } else if (RawResource.TYPE_INHOUSE_HEADSET.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "headphones-icon.png", false);
                            type = "Headphones";
                        } else if (RawResource.TYPE_INHOUSE_LOCK.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "lock-icon.png", false);
                            type = "Padlock";
                        } else if (RawResource.TYPE_TUTOR_TABLET.equals(foundType)) {
                            icon = AppFileLoader.loadFileAsImage(LendCard.class, "tablet-icon.png", false);
                            type = "Tutor Tablet";
                        } else {
                            type = "*** Unknown resource type ***";
                        }

                        if (icon == null) {
                            this.resourceTypeDisplay.setIcon(null);
                        } else {
                            this.resourceTypeDisplay.setIcon(new ImageIcon(icon));
                        }
                        this.resourceTypeDisplay.setText(type);

                        this.resourceIdField.setBackground(Skin.FIELD_BG);
                    } else {
                        this.resourceIdField.setBackground(Skin.FIELD_ERROR_BG);
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

            if (foundType != null) {
                final String sql2 = "SELECT stu_id FROM stresource WHERE resource_id=? AND return_dt IS NULL";

                String foundStu = null;
                try (final PreparedStatement ps2 = this.cache.conn.prepareStatement(sql2)) {
                    ps2.setString(1, cleanRes);
                    try (final ResultSet rs = ps2.executeQuery()) {
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
                }

                if (foundStu == null) {
                    this.error1.setText("This item is not checked out.");
                    this.error2.setText("Check that item barcode was entered correctly.");
                } else {
                    final RawStudent stu;
                    try {
                        stu = RawStudentLogic.query(this.cache, foundStu, false);

                        if (stu == null) {
                            this.studentNameDisplay.setText("Student " + foundStu + " (not in database!)");
                        } else {
                            final String first = stu.firstName == null ? CoreConstants.EMPTY : stu.firstName;
                            final String last = stu.lastName == null ? CoreConstants.EMPTY : stu.lastName;

                            this.studentNameDisplay.setText(
                                    "Item was checked out to: " + first + CoreConstants.SPC + last);

                            // Process the return...
                            final LocalDate today = LocalDate.now();

                            final String sql4 = "UPDATE stresource set return_dt=?, finish_time=? "
                                    + "WHERE stu_id=? AND resource_id=? AND return_dt IS NULL";

                            final LocalTime now = LocalTime.now();
                            final int finish = now.getHour() * 60 + now.getMinute();

                            try (final PreparedStatement ps = this.cache.conn.prepareStatement(sql4)) {
                                ps.setDate(1, Date.valueOf(today));
                                ps.setInt(2, finish);
                                ps.setString(3, foundStu);
                                ps.setString(4, cleanRes);

                                final int numRows = ps.executeUpdate();

                                if (numRows == 1) {
                                    this.cache.conn.commit();
                                    this.error1.setText("Item has been returned.");
                                } else {
                                    this.error1.setText("Error updating stresource table:");
                                }
                            } catch (final SQLException ex) {
                                this.error1.setText("Error updating stresource table:");
                                if (ex.getMessage() == null) {
                                    this.error2.setText(ex.getClass().getSimpleName());
                                } else {
                                    this.error2.setText(ex.getMessage());
                                }
                            }

                            // See if there are any holds that can be removed
                            recalculateHolds(stu);
                        }
                    } catch (final SQLException ex1) {
                        this.error1.setText("Failed to query student record");
                        this.error2.setText(ex1.getMessage());
                    }

                    this.doneBtn.requestFocus();
                }
            }
        }
    }

    /**
     * Recalculates the resource-related holds for a student.
     *
     * @param stu the student
     */
    private void recalculateHolds(final RawStudent stu) {

        Log.info("Recalculating  holds for ", stu.stuId);

        try {
            List<RawAdminHold> holds = RawAdminHoldLogic.queryByStudent(this.cache, stu.stuId);

            final List<RawStresource> outstanding = RawStresourceLogic.queryByStudent(this.cache, stu.stuId);

            final List<String> outstandingTypes = new ArrayList<>(outstanding.size());
            for (final RawStresource row : outstanding) {
                if (row.returnDt == null) {
                    final RawResource res = RawResourceLogic.query(this.cache, row.resourceId);
                    if (res != null) {
                        outstandingTypes.add(res.resourceType);
                    }
                }
            }
            Log.info("  Outstanding: ", outstandingTypes);
            Log.info("  Holds: ", holds);

            for (final RawAdminHold hold : holds) {
                boolean contains = false;
                if ("42".equals(hold.holdId)) {
                    // Overdue textbook (IT)
                    contains = outstandingTypes.contains("IT");
                } else if ("43".equals(hold.holdId)) {
                    // Overdue overnight video (OV)
                    contains = outstandingTypes.contains("OV");
                } else if ("44".equals(hold.holdId)) {
                    // Overdue overnight textbook (OT)
                    contains = outstandingTypes.contains("OT");
                } else if ("45".equals(hold.holdId)) {
                    // Overdue calculator (IC)
                    contains = outstandingTypes.contains("IC");
                } else if ("46".equals(hold.holdId)) {
                    // Overdue rental calculator (RC)
                    contains = outstandingTypes.contains("RC");
                } else if ("47".equals(hold.holdId)) {
                    // Overdue rental calculator manual (RM)
                    contains = outstandingTypes.contains("RM");
                } else if ("51".equals(hold.holdId)) {
                    // Overdue headphones (IH)
                    contains = outstandingTypes.contains("IH");
                } else if ("52".equals(hold.holdId)) {
                    // Overdue pad-lock (IL)
                    contains = outstandingTypes.contains("IL");
                } else if ("53".equals(hold.holdId)) {
                    // Overdue iPad (IP)
                    contains = outstandingTypes.contains("IP");
                } else if ("54".equals(hold.holdId)) {
                    // Overdue Laptop (IW)
                    contains = outstandingTypes.contains("IN");
                }

                if (!contains) {
                    Log.info("Deleting hold " + hold.holdId);
                    RawAdminHoldLogic.INSTANCE.delete(this.cache, hold);
                }
            }

            holds = RawAdminHoldLogic.queryByStudent(this.cache, stu.stuId);
            String sev = null;
            for (final RawAdminHold row : holds) {
                if ("F".equals(row.sevAdminHold)) {
                    sev = "F";
                } else if (sev == null && "N".equals(row.sevAdminHold)) {
                    sev = "N";
                }
            }

            if (!Objects.equals(sev, stu.sevAdminHold)) {
                RawStudentLogic.updateHoldSeverity(this.cache, stu.stuId, sev);
            }
        } catch (final SQLException ex) {
            this.error1.setText("Failed to query holds, outstanding resouces");
            this.error2.setText(ex.getMessage());
        }
    }

    /**
     * Called when the "Done" button is pressed.
     */
    private void processDone() {

        reset();
    }
}
