package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "pending_exam" record.
 */
public final class RawPendingExam extends RecBase implements Comparable<RawPendingExam> {

    /** A field name. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_EXAM_SCORE = "exam_score";

    /** A field name. */
    private static final String FLD_START_TIME = "start_time";

    /** A field name. */
    private static final String FLD_FINISH_TIME = "finish_time";

    /** A field name. */
    private static final String FLD_TIME_OK = "time_ok";

    /** A field name. */
    private static final String FLD_PASSED = "passed";

    /** A field name. */
    private static final String FLD_SEQ_NBR = "seq_nbr";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_EXAM_TYPE = "exam_type";

    /** A field name. */
    private static final String FLD_TIMELIMIT_FACTOR = "timelimit_factor";

    /** A field name. */
    private static final String FLD_STU_TYPE = "stu_type";

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'version' field value. */
    public String version;

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 'exam_score' field value. */
    public Integer examScore;

    /** The 'start_time' field value. */
    public Integer startTime;

    /** The 'finish_time' field value. */
    public Integer finishTime;

    /** The 'time_ok' field value. */
    public String timeOk;

    /** The 'passed' field value. */
    public String passed;

    /** The 'seq_nbr' field value. */
    public Integer seqNbr;

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'exam_type' field value. */
    public String examType;

    /** The 'timelimit_factor' field value. */
    public Float timelimitFactor;

    /** The 'stu_type' field value. */
    public String stuType;

    /**
     * Constructs a new {@code RawPendingExam}.
     */
    private RawPendingExam() {

        super();
    }

    /**
     * Constructs a new {@code RawPendingExam}.
     *
     * @param theSerialNbr       the serial number
     * @param theVerison         the version
     * @param theStuId           the student ID
     * @param theExamDt          the exam date
     * @param theExamScore       the exam score
     * @param theStartTime       the start time
     * @param theFinishTime      the finish time
     * @param theTimeOk          "Y" if time is OK; "N" if not, null if not finished
     * @param thePassed          "Y" if passed, "N" if not, null if not finished
     * @param theSeqNbr          the sequence number
     * @param theCourse          the course
     * @param theUnit            the unit
     * @param theExamType        the exam type
     * @param theTimelimitFactor the time limit factor
     * @param theStuType         the student type
     */
    public RawPendingExam(final Long theSerialNbr, final String theVerison, final String theStuId,
                          final LocalDate theExamDt, final Integer theExamScore, final Integer theStartTime,
                          final Integer theFinishTime, final String theTimeOk, final String thePassed,
                          final Integer theSeqNbr, final String theCourse, final Integer theUnit,
                          final String theExamType, final Float theTimelimitFactor, final String theStuType) {

        super();

        this.serialNbr = theSerialNbr;
        this.version = theVerison;
        this.stuId = theStuId;
        this.examDt = theExamDt;
        this.examScore = theExamScore;
        this.startTime = theStartTime;
        this.finishTime = theFinishTime;
        this.timeOk = theTimeOk;
        this.passed = thePassed;
        this.seqNbr = theSeqNbr;
        this.course = theCourse;
        this.unit = theUnit;
        this.examType = theExamType;
        this.timelimitFactor = theTimelimitFactor;
        this.stuType = theStuType;
    }

    /**
     * Extracts a "remote_mpe" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPendingExam fromResultSet(final ResultSet rs) throws SQLException {

        final RawPendingExam result = new RawPendingExam();

        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.version = getStringField(rs, FLD_VERSION);
        result.stuId = getStringField(rs, FLD_STU_ID);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.examScore = getIntegerField(rs, FLD_EXAM_SCORE);
        result.startTime = getIntegerField(rs, FLD_START_TIME);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);
        result.timeOk = getStringField(rs, FLD_TIME_OK);
        result.passed = getStringField(rs, FLD_PASSED);
        result.seqNbr = getIntegerField(rs, FLD_SEQ_NBR);
        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.examType = getStringField(rs, FLD_EXAM_TYPE);
        result.timelimitFactor = getFloatField(rs, FLD_TIMELIMIT_FACTOR);
        result.stuType = getStringField(rs, FLD_STU_TYPE);

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
    public int compareTo(final RawPendingExam o) {

        int result;

        result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.version, o.version);

            if (result == 0) {
                result = compareAllowingNull(this.examDt, o.examDt);

                if (result == 0) {
                    result = compareAllowingNull(this.finishTime, o.finishTime);
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

        appendField(htm, FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_SCORE, this.examScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_TIME, this.startTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_FINISH_TIME, this.finishTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_TIME_OK, this.timeOk);
        htm.add(DIVIDER);
        appendField(htm, FLD_PASSED, this.passed);
        htm.add(DIVIDER);
        appendField(htm, FLD_SEQ_NBR, this.seqNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_TYPE, this.examType);
        htm.add(DIVIDER);
        appendField(htm, FLD_TIMELIMIT_FACTOR, this.timelimitFactor);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_TYPE, this.stuType);

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
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.examScore)
                + Objects.hashCode(this.startTime)
                + Objects.hashCode(this.finishTime)
                + Objects.hashCode(this.timeOk)
                + Objects.hashCode(this.passed)
                + Objects.hashCode(this.seqNbr)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.unit)
                + Objects.hashCode(this.examType)
                + Objects.hashCode(this.timelimitFactor)
                + Objects.hashCode(this.stuType);
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
        } else if (obj instanceof final RawPendingExam rec) {
            equal = Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.examScore, rec.examScore)
                    && Objects.equals(this.startTime, rec.startTime)
                    && Objects.equals(this.finishTime, rec.finishTime)
                    && Objects.equals(this.timeOk, rec.timeOk)
                    && Objects.equals(this.passed, rec.passed)
                    && Objects.equals(this.seqNbr, rec.seqNbr)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.examType, rec.examType)
                    && Objects.equals(this.timelimitFactor, rec.timelimitFactor)
                    && Objects.equals(this.stuType, rec.stuType);
        } else {
            equal = false;
        }

        return equal;
    }
}
