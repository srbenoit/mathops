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
 * A raw "stmpe" record.
 */
public final class RawStmpe extends RecBase implements Comparable<RawStmpe> {

    /** The table name. */
    public static final String TABLE_NAME = "stmpe";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_ACADEMIC_YR = "academic_yr";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_START_TIME = "start_time";

    /** A field name. */
    private static final String FLD_FINISH_TIME = "finish_time";

    /** A field name. */
    private static final String FLD_LAST_NAME = "last_name";

    /** A field name. */
    private static final String FLD_FIRST_NAME = "first_name";

    /** A field name. */
    private static final String FLD_MIDDLE_INITIAL = "middle_initial";

    /** A field name. */
    private static final String FLD_SEQ_NBR = "seq_nbr";

    /** A field name. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name. */
    private static final String FLD_STS_A = "sts_a";

    /** A field name. */
    private static final String FLD_STS_117 = "sts_117";

    /** A field name. */
    private static final String FLD_STS_118 = "sts_118";

    /** A field name. */
    private static final String FLD_STS_124 = "sts_124";

    /** A field name. */
    private static final String FLD_STS_125 = "sts_125";

    /** A field name. */
    private static final String FLD_STS_126 = "sts_126";

    /** A field name. */
    private static final String FLD_PLACED = "placed";

    /** A field name. */
    private static final String FLD_HOW_VALIDATED = "how_validated";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'version' field value. */
    public String version;

    /** The 'academic_yr' field value. */
    public String academicYr;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 'start_time' field value. */
    public Integer startTime;

    /** The 'finish_time' field value. */
    public Integer finishTime;

    /** The 'last_name' field value. */
    public String lastName;

    /** The 'first_name' field value. */
    public String firstName;

    /** The 'middle_initial' field value. */
    public String middleInitial;

    /** The 'seq_nbr' field value. */
    public Integer seqNbr;

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'sts_a' field value. */
    public Integer stsA;

    /** The 'sts_117' field value. */
    public Integer sts117;

    /** The 'sts_118' field value. */
    public Integer sts118;

    /** The 'sts_124' field value. */
    public Integer sts124;

    /** The 'sts_125' field value. */
    public Integer sts125;

    /** The 'sts_126' field value. */
    public Integer sts126;

    /** The 'placed' field value. */
    public String placed;

    /** The 'how_validated' field value. */
    public String howValidated;

    /**
     * Constructs a new {@code RawStmpe}.
     */
    private RawStmpe() {

        super();
    }

