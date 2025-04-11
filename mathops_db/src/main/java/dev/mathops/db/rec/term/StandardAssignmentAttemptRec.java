package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * An immutable raw "standard assignment attempt" record.
 *
 * <p>
 * Each record represents a student attempt on a standard assignment (homework or mastery exam).
 *
 * <p>
 * The primary key on the underlying table is the serial number.
 */
public final class StandardAssignmentAttemptRec extends RecBase implements Comparable<StandardAssignmentAttemptRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standard_assignment_attempt";

    /** The 'serial_nbr' field value. */
    public final Integer serialNbr;

    /** The 'student_id' field value. */
    public final String studentId;

    /** The 'assignment_id' field value. */
    public final String assignmentId;

    /** The 'attempt_date' field value. */
    public final LocalDate attemptDate;

    /** The 'attempt_time_sec' field value. */
    public final Integer attemptTimeSec;

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'module_nbr' field value. */
    public final Integer moduleNbr;

    /** The 'standard_nbr' field value. */
    public final Integer standardNbr;

    /** The 'pts_possible' field value. */
    public final Integer ptsPossible;

    /** The 'min_passing_score' field value. */
    public final Integer minPassingScore;

    /** The 'score' field value. */
    public final Integer score;

    /** The 'passed' field value. */
    public final String passed;

    /**
     * Constructs a new {@code StandardAssignmentAttemptRec}.
     *
     * @param theSerialNbr       the unique serial number for the attempt (negative for practice mode)
     * @param theStudentId       the student ID
     * @param theAssignmentId    the assignment ID
     * @param theAttemptDate     the attempt date
     * @param theAttemptTimeSec  the attempt time, in seconds of the day  (numbers greater than 86,400 represent
     *                           assignments accepted on the following day but counted on the attempt date)
     * @param theCourseId        the course ID
     * @param theModuleNbr       the module number within the course
     * @param theStandardNbr     the standard number within the module
     * @param thePtsPossible     the number of points possible
     * @param theMinPassingScore the number of points needed to pass/master the assignment
     * @param theScore           the earned score
     * @param thePassed          "Y" if passed, "N" if not
     */
    public StandardAssignmentAttemptRec(final Integer theSerialNbr, final String theStudentId,
                                        final String theAssignmentId, final LocalDate theAttemptDate,
                                        final Integer theAttemptTimeSec, final String theCourseId,
                                        final Integer theModuleNbr, final Integer theStandardNbr,
                                        final Integer thePtsPossible, final Integer theMinPassingScore,
                                        final Integer theScore, final String thePassed) {

        super();

        if (theSerialNbr == null) {
            throw new IllegalArgumentException("Serial number may not be null");
        }
        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theAssignmentId == null) {
            throw new IllegalArgumentException("Assignment ID may not be null");
        }
        if (theAttemptDate == null) {
            throw new IllegalArgumentException("Attempt date may not be null");
        }
        if (theAttemptTimeSec == null) {
            throw new IllegalArgumentException("Attempt time in seconds may not be null");
        }
        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theModuleNbr == null) {
            throw new IllegalArgumentException("Module number may not be null");
        }
        if (theStandardNbr == null) {
            throw new IllegalArgumentException("Standard number may not be null");
        }
        if (theScore == null) {
            throw new IllegalArgumentException("Score may not be null");
        }
        if (thePassed == null) {
            throw new IllegalArgumentException("Passed flag may not be null");
        }

        this.serialNbr = theSerialNbr;
        this.studentId = theStudentId;
        this.assignmentId = theAssignmentId;
        this.attemptDate = theAttemptDate;
        this.attemptTimeSec = theAttemptTimeSec;
        this.courseId = theCourseId;
        this.moduleNbr = theModuleNbr;
        this.standardNbr = theStandardNbr;
        this.ptsPossible = thePtsPossible;
        this.minPassingScore = theMinPassingScore;
        this.score = theScore;
        this.passed = thePassed;
    }

    /**
     * Compares two records for order.  Order is based on serial number (which should be monotonically increasing, up to
     * the negation used to indicate practice mode).
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardAssignmentAttemptRec o) {

        return this.serialNbr.compareTo(o.serialNbr);
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

        appendField(htm, DataDict.FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_STUDENT_ID, this.studentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ASSIGNMENT_ID, this.assignmentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ATTEMPT_DATE, this.attemptDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ATTEMPT_TIME_SEC, this.attemptTimeSec);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MODULE_NBR, this.moduleNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_STANDARD_NBR, this.standardNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PTS_POSSIBLE, this.ptsPossible);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MIN_PASSING_SCORE, this.minPassingScore);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_SCORE, this.score);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PASSED, this.passed);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.serialNbr.hashCode()
               + this.studentId.hashCode()
               + this.assignmentId.hashCode()
               + this.attemptDate.hashCode()
               + this.attemptTimeSec.hashCode()
               + this.courseId.hashCode()
               + this.moduleNbr.hashCode()
               + this.standardNbr.hashCode()
               + Objects.hashCode(this.ptsPossible)
               + Objects.hashCode(this.minPassingScore)
               + this.score.hashCode()
               + this.passed.hashCode();
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
        } else if (obj instanceof final StandardAssignmentAttemptRec rec) {
            equal = this.serialNbr.equals(rec.serialNbr)
                    && this.studentId.equals(rec.studentId)
                    && this.assignmentId.equals(rec.assignmentId)
                    && this.attemptDate.equals(rec.attemptDate)
                    && this.attemptTimeSec.equals(rec.attemptTimeSec)
                    && this.courseId.equals(rec.courseId)
                    && this.moduleNbr.equals(rec.moduleNbr)
                    && this.standardNbr.equals(rec.standardNbr)
                    && Objects.equals(this.ptsPossible, rec.ptsPossible)
                    && Objects.equals(this.minPassingScore, rec.minPassingScore)
                    && this.score.equals(rec.score)
                    && this.passed.equals(rec.passed);
        } else {
            equal = false;
        }

        return equal;
    }
}
