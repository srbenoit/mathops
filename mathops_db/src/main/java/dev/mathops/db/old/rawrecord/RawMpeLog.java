package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "mpe_log" record.
 */
public final class RawMpeLog extends RecBase implements Comparable<RawMpeLog> {

    /** The table name. */
    public static final String TABLE_NAME = "mpe_log";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_ACADEMIC_YR = "academic_yr";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_START_DT = "start_dt";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_RECOVER_DT = "recover_dt";

    /** A field name. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name. */
    private static final String FLD_START_TIME = "start_time";

    /** A field name. */
    private static final String FLD_CALC_NBR = "calc_nbr";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'academic_yr' field value. */
    public String academicYr;

    /** The 'course' field value. */
    public String course;

    /** The 'version' field value. */
    public String version;

    /** The 'start_dt' field value. */
    public LocalDate startDt;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 'recover_dt' field value. */
    public LocalDate recoverDt;

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'start_time' field value. */
    public Integer startTime;

    /** The 'calc_nbr' field value. */
    public String calcNbr;

    /**
     * Constructs a new {@code RawMpeLog}.
     */
    private RawMpeLog() {

        super();
    }

    /**
     * Constructs a new {@code RawMpeLog}.
     *
     * @param theStuId      the student ID
     * @param theAcademicYr the academic year
     * @param theCourse     the course
     * @param theVersion    the exam version
     * @param theStartDt    the exam start date
     * @param theExamDt     the exam finish date
     * @param theRecoverDt  the date the exam was recovered
     * @param theSerialNbr  the serial number
     * @param theStartTime  the exam start time
     * @param theCalcNbr    the calculator number
     */
    public RawMpeLog(final String theStuId, final String theAcademicYr, final String theCourse,
                     final String theVersion, final LocalDate theStartDt, final LocalDate theExamDt,
                     final LocalDate theRecoverDt, final Long theSerialNbr, final Integer theStartTime,
                     final String theCalcNbr) {

        super();

        this.stuId = theStuId;
        this.academicYr = theAcademicYr;
        this.course = theCourse;
        this.version = theVersion;
        this.startDt = theStartDt;
        this.examDt = theExamDt;
        this.recoverDt = theRecoverDt;
        this.serialNbr = theSerialNbr;
        this.startTime = theStartTime;
        this.calcNbr = theCalcNbr;
    }

    /**
     * Extracts an "mpe_log" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawMpeLog fromResultSet(final ResultSet rs) throws SQLException {

        final RawMpeLog result = new RawMpeLog();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.academicYr = getStringField(rs, FLD_ACADEMIC_YR);
        result.course = getStringField(rs, FLD_COURSE);
        result.version = getStringField(rs, FLD_VERSION);
        result.startDt = getDateField(rs, FLD_START_DT);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.recoverDt = getDateField(rs, FLD_RECOVER_DT);
        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.startTime = getIntegerField(rs, FLD_START_TIME);
        result.calcNbr = getStringField(rs, FLD_CALC_NBR);

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
    public int compareTo(final RawMpeLog o) {

        int result = compareAllowingNull(this.startDt, o.startDt);

        if (result == 0) {
            result = compareAllowingNull(this.startTime, o.startTime);

            if (result == 0) {
                result = compareAllowingNull(this.stuId, o.stuId);
                if (result == 0) {
                    result = compareAllowingNull(this.course, o.course);
                    if (result == 0) {
                        result = compareAllowingNull(this.version, o.version);
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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACADEMIC_YR, this.academicYr);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_DT, this.startDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_RECOVER_DT, this.recoverDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_TIME, this.startTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_CALC_NBR, this.calcNbr);

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
                + Objects.hashCode(this.academicYr)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.startDt)
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.recoverDt)
                + Objects.hashCode(this.serialNbr)
                + Objects.hashCode(this.startTime)
                + Objects.hashCode(this.calcNbr);
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
        } else if (obj instanceof final RawMpeLog rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.academicYr, rec.academicYr)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.startDt, rec.startDt)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.recoverDt, rec.recoverDt)
                    && Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.startTime, rec.startTime)
                    && Objects.equals(this.calcNbr, rec.calcNbr);
        } else {
            equal = false;
        }

        return equal;
    }
}
