package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
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
public final class RawCampusCalendarLogic extends AbstractRawLogic<RawCampusCalendar> {

    /** A single instance. */
    public static final RawCampusCalendarLogic INSTANCE = new RawCampusCalendarLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawCampusCalendarLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final RawCampusCalendar record) throws SQLException {

        if (record.campusDt == null || record.dtDesc == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO campus_calendar (campus_dt,dt_desc,open_time1,open_time2,",
                "close_time1,close_time2,weekdays_1,weekdays_2) VALUES (",
                sqlDateValue(record.campusDt), ",",
                sqlStringValue(record.dtDesc), ",",
                sqlStringValue(record.openTime1), ",",
                sqlStringValue(record.openTime2), ",",
                sqlStringValue(record.closeTime1), ",",
                sqlStringValue(record.closeTime2), ",",
                sqlStringValue(record.weekdays1), ",",
                sqlStringValue(record.weekdays2), ")");

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
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean delete(final Cache cache, final RawCampusCalendar record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM campus_calendar ",
                "WHERE campus_dt=", sqlDateValue(record.campusDt),
                "  AND dt_desc=", sqlStringValue(record.dtDesc));

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
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public List<RawCampusCalendar> queryAll(final Cache cache) throws SQLException {

        final List<RawCampusCalendar> result = new ArrayList<>(50);

        final String sql = "SELECT * FROM campus_calendar";

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCampusCalendar.fromResultSet(rs));
            }
        }

        return result;
    }
}
