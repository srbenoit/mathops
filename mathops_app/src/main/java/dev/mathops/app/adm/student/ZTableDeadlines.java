package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractZTable;
import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawStmilestone;

import java.io.Serial;
import java.util.List;

/**
 * A table to present the student's exam deadlines for the term.
 */
class ZTableDeadlines extends AbstractZTable<DeadlineListRow> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 9039219357149831901L;

    /** An action command. */
    public static final String CMD_APPEAL = "APPEAL";

    /** An action command. */
    public static final String CMD_EDIT = "EDIT";

    /** True to allow editing. */
    private final boolean allowEdit;

    /**
     * Constructs a new {@code ZTableDeadlines}.
     *
     * @param theListener   the listener that will be notified when a button is pressed in a row
     * @param isEditAllowed true if editing is allowed
     */
    ZTableDeadlines(final IZTableCommandListener<DeadlineListRow> theListener, final boolean isEditAllowed) {

        super(theListener);

        this.allowEdit = isEditAllowed;
    }

    /**
     * Adds the column headings to the table.
     */
    @Override
    protected void addColumnHeadings() {

        addHeaderCell("Order", 0, 0, 9);
        addHeaderCell("Course", 1, 0, 9);
        addHeaderCell("Unit", 2, 0, 9);
        addHeaderCell("Type", 3, 0, 9);
        addHeaderCell("Deadline", 4, 0, 9);
        addHeaderCell("Override", 5, 0, 9);
        addHeaderCell("Type", 6, 0, 9);
        addHeaderCell("Completed", 7, 0, 9);
        addHeaderCell("On Time?", 8, 0, 9);
    }

    /**
     * Sets table data from a list of records. This method should call {@code storeCurrentData} if any records will
     * include buttons.
     *
     * @param data the data
     */
    @Override
    public void setData(final List<DeadlineListRow> data) {

        clear();
        storeCurrentData(data);

        int yy = 1;

        for (final DeadlineListRow row : data) {
            final RawMilestone ms = row.milestoneRecord;
            final List<RawStmilestone> stmsList = row.stmilestoneRecords;

            addCell(Integer.toString(ms.getIndex()), 0, yy);
            addCell(row.course, 1, yy);
            addCell(Integer.toString(ms.getUnit()), 2, yy);
            addCell(ms.msType, 3, yy);
            addCell(FMT_WMD.format(ms.msDate), 4, yy);

            int numStms = stmsList.size();
            if (numStms == 0) {
                addCell(CoreConstants.SPC, 5, yy);
                addCell(CoreConstants.SPC, 6, yy);
            } else if (numStms == 1) {
                final RawStmilestone stms = stmsList.getFirst();
                final String overrideDate = FMT_WMD.format(stms.msDate);
                addCell(overrideDate, 5, yy);
                addCell(stms.extType, 6, yy);
            } else {
                final StringBuilder dates = new StringBuilder(100);
                final StringBuilder types = new StringBuilder(100);

                boolean comma = false;
                for (final RawStmilestone stms : stmsList) {
                    if (comma) {
                        dates.append(CoreConstants.CRLF);
                        types.append(CoreConstants.CRLF);
                    }
                    final String overrideDate = FMT_WMD.format(stms.msDate);
                    dates.append(overrideDate);
                    types.append(stms.extType);
                    comma = true;
                }
            }

            String completedStr = CoreConstants.SPC;
            if (row.whenCompleted != null) {
                completedStr = FMT_WMD.format(row.whenCompleted);
            }
            addCell(completedStr, 7, yy);

            String onTimeStr = CoreConstants.SPC;
            if (row.onTime != null) {
                onTimeStr = "Yes";
            }
            addCell(onTimeStr, 8, yy);

            if (this.allowEdit) {
                if (stmsList.isEmpty()) {
                    addButtonCell("Appeal", 9, yy, CMD_APPEAL);
                } else {
                    addButtonCell("Edit", 9, yy, CMD_EDIT);
                }
            }

            ++yy;
        }

        final int numColumns = this.allowEdit ? 9 : 8;
        addLastRow(yy, numColumns);
        addLastCol(numColumns, yy + 1);
    }
}
