package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "calcs" record.
 */
public final class RawCalcs extends RecBase implements Comparable<RawCalcs> {

    /** The table name. */
    public static final String TABLE_NAME = "calcs";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_ISSUED_NBR = "issued_nbr";

    /** A field name. */
    private static final String FLD_RETURN_NBR = "return_nbr";

    /** A field name. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'issued_nbr' field value. */
    public String issuedNbr;

    /** The 'returned_nbr' field value. */
    public String returnNbr;

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /**
     * Constructs a new {@code RawCalcs}.
     */
    private RawCalcs() {

        super();
    }

    /**
     * Constructs a new {@code RawCalcs}.
     *
     * @param theStuId       the student ID
     * @param theIssuedNbr   the issued calculator number
     * @param theReturnedNbr ???
     * @param theSerialNbr   the serial number of the exam
     * @param theExamDt      the exam date
     */
    public RawCalcs(final String theStuId, final String theIssuedNbr, final String theReturnedNbr,
                    final Long theSerialNbr, final LocalDate theExamDt) {
        super();

        this.stuId = theStuId;
        this.issuedNbr = theIssuedNbr;
        this.returnNbr = theReturnedNbr;
        this.serialNbr = theSerialNbr;
        this.examDt = theExamDt;
    }

    /**
     * Extracts a "testing_centers" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCalcs fromResultSet(final ResultSet rs) throws SQLException {

        final RawCalcs result = new RawCalcs();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.issuedNbr = getStringField(rs, FLD_ISSUED_NBR);
        result.returnNbr = getStringField(rs, FLD_RETURN_NBR);
        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.examDt = getDateField(rs, FLD_EXAM_DT);

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
    public int compareTo(final RawCalcs o) {

        int result = compareAllowingNull(this.examDt, o.examDt);

        if (result == 0) {
            result = compareAllowingNull(this.issuedNbr, o.issuedNbr);
            if (result == 0) {
                result = compareAllowingNull(this.stuId, o.stuId);
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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_ISSUED_NBR, this.issuedNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_RETURN_NBR, this.returnNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId)
                + Objects.hashCode(this.issuedNbr)
                + Objects.hashCode(this.returnNbr)
                + Objects.hashCode(this.serialNbr)
                + Objects.hashCode(this.examDt);
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
        } else if (obj instanceof final RawCalcs rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.issuedNbr, rec.issuedNbr)
                    && Objects.equals(this.returnNbr, rec.returnNbr)
                    && Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.examDt, rec.examDt);
        } else {
            equal = false;
        }

        return equal;
    }
}
