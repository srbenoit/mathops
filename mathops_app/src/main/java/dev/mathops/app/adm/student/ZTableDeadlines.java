package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractZTable;
import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.core.CoreConstants;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawStmilestone;

import java.io.Serial;
import java.util.List;

/**
 * A table to present the student's exam deadlines for the term.
 */
/* default */ class ZTableDeadlines extends AbstractZTable<DeadlineListRow> {

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
     * @param theListener      the listener that will be notified when a button is pressed in a row
     * @param isEditAllowed    true if editing is allowed
     */
    /* default */ ZTableDeadlines(final IZTableCommandListener<DeadlineListRow> theListener,
                                  final boolean isEditAllowed) {

        super(theListener);

        this.allowEdit = isEditAllowed;
    }

    /**
     * Adds the column headings to the table.
     */
    @Override
    protected void addColumnHeadings() {

        final int numColumns = this.allowEdit ? 9 : 8;

        addHeaderCell("Order", 0, 0, numColumns);
        addHeaderCell("Course", 1, 0, numColumns);
        addHeaderCell("Unit", 2, 0, numColumns);
        addHeaderCell("Type", 3, 0, numColumns);
        addHeaderCell("Deadline", 4, 0, numColumns);
        addHeaderCell("Override", 5, 0, numColumns);
        addHeaderCell("Completed", 6, 0, numColumns);
        addHeaderCell("On Time?", 7, 0, numColumns);
        if (this.allowEdit) {
            addHeaderCell(CoreConstants.SPC, 8, 0, numColumns);
        }
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
            final RawStmilestone stms = row.stmilestoneRecord;

            addCell(Integer.toString(ms.getIndex()), 0, yy);
            addCell(row.course, 1, yy);
            addCell(Integer.toString(ms.getUnit()), 2, yy);
            addCell(ms.msType, 3, yy);
            addCell(FMT_WMD.format(ms.msDate), 4, yy);

            String overrideDate = CoreConstants.SPC;
            if (stms != null) {
                overrideDate = FMT_WMD.format(stms.msDate);
            }
            addCell(overrideDate, 5, yy);

            String completedStr = CoreConstants.SPC;
            if (row.whenCompleted != null) {
                completedStr = FMT_WMD.format(row.whenCompleted);
            }
            addCell(completedStr, 6, yy);

            final String onTimeStr = CoreConstants.SPC;
            if (row.onTime != null) {
            }
            addCell(onTimeStr, 7, yy);

            if (this.allowEdit) {
                if (stms == null) {
                    addButtonCell("Appeal", 8, yy, CMD_APPEAL);
                } else {
                    addButtonCell("Edit", 8, yy, CMD_EDIT);
                }
            }

            ++yy;
        }

        final int numColumns = this.allowEdit ? 9 : 8;
        addLastRow(yy, numColumns);
        addLastCol(numColumns, yy + 1);
    }
}
