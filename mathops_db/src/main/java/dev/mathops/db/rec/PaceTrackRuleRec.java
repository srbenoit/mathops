package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "pace_track_rule" record.
 *
 * <p>
 * A pace track rule can assign students to a pace track within their pace and sub-term (where a student may have only
 * one pace and sub-term).  The combination of the sub-term, pace and pace track sets the student's milestone schedule.
 *
 * <p>
 * Within a sub-term and pace, the set of records present MUST provide an unambiguous pace track assignment for every
 * possible set of student registrations that participate in the student's pace.  There SHOULD be a default rule with
 * a criteria field of '*' to assign students who do not match any other criteria fields.
 */
public final class PaceTrackRuleRec extends RecBase implements Comparable<PaceTrackRuleRec> {

    /** A field name. */
    private static final String FLD_SUBTERM = "subterm";

    /** A field name. */
    private static final String FLD_PACE = "pace";

    /** A field name. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name. */
    private static final String FLD_CRITERIA = "criteria";

    /**
     * The ID of the sub-term.  For example, there could be a "FULL" sub-term that covers the entire term, plus a "LATE"
     * sub-term that begins halfway through for students who satisfy prerequisites late, or who need to add a course
     * mid-semester to compensate for a drop or withdrawal.  Face-to-face sections may have different sub-terms than
     * online, hybrid, or distance sections.  Within each sub-term, there are a set of allowed paces and pace tracks
     * that define milestone schedules.
     */
    public final String subterm;

    /**
     * The student's "pace", which is the number of courses they are working to complete within the sub-term.  The
     * milestone schedule will be generated based on completing this number of courses over the course of the sub-term.
     */
    public final Integer pace;

    /**
     * The "pace track".  Each pace may be divided into multiple "tracks" to try to better balance load on testing
     * centers or tutoring centers.  Each track will likely have different milestone dates than the others.  The
     * decision on which track a student will fall into is based on the "criteria" field contents.
     */
    public final String paceTrack;

    /**
     * The criteria used to assign a student to a track.  At this time, the only selection criterion is the presence
     * of a specific course ID in list of registrations that contribute toward the student's pace.
     * <p>
     * Allowed values are best illustrated by example.  A value of "+MATH 117" would mean a student would be assigned
     * the pace track if their registration list does contain "MATH 117".  A value of "+MATH 117+MATH 118" would match
     * a student that has both "MATH 117" and "MATH 118" in their registration list.  A value of "-MATH 117" would
     * match any student that does NOT have "MATH 117" in their registration list.  A value of '*' represents a
     * "default" rule that assigns a student a pace track who does not match any other record's criteria.
     */
    public final String criteria;

    /**
     * Constructs a new {@code PaceTrackRuleRec}.
     *
     * @param theSubterm   the ID of the sub-term (may not be {@code null})
     * @param thePace      the student's pace (may not be {@code null})
     * @param thePaceTrack the pace track (may not be {@code null})
     * @param theCriteria  the criteria used to assign a student to a track (may not be {@code null})
     */
    public PaceTrackRuleRec(final String theSubterm, final Integer thePace, final String thePaceTrack,
                            final String theCriteria) {

        super();

        if (theSubterm == null) {
            throw new IllegalArgumentException("Subterm may not be null");
        }
        if (thePace == null) {
            throw new IllegalArgumentException("Pace may not be null");
        }
        if (thePaceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (theCriteria == null) {
            throw new IllegalArgumentException("Criteria may not be null");
        }

        this.subterm = theSubterm;
        this.pace = thePace;
        this.paceTrack = thePaceTrack;
        this.criteria = theCriteria;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final PaceTrackRuleRec o) {

        int result = compareAllowingNull(this.subterm, o.subterm);
        if (result == 0) {
            result = compareAllowingNull(this.pace, o.pace);
            if (result == 0) {
                result = compareAllowingNull(this.paceTrack, o.paceTrack);
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

        return this.subterm.hashCode() + this.pace.hashCode() + this.paceTrack.hashCode() + this.criteria.hashCode();
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
        } else if (obj instanceof final PaceTrackRuleRec rec) {
            equal = this.subterm.equals(rec.subterm)
                    && this.pace.equals(rec.pace)
                    && this.paceTrack.equals(rec.paceTrack)
                    && this.criteria.equals(rec.criteria);
        } else {
            equal = false;
        }

        return equal;
    }
}
