package dev.mathops.db.rec;


import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A "standard milestone" record.
 */
public final class StandardMilestoneRec extends RecBase implements Comparable<StandardMilestoneRec> {

    /** The table name. */
    public static final String TABLE_NAME = "std_milestone";

    /** A possible value for 'ms_type' field (indicating due date for a standard assignment). */
    public static final String MS_TYPE_STD_ASSIGNMENT = "HW";

    /** A possible value for 'ms_type' field (indicating due date for standard mastery). */
    public static final String MS_TYPE_STD_MASTERY = "MA";

    /** A possible value for 'ms_type' field (indicating an overall course deadline). */
    public static final String MS_TYPE_COURSE_DEADLINE = "CD";

    /** A field name for serialization of records. */
    private static final String FLD_PACE_TRACK = "pace_track";

    /** A field name for serialization of records. */
    private static final String FLD_PACE = "pace";

    /** A field name for serialization of records. */
    private static final String FLD_PACE_INDEX = "pace_index";

    /** A field name for serialization of records. */
    private static final String FLD_UNIT = "unit";

    /** A field name for serialization of records. */
    private static final String FLD_OBJECTIVE = "objective";

    /** A field name for serialization of records. */
    private static final String FLD_MS_TYPE = "ms_type";

    /** A field name for serialization of records. */
    private static final String FLD_MS_DATE = "ms_date";

    /** The 'pace_track' field value. */
    public String paceTrack;

    /** The 'pace' field value. */
    public Integer pace;

    /** The 'pace_index' field value. */
    public Integer paceIndex;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'objective' field value. */
    public Integer objective;

    /** The 'ms_type' field value. */
    public String msType;

    /** The 'ms_date' field value. */
    public LocalDate msDate;

    /**
     * Constructs a new {@code StandardMilestoneRec}.
     */
    public StandardMilestoneRec() {

        super();
    }

    /**
     * Constructs a new {@code StandardMilestoneRec}.
     *
     * @param thePaceTrack the pace track
     * @param thePace      the pace
     * @param thePaceIndex the pace index
     * @param theUnit      the unit
     * @param theObjective the objective
     * @param theMsType    the milestone type
     * @param theMsDate    the milestone date
     */
    public StandardMilestoneRec(final String thePaceTrack, final Integer thePace,
                                final Integer thePaceIndex, final Integer theUnit, final Integer theObjective,
                                final String theMsType, final LocalDate theMsDate) {

        super();

        this.paceTrack = thePaceTrack;
        this.pace = thePace;
        this.paceIndex = thePaceIndex;
        this.unit = theUnit;
        this.objective = theObjective;
        this.msType = theMsType;
        this.msDate = theMsDate;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardMilestoneRec o) {

        int result = compareAllowingNull(this.paceTrack, o.paceTrack);

        if (result == 0) {
            result = compareAllowingNull(this.pace, o.pace);
            if (result == 0) {
                result = compareAllowingNull(this.paceIndex, o.paceIndex);
                if (result == 0) {
                    result = compareAllowingNull(this.unit, o.unit);
                    if (result == 0) {
                        result = compareAllowingNull(this.paceIndex, o.paceIndex);
                        if (result == 0) {
                            result = compareAllowingNull(this.msType, o.msType);
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

        appendField(htm, FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_INDEX, this.paceIndex);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_OBJECTIVE, this.objective);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_TYPE, this.msType);
        htm.add(DIVIDER);
        appendField(htm, FLD_MS_DATE, this.msDate);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.paceTrack) //
                + Objects.hashCode(this.pace) //
                + Objects.hashCode(this.paceIndex) //
                + Objects.hashCode(this.unit) //
                + Objects.hashCode(this.objective) //
                + Objects.hashCode(this.msType) //
                + Objects.hashCode(this.msDate);
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
        } else if (obj instanceof final StandardMilestoneRec rec) {
            equal = Objects.equals(this.paceTrack, rec.paceTrack)
                    && Objects.equals(this.pace, rec.pace)
                    && Objects.equals(this.paceIndex, rec.paceIndex)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.objective, rec.objective)
                    && Objects.equals(this.msType, rec.msType)
                    && Objects.equals(this.msDate, rec.msDate);
        } else {
            equal = false;
        }

        return equal;
    }
}
