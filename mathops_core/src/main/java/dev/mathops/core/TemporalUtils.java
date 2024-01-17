package dev.mathops.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilities that provide common operations on LocalDate, LocalTime, and LocalDateTime values.
 */
public enum TemporalUtils {
    ;

    // NOTE: DateTimeFormatters created from patterns are immutable and thread-safe

    /** A date format with month, day. */
    public static final DateTimeFormatter FMT_MD = DateTimeFormatter.ofPattern("MMM d", Locale.US);

    /** A date format with month, period, and day. */
    public static final DateTimeFormatter FMT_MPD = DateTimeFormatter.ofPattern("MMM'.' d", Locale.US);

    /** A date format with month, day, year. */
    public static final DateTimeFormatter FMT_MDY = DateTimeFormatter.ofPattern("MMM d',' yyyy", Locale.US);

    /** A compact date format with month, day, year. */
    public static final DateTimeFormatter FMT_MDY_COMPACT = DateTimeFormatter.ofPattern("M/d/yy", Locale.US);

    /** A compact date format with month, day, year in a fixed-width format. */
    public static final DateTimeFormatter FMT_MDY_COMPACT_FIXED = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);

    /** A date format with weekday, month, day, year. */
    public static final DateTimeFormatter FMT_WMDY = DateTimeFormatter.ofPattern("EEE',' MMM d',' yyyy", Locale.US);

    /** A date format with weekday, month, day. */
    public static final DateTimeFormatter FMT_WMD = DateTimeFormatter.ofPattern("EEE',' MMM d", Locale.US);

    /** A date format with long weekday, month, day. */
    public static final DateTimeFormatter FMT_WMD_LONG = DateTimeFormatter.ofPattern("EEEE',' MMMM d", Locale.US);

    /** A date/time format with full date and time down to the second. */
    public static final DateTimeFormatter FMT_MDY_AT_HMS_A =
            DateTimeFormatter.ofPattern("MMM d',' yyyy 'at' hh':'mm':'ss a", Locale.US);

    /** A date/time format with full date and time down to the minute. */
    public static final DateTimeFormatter FMT_MDY_AT_HM_A =
            DateTimeFormatter.ofPattern("MMM d',' yyyy 'at' hh':'mm a", Locale.US);

    /** A date/time format with weekday and full date and time down to the minute. */
    public static final DateTimeFormatter FMT_WMDY_AT_HM_A = //
            DateTimeFormatter.ofPattern("EEEE',' MMMM d',' yyyy 'at' h':'mm a", Locale.US);

    /** A time format with time down to the second. */
    public static final DateTimeFormatter FMT_HMS_A = DateTimeFormatter.ofPattern("hh':'mm':'ss a", Locale.US);

    /** A time format with time down to the minute. */
    public static final DateTimeFormatter FMT_HM_A = DateTimeFormatter.ofPattern("hh':'mm a", Locale.US);

    /** A date/time format with full date and time down to the second. */
    public static final DateTimeFormatter FMT_MDY_HMS = DateTimeFormatter.ofPattern("MMM dd yyyy',' HH':'mm':'ss",
            Locale.US);

    /** A date/time format that matches that expected by Informix. */
    public static final DateTimeFormatter FMT_INFORMIX = DateTimeFormatter.ofPattern("MMddyy", Locale.US);

    /**
     * Gets a local date/time represented by a Java (long) timestamp in the system default time zone.
     *
     * @param timestamp the timestamp
     * @return the local date/time
     */
    public static LocalDateTime toLocalDateTime(final long timestamp) {

        return toLocalDateTime(Instant.ofEpochMilli(timestamp));
    }

    /**
     * Gets a local date/time represented by an {@code Instant} in the system default time zone.
     *
     * @param instant the instant (converted to a LocalDate in the system default time zone for conversion to a day
     *                number)
     * @return the local date/time
     */
    public static LocalDateTime toLocalDateTime(final Instant instant) {

        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Generates a local date/time from a local date and a number of minutes past midnight.
     *
     * @param date    the date
     * @param minutes the number of minutes past midnight
     * @return the local date/time
     */
    public static LocalDateTime toLocalDateTime(final LocalDate date, final Integer minutes) {

        LocalDate actualDate = date;
        int min = minutes.intValue();

        if (min < 0) {
            actualDate = actualDate.minusDays(1L);
            min += 1440;
        } else if (min >= 1440) {
            actualDate = actualDate.plusDays(1L);
            min -= 1440;
        }

        return LocalDateTime.of(actualDate, LocalTime.of(min / 60, min % 60));
    }

    /**
     * Gets the minute within a day represented by a local date/time. Seconds and partial seconds are discarded, so a
     * time of "01:23:45.678" returns a value of 83 (1 * 60 + 23).
     *
     * @param time the local time
     * @return the minute of the day (0 to 1,439)
     */
    public static int minuteOfDay(final LocalDateTime time) {

        return time.getHour() * 60 + time.getMinute();
    }

    /**
     * Gets the minute within a day represented by a local time. Seconds and partial seconds are discarded, so a time of
     * "01:23:45.678" returns a value of 83 (1 * 60 + 23).
     *
     * @param time the local time
     * @return the minute of the day (0 to 1,439)
     */
    public static int minuteOfDay(final LocalTime time) {

        return time.getHour() * 60 + time.getMinute();
    }

    /**
     * Gets the second within a day represented by a local time. Partial seconds are discarded, so a time of
     * "01:23:45.678" returns a value of 5025 (1 * 3600 + 23 * 60 + 45).
     *
     * @param time the local time
     * @return the minute of the day (0 to 86,399)
     */
    public static int secondOfDay(final LocalTime time) {

        return time.getHour() * 3600 + time.getMinute() * 60 + time.getSecond();
    }
}
