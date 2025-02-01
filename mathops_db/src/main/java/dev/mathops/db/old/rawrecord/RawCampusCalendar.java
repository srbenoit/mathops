package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "campus_calendar" record.
 */
public final class RawCampusCalendar extends RecBase implements Comparable<RawCampusCalendar> {

    /** The table name. */
    public static final String TABLE_NAME = "campus_calendar";

    /** A field name. */
    private static final String FLD_CAMPUS_DT = "campus_dt";

    /** A field name. */
    private static final String FLD_DT_DESC = "dt_desc";

    /** A field name. */
    private static final String FLD_OPEN_TIME1 = "open_time1";

    /** A field name. */
    private static final String FLD_OPEN_TIME2 = "open_time2";

    /** A field name. */
    private static final String FLD_CLOSE_TIME1 = "close_time1";

    /** A field name. */
    private static final String FLD_CLOSE_TIME2 = "close_time2";

    /** A field name. */
    private static final String FLD_WEEKDAYS_1 = "weekdays_1";

    /** A field name. */
    private static final String FLD_WEEKDAYS_2 = "weekdays_2";

    /** Start of the first interval of "open" days. */
    public static final String DT_DESC_START_DATE_1 = "start_dt1";

    /** Start of the second interval of "open" days. */
    public static final String DT_DESC_START_DATE_2 = "start_dt2";

    /** End of the first interval of "open" days. */
    public static final String DT_DESC_END_DATE_1 = "end_dt1";

    /** End of the second interval of "open" days. */
    public static final String DT_DESC_END_DATE_2 = "end_dt2";

    /** Start of the first interval of "open" days for the next term. */
    public static final String DT_DESC_START_DATE_1_NEXT = "start_dt1x";

    /** End of the first interval of "open" days for the next term. */
    public static final String DT_DESC_END_DATE_1_NEXT = "end_dt1x";

    /** A holiday. */
    public static final String DT_DESC_HOLIDAY = "holiday";

    /** The last date the bookstore will accept returns of e-texts. */
    public static final String DT_DESC_BOOKSTORE = "bookstore";

    /** The first date tutorial sites will be open. */
    public static final String DT_DESC_TUT_START = "tut_start";

    /** The last date tutorial sites will be open. */
    public static final String DT_DESC_TUT_END = "tut_end";

    /** When tutorial information becomes available. */
    public static final String DT_DESC_TUT_INFO = "tut_info";
    /** Walk-in placement exam session. */
    public static final String DT_DESC_WALKIN_PLACEMENT = "walk_in";

    /** First date of the first upcoming placement website maintenance window. */
    public static final String DT_DESC_PLACEMENT_MAINT_START1 = "plc_maint_start1";

    /** Last date of the first upcoming placement website maintenance window. */
    public static final String DT_DESC_PLACEMENT_MAINT_END1 = "plc_maint_end1";

    /** First date of the second upcoming placement website maintenance window. */
    public static final String DT_DESC_PLACEMENT_MAINT_START2 = "plc_maint_start2";

    /** Last date of the second upcoming placement website maintenance window. */
    public static final String DT_DESC_PLACEMENT_MAINT_END2 = "plc_maint_end2";

    /** First date of the third upcoming placement website maintenance window. */
    public static final String DT_DESC_PLACEMENT_MAINT_START3 = "plc_maint_start3";

    /** Last date of the third upcoming placement website maintenance window. */
    public static final String DT_DESC_PLACEMENT_MAINT_END3 = "plc_maint_end3";

    /** The 'campus_dt' field value. */
    public LocalDate campusDt;

    /** The 'dt_desc' field value. */
    public String dtDesc;

    /** The 'open_time1' field value. */
    public String openTime1;

    /** The 'open_time2' field value. */
    public String openTime2;

    /** The 'close_time1' field value. */
    public String closeTime1;

    /** The 'close_time2' field value. */
    public String closeTime2;

    /** The 'weekdays_1' field value. */
    public String weekdays1;

    /** The 'weekdays_2' field value. */
    public String weekdays2;

    /**
     * Constructs a new {@code RawCampusCalendar}.
     */
    private RawCampusCalendar() {

        super();
    }

