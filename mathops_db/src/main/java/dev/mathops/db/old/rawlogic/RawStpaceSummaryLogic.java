package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawrecord.RawStpaceSummary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stpace_summary" records.
 *
 * <pre>
 * Table:  'stpace_summary'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * sect                 char(4)                   no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * i_in_progress        char(1)                   no
 * pace                 smallint                  no
 * pace_track           char(2)                   yes
 * pace_order           smallint                  no
 * ms_nbr               smallint                  no      PK
 * ms_unit              smallint                  no
 * ms_date              date                      no
 * new_ms_date          char(1)                   yes
 * exam_dt              date                      no
 * re_points            smallint                  no
 * </pre>
 */
public final class RawStpaceSummaryLogic extends AbstractRawLogic<RawStpaceSummary> {

    /** A single instance. */
    public static final RawStpaceSummaryLogic INSTANCE = new RawStpaceSummaryLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawStpaceSummaryLogic() {

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
    public boolean insert(final Cache cache, final RawStpaceSummary record) throws SQLException {

        if (record.stuId == null || record.course == null || record.sect == null
                || record.termKey == null || record.iInProgress == null || record.pace == null
                || record.paceOrder == null || record.msNbr == null || record.msUnit == null
                || record.msDate == null || record.examDt == null || record.rePoints == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat( //
                "INSERT INTO stpace_summary (stu_id,course,sect,term,term_yr,",
                "i_in_progress,pace,pace_track,pace_order,ms_nbr,ms_unit,ms_date,",
                "new_ms_date,exam_dt,re_points) VALUES (",
                sqlStringValue(record.stuId), ",",
                sqlStringValue(record.course), ",",
                sqlStringValue(record.sect), ",",
                sqlStringValue(record.termKey.termCode), ",",
                sqlIntegerValue(record.termKey.shortYear), ",",
                sqlStringValue(record.iInProgress), ",",
                sqlIntegerValue(record.pace), ",",
                sqlStringValue(record.paceTrack), ",",
                sqlIntegerValue(record.paceOrder), ",",
                sqlIntegerValue(record.msNbr), ",",
                sqlIntegerValue(record.msUnit), ",",
                sqlDateValue(record.msDate), ",",
                sqlStringValue(record.newMsDate), ",",
                sqlDateValue(record.examDt), ",",
                sqlIntegerValue(record.rePoints), ")");

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
    public boolean delete(final Cache cache, final RawStpaceSummary record) throws SQLException {

        final boolean result;

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("DELETE FROM stpace_summary ",
                "WHERE stu_id=", sqlStringValue(record.stuId),
                "  AND course=", sqlStringValue(record.course),
                "  AND sect=", sqlStringValue(record.sect),
                "  AND term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "  AND ms_nbr=", sqlIntegerValue(record.msNbr));

        final String sql = builder.toString();

        try (final Statement stmt = cache.conn.createStatement()) {
            result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public List<RawStpaceSummary> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM stpace_summary";

        final List<RawStpaceSummary> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStpaceSummary.fromResultSet(rs));
            }
        }

        return result;
    }
}
