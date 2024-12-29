package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "milestone_appeal" record.  One record exists for every change to a student's milestone date.  Records can also
 * exist to document appeals that don't result in milestone date changes, or general accommodation information.
 *
 * <p>
 * For general accommodation records, the fields relating to pace, track, and milestones can be empty to indicate the
 * appeal applies to the student generally.
 *
 * <p>
 * For appeals associated with a particular milestone, the pace, track, and milestone information are present, and the
 * "prior milestone date" is the student's effective milestone date at the time the appeal was entered.  It should
 * always be possible, by querying appeal records in order by appeal date/time, to understand all adjustments that have
 * been made to a student's milestone dates.
 *
 * <pre>
 * Column name          Type                       Nulls
 * -------------------  -------------------------  -------
 * stu_id               char(9)                    no
 * term                 char(2)                    no
 * term_yr              smallint                   no
 * appeal_date_time     datetime year to second    no
 * appeal_type          char(3)                    no
 * pace                 smallint                   yes
 * pace_track           char(2)                    yes
 * ms_nbr               smallint                   yes
 * ms_type              char(8)                    yes
 * prior_ms_dt          date                       yes
 * new_ms_dt            date                       yes
 * attempts_allowed     smallint                   yes
 * circumstances        char(200)                  no
 * comment              char(200)                  yes
 * interviewer          char(20)                   no
 * </pre>
 */
public final class RawMilestoneAppeal extends RawTermRecordBase implements Comparable<RawMilestoneAppeal> {

    /** The table name. */
    public static final String TABLE_NAME = "milestone_appeal";

    /** An appeal type (accommodation). */
    public static final String APPEAL_TYPE_ACC = "ACC";

    /** An appeal type (excused absence). */
    public static final String APPEAL_TYPE_EXC = "EXC";

    /** An appeal type (close to finishing). */
    public static final String APPEAL_TYPE_FIN = "FIN";

    /** An appeal type (medical). */
    public static final String APPEAL_TYPE_MED = "MED";

    /** An appeal type (family emergency). */
    public static final String APPEAL_TYPE_FAM = "FAM";

    /** An appeal type (requested automatic extension) */
    public static final String APPEAL_TYPE_REQ = "REQ";

    /** An appeal type (automatically-applied automatic extension) */
    public static final String APPEAL_TYPE_AUT = "AUT";

    /** An appeal type (other) */
    public static final String APPEAL_TYPE_OTH = "OTH";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_APPEAL_DATE_TIME = "appeal_date_time";

    /** A field name. */
    private static final String FLD_APPEAL_TYPE = "appeal_type";

    /** A field name. */
    private static final String FLD_PACE = "pace";

    /** A field name. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name. */
    private static final String FLD_MS_NBR = "ms_nbr";

    /** A field name. */
    private static final String FLD_MS_TYPE = "ms_type";

    /** A field name. */
    private static final String FLD_PRIOR_MS_DT = "prior_ms_dt";

    /** A field name. */
    private static final String FLD_NEW_MS_DT = "new_ms_dt";

    /** A field name. */
    private static final String FLD_ATTEMPTS_ALLOWED = "attempts_allowed";

    /** A field name. */
    private static final String FLD_CIRCUMSTANCES = "circumstances";

    /** A field name. */
    private static final String FLD_COMMENT = "comment";

    /** A field name. */
    private static final String FLD_INTERVIEWER = "interviewer";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'appeal_dtime' field value. */
    public LocalDateTime appealDateTime;

    /** The 'appeal_type' field value. */
    public String appealType;

    /** The 'pace' field value. */
    public Integer pace;

    /** The 'pace_track' field value. */
    public String paceTrack;

    /** The 'ms_nbr' field value. */
    public Integer msNbr;

    /** The 'ms_type' field value. */
    public String msType;

    /** The 'prior_ms_dt' field value. */
    public LocalDate priorMsDt;

    /** The 'new_ms_dt' field value. */
    public LocalDate newMsDt;

    /** The 'attempts_allowed' field value. */
    public Integer attemptsAllowed;

    /** The 'circumstances' field value. */
    public String circumstances;

    /** The 'comment' field value. */
    public String comment;

    /** The 'interviewer' field value. */
    public String interviewer;

    /**
     * Constructs a new {@code RawMilestoneAppeal}.
     */
    private RawMilestoneAppeal() {

        super();
    }

