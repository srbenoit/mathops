package dev.mathops.db.old.rawlogic;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawParameters;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with parameters records.
 *
 * <pre>
 * Table:  'parameters'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * pgm_name             char(20)                  no      PK
 * parm1                char(20)                  yes
 * parm2                char(20)                  yes
 * parm3                char(20)                  yes
 * parm4                char(20)                  yes
 * parm5                char(20)                  yes
 * parm6                char(20)                  yes
 * parm7                char(20)                  yes
 * parm8                char(20)                  yes
 * parm9                char(20)                  yes
 * parm10               date                      yes
 * </pre>
 */
public final class RawParametersLogic extends AbstractRawLogic<RawParameters> {

    /** A single instance. */
    public static final RawParametersLogic INSTANCE = new RawParametersLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawParametersLogic() {

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
    public boolean insert(final Cache cache, final RawParameters record) throws SQLException {

        if (record.pgmName == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO parameters (",
                "pgm_name,parm1,parm2,parm3,parm4,parm5,parm6,parm7,parm8,parm9,parm10) VALUES (",
                sqlStringValue(record.pgmName), ",",
                sqlStringValue(record.parm1), ",",
                sqlStringValue(record.parm2), ",",
                sqlStringValue(record.parm3), ",",
                sqlStringValue(record.parm4), ",",
                sqlStringValue(record.parm5), ",",
                sqlStringValue(record.parm6), ",",
                sqlStringValue(record.parm7), ",",
                sqlStringValue(record.parm8), ",",
                sqlStringValue(record.parm9), ",",
                sqlDateValue(record.parm10), ")");

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
    public boolean delete(final Cache cache, final RawParameters record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM parameters ",
                "WHERE pgm_name=", sqlStringValue(record.pgmName));

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
    public List<RawParameters> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM parameters";

        final List<RawParameters> result = new ArrayList<>(20);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawParameters.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Queries for the parameters having a given program.
     *
     * @param cache   the data cache
     * @param pgmName the program name
     * @return the parameters model; {@code null} if not found or an error occurs
     * @throws SQLException if there is an error performing the query
     */
    public static RawParameters query(final Cache cache, final String pgmName) throws SQLException {

        return doSingleQuery(cache, SimpleBuilder.concat(
                "SELECT * FROM parameters WHERE pgm_name=", sqlStringValue(pgmName)));
    }

    /**
     * Updates the value of the 'parm1' parameter for a specified program.
     *
     * @param cache    the data cache
     * @param pgmName  the program name
     * @param newParm1 the new value for 'parm1'
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error performing the query
     */
    public static boolean updateParm1(final Cache cache, final String pgmName,
                                      final String newParm1) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "UPDATE parameters SET parm1=", sqlStringValue(newParm1),
                " WHERE pgm_name=", sqlStringValue(pgmName));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) > 0;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Updates the value of the 'parm2' parameter for a specified program.
     *
     * @param cache    the data cache
     * @param pgmName  the program name
     * @param newParm2 the new value for 'parm2'
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error performing the query
     */
    public static boolean updateParm2(final Cache cache, final String pgmName,
                                      final String newParm2) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "UPDATE parameters SET parm2=", sqlStringValue(newParm2),
                " WHERE pgm_name=", sqlStringValue(pgmName));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) > 0;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Updates the value of the 'parm10' parameter for a specified program.
     *
     * @param cache     the data cache
     * @param pgmName   the program name
     * @param newParm10 the new value for 'parm10'
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error performing the query
     */
    public static boolean updateParm10(final Cache cache, final String pgmName,
                                       final LocalDate newParm10) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "UPDATE parameters SET parm10=", sqlDateValue(newParm10),
                " WHERE pgm_name=", sqlStringValue(pgmName));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) > 0;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Performs a query that returns single record.
     *
     * @param cache the data cache
     * @param sql   the query SQL
     * @return the record; null if none returned
     * @throws SQLException if there is an error performing the query
     */
    private static RawParameters doSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawParameters result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawParameters.fromResultSet(rs);
            }
        }

        return result;
    }
}