    /**
     * Constructs a new {@code RawStmpe}.
     *
     * @param theStuId         the student ID
     * @param theVersion       the version
     * @param theAcademicYr    the academic year, like "2122"
     * @param theExamDt        the date the exam was submitted
     * @param theStartTime     the time (minute of day) the exam was started
     * @param theFinishTime    the time (minute of day) the exam was submitted
     * @param theFirstName     the student's first name
     * @param theLastName      the student's last name
     * @param theMiddleInitial the student's middle initial
     * @param theSeqNbr        the sequence number
     * @param theSerialNbr     the serial number
     * @param theStsA          the subtest "A" score
     * @param theSts117        the subtest "117" score
     * @param theSts118        the subtest "118" score
     * @param theSts124        the subtest "124" score
     * @param theSts125        the subtest "125" score
     * @param theSts126        the subtest "126" score
     * @param thePlaced        "Y" if student placed, "N" if not, a digit for illegal attempts
     * @param theHowValidated  the method by which results were validated
     */
    public RawStmpe(final String theStuId, final String theVersion, final String theAcademicYr,
                    final LocalDate theExamDt, final Integer theStartTime, final Integer theFinishTime,
                    final String theLastName, final String theFirstName, final String theMiddleInitial,
                    final Integer theSeqNbr, final Long theSerialNbr, final Integer theStsA,
                    final Integer theSts117, final Integer theSts118, final Integer theSts124,
                    final Integer theSts125, final Integer theSts126, final String thePlaced,
                    final String theHowValidated) {

        super();

        this.stuId = theStuId;
        this.version = theVersion;
        this.academicYr = theAcademicYr;
        this.examDt = theExamDt;
        this.startTime = theStartTime;
        this.finishTime = theFinishTime;
        this.lastName = theLastName;
        this.firstName = theFirstName;
        this.middleInitial = theMiddleInitial;
        this.seqNbr = theSeqNbr;
        this.serialNbr = theSerialNbr;
        this.stsA = theStsA;
        this.sts117 = theSts117;
        this.sts118 = theSts118;
        this.sts124 = theSts124;
        this.sts125 = theSts125;
        this.sts126 = theSts126;
        this.placed = thePlaced;
        this.howValidated = theHowValidated;
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
     * Extracts a "stmpe" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStmpe fromResultSet(final ResultSet rs) throws SQLException {

        final RawStmpe result = new RawStmpe();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.version = getStringField(rs, FLD_VERSION);
        result.academicYr = getStringField(rs, FLD_ACADEMIC_YR);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.startTime = getIntegerField(rs, FLD_START_TIME);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);
        result.lastName = getStringField(rs, FLD_LAST_NAME);
        result.firstName = getStringField(rs, FLD_FIRST_NAME);
        result.middleInitial = getStringField(rs, FLD_MIDDLE_INITIAL);
        result.seqNbr = getIntegerField(rs, FLD_SEQ_NBR);
        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.stsA = getIntegerField(rs, FLD_STS_A);
        result.sts117 = getIntegerField(rs, FLD_STS_117);
        result.sts118 = getIntegerField(rs, FLD_STS_118);
        result.sts124 = getIntegerField(rs, FLD_STS_124);
        result.sts125 = getIntegerField(rs, FLD_STS_125);
        result.sts126 = getIntegerField(rs, FLD_STS_126);
        result.placed = getStringField(rs, FLD_PLACED);
        result.howValidated = getStringField(rs, FLD_HOW_VALIDATED);

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
    public int compareTo(final RawStmpe o) {

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
     * A comparator that can be used to sort a list of {@code RawStmpe} by finish date/time.
     */
    public static final class FinishDateTimeComparator implements Comparator<RawStmpe> {

        /**
         * Perform the comparison.
         */
        @Override
        public int compare(final RawStmpe o1, final RawStmpe o2) {

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
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACADEMIC_YR, this.academicYr);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_TIME, this.startTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_FINISH_TIME, this.finishTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_LAST_NAME, this.lastName);
        htm.add(DIVIDER);
        appendField(htm, FLD_FIRST_NAME, this.firstName);
        htm.add(DIVIDER);
        appendField(htm, FLD_MIDDLE_INITIAL, this.middleInitial);
        htm.add(DIVIDER);
        appendField(htm, FLD_SEQ_NBR, this.seqNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_STS_A, this.stsA);
        htm.add(DIVIDER);
        appendField(htm, FLD_STS_117, this.sts117);
        htm.add(DIVIDER);
        appendField(htm, FLD_STS_118, this.sts118);
        htm.add(DIVIDER);
        appendField(htm, FLD_STS_124, this.sts124);
        htm.add(DIVIDER);
        appendField(htm, FLD_STS_125, this.sts125);
        htm.add(DIVIDER);
        appendField(htm, FLD_STS_126, this.sts126);
        htm.add(DIVIDER);
        appendField(htm, FLD_PLACED, this.placed);
        htm.add(DIVIDER);
        appendField(htm, FLD_HOW_VALIDATED, this.howValidated);

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
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.academicYr)
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.startTime)
                + Objects.hashCode(this.finishTime)
                + Objects.hashCode(this.lastName)
                + Objects.hashCode(this.firstName)
                + Objects.hashCode(this.middleInitial)
                + Objects.hashCode(this.seqNbr)
                + Objects.hashCode(this.serialNbr)
                + Objects.hashCode(this.stsA)
                + Objects.hashCode(this.sts117)
                + Objects.hashCode(this.sts118)
                + Objects.hashCode(this.sts124)
                + Objects.hashCode(this.sts125)
                + Objects.hashCode(this.sts126)
                + Objects.hashCode(this.placed)
                + Objects.hashCode(this.howValidated);
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
        } else if (obj instanceof final RawStmpe rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.academicYr, rec.academicYr)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.startTime, rec.startTime)
                    && Objects.equals(this.finishTime, rec.finishTime)
                    && Objects.equals(this.lastName, rec.lastName)
                    && Objects.equals(this.firstName, rec.firstName)
                    && Objects.equals(this.middleInitial, rec.middleInitial)
                    && Objects.equals(this.seqNbr, rec.seqNbr)
                    && Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.stsA, rec.stsA)
                    && Objects.equals(this.sts117, rec.sts117)
                    && Objects.equals(this.sts118, rec.sts118)
                    && Objects.equals(this.sts124, rec.sts124)
                    && Objects.equals(this.sts125, rec.sts125)
                    && Objects.equals(this.sts126, rec.sts126)
                    && Objects.equals(this.placed, rec.placed)
                    && Objects.equals(this.howValidated, rec.howValidated);
        } else {
            equal = false;
        }

        return equal;
    }
}
