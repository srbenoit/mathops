package dev.mathops.db.rec.main;

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

    /** A field name for serialization of records. */
    private static final String FLD_FACILITY = "facility";

    /** A field name for serialization of records. */
    private static final String FLD_DISPLAY_INDEX = "display_index";

    /** A field name for serialization of records. */
    private static final String FLD_WEEKDAYS = "weekdays";

    /** A field name for serialization of records. */
    private static final String FLD_START_DT = "start_dt";

    /** A field name for serialization of records. */
    private static final String FLD_END_DT = "end_dt";

    /** A field name for serialization of records. */
    private static final String FLD_OPEN_TIME_1 = "open_time_1";

    /** A field name for serialization of records. */
    private static final String FLD_CLOSE_TIME_1 = "close_time_1";

    /** A field name for serialization of records. */
    private static final String FLD_OPEN_TIME_2 = "open_time_2";

    /** A field name for serialization of records. */
    private static final String FLD_CLOSE_TIME_2 = "close_time_2";

    /** The 'facility' field value. */
    public final String facility;

    /** The 'display_index' field value. */
    public final Integer displayIndex;

    /** The 'weekdays' field value. */
    public final Integer weekdays;

    /** The 'start_dt' field value. */
    public final LocalDate startDt;

    /** The 'end_dt' field value. */
    public final LocalDate endDt;

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
     * @param theFacility     the facility ID
     * @param theDisplayIndex the display index
     * @param theWeekdays     the weekdays for which this record applies
     * @param theStartDt      the start date
     * @param theEndDt        the end date
     * @param theOpenTime1    the first opening time
     * @param theCloseTime1   the first closing time
     * @param theOpenTime2    the second opening time
     * @param theCloseTime2   the second closing time
     */
    public FacilityHoursRec(final String theFacility, final Integer theDisplayIndex, final Integer theWeekdays,
                            final LocalDate theStartDt, final LocalDate theEndDt,
                            final LocalTime theOpenTime1, final LocalTime theCloseTime1,
                            final LocalTime theOpenTime2, final LocalTime theCloseTime2) {

        super();

        if (theFacility == null) {
            throw new IllegalArgumentException("Facility ID may not be null");
        }
        if (theDisplayIndex == null) {
            throw new IllegalArgumentException("Display index may not be null");
        }
        if (theWeekdays == null) {
            throw new IllegalArgumentException("Weekdays may not be null");
        }
        if (theStartDt == null) {
            throw new IllegalArgumentException("Start date may not be null");
        }
        if (theEndDt == null) {
            throw new IllegalArgumentException("End date may not be null");
        }
        if (theOpenTime1 == null) {
            throw new IllegalArgumentException("First opening time may not be null");
        }
        if (theCloseTime1 == null) {
            throw new IllegalArgumentException("First closing time may not be null");
        }

        this.facility = theFacility;
        this.displayIndex = theDisplayIndex;
        this.weekdays = theWeekdays;
        this.startDt = theStartDt;
        this.endDt = theEndDt;
        this.openTime1 = theOpenTime1;
        this.closeTime1 = theCloseTime1;
        this.openTime2 = theOpenTime2;
        this.closeTime2 = theCloseTime2;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final FacilityHoursRec o) {

        int result = this.facility.compareTo(o.facility);

        if (result == 0) {
            result = this.startDt.compareTo(o.startDt);
            if (result == 0) {
                result = this.endDt.compareTo(o.endDt);
                if (result == 0) {
                    result = this.openTime1.compareTo(o.openTime1);
                    if (result == 0) {
                        result = this.closeTime1.compareTo(o.closeTime1);
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

        appendField(htm, FLD_FACILITY, this.facility);
        htm.add(DIVIDER);
        appendField(htm, FLD_DISPLAY_INDEX, this.displayIndex);
        htm.add(DIVIDER);
        appendField(htm, FLD_WEEKDAYS, this.weekdays);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_DT, this.startDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_END_DT, this.endDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_OPEN_TIME_1, this.openTime1);
        htm.add(DIVIDER);
        appendField(htm, FLD_CLOSE_TIME_1, this.closeTime1);
        htm.add(DIVIDER);
        appendField(htm, FLD_OPEN_TIME_2, this.openTime2);
        htm.add(DIVIDER);
        appendField(htm, FLD_CLOSE_TIME_2, this.closeTime2);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.facility.hashCode()
               + this.displayIndex.hashCode()
               + this.weekdays.hashCode()
               + this.startDt.hashCode()
               + this.endDt.hashCode()
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
            equal = this.facility.equals(rec.facility)
                    && this.displayIndex.equals(rec.displayIndex)
                    && this.weekdays.equals(rec.weekdays)
                    && this.startDt.equals(rec.startDt)
                    && this.endDt.equals(rec.endDt)
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
