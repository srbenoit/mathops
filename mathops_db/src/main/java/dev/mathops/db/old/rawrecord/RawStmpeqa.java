package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "stmpeqa" record.
 */
public final class RawStmpeqa extends RecBase {

    /** The table name. */
    public static final String TABLE_NAME = "stmpeqa";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_FINISH_TIME = "finish_time";

    /** A field name. */
    private static final String FLD_QUESTION_NBR = "question_nbr";

    /** A field name. */
    private static final String FLD_STU_ANSWER = "stu_answer";

    /** A field name. */
    private static final String FLD_ANS_CORRECT = "ans_correct";

    /** A field name. */
    private static final String FLD_SUBTEST = "subtest";

    /** A field name. */
    private static final String FLD_TREE_REF = "tree_ref";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'version' field value. */
    public String version;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 'finish_time' field value. */
    public Integer finishTime;

    /** The 'question_nbr' field value. */
    public Integer questionNbr;

    /** The 'stu_answer' field value. */
    public String stuAnswer;

    /** The 'ans_correct' field value. */
    public String ansCorrect;

    /** The 'subtest' field value. */
    public String subtest;

    /** The 'tree_ref' field value. */
    public String treeRef;

    /**
     * Constructs a new {@code RawStmpeqa}.
     */
    private RawStmpeqa() {

        super();
    }

    /**
     * Constructs a new {@code RawStmpeqa}.
     *
     * @param theStuId       the student ID
     * @param theVersion     the exam version
     * @param theExamDt      the date the exam was submitted
     * @param theFinishTime  the exam finish time
     * @param theQuestionNbr the question number
     * @param theStuAnswer   the student answer
     * @param theAnsCorrect  "Y" if the answer was correct, "N" if not
     * @param theSubtest     the subtest (questions that apply to multiple subtests will generate multiple records)
     * @param theTreeRef     the tree reference
     */
    public RawStmpeqa(final String theStuId, final String theVersion, final LocalDate theExamDt,
                      final Integer theFinishTime, final Integer theQuestionNbr, final String theStuAnswer,
                      final String theAnsCorrect, final String theSubtest, final String theTreeRef) {

        super();

        this.stuId = theStuId;
        this.version = theVersion;
        this.examDt = theExamDt;
        this.finishTime = theFinishTime;
        this.questionNbr = theQuestionNbr;
        this.stuAnswer = theStuAnswer;
        this.ansCorrect = theAnsCorrect;
        this.subtest = theSubtest;
        this.treeRef = theTreeRef;
    }

    /**
     * Extracts an "stmpeqa" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStmpeqa fromResultSet(final ResultSet rs) throws SQLException {

        final RawStmpeqa result = new RawStmpeqa();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.version = getStringField(rs, FLD_VERSION);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);
        result.questionNbr = getIntegerField(rs, FLD_QUESTION_NBR);
        result.stuAnswer = getStringField(rs, FLD_STU_ANSWER);
        result.ansCorrect = getStringField(rs, FLD_ANS_CORRECT);
        result.subtest = getStringField(rs, FLD_SUBTEST);
        result.treeRef = getStringField(rs, FLD_TREE_REF);

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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_FINISH_TIME, this.finishTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_QUESTION_NBR, this.questionNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ANSWER, this.stuAnswer);
        htm.add(DIVIDER);
        appendField(htm, FLD_ANS_CORRECT, this.ansCorrect);
        htm.add(DIVIDER);
        appendField(htm, FLD_SUBTEST, this.subtest);
        htm.add(DIVIDER);
        appendField(htm, FLD_TREE_REF, this.treeRef);

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
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.finishTime)
                + Objects.hashCode(this.questionNbr)
                + Objects.hashCode(this.stuAnswer)
                + Objects.hashCode(this.ansCorrect)
                + Objects.hashCode(this.subtest)
                + Objects.hashCode(this.treeRef);
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
        } else if (obj instanceof final RawStmpeqa rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.finishTime, rec.finishTime)
                    && Objects.equals(this.questionNbr, rec.questionNbr)
                    && Objects.equals(this.stuAnswer, rec.stuAnswer)
                    && Objects.equals(this.ansCorrect, rec.ansCorrect)
                    && Objects.equals(this.subtest, rec.subtest)
                    && Objects.equals(this.treeRef, rec.treeRef);
        } else {
            equal = false;
        }

        return equal;
    }
}
