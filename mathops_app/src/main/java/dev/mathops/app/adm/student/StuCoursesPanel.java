package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawStcourse;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A panel that shows the courses in which a student is enrolled.
 */
final class StuCoursesPanel extends AdmPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4427919858395831972L;

    /** An action command. */
    private static final String ADD_TRANSFER_CMD = "ADD_TRANSFER";

    /** The database connection. */
    private final DbConnection conn;

    /** The course history table. */
    private final JTableCourseHistory historyTable;

    /** Scroll pane for the history table. */
    private final JScrollPane historyScroll;

    /** A table that shows all transfer credit. */
    private final JTableTransferCredit transferTable;

    /** Scroll pane for transfer credit results. */
    private final JScrollPane transferScroll;

    /** The current course table. */
    private final JTableCurrentCourses currentTable;

    /** Scroll pane for the current course table. */
    private final JScrollPane currentScroll;

    /** The header for the current course list. */
    private final JLabel currentHeader;

    /** The dropped course table. */
    private final JTableDroppedCourses droppedTable;

    /** Scroll pane for the dropped course table. */
    private final JScrollPane droppedScroll;

    /** Split pane between current and dropped. */
    private final JSplitPane coursesSplit;

    /** Split pane between history and transfer. */
    private final JSplitPane transferSplit;

    /** Button to add transfer credit. */
    private final JButton addTransfer;

    /** An error message. */
    private final JLabel error;

    /** Data on the current student. */
    private StudentData currentStudentData = null;

    /** The dialog to add a transfer credit record. */
    private DlgAddTransfer addTransferDialog = null;

    /**
     * Constructs a new {@code AdminCoursePanel}.
     *
     * @param theConn the database connection
     */
    StuCoursesPanel(final DbConnection theConn) {

        super();
        setBackground(Skin.LIGHTEST);

        this.conn = theConn;

        // Left side: history of past registrations

        final JPanel historyBlock = makeOffWhitePanel(new BorderLayout(5, 5));
        historyBlock.setBackground(Skin.LIGHTEST);

        historyBlock.add(makeHeader("History", false), BorderLayout.NORTH);

        this.historyTable = new JTableCourseHistory();
        this.historyTable.setFillsViewportHeight(true);
        this.historyScroll = new JScrollPane(this.historyTable);
        historyBlock.add(this.historyScroll, BorderLayout.CENTER);

        final Dimension histPref = this.historyTable.getPreferredSize();
        this.historyScroll.setPreferredSize(new Dimension(histPref.width, histPref.height + 30));

        final JPanel transferBlock = makeOffWhitePanel(new BorderLayout(5, 5));
        transferBlock.setBackground(Skin.LIGHTEST);

        transferBlock.add(makeHeader("Transfer Credit", true), BorderLayout.NORTH);

        this.transferTable = new JTableTransferCredit();
        this.transferTable.setFillsViewportHeight(true);

        this.transferScroll = new JScrollPane(this.transferTable);
        this.transferScroll.setPreferredSize(this.transferTable.getPreferredScrollSize(this.transferScroll, 3));
        transferBlock.add(this.transferScroll, BorderLayout.CENTER);

        final JPanel transferButtonBar = makeOffWhitePanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        transferButtonBar.setBackground(Skin.LIGHTEST);
        this.addTransfer = new JButton("Add...");
        this.addTransfer.setActionCommand(ADD_TRANSFER_CMD);
        this.addTransfer.addActionListener(this);
        this.addTransfer.setEnabled(false);
        transferButtonBar.add(this.addTransfer);
        transferBlock.add(transferButtonBar, BorderLayout.PAGE_END);

        this.transferSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, historyBlock, transferBlock);
        this.transferSplit.setBackground(Skin.LIGHTEST);
        add(this.transferSplit, StackedBorderLayout.WEST);

        // Center: current registrations

        final JPanel splitTop = makeOffWhitePanel(new BorderLayout(5, 5));
        splitTop.setBackground(Skin.LIGHTEST);

        final JPanel splitBottom = makeOffWhitePanel(new BorderLayout(5, 5));
        splitBottom.setBackground(Skin.LIGHTEST);

        this.currentHeader = makeHeader("Current Courses", false);
        splitTop.add(this.currentHeader, BorderLayout.NORTH);

        this.currentTable = new JTableCurrentCourses();
        this.currentTable.setFillsViewportHeight(true);
        this.currentScroll = new JScrollPane(this.currentTable);
        splitTop.add(this.currentScroll, BorderLayout.CENTER);

        final JLabel droppedHeader = makeHeader("Courses Dropped/Forfeit this Term", false);
        splitBottom.add(droppedHeader, BorderLayout.NORTH);

        this.droppedTable = new JTableDroppedCourses();
        this.droppedTable.setFillsViewportHeight(true);
        this.droppedScroll = new JScrollPane(this.droppedTable);
        splitBottom.add(this.droppedScroll, BorderLayout.CENTER);

        this.coursesSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitTop, splitBottom);
        add(this.coursesSplit, StackedBorderLayout.CENTER);

        //
        //
        //

        final Dimension curPref = this.currentTable.getPreferredSize();
        this.currentScroll.setPreferredSize(new Dimension(curPref.width, curPref.height + 30));

        final Dimension droppedPref = this.droppedTable.getPreferredSize();
        this.droppedScroll
                .setPreferredSize(new Dimension(droppedPref.width, droppedPref.height + 30));

        this.error = makeError();
        add(this.error, StackedBorderLayout.SOUTH);
    }

    /**
     * Sets the selected student data.
     *
     * @param data the selected student data
     */
    public void setSelectedStudent(final StudentData data) {

        this.error.setText(CoreConstants.SPC);
        clearDisplay();

        if (data != null) {
            populateDisplay(data);

            this.historyScroll.setPreferredSize(this.historyTable.getPreferredScrollSize(this.historyScroll, 3));
            this.transferScroll.setPreferredSize(this.transferTable.getPreferredScrollSize(this.transferScroll, 3));
            this.currentScroll.setPreferredSize(this.currentTable.getPreferredScrollSize(this.currentScroll, 3));
            this.droppedScroll.setPreferredSize(this.currentTable.getPreferredScrollSize(this.droppedScroll, 3));
        }
    }

    /**
     * Clears all displayed fields.
     */
    void clearDisplay() {

        this.historyTable.clear();
        this.transferTable.clear();
        this.currentTable.clear();

        this.currentHeader.setText("Current Courses");
        this.addTransfer.setEnabled(false);

        if (this.addTransferDialog != null) {
            this.addTransferDialog.setVisible(false);
            this.addTransferDialog.dispose();
            this.addTransferDialog = null;
        }
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        TermKey active = null;

        this.currentStudentData = data;

        try (final Statement s = this.conn.createStatement()) {
            try (final ResultSet rs = s.executeQuery(//
                    "SELECT term, term_yr FROM term WHERE active='Y'")) {
                if (rs.next()) {
                    final ETermName name = ETermName.forName(rs.getString(1));
                    final int y = rs.getInt(2);

                    if (name != null) {
                        active = new TermKey(name, y <= 80 ? y + 2000 : y + 1900);
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            this.error.setText("Unable to query for active term: " + ex.getMessage());
        }

        if (active == null) {
            this.error.setText("No active term found.");
        } else {
            this.currentHeader.setText(active.longString + " Courses");

            final List<RawStcourse> regs = data.studentCoursesPastAndCurrent;

            final List<RawStcourse> currentTerm = new ArrayList<>(10);
            final List<RawStcourse> priorTerms = new ArrayList<>(10);
            final List<RawStcourse> dropped = new ArrayList<>(10);

            for (final RawStcourse reg : regs) {
                if (active.equals(reg.termKey)) {
                    if ("D".equals(reg.openStatus)
                            || "G".equals(reg.openStatus)) {
                        dropped.add(reg);
                    } else if ("N".equals(reg.openStatus)
                            && "N".equals(reg.completed)) {
                        dropped.add(reg);
                    } else {
                        currentTerm.add(reg);
                    }
                } else if (!"D".equals(reg.openStatus)) {
                    priorTerms.add(reg);
                }
            }

            Collections.sort(currentTerm);
            Collections.sort(priorTerms);
            Collections.sort(dropped);

            this.currentTable.addData(currentTerm, 2);
            this.historyTable.addData(priorTerms, 2);
            this.transferTable.addData(data.studentTransferCredit, 2);
            this.droppedTable.addData(dropped, 2);

            this.coursesSplit.setDividerLocation(0.5);
            this.transferSplit.setDividerLocation(0.6);

            this.addTransfer.setEnabled(true);
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

        if (ADD_TRANSFER_CMD.equals(cmd)) {

            if (this.currentStudentData != null) {
                if (this.addTransferDialog == null) {
                    this.addTransferDialog = new DlgAddTransfer(this.conn, this);
                }

                this.addTransferDialog.populateDisplay(this.currentStudentData);
                this.addTransferDialog.setVisible(true);
                this.addTransferDialog.toFront();
            }
        }
    }
}

