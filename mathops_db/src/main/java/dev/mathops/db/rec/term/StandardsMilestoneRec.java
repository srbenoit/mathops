package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;

/**
 * An immutable raw "standard milestone" record.
 *
 * <p>
 * Each record represents a milestone date for one standards-based course milestone in a particular pace, pace track,
 * and pace index.
 *
 * <p>
 * The primary key on the underlying table is the pace track, pace, pace index, module number, and milestone type.
 */
public final class StandardsMilestoneRec extends RecBase implements Comparable<StandardsMilestoneRec> {

    /** A milestone type - the due date for mastery of a standard to earn "on-time" points. */
    public static final String MS_TYPE_MASTERY = "MA";

    /** A milestone type - the due date for the course to be complete. */
    public static final String MS_TYPE_COMPLETE = "COMP";

    /** A milestone type - the date the "pre-course" survey opens. */
    public static final String MS_TYPE_PRE_SURVEY_OPEN = "PREO";

    /** A milestone type - the date the "pre-course" survey closes. */
    public static final String MS_TYPE_PRE_SURVEY_CLOSE = "PREC";

    /** A milestone type - the date the "pre-course" survey opens. */
    public static final String MS_TYPE_POST_SURVEY_OPEN = "SRVO";

    /** A milestone type - the date the "pre-course" survey closes. */
    public static final String MS_TYPE_POST_SURVEY_CLOSE = "SRVC";

    /** The table name. */
    public static final String TABLE_NAME = "standards_milestone";

    /** The 'pace_track' field value. */
    public final String paceTrack;

    /** The 'pace' field value. */
    public final Integer pace;

    /** The 'pace_index' field value. */
    public final Integer paceIndex;

    /** The 'module_nbr' field value. */
    public final Integer moduleNbr;

    /** The 'ms_type' field value. */
    public final String msType;

    /** The 'ms_date' field value. */
    public final LocalDate msDate;

    /**
     * Constructs a new {@code StandardsMilestoneRec}.
     *
     * @param thePaceTrack the pace track
     * @param thePace      the pace
     * @param thePaceIndex the pace index
     * @param theModuleNbr the module number
     * @param theMsType    the milestone type
     * @param theMsDate    the milestone date
     */
    public StandardsMilestoneRec(final String thePaceTrack, final Integer thePace, final Integer thePaceIndex,
                                 final Integer theModuleNbr, final String theMsType, final LocalDate theMsDate) {

        super();

        if (thePaceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (thePace == null) {
            throw new IllegalArgumentException("Pace may not be null");
        }
        if (thePaceIndex == null) {
            throw new IllegalArgumentException("Pace index may not be null");
        }
        if (theModuleNbr == null) {
            throw new IllegalArgumentException("Module number may not be null");
        }
        if (theMsType == null) {
            throw new IllegalArgumentException("Milestone type may not be null");
        }
        if (theMsDate == null) {
            throw new IllegalArgumentException("Milestone date may not be null");
        }

        this.paceTrack = thePaceTrack;
        this.pace = thePace;
        this.paceIndex = thePaceIndex;
        this.moduleNbr = theModuleNbr;
        this.msType = theMsType;
        this.msDate = theMsDate;
    }

    /**
     * Compares two records for order.  Order is based on pace track, pace, pace index, module number, then milestone
     * type.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardsMilestoneRec o) {

        int result = compareAllowingNull(this.paceTrack, o.paceTrack);

        if (result == 0) {
            result = compareAllowingNull(this.pace, o.pace);
            if (result == 0) {
                result = compareAllowingNull(this.paceIndex, o.paceIndex);
                if (result == 0) {
                    result = compareAllowingNull(this.moduleNbr, o.moduleNbr);
                    if (result == 0) {
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

        appendField(htm, DataDict.FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PACE_INDEX, this.paceIndex);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MODULE_NBR, this.moduleNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MS_TYPE, this.msType);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MS_DATE, this.msDate);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.paceTrack.hashCode()
               + this.pace.hashCode()
               + this.paceIndex.hashCode()
               + this.moduleNbr.hashCode()
               + this.msType.hashCode()
               + this.msDate.hashCode();
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
        } else if (obj instanceof final StandardsMilestoneRec rec) {
            equal = this.paceTrack.equals(rec.paceTrack)
                    && this.pace.equals(rec.pace)
                    && this.paceIndex.equals(rec.paceIndex)
                    && this.moduleNbr.equals(rec.moduleNbr)
                    && this.msType.equals(rec.msType)
                    && this.msDate.equals(rec.msDate);
        } else {
            equal = false;
        }

        return equal;
    }
}
