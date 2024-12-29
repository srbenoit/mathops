package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "pace_appeals" record.
 */
public final class RawPaceAppeals extends RawTermRecordBase implements Comparable<RawPaceAppeals> {

    /** The table name. */
    public static final String TABLE_NAME = "pace_appeals";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_APPEAL_DT = "appeal_dt";

    /** A field name. */
    private static final String FLD_RELIEF_GIVEN = "relief_given";

    /** A field name. */
    private static final String FLD_PACE = "pace";

    /** A field name. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name. */
    private static final String FLD_MS_NBR = "ms_nbr";

    /** A field name. */
    private static final String FLD_MS_TYPE = "ms_type";

    /** A field name. */
    private static final String FLD_MS_DATE = "ms_date";

    /** A field name. */
    private static final String FLD_NEW_DEADLINE_DT = "new_deadline_dt";

    /** A field name. */
    private static final String FLD_NBR_ATMPTS_ALLOW = "nbr_atmpts_allow";

    /** A field name. */
    private static final String FLD_CIRCUMSTANCES = "circumstances";

    /** A field name. */
    private static final String FLD_COMMENT = "comment";

    /** A field name. */
    private static final String FLD_INTERVIEWER = "interviewer";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'appeal_dt' field value. */
    public LocalDate appealDt;

    /** The 'relief_given' field value. */
    public String reliefGiven;

    /** The 'pace' field value. */
    public Integer pace;

    /** The 'pace_track' field value. */
    public String paceTrack;

    /** The 'ms_nbr' field value. */
    public Integer msNbr;

    /** The 'ms_type' field value. */
    public String msType;

    /** The 'ms_date' field value. */
    public LocalDate msDate;

    /** The 'new_deadline_dt' field value. */
    public LocalDate newDeadlineDt;

    /** The 'nbr_atmpts_allow' field value. */
    public Integer nbrAtmptsAllow;

    /** The 'circumstances' field value. */
    public String circumstances;

    /** The 'comment' field value. */
    public String comment;

    /** The 'interviewer' field value. */
    public String interviewer;

    /**
     * Constructs a new {@code RawPaceAppeals}.
     */
    private RawPaceAppeals() {

        super();
    }

    /**
     * Constructs a new {@code RawPaceAppeals}.
     *
     * @param theTermKey        the term key
     * @param theStuId          the student ID
     * @param theAppealDt       the appeal date
     * @param theReliefGiven    "Y" if relief was given, "N" if not
     * @param thePace           the pace
     * @param thePaceTrack      the pace track
     * @param theMsNbr          the milestone number (selects a course and unit)
     * @param theMsType         the milestone type
     * @param theMsDate         the original milestone date
     * @param theNewDeadlineDt  the new deadline date
     * @param theNbrAtmptsAllow the number of attempts allowed (used only for F1 milestones)
     * @param theCircumstances  the circumstances prompting the change
     * @param theComment        the interviewer comment
     * @param theInterviewer    the interviewer (login ID or name)
     */
    public RawPaceAppeals(final TermKey theTermKey, final String theStuId,
                          final LocalDate theAppealDt, final String theReliefGiven, final Integer thePace,
                          final String thePaceTrack, final Integer theMsNbr, final String theMsType,
                          final LocalDate theMsDate, final LocalDate theNewDeadlineDt,
                          final Integer theNbrAtmptsAllow, final String theCircumstances, final String theComment,
                          final String theInterviewer) {

        super(theTermKey);

        this.stuId = theStuId;
        this.appealDt = theAppealDt;
        this.reliefGiven = theReliefGiven;
        this.pace = thePace;
        this.paceTrack = thePaceTrack;
        this.msNbr = theMsNbr;
        this.msType = theMsType;
        this.msDate = theMsDate;
        this.newDeadlineDt = theNewDeadlineDt;
        this.nbrAtmptsAllow = theNbrAtmptsAllow;
        this.circumstances = theCircumstances;
        this.comment = theComment;
        this.interviewer = theInterviewer;
    }

