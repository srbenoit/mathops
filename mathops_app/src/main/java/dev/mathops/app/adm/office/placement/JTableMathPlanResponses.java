package dev.mathops.app.adm.office.placement;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.logic.mathplan.MathPlanConstants;
import dev.mathops.db.schema.legacy.rec.RawStmathplan;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the all placement attempts on the student's record.
 */
final class JTableMathPlanResponses extends AbstractAdmTable<RawStmathplan> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2263189380707791642L;

    /**
     * Constructs a new {@code JTableMathPlanResponses}.
     *
     */
    JTableMathPlanResponses() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(8);

        result.add(new LocalTableColumn("Version", 65));
        result.add(new LocalTableColumn("Date", 95));
        result.add(new LocalTableColumn("Time", 85));
        result.add(new LocalTableColumn("Phase", 55));
        result.add(new LocalTableColumn("Description", 260));
        result.add(new LocalTableColumn("Question", 75));
        result.add(new LocalTableColumn("Answer", 65));

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
    protected int installData(final List<? extends RawStmathplan> data, final int minRows) {

        final String[] row = new String[7];

        data.sort(null);

        for (final RawStmathplan record : data) {

            final LocalDateTime datetime = TemporalUtils.toLocalDateTime(record.examDt, record.finishTime);

            row[0] = valueToString(record.version);
            row[1] = FMT_MDY.format(datetime.toLocalDate());
            row[2] = FMT_HM.format(datetime.toLocalTime());
            row[3] = record.version;

            switch (record.version) {
                case MathPlanConstants.MAJORS_PROFILE -> row[4] = "Majors of interest";
                case MathPlanConstants.PLAN_PROFILE -> row[4] = "Recommendations";
                case MathPlanConstants.ONLY_RECOM_PROFILE -> row[4] = "Affirm 'only a recommendation'";
                case MathPlanConstants.EXISTING_PROFILE -> row[4] = "Existing work";
                case MathPlanConstants.INTENTIONS_PROFILE -> row[4] = "Indicated intentions";
                case MathPlanConstants.REVIEWED_PROFILE -> row[4] = "Plan reviewed";
                case MathPlanConstants.CHECKED_RESULTS_PROFILE -> row[4] = "Placement results checked";
                case null, default -> row[4] = "(unrecognized)";
            }

            row[5] = record.surveyNbr == null ? CoreConstants.EMPTY : record.surveyNbr.toString();
            row[6] = record.stuAnswer == null ? CoreConstants.EMPTY : record.stuAnswer;

            getModel().addRow(row);
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();

    }
}
