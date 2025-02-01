package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "stsurveyqa" record.
 */
public final class RawStsurveyqa extends RecBase implements Comparable<RawStsurveyqa> {

    /** The table name. */
    public static final String TABLE_NAME = "stsurveyqa";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_SURVEY_NBR = "survey_nbr";

    /** A field name. */
    private static final String FLD_STU_ANSWER = "stu_answer";

    /** A field name. */
    private static final String FLD_FINISH_TIME = "finish_time";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'version' field value. */
    public String version;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 'survey_nbr' field value. */
    public Integer surveyNbr;

    /** The 'stu_answer' field value. */
    public String stuAnswer;

    /** The 'finish_time' field value. */
    public Integer finishTime;

    /**
     * Constructs a new {@code RawStsurveyqa}.
     */
    private RawStsurveyqa() {

        super();
    }

    /**
     * Constructs a new {@code RawStsurveyqa}.
     *
     * @param theStuId      the student ID
     * @param theVersion    the version
     * @param theExamDt     the date the response was recorded
     * @param theSurveyNbr  the question number
     * @param theStuAnswer  the student answer
     * @param theFinishTime the time (minutes past midnight) when the response was recorded
     */
    public RawStsurveyqa(final String theStuId, final String theVersion, final LocalDate theExamDt,
                         final Integer theSurveyNbr, final String theStuAnswer, final Integer theFinishTime) {

        super();

        this.stuId = theStuId;
        this.version = theVersion;
        this.examDt = theExamDt;
        this.surveyNbr = theSurveyNbr;
        this.stuAnswer = theStuAnswer;
        this.finishTime = theFinishTime;
    }

    /**
     * Extracts a "stsurveyqa" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStsurveyqa fromResultSet(final ResultSet rs) throws SQLException {

        final RawStsurveyqa result = new RawStsurveyqa();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.version = getStringField(rs, FLD_VERSION);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.surveyNbr = getIntegerField(rs, FLD_SURVEY_NBR);
        result.stuAnswer = getStringField(rs, FLD_STU_ANSWER);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to which to compare
     * @return a negative value, 0, or a positive value as this object is less than, equal to, or greater than
     *         {@code o}.
     */
    @Override
    public int compareTo(final RawStsurveyqa o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.version, o.version);
            if (result == 0) {
                result = compareAllowingNull(this.examDt, o.examDt);
                if (result == 0) {
                    result = compareAllowingNull(this.finishTime, o.finishTime);
                    if (result == 0) {
                        result = compareAllowingNull(this.surveyNbr, o.surveyNbr);
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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_SURVEY_NBR, this.surveyNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ANSWER, this.stuAnswer);
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

        return Objects.hashCode(this.stuId)
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.surveyNbr)
                + Objects.hashCode(this.stuAnswer)
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
        } else if (obj instanceof final RawStsurveyqa rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.surveyNbr, rec.surveyNbr)
                    && Objects.equals(this.stuAnswer, rec.stuAnswer)
                    && Objects.equals(this.finishTime, rec.finishTime);
        } else {
            equal = false;
        }

        return equal;
    }
}
