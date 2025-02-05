package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawEtextKey;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with etext_key records.
 *
 * <pre>
 * Table:  'etext_key'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * etext_id             char(6)                   no
 * etext_key            char(20)                  no      PK
 * active_dt            datetime year to second   yes
 * </pre>
 */
public enum RawEtextKeyLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawEtextKey record) throws SQLException {

        if (record.etextKey == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO etext_key ",
                "(etext_id,etext_key,active_dt) VALUES (",
                LogicUtils.sqlStringValue(record.etextId), ",",
                LogicUtils.sqlStringValue(record.etextKey), ",",
                LogicUtils.sqlDateTimeValue(record.activeDt), ")");

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
    public static boolean delete(final Cache cache, final RawEtextKey record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM etext_key ",
                "WHERE etext_key=", LogicUtils.sqlStringValue(record.etextKey));

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
    public static List<RawEtextKey> queryAll(final Cache cache) throws SQLException {

        final List<RawEtextKey> result = new ArrayList<>(500);

        final String sql = "SELECT * FROM etext_key";

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawEtextKey.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Queries for a single etext_key.
     *
     * @param cache    the data cache
     * @param etextKey the etext key for which to query
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    public static RawEtextKey query(final Cache cache, final String etextKey) throws SQLException {

        RawEtextKey result = null;

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM etext_key WHERE etext_key=",
                LogicUtils.sqlStringValue(etextKey));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawEtextKey.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Updates the active_dt field in the etext_key.
     *
     * @param cache       the data cache
     * @param etextKey    the e-text key
     * @param newActiveDt the new active date/time
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the query
     */
    public static boolean updateActiveDt(final Cache cache, final String etextKey,
                                         final LocalDateTime newActiveDt) throws SQLException {

        final String sql = SimpleBuilder.concat("UPDATE etext_key SET active_dt=",
                LogicUtils.sqlDateTimeValue(newActiveDt), " WHERE etext_key=", LogicUtils.sqlStringValue(etextKey));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            boolean result = stmt.executeUpdate(sql) == 1;

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
