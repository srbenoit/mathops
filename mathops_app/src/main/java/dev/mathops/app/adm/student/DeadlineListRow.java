package dev.mathops.app.adm.student;

import dev.mathops.db.rawrecord.RawMilestone;
import dev.mathops.db.rawrecord.RawPaceAppeals;
import dev.mathops.db.rawrecord.RawStmilestone;

import java.time.LocalDate;

/**
 * The data for one row in the deadline table.
 */
/* default */ class DeadlineListRow implements Comparable<DeadlineListRow> {

    /** The course. */
    public final String course;

    /** The milestone record. */
    public final RawMilestone milestoneRecord;

    /** The student milestone record (optional). */
    public final RawStmilestone stmilestoneRecord;

    /** The pace appeals record. */
    public final RawPaceAppeals paceAppealRecord;

    /** The date the student completed the milestone assignment; null if not completed. */
    public final LocalDate whenCompleted;

    /** TRUE if student completed assignment on time; FALSE if late, null if not completed. */
    public final Boolean onTime;

    /**
     * Constructs a new {@code DeadlineListRow}.
     *
     * @param theCourse            the course for which the deadline applies
     * @param theMilestoneRecord   the milestone record
     * @param theStmilestoneRecord the student milestone record (optional)
     * @param thePaceAppealRecord  the pace appeal record (optional)
     * @param theWhenCompleted     the date the student completed the milestone assignment;null if not completed
     * @param theOnTime            TRUE if student completed assignment on time; FALSE if late, null if not completed
     */
    /* default */ DeadlineListRow(final String theCourse, final RawMilestone theMilestoneRecord,
                                  final RawStmilestone theStmilestoneRecord, final RawPaceAppeals thePaceAppealRecord,
                                  final LocalDate theWhenCompleted, final Boolean theOnTime) {

        this.course = theCourse;
        this.milestoneRecord = theMilestoneRecord;
        this.stmilestoneRecord = theStmilestoneRecord;
        this.paceAppealRecord = thePaceAppealRecord;
        this.whenCompleted = theWhenCompleted;
        this.onTime = theOnTime;
    }

    /**
     * Compares two activity rows for order. Order is based on the natural order for milestones.
     *
     * @param o the row to which to compare
     * @return negative, 0, or positive as this row is less than, equal to, or greater than {@code o}.
     */
    @Override
    public int compareTo(final DeadlineListRow o) {

        return this.milestoneRecord.compareTo(o.milestoneRecord);
    }
}
