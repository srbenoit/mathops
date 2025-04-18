package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * An immutable raw "facility_closure" record.
 *
 * <p>
 * Each record represents a facility closure.  Closures are singular events that disrupt the regular schedule as
 * described in "facility_hours".  A closure may be a full day or stretch of days, like a holiday or semester break (in
 * which case start and end time are null), or may be some span of time within a day, like an afternoon weather-related
 * closure.
 *
 * <p>
 * Each row has a "display_index" field that controls the order in which schedule information is presented to users. The
 * primary key on the underlying table is the combination of the facility ID and display index, so there should be no
 * ambiguity in display order.
 */
public final class FacilityClosureRec extends RecBase implements Comparable<FacilityClosureRec> {

    /** A defined closure type. */
    public static final String HOLIDAY = "HOLIDAY";

    /** A defined closure type. */
    public static final String SP_BREAK = "SP_BREAK";

    /** A defined closure type. */
    public static final String FA_BREAK = "FA_BREAK";

    /** A defined closure type. */
    public static final String WEATHER = "WEATHER";

    /** A defined closure type. */
    public static final String EMERGENCY = "EMERGENCY";

    /** A defined closure type. */
    public static final String MAINT = "MAINT";

    /** A defined closure type. */
    public static final String EVENT = "EVENT";

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "facility_closure";

    /** The 'facility_id' field value. */
    public final String facilityId;

    /** The 'start_date' field value (the first date of the closure) */
    public final LocalDate startDate;

    /** The 'end_date' field value (the last date of the closure). */
    public final LocalDate endDate;

    /** The 'closure_type' field value. */
    public final String closureType;

    /** The 'start_time' field value. */
    public final LocalTime startTime;

    /** The 'end_time' field value. */
    public final LocalTime endTime;

    /**
     * Constructs a new {@code FacilityClosureRec}.
     *
     * @param theFacilityId  the facility ID
     * @param theStartDate   the closure start date (the first day of the closure)
     * @param theEndDate     the closure end date (the last date of the closure)
     * @param theClosureType the closure type
     * @param theStartTime   the start time (null for a full-day closure)
     * @param theEndTime     the end time (null for a full-day closure)
     */
    public FacilityClosureRec(final String theFacilityId, final LocalDate theStartDate, final LocalDate theEndDate,
                              final String theClosureType, final LocalTime theStartTime, final LocalTime theEndTime) {

        super();

        if (theFacilityId == null) {
            throw new IllegalArgumentException("Facility ID may not be null");
        }
        if (theStartDate == null) {
            throw new IllegalArgumentException("Closure start date may not be null");
        }
        if (theEndDate == null) {
            throw new IllegalArgumentException("Closure end date may not be null");
        }
        if (theClosureType == null) {
            throw new IllegalArgumentException("Closure type may not be null");
        }

        this.facilityId = theFacilityId;
        this.startDate = theStartDate;
        this.endDate = theEndDate;
        this.closureType = theClosureType;
        this.startTime = theStartTime;
        this.endTime = theEndTime;
    }

    /**
     * Compares two records for order.  Order is based on facility ID, closure date, then closure type.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final FacilityClosureRec o) {

        int result = this.facilityId.compareTo(o.facilityId);

        if (result == 0) {
            result = this.startDate.compareTo(o.startDate);
            if (result == 0) {
                result = this.closureType.compareTo(o.closureType);
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

        appendField(htm, DataDict.FLD_FACILITY_ID, this.facilityId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_START_DATE, this.startDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_END_DATE, this.endDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CLOSURE_TYPE, this.closureType);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_START_TIME, this.startTime);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_END_TIME, this.endTime);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.facilityId.hashCode()
               + this.startDate.hashCode()
               + this.endDate.hashCode()
               + this.closureType.hashCode()
               + Objects.hashCode(this.startTime)
               + Objects.hashCode(this.endTime);
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
        } else if (obj instanceof final FacilityClosureRec rec) {
            equal = this.facilityId.equals(rec.facilityId)
                    && this.startDate.equals(rec.startDate)
                    && this.endDate.equals(rec.endDate)
                    && this.closureType.equals(rec.closureType)
                    && Objects.equals(this.startTime, rec.startTime)
                    && Objects.equals(this.endTime, rec.endTime);
        } else {
            equal = false;
        }

        return equal;
    }
}
