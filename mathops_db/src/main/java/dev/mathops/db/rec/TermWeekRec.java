package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;

/**
 * An immutable raw "term_week" record.
 *
 * <p>
 * The dates contained in a "term" may include dates before the first official week, or after the last official week.
 * The term week records for a term should form a disjoint set that covers all days in the term.  The "official" weeks
 * in the term are numbered 1 through N.  A week number of zero  represents any days before the first "official" week
 * (even if that exceeds a week in length), and a week number of (N + 1) represents any days that fall after the last
 * "official" week (even if those exceed a week).
 *
 * <p>
 * The "main" database will store these records for the active term.  Each "term" database will store these records
 * for the associated term.
 */
public final class TermWeekRec extends RecBase implements Comparable<TermWeekRec> {

    /** A field name. */
    private static final String FLD_WEEK_NBR = "week_nbr";

    /** A field name. */
    private static final String FLD_START_DATE = "start_date";

    /** A field name. */
    private static final String FLD_END_DATE = "end_date";

    /** The week number. */
    public final Integer weekNbr;

    /** The date of the first day of the week (a Sunday for weeks 1 through N). */
    public final LocalDate startDate;

    /** The date of the last day of the week (a Saturday for weeks 1 through N).  */
    public final LocalDate endDate;

    /**
     * Constructs a new {@code TermWeekRec}.
     *
     * @param theWeekNbr   the week number (may not be {@code null})
     * @param theStartDate the date of the first day of the week (may not be {@code null})
     * @param theEndDate   the date of the last day of the week (may not be {@code null})
     */
    public TermWeekRec(final Integer theWeekNbr, final LocalDate theStartDate, final LocalDate theEndDate) {

        super();

        if (theWeekNbr == null) {
            throw new IllegalArgumentException("Week number may not be null");
        }
        if (theStartDate == null) {
            throw new IllegalArgumentException("Start date may not be null");
        }
        if (theEndDate == null) {
            throw new IllegalArgumentException("End date may not be null");
        }

        this.weekNbr = theWeekNbr;
        this.startDate = theStartDate;
        this.endDate = theEndDate;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final TermWeekRec o) {

        return this.weekNbr.compareTo(o.weekNbr);
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_WEEK_NBR, this.weekNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_DATE, this.startDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_END_DATE, this.endDate);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.weekNbr.hashCode() + this.startDate.hashCode() + this.endDate.hashCode();
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final TermWeekRec rec) {
            equal = this.weekNbr.equals(rec.weekNbr)
                    && this.startDate.equals(rec.startDate)
                    && this.endDate.equals(rec.endDate);
        } else {
            equal = false;
        }

        return equal;
    }
}
