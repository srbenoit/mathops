package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the collection of courses taken by a student in past terms.
 */
final class JTableCourseHistory extends AbstractAdmTable<RawStcourse> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8616039849185390240L;

    /**
     * Constructs a new {@code JTableCourseHistory}.
     */
    JTableCourseHistory() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(8);

        result.add(new LocalTableColumn("Course", 60));
        result.add(new LocalTableColumn("Section", 60));
        result.add(new LocalTableColumn("Term", 45));
        result.add(new LocalTableColumn("Year", 45));
        result.add(new LocalTableColumn("Grade", 60));
        result.add(new LocalTableColumn("Format", 60));

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
            row[0] = valueToString(reg.course);
            row[1] = valueToString(reg.sect);
            row[2] = reg.termKey.termCode;
            row[3] = valueToString(reg.termKey.year);
            row[4] = valueToString(reg.courseGrade);
            row[5] = reg.instrnType == null ? CoreConstants.EMPTY : reg.instrnType;

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
