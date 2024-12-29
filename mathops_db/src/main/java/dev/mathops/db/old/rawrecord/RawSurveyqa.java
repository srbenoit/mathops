package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "surveyqa" record.
 */
public final class RawSurveyqa extends RawTermRecordBase implements Comparable<RawSurveyqa> {

    /** The table name. */
    public static final String TABLE_NAME = "surveyqa";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_SURVEY_NBR = "survey_nbr";

    /** A field name. */
    private static final String FLD_QUESTION_DESC = "question_desc";

    /** A field name. */
    private static final String FLD_TYPE_QUESTION = "type_question";

    /** A field name. */
    private static final String FLD_ANSWER = "answer";

    /** A field name. */
    private static final String FLD_ANSWER_DESC = "answer_desc";

    /** A field name. */
    private static final String FLD_ANSWER_MEANING = "answer_meaning";

    /** A field name. */
    private static final String FLD_MUST_ANSWER = "must_answer";

    /** A field name. */
    private static final String FLD_TREE_REF = "tree_ref";

    /** The 'pacing_structure' field value. */
    public String version;

    /** The 'survey_nbr' field value. */
    public Integer surveyNbr;

    /** The 'question_desc' field value. */
    public String questionDesc;

    /** The 'type_question' field value ('CC' for calc course, 'VF' for verify). */
    public String typeQuestion;

    /**
     * The 'answer' field value (<pre>'A    ', ' B   ', '  C  ', '   D ', '    E',</pre> 'ANY', or integer.
     */
    public String answer;

    /** The 'answer_desc' field value. */
    public String answerDesc;

    /** The 'answer_meaning' field value ('BI', 'BU', 'E', 'N', 'U', or 'Y'). */
    public String answerMeaning;

    /** The 'must_answer' field value ('Y' or 'N'). */
    public String mustAnswer;

    /** The 'tree_ref' field value. */
    public String treeRef;

    /**
     * Constructs a new {@code RawSurveyqa}.
     */
    private RawSurveyqa() {

        super();
    }

    /**
     * Constructs a new {@code RawSurveyqa}.
     *
     * @param theTermKey       the term key
     * @param theVersion       the survey version
     * @param theSurveyNbr     the question number
     * @param theQuestionDesc  the question description
     * @param theTypeQuestion  the type of question
     * @param theAnswer        the answer
     * @param theAnswerDesc    the answer description
     * @param theAnswerMeaning the answer meaning
     * @param theMustAnswer    the must-answer flag
     * @param theTreeRef       the tree reference
     */
    public RawSurveyqa(final TermKey theTermKey, final String theVersion,
                       final Integer theSurveyNbr, final String theQuestionDesc, final String theTypeQuestion,
                       final String theAnswer, final String theAnswerDesc, final String theAnswerMeaning,
                       final String theMustAnswer, final String theTreeRef) {

        super(theTermKey);

        this.version = theVersion;
        this.surveyNbr = theSurveyNbr;
        this.questionDesc = theQuestionDesc;
        this.typeQuestion = theTypeQuestion;
        this.answer = theAnswer;
        this.answerDesc = theAnswerDesc;
        this.answerMeaning = theAnswerMeaning;
        this.mustAnswer = theMustAnswer;
        this.treeRef = theTreeRef;
    }

    /**
     * Extracts a "remote_mpe" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawSurveyqa fromResultSet(final ResultSet rs) throws SQLException {

        final RawSurveyqa result = new RawSurveyqa();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.version = getStringField(rs, FLD_VERSION);
        result.surveyNbr = getIntegerField(rs, FLD_SURVEY_NBR);
        result.questionDesc = getStringField(rs, FLD_QUESTION_DESC);
        result.typeQuestion = getStringField(rs, FLD_TYPE_QUESTION);
        result.answer = getStringField(rs, FLD_ANSWER);
        result.answerDesc = getStringField(rs, FLD_ANSWER_DESC);
        result.answerMeaning = getStringField(rs, FLD_ANSWER_MEANING);
        result.mustAnswer = getStringField(rs, FLD_MUST_ANSWER);
        result.treeRef = getStringField(rs, FLD_TREE_REF);

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
    public int compareTo(final RawSurveyqa o) {

        int result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.version, o.version);
            if (result == 0) {
                result = compareAllowingNull(this.surveyNbr, o.surveyNbr);
                if (result == 0) {
                    result = compareAllowingNull(this.answer, o.answer);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_SURVEY_NBR, this.surveyNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_QUESTION_DESC, this.questionDesc);
        htm.add(DIVIDER);
        appendField(htm, FLD_TYPE_QUESTION, this.typeQuestion);
        htm.add(DIVIDER);
        appendField(htm, FLD_ANSWER, this.answer);
        htm.add(DIVIDER);
        appendField(htm, FLD_ANSWER_DESC, this.answerDesc);
        htm.add(DIVIDER);
        appendField(htm, FLD_ANSWER_MEANING, this.answerMeaning);
        htm.add(DIVIDER);
        appendField(htm, FLD_MUST_ANSWER, this.mustAnswer);
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

        return Objects.hashCode(this.termKey)
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.surveyNbr)
                + Objects.hashCode(this.questionDesc)
                + Objects.hashCode(this.typeQuestion)
                + Objects.hashCode(this.answer)
                + Objects.hashCode(this.answerDesc)
                + Objects.hashCode(this.answerMeaning)
                + Objects.hashCode(this.mustAnswer)
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
        } else if (obj instanceof final RawSurveyqa rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.surveyNbr, rec.surveyNbr)
                    && Objects.equals(this.questionDesc, rec.questionDesc)
                    && Objects.equals(this.typeQuestion, rec.typeQuestion)
                    && Objects.equals(this.answer, rec.answer)
                    && Objects.equals(this.answerDesc, rec.answerDesc)
                    && Objects.equals(this.answerMeaning, rec.answerMeaning)
                    && Objects.equals(this.mustAnswer, rec.mustAnswer)
                    && Objects.equals(this.treeRef, rec.treeRef);
        } else {
            equal = false;
        }

        return equal;
    }
}
