package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawMpe;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with 'mpe' records.
 *
 * <pre>
 * Table:  'mpe'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * version              char(5)           no      PK
 * max_online_atmpts    smallint          no
 * max_proctored_atm+   smallint          no
 * </pre>
 */
public enum RawMpeLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawMpe record) throws SQLException {

        if (record.version == null || record.maxOnlineAtmpts == null || record.maxProctoredAtmpts == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO mpe (version,max_online_atmpts,max_proctored_atmpts) VALUES (",
                LogicUtils.sqlStringValue(record.version), ",",
                LogicUtils.sqlIntegerValue(record.maxOnlineAtmpts), ",",
                LogicUtils.sqlIntegerValue(record.maxProctoredAtmpts), ")");

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
    public static boolean delete(final Cache cache, final RawMpe record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM mpe ",
                "WHERE version=", LogicUtils.sqlStringValue(record.version));

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
    public static List<RawMpe> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM mpe";

        final List<RawMpe> result = new ArrayList<>(10);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpe.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
