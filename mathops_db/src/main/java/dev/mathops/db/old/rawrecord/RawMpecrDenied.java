package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "mpecr_denied" record.
 */
public final class RawMpecrDenied extends RecBase implements Comparable<RawMpecrDenied> {

    /** The table name. */
    public static final String TABLE_NAME = "mpecr_denied";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_EXAM_PLACED = "exam_placed";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_WHY_DENIED = "why_denied";

    /** A field name. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_EXAM_SOURCE = "exam_source";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'exam_placed' field value. */
    public String examPlaced;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 'why_denied' field value. */
    public String whyDenied;

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'version' field value. */
    public String version;

    /** The 'exam_source' field value. */
    public String examSource;

    /**
     * Constructs a new {@code RawMpecrDenied}.
     */
    private RawMpecrDenied() {

        super();
    }

    /**
     * Constructs a new {@code RawMpecrDenied}.
     *
     * @param theStuId      the student ID
     * @param theCourse     the course
     * @param theExamPlaced the placed result of the exam
     * @param theExamDt     the exam date
     * @param theWhyDenied  the reason for the denial
     * @param theSerialNbr  the serial number
     * @param theVersion    the exam version
     * @param theExamSource the exam source
     */
    public RawMpecrDenied(final String theStuId, final String theCourse, final String theExamPlaced,
                          final LocalDate theExamDt, final String theWhyDenied, final Long theSerialNbr,
                          final String theVersion, final String theExamSource) {

        super();

        this.stuId = theStuId;
        this.course = theCourse;
        this.examPlaced = theExamPlaced;
        this.examDt = theExamDt;
        this.whyDenied = theWhyDenied;
        this.serialNbr = theSerialNbr;
        this.version = theVersion;
        this.examSource = theExamSource;
    }

    /**
     * Extracts an "mpecr_denied" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawMpecrDenied fromResultSet(final ResultSet rs) throws SQLException {

        final RawMpecrDenied result = new RawMpecrDenied();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.examPlaced = getStringField(rs, FLD_EXAM_PLACED);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.whyDenied = getStringField(rs, FLD_WHY_DENIED);
        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.version = getStringField(rs, FLD_VERSION);
        result.examSource = getStringField(rs, FLD_EXAM_SOURCE);

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
    public int compareTo(final RawMpecrDenied o) {

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
        appendField(htm, FLD_EXAM_PLACED, this.examPlaced);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHY_DENIED, this.whyDenied);
        htm.add(DIVIDER);
        appendField(htm, FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_SOURCE, this.examSource);

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
                + Objects.hashCode(this.examPlaced)
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.whyDenied)
                + Objects.hashCode(this.serialNbr)
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.examSource);
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
        } else if (obj instanceof final RawMpecrDenied rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.examPlaced, rec.examPlaced)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.whyDenied, rec.whyDenied)
                    && Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.examSource, rec.examSource);
        } else {
            equal = false;
        }

        return equal;
    }
}
