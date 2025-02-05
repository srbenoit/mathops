package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawLogins;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with logins records.
 *
 * <pre>
 * Table:  'logins'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * user_id              char(9)                   no
 * user_type            char(3)                   no
 * user_name            char(20)                  no      PK
 * salt                 char(32)                  no
 * stored_key           char(64)                  no
 * server_key           char(64)                  no
 * dtime_created        datetime year to second   no
 * dtime_expires        datetime year to second   yes
 * dtime_last_login     datetime year to second   yes
 * force_pw_change      char(1)                   no
 * email                char(40)                  yes
 * nbr_invalid_atmpts   smallint                  yes
 * </pre>
 */
public enum RawLoginsLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawLogins record) throws SQLException {

        if (record.userId == null || record.userType == null || record.userName == null
            || record.dtimeCreated == null || record.forcePwChange == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO logins (user_id,user_type,user_name,stored_key,",
                "server_key,dtime_created,dtime_expires,dtime_last_login,force_pw_change,email,salt,",
                "nbr_invalid_atmpts) VALUES (",
                LogicUtils.sqlStringValue(record.userId), ",",
                LogicUtils.sqlStringValue(record.userType), ",",
                LogicUtils.sqlStringValue(record.userName), ",",
                LogicUtils.sqlStringValue(record.storedKey), ",",
                LogicUtils.sqlStringValue(record.serverKey), ",",
                LogicUtils.sqlDateTimeValue(record.dtimeCreated), ",",
                LogicUtils.sqlDateTimeValue(record.dtimeExpires), ",",
                LogicUtils.sqlDateTimeValue(record.dtimeLastLogin), ",",
                LogicUtils.sqlStringValue(record.forcePwChange), ",",
                LogicUtils.sqlStringValue(record.email), ",",
                LogicUtils.sqlStringValue(record.salt), ",",
                LogicUtils.sqlIntegerValue(record.nbrInvalidAtmpts), ")");

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
    public static boolean delete(final Cache cache, final RawLogins record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM logins ",
                "WHERE user_name=", LogicUtils.sqlStringValue(record.userName));

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
    public static List<RawLogins> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM logins";

        final List<RawLogins> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawLogins.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Queries for the login having a given username.
     *
     * @param cache    the data cache
     * @param username the username for which to query
     * @return the login model; {@code null} if not found or an error occurs
     * @throws SQLException if there is an error performing the query
     */
    public static RawLogins query(final Cache cache, final String username) throws SQLException {

        return doSingleQuery(cache, SimpleBuilder.concat(
                "SELECT * FROM logins WHERE user_name=", LogicUtils.sqlStringValue(username)));
    }

    /**
     * Updates the last successful login time on the login record to the current date/time.
     *
     * @param cache  the data cache
     * @param record the login object whose last login time is to be updated
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error performing the query
     */
    public static boolean updateLastLoginTime(final Cache cache, final RawLogins record) throws SQLException {

        final LocalDateTime now = LocalDateTime.now();

        final String sql = SimpleBuilder.concat(
                "UPDATE logins SET dtime_last_login=", LogicUtils.sqlDateTimeValue(now),
                " WHERE user_name=", LogicUtils.sqlStringValue(record.userName));

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
     * Updates the number of password fails and "disabled" state/reason for a local login record.
     *
     * @param cache    the data cache
     * @param username the username of the login object to update
     * @param fails    the new number of password fails
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error performing the query
     */
    public static boolean updatePasswordFails(final Cache cache, final String username,
                                              final Integer fails) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "UPDATE logins SET nbr_invalid_atmpts=", LogicUtils.sqlIntegerValue(fails),
                " WHERE user_name=", LogicUtils.sqlStringValue(username));

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
    private static RawLogins doSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawLogins result = null;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawLogins.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
