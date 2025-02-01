package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

/**
 * A raw "sthomework" record.
 */
public final class RawSthomework extends RecBase implements Comparable<RawSthomework> {

    /** The table name. */
    public static final String TABLE_NAME = "sthomework";

    /** A field name. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_HW_DT = "hw_dt";

    /** A field name. */
    private static final String FLD_HW_SCORE = "hw_score";

    /** A field name. */
    private static final String FLD_START_TIME = "start_time";

    /** A field name. */
    private static final String FLD_FINISH_TIME = "finish_time";

    /** A field name. */
    private static final String FLD_TIME_OK = "time_ok";

    /** A field name. */
    private static final String FLD_PASSED = "passed";

    /** A field name. */
    private static final String FLD_HW_TYPE = "hw_type";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_SECT = "sect";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_OBJECTIVE = "objective";

    /** A field name. */
    private static final String FLD_HW_COUPON = "hw_coupon";

    /** A field name. */
    private static final String FLD_USED_DT = "used_dt";

    /** A field name. */
    private static final String FLD_USED_SERIAL_NBR = "used_serial_nbr";

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'version' field value. */
    public String version;

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'hw_date' field value. */
    public LocalDate hwDt;

    /** The 'hw_score' field value. */
    public Integer hwScore;

    /** The 'start_time' field value. */
    public Integer startTime;

    /** The 'finish_time' field value. */
    public Integer finishTime;

    /** The 'time_ok' field value. */
    public String timeOk;

    /** The 'passed' field value. */
    public String passed;

    /** The 'hw_type' field value ("HW" or "LB"). */
    public String hwType;

    /** The 'course' field value. */
    public String course;

    /** The 'sect' field value. */
    public String sect;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'objective' field value. */
    public Integer objective;

    /** The 'hw_coupon' field value. */
    public String hwCoupon;

    /** The 'used_dt' field value. */
    public LocalDate usedDt;

    /** The 'used_serial_nbr' field value. */
    public Long usedSerialNbr;

    /**
     * Constructs a new {@code RawSthomework}.
     */
    public RawSthomework() {

        super();
    }

    /**
     * Constructs a new {@code RawSthomework}.
     *
     * @param theSerialNbr     the serial number
     * @param theVersion       the exam version
     * @param theStuId         the student ID
     * @param theHwDt          the exam date
     * @param theHwScore       the exam score
     * @param theStartTime     the start time
     * @param theFinishTime    the finish time
     * @param theTimeOk        "Y" if time is OK, "N" if not
     * @param thePassed        "Y" if passed, "N" if not, "P" if passed and taken away, "G" if ignored, "C" if submitted
     *                         in practice mode
     * @param theHwType        the homework type ("HW" or "LB")
     * @param theCourse        the course
     * @param theSect          the section
     * @param theUnit          the unit
     * @param theObjective     the objective
     * @param theHwCoupon      "Y" if homework counts as a coupon
     * @param theUsedDt        the date the homework coupon was used
     * @param theUsedSerialNbr the serial number of the exam on which the coupon was used
     */
    public RawSthomework(final Long theSerialNbr, final String theVersion, final String theStuId,
                         final LocalDate theHwDt, final Integer theHwScore, final Integer theStartTime,
                         final Integer theFinishTime, final String theTimeOk, final String thePassed,
                         final String theHwType, final String theCourse, final String theSect, final Integer theUnit,
                         final Integer theObjective, final String theHwCoupon, final LocalDate theUsedDt,
                         final Long theUsedSerialNbr) {

        super();

        this.serialNbr = theSerialNbr;
        this.version = theVersion;
        this.stuId = theStuId;
        this.hwDt = theHwDt;
        this.hwScore = theHwScore;
        this.startTime = theStartTime;
        this.finishTime = theFinishTime;
        this.timeOk = theTimeOk;
        this.passed = thePassed;
        this.hwType = theHwType;
        this.course = theCourse;
        this.sect = theSect;
        this.unit = theUnit;
        this.objective = theObjective;
        this.hwCoupon = theHwCoupon;
        this.usedDt = theUsedDt;
        this.usedSerialNbr = theUsedSerialNbr;
    }

    /**
     * Retrieves the start date/time of the exam.
     *
     * @return the start date/time
     */
    public LocalDateTime getStartDateTime() {

        final LocalDateTime result;

        if (this.hwDt == null || this.startTime == null) {
            result = null;
        } else {
            result = TemporalUtils.toLocalDateTime(this.hwDt, this.startTime);
        }

        return result;
    }

