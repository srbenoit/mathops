package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "plc_fee" record.
 */
public final class RawPlcFee extends RecBase implements Comparable<RawPlcFee> {

    /** The table name. */
    public static final String TABLE_NAME = "plc_fee";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_BILL_DT = "bill_dt";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 'bill_dt' field value. */
    public LocalDate billDt;

    /**
     * Constructs a new {@code RawPlcFee}.
     */
    private RawPlcFee() {

        super();
    }

    /**
     * Constructs a new {@code RawPlcFee}.
     *
     * @param theStuId  the student ID
     * @param theCourse the course
     * @param theExamDt the exam date
     * @param theBillDt the bill date
     */
    public RawPlcFee(final String theStuId, final String theCourse, final LocalDate theExamDt,
                     final LocalDate theBillDt) {

        super();

        this.stuId = theStuId;
        this.course = theCourse;
        this.examDt = theExamDt;
        this.billDt = theBillDt;
    }

    /**
     * Extracts an "plc_fee" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPlcFee fromResultSet(final ResultSet rs) throws SQLException {

        final RawPlcFee result = new RawPlcFee();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.billDt = getDateField(rs, FLD_BILL_DT);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawPlcFee o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.course, o.course);

            if (result == 0) {
                result = compareAllowingNull(this.examDt, o.examDt);
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
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_BILL_DT, this.billDt);

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
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.billDt);
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
        } else if (obj instanceof final RawPlcFee rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.billDt, rec.billDt);
        } else {
            equal = false;
        }

        return equal;
    }
}
