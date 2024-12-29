package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "stterm" record.
 */
public final class RawStterm extends RawTermRecordBase {

    /** The table name. */
    public static final String TABLE_NAME = "stterm";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_PACE = "pace";

    /** A field name. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name. */
    private static final String FLD_FIRST_COURSE = "first_course";

    /** A field name. */
    private static final String FLD_COHORT = "cohort";

    /** A field name. */
    private static final String FLD_URGENCY = "urgency";

    /** A field name. */
    private static final String FLD_DO_NOT_DISTURB = "do_not_disturb";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'pace' field value. */
    public Integer pace;

    /** The 'pace_track' field value. */
    public String paceTrack;

    /** The 'first_course' field value. */
    public String firstCourse;

    /** The 'cohort' field value. */
    public String cohort;

    /** The 'urgency' field value. */
    public Integer urgency;

    /** The 'do_not_disturb' field value. */
    public String doNotDisturb;

    /**
     * Constructs a new {@code RawStterm}.
     */
    private RawStterm() {

        super();
    }

    /**
     * Constructs a new {@code RawStterm}.
     *
     * @param theTermKey      the term key
     * @param theStuId        the student ID
     * @param thePace         the pace
     * @param thePaceTrack    the pace track
     * @param theFirstCourse  the first course
     * @param theCohort       the assigned cohort
     * @param theUrgency      the urgency
     * @param theDoNotDisturb the do-not-disturb setting
     */
    public RawStterm(final TermKey theTermKey, final String theStuId, final Integer thePace,
                     final String thePaceTrack, final String theFirstCourse, final String theCohort,
                     final Integer theUrgency, final String theDoNotDisturb) {

        super(theTermKey);

        this.stuId = theStuId;
        this.pace = thePace;
        this.paceTrack = thePaceTrack;
        this.firstCourse = theFirstCourse;
        this.cohort = theCohort;
        this.urgency = theUrgency;
        this.doNotDisturb = theDoNotDisturb;
    }

    /**
     * Extracts a "stterm" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStterm fromResultSet(final ResultSet rs) throws SQLException {

        final RawStterm result = new RawStterm();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.stuId = getStringField(rs, FLD_STU_ID);
        result.pace = getIntegerField(rs, FLD_PACE);
        result.paceTrack = getStringField(rs, FLD_PACE_TRACK);
        result.firstCourse = getStringField(rs, FLD_FIRST_COURSE);
        result.cohort = getStringField(rs, FLD_COHORT);
        result.urgency = getIntegerField(rs, FLD_URGENCY);
        result.doNotDisturb = getStringField(rs, FLD_DO_NOT_DISTURB);

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
        appendField(htm, FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_FIRST_COURSE, this.firstCourse);
        htm.add(DIVIDER);
        appendField(htm, FLD_COHORT, this.cohort);
        htm.add(DIVIDER);
        appendField(htm, FLD_URGENCY, this.urgency);
        htm.add(DIVIDER);
        appendField(htm, FLD_DO_NOT_DISTURB, this.doNotDisturb);

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
                + Objects.hashCode(this.pace)
                + Objects.hashCode(this.paceTrack)
                + Objects.hashCode(this.firstCourse)
                + Objects.hashCode(this.cohort)
                + Objects.hashCode(this.urgency)
                + Objects.hashCode(this.doNotDisturb);
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
        } else if (obj instanceof final RawStterm rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.pace, rec.pace)
                    && Objects.equals(this.paceTrack, rec.paceTrack)
                    && Objects.equals(this.firstCourse, rec.firstCourse)
                    && Objects.equals(this.cohort, rec.cohort)
                    && Objects.equals(this.urgency, rec.urgency)
                    && Objects.equals(this.doNotDisturb, rec.doNotDisturb);
        } else {
            equal = false;
        }

        return equal;
    }
}