    /**
     * Extracts a "pace_appeals" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPaceAppeals fromResultSet(final ResultSet rs) throws SQLException {

        final RawPaceAppeals result = new RawPaceAppeals();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.appealDt = getDateField(rs, FLD_APPEAL_DT);
        result.reliefGiven = getStringField(rs, FLD_RELIEF_GIVEN);
        result.pace = getIntegerField(rs, FLD_PACE);
        result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
        result.msNbr = getIntegerField(rs, FLD_MS_NBR);
        result.msType = getStringField(rs, FLD_MS_TYPE);
        result.msDate = getDateField(rs, FLD_MS_DATE);
        result.newDeadlineDt = getDateField(rs, FLD_NEW_DEADLINE_DT);
        result.nbrAtmptsAllow = getIntegerField(rs, FLD_NBR_ATMPTS_ALLOW);
        result.circumstances = getStringField(rs, FLD_CIRCUMSTANCES);
        result.comment = getStringField(rs, FLD_COMMENT);
        result.interviewer = getStringField(rs, FLD_INTERVIEWER);

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
    public int compareTo(final RawPaceAppeals o) {

        int result = compareAllowingNull(this.appealDt, o.appealDt);

        if (result == 0) {
            result = compareAllowingNull(this.stuId, o.stuId);
            if (result == 0) {
                result = compareAllowingNull(this.msNbr, o.msNbr);
                if (result == 0) {
                    // We want "F1" to sort after "FE", which is not the normal collation order for Strings
                    if ("FE".equals(this.msType)) {
                        if ("F1".equals(o.msType)) {
                            result = -1;
                        } else {
                            result = compareAllowingNull(this.msType, o.msType);
                        }
                    } else if ("F1".equals(this.msType)) {
                        if ("FE".equals(o.msType)) {
                            result = 1;
                        } else {
                            result = compareAllowingNull(this.msType, o.msType);
                        }
                    } else {
                        result = compareAllowingNull(this.msType, o.msType);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_APPEAL_DT, this.appealDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_RELIEF_GIVEN, this.reliefGiven);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_NBR, this.msNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_TYPE, this.msType);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_DATE, this.msDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_NEW_DEADLINE_DT, this.newDeadlineDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_ATMPTS_ALLOW, this.nbrAtmptsAllow);
        htm.add(DIVIDER);
        appendField(htm, FLD_CIRCUMSTANCES, this.circumstances);
        htm.add(DIVIDER);
        appendField(htm, FLD_COMMENT, this.comment);
        htm.add(DIVIDER);
        appendField(htm, FLD_INTERVIEWER, this.interviewer);

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
                + Objects.hashCode(this.stuId)
                + Objects.hashCode(this.appealDt)
                + Objects.hashCode(this.reliefGiven)
                + Objects.hashCode(this.pace)
                + Objects.hashCode(this.paceTrack)
                + Objects.hashCode(this.msNbr)
                + Objects.hashCode(this.msType)
                + Objects.hashCode(this.msDate)
                + Objects.hashCode(this.newDeadlineDt)
                + Objects.hashCode(this.nbrAtmptsAllow)
                + Objects.hashCode(this.circumstances)
                + Objects.hashCode(this.comment)
                + Objects.hashCode(this.interviewer);
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
        } else if (obj instanceof final RawPaceAppeals rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.appealDt, rec.appealDt)
                    && Objects.equals(this.reliefGiven, rec.reliefGiven)
                    && Objects.equals(this.pace, rec.pace)
                    && Objects.equals(this.paceTrack, rec.paceTrack)
                    && Objects.equals(this.msNbr, rec.msNbr)
                    && Objects.equals(this.msType, rec.msType)
                    && Objects.equals(this.msDate, rec.msDate)
                    && Objects.equals(this.newDeadlineDt, rec.newDeadlineDt)
                    && Objects.equals(this.nbrAtmptsAllow, rec.nbrAtmptsAllow)
                    && Objects.equals(this.circumstances, rec.circumstances)
                    && Objects.equals(this.comment, rec.comment)
                    && Objects.equals(this.interviewer, rec.interviewer);
        } else {
            equal = false;
        }

        return equal;
    }
}
