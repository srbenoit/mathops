package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "mpe_credit" record.
 */
public final class RawMpeCredit extends RecBase implements Comparable<RawMpeCredit> {

    /** The table name. */
    public static final String TABLE_NAME = "mpe_credit";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_EXAM_PLACED = "exam_placed";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_DT_CR_REFUSED = "dt_cr_refused";

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

    /** The 'dt_cr_refused' field value. */
    public LocalDate dtCrRefused;

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'version' field value. */
    public String version;

    /** The 'exam_source' field value. */
    public String examSource;

    /**
     * Constructs a new {@code RawMpeCredit}.
     */
    private RawMpeCredit() {

        super();
    }

    /**
     * Constructs a new {@code RawMpeCredit}.
     *
     * @param theStuId       the student ID
     * @param theCourse      the course
     * @param theExamPlaced  the exam placed flag
     * @param theExamDt      the exam date
     * @param theDtCrRefused date credit was refused
     * @param theSerialNbr   the serial number
     * @param theVersion     the version
     * @param theExamSource  the exam source
     */
    public RawMpeCredit(final String theStuId, final String theCourse, final String theExamPlaced,
                        final LocalDate theExamDt, final LocalDate theDtCrRefused, final Long theSerialNbr,
                        final String theVersion, final String theExamSource) {

        super();

        this.stuId = theStuId;
        this.course = theCourse;
        this.examPlaced = theExamPlaced;
        this.examDt = theExamDt;
        this.dtCrRefused = theDtCrRefused;
        this.serialNbr = theSerialNbr;
        this.version = theVersion;
        this.examSource = theExamSource;
    }

    /**
     * Extracts an "mpe_credit" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawMpeCredit fromResultSet(final ResultSet rs) throws SQLException {

        final RawMpeCredit result = new RawMpeCredit();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.examPlaced = getStringField(rs, FLD_EXAM_PLACED);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.dtCrRefused = getDateField(rs, FLD_DT_CR_REFUSED);
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
    public int compareTo(final RawMpeCredit o) {

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
        appendField(htm, FLD_DT_CR_REFUSED, this.dtCrRefused);
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
                + Objects.hashCode(this.dtCrRefused)
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
        } else if (obj instanceof final RawMpeCredit rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.examPlaced, rec.examPlaced)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.dtCrRefused, rec.dtCrRefused)
                    && Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.examSource, rec.examSource);
        } else {
            equal = false;
        }

        return equal;
    }
}
