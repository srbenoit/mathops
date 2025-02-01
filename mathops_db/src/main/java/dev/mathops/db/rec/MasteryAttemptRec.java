package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A "mastery attempt" record.
 */
public final class MasteryAttemptRec extends RecBase implements Comparable<MasteryAttemptRec> {

    /** The table name. */
    public static final String TABLE_NAME = "mastery_attempt";

    /** A field name for serialization of records. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name for serialization of records. */
    private static final String FLD_EXAM_ID = "exam_id";

    /** A field name for serialization of records. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name for serialization of records. */
    private static final String FLD_WHEN_STARTED = "when_started";

    /** A field name for serialization of records. */
    private static final String FLD_WHEN_FINISHED = "when_finished";

    /** A field name for serialization of records. */
    private static final String FLD_EXAM_SCORE = "exam_score";

    /** A field name for serialization of records. */
    private static final String FLD_MASTERY_SCORE = "mastery_score";

    /** A field name for serialization of records. */
    private static final String FLD_PASSED = "passed";

    /** A field name for serialization of records. */
    private static final String FLD_IS_FIRST_PASSED = "is_first_passed";

    /** A field name for serialization of records. */
    private static final String FLD_EXAM_SOURCE = "exam_source";

    /** The 'serial_nbr' field value. */
    public Integer serialNbr;

    /** The 'exam_id' field value. */
    public String examId;

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'when_started' field value. */
    public LocalDateTime whenStarted;

    /** The 'when_finished' field value. */
    public LocalDateTime whenFinished;

    /** The 'exam_score' field value. */
    public Integer examScore;

    /** The 'mastery_score' field value. */
    public Integer masteryScore;

    /** The 'passed' field value. */
    public String passed;

    /** The 'is_first_passed' field value. */
    public String isFirstPassed;

    /** The 'exam_source' field value. */
    public String examSource;

    /**
     * Constructs a new {@code MasteryAttemptRec}.
     */
    public MasteryAttemptRec() {

        super();
    }

    /**
     * Constructs a new {@code MasteryAttemptRec}.
     *
     * @param theSerialNbr     the serial number
     * @param theExamId        the exam ID
     * @param theStuId         the student ID
     * @param theWhenStarted   the date the attempt was started
     * @param theWhenFinished  the date the attempt was finished/submitted
     * @param theExamScore     the exam score
     * @param theMasteryScore  the mastery score
     * @param thePassed        "Y" if passed, "N" if not, "G" if ignored, "P" if taken away
     * @param theIsFirstPassed "Y" if this is the first passing attempt; "N" if nots
     * @param theExamSource    the exam source
     */
    public MasteryAttemptRec(final Integer theSerialNbr, final String theExamId,
                             final String theStuId, final LocalDateTime theWhenStarted,
                             final LocalDateTime theWhenFinished, final Integer theExamScore,
                             final Integer theMasteryScore, final String thePassed, final String theIsFirstPassed,
                             final String theExamSource) {

        super();

        this.serialNbr = theSerialNbr;
        this.examId = theExamId;
        this.stuId = theStuId;
        this.whenStarted = theWhenStarted;
        this.whenFinished = theWhenFinished;
        this.examScore = theExamScore;
        this.masteryScore = theMasteryScore;
        this.passed = thePassed;
        this.isFirstPassed = theIsFirstPassed;
        this.examSource = theExamSource;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final MasteryAttemptRec o) {

        // Serial number is a proxy for date/time, so sort on that first
        int result = compareAllowingNull(this.serialNbr, o.serialNbr);

        if (result == 0) {
            // Serial and exam ID should be primary key, so no need to sort further
            result = compareAllowingNull(this.examId, o.examId);
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
        appendField(htm, FLD_EXAM_ID, this.examId);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_STARTED, this.whenStarted);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_FINISHED, this.whenFinished);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_SCORE, this.examScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_MASTERY_SCORE, this.masteryScore);
        htm.add(DIVIDER);
        appendField(htm, FLD_PASSED, this.passed);
        htm.add(DIVIDER);
        appendField(htm, FLD_IS_FIRST_PASSED, this.isFirstPassed);
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

        return Objects.hashCode(this.serialNbr) //
                + Objects.hashCode(this.examId) //
                + Objects.hashCode(this.stuId) //
                + Objects.hashCode(this.whenStarted) //
                + Objects.hashCode(this.whenFinished) //
                + Objects.hashCode(this.examScore) //
                + Objects.hashCode(this.masteryScore) //
                + Objects.hashCode(this.passed) //
                + Objects.hashCode(this.isFirstPassed) //
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
        } else if (obj instanceof final MasteryAttemptRec rec) {
            equal = Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.examId, rec.examId)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.whenStarted, rec.whenStarted)
                    && Objects.equals(this.whenFinished, rec.whenFinished)
                    && Objects.equals(this.examScore, rec.examScore)
                    && Objects.equals(this.masteryScore, rec.masteryScore)
                    && Objects.equals(this.passed, rec.passed)
                    && Objects.equals(this.isFirstPassed, rec.isFirstPassed)
                    && Objects.equals(this.examSource, rec.examSource);
        } else {
            equal = false;
        }

        return equal;
    }
}
