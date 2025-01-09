package dev.mathops.app.adm.office.registration;

import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.old.rec.RecBase;

import java.util.ArrayList;
import java.util.List;

/**
 * The data for one row in the activity table.
 */
public class ExamListRow implements Comparable<ExamListRow> {

    /** The week. */
    public final int week;

    /** The student exam record. */
    public final RawStexam examRecord;

    /** The answers. */
    public final List<RawStqa> answers;

    /**
     * Constructs a new {@code ExamListRow}.
     *
     * @param theWeek       the week
     * @param theExamRecord the exam record
     * @param theAnswers    the answers
     */
    ExamListRow(final int theWeek, final RawStexam theExamRecord,
                              final List<RawStqa> theAnswers) {

        this.week = theWeek;
        this.examRecord = theExamRecord;
        this.answers = new ArrayList<>(theAnswers);
    }

    /**
     * Compares two activity rows for order. Order is based on date, then on end time.
     *
     * @param o the row to which to compare
     * @return negative, 0, or positive as this row is less than, equal to, or greater than {@code o}.
     */
    @Override
    public int compareTo(final ExamListRow o) {

        int result = RecBase.compareAllowingNull(this.examRecord.examDt, o.examRecord.examDt);

        if (result == 0) {
            result = RecBase.compareAllowingNull(this.examRecord.finishTime,
                    o.examRecord.finishTime);

            if (result == 0) {
                result = RecBase.compareAllowingNull(this.examRecord.startTime,
                        o.examRecord.startTime);
            }
        }

        return result;
    }
}
