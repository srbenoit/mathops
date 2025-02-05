package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawParameters;
import dev.mathops.text.builder.SimpleBuilder;

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
public enum RawParametersLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawParameters record) throws SQLException {

        if (record.pgmName == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO parameters (",
                "pgm_name,parm1,parm2,parm3,parm4,parm5,parm6,parm7,parm8,parm9,parm10) VALUES (",
                LogicUtils.sqlStringValue(record.pgmName), ",",
                LogicUtils.sqlStringValue(record.parm1), ",",
                LogicUtils.sqlStringValue(record.parm2), ",",
                LogicUtils.sqlStringValue(record.parm3), ",",
                LogicUtils.sqlStringValue(record.parm4), ",",
                LogicUtils.sqlStringValue(record.parm5), ",",
                LogicUtils.sqlStringValue(record.parm6), ",",
                LogicUtils.sqlStringValue(record.parm7), ",",
                LogicUtils.sqlStringValue(record.parm8), ",",
                LogicUtils.sqlStringValue(record.parm9), ",",
                LogicUtils.sqlDateValue(record.parm10), ")");

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
    public static boolean delete(final Cache cache, final RawParameters record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM parameters ",
                "WHERE pgm_name=", LogicUtils.sqlStringValue(record.pgmName));

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
    public static List<RawParameters> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM parameters";

        final List<RawParameters> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawParameters.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
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
                "SELECT * FROM parameters WHERE pgm_name=", LogicUtils.sqlStringValue(pgmName)));
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
                "UPDATE parameters SET parm1=", LogicUtils.sqlStringValue(newParm1),
                " WHERE pgm_name=", LogicUtils.sqlStringValue(pgmName));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) > 0;

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
                "UPDATE parameters SET parm2=", LogicUtils.sqlStringValue(newParm2),
                " WHERE pgm_name=", LogicUtils.sqlStringValue(pgmName));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) > 0;

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
                "UPDATE parameters SET parm10=", LogicUtils.sqlDateValue(newParm10),
                " WHERE pgm_name=", LogicUtils.sqlStringValue(pgmName));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) > 0;

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
     * Performs a query that returns single record.
     *
     * @param cache the data cache
     * @param sql   the query SQL
     * @return the record; null if none returned
     * @throws SQLException if there is an error performing the query
     */
    private static RawParameters doSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawParameters result = null;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawParameters.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
