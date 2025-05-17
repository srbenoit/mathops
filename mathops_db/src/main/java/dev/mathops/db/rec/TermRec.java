package dev.mathops.db.rec;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.util.Objects;

/**
 * An immutable raw "term" record.
 */
public final class TermRec extends RecBase implements Comparable<TermRec> {

    /** The table name. */
    public static final String TABLE_NAME = "term";

    /** A field name. */
    private static final String FLD_TERM = "term";

    /** A field name. */
    private static final String FLD_START_DATE = "start_date";

    /** A field name. */
    private static final String FLD_END_DATE = "end_date";

    /** A field name. */
    private static final String FLD_ACADEMIC_YEAR = "academic_year";

    /** A field name. */
    private static final String FLD_ACTIVE_INDEX = "active_index";

    /** A field name. */
    private static final String FLD_DROP_DEADLINE = "drop_deadline";

    /** A field name. */
    private static final String FLD_WITHDRAW_DEADLINE = "withdraw_deadline";

    /** A field name. */
    private static final String FLD_INC_DEADLINE = "inc_deadline";

    /** The term key. */
    public final TermKey term;

    /** The 'start_date' field value. */
    public final LocalDate startDate;

    /** The 'end_date' field value. */
    public final LocalDate endDate;

    /** The 'academic_year' field value. */
    public final String academicYear;

    /** The 'active_index' field value. */
    public final Integer activeIndex;

    /** The 'drop_deadline' field value. */
    public final LocalDate dropDeadline;

    /** The 'withdraw_deadline' field value. */
    public final LocalDate withdrawDeadline;

    /** The 'inc_deadline' field value. */
    public final LocalDate incDeadline;

    /**
     * Constructs a new {@code TermRec}.
     *
     * @param theTermKey          the term key
     * @param theStartDate        the start date
     * @param theEndDate          the end date
     * @param theAcademicYear     the academic year, such as "2122"
     * @param theActiveIndex      0 for the active term, +1 for the next, -1 for the prior, etc.
     * @param theDropDeadline     the drop deadline date
     * @param theWithdrawDeadline the withdrawal deadline date
     * @param theIncDeadline the deadline to set for automatic Incompletes earned during this term (this is a
     *         date in the subsequent term)
     */
    public TermRec(final TermKey theTermKey, final LocalDate theStartDate, final LocalDate theEndDate,
                   final String theAcademicYear, final Integer theActiveIndex, final LocalDate theDropDeadline,
                   final LocalDate theWithdrawDeadline, final LocalDate theIncDeadline) {

        super();

        this.term = theTermKey;
        this.startDate = theStartDate;
        this.endDate = theEndDate;
        this.academicYear = theAcademicYear;
        this.activeIndex = theActiveIndex;
        this.dropDeadline = theDropDeadline;
        this.withdrawDeadline = theWithdrawDeadline;
        this.incDeadline = theIncDeadline;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final TermRec o) {

        return compareAllowingNull(this.term, o.term);
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

        appendField(htm, FLD_TERM, Integer.toString(this.term.toNumeric()));
        htm.add(DIVIDER);
        appendField(htm, FLD_START_DATE, this.startDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_END_DATE, this.endDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACADEMIC_YEAR, this.academicYear);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTIVE_INDEX, this.activeIndex);
        htm.add(DIVIDER);
        appendField(htm, FLD_DROP_DEADLINE, this.dropDeadline);
        htm.add(DIVIDER);
        appendField(htm, FLD_WITHDRAW_DEADLINE, this.withdrawDeadline);
        htm.add(DIVIDER);
        appendField(htm, FLD_INC_DEADLINE, this.incDeadline);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.term)
               + Objects.hashCode(this.startDate)
               + Objects.hashCode(this.endDate)
               + Objects.hashCode(this.academicYear)
               + Objects.hashCode(this.activeIndex)
               + Objects.hashCode(this.dropDeadline)
               + Objects.hashCode(this.withdrawDeadline)
               + Objects.hashCode(this.incDeadline);
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
        } else if (obj instanceof final TermRec rec) {
            equal = Objects.equals(this.term, rec.term)
                    && Objects.equals(this.startDate, rec.startDate)
                    && Objects.equals(this.endDate, rec.endDate)
                    && Objects.equals(this.academicYear, rec.academicYear)
                    && Objects.equals(this.activeIndex, rec.activeIndex)
                    && Objects.equals(this.dropDeadline, rec.dropDeadline)
                    && Objects.equals(this.withdrawDeadline, rec.withdrawDeadline)
                    && Objects.equals(this.incDeadline, rec.incDeadline);
        } else {
            equal = false;
        }

        return equal;
    }
}
