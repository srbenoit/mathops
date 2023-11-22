package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractAdminTable;
import dev.mathops.core.CoreConstants;
import dev.mathops.db.rawrecord.RawStmpe;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the all placement attempts on the student's record.
 */
final class JTablePlacementAttempts extends AbstractAdminTable<RawStmpe> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5880731691483229924L;

    /**
     * Constructs a new {@code JTablePlacementAttempts}.
     */
    JTablePlacementAttempts() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<AdminTableColumn> makeColumns() {

        final List<AdminTableColumn> result = new ArrayList<>(14);

        result.add(new AdminTableColumn("Version", 65));
        result.add(new AdminTableColumn("Proctored", 90));
        result.add(new AdminTableColumn("Started", 165));
        result.add(new AdminTableColumn("Finished", 165));
        result.add(new AdminTableColumn("Serial", 90));
        result.add(new AdminTableColumn("Placed", 60));
        result.add(new AdminTableColumn("A", 35));
        result.add(new AdminTableColumn("117", 45));
        result.add(new AdminTableColumn("118", 45));
        result.add(new AdminTableColumn("124", 45));
        result.add(new AdminTableColumn("125", 45));
        result.add(new AdminTableColumn("126", 45));

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
    protected int installData(final List<? extends RawStmpe> data, final int minRows) {

        final String[] row = new String[12];

        for (final RawStmpe record : data) {
            final boolean proctored = "PPPPP".equals(record.version)
                    || "MPTPU".equals(record.version)
                    || "MPTTC".equals(record.version)
                    || "MPTRW".equals(record.version)
                    || "MPTUT".equals(record.version)
                    || "MPTHL".equals(record.version)
                    || "MPTRM".equals(record.version);

            final LocalDateTime start = record.getStartDateTime();
            final LocalDateTime fin = record.getFinishDateTime();

            row[0] = valueToString(record.version);
            row[1] = proctored ? "Yes" : "No";
            row[2] = start == null ? CoreConstants.EMPTY : FMT_MDY_HM.format(start);
            row[3] = fin == null ? CoreConstants.EMPTY : FMT_MDY_HM.format(fin);
            row[4] = valueToString(record.serialNbr);
            if ("Y".equals(record.placed)) {
                row[5] = valueToString("Yes");
            } else if ("N".equals(record.placed)) {
                row[5] = valueToString("No");
            } else {
                row[5] = valueToString(record.placed);
            }
            row[6] = valueToString(record.stsA);
            row[7] = valueToString(record.sts117);
            row[8] = valueToString(record.sts118);
            row[9] = valueToString(record.sts124);
            row[10] = valueToString(record.sts125);
            row[11] = valueToString(record.sts126);

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
