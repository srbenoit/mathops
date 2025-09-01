package dev.mathops.app.adm.office.placement;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.schema.legacy.RawMpeCredit;
import dev.mathops.db.schema.RawRecordConstants;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the all earned placement results on a student's record
 */
final class JTableEarnedPlacement extends AbstractAdmTable<RawMpeCredit> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5880731691483229924L;

    /**
     * Constructs a new {@code JTablePlacementAttempts}.
     */
    JTableEarnedPlacement() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(8);

        result.add(new LocalTableColumn("Code", 80));
        result.add(new LocalTableColumn("Result", 220));
        result.add(new LocalTableColumn("Exam Date", 120));
        result.add(new LocalTableColumn("Version", 70));
        result.add(new LocalTableColumn("Serial", 90));

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
    protected int installData(final List<? extends RawMpeCredit> data, final int minRows) {

        final String[] row = new String[6];

        for (final RawMpeCredit record : data) {

            row[0] = record.course;

            if (RawRecordConstants.M100C.equals(record.course)) {
                if ("P".equals(record.examPlaced) || "C".equals(record.examPlaced)) {
                    row[1] = "Placed Into MATH 117 or MATH 120";
                } else {
                    row[1] = record.examPlaced;
                }
            } else {
                final String crs = valueToString(record.course.replace("M ", "MATH "));

                if ("P".equals(record.examPlaced)) {
                    row[1] = "Placed Out Of " + crs;
                } else if ("C".equals(record.examPlaced)) {
                    row[1] = "Earned Credit in " + crs;
                } else {
                    row[1] = record.examPlaced;
                }
            }

            row[2] = record.examDt == null ? CoreConstants.EMPTY : FMT_MDY.format(record.examDt);

            row[3] = valueToString(record.version);
            row[4] = valueToString(record.serialNbr);

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
