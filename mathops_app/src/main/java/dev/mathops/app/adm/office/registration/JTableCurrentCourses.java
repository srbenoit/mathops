package dev.mathops.app.adm.office.registration;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.schema.legacy.RawStcourse;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the collection of courses being taken by a student in the active term.
 */
final class JTableCurrentCourses extends AbstractAdmTable<RawStcourse> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1863729721654106635L;

    /**
     * Constructs a new {@code JTableCurrentCourses}.
     */
    JTableCurrentCourses() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(10);

        result.add(new LocalTableColumn("Course", 60));
        result.add(new LocalTableColumn("Section", 60));
        result.add(new LocalTableColumn("Format", 60));
        result.add(new LocalTableColumn("Prereq", 60));
        result.add(new LocalTableColumn("Order", 50));
        result.add(new LocalTableColumn("Open", 50));
        result.add(new LocalTableColumn("Complete", 70));
        result.add(new LocalTableColumn("Score", 50));
        result.add(new LocalTableColumn("Incomplete", 140));

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

        final String[] row = new String[9];

        for (final RawStcourse reg : data) {
            final String instr = reg.instrnType;
            final String prereq = reg.prereqSatis;
            final String open = reg.openStatus;

            row[0] = valueToString(reg.course);
            row[1] = valueToString(reg.sect);
            row[2] = instr == null ? CoreConstants.EMPTY : instr;
            row[3] = prereq == null ? CoreConstants.EMPTY : prereq;
            row[4] = valueToString(reg.paceOrder);
            row[5] = open == null ? CoreConstants.EMPTY : open;
            row[6] = reg.completed;
            row[7] = valueToString(reg.score);

            if ("Y".equals(reg.iInProgress)) {
                if ("Y".equals(reg.iCounted)) {
                    row[8] = reg.iTermKey.shortString + ", In pace";
                } else {
                    row[8] = reg.iTermKey.shortString + ", Not in pace";
                }
            } else {
                row[8] = CoreConstants.EMPTY;
            }

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
