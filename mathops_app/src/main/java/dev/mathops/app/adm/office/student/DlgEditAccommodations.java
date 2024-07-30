package dev.mathops.app.adm.office.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.sql.SQLException;
import java.util.Objects;

/**
 * A dialog to edit the time-limit factor and extension days fields on a student record.
 */
public final class DlgEditAccommodations extends JFrame implements ActionListener {

    /** The dialog title. */
    private static final String TITLE = "Edit Accommodations";

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The data cache. */
    private final Cache cache;

    /** The owning panel to be refreshed if an appeal record is added. */
    private final IStudentListener owner;

    /** The field for the student ID. */
    private final JTextField studentIdField;

    /** The field for the student name. */
    private final JTextField studentNameField;

    /** The field for the time limit factor. */
    private final JTextField timelimitFactor;

    /** The field for the the number of extension days. */
    private final JTextField extensionDays;

    /** The "Apply" button". */
    private final JButton applyButton;

    /**
     * Constructs a new {@code DlgEditAccommodations}.
     *
     * @param theCache the data cache
     * @param theOwner the owning panel to be refreshed if an appeal record is added
     */
    public DlgEditAccommodations(final Cache theCache, final IStudentListener theOwner) {

        super(TITLE);
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.owner = theOwner;

        final JPanel content = AdmPanelBase.makeOffWhitePanel(new StackedBorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        content.setBorder(padding);
        setContentPane(content);

        final JLabel[] labels = new JLabel[4];

        labels[0] = new JLabel("Student ID: ");
        labels[1] = new JLabel("Student Name: ");
        labels[2] = new JLabel("Time Limit Factor: ");
        labels[3] = new JLabel("Extension Days: ");
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

        this.timelimitFactor = new JTextField(9);
        this.timelimitFactor.setFont(Skin.MEDIUM_13_FONT);

        this.extensionDays = new JTextField(9);
        this.extensionDays.setFont(Skin.MEDIUM_13_FONT);

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
        flow3.add(this.timelimitFactor);
        content.add(flow3, StackedBorderLayout.NORTH);

        final JPanel flow4 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        flow4.add(labels[3]);
        flow4.add(this.extensionDays);
        content.add(flow4, StackedBorderLayout.NORTH);

        this.applyButton = new JButton("Apply");
        this.applyButton.setFont(Skin.BUTTON_13_FONT);
        this.applyButton.setActionCommand(APPLY_CMD);
        this.applyButton.addActionListener(this);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(Skin.BUTTON_13_FONT);
        cancelButton.setActionCommand(CANCEL_CMD);
        cancelButton.addActionListener(this);

        final JPanel flow6 = AdmPanelBase.makeOffWhitePanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        flow6.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        flow6.add(this.applyButton);
        flow6.add(cancelButton);
        content.add(flow6, StackedBorderLayout.NORTH);

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
    public void populateDisplay(final StudentData data) {

        this.studentIdField.setText(data.student.stuId);

        final String screenName = data.student.getScreenName();
        this.studentNameField.setText(screenName);

        if (data.student.timelimitFactor == null) {
            this.timelimitFactor.setText(CoreConstants.EMPTY);
        } else {
            final String factorStr = data.student.timelimitFactor.toString();
            this.timelimitFactor.setText(factorStr);
        }

        if (data.student.extensionDays == null) {
            this.extensionDays.setText(CoreConstants.EMPTY);
        } else {
            final String daysStr = data.student.extensionDays.toString();
            this.extensionDays.setText(daysStr);
        }
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
            try {
                final RawStudent stu = RawStudentLogic.query(this.cache, stuId, false);

                if (stu == null) {
                    JOptionPane.showMessageDialog(this, "Student not found", TITLE, JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        final String factorStr = this.timelimitFactor.getText();
                        final Float newFactor = factorStr == null ? null : Float.valueOf(factorStr);
                        try {
                            final String daysStr = this.extensionDays.getText();
                            final Integer newDays = daysStr == null ? null : Integer.valueOf(daysStr);

                            boolean ok = true;

                            if (!Objects.equals(newFactor, stu.timelimitFactor)) {
                                try {
                                    RawStudentLogic.updateTimeLimitFactor(this.cache, stuId, newFactor);
                                } catch (final SQLException ex) {
                                    final String[] msg = {"Failed to update time limit extension", ex.getMessage()};
                                    Log.warning(ex);
                                    JOptionPane.showMessageDialog(this, msg, TITLE, JOptionPane.ERROR_MESSAGE);
                                    ok = false;
                                }
                            }

                            if (!Objects.equals(newDays, stu.extensionDays)) {
                                try {
                                    RawStudentLogic.updateExtensionDays(this.cache, stuId, newDays);
                                } catch (final SQLException ex) {
                                    final String[] msg = {"Failed to update number of extension days", ex.getMessage()};
                                    Log.warning(ex);
                                    JOptionPane.showMessageDialog(this, msg, TITLE, JOptionPane.ERROR_MESSAGE);
                                    ok = false;
                                }
                            }

                            if (ok) {
                                this.owner.updateStudent();
                                setVisible(false);
                            }
                        } catch (final NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Invalid number of extension days.", TITLE,
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (final NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid time limit factor.", TITLE,
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (final SQLException ex) {
                final String[] msg = {"Failed to query student record", ex.getMessage()};
                Log.warning(ex);
                JOptionPane.showMessageDialog(this, msg, TITLE, JOptionPane.ERROR_MESSAGE);
            }
        } else if (CANCEL_CMD.equals(cmd)) {
            setVisible(false);
        }
    }
}

