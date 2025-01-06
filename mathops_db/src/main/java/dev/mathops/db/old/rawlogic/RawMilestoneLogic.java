package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with 'milestone' records.
 *
 * <pre>
 * Table:  'milestone'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * term                 char(2)           no      PK
 * term_yr              smallint          no      PK
 * pace                 smallint          no
 * pace_track           char(2)           no      PK
 * ms_nbr               smallint          no      PK
 * ms_type              char(8)           no      PK
 * ms_date              date              no
 * nbr_atmpts_allow     smallint          yes
 * </pre>
 */
public final class RawMilestoneLogic extends AbstractRawLogic<RawMilestone> {

    /** A single instance. */
    public static final RawMilestoneLogic INSTANCE = new RawMilestoneLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawMilestoneLogic() {

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
    public boolean insert(final Cache cache, final RawMilestone record) throws SQLException {

        if (record.termKey == null || record.paceTrack == null || record.msNbr == null || record.msType == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO milestone (term,term_yr,pace,pace_track,ms_nbr,ms_type,ms_date,nbr_atmpts_allow)",
                " VALUES (",
                "'", record.termKey.termCode, "',",
                record.termKey.shortYear, ",",
                sqlIntegerValue(record.pace), ",",
                sqlStringValue(record.paceTrack), ",",
                record.msNbr, ",",
                sqlStringValue(record.msType), ",",
                sqlDateValue(record.msDate), ",",
                sqlIntegerValue(record.nbrAtmptsAllow), ")");

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
    public boolean delete(final Cache cache, final RawMilestone record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM milestone ",
                "WHERE term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "  AND pace=", sqlIntegerValue(record.pace),
                "  AND pace_track=", sqlStringValue(record.paceTrack),
                "  AND ms_nbr=", sqlIntegerValue(record.msNbr),
                "  AND ms_type=", sqlStringValue(record.msType));

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
    public List<RawMilestone> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM milestone");
    }

    /**
     * Gets all milestone records.
     *
     * @param cache   the data cache
     * @param termKey the term key for which to return records
     * @return the list of milestone records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMilestone> getAllMilestones(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM milestone",
                " WHERE term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(termKey.shortYear));

        return executeListQuery(cache, sql);
    }

    /**
     * Gets all milestone records for a specified pace, ordered by milestone date.
     *
     * @param cache     the data cache
     * @param termKey   the term key for which to return records
     * @param pace      the pace for which to return records
     * @param paceTrack the pace ID for which to return records
     * @return the list of milestone records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMilestone> getAllMilestones(final Cache cache, final TermKey termKey, final int pace,
                                                      final String paceTrack) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM milestone",
                " WHERE term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(termKey.shortYear),
                "   AND pace=", sqlIntegerValue(Integer.valueOf(pace)),
                "   AND pace_track=", sqlStringValue(paceTrack));

        return executeListQuery(cache, sql);
    }

    /**
     * Gets all milestone records for a specified pace, ordered by milestone date.
     *
     * @param cache     the data cache
     * @param milestone the milestone to update
     * @param newMsDate the new milestone date
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateMsDate(final Cache cache, final RawMilestone milestone,
                                       final LocalDate newMsDate) throws SQLException {

        final String sql = SimpleBuilder.concat("UPDATE milestone",
                " SET ms_date=", sqlDateValue(newMsDate),
                " WHERE term=", sqlStringValue(milestone.termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(milestone.termKey.shortYear),
                "   AND pace=", sqlIntegerValue(milestone.pace),
                "   AND pace_track=", sqlStringValue(milestone.paceTrack),
                "   AND ms_nbr=", sqlIntegerValue(milestone.msNbr),
                "   AND ms_type=", sqlStringValue(milestone.msType));

        boolean result = false;

        Log.info(sql);

        try (final Statement stmt = cache.conn.createStatement()) {
            final int numRows = stmt.executeUpdate(sql);
            result = numRows == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
        }

        return result;
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawMilestone> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawMilestone> result = new ArrayList<>(10);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMilestone.fromResultSet(rs));
            }
        }

        return result;
    }
}
