package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "remote_mpe" record.
 */
public final class RawRemoteMpe extends RawTermRecordBase implements Comparable<RawRemoteMpe> {

    /** The table name. */
    public static final String TABLE_NAME = "remote_mpe";

    /** A field name. */
    private static final String FLD_APLN_TERM = "apln_term";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_START_DT = "start_dt";

    /** A field name. */
    private static final String FLD_END_DT = "end_dt";

    /** The 'apln_term' field value. */
    public TermKey aplnTerm;

    /** The 'course' field value. */
    public String course;

    /** The 'start_dt' field value. */
    public LocalDate startDt;

    /** The 'end_dt' field value. */
    public LocalDate endDt;

    /**
     * Constructs a new {@code RawRemoteMpe}.
     */
    private RawRemoteMpe() {

        super();
    }

    /**
     * Constructs a new {@code RawRemoteMpe}.
     *
     * @param theTermKey  the term key
     * @param theAplnTerm the application term
     * @param theCourse   the course
     * @param theStartDt  the start date
     * @param theEndDt    the end date
     */
    public RawRemoteMpe(final TermKey theTermKey, final TermKey theAplnTerm, final String theCourse,
                        final LocalDate theStartDt, final LocalDate theEndDt) {

        super(theTermKey);

        this.aplnTerm = theAplnTerm;
        this.course = theCourse;
        this.startDt = theStartDt;
        this.endDt = theEndDt;
    }

    /**
     * Extracts a "remote_mpe" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawRemoteMpe fromResultSet(final ResultSet rs) throws SQLException {

        final RawRemoteMpe result = new RawRemoteMpe();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.aplnTerm = getShortTermStringField(rs, FLD_APLN_TERM);
        result.course = getStringField(rs, FLD_COURSE);
        result.startDt = getDateField(rs, FLD_START_DT);
        result.endDt = getDateField(rs, FLD_END_DT);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    @Override
    public int compareTo(final RawRemoteMpe o) {

        int result;

        result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.aplnTerm, o.aplnTerm);

            if (result == 0) {
                result = compareAllowingNull(this.course, o.course);

                if (result == 0) {
                    result = compareAllowingNull(this.startDt, o.startDt);

                    if (result == 0) {
                        result = compareAllowingNull(this.endDt, o.endDt);
                    }
                }
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
        appendField(htm, FLD_APLN_TERM, this.aplnTerm);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
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
                + Objects.hashCode(this.aplnTerm)
                + Objects.hashCode(this.course)
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
        } else if (obj instanceof final RawRemoteMpe rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.aplnTerm, rec.aplnTerm)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.startDt, rec.startDt)
                    && Objects.equals(this.endDt, rec.endDt);
        } else {
            equal = false;
        }

        return equal;
    }
}
