package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractZTable;
import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.core.CoreConstants;
import dev.mathops.db.old.rawrecord.RawStexam;

import java.io.Serial;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A table to present a student's exam record.
 */
/* default */ class ZTableExamList extends AbstractZTable<ExamListRow> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3581446692750965297L;

    /**
     * Constructs a new {@code ZTableExamRecord}.
     *
     * @param theListener      the listener that will be notified when a button is pressed in a row
     */
    /* default */ ZTableExamList(final IZTableCommandListener<ExamListRow> theListener) {

        super(theListener);
    }

    /**
     * Adds the column headings to the table.
     */
    @Override
    protected void addColumnHeadings() {

        addHeaderCell("Week", 0, 0, 13);
        addHeaderCell("Date", 1, 0, 13);
        addHeaderCell("Course", 2, 0, 13);
        addHeaderCell("Unit", 3, 0, 13);
        addHeaderCell("Type", 4, 0, 13);
        addHeaderCell("Version", 5, 0, 13);
        addHeaderCell("Serial", 6, 0, 13);
        addHeaderCell("Start", 7, 0, 13);
        addHeaderCell("End", 8, 0, 13);
        addHeaderCell("Duration", 9, 0, 13);
        addHeaderCell("Score", 10, 0, 13);
        addHeaderCell("Passed", 11, 0, 13);
        addHeaderCell(CoreConstants.EMPTY, 12, 0, 13);
    }

    /**
     * Sets table data from a list of records. This method should call {@code storeCurrentData} if any records will
     * include buttons.
     *
     * @param data the data
     */
    @Override
    public void setData(final List<ExamListRow> data) {

        clear();
        storeCurrentData(data);

        int yy = 1;

        for (final ExamListRow row : data) {
            final RawStexam stexam = row.examRecord;

            addCell(Integer.toString(row.week), 0, yy);
            addCell(FMT_WMD.format(stexam.examDt), 1, yy);
            addCell(stexam.course, 2, yy);
            addCell(stexam.unit, 3, yy);

            final String type;
            if (RawStexam.QUALIFYING_EXAM.equals(stexam.examType)) {
                type = "User's Exam";
            } else if (RawStexam.REVIEW_EXAM.equals(stexam.examType)) {
                if (Integer.valueOf(0).equals(stexam.unit)) {
                    type = "Skills Rev.";
                } else {
                    type = "Unit Rev.";
                }
            } else if (RawStexam.UNIT_EXAM.equals(stexam.examType)) {
                type = "Unit Exam";
            } else if (RawStexam.FINAL_EXAM.equals(stexam.examType)) {
                type = "Final Exam";
            } else {
                type = stexam.examType;
            }
            addCell(type, 4, yy);
            addCell(stexam.version, 5, yy);
            addCell(stexam.serialNbr, 6, yy);

            final LocalDateTime start = stexam.getStartDateTime();
            final LocalDateTime fin = stexam.getFinishDateTime();

            addCell(start == null ? CoreConstants.EMPTY : FMT_HM.format(start.toLocalTime()), 7,
                    yy);
            addCell(FMT_HM.format(fin.toLocalTime()), 8, yy);

            String durstr = null;
            if (start != null) {
                final long sec = Duration.between(start, fin).getSeconds();

                if ((sec % 60L) == 0L) {
                    durstr = (sec / 60L) + " min";
                } else {
                    durstr = String.format("%d:%02d", Long.valueOf(sec / 60L),
                            Long.valueOf(sec % 60L));
                }
            }

            addCell(durstr, 9, yy);
            addCell(stexam.examScore, 10, yy);

            final String passed;
            if ("Y".equals(stexam.passed)) {
                if ("Y".equals(stexam.isFirstPassed)) {
                    passed = "Yes (*)";
                } else {
                    passed = "Yes";
                }
            } else if ("N".equals(stexam.passed)) {
                passed = "No";
            } else {
                passed = stexam.passed;
            }

            addCell(passed, 11, yy);
            addButtonCell("Details", 12, yy, "DETAIL");

            ++yy;
        }

        addLastRow(yy, 13);
        addLastCol(13, yy + 1);
    }
}
