package dev.mathops.app.adm.office.placement;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.rawrecord.RawFfrTrns;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the all transfer credit on the student's record.
 */
final class JTableTransferCredit extends AbstractAdmTable<RawFfrTrns> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 9200950362323535992L;

    /**
     * Constructs a new {@code JTableTransferCredit}.
     */
    JTableTransferCredit() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(2);

        result.add(new LocalTableColumn("Course", 100));
        result.add(new LocalTableColumn("Date", 120));

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
    protected int installData(final List<? extends RawFfrTrns> data, final int minRows) {

        final String[] row = new String[2];

        for (final RawFfrTrns record : data) {
            row[0] = valueToString(record.course.replace("M ", "MATH "));
            row[1] = record.examDt == null ? CoreConstants.EMPTY : FMT_MDY.format(record.examDt);

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
