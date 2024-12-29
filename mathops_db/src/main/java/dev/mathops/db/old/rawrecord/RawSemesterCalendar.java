package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "semester_calendar" record.
 */
public final class RawSemesterCalendar extends RawTermRecordBase implements Comparable<RawSemesterCalendar> {

    /** The table name. */
    public static final String TABLE_NAME = "semester_calendar";

    /** A field name. */
    private static final String FLD_WEEK_NBR = "week_nbr";

    /** A field name. */
    private static final String FLD_START_DT = "start_dt";

    /** A field name. */
    private static final String FLD_END_DT = "end_dt";

    /** The 'week_nbr' field value. */
    public Integer weekNbr;

    /** The 'start_dt' field value. */
    public LocalDate startDt;

    /** The 'end_dt' field value. */
    public LocalDate endDt;

    /**
     * Constructs a new {@code RawSemesterCalendar}.
     */
    private RawSemesterCalendar() {

        super();
    }

    /**
     * Constructs a new {@code RawSemesterCalendar}.
     *
     * @param theTermKey the term key
     * @param theWeekNbr the week number
     * @param theStartDt the start date
     * @param theEndDt   the end date
     */
    public RawSemesterCalendar(final TermKey theTermKey, final Integer theWeekNbr,
                               final LocalDate theStartDt, final LocalDate theEndDt) {

        super(theTermKey);

        this.weekNbr = theWeekNbr;
        this.startDt = theStartDt;
        this.endDt = theEndDt;
    }

    /**
     * Extracts a "testing_centers" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawSemesterCalendar fromResultSet(final ResultSet rs) throws SQLException {

        final RawSemesterCalendar result = new RawSemesterCalendar();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.weekNbr = getIntegerField(rs, FLD_WEEK_NBR);
        result.startDt = getDateField(rs, FLD_START_DT);
        result.endDt = getDateField(rs, FLD_END_DT);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to which to compare
     * @return a negative value, 0, or a positive value as this object is less than, equal to, or greater than
     *         {@code o}.
     */
    @Override
    public int compareTo(final RawSemesterCalendar o) {

        int result = compareAllowingNull(this.startDt, o.startDt);

        if (result == 0) {
            result = compareAllowingNull(this.endDt, o.endDt);
            if (result == 0) {
                result = compareAllowingNull(this.weekNbr, o.weekNbr);
            }
        }

        return result;
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_WEEK_NBR, this.weekNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_DT, this.startDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_END_DT, this.endDt);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.termKey)
                + Objects.hashCode(this.weekNbr)
                + Objects.hashCode(this.startDt)
                + Objects.hashCode(this.endDt);
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
        } else if (obj instanceof final RawSemesterCalendar rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.weekNbr, rec.weekNbr)
                    && Objects.equals(this.startDt, rec.startDt)
                    && Objects.equals(this.endDt, rec.endDt);
        } else {
            equal = false;
        }

        return equal;
    }
}
