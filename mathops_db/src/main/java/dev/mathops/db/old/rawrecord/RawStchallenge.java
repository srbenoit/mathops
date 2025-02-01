package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

/**
 * A raw "stchallenge" record.
 */
public final class RawStchallenge extends RecBase implements Comparable<RawStchallenge> {

    /** The table name. */
    public static final String TABLE_NAME = "stchallenge";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

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
    private static final String FLD_SCORE = "score";

    /** A field name. */
    private static final String FLD_PASSED = "passed";

    /** A field name. */
    private static final String FLD_HOW_VALIDATED = "how_validated";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

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

    /** The 'score' field value. */
    public Integer score;

    /** The 'passed' field value. */
    public String passed;

    /** The 'how_validated' field value. */
    public String howValidated;

    /**
     * Constructs a new {@code RawStchallenge}.
     */
    private RawStchallenge() {

        super();
    }

    /**
     * Constructs a new {@code RawStchallenge}.
     *
     * @param theStuId         the student ID
     * @param theCourse        the course
     * @param theVersion       the exam version
     * @param theAcademicYr    the academic year
     * @param theExamDt        the exam date
     * @param theStartTime     the start time
     * @param theFinishTime    the finish time
     * @param theLastName      the last name
     * @param theFirstName     the first name
     * @param theMiddleInitial the middle initial
     * @param theSeqNbr        the sequence number
     * @param theSerialNbr     the serial number
     * @param theScore         the score
     * @param thePassed        the passed flag
     * @param theHowValidated  the way the exam was validated
     */
    public RawStchallenge(final String theStuId, final String theCourse, final String theVersion,
                          final String theAcademicYr, final LocalDate theExamDt, final Integer theStartTime,
                          final Integer theFinishTime, final String theLastName, final String theFirstName,
                          final String theMiddleInitial, final Integer theSeqNbr, final Long theSerialNbr,
                          final Integer theScore, final String thePassed, final String theHowValidated) {

        super();

        this.stuId = theStuId;
        this.course = theCourse;
        this.version = theVersion;
        this.academicYr = theAcademicYr;
        this.examDt = theExamDt;
        this.startTime = theStartTime;
        this.finishTime = theFinishTime;
        this.firstName = theFirstName;
        this.lastName = theLastName;
        this.middleInitial = theMiddleInitial;
        this.seqNbr = theSeqNbr;
        this.serialNbr = theSerialNbr;
        this.score = theScore;
        this.passed = thePassed;
        this.howValidated = theHowValidated;
    }

    /**
     * Extracts a "stchallenge" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStchallenge fromResultSet(final ResultSet rs) throws SQLException {

        final RawStchallenge result = new RawStchallenge();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
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
        result.score = getIntegerField(rs, FLD_SCORE);
        result.passed = getStringField(rs, FLD_PASSED);
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
    public int compareTo(final RawStchallenge o) {

        int result = this.stuId.compareTo(o.stuId);

        if (result == 0) {
            result = this.course.compareTo(o.course);
            if (result == 0) {
                result = this.version.compareTo(o.version);
                if (result == 0) {
                    result = this.examDt.compareTo(o.examDt);
                    if (result == 0) {
                        result = this.finishTime.compareTo(o.finishTime);
                    }
                }
            }
        }

        return result;
    }

    /**
     * A comparator that can be used to sort a list of {@code RawStchallenge} by finish date/time.
     */
    public static final class FinishDateTimeComparator implements Comparator<RawStchallenge> {

        /**
         * Perform the comparison.
         */
        @Override
        public int compare(final RawStchallenge o1, final RawStchallenge o2) {

            int result = o1.examDt.compareTo(o2.examDt);
            if (result == 0) {
                result = o1.finishTime.compareTo(o2.finishTime);
                if (result == 0) {
                    result = o1.version.compareTo(o2.version);
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
        appendField(htm, FLD_COURSE, this.course);
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
        appendField(htm, FLD_SCORE, this.score);
        htm.add(DIVIDER);
        appendField(htm, FLD_PASSED, this.passed);
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
                + Objects.hashCode(this.course)
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
                + Objects.hashCode(this.score)
                + Objects.hashCode(this.passed)
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
        } else if (obj instanceof final RawStchallenge rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
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
                    && Objects.equals(this.score, rec.score)
                    && Objects.equals(this.passed, rec.passed)
                    && Objects.equals(this.howValidated, rec.howValidated);
        } else {
            equal = false;
        }

        return equal;
    }
}
