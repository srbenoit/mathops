package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A "mastery attempt QA" record.
 */
public final class MasteryAttemptQaRec extends RecBase implements Comparable<MasteryAttemptQaRec> {

    /** The table name. */
    public static final String TABLE_NAME = "mastery_attempt_qa";

    /** A field name for serialization of records. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name for serialization of records. */
    private static final String FLD_EXAM_ID = "exam_id";

    /** A field name for serialization of records. */
    private static final String FLD_QUESTION_NBR = "question_nbr";

    /** A field name for serialization of records. */
    private static final String FLD_CORRECT = "correct";

    /** The 'serial_nbr' field value. */
    public Integer serialNbr;

    /** The 'exam_id' field value. */
    public String examId;

    /** The 'question_nbr' field value. */
    public Integer questionNbr;

    /** The 'correct' field value. */
    public String correct;

    /**
     * Constructs a new {@code MasteryAttemptQaRec}.
     */
    public MasteryAttemptQaRec() {

        super();
    }

    /**
     * Constructs a new {@code MasteryAttemptQaRec}.
     *
     * @param theSerialNbr   the serial number
     * @param theExamId      the exam ID
     * @param theQuestionNbr the question number
     * @param theCorrect     "Y" if correct, "N" if not
     */
    public MasteryAttemptQaRec(final Integer theSerialNbr, final String theExamId,
                               final Integer theQuestionNbr, final String theCorrect) {

        super();

        this.serialNbr = theSerialNbr;
        this.examId = theExamId;
        this.questionNbr = theQuestionNbr;
        this.correct = theCorrect;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final MasteryAttemptQaRec o) {

        int result = compareAllowingNull(this.serialNbr, o.serialNbr);

        if (result == 0) {
            result = compareAllowingNull(this.examId, o.examId);

            if (result == 0) {
                result = compareAllowingNull(this.questionNbr, o.questionNbr);
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
        appendField(htm, FLD_EXAM_ID, this.examId);
        htm.add(DIVIDER);
        appendField(htm, FLD_QUESTION_NBR, this.questionNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_CORRECT, this.correct);

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
                + Objects.hashCode(this.questionNbr) //
                + Objects.hashCode(this.correct); //
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
        } else if (obj instanceof final MasteryAttemptQaRec rec) {
            equal = Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.examId, rec.examId)
                    && Objects.equals(this.questionNbr, rec.questionNbr)
                    && Objects.equals(this.correct, rec.correct);
        } else {
            equal = false;
        }

        return equal;
    }
}
