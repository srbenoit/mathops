package dev.mathops.app.adm.office.registration;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.rec.RawStcourse;
import dev.mathops.db.schema.main.rec.TermRec;
import dev.mathops.db.field.TermKey;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * A panel that shows the student's history of course registrations.
 */
public final class CourseHistoryPanel extends AdmPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4427919858395831972L;

    /** The data cache. */
    private final Cache cache;

    /** The course history table. */
    private final JTableCourseHistory historyTable;

    /** Scroll pane for the history table. */
    private final JScrollPane historyScroll;

    /** An error message. */
    private final JLabel error;

    /**
     * Constructs a new {@code CourseHistoryPanel}.
     *
     * @param theCache the data cache
     */
    public CourseHistoryPanel(final Cache theCache) {

        super();
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;

        // Center side: history of past registrations

        final JPanel historyBlock = makeOffWhitePanel(new BorderLayout(5, 5));
        historyBlock.setBackground(Skin.LIGHTEST);

        historyBlock.add(makeHeader("Registration History", false), BorderLayout.NORTH);

        this.historyTable = new JTableCourseHistory();
        this.historyTable.setFillsViewportHeight(true);
        this.historyScroll = new JScrollPane(this.historyTable);
        historyBlock.add(this.historyScroll, BorderLayout.CENTER);

        final Dimension histPref = this.historyTable.getPreferredSize();
        this.historyScroll.setPreferredSize(new Dimension(histPref.width, histPref.height + 30));

        add(this.historyScroll, StackedBorderLayout.CENTER);

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
        }
    }

    /**
     * Clears all displayed fields.
     */
    public void clearDisplay() {

        this.historyTable.clear();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        TermKey active = null;

        try {
            final TermRec activeTerm = this.cache.getSystemData().getActiveTerm();
            active = activeTerm.term;
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            this.error.setText("Unable to query for active term: " + exMsg);
        }

        if (active == null) {
            this.error.setText("No active term found.");
        } else {
            final List<RawStcourse> regs = data.studentCoursesPastAndCurrent;

            Collections.sort(regs);

            this.historyTable.clear();
            this.historyTable.addData(regs, 2);
        }
    }
}