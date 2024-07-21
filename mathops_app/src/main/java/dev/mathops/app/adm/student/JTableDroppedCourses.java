package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.old.rawrecord.RawStcourse;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the collection of courses that have been dropped by a student this term.
 */
final class JTableDroppedCourses extends AbstractAdmTable<RawStcourse> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7737399614498944414L;

    /**
     * Constructs a new {@code JTableDroppedCourses}.
     *
     */
    JTableDroppedCourses() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(8);

        result.add(new LocalTableColumn("Course", 55));
        result.add(new LocalTableColumn("Section", 55));
        result.add(new LocalTableColumn("Format", 55));
        result.add(new LocalTableColumn("Prereq", 50));
        result.add(new LocalTableColumn("Open", 45));
        result.add(new LocalTableColumn("Last Roll Date", 120));

        return result;
    }

    /**
     * Installs data in the table.
     *
     * @param data    the data to install
     * @param minRows the minimum number of rows to show in the table
     * @return the number of records in the table after the insert
     */
    @Override
    protected int installData(final List<? extends RawStcourse> data, final int minRows) {

        final String[] row = new String[6];

        for (final RawStcourse reg : data) {
            final String instr = reg.instrnType;
            final String prereq = reg.prereqSatis;
            final String open = reg.openStatus;

            row[0] = valueToString(reg.course);
            row[1] = valueToString(reg.sect);
            row[2] = instr == null ? CoreConstants.EMPTY : instr;
            row[3] = prereq == null ? CoreConstants.EMPTY : prereq;
            row[4] = open == null ? CoreConstants.EMPTY : open;
            row[5] = reg.lastClassRollDt == null ? "N/A"
                    : TemporalUtils.FMT_MDY.format(reg.lastClassRollDt);

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
