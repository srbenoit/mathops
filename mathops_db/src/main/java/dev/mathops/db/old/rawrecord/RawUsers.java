package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "users" record.
 */
public final class RawUsers extends RawTermRecordBase implements Comparable<RawUsers> {

    /** The table name. */
    public static final String TABLE_NAME = "users";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_EXAM_SCORE = "exam_score";

    /** A field name. */
    private static final String FLD_CALC_COURSE = "calc_course";

    /** A field name. */
    private static final String FLD_PASSED = "passed";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'version' field value. */
    public String version;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 'exam_score' field value. */
    public Integer examScore;

    /** The 'calc_course' field value. */
    public String calcCourse;

    /** The 'passed' field value. */
    public String passed;

    /**
     * Constructs a new {@code RawUsers}.
     */
    private RawUsers() {

        super();
    }

    /**
     * Constructs a new {@code RawUsers}.
     *
     * @param theTermKey    the term key
     * @param theSerialNbr  the serial number
     * @param theVersion    the exam version
     * @param theStuId      the student ID
     * @param theExamDt     the exam date
     * @param theExamScore  the exam score
     * @param theCalcCourse the calculus course t
     * @param thePassed     "Y" if passed, "N" if not, "P" if passed and taken away, "G" if ignored, "C" if submitted in
     *                      practice mode
     */
    public RawUsers(final TermKey theTermKey, final String theStuId, final Long theSerialNbr,
                    final String theVersion, final LocalDate theExamDt, final Integer theExamScore,
                    final String theCalcCourse, final String thePassed) {

        super(theTermKey);

        this.stuId = theStuId;
        this.serialNbr = theSerialNbr;
        this.version = theVersion;
        this.examDt = theExamDt;
        this.examScore = theExamScore;
        this.calcCourse = theCalcCourse;
        this.passed = thePassed;
    }

    /**
     * Extracts a "users" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawUsers fromResultSet(final ResultSet rs) throws SQLException {

        final RawUsers result = new RawUsers();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.version = getStringField(rs, FLD_VERSION);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.examScore = getIntegerField(rs, FLD_EXAM_SCORE);
        result.calcCourse = getStringField(rs, FLD_CALC_COURSE);
        result.passed = getStringField(rs, FLD_PASSED);

        return result;
    }

    /**
     * Generates a label for the exam with its type name and submission date/time.
     *
     * @return the label
     */
    public String getExamLabel() {

        return "User's Exam submitted " + TemporalUtils.FMT_MDY.format(this.examDt);
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawUsers o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.version, o.version);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_SCORE, this.examScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_CALC_COURSE, this.calcCourse);
        htm.add(DIVIDER);
        appendField(htm, FLD_PASSED, this.passed);

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
                + Objects.hashCode(this.stuId)
                + Objects.hashCode(this.serialNbr)
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.examScore)
                + Objects.hashCode(this.calcCourse)
                + Objects.hashCode(this.passed);
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
        } else if (obj instanceof final RawUsers rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.examScore, rec.examScore)
                    && Objects.equals(this.calcCourse, rec.calcCourse)
                    && Objects.equals(this.passed, rec.passed);
        } else {
            equal = false;
        }

        return equal;
    }
}
