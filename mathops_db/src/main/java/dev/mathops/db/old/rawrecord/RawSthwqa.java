package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "sthwqa" record.
 */
public final class RawSthwqa extends RecBase implements Comparable<RawSthwqa> {

    /** The table name. */
    public static final String TABLE_NAME = "sthwqa";

    /** A field name. */
    private static final String FLD_SERIAL_NBR = "serial_nbr";

    /** A field name. */
    private static final String FLD_QUESTION_NBR = "question_nbr";

    /** A field name. */
    private static final String FLD_ANSWER_NBR = "answer_nbr";

    /** A field name. */
    private static final String FLD_OBJECTIVE = "objective";

    /** A field name. */
    private static final String FLD_STU_ANSWER = "stu_answer";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_ANS_CORRECT = "ans_correct";

    /** A field name. */
    private static final String FLD_HW_DT = "hw_dt";

    /** A field name. */
    private static final String FLD_FINISH_TIME = "finish_time";

    /** The 'serial_nbr' field value. */
    public Long serialNbr;

    /** The 'question_nbr' field value. */
    public Integer questionNbr;

    /** The 'answer_nbr' field value. */
    public Integer answerNbr;

    /** The 'objective' field value. */
    public String objective;

    /** The 'stu_answer' field value. */
    public String stuAnswer;

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'version' field value. */
    public String version;

    /** The 'ans_correct' field value. */
    public String ansCorrect;

    /** The 'hw_dt' field value. */
    public LocalDate hwDt;

    /** The 'finish_time' field value. */
    public Integer finishTime;

    /**
     * Constructs a new {@code RawSthwqa}.
     */
    private RawSthwqa() {

        super();
    }

    /**
     * Constructs a new {@code RawSthwqa}.
     *
     * @param theSerialNbr   the exam serial number
     * @param theQuestionNbr the question number
     * @param theAnswerNbr   the answer number (for multipart questions)
     * @param theObjective   the objective
     * @param theStuAnswer   the student answer (stored for multiple choice/multiple selection)
     * @param theStuId       the student ID
     * @param theVersion     the version
     * @param theAnsCorrect  "Y" if correct, "N" if not
     * @param theHwDt        the homework date
     * @param theFinishTime  the finish time (of the question, if available, or of the exam itself if not)
     */
    public RawSthwqa(final Long theSerialNbr, final Integer theQuestionNbr,
                     final Integer theAnswerNbr, final String theObjective, final String theStuAnswer,
                     final String theStuId, final String theVersion, final String theAnsCorrect,
                     final LocalDate theHwDt, final Integer theFinishTime) {

        super();

        this.serialNbr = theSerialNbr;
        this.questionNbr = theQuestionNbr;
        this.answerNbr = theAnswerNbr;
        this.objective = theObjective;
        this.stuAnswer = theStuAnswer;
        this.stuId = theStuId;
        this.version = theVersion;
        this.ansCorrect = theAnsCorrect;
        this.hwDt = theHwDt;
        this.finishTime = theFinishTime;
    }

    /**
     * Extracts a "sthwqa" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawSthwqa fromResultSet(final ResultSet rs) throws SQLException {

        final RawSthwqa result = new RawSthwqa();

        result.serialNbr = getLongField(rs, FLD_SERIAL_NBR);
        result.questionNbr = getIntegerField(rs, FLD_QUESTION_NBR);
        result.answerNbr = getIntegerField(rs, FLD_ANSWER_NBR);
        result.objective = getStringField(rs, FLD_OBJECTIVE);
        result.stuAnswer = getStringField(rs, FLD_STU_ANSWER);
        result.stuId = getStringField(rs, FLD_STU_ID);
        result.version = getStringField(rs, FLD_VERSION);
        result.ansCorrect = getStringField(rs, FLD_ANS_CORRECT);
        result.hwDt = getDateField(rs, FLD_HW_DT);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);

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
    public int compareTo(final RawSthwqa o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.version, o.version);
            if (result == 0) {
                result = compareAllowingNull(this.hwDt, o.hwDt);
                if (result == 0) {
                    result = compareAllowingNull(this.finishTime, o.finishTime);
                    if (result == 0) {
                        result = compareAllowingNull(this.questionNbr, o.questionNbr);
                        if (result == 0) {
                            result = compareAllowingNull(this.answerNbr, o.answerNbr);
                        }
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

        appendField(htm, FLD_SERIAL_NBR, this.serialNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_QUESTION_NBR, this.questionNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_ANSWER_NBR, this.answerNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_OBJECTIVE, this.objective);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ANSWER, this.stuAnswer);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_ANS_CORRECT, this.ansCorrect);
        htm.add(DIVIDER);
        appendField(htm, FLD_HW_DT, this.hwDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_FINISH_TIME, this.finishTime);

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
                + Objects.hashCode(this.questionNbr)
                + Objects.hashCode(this.answerNbr)
                + Objects.hashCode(this.objective)
                + Objects.hashCode(this.stuAnswer)
                + Objects.hashCode(this.stuId)
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.ansCorrect)
                + Objects.hashCode(this.hwDt)
                + Objects.hashCode(this.finishTime);
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
        } else if (obj instanceof final RawSthwqa rec) {
            equal = Objects.equals(this.serialNbr, rec.serialNbr)
                    && Objects.equals(this.questionNbr, rec.questionNbr)
                    && Objects.equals(this.answerNbr, rec.answerNbr)
                    && Objects.equals(this.objective, rec.objective)
                    && Objects.equals(this.stuAnswer, rec.stuAnswer)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.ansCorrect, rec.ansCorrect)
                    && Objects.equals(this.hwDt, rec.hwDt)
                    && Objects.equals(this.finishTime, rec.finishTime);
        } else {
            equal = false;
        }

        return equal;
    }
}
