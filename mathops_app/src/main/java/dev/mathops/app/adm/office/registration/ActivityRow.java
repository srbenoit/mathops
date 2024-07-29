package dev.mathops.app.adm.office.registration;

import dev.mathops.db.old.rec.RecBase;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The data for one row in the activity table.
 */
class ActivityRow implements Comparable<ActivityRow> {

    /** The week. */
    final int week;

    /** The course. */
    final String course;

    /** The unit. */
    final Integer unit;

    /** The activity. */
    final String activity;

    /** The activity ID. */
    final String id;

    /** The activity date. */
    final LocalDate date;

    /** The start time. */
    final LocalTime start;

    /** The finish time. */
    final LocalTime finish;

    /** The score. */
    final String score;

    /** "Y" if passed, "N" if not, null if irrelevant. */
    final String passed;

    /** true if first passing exam (set only for course exams). */
    final boolean first;

    /**
     * Constructs a new {@code ActivityRow}.
     *
     * @param theWeek     the week
     * @param theCourse   the course
     * @param theUnit     the unit
     * @param theActivity the activity
     * @param theId       the activity id
     * @param theDate     the activity date
     * @param theStart    the start time
     * @param theFinish   the finish time
     * @param theScore    the score (as a string)
     * @param thePassed   "Y" if passed, "N" if not, null if irrelevant
     * @param theFirst    true if first passing exam (set only for course exams)
     */
    ActivityRow(final int theWeek, final String theCourse, final Integer theUnit, final String theActivity,
                final String theId, final LocalDate theDate, final LocalTime theStart, final LocalTime theFinish,
                final String theScore, final String thePassed, final boolean theFirst) {

        this.week = theWeek;
        this.course = theCourse;
        this.unit = theUnit;
        this.activity = theActivity;
        this.id = theId;
        this.date = theDate;
        this.start = theStart;
        this.finish = theFinish;
        this.score = theScore;
        this.passed = thePassed;
        this.first = theFirst;
    }

    /**
     * Compares two activity rows for order. Order is based on date, then on end time.
     *
     * @param o the row to which to compare
     * @return negative, 0, or positive as this row is less than, equal to, or greater than {@code o}.
     */
    @Override
    public int compareTo(final ActivityRow o) {

        int result = Integer.compare(this.week, o.week);
        if (result == 0) {
            result = RecBase.compareAllowingNull(this.date, o.date);
            if (result == 0) {
                result = RecBase.compareAllowingNull(this.finish, o.finish);
                if (result == 0) {
                    result = RecBase.compareAllowingNull(this.start, o.start);
                    if (result == 0) {
                        result = this.id.compareTo(o.id);
                    }
                }
            }
        }

        return result;
    }
}
