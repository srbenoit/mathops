package dev.mathops.app.adm.student;

import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawHoldType;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rawrecord.RawUserClearance;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A card within the "Holds" tab of the admin app that displays the list of all holds on a student's record.
 */
/* default */ class HoldsCard extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 8712466750626845976L;

    /** An action command. */
    private static final String DELETE_CMD = "DELETE";

    /** The data cache. */
    private final Cache cache;

    /** The fixed data. */
    private final FixedData fixed;

    /** The current student data. */
    private StudentData data;

    /** The logged-in user's permission level. */
    private final int permissionLevel;

    /** The panel that contains the dynamically generated list of holds. */
    private final JPanel holdList;

    /**
     * Constructs a new {@code HoldsCard}.
     *
     * @param theCache    the data cache
     * @param theFixed    the fixed data
     * @param theListener the listener to notify when the "Add" button is pressed
     */
    /* default */ HoldsCard(final Cache theCache, final FixedData theFixed,
                            final ActionListener theListener) {

        super(new BorderLayout(10, 10));

        setBackground(Skin.WHITE);

        this.cache = theCache;
        this.fixed = theFixed;

        int perm = 5;
        for (final RawUserClearance p : theFixed.userPermissions) {
            if ("ADHOLD".equals(p.clearFunction)) {
                perm = p.clearType.intValue();
                break;
            }
        }
        this.permissionLevel = perm;

        this.holdList = new JPanel(new BorderLayout(10, 10));
        this.holdList.setBackground(Skin.WHITE);
        this.holdList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(this.holdList, BorderLayout.CENTER);

        // Permission levels 1 and 2 can add any holds, level 3 can add those marked "addable"
        if (this.permissionLevel < 4) {
            final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
            buttons.setBackground(Skin.WHITE);
            final JButton addButton = new JButton("Add Hold");
            addButton.setActionCommand(StudentDisciplinePanel.ADD_CMD);
            addButton.addActionListener(theListener);
            buttons.add(addButton);
            add(buttons, BorderLayout.SOUTH);
        }
    }

    /**
     * Clears the display.
     */
    /* default */ void clear() {

        this.holdList.removeAll();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param theData the student data
     */
    /* default */ void populateDisplay(final StudentData theData) {

        this.data = theData;

        this.holdList.removeAll();

        if (theData != null) {
            try {
                final List<RawAdminHold> holds = theData.getHolds();

                if (holds.isEmpty()) {
                    final JLabel noHoldsLbl = new JLabel("(No holds on record)");
                    this.holdList.add(noHoldsLbl, BorderLayout.NORTH);
                } else {
                    JPanel outer = new JPanel(new BorderLayout(5, 5));
                    outer.setBackground(Skin.LIGHT);
                    outer.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

                    final JScrollPane scroll = new JScrollPane(outer);

                    this.holdList.add(scroll, BorderLayout.CENTER);

                    scroll.getVerticalScrollBar().setUnitIncrement(6);

                    for (final RawAdminHold record : holds) {
                        final String msgStudent = RawAdminHoldLogic.getStudentMessage(record.holdId);
                        final String msgAdmin = RawAdminHoldLogic.getStaffMessage(record.holdId);

                        final JPanel pane = createHoldPanel(record, msgStudent, msgAdmin);
                        outer.add(pane, BorderLayout.NORTH);
                        final JPanel inner = new JPanel(new BorderLayout(5, 5));
                        inner.setBackground(Skin.LIGHT);
                        outer.add(inner, BorderLayout.CENTER);
                        outer = inner;
                    }
                }
            } catch (final SQLException ex) {
                final JLabel noHoldsLbl = new JLabel("(Unable to query for holds)");
                this.holdList.add(noHoldsLbl, BorderLayout.NORTH);
            }
        }

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Creates a panel to present a record.
     *
     * @param record     the record
     * @param msgStudent the student message
     * @param msgAdmin   the admin message
     * @return the panel
     */
    private JPanel createHoldPanel(final RawAdminHold record, final String msgStudent, final String msgAdmin) {

        final JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JLabel[] lbls = new JLabel[5];
        lbls[0] = new JLabel("Hold:");
        lbls[1] = new JLabel("Severity:");
        lbls[2] = new JLabel("Description:");
        lbls[3] = new JLabel("Student Message:");
        lbls[4] = new JLabel("When Applied:");
        int maxw = 0;
        int maxh = 0;
        for (final JLabel lbl : lbls) {
            lbl.setForeground(Skin.LABEL_COLOR);
            lbl.setFont(Skin.BOLD_12_FONT);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension dim = lbl.getPreferredSize();
            maxw = Math.max(maxw, dim.width);
            maxh = Math.max(maxh, dim.height);
        }
        final Dimension dim = new Dimension(maxw, maxh);
        for (final JLabel lbl : lbls) {
            lbl.setPreferredSize(dim);
        }

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow1.setBackground(Skin.WHITE);
        flow1.add(lbls[0]);
        final JLabel holdId = new JLabel(record.holdId == null ? CoreConstants.SPC : record.holdId);
        holdId.setFont(Skin.BODY_12_FONT);
        flow1.add(holdId);
        panel.add(flow1, BorderLayout.NORTH);

        final JPanel inner1 = new JPanel(new BorderLayout());
        inner1.setBackground(Skin.WHITE);
        panel.add(inner1, BorderLayout.CENTER);

        final JPanel inner2 = new JPanel(new BorderLayout());
        inner2.setBackground(Skin.WHITE);
        inner1.add(inner2, BorderLayout.CENTER);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow2.setBackground(Skin.WHITE);
        flow2.add(lbls[1]);
        final JLabel sev = new JLabel(record.sevAdminHold == null ? CoreConstants.SPC : record.sevAdminHold);
        sev.setFont(Skin.BODY_12_FONT);
        flow2.add(sev);
        inner1.add(flow2, BorderLayout.NORTH);

        String msg1 = msgAdmin;
        if (msg1 != null && msg1.length() > 100) {
            int pos = 100;
            while (pos > 1 && msg1.charAt(pos - 1) != ' ') {
                --pos;
            }

            msg1 = msg1.substring(0, pos) + "<br>" + msg1.substring(pos);
        }

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow3.setBackground(Skin.WHITE);
        flow3.add(lbls[2]);
        final JLabel admMsg = new JLabel(msg1 == null ? CoreConstants.SPC : "<html>" + msg1);
        admMsg.setFont(Skin.BODY_12_FONT);
        flow3.add(admMsg);
        inner2.add(flow3, BorderLayout.NORTH);

        String msg2 = msgStudent;
        if (msg2 != null && msg2.length() > 100) {
            int pos = 100;
            while (pos > 1 && msg2.charAt(pos - 1) != ' ') {
                --pos;
            }

            msg2 = msg2.substring(0, pos) + "<br>" + msg2.substring(pos);
        }

        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow4.setBackground(Skin.WHITE);
        flow4.add(lbls[3]);
        final JLabel stuMsg = new JLabel(msg2 == null ? CoreConstants.SPC : "<html>" + msg2);
        stuMsg.setAlignmentY(0.0f);
        stuMsg.setFont(Skin.BODY_12_FONT);
        flow4.add(stuMsg);
        inner2.add(flow4, BorderLayout.CENTER);

        final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow5.setBackground(Skin.WHITE);
        flow5.add(lbls[4]);
        final JLabel date = new JLabel(record.createDt == null
                ? CoreConstants.SPC : TemporalUtils.FMT_MDY.format(record.createDt));
        date.setFont(Skin.BODY_12_FONT);
        flow5.add(date);
        inner2.add(flow5, BorderLayout.SOUTH);

        // Permission levels 1 and 2 can delete all holds, level 3 can delete those marked
        // "deletable"
        boolean canDelete = false;
        if (this.permissionLevel < 3) {
            canDelete = true;
        } else if (this.permissionLevel == 3) {
            final SystemData systemData = this.data.getSystemData();

            try {
                for (final RawHoldType test : systemData.getHoldTypes()) {
                    if (test.holdId.equals(record.holdId)) {
                        if ("Y".equals(test.deleteHold)) {
                            canDelete = true;
                        }
                        break;
                    }
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }

        if (canDelete) {
            final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 4));
            buttons.setBackground(Skin.WHITE);
            final JButton addButton = new JButton("Delete Hold");
            addButton.setActionCommand(DELETE_CMD + record.holdId);
            addButton.addActionListener(this);
            buttons.add(addButton);
            panel.add(buttons, BorderLayout.SOUTH);
        }

        return panel;
    }

    /**
     * Called when a "Delete Hold" button is pressed within a hold panel. The action command will be "DELETE" plus the
     * hold ID.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (cmd.startsWith(DELETE_CMD) && this.data != null) {
            final String holdId = cmd.substring(DELETE_CMD.length());

            try (final PreparedStatement ps = this.cache.conn.prepareStatement(
                    "DELETE FROM admin_hold WHERE stu_id=? AND hold_id=?")) {

                final String studentId = this.data.getStudentId();
                ps.setString(1, studentId);
                ps.setString(2, holdId);

                final int numRows = ps.executeUpdate();
                if (numRows == 1) {
                    this.cache.conn.commit();

                    this.data.forgetHolds();
                    final List<RawAdminHold> holds = this.data.getHolds();

                    final Iterator<RawAdminHold> iter = holds.iterator();
                    String sev = null;
                    while (iter.hasNext()) {
                        final RawAdminHold row = iter.next();
                        if (row.holdId.equals(holdId)) {
                            iter.remove();
                        } else if ("F".equals(row.sevAdminHold)) {
                            sev = "F";
                        } else if ("N".equals(row.sevAdminHold) && sev == null) {
                            sev = "N";
                        }
                    }

                    final RawStudent student = this.data.getStudentRecord();
                    if (!Objects.equals(sev, student.sevAdminHold)) {
                        updateStudentHoldSeverity(sev);
                    }

                    clear();
                    populateDisplay(this.data);
                } else {
                    Log.warning("Failed to delete hold");
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to delete hold", ex);
            }
        }
    }

    /**
     * Updates the sev_admin_hold field of the "student" record.
     *
     * @param severity the new severity
     */
    private void updateStudentHoldSeverity(final String severity) {

        try (final PreparedStatement ps = this.cache.conn
                .prepareStatement("UPDATE student SET sev_admin_hold=? WHERE stu_id=?")) {

            final String studentId = this.data.getStudentId();

            ps.setString(1, severity);
            ps.setString(2, studentId);
            this.data.forgetStudentRecord();

            final int numRows = ps.executeUpdate();
            if (numRows == 1) {
                this.cache.conn.commit();
            } else {
                Log.warning("Failed to update 'sev_admin_hold' in student record");
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to update 'sev_admin_hold' in student record", ex);
        }
    }
}
