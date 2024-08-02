package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.type.TermKey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "pace_track_rule" record.
 */
public final class RawPaceTrackRule extends RawTermRecordBase implements Comparable<RawPaceTrackRule> {

    /** The table name. */
    public static final String TABLE_NAME = "pace_track_rule";

    /** A field name. */
    private static final String FLD_SUBTERM = "subterm";

    /** A field name. */
    private static final String FLD_PACE = "pace";

    /** A field name. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name. */
    private static final String FLD_CRITERIA = "criteria";

    /** The 'subterm' field value. */
    public String subterm;

    /** The 'pace' field value. */
    public Integer pace;

    /** The 'pace_track' field value. */
    public String paceTrack;

    /** The 'criteria' field value. */
    public String criteria;

    /**
     * Constructs a new {@code RawPaceTrackRule}.
     */
    private RawPaceTrackRule() {

        super();
    }

    /**
     * Constructs a new {@code RawPaceTrackRule}.
     *
     * @param theTermKey   the term key
     * @param theSubterm   the subterm
     * @param thePace      the pace
     * @param thePaceTrack the pace track
     * @param theCriteria  the criteria ("DEFAULT" or a comma-separated list of course IDs)
     */
    public RawPaceTrackRule(final TermKey theTermKey, final String theSubterm,
                            final Integer thePace, final String thePaceTrack, final String theCriteria) {

        super(theTermKey);

        this.subterm = theSubterm;
        this.pace = thePace;
        this.paceTrack = thePaceTrack;
        this.criteria = theCriteria;
    }

    /**
     * Extracts a "pace_track_rule" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPaceTrackRule fromResultSet(final ResultSet rs) throws SQLException {

        final RawPaceTrackRule result = new RawPaceTrackRule();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.subterm = getStringField(rs, FLD_SUBTERM);
        result.pace = getIntegerField(rs, FLD_PACE);
        result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
        result.criteria = getStringField(rs, FLD_CRITERIA);

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
    public int compareTo(final RawPaceTrackRule o) {

        int result = this.termKey.compareTo(o.termKey);

        if (result == 0) {
            result = this.subterm.compareTo(o.subterm);
            if (result == 0) {
                result = this.pace.compareTo(o.pace);
                if (result == 0) {
                    result = this.paceTrack.compareTo(o.paceTrack);
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
        appendField(htm, FLD_SUBTERM, this.subterm);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_CRITERIA, this.criteria);

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
                + Objects.hashCode(this.subterm)
                + Objects.hashCode(this.pace)
                + Objects.hashCode(this.paceTrack)
                + Objects.hashCode(this.criteria);
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
        } else if (obj instanceof final RawPaceTrackRule rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.subterm, rec.subterm)
                    && Objects.equals(this.pace, rec.pace)
                    && Objects.equals(this.paceTrack, rec.paceTrack)
                    && Objects.equals(this.criteria, rec.criteria);
        } else {
            equal = false;
        }

        return equal;
    }
}
