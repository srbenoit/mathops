package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawPaceTrackRule;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "pace_track_rule" records.
 *
 * <pre>
 * Table:  'pace_track_rule'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * subterm              char(4)                   no      PK
 * pace                 smallint                  no      PK
 * pace_track           char(2)                   no      PK
 * criteria             char(30)                  no
 * </pre>
 */
public enum RawPaceTrackRuleLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawPaceTrackRule record) throws SQLException {

        if (record.termKey == null || record.subterm == null || record.pace == null || record.paceTrack == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO pace_track_rule (term,term_yr,subterm,pace,pace_track,criteria) VALUES (",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlStringValue(record.subterm), ",",
                LogicUtils.sqlIntegerValue(record.pace), ",",
                LogicUtils.sqlStringValue(record.paceTrack), ",",
                LogicUtils.sqlStringValue(record.criteria), ")");

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
    public static boolean delete(final Cache cache, final RawPaceTrackRule record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM pace_track_rule ",
                "WHERE term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "  AND subterm=", LogicUtils.sqlStringValue(record.subterm),
                "  AND pace=", LogicUtils.sqlIntegerValue(record.pace),
                "  AND pace_track=", LogicUtils.sqlStringValue(record.paceTrack));

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
    public static List<RawPaceTrackRule> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM pace_track_rule");
    }

    /**
     * Retrieves all pacing structures for a particular term.
     *
     * @param cache   the data cache
     * @param termKey the key of the term for which to query
     * @return the corresponding list of records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawPaceTrackRule> queryByTerm(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM pace_track_rule",
                " WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                "   AND term_yr=", termKey.shortYear);

        return executeListQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawPaceTrackRule> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawPaceTrackRule> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawPaceTrackRule.fromResultSet(rs));
            }
        }

        return result;
    }
}
