package dev.mathops.db.old.rawlogic;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawCohort;

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
public final class RawCohortLogic extends AbstractRawLogic<RawCohort> {

    /** A single instance. */
    public static final RawCohortLogic INSTANCE = new RawCohortLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawCohortLogic() {

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
    public boolean insert(final Cache cache, final RawCohort record) throws SQLException {

        if (record.cohort == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO cohort (cohort,size,instructor) VALUES (",
                sqlStringValue(record.cohort), ",",
                sqlIntegerValue(record.size), ",",
                sqlStringValue(record.instructor), ")");

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
    public boolean delete(final Cache cache, final RawCohort record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM cohort ",
                "WHERE cohort=", sqlStringValue(record.cohort));

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
    public List<RawCohort> queryAll(final Cache cache) throws SQLException {

        final List<RawCohort> result = new ArrayList<>(100);

        final String sql = "SELECT * FROM cohort";

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCohort.fromResultSet(rs));
            }
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

        final String sql = SimpleBuilder.concat("SELECT * FROM cohort WHERE cohort=", sqlStringValue(cohort));

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawCohort.fromResultSet(rs);
            }
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
                sqlIntegerValue(newSize), " WHERE cohort=", sqlStringValue(cohort));

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
}
