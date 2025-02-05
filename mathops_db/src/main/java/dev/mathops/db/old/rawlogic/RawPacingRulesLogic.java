package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "pacing_rules" records.
 *
 * <pre>
 * Table:  'pacing_rules'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * pacing_rules         char(1)                   no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * activity_type        char(2)                   no      PK
 * requirement          char(4)                   no      PK
 * </pre>
 */
public enum RawPacingRulesLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawPacingRules record) throws SQLException {

        if (record.termKey == null || record.pacingStructure == null || record.activityType == null
                || record.requirement == null) {

            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat( //
                "INSERT INTO pacing_rules (term,term_yr,pacing_structure,activity_type,requirement) VALUES (",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                record.termKey.shortYear, ",",
                LogicUtils.sqlStringValue(record.pacingStructure), ",",
                LogicUtils.sqlStringValue(record.activityType), ",",
                LogicUtils.sqlStringValue(record.requirement), ")");

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
    public static boolean delete(final Cache cache, final RawPacingRules record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM pacing_rules ",
                "WHERE term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "  AND pacing_structure=", LogicUtils.sqlStringValue(record.pacingStructure),
                "  AND activity_type=", LogicUtils.sqlStringValue(record.activityType),
                "  AND requirement=", LogicUtils.sqlStringValue(record.requirement));

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
    public static List<RawPacingRules> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM pacing_rules");
    }

    /**
     * Retrieves all pacing structures for a particular term.
     *
     * @param cache   the data cache
     * @param termKey the key of the term for which to query
     * @return the corresponding list of records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawPacingRules> queryByTerm(final Cache cache, final TermKey termKey)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM pacing_rules WHERE term=",
                LogicUtils.sqlStringValue(termKey.termCode), " AND term_yr=", termKey.shortYear);

        return executeListQuery(cache, sql);
    }

    /**
     * Retrieves all pacing structures for a particular term.
     *
     * @param cache           the data cache
     * @param termKey         the key of the term for which to query
     * @param pacingStructure the pacing structure for which to query
     * @return the corresponding list of records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawPacingRules> queryByTermAndPacingStructure(final Cache cache, final TermKey termKey,
                                                                     final String pacingStructure) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM pacing_rules WHERE term=",
                LogicUtils.sqlStringValue(termKey.termCode), " AND term_yr=", termKey.shortYear,
                " AND pacing_structure=", LogicUtils.sqlStringValue(pacingStructure));

        return executeListQuery(cache, sql);
    }

    /**
     * Tests whether there is a requirement attached to an activity under a specified pacing structure in a term.
     *
     * @param cache           the data cache
     * @param termKey         the term
     * @param pacingStructure the pacing structure
     * @param activityType    the activity for which to test the requirement
     * @param requirement     the requirement to test
     * @return true if the requirement is necessary according to pacing rules; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean isRequired(final Cache cache, final TermKey termKey, final String pacingStructure,
                                     final String activityType, final String requirement)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM pacing_rules",
                " WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                "   AND term_yr=", termKey.shortYear,
                "   AND pacing_structure=", LogicUtils.sqlStringValue(pacingStructure),
                "   AND activity_type=", LogicUtils.sqlStringValue(activityType),
                "   AND requirement=", LogicUtils.sqlStringValue(requirement));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next();
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawPacingRules> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawPacingRules> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawPacingRules.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
