package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.rawrecord.RawStchallenge;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the all challenge attempts on the student's record.
 */
final class JTableChallengeAttempts extends AbstractAdmTable<RawStchallenge> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 8608223729673451480L;

    /**
     * Constructs a new {@code JTableChallengeAttempts}.
     */
    JTableChallengeAttempts() {

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
        result.add(new LocalTableColumn("Version", 65));
        result.add(new LocalTableColumn("Started", 165));
        result.add(new LocalTableColumn("Finished", 165));
        result.add(new LocalTableColumn("Serial", 90));
        result.add(new LocalTableColumn("Score", 55));
        result.add(new LocalTableColumn("Passed", 65));

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
    protected int installData(final List<? extends RawStchallenge> data, final int minRows) {

        final String[] row = new String[7];

        for (final RawStchallenge record : data) {

            final LocalDateTime start;
            final LocalDateTime finish;

            if (record.startTime == null || record.examDt == null) {
                start = null;
            } else {
                final int startMin = record.startTime.intValue();
                start = LocalDateTime.of(record.examDt, LocalTime.of(startMin / 60, startMin % 60));
            }

            if (record.finishTime == null || record.examDt == null) {
                finish = null;
            } else {
                final int finishMin = record.finishTime.intValue();
                finish = LocalDateTime.of(record.examDt, LocalTime.of(finishMin / 60, finishMin % 60));
            }

            row[0] = valueToString(record.course);
            row[1] = valueToString(record.version);
            row[2] = start == null ? CoreConstants.EMPTY : FMT_MDY_HM.format(start);
            row[3] = finish == null ? CoreConstants.EMPTY : FMT_MDY_HM.format(finish);
            row[4] = valueToString(record.serialNbr);
            row[5] = valueToString(record.score);
            row[6] = valueToString(record.passed);

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
