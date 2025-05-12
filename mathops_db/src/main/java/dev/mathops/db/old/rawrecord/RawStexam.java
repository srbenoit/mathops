package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

/**
 * A raw "stexam" record.
 */
public final class RawStexam extends RecBase implements Comparable<RawStexam> {

    /** The table name. */
    public static final String TABLE_NAME = "stexam";

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
    private static final String FLD_MASTERY_SCORE = "mastery_score";

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
    private static final String FLD_IS_FIRST_PASSED = "is_first_passed";

    /** A field name. */
    private static final String FLD_EXAM_SOURCE = "exam_source";

    /** A field name. */
    private static final String FLD_CALC_NBR = "calc_nbr";

    /** An exam type. */
    public static final String QUALIFYING_EXAM = "Q";

    /** An exam type. */
    public static final String REVIEW_EXAM = "R";

    /** An exam type. */
    public static final String UNIT_EXAM = "U";

    /** An exam type. */
    public static final String FINAL_EXAM = "F";

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

    /** The 'mastery_score' field value. */
    public Integer masteryScore;

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

    /** The 'exam_type' field value ("R", "U", "F", or "Q" for User's Exams). */
    public String examType;

    /** The 'is_first_passed' field value. */
    public String isFirstPassed;

    /** The 'exam_source' field value. */
    public String examSource;

    /**
     * The 'calc_nbr' field value. This field is no longer used, and is being re-tasked to manage "on-time" status of
     * the exam.
     *
     * <p>
     * The field is null for exams (Unit, Final) for which on-time status has no impact on grade.
     *
     * <p>
     * For Review exams, the field is calculated based on deadlines at the time the exam is submitted and marked "N" if
     * not on time, "Y" if on time (this should be updated when MILESTONE or STMILESTONE records are updated so it is
     * always correct and can be relied on for grade calculation).
     *
     * <p>
     * Instructors can override these calculations for special situations.  The field is set to "NN" to "force" the exam
     * to be considered late even if it was completed on time, or "YY" to "force" the exam to be considered on-time even
     * if it was late.  "YY" and "NN" values are not updated if milestones change.
     *
     * <p>
     * If/when we allow for incremental point deductions based on how late an exam is (for example, 1 point off for 1
     * day late, 2 points for 2 days, 3 points for 3 or more days), this field can be set to something of the form "N19"
     * (not on time, 19 days late), "N2" (2 days late), "Y" (on time), "NN2" (forced to be considered 2 days late
     * regardless of deadlines), or "YY" (forced to be considered on time).
     *
     * <p>
     * If we ever wanted to frame this as an "early completion bonus", we could introduce "Y1", and "YY2" formats to
     * mean "1 day early" or "force to consider as 2 days early".
     */
    public String calcNbr;

    /**
     * Constructs a new {@code RawStexam}.
     */
    private RawStexam() {

        super();
    }

    /**
     * Constructs a new {@code RawStexam}.
     *
     * @param theSerialNbr     the serial number
     * @param theVersion       the exam version
     * @param theStuId         the student ID
     * @param theExamDt        the exam date
     * @param theExamScore     the exam score
     * @param theMasteryScore  the mastery score used to determine "passed" status
     * @param theStartTime     the start time
     * @param theFinishTime    the finish time
     * @param theTimeOk        "Y" if time is OK, "N" if not
     * @param thePassed        "Y" if passed, "N" if not, "P" if passed and taken away, "G" if ignored, "C" if submitted
     *                         in practice mode
     * @param theSeqNbr        the sequence number
     * @param theCourse        the course
     * @param theUnit          the unit
     * @param theExamType      the exam type ("R", "U", "F", or "Q" for User's Exams)
     * @param theIsFirstPassed "Y" if this is the first time this exam was passed
     * @param theExamSource    the exam source ("TC" for testing center, "RM" for remotely proctored, null if not
     *                         proctored)
     * @param theCalcNbr       the hand calculator number issued
     */
    public RawStexam(final Long theSerialNbr, final String theVersion, final String theStuId,
                     final LocalDate theExamDt, final Integer theExamScore, final Integer theMasteryScore,
                     final Integer theStartTime, final Integer theFinishTime, final String theTimeOk,
                     final String thePassed, final Integer theSeqNbr, final String theCourse,
                     final Integer theUnit, final String theExamType, final String theIsFirstPassed,
                     final String theExamSource, final String theCalcNbr) {

        super();

        this.serialNbr = theSerialNbr;
        this.version = theVersion;
        this.stuId = theStuId;
        this.examDt = theExamDt;
        this.examScore = theExamScore;
        this.masteryScore = theMasteryScore;
        this.startTime = theStartTime;
        this.finishTime = theFinishTime;
        this.timeOk = theTimeOk;
        this.passed = thePassed;
        this.seqNbr = theSeqNbr;
        this.course = theCourse;
        this.unit = theUnit;
        this.examType = theExamType;
        this.isFirstPassed = theIsFirstPassed;
        this.examSource = theExamSource;
        this.calcNbr = theCalcNbr;
    }

