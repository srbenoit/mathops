package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * An immutable raw "facility_hours" record.
 *
 * <p>
 * Each record represents a regularly scheduled time when a facility is open or available for use.  It may take several
 * such records to collectively represent the total set of scheduled open times, but in this case, the rows should not
 * overlap (for example, if a facility is open 8AM to 5PM Monday through Friday, but stays open until 6PM on Tuesdays,
 * it should have one record for the 8AM to 5PM times, and a second record for the hour form 5PM to 6PM on Tuesdays.
 *
 * <p>
 * Each row has a "display_index" field that controls the order in which schedule information is presented to users. The
 * primary key on the underlying table is the combination of the facility ID and display index, so there should be no
 * ambiguity in display order.
 */
public final class FacilityHoursRec extends RecBase implements Comparable<FacilityHoursRec> {

    /** Integer weekday constant - 'weekdays' field is a logical OR of these constants. */
    public static final int SUN = 1;

    /** Integer weekday constant - 'weekdays' field is a logical OR of these constants. */
    public static final int MON = 2;

    /** Integer weekday constant - 'weekdays' field is a logical OR of these constants. */
    public static final int TUE = 4;

    /** Integer weekday constant - 'weekdays' field is a logical OR of these constants. */
    public static final int WED = 8;

    /** Integer weekday constant - 'weekdays' field is a logical OR of these constants. */
    public static final int THU = 16;

    /** Integer weekday constant - 'weekdays' field is a logical OR of these constants. */
    public static final int FRI = 32;

    /** Integer weekday constant - 'weekdays' field is a logical OR of these constants. */
    public static final int MON_TO_FRI = MON + TUE + WED + THU + FRI;

    /** Integer weekday constant - 'weekdays' field is a logical OR of these constants. */
    public static final int SAT = 64;

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "facility_hours";

    /** The 'facility_id' field value. */
    public final String facilityId;

    /** The 'display_index' field value. */
    public final Integer displayIndex;

    /** The 'weekdays' field value. */
    public final Integer weekdays;

    /** The 'start_date' field value. */
    public final LocalDate startDate;

    /** The 'end_date' field value. */
    public final LocalDate endDate;

    /** The 'open_time_1' field value. */
    public final LocalTime openTime1;

    /** The 'close_time_1' field value. */
    public final LocalTime closeTime1;

    /** The 'open_time_2' field value. */
    public final LocalTime openTime2;

    /** The 'close_time_2' field value. */
    public final LocalTime closeTime2;

    /**
     * Constructs a new {@code FacilityHoursRec}.
     *
     * @param theFacilityId   the facility ID
     * @param theDisplayIndex the display index
     * @param theWeekdays     the weekdays for which this record applies
     * @param theStartDate    the start date
     * @param theEndDate      the end date
     * @param theOpenTime1    the first opening time
     * @param theCloseTime1   the first closing time
     * @param theOpenTime2    the second opening time
     * @param theCloseTime2   the second closing time
     */
    public FacilityHoursRec(final String theFacilityId, final Integer theDisplayIndex, final Integer theWeekdays,
                            final LocalDate theStartDate, final LocalDate theEndDate,
                            final LocalTime theOpenTime1, final LocalTime theCloseTime1,
                            final LocalTime theOpenTime2, final LocalTime theCloseTime2) {

        super();

        if (theFacilityId == null) {
            throw new IllegalArgumentException("Facility ID may not be null");
        }
        if (theDisplayIndex == null) {
            throw new IllegalArgumentException("Display index may not be null");
        }
        if (theWeekdays == null) {
            throw new IllegalArgumentException("Weekdays may not be null");
        }
        if (theStartDate == null) {
            throw new IllegalArgumentException("Start date may not be null");
        }
        if (theEndDate == null) {
            throw new IllegalArgumentException("End date may not be null");
        }
        if (theOpenTime1 == null) {
            throw new IllegalArgumentException("First opening time may not be null");
        }
        if (theCloseTime1 == null) {
            throw new IllegalArgumentException("First closing time may not be null");
        }

        this.facilityId = theFacilityId;
        this.displayIndex = theDisplayIndex;
        this.weekdays = theWeekdays;
        this.startDate = theStartDate;
        this.endDate = theEndDate;
        this.openTime1 = theOpenTime1;
        this.closeTime1 = theCloseTime1;
        this.openTime2 = theOpenTime2;
        this.closeTime2 = theCloseTime2;
    }

    /**
     * Compares two records for order.  Order is based on facility ID and display index.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final FacilityHoursRec o) {

        int result = this.facilityId.compareTo(o.facilityId);

        if (result == 0) {
            result = this.displayIndex.compareTo(o.displayIndex);
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
        appendField(htm, DataDict.FLD_DISPLAY_INDEX, this.displayIndex);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_WEEKDAYS, this.weekdays);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_START_DATE, this.startDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_END_DATE, this.endDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_OPEN_TIME_1, this.openTime1);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CLOSE_TIME_1, this.closeTime1);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_OPEN_TIME_2, this.openTime2);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CLOSE_TIME_2, this.closeTime2);

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
               + this.displayIndex.hashCode()
               + this.weekdays.hashCode()
               + this.startDate.hashCode()
               + this.endDate.hashCode()
               + this.openTime1.hashCode()
               + this.closeTime1.hashCode()
               + Objects.hashCode(this.openTime2)
               + Objects.hashCode(this.closeTime2);
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
        } else if (obj instanceof final FacilityHoursRec rec) {
            equal = this.facilityId.equals(rec.facilityId)
                    && this.displayIndex.equals(rec.displayIndex)
                    && this.weekdays.equals(rec.weekdays)
                    && this.startDate.equals(rec.startDate)
                    && this.endDate.equals(rec.endDate)
                    && this.openTime1.equals(rec.openTime1)
                    && this.closeTime1.equals(rec.closeTime1)
                    && Objects.equals(this.openTime2, rec.openTime2)
                    && Objects.equals(this.closeTime2, rec.closeTime2);
        } else {
            equal = false;
        }

        return equal;
    }
}