    /**
     * Retrieves the finish date/time of the exam.
     *
     * @return the finish date/time
     */
    public LocalDateTime getFinishDateTime() {

        final LocalDateTime result;

        if (this.hwDt == null || this.finishTime == null) {
            result = null;
        } else {
            result = TemporalUtils.toLocalDateTime(this.hwDt, this.finishTime);
        }

        return result;
    }

    /**
     * Extracts a "sthomework" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawSthomework fromResultSet(final ResultSet rs) throws SQLException {

        final RawSthomework result = new RawSthomework();

        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.version = getStringField(rs, FLD_VERSION);
        result.stuId = getStringField(rs, FLD_STU_ID);
        result.hwDt = getDateField(rs, FLD_HW_DT);
        result.hwScore = getIntegerField(rs, FLD_HW_SCORE);
        result.startTime = getIntegerField(rs, FLD_START_TIME);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);
        result.timeOk = getStringField(rs, FLD_TIME_OK);
        result.passed = getStringField(rs, FLD_PASSED);
        result.hwType = getStringField(rs, FLD_HW_TYPE);
        result.course = getStringField(rs, FLD_COURSE);
        result.sect = getStringField(rs, FLD_SECT);
        result.unit = getIntegerField(rs, FLD_UNIT);

        Integer objective = null;
        final String obj = getStringField(rs, FLD_OBJECTIVE);
        if (obj != null) {
            try {
                objective = Integer.valueOf(obj);
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid objective in sthomework table: ", obj, ex);
            }
        }

        result.objective = objective;
        result.hwCoupon = getStringField(rs, FLD_HW_COUPON);
        result.usedDt = getDateField(rs, FLD_USED_DT);
        result.usedSerialNbr = getLongField(rs, FLD_USED_SERIAL_NBR);

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
    public int compareTo(final RawSthomework o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.version, o.version);
            if (result == 0) {
                result = compareAllowingNull(this.hwDt, o.hwDt);
                if (result == 0) {
                    result = compareAllowingNull(this.finishTime, o.finishTime);
                }
            }
        }

        return result;
    }

    /**
     * A comparator that can be used to sort a list of {@code RawSthomework} by finish date/time.
     */
    public static final class FinishDateTimeComparator implements Comparator<RawSthomework> {

        /**
         * Perform the comparison.
         */
        @Override
        public int compare(final RawSthomework o1, final RawSthomework o2) {

            int result = compareAllowingNull(o1.hwDt, o2.hwDt);
            if (result == 0) {
                result = compareAllowingNull(o1.finishTime, o2.finishTime);
                if (result == 0) {
                    result = compareAllowingNull(o1.version, o2.version);
                }
            }

            return result;
        }
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

        appendField(htm, FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_HW_DT, this.hwDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_HW_SCORE, this.hwScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_TIME, this.startTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_FINISH_TIME, this.finishTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_TIME_OK, this.timeOk);
        htm.add(DIVIDER);
        appendField(htm, FLD_PASSED, this.passed);
        htm.add(DIVIDER);
        appendField(htm, FLD_HW_TYPE, this.hwType);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT, this.sect);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_OBJECTIVE, this.objective);
        htm.add(DIVIDER);
        appendField(htm, FLD_HW_COUPON, this.hwCoupon);
        htm.add(DIVIDER);
        appendField(htm, FLD_USED_DT, this.usedDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_USED_SERIAL_NBR, this.usedSerialNbr);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.serialNbr)
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.stuId)
                + Objects.hashCode(this.hwDt)
                + Objects.hashCode(this.hwScore)
                + Objects.hashCode(this.startTime)
                + Objects.hashCode(this.finishTime)
                + Objects.hashCode(this.timeOk)
                + Objects.hashCode(this.passed)
                + Objects.hashCode(this.hwType)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.sect)
                + Objects.hashCode(this.unit)
                + Objects.hashCode(this.objective)
                + Objects.hashCode(this.hwCoupon)
                + Objects.hashCode(this.usedDt)
                + Objects.hashCode(this.usedSerialNbr);
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
        } else if (obj instanceof final RawSthomework rec) {
            equal = Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.hwDt, rec.hwDt)
                    && Objects.equals(this.hwScore, rec.hwScore)
                    && Objects.equals(this.startTime, rec.startTime)
                    && Objects.equals(this.finishTime, rec.finishTime)
                    && Objects.equals(this.timeOk, rec.timeOk)
                    && Objects.equals(this.passed, rec.passed)
                    && Objects.equals(this.hwType, rec.hwType)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.sect, rec.sect)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.objective, rec.objective)
                    && Objects.equals(this.hwCoupon, rec.hwCoupon)
                    && Objects.equals(this.usedDt, rec.usedDt)
                    && Objects.equals(this.usedSerialNbr, rec.usedSerialNbr);
        } else {
            equal = false;
        }

        return equal;
    }
}
