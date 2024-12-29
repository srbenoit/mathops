package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "stmilestone" record.
 */
public final class RawStmilestone extends RawTermRecordBase implements Comparable<RawStmilestone> {

    /** The table name. */
    public static final String TABLE_NAME = "stmilestone";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name. */
    private static final String FLD_MS_NBR = "ms_nbr";

    /** A field name. */
    private static final String FLD_MS_TYPE = "ms_type";

    /** A field name. */
    private static final String FLD_MS_DATE = "ms_date";

    /** A field name. */
    private static final String FLD_NBR_ATMPTS_ALLOW = "nbr_atmpts_allow";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'pace_track' field value. */
    public String paceTrack;

    /** The 'ms_nbr' field value. */
    public Integer msNbr;

    /** The 'ms_type' field value. */
    public String msType;

    /** The 'ms_date' field value. */
    public LocalDate msDate;

    /** The 'nbr_atmpts_allow' field value. */
    public Integer nbrAtmptsAllow;

    /**
     * Constructs a new {@code RawStmilestone}.
     */
    private RawStmilestone() {

        super();
    }

    /**
     * Constructs a new {@code RawStmilestone}.
     *
     * @param theStuId          the student ID
     * @param theTermKey        the term key
     * @param thePaceTrack      the pace track
     * @param theMsNbr          the milestone number
     * @param theMsType         the milestone type
     * @param theMsDate         the milestone date
     * @param theNbrAtmptsAllow the number of attempts allowed
     */
    public RawStmilestone(final TermKey theTermKey, final String theStuId, final String thePaceTrack,
                          final Integer theMsNbr, final String theMsType, final LocalDate theMsDate,
                          final Integer theNbrAtmptsAllow) {

        super(theTermKey);

        this.stuId = theStuId;
        this.paceTrack = thePaceTrack;
        this.msNbr = theMsNbr;
        this.msType = theMsType;
        this.msDate = theMsDate;
        this.nbrAtmptsAllow = theNbrAtmptsAllow;
    }

    /**
     * Gets the pace from the milestone number (the first digit is pace).
     *
     * @return the pace (1 to 5)
     */
    public int getPace() {

        return this.msNbr.intValue() / 100;
    }

    /**
     * Gets the course index from the milestone number (the second digit is index).
     *
     * @return the index (1 to pace)
     */
    public int getIndex() {

        return (this.msNbr.intValue() / 10) % 10;
    }

    /**
     * Gets the unit from the milestone number (the last digit is index).
     *
     * @return the unit (1 to 5)
     */
    public int getUnit() {

        return this.msNbr.intValue() % 10;
    }

    /**
     * Extracts a "stmilestone" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStmilestone fromResultSet(final ResultSet rs) throws SQLException {

        final RawStmilestone result = new RawStmilestone();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
        result.msNbr = getIntegerField(rs, FLD_MS_NBR);
        result.msType = getStringField(rs, FLD_MS_TYPE);
        result.msDate = getDateField(rs, FLD_MS_DATE);
        result.nbrAtmptsAllow = getIntegerField(rs, FLD_NBR_ATMPTS_ALLOW);

        // FIXME: "ext_type" is not supported by 'admin'

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
    public int compareTo(final RawStmilestone o) {

        int result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.paceTrack, o.paceTrack);

            if (result == 0) {
                result = compareAllowingNull(this.msNbr, o.msNbr);

                if (result == 0) {
                    result = compareAllowingNull(this.msDate, this.msDate);

                    if (result == 0) {
                        result = compareAllowingNull(this.msType, o.msType);

                        if (result == 0) {
                            result = compareAllowingNull(this.stuId, o.stuId);
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
        appendField(htm, FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_NBR, this.msNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_TYPE, this.msType);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_DATE, this.msDate);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_ATMPTS_ALLOW, this.nbrAtmptsAllow);

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
                + Objects.hashCode(this.paceTrack)
                + Objects.hashCode(this.msNbr)
                + Objects.hashCode(this.msType)
                + Objects.hashCode(this.msDate)
                + Objects.hashCode(this.nbrAtmptsAllow);
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
        } else if (obj instanceof final RawStmilestone rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.paceTrack, rec.paceTrack)
                    && Objects.equals(this.msNbr, rec.msNbr)
                    && Objects.equals(this.msType, rec.msType)
                    && Objects.equals(this.msDate, rec.msDate)
                    && Objects.equals(this.nbrAtmptsAllow, rec.nbrAtmptsAllow);
        } else {
            equal = false;
        }

        return equal;
    }
}