    /**
     * Constructs a new {@code RawMilestoneAppeal}.
     *
     * @param theTermKey         the term key
     * @param theStuId           the student ID
     * @param theAppealType      the appeal type
     * @param theAppealDateTime  the appeal date/time
     * @param thePace            the pace
     * @param thePaceTrack       the pace track
     * @param theMsNbr           the milestone number (selects a course and unit)
     * @param theMsType          the milestone type
     * @param thePriorMsDate     the prior milestone date
     * @param tneNewMsDate       the new milestone date
     * @param theAttemptsAllowed the number of attempts allowed (used only for F1 milestones)
     * @param theCircumstances   the circumstances prompting the change
     * @param theComment         the interviewer comment
     * @param theInterviewer     the interviewer (login ID or name)
     */
    public RawMilestoneAppeal(final TermKey theTermKey, final String theStuId, final LocalDateTime theAppealDateTime,
                              final String theAppealType, final Integer thePace, final String thePaceTrack,
                              final Integer theMsNbr, final String theMsType, final LocalDate thePriorMsDate,
                              final LocalDate tneNewMsDate, final Integer theAttemptsAllowed,
                              final String theCircumstances, final String theComment, final String theInterviewer) {

        super(theTermKey);

        this.stuId = theStuId;
        this.appealDateTime = theAppealDateTime;
        this.appealType = theAppealType;
        this.pace = thePace;
        this.paceTrack = thePaceTrack;
        this.msNbr = theMsNbr;
        this.msType = theMsType;
        this.priorMsDt = thePriorMsDate;
        this.newMsDt = tneNewMsDate;
        this.attemptsAllowed = theAttemptsAllowed;
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
    public static RawMilestoneAppeal fromResultSet(final ResultSet rs) throws SQLException {

        final RawMilestoneAppeal result = new RawMilestoneAppeal();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.appealDateTime = getDateTimeField(rs, FLD_APPEAL_DATE_TIME);
        result.appealType = getStringField(rs, FLD_APPEAL_TYPE);
        result.pace = getIntegerField(rs, FLD_PACE);
        result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
        result.msNbr = getIntegerField(rs, FLD_MS_NBR);
        result.msType = getStringField(rs, FLD_MS_TYPE);
        result.priorMsDt = getDateField(rs, FLD_PRIOR_MS_DT);
        result.newMsDt = getDateField(rs, FLD_NEW_MS_DT);
        result.attemptsAllowed = getIntegerField(rs, FLD_ATTEMPTS_ALLOWED);
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
    public int compareTo(final RawMilestoneAppeal o) {

        int result = compareAllowingNull(this.appealDateTime, o.appealDateTime);

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
        appendField(htm, FLD_APPEAL_DATE_TIME, this.appealDateTime);
        htm.add(DIVIDER);
        appendField(htm, FLD_APPEAL_TYPE, this.appealType);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_NBR, this.msNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_TYPE, this.msType);
        htm.add(DIVIDER);
        appendField(htm, FLD_PRIOR_MS_DT, this.priorMsDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_NEW_MS_DT, this.newMsDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_ATTEMPTS_ALLOWED, this.attemptsAllowed);
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
                + Objects.hashCode(this.appealDateTime)
                + Objects.hashCode(this.appealType)
                + Objects.hashCode(this.pace)
                + Objects.hashCode(this.paceTrack)
                + Objects.hashCode(this.msNbr)
                + Objects.hashCode(this.msType)
                + Objects.hashCode(this.priorMsDt)
                + Objects.hashCode(this.newMsDt)
                + Objects.hashCode(this.attemptsAllowed)
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
        } else if (obj instanceof final RawMilestoneAppeal rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.appealDateTime, rec.appealDateTime)
                    && Objects.equals(this.appealType, rec.appealType)
                    && Objects.equals(this.pace, rec.pace)
                    && Objects.equals(this.paceTrack, rec.paceTrack)
                    && Objects.equals(this.msNbr, rec.msNbr)
                    && Objects.equals(this.msType, rec.msType)
                    && Objects.equals(this.priorMsDt, rec.priorMsDt)
                    && Objects.equals(this.newMsDt, rec.newMsDt)
                    && Objects.equals(this.attemptsAllowed, rec.attemptsAllowed)
                    && Objects.equals(this.circumstances, rec.circumstances)
                    && Objects.equals(this.comment, rec.comment)
                    && Objects.equals(this.interviewer, rec.interviewer);
        } else {
            equal = false;
        }

        return equal;
    }
}
