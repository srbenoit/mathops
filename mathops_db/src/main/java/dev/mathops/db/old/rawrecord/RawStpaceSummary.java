package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "stpace_summary" record.
 */
public final class RawStpaceSummary extends RawTermRecordBase implements Comparable<RawStpaceSummary> {

    /** The table name. */
    public static final String TABLE_NAME = "stpace_summary";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_SECT = "sect";

    /** A field name. */
    private static final String FLD_I_IN_PROGRESS = "i_in_progress";

    /** A field name. */
    private static final String FLD_PACE = "pace";

    /** A field name. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name. */
    private static final String FLD_PACE_ORDER = "pace_order";

    /** A field name. */
    private static final String FLD_MS_NBR = "ms_nbr";

    /** A field name. */
    private static final String FLD_MS_UNIT = "ms_unit";

    /** A field name. */
    private static final String FLD_MS_DATE = "ms_date";

    /** A field name. */
    private static final String FLD_NEW_MS_DATE = "new_ms_date";

    /** A field name. */
    private static final String FLD_EXAM_DT = "exam_dt";

    /** A field name. */
    private static final String FLD_RE_POINTS = "re_points";

    //

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'sect' field value. */
    public String sect;

    /** The 'i_in_progress' field value. */
    public String iInProgress;

    /** The 'pace' field value. */
    public Integer pace;

    /** The 'pace_track' field value. */
    public String paceTrack;

    /** The 'pace_order' field value. */
    public Integer paceOrder;

    /** The 'ms_nbr' field value. */
    public Integer msNbr;

    /** The 'ms_unit' field value. */
    public Integer msUnit;

    /** The 'ms_date' field value. */
    public LocalDate msDate;

    /** The 'new_ms_date' field value. */
    public String newMsDate;

    /** The 'exam_dt' field value. */
    public LocalDate examDt;

    /** The 're_points' field value. */
    public Integer rePoints;

    /**
     * Constructs a new {@code RawStpaceSummary}.
     */
    private RawStpaceSummary() {

        super();
    }

    /**
     * Constructs a new {@code RawStpaceSummary}.
     *
     * @param theTermKey     the term key
     * @param theStuId       The 'stu_id' field value
     * @param theCourse      the 'course' field value
     * @param theSect        the 'sect' field value
     * @param theIInProgress the 'i_in_progress' field value
     * @param thePace        the 'pace' field value
     * @param thePaceTrack   the 'pace_track' field value
     * @param thePaceOrder   the 'pace_order' field value
     * @param theMsNbr       the 'ms_nbr' field value
     * @param theMsUnit      the 'ms_unit' field value
     * @param theMsDate      the 'ms_date' field value
     * @param theNewMsDate   the 'new_ms_date' field value
     * @param theExamDt      the 'exam_dt' field value
     * @param theRePoints    the 're_points' field value
     */
    public RawStpaceSummary(final TermKey theTermKey, final String theStuId, final String theCourse,
                            final String theSect, final String theIInProgress, final Integer thePace,
                            final String thePaceTrack, final Integer thePaceOrder, final Integer theMsNbr,
                            final Integer theMsUnit, final LocalDate theMsDate, final String theNewMsDate,
                            final LocalDate theExamDt, final Integer theRePoints) {

        super(theTermKey);

        this.stuId = theStuId;
        this.course = theCourse;
        this.sect = theSect;
        this.iInProgress = theIInProgress;
        this.pace = thePace;
        this.paceTrack = thePaceTrack;
        this.paceOrder = thePaceOrder;
        this.msNbr = theMsNbr;
        this.msUnit = theMsUnit;
        this.msDate = theMsDate;
        this.newMsDate = theNewMsDate;
        this.examDt = theExamDt;
        this.rePoints = theRePoints;
    }

    /**
     * Extracts a "stpace_summary" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStpaceSummary fromResultSet(final ResultSet rs) throws SQLException {

        final RawStpaceSummary result = new RawStpaceSummary();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.sect = getStringField(rs, FLD_SECT);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.iInProgress = getStringField(rs, FLD_I_IN_PROGRESS);
        result.pace = getIntegerField(rs, FLD_PACE);
        result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
        result.paceOrder = getIntegerField(rs, FLD_PACE_ORDER);
        result.msNbr = getIntegerField(rs, FLD_MS_NBR);
        result.msUnit = getIntegerField(rs, FLD_MS_UNIT);
        result.msDate = getDateField(rs, FLD_MS_DATE);
        result.newMsDate = getStringField(rs, FLD_NEW_MS_DATE);
        result.examDt = getDateField(rs, FLD_EXAM_DT);
        result.rePoints = getIntegerField(rs, FLD_RE_POINTS);

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
    public int compareTo(final RawStpaceSummary o) {

        int result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.stuId, o.stuId);

            if (result == 0) {
                result = compareAllowingNull(this.course, o.course);

                if (result == 0) {
                    result = this.sect.compareTo(o.sect);

                    if (result == 0) {
                        result = compareAllowingNull(this.msUnit, o.msUnit);

                        if (result == 0) {
                            result = compareAllowingNull(this.msDate, o.msDate);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT, this.sect);
        htm.add(DIVIDER);
        appendField(htm, FLD_I_IN_PROGRESS, this.iInProgress);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_ORDER, this.paceOrder);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_NBR, this.msNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_UNIT, this.msUnit);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_DATE, this.msDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_NEW_MS_DATE, this.newMsDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_DT, this.examDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_RE_POINTS, this.rePoints);

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
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.sect)
                + Objects.hashCode(this.iInProgress)
                + Objects.hashCode(this.pace)
                + Objects.hashCode(this.paceTrack)
                + Objects.hashCode(this.paceOrder)
                + Objects.hashCode(this.msNbr)
                + Objects.hashCode(this.msUnit)
                + Objects.hashCode(this.msDate)
                + Objects.hashCode(this.newMsDate)
                + Objects.hashCode(this.examDt)
                + Objects.hashCode(this.rePoints);
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
        } else if (obj instanceof final RawStpaceSummary rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.sect, rec.sect)
                    && Objects.equals(this.iInProgress, rec.iInProgress)
                    && Objects.equals(this.pace, rec.pace)
                    && Objects.equals(this.paceTrack, rec.paceTrack)
                    && Objects.equals(this.paceOrder, rec.paceOrder)
                    && Objects.equals(this.msNbr, rec.msNbr)
                    && Objects.equals(this.msUnit, rec.msUnit)
                    && Objects.equals(this.msDate, rec.msDate)
                    && Objects.equals(this.newMsDate, rec.newMsDate)
                    && Objects.equals(this.examDt, rec.examDt)
                    && Objects.equals(this.rePoints, rec.rePoints);
        } else {
            equal = false;
        }

        return equal;
    }
}
