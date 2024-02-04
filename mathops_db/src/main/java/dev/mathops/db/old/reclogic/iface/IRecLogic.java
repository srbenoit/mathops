package dev.mathops.db.old.reclogic.iface;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbConfig;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rec.RecBase;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * An interface implemented by record logic implementations.
 *
 * @param <T> the record type
 */
public interface IRecLogic<T extends RecBase> {

    /** A string used to build queries. */
    String WHERE = " WHERE ";

    /** A string used to build queries. */
    String AND = " AND ";

    /**
     * Gets the database installation type for a cache.
     *
     * @param cache the cache
     * @return the database installation type
     */
    static EDbProduct getDbType(final Cache cache) {

        final DbConfig db = cache.getDbProfile().getDbContext(ESchemaUse.PRIMARY).loginConfig.db;

        return db.server.type;
    }

    /**
     * Returns the string needed to include an Integer in an SQL statement.
     *
     * @param i the integer
     * @return the SQL string, in the form "null" or "123".
     */
    default String sqlIntegerValue(final int i) {

        return Integer.toString(i);
    }

    /**
     * Returns the string needed to include an Integer in an SQL statement.
     *
     * @param i the integer
     * @return the SQL string, in the form "null" or "123".
     */
    default String sqlIntegerValue(final Integer i) {

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
    default String sqlLongValue(final Long l) {

        final String result;

        if (l == null) {
            result = "null";
        } else {
            result = l.toString();
        }

        return result;
    }

//    /**
//     * Returns the string needed to include a Float in an SQL statement.
//     *
//     * @param f the float
//     * @return the SQL string, in the form "null" or "123.456".
//     */
//    default String sqlFloatValue(final Float f) {
//
//        final String result;
//
//        if (f == null) {
//            result = "null";
//        } else {
//            result = f.toString();
//        }
//
//        return result;
//    }

    /**
     * Returns the string needed to include a string in an SQL statement.
     *
     * @param str the string
     * @return the SQL string, in the form "null" or "'string'".
     */
    default String sqlStringValue(final String str) {

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
    default String sqlDateValue(final LocalDate dt) {

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

//    /**
//     * Returns the string needed to include a time in an SQL statement.
//     *
//     * @param tm the time
//     * @return the SQL string, in the form "'23:59:58'".
//     */
//    default String sqlTimeValue(final LocalTime tm) {
//
//        final String result;
//
//        if (tm == null) {
//            result = "null";
//        } else {
//            final int hh = tm.getHour();
//            final int mm = tm.getMinute();
//            final int ss = tm.getSecond();
//
//            result = "'" + hh + ":" + mm + ":" + ss + "')";
//        }
//
//        return result;
//    }

    /**
     * Returns the string needed to include a date/time in an SQL statement.
     *
     * @param dtm the date/time
     * @return the SQL string, in the form "'2021-12-31 12:34:56'".
     */
    default String sqlDateTimeValue(final LocalDateTime dtm) {

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

            sql.add("'").add(y1).add('-').add(m1).add('-').add(d1).add(' ').add(hh1).add(':').add(mm1).add(':')
                    .add(ss1).add("'");

            result = sql.toString();
        }

        return result;
    }

//    /**
//     * Returns the string needed to include a TermKey in an SQL statement.
//     *
//     * @param key the term key
//     * @return the SQL string, in the form "null" or something of the form "'FA20'".
//     */
//    default String sqlTermValue(final TermKey key) {
//
//        final String result;
//
//        if (key == null) {
//            result = "null";
//        } else {
//            result = "'" + key.shortString + "'";
//        }
//
//        return result;
//    }

    /**
     * Retrieves a String field value from a result set, returning null if the result set indicates a null value was
     * present. The string is trimmed to remove leading or trailing whitespace.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    default String getStringField(final ResultSet rs, final String name)
            throws SQLException {

        final String tmp = rs.getString(name);

        return tmp == null ? null : tmp.trim();
    }

    /**
     * Retrieves a Long field value from a result set, returning null if the result set indicates a null value was
     * present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    default Long getLongField(final ResultSet rs, final String name) throws SQLException {

        final long tmp = rs.getLong(name);

        return rs.wasNull() ? null : Long.valueOf(tmp);
    }

    /**
     * Retrieves an Integer field value from a result set, returning null if the result set indicates a null value was
     * present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    default Integer getIntegerField(final ResultSet rs, final String name)
            throws SQLException {

        final int tmp = rs.getInt(name);

        return rs.wasNull() ? null : Integer.valueOf(tmp);
    }

//    /**
//     * Retrieves a Float field value from a result set, returning null if the result set indicates a null value was
//     * present.
//     *
//     * @param rs   the result set
//     * @param name the field name
//     * @return the value
//     * @throws SQLException if there is an error retrieving the value
//     */
//    default Float getFloatField(final ResultSet rs, final String name) throws SQLException {
//
//        final float tmp = rs.getFloat(name);
//
//        return rs.wasNull() ? null : Float.valueOf(tmp);
//    }

    /**
     * Retrieves a LocalDate field value from a result set, returning null if the result set indicates a null value was
     * present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    default LocalDate getDateField(final ResultSet rs, final String name)
            throws SQLException {

        final Date tmp = rs.getDate(name);

        return tmp == null ? null : tmp.toLocalDate();
    }

    /**
     * Retrieves a LocalDateTime field value from a result set, returning null if the result set indicates a null value
     * was present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    default LocalDateTime getDateTimeField(final ResultSet rs, final String name)
            throws SQLException {

        final Timestamp tmp = rs.getTimestamp(name);

        return tmp == null ? null : tmp.toLocalDateTime();
    }

//    /**
//     * Retrieves a TermKey field value from a result set, returning null if the result set indicates a null value was
//     * present, or the value found could not be parsed.
//     *
//     * @param rs   the result set
//     * @param name the field name
//     * @return the value
//     * @throws SQLException if there is an error retrieving the value
//     */
//    default TermKey getTermField(final ResultSet rs, final String name) throws SQLException {
//
//        TermKey result = null;
//
//        try {
//            final String str = rs.getString(name);
//            if (str != null) {
//                final String trim = str.trim();
//                if (!trim.isEmpty()) {
//                    result = new TermKey(trim);
//                }
//            }
//        } catch (final IllegalArgumentException ex) {
//            Log.warning(ex);
//        }
//
//        return result;
//    }

    /**
     * Executes an update SQL statement that SHOULD alter one row.
     *
     * @param cache the data cache
     * @param sql   the query SQL
     * @return true of the statement succeeded and indicated one row was changed; false otherwise
     * @throws SQLException if there is an error performing the update
     */
    default boolean doUpdateOneRow(final Cache cache, final String sql) throws SQLException {

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Performs a query that returns single record.
     *
     * @param cache the data cache
     * @param sql   the query SQL
     * @return the record; null if none returned
     * @throws SQLException if there is an error performing the query
     */
    default T doSingleQuery(final Cache cache, final String sql) throws SQLException {

        T result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = fromResultSet(rs);
            }
        }

        return result;
    }

    /**
     * Performs a query that returns list of records.
     *
     * @param cache the data cache
     * @param sql   the query SQL
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    default List<T> doListQuery(final Cache cache, final String sql) throws SQLException {

        final List<T> result = new ArrayList<>(10);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Extracts a record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    T fromResultSet(ResultSet rs) throws SQLException;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    boolean insert(Cache cache, T record) throws SQLException;

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    boolean delete(Cache cache, T record) throws SQLException;

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    List<T> queryAll(final Cache cache) throws SQLException;
}
