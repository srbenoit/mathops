package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "stmathplan" record.
 */
public final class RawStmathplan extends RecBase implements Comparable<RawStmathplan> {

    /** The table name. */
    public static final String TABLE_NAME = "stmathplan";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_PIDM = "pidm";

    /** A field name. */
    private static final String FLD_APLN_TERM = "apln_term";

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

    /** A field name. */
    private static final String FLD_SESSION = "session";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'pidm' field value. */
    public Integer pidm;

    /** The 'apln_term' field value. */
    public String aplnTerm;

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

    /** The 'session' field value. */
    public Long session;

    /**
     * Constructs a new {@code RawStmathplan}.
     */
    private RawStmathplan() {

        super();
    }

    /**
     * Gets the submission date/time.
     *
     * @return the submission date/time
     */
    public LocalDateTime getWhen() {

        return this.examDt == null || this.finishTime == null ? null :
                TemporalUtils.toLocalDateTime(this.examDt, this.finishTime);
    }

    /**
     * Constructs a new {@code RawStmathplan}.
     *
     * @param theStuId      the student ID
     * @param thePidm       the student's PIDM
     * @param theAplnTerm   the application term
     * @param theVersion    the version (the page of the math plan)
     * @param theExamDt     the submission date
     * @param theSurveyNbr  the question number
     * @param theStuAnswer  the student's answer
     * @param theFinishTime the submission time
     * @param theSession    the session
     */
    public RawStmathplan(final String theStuId, final Integer thePidm, final String theAplnTerm,
                         final String theVersion, final LocalDate theExamDt, final Integer theSurveyNbr,
                         final String theStuAnswer, final Integer theFinishTime, final Long theSession) {

        super();

        this.stuId = theStuId;
        this.pidm = thePidm;
        this.aplnTerm = theAplnTerm;
        this.version = theVersion;
        this.examDt = theExamDt;
        this.surveyNbr = theSurveyNbr;
        this.stuAnswer = theStuAnswer;
        this.finishTime = theFinishTime;
        this.session = theSession;
    }

    /**
     * Extracts a "stmathplan" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStmathplan fromResultSet(final ResultSet rs) throws SQLException {

        final RawStmathplan result = new RawStmathplan();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.pidm = getIntegerField(rs, FLD_PIDM);
        result.aplnTerm = getStringField(rs, FLD_APLN_TERM);
        result.version = getStringField(rs, FLD_VERSION);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.surveyNbr = getIntegerField(rs, FLD_SURVEY_NBR);
        result.stuAnswer = getStringField(rs, FLD_STU_ANSWER);
        result.finishTime = getIntegerField(rs, FLD_FINISH_TIME);
        result.session = getLongField(rs, FLD_SESSION);

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
    public int compareTo(final RawStmathplan o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.examDt, o.examDt);

            if (result == 0) {
                result = compareAllowingNull(this.finishTime, o.finishTime);

                if (result == 0) {
                    result = compareAllowingNull(this.version, o.version);
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
        appendField(htm, FLD_PIDM, this.pidm);
        htm.add(DIVIDER);
        appendField(htm, FLD_APLN_TERM, this.aplnTerm);
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
        htm.add(DIVIDER);
        appendField(htm, FLD_SESSION, this.session);

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
                + Objects.hashCode(this.pidm)
                + Objects.hashCode(this.aplnTerm)
                + Objects.hashCode(this.version)
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.surveyNbr)
                + Objects.hashCode(this.stuAnswer)
                + Objects.hashCode(this.finishTime)
                + Objects.hashCode(this.session);
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
        } else if (obj instanceof final RawStmathplan rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.pidm, rec.pidm)
                    && Objects.equals(this.aplnTerm, rec.aplnTerm)
                    && Objects.equals(this.version, rec.version)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.surveyNbr, rec.surveyNbr)
                    && Objects.equals(this.stuAnswer, rec.stuAnswer)
                    && Objects.equals(this.finishTime, rec.finishTime)
                    && Objects.equals(this.session, rec.session);
        } else {
            equal = false;
        }

        return equal;
    }
}