    /**
     * Retrieves the start date/time of the exam.
     *
     * @return the start date/time
     */
    public LocalDateTime getStartDateTime() {

        final LocalDateTime result;

        if (this.examDt == null || this.startTime == null) {
            result = null;
        } else {
            result = TemporalUtils.toLocalDateTime(this.examDt, this.startTime);
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

        if (this.examDt == null || this.finishTime == null) {
            result = null;
        } else {
            result = TemporalUtils.toLocalDateTime(this.examDt, this.finishTime);
        }

        return result;
    }

    /**
     * Generates a label for the exam with its type name and submission date/time.
     *
     * @return the label
     */
    public String getExamLabel() {

        String lbl = null;
        if (this.examType != null) {
            String name = RawExam.getExamTypeName(this.examType);

            if ("R".equals(this.examType)) {
                if (Integer.valueOf(0).equals(this.unit)) {
                    name = "Skills Review Exam";
                } else {
                    name = "Unit " + this.unit + " Review Exam";
                }
            } else if ("U".equals(this.examType)) {
                name = "Unit " + this.unit + " Exam";
            }

            final LocalDateTime fin = getFinishDateTime();

            lbl = fin == null ? name : name + " submitted " + TemporalUtils.FMT_MDY_AT_HM_A.format(fin);
        }

        return lbl;
    }

    /**
     * Extracts a "stexam" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStexam fromResultSet(final ResultSet rs) throws SQLException {

        final RawStexam result = new RawStexam();

        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.version = getStringField(rs, FLD_VERSION);
        result.stuId = getStringField(rs, FLD_STU_ID);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.examScore = getIntegerField(rs, FLD_EXAM_SCORE);

        try {
            result.masteryScore = getIntegerField(rs, FLD_MASTERY_SCORE);
        } catch (final SQLException ex) {
            if (result.unit != null) {
                if (result.unit.intValue() < 5) {
                    result.masteryScore = Integer.valueOf(8);
                } else {
                    result.masteryScore = Integer.valueOf(16);
                }
            }
        }

        result.startTime = getIntegerField(rs, FLD_START_TIME);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);
        result.timeOk = getStringField(rs, FLD_TIME_OK);
        result.passed = getStringField(rs, FLD_PASSED);
        result.seqNbr = getIntegerField(rs, FLD_SEQ_NBR);
        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.examType = getStringField(rs, FLD_EXAM_TYPE);
        result.isFirstPassed = getStringField(rs, FLD_IS_FIRST_PASSED);
        try {
            result.examSource = getStringField(rs, FLD_EXAM_SOURCE);
        } catch (final SQLException ex) {
            result.examSource = null;
        }
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
    public int compareTo(final RawStexam o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

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
     * A comparator that can be used to sort a list of {@code RawStexam} by finish date/time.
     */
    public static final class FinishDateTimeComparator implements Comparator<RawStexam> {

        /**
         * Perform the comparison.
         */
        @Override
        public int compare(final RawStexam o1, final RawStexam o2) {

            int result = compareAllowingNull(o1.examDt, o2.examDt);
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
     * A comparator that can be used to sort a list of {@code RawStexam} by course then unit then finish time.
     */
    public static final class CourseUnitComparator implements Comparator<RawStexam> {

        /**
         * Perform the comparison.
         */
        @Override
        public int compare(final RawStexam o1, final RawStexam o2) {

            int result = compareAllowingNull(o1.course, o2.course);
            if (result == 0) {
                result = compareAllowingNull(o1.unit, o2.unit);
                if (result == 0) {
                    result = compareAllowingNull(o1.examDt, o2.examDt);
                    if (result == 0) {
                        result = compareAllowingNull(o1.finishTime, o2.finishTime);
                        if (result == 0) {
                            result = compareAllowingNull(o1.version, o2.version);
                        }
                    }
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
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_SCORE, this.examScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_MASTERY_SCORE, this.masteryScore);
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
        appendField(htm, FLD_IS_FIRST_PASSED, this.isFirstPassed);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_SOURCE, this.examSource);
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

        return Objects.hashCode(this.serialNbr)
               + Objects.hashCode(this.version)
               + Objects.hashCode(this.stuId)
               + Objects.hashCode(this.examDt)
               + Objects.hashCode(this.examScore)
               + Objects.hashCode(this.masteryScore)
               + Objects.hashCode(this.startTime)
               + Objects.hashCode(this.finishTime)
               + Objects.hashCode(this.timeOk)
               + Objects.hashCode(this.passed)
               + Objects.hashCode(this.seqNbr)
               + Objects.hashCode(this.course)
               + Objects.hashCode(this.unit)
               + Objects.hashCode(this.examType)
               + Objects.hashCode(this.isFirstPassed)
               + Objects.hashCode(this.examSource)
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
        } else if (obj instanceof final RawStexam rec) {
            equal = Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.examScore, rec.examScore)
                    && Objects.equals(this.masteryScore, rec.masteryScore)
                    && Objects.equals(this.startTime, rec.startTime)
                    && Objects.equals(this.finishTime, rec.finishTime)
                    && Objects.equals(this.timeOk, rec.timeOk)
                    && Objects.equals(this.passed, rec.passed)
                    && Objects.equals(this.seqNbr, rec.seqNbr)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.examType, rec.examType)
                    && Objects.equals(this.isFirstPassed, rec.isFirstPassed)
                    && Objects.equals(this.examSource, rec.examSource)
                    && Objects.equals(this.calcNbr, rec.calcNbr);
        } else {
            equal = false;
        }

        return equal;
    }
}
