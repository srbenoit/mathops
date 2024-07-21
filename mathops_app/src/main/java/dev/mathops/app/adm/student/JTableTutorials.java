package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractAdmTable;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A table to present tutorial completions.
 */
final class JTableTutorials extends AbstractAdmTable<RawStexam> {

    /** A commonly used string. */
    private static final String TUTORIAL_EXAM = "Tutorial Exam";

    /** A commonly used string. */
    private static final String Y = "Y";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 8657729325578100959L;

    /**
     * Constructs a new {@code JTablePlacementAttempts}.
     */
    JTableTutorials() {

        super(makeColumns());
    }

    /**
     * Creates the list of the table columns.
     *
     * @return the list of columns.
     */
    private static List<LocalTableColumn> makeColumns() {

        final List<LocalTableColumn> result = new ArrayList<>(5);

        result.add(new LocalTableColumn("Tutorial", 80));
        result.add(new LocalTableColumn("Exam", 70));
        result.add(new LocalTableColumn("When Passed", 160));
        result.add(new LocalTableColumn("Result", 300));

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
    protected int installData(final List<? extends RawStexam> data, final int minRows) {

        final String[] row = new String[4];

        // See if there is an ELM unit 3 review
        for (final RawStexam record : data) {
            if (RawRecordConstants.M100T.equals(record.course) && Integer.valueOf(3).equals(record.unit)
                    && Y.equals(record.passed) && "R".equals(record.examType)) {

                row[0] = "ELM";
                row[1] = "Unit 3 Review";
                row[2] = FMT_MDY.format(record.examDt);
                row[3] = valueToString("Eligible for MATH 116");

                getModel().addRow(row);
            }
        }

        // See if there is an ELM Exam
        for (final RawStexam record : data) {
            if (RawRecordConstants.M100T.equals(record.course) && Integer.valueOf(4).equals(record.unit)
                    && Y.equals(record.passed) && RawStexam.UNIT_EXAM.equals(record.examType)) {

                row[0] = "ELM";
                row[1] = "ELM Exam";
                row[2] = FMT_MDY.format(record.examDt);
                row[3] = valueToString("Placed into MATH 117");

                getModel().addRow(row);
            }
        }

        // See if there is a M 1170 final
        for (final RawStexam record : data) {
            if (RawRecordConstants.M1170.equals(record.course) && Integer.valueOf(4).equals(record.unit)
                    && Y.equals(record.passed) && RawStexam.UNIT_EXAM.equals(record.examType)) {

                row[0] = "Precalc (117)";
                row[1] = TUTORIAL_EXAM;
                row[2] = FMT_MDY.format(record.examDt);
                row[3] = valueToString("Placed out of MATH 117");

                getModel().addRow(row);
            }
        }

        // See if there is a M 1180 final
        for (final RawStexam record : data) {
            if (RawRecordConstants.M1180.equals(record.course) && Integer.valueOf(4).equals(record.unit)
                    && Y.equals(record.passed) && RawStexam.UNIT_EXAM.equals(record.examType)) {

                row[0] = "Precalc (118)";
                row[1] = TUTORIAL_EXAM;
                row[2] = FMT_MDY.format(record.examDt);
                row[3] = valueToString("Placed out of MATH 118");

                getModel().addRow(row);
            }
        }

        // See if there is a M 1240 final
        for (final RawStexam record : data) {
            if (RawRecordConstants.M1240.equals(record.course) && Integer.valueOf(4).equals(record.unit)
                    && Y.equals(record.passed) && RawStexam.UNIT_EXAM.equals(record.examType)) {

                row[0] = "Precalc (124)";
                row[1] = TUTORIAL_EXAM;
                row[2] = FMT_MDY.format(record.examDt);
                row[3] = valueToString("Placed out of MATH 124");

                getModel().addRow(row);
            }
        }

        // See if there is a M 1250 final
        for (final RawStexam record : data) {
            if (RawRecordConstants.M1250.equals(record.course) && Integer.valueOf(4).equals(record.unit)
                    && Y.equals(record.passed) && RawStexam.UNIT_EXAM.equals(record.examType)) {

                row[0] = "Precalc (125)";
                row[1] = TUTORIAL_EXAM;
                row[2] = FMT_MDY.format(record.examDt);
                row[3] = valueToString("Placed out of MATH 125");

                getModel().addRow(row);
            }
        }

        // See if there is a M 1260 final
        for (final RawStexam record : data) {
            if (RawRecordConstants.M1260.equals(record.course) && Integer.valueOf(4).equals(record.unit)
                    && Y.equals(record.passed) && RawStexam.UNIT_EXAM.equals(record.examType)) {

                row[0] = "Precalc (126)";
                row[1] = TUTORIAL_EXAM;
                row[2] = FMT_MDY.format(record.examDt);
                row[3] = valueToString("Placed out of MATH 126");

                getModel().addRow(row);
            }
        }

        Arrays.fill(row, CoreConstants.EMPTY);
        for (int i = getModel().getRowCount(); i < minRows; ++i) {
            getModel().addRow(row);
        }

        return getModel().getRowCount();
    }
}
