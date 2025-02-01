package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "examqa" record.
 */
public final class RawExamQa extends RecBase implements Comparable<RawExamQa> {

    /** The table name. */
    public static final String TABLE_NAME = "examqa";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_PROBLEM_NBR = "problem_nbr";

    /** A field name. */
    private static final String FLD_EXAM_SECTION_NBR = "exam_section_nbr";

    /** A field name. */
    private static final String FLD_QUESTION_NBR = "question_nbr";

    /** A field name. */
    private static final String FLD_CORRECT_ANSWER = "correct_answer";

    /** A field name. */
    private static final String FLD_OBJECTIVE = "objective";

    /** A field name. */
    private static final String FLD_BOGUS = "bogus";

    /** A field name. */
    private static final String FLD_SUBTEST = "subtest";

    /** The 'version' field value. */
    public String version;

    /** The 'problem_nbr' field value. */
    public Integer problemNbr;

    /** The 'exam_section_nbr' field value. */
    public Integer examSectionNbr;

    /** The 'question_nbr' field value. */
    public Integer questionNbr;

    /** The 'correct_answer' field value. */
    public String correctAnswer;

    /** The 'objective' field value. */
    public String objective;

    /** The 'bogus' field value. */
    public String bogus;

    /** The 'subtest' field value. */
    public String subtest;

    /**
     * Constructs a new {@code RawExamQa}.
     */
    private RawExamQa() {

        super();
    }

    /**
     * Constructs a new {@code RawExam}.
     *
     * @param theVersion        the exam version
     * @param theProblemNbr     the problem number
     * @param theExamSectionNbr the exam section number
     * @param theQuestionNbr    the question number
     * @param theCorrectAnswer  the correct answer
     * @param theObjective      the objective
     * @param theBogus          "Y" if this record is bogus; "N" if not
     * @param theSubtest        the subtest to which the question applies
     */
    public RawExamQa(final String theVersion, final Integer theProblemNbr, final Integer theExamSectionNbr,
                     final Integer theQuestionNbr, final String theCorrectAnswer, final String theObjective,
                     final String theBogus, final String theSubtest) {

        super();

        this.version = theVersion;
        this.problemNbr = theProblemNbr;
        this.examSectionNbr = theExamSectionNbr;
        this.questionNbr = theQuestionNbr;
        this.correctAnswer = theCorrectAnswer;
        this.objective = theObjective;
        this.bogus = theBogus;
        this.subtest = theSubtest;
    }

    /**
     * Extracts a "course" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawExamQa fromResultSet(final ResultSet rs) throws SQLException {

        final RawExamQa result = new RawExamQa();

        result.version = getStringField(rs, FLD_VERSION);
        result.problemNbr = getIntegerField(rs, FLD_PROBLEM_NBR);
        result.examSectionNbr = getIntegerField(rs, FLD_EXAM_SECTION_NBR);
        result.questionNbr = getIntegerField(rs, FLD_QUESTION_NBR);
        result.correctAnswer = getStringField(rs, FLD_CORRECT_ANSWER);
        result.objective = getStringField(rs, FLD_OBJECTIVE);
        result.bogus = getStringField(rs, FLD_BOGUS);
        result.subtest = getStringField(rs, FLD_SUBTEST);

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
    public int compareTo(final RawExamQa o) {

        int result = compareAllowingNull(this.version, o.version);

        if (result == 0) {
            result = compareAllowingNull(this.problemNbr, o.problemNbr);
            if (result == 0) {
                result = compareAllowingNull(this.examSectionNbr, o.examSectionNbr);
                if (result == 0) {
                    result = compareAllowingNull(this.questionNbr, o.questionNbr);
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

        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_PROBLEM_NBR, this.problemNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_SECTION_NBR, this.examSectionNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_QUESTION_NBR, this.questionNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_CORRECT_ANSWER, this.correctAnswer);
        htm.add(DIVIDER);
        appendField(htm, FLD_OBJECTIVE, this.objective);
        htm.add(DIVIDER);
        appendField(htm, FLD_BOGUS, this.bogus);
        htm.add(DIVIDER);
        appendField(htm, FLD_SUBTEST, this.subtest);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.version)
               + Objects.hashCode(this.problemNbr)
               + Objects.hashCode(this.examSectionNbr)
               + Objects.hashCode(this.questionNbr)
               + Objects.hashCode(this.correctAnswer)
               + Objects.hashCode(this.objective)
               + Objects.hashCode(this.bogus)
               + Objects.hashCode(this.subtest);
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
        } else if (obj instanceof final RawExamQa rec) {
            equal = Objects.equals(this.version, rec.version)
                    && Objects.equals(this.problemNbr, rec.problemNbr)
                    && Objects.equals(this.examSectionNbr, rec.examSectionNbr)
                    && Objects.equals(this.questionNbr, rec.questionNbr)
                    && Objects.equals(this.correctAnswer, rec.correctAnswer)
                    && Objects.equals(this.objective, rec.objective)
                    && Objects.equals(this.bogus, rec.bogus)
                    && Objects.equals(this.subtest, rec.subtest);
        } else {
            equal = false;
        }

        return equal;
    }
}
