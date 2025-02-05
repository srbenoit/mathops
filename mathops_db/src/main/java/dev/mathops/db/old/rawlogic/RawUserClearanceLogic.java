package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawUserClearance;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with user_clearance records.
 *
 * <pre>
 * Table:  'user_clearance'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * login                char(8)                   no      PK
 * clear_function       char(9)                   no      PK
 * clear_type           smallint                  no
 * clear_passwd         char(8)                   yes
 * </pre>
 */
public enum RawUserClearanceLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawUserClearance record) throws SQLException {

        if (record.login == null || record.clearFunction == null || record.clearType == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO user_clearance (login,clear_function,clear_type,clear_passwd) VALUES (",
                LogicUtils.sqlStringValue(record.login), ",",
                LogicUtils.sqlStringValue(record.clearFunction), ",",
                LogicUtils.sqlIntegerValue(record.clearType), ",",
                LogicUtils.sqlStringValue(record.clearPasswd), ")");

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
    public static boolean delete(final Cache cache, final RawUserClearance record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM user_clearance ",
                "WHERE login=", LogicUtils.sqlStringValue(record.login),
                "  AND clear_function=", LogicUtils.sqlStringValue(record.clearFunction));

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
    public static List<RawUserClearance> queryAll(final Cache cache) throws SQLException {

        final List<RawUserClearance> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM user_clearance")) {

            while (rs.next()) {
                result.add(RawUserClearance.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Gets all records with a specified login.
     *
     * @param cache the data cache
     * @param login the login
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawUserClearance> queryAllForLogin(final Cache cache, final String login)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM user_clearance WHERE login=",
                LogicUtils.sqlStringValue(login));

        final List<RawUserClearance> result = new ArrayList<>(10);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawUserClearance.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
