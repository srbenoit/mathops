package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The base class for logic modules.
 */
public abstract class AbstractLogicModule {

    /** Flag to disable live queries. */
    private static LocalDateTime bannerDownUntil;

    /**
     * Constructs a new {@code AbstractLogicModule}.
     */
    AbstractLogicModule() {

        // No action
    }

    /**
     * Tests whether Banner is down currently.
     *
     * @return true if down; false if not
     */
    public static boolean isBannerDown() {

        boolean result = false;

        if (bannerDownUntil != null) {
            final LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(bannerDownUntil)) {
                result = true;
            } else {
                bannerDownUntil = null;
            }
        }

        return result;
    }

    /**
     * Records the fact that a Banner operation failed, which marks Banner as being "Down" for 15 minutes.
     */
    public static void indicateBannerDown() {

        bannerDownUntil = LocalDateTime.now().plusMinutes(15L);

        Log.warning("Banner will be considered DOWN until ", TemporalUtils.FMT_MDY_AT_HMS_A.format(bannerDownUntil));
    }

    /**
     * Records the fact that a Banner operation failed, which marks Banner as being "Down" for a year ("indefinitely").
     */
    public static void indicateBannerDownIndefinitely() {

        bannerDownUntil = LocalDateTime.now().plusYears(1L);

        Log.warning("Banner will be considered DOWN until ", TemporalUtils.FMT_MDY.format(bannerDownUntil));
    }

    /**
     * Records the fact that a Banner operation failed, which marks Banner as being "Down" for 15 minutes.
     */
    public static void indicateBannerUp() {

        bannerDownUntil = null;

        Log.warning("Banner will now be considered UP");
    }

    /**
     * Returns the string needed to include an Integer in an SQL statement.
     *
     * @param i the integer
     * @return the SQL string, in the form "null" or "123".
     */
    static String sqlIntegerValue(final Integer i) {

        final String result;

        if (i == null) {
            result = "null";
        } else {
            result = i.toString();
        }

        return result;
    }

    /**
     * Returns the string needed to include a Long in an SQL statement.
     *
     * @param l the long
     * @return the SQL string, in the form "null" or "123".
     */
    static String sqlLongValue(final Long l) {

        final String result;

        if (l == null) {
            result = "null";
        } else {
            result = l.toString();
        }

        return result;
    }

    /**
     * Returns the string needed to include a Float in an SQL statement.
     *
     * @param f the float
     * @return the SQL string, in the form "null" or "123.456".
     */
    static String sqlFloatValue(final Float f) {

        final String result;

        if (f == null) {
            result = "null";
        } else {
            result = f.toString();
        }

        return result;
    }

    /**
     * Returns the string needed to include a string in an SQL statement.
     *
     * @param str the string
     * @return the SQL string, in the form "null" or "'string'".
     */
    static String sqlStringValue(final String str) {

        final String result;

        if (str == null) {
            result = "null";
        } else {
            result = "'" + str.replace("'", "''") + "'";
        }

        return result;
    }

    /**
     * Returns the string needed to include a date in an SQL statement.
     *
     * @param dt the date
     * @return the SQL string, in the form "DATE('12/31/2021')".
     */
    static String sqlDateValue(final LocalDate dt) {

        final String result;

        if (dt == null) {
            result = "null";
        } else {
            final int yy = dt.getYear();
            final int mm = dt.getMonthValue();
            final int dd = dt.getDayOfMonth();

            result = "DATE('" + mm + "/" + dd + "/" + yy + "')";
        }

        return result;
    }

    /**
     * Returns the string needed to include a date/time in an SQL statement.
     *
     * @param dtm the date/time
     * @return the SQL string, in the form "TO_DATE('2021-12-31 12:34:56')".
     */
    static String sqlDateTimeValue(final LocalDateTime dtm) {

        final String result;

        if (dtm == null) {
            result = "null";
        } else {
            final int y1 = dtm.getYear();
            final int m1 = dtm.getMonthValue();
            final int d1 = dtm.getDayOfMonth();
            final int hh1 = dtm.getHour();
            final int mm1 = dtm.getMinute();
            final int ss1 = dtm.getSecond();

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("TO_DATE('").add(y1).add('-').add(m1).add('-').add(d1).add(' ').add(hh1).add(':').add(mm1)
                    .add(':').add(ss1).add("')");

            result = sql.toString();
        }

        return result;
    }

    /**
     * Returns the string needed to include a TermKey in an SQL statement.
     *
     * @param key the term key
     * @return the SQL string, in the form "null" or something of the form "'FA20'".
     */
    static String sqlTermValue(final TermKey key) {

        final String result;

        if (key == null) {
            result = "null";
        } else {
            result = "'" + key.shortString + "'";
        }

        return result;
    }

    /**
     * Sets a string parameter in a prepared statement based on an {@code Object} which may be {@code null}. The
     * {@code toString} method on the object is used to generate the string value.
     *
     * @param ps    the prepared statement
     * @param index the index of the parameter to set
     * @param value the value
     * @throws SQLException if there is an error setting the value
     */
    static void setPsString(final PreparedStatement ps, final int index, final Object value) throws SQLException {

        if (value == null) {
            ps.setString(index, null);
        } else {
            final String s = value.toString().replace('\u2019', '\'');

            ps.setString(index, s);
        }
    }

    /**
     * Sets an integer parameter in a prepared statement based on an {@code Integer} which may be {@code null}.
     *
     * @param ps    the prepared statement
     * @param index the index of the parameter to set
     * @param value the value
     * @throws SQLException if there is an error setting the value
     */
    static void setPsInteger(final PreparedStatement ps, final int index, final Integer value) throws SQLException {

        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value.intValue());
        }
    }

    /**
     * Sets a date parameter in a prepared statement based on a {@code LocalDate} which may be {@code null}.
     *
     * @param ps    the prepared statement
     * @param index the index of the parameter to set
     * @param value the value
     * @throws SQLException if there is an error setting the value
     */
    static void setPsDate(final PreparedStatement ps, final int index, final LocalDate value) throws SQLException {

        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    /**
     * Sets a date/time parameter in a prepared statement based on a {@code LocalDateTime} which may be {@code null}.
     *
     * @param ps    the prepared statement
     * @param index the index of the parameter to set
     * @param value the value
     * @throws SQLException if there is an error setting the value
     */
    static void setPsTimestamp(final PreparedStatement ps, final int index, final LocalDateTime value)
            throws SQLException {

        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    /**
     * Executes an SQL query that should return a single integer (such as a COUNT or MAX function).
     *
     * @param conn the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the result of the query, or {@code null} if the query returned no record
     * @throws SQLException if there is an error executing the query
     */
    static Integer executeSimpleIntQuery(final DbConnection conn, final String sql) throws SQLException {

        Integer result = null;

        try (final Statement stmt = conn.createStatement();
             final ResultSet rset = stmt.executeQuery(sql)) {

            if (rset.next()) {
                final int value = rset.getInt(1);
                if (!rset.wasNull()) {
                    result = Integer.valueOf(value);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Query failed: [", sql, "]", ex);
            throw ex;
        }

        return result;
    }

//    /**
//     * Executes an SQL query that should return a single String.
//     *
//     * @param conn the database connection, checked out to this thread
//     * @param sql  the SQL to execute
//     * @return the result of the query, or {@code null} if the query returned no record
//     * @throws SQLException if there is an error executing the query
//     */
//    protected static String executeSimpleStringQuery(final DbConnection conn,
//                                                     final String sql) throws SQLException {
//
//        String result = null;
//
//        try (final Statement stmt = conn.createStatement();
//             final ResultSet rset = stmt.executeQuery(sql)) {
//
//            if (rset.next()) {
//                result = rset.getString(1);
//
//                if (result != null) {
//                    result = result.trim();
//                }
//            }
//        } catch (final SQLException ex) {
//            Log.warning("Query failed: [", sql, "]", ex);
//            throw ex;
//        }
//
//        return result;
//    }

    /**
     * Executes an SQL query that should return a single LocalDate.
     *
     * @param conn the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the result of the query, or {@code null} if the query returned no record
     * @throws SQLException if there is an error executing the query
     */
    static LocalDate executeSimpleDateQuery(final DbConnection conn, final String sql) throws SQLException {

        LocalDate result = null;

        try (final Statement stmt = conn.createStatement();
             final ResultSet rset = stmt.executeQuery(sql)) {

            if (rset.next()) {
                final Date dt = rset.getDate(1);
                result = dt == null ? null : dt.toLocalDate();
            }
        } catch (final SQLException ex) {
            Log.warning("Query failed: [", sql, "]", ex);
            throw ex;
        }

        return result;
    }

    ///**
    // * Executes an SQL query that should return a list of strings.
    // *
    // * @param conn the database connection, checked out to this thread
    // * @param sql the SQL to execute
    // * @return the result of the query, or {@code null} if the query returned no record
    // * @throws SQLException if there is an error executing the query
    // */
    // protected static final List<String> executeSimpleStringListQuery(final DbConnection conn,
    // final String sql) throws SQLException {
    //
    // List<String> result = new ArrayList<>();
    //
    // try (Statement stmt = conn.createStatement(); //
    // ResultSet rset = stmt.executeQuery(sql)) {
    //
    // while (rset.next()) {
    // String s = rset.getString(1);
    // if (s != null) {
    // s = s.trim();
    // }
    // result.add(s);
    // }
    // }
    //
    // return result;
    // }
}
