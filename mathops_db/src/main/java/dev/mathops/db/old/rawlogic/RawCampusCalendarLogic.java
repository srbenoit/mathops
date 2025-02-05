package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with client PCs (testing stations).
 *
 * <pre>
 * Table:  'campus_calendar'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * campus_dt            date                      no      PK
 * dt_desc              char(20)                  no      PK
 * open_time1           char(10)                  yes
 * open_time2           char(10)                  yes
 * close_time1          char(10)                  yes
 * close_time2          char(10)                  yes
 * weekdays_1           char(20)                  yes
 * weekdays_2           char(20)                  yes
 * </pre>
 */
public enum RawCampusCalendarLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawCampusCalendar record) throws SQLException {

        if (record.campusDt == null || record.dtDesc == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO campus_calendar (campus_dt,dt_desc,open_time1,open_time2,",
                "close_time1,close_time2,weekdays_1,weekdays_2) VALUES (",
                LogicUtils.sqlDateValue(record.campusDt), ",",
                LogicUtils.sqlStringValue(record.dtDesc), ",",
                LogicUtils.sqlStringValue(record.openTime1), ",",
                LogicUtils.sqlStringValue(record.openTime2), ",",
                LogicUtils.sqlStringValue(record.closeTime1), ",",
                LogicUtils.sqlStringValue(record.closeTime2), ",",
                LogicUtils.sqlStringValue(record.weekdays1), ",",
                LogicUtils.sqlStringValue(record.weekdays2), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawCampusCalendar record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM campus_calendar ",
                "WHERE campus_dt=", LogicUtils.sqlDateValue(record.campusDt),
                "  AND dt_desc=", LogicUtils.sqlStringValue(record.dtDesc));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawCampusCalendar> queryAll(final Cache cache) throws SQLException {

        final List<RawCampusCalendar> result = new ArrayList<>(50);

        final String sql = "SELECT * FROM campus_calendar";

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCampusCalendar.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
