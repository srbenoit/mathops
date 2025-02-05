package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with cuobjective records.
 *
 * <pre>
 * Table:  'cuobjective'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * course               char(6)                   no      PK
 * unit                 smallint                  no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * objective            smallint                  no      PK
 * lesson_id            char(40)                  yes
 * lesson_nbr           char(10)                  yes
 * start_dt             date                      yes
 * </pre>
 */
public enum RawCuobjectiveLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawCuobjective record) throws SQLException {

        if (record.course == null || record.unit == null || record.objective == null || record.termKey == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO cuobjective (course,unit,term,term_yr,objective,lesson_id,lesson_nbr,start_dt) VALUES (",
                LogicUtils.sqlStringValue(record.course), ",",
                record.unit, ",",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                record.termKey.shortYear, ",",
                record.objective, ",",
                LogicUtils.sqlStringValue(record.lessonId), ",",
                LogicUtils.sqlStringValue(record.lessonNbr), ",",
                LogicUtils.sqlDateValue(record.startDt), ")");

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
    public static boolean delete(final Cache cache, final RawCuobjective record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM cuobjective ",
                "WHERE course=", LogicUtils.sqlStringValue(record.course),
                "  AND unit=", LogicUtils.sqlIntegerValue(record.unit),
                "  AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "  AND objective=", LogicUtils.sqlIntegerValue(record.objective));

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
    public static List<RawCuobjective> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM cuobjective");
    }

    /**
     * Retrieves all course unit objectives in a given term.
     *
     * @param cache   the data cache
     * @param termKey the term key
     * @return the list of course units; {@code null} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawCuobjective> queryByTerm(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM cuobjective",
                " WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                " AND term_yr=", LogicUtils.sqlIntegerValue(termKey.shortYear));

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
    private static List<RawCuobjective> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawCuobjective> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCuobjective.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
