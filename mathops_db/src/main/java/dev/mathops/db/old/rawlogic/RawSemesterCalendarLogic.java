package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;

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
public final class RawSemesterCalendarLogic extends AbstractRawLogic<RawSemesterCalendar> {

    /** A single instance. */
    public static final RawSemesterCalendarLogic INSTANCE = new RawSemesterCalendarLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawSemesterCalendarLogic() {

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
    public boolean insert(final Cache cache, final RawSemesterCalendar record) throws SQLException {

        if (record.termKey == null || record.weekNbr == null || record.startDt == null || record.endDt == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO semester_calendar ",
                "(term,term_yr,week_nbr,start_dt,end_dt) VALUES (",
                sqlStringValue(record.termKey.termCode), ",",
                sqlIntegerValue(record.termKey.shortYear), ",",
                sqlIntegerValue(record.weekNbr), ",",
                sqlDateValue(record.startDt), ",",
                sqlDateValue(record.endDt), ")");

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
    public boolean delete(final Cache cache, final RawSemesterCalendar record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM semester_calendar ",
                "WHERE term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "  AND week_nbr=", sqlIntegerValue(record.weekNbr));

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
    public List<RawSemesterCalendar> queryAll(final Cache cache) throws SQLException {

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
