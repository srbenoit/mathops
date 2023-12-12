package dev.mathops.db.old;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Database utilities.
 */
public enum DbUtils {
    ;

    /** The lowest three-digit number. */
    private static final int MAX_TWO_DIGIT = 99;

    /** The last 2-digit year we interpret as being in the 20th century. */
    private static final int LAST_1900_YEAR = 80;

    /** First year of the 20th century. */
    private static final int CENTURY_20 = 1900;

    /** First year of the 21st century. */
    private static final int CENTURY_21 = 2000;

    //    /**
//     * Extracts a named boolean value from a result set. The underlying data field is assumed to be character data,
//     * containing either "Y/y/N/n" or "T/t/F/f" or "1/0" as its first character.
//     *
//     * @param rs      the {@code ResultSet} from which to extract the value
//     * @param fieldName the name of the value to extract
//     * @return the boolean value, or {@code null} if the value was NULL
//     * @throws SQLException if an exception occurs extracting the value
//     */
//    public static Boolean getBoolean(final ResultSet rs, final String fieldName) throws SQLException {
//
//        Boolean result = null;
//
//        final String test = rs.getString(fieldName);
//
//        if (test != null && !test.isEmpty()) {
//            result = Boolean.valueOf("YyTt1".indexOf(test.charAt(0)) != -1);
//        }
//
//        return result;
//    }

    /**
     * Extracts a named string value from a result set, trimming off any leading or trailing spaces.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the value to extract
     * @return the trimmed string value, or {@code null} if the value was NULL
     * @throws SQLException if an exception occurs extracting the value
     */
    public static String getString(final ResultSet rs, final String fieldName) throws SQLException {

        String result = null;

        String str = rs.getString(fieldName);
        if (str != null) {
            str = str.trim();

            if (!str.isEmpty()) {
                result = str;
            }
        }

        return result;
    }

    /**
     * Extracts a named integer value from a result set.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the value to extract
     * @return the integer value, or {@code null} if the value was NULL
     * @throws SQLException if an exception occurs extracting the value
     */
    public static Integer getInteger(final ResultSet rs, final String fieldName) throws SQLException {

        Integer result = null;

        String test = rs.getString(fieldName);

        if (test != null) {
            test = test.trim();

            if (!test.isEmpty()) {
                result = Integer.valueOf(test);
            }
        }

        return result;
    }

//    /**
//     * Extracts a named long value from a result set.
//     *
//     * @param rs      the {@code ResultSet} from which to extract the value
//     * @param fieldName the name of the value to extract
//     * @return the integer value, or {@code null} if the value was NULL
//     * @throws SQLException if an exception occurs extracting the value
//     */
//    public static Long getLong(final ResultSet rs, final String fieldName) throws SQLException {
//
//        Long result = null;
//
//        String test = rs.getString(fieldName);
//
//        if (test != null) {
//            test = test.trim();
//
//            if (!test.isEmpty()) {
//                result = Long.valueOf(test);
//            }
//        }
//
//        return result;
//    }

//    /**
//     * Extracts a named float value from a result set.
//     *
//     * @param rs      the {@code ResultSet} from which to extract the value
//     * @param fieldName the name of the value to extract
//     * @return the float value, or {@code null} if the value was NULL
//     * @throws SQLException if an exception occurs extracting the value
//     */
//    public static Double getDouble(final ResultSet rs, final String fieldName) throws SQLException {
//
//        Double result = null;
//
//        String test = rs.getString(fieldName);
//
//        if (test != null) {
//            test = test.trim();
//
//            if (!test.isEmpty()) {
//                result = Double.valueOf(test);
//            }
//        }
//
//        return result;
//    }

    /**
     * Extracts a named date value from a result set.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the value to extract
     * @return the {@code IDate} value, or {@code null} if the value was NULL
     * @throws SQLException if an exception occurs extracting the value
     */
    public static LocalDate getDate(final ResultSet rs, final String fieldName) throws SQLException {

        final Date date = rs.getDate(fieldName);
        LocalDate result = null;

        if (date != null) {
            final Instant inst = Instant.ofEpochMilli(date.getTime());
            result = inst.atZone(ZoneId.systemDefault()).toLocalDate();
        }

        return result;
    }

//    /**
//     * Extracts a named time value from a result set.
//     *
//     * @param rs      the {@code ResultSet} from which to extract the value
//     * @param fieldName the name of the value to extract
//     * @return the {@code ITime} value, or {@code null} if the value was NULL
//     * @throws SQLException if an exception occurs extracting the value
//     */
//    public static LocalTime getTime(final ResultSet rs, final String fieldName) throws SQLException {
//
//        final Date time = rs.getDate(fieldName);
//        LocalTime result = null;
//
//        if (time != null) {
//            result = Instant.ofEpochMilli(time.getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
//        }
//
//        return result;
//    }

    /**
     * Extracts a named date/time value from a result set.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the value to extract
     * @return the {@code IDateTime} value, or {@code null} if the value was NULL
     * @throws SQLException if an exception occurs extracting the value
     */
    public static LocalDateTime getDateTime(final ResultSet rs, final String fieldName) throws SQLException {

        final Date datetime = rs.getDate(fieldName);
        LocalDateTime result = null;

        if (datetime != null) {
            final Instant inst = Instant.ofEpochMilli(datetime.getTime());
            result = inst.atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        return result;
    }

    /**
     * Extracts an integer field from the database, and interprets it as a year. If the value is less than 100, then all
     * values between 0 and 89 are interpreted as '2000' through '2089', and values between 90 and 99 are interpreted as
     * '1990' through '1999'.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the field to retrieve
     * @return the resulting year value
     * @throws SQLException if there is an error retrieving the field value
     */
    public static Integer getYear(final ResultSet rs, final String fieldName) throws SQLException {

        Integer year = getInteger(rs, fieldName);

        if (year != null && year.intValue() <= MAX_TWO_DIGIT) {

            if (year.intValue() >= LAST_1900_YEAR) {

                // We treat '80' through '99' as 1980 through 1999
                year = Integer.valueOf(year.intValue() + CENTURY_20);
            } else {

                // We treat '00' through '79' as 2000 through 2079
                year = Integer.valueOf(year.intValue() + CENTURY_21);
            }
        }

        return year;
    }
}
