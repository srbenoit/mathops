package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawPrereq;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "prereq" records.
 *
 * <pre>
 * Table:  'precalc'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * course               char(6)                   no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * prerequisite         char(6)                   no      PK
 * </pre>
 */
public enum RawPrereqLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawPrereq record) throws SQLException {

        if (record.termKey == null || record.course == null || record.prerequisite == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO prereq (term,term_yr,course,prerequisite) VALUES (",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                record.termKey.shortYear, ",",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlStringValue(record.prerequisite), ")");

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
    public static boolean delete(final Cache cache, final RawPrereq record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM prereq ",
                "WHERE course=", LogicUtils.sqlStringValue(record.course),
                "  AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "  AND prerequisite=", LogicUtils.sqlStringValue(record.prerequisite));

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
    public static List<RawPrereq> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM prereq");
    }

    /**
     * Retrieves all prerequisites for a particular term.
     *
     * @param cache   the data cache
     * @param termKey the key of the term for which to query
     * @return the corresponding list of records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawPrereq> queryByTerm(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM prereq",
                " WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                "   AND term_yr=", termKey.shortYear);

        return executeListQuery(cache, sql);
    }

    /**
     * Retrieves all prerequisites for a particular term and course.
     *
     * @param cache   the data cache
     * @param termKey the key of the term for which to query
     * @param course  the course for which to query
     * @return the corresponding list of records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawPrereq> queryByTermAndCourse(final Cache cache, final TermKey termKey,
                                                       final String course) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM prereq",
                " WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                "   AND term_yr=", termKey.shortYear,
                "   AND course=", LogicUtils.sqlStringValue(course));

        return executeListQuery(cache, sql);
    }

    /**
     * Gets the list of courses that can satisfy the prerequisites of a specified course.
     *
     * @param cache  the data cache
     * @param course the course
     * @return the list of courses
     * @throws SQLException if there is an error performing the query
     */
    public static List<String> getPrerequisitesByCourse(final Cache cache, final String course) throws SQLException {

        final TermRec activeTerm = cache.getSystemData().getActiveTerm();
        final List<RawPrereq> list = queryByTermAndCourse(cache, activeTerm.term, course);

        final List<String> result = new ArrayList<>(list.size());

        for (final RawPrereq rec : list) {
            result.add(rec.prerequisite);
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
    private static List<RawPrereq> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawPrereq> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawPrereq.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
