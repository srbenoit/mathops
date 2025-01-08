package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with semester_calendar records (the weeks in the current term).
 *
 * <pre>
 * Table:  'semester_calendar'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * week_nbr             smallint                  no      PK
 * start_dt             date                      no
 * end_dt               date                      no
 * </pre>
 */
public enum RawSemesterCalendarLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawSemesterCalendar record) throws SQLException {

        if (record.termKey == null || record.weekNbr == null || record.startDt == null || record.endDt == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO semester_calendar ",
                "(term,term_yr,week_nbr,start_dt,end_dt) VALUES (",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlIntegerValue(record.weekNbr), ",",
                LogicUtils.sqlDateValue(record.startDt), ",",
                LogicUtils.sqlDateValue(record.endDt), ")");

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
    public static boolean delete(final Cache cache, final RawSemesterCalendar record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM semester_calendar ",
                "WHERE term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "  AND week_nbr=", LogicUtils.sqlIntegerValue(record.weekNbr));

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
    public static List<RawSemesterCalendar> queryAll(final Cache cache) throws SQLException {

        final List<RawSemesterCalendar> result = new ArrayList<>(50);

        final String sql = "SELECT * FROM semester_calendar";

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawSemesterCalendar.fromResultSet(rs));
            }
        }

        return result;
    }
}
