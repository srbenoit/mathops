package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractAdminTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.session.sitelogic.mathplan.MathPlanLogic;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present the all placement attempts on the student's record.
 */
final class JTableMathPlanResponses extends AbstractAdminTable<RawStmathplan> {

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
    private static List<AdminTableColumn> makeColumns() {

        final List<AdminTableColumn> result = new ArrayList<>(8);

        result.add(new AdminTableColumn("Version", 65));
        result.add(new AdminTableColumn("Date", 95));
        result.add(new AdminTableColumn("Time", 85));
        result.add(new AdminTableColumn("Phase", 55));
        result.add(new AdminTableColumn("Description", 260));
        result.add(new AdminTableColumn("Question", 75));
        result.add(new AdminTableColumn("Answer", 65));

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

            if (MathPlanLogic.MAJORS_PROFILE.equals(record.version)) {
                row[4] = "Majors of interest";
            } else if (MathPlanLogic.PLAN_PROFILE.equals(record.version)) {
                row[4] = "Recommendations";
            } else if (MathPlanLogic.ONLY_RECOM_PROFILE.equals(record.version)) {
                row[4] = "Affirm 'only a recommendation'";
            } else if (MathPlanLogic.EXISTING_PROFILE.equals(record.version)) {
                row[4] = "Existing work";
            } else if (MathPlanLogic.INTENTIONS_PROFILE.equals(record.version)) {
                row[4] = "Indicated intentions";
            } else if (MathPlanLogic.REVIEWED_PROFILE.equals(record.version)) {
                row[4] = "Plan reviewed";
            } else if (MathPlanLogic.CHECKED_RESULTS_PROFILE.equals(record.version)) {
                row[4] = "Placement results checked";
            } else {
                row[4] = "(unrecognized)";
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
