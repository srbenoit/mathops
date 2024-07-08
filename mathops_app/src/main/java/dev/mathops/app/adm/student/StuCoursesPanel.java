package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawStcourse;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
class StuCoursesPanel extends AdminPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4427919858395831972L;

    /** The database connection. */
    private final DbConnection conn;

    /** The course history table. */
    private final JTableCourseHistory historyTable;

    /** Scroll pane for the history table. */
    private final JScrollPane historyScroll;

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
    private final JSplitPane split;

    /** An error message. */
    private final JLabel error;

    /**
     * Constructs a new {@code AdminCoursePanel}.
     *
     * @param theConn          the database connection
     */
    StuCoursesPanel(final DbConnection theConn) {

        super();
        setBackground(Skin.LIGHTEST);

        this.conn = theConn;

        // Left side: history of past registrations

        final JPanel left = makeOffWhitePanel(new BorderLayout(5, 5));
        left.setBackground(Skin.LIGHTEST);

        add(left, BorderLayout.WEST);

        left.add(makeHeader("History", false), BorderLayout.NORTH);

        this.historyTable = new JTableCourseHistory();
        this.historyTable.setFillsViewportHeight(true);
        this.historyScroll = new JScrollPane(this.historyTable);
        left.add(this.historyScroll, BorderLayout.CENTER);

        final Dimension histPref = this.historyTable.getPreferredSize();
        this.historyScroll.setPreferredSize(new Dimension(histPref.width, histPref.height + 30));

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

        this.split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitTop, splitBottom);
        this.split.setDividerLocation(0.5);
        add(this.split, BorderLayout.CENTER);

        //
        //
        //

        final Dimension curPref = this.currentTable.getPreferredSize();
        this.currentScroll.setPreferredSize(new Dimension(curPref.width, curPref.height + 30));

        final Dimension droppedPref = this.droppedTable.getPreferredSize();
        this.droppedScroll
                .setPreferredSize(new Dimension(droppedPref.width, droppedPref.height + 30));

        this.error = makeError();
        add(this.error, BorderLayout.SOUTH);
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
            this.currentScroll.setPreferredSize(this.currentTable.getPreferredScrollSize(this.currentScroll, 3));
            this.droppedScroll.setPreferredSize(this.currentTable.getPreferredScrollSize(this.droppedScroll, 3));
        }
    }

    /**
     * Clears all displayed fields.
     */
    private void clearDisplay() {

        this.historyTable.clear();
        this.currentTable.clear();

        this.currentHeader.setText("Current Courses");
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        TermKey active = null;

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
            this.droppedTable.addData(dropped, 2);

            this.split.setDividerLocation(0.5);
        }
    }
}
