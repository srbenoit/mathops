package dev.mathops.app.adm.office.student;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.schema.legacy.rec.RawSpecialStus;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present all special categories to which a student belongs.
 */
final class JTableSpecialCategories extends AbstractAdmTable<RawSpecialStus> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2729866737451657580L;

    /**
     * Constructs a new {@code JTableSpecialCategories}.
     */
    JTableSpecialCategories() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(4);

        result.add(new LocalTableColumn("Category", 70));
        result.add(new LocalTableColumn("Start", 55));
        result.add(new LocalTableColumn("End", 55));

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
    protected int installData(final List<? extends RawSpecialStus> data, final int minRows) {

        final String[] row = new String[3];

        for (final RawSpecialStus record : data) {
            row[0] = record.stuType;
            row[1] = record.startDt == null ? CoreConstants.EMPTY : FMT_MDY.format(record.startDt);
            row[2] = record.endDt == null ? CoreConstants.EMPTY : FMT_MDY.format(record.endDt);

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