    /**
     * Constructs a new {@code RawCampusCalendar}.
     *
     * @param theCampusDt   the date
     * @param theDtDesc     the date type descriptor
     * @param theOpenTime1  the first open time
     * @param theOpenTime2  the second open time
     * @param theCloseTime1 the first close time
     * @param theCloseTime2 the second close time
     * @param theWeekdays1  the weekdays of the first open period
     * @param theWeekdays2  the weekdays of the second open period
     */
    public RawCampusCalendar(final LocalDate theCampusDt, final String theDtDesc,
                             final String theOpenTime1, final String theOpenTime2, final String theCloseTime1,
                             final String theCloseTime2, final String theWeekdays1, final String theWeekdays2) {
        super();

        this.campusDt = theCampusDt;
        this.dtDesc = theDtDesc;
        this.openTime1 = theOpenTime1;
        this.openTime2 = theOpenTime2;
        this.closeTime1 = theCloseTime1;
        this.closeTime2 = theCloseTime2;
        this.weekdays1 = theWeekdays1;
        this.weekdays2 = theWeekdays2;
    }

    /**
     * Gets the number of times (0 if none of the open/close times are present, 1 if times 1 are present but not times
     * 2, and 2 if all are present)
     *
     * @return the number of times
     */
    public int numTimes() {

        final int result;

        if (this.openTime1 == null || this.closeTime1 == null || this.weekdays1 == null) {
            result = 0;
        } else if (this.openTime2 == null || this.closeTime2 == null || this.weekdays2 == null) {
            result = 1;
        } else {
            result = 2;
        }

        return result;
    }

    /**
     * Extracts a "testing_centers" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCampusCalendar fromResultSet(final ResultSet rs) throws SQLException {

        final RawCampusCalendar result = new RawCampusCalendar();

        result.campusDt = getDateField(rs, FLD_CAMPUS_DT);
        result.dtDesc = getStringField(rs, FLD_DT_DESC);
        result.openTime1 = getStringField(rs, FLD_OPEN_TIME1);
        result.openTime2 = getStringField(rs, FLD_OPEN_TIME2);
        result.closeTime1 = getStringField(rs, FLD_CLOSE_TIME1);
        result.closeTime2 = getStringField(rs, FLD_CLOSE_TIME2);
        result.weekdays1 = getStringField(rs, FLD_WEEKDAYS_1);
        result.weekdays2 = getStringField(rs, FLD_WEEKDAYS_2);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to which to compare
     * @return a negative value, 0, or a positive value as this object is less than, equal to, or greater than
     *         {@code o}.
     */
    @Override
    public int compareTo(final RawCampusCalendar o) {

        int result = compareAllowingNull(this.campusDt, o.campusDt);

        if (result == 0) {
            result = compareAllowingNull(this.dtDesc, o.dtDesc);
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

        appendField(htm, FLD_CAMPUS_DT, this.campusDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_DT_DESC, this.dtDesc);
        htm.add(DIVIDER);
        appendField(htm, FLD_OPEN_TIME1, this.openTime1);
        htm.add(DIVIDER);
        appendField(htm, FLD_OPEN_TIME2, this.openTime2);
        htm.add(DIVIDER);
        appendField(htm, FLD_CLOSE_TIME1, this.closeTime1);
        htm.add(DIVIDER);
        appendField(htm, FLD_CLOSE_TIME2, this.closeTime2);
        htm.add(DIVIDER);
        appendField(htm, FLD_WEEKDAYS_1, this.weekdays1);
        htm.add(DIVIDER);
        appendField(htm, FLD_WEEKDAYS_2, this.weekdays2);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.campusDt)
                + Objects.hashCode(this.dtDesc)
                + Objects.hashCode(this.openTime1)
                + Objects.hashCode(this.openTime2)
                + Objects.hashCode(this.closeTime1)
                + Objects.hashCode(this.closeTime2)
                + Objects.hashCode(this.weekdays1)
                + Objects.hashCode(this.weekdays2);
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
        } else if (obj instanceof final RawCampusCalendar rec) {
            equal = Objects.equals(this.campusDt, rec.campusDt)
                    && Objects.equals(this.dtDesc, rec.dtDesc)
                    && Objects.equals(this.openTime1, rec.openTime1)
                    && Objects.equals(this.openTime2, rec.openTime2)
                    && Objects.equals(this.closeTime1, rec.closeTime1)
                    && Objects.equals(this.closeTime2, rec.closeTime2)
                    && Objects.equals(this.weekdays1, rec.weekdays1)
                    && Objects.equals(this.weekdays2, rec.weekdays2);
        } else {
            equal = false;
        }

        return equal;
    }
}
