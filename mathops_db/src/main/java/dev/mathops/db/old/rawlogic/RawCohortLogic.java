package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawCohort;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with cohort records.
 *
 * <pre>
 * Table:  'cohort'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * cohort               char(8)                   no      PK
 * size                 smallint                  no
 * instructor           char(30)                  yes
 * </pre>
 */
public enum RawCohortLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawCohort record) throws SQLException {

        if (record.cohort == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO cohort (cohort,size,instructor) VALUES (",
                LogicUtils.sqlStringValue(record.cohort), ",",
                LogicUtils.sqlIntegerValue(record.size), ",",
                LogicUtils.sqlStringValue(record.instructor), ")");

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
    public static boolean delete(final Cache cache, final RawCohort record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM cohort ",
                "WHERE cohort=", LogicUtils.sqlStringValue(record.cohort));

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
    public static List<RawCohort> queryAll(final Cache cache) throws SQLException {

        final List<RawCohort> result = new ArrayList<>(100);

        final String sql = "SELECT * FROM cohort";

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCohort.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Queries for a single cohort.
     *
     * @param cache  the data cache
     * @param cohort the ID of the cohort for which to query
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    public static RawCohort query(final Cache cache, final String cohort) throws SQLException {

        RawCohort result = null;

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM cohort WHERE cohort=", LogicUtils.sqlStringValue(cohort));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawCohort.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Updates the size field in a cohort.
     *
     * @param cache   the data cache
     * @param cohort  the cohort ID
     * @param newSize the new size
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the query
     */
    public static boolean updateCohortSize(final Cache cache, final String cohort,
                                           final Integer newSize) throws SQLException {

        final String sql = SimpleBuilder.concat("UPDATE cohort SET size=",
                LogicUtils.sqlIntegerValue(newSize), " WHERE cohort=", LogicUtils.sqlStringValue(cohort));

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
}
