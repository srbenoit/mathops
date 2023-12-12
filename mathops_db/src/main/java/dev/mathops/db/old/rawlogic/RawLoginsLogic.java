package dev.mathops.db.old.rawlogic;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawLogins;

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
public final class RawLoginsLogic extends AbstractRawLogic<RawLogins> {

    /** A single instance. */
    public static final RawLoginsLogic INSTANCE = new RawLoginsLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawLoginsLogic() {

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
    public boolean insert(final Cache cache, final RawLogins record) throws SQLException {

        if (record.userId == null || record.userType == null || record.userName == null
                || record.dtimeCreated == null || record.forcePwChange == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO logins (user_id,user_type,user_name,stored_key,",
                "server_key,dtime_created,dtime_expires,dtime_last_login,force_pw_change,email,salt,",
                "nbr_invalid_atmpts) VALUES (",
                sqlStringValue(record.userId), ",",
                sqlStringValue(record.userType), ",",
                sqlStringValue(record.userName), ",",
                sqlStringValue(record.storedKey), ",",
                sqlStringValue(record.serverKey), ",",
                sqlDateTimeValue(record.dtimeCreated), ",",
                sqlDateTimeValue(record.dtimeExpires), ",",
                sqlDateTimeValue(record.dtimeLastLogin), ",",
                sqlStringValue(record.forcePwChange), ",",
                sqlStringValue(record.email), ",",
                sqlStringValue(record.salt), ",",
                sqlIntegerValue(record.nbrInvalidAtmpts), ")");

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
    public boolean delete(final Cache cache, final RawLogins record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM logins ",
                "WHERE user_name=", sqlStringValue(record.userName));

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
    public List<RawLogins> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM logins";

        final List<RawLogins> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawLogins.fromResultSet(rs));
            }
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
                "SELECT * FROM logins WHERE user_name=", sqlStringValue(username)));
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
                "UPDATE logins SET dtime_last_login=", sqlDateTimeValue(now),
                " WHERE user_name=", sqlStringValue(record.userName));

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
                "UPDATE logins SET nbr_invalid_atmpts=", sqlIntegerValue(fails),
                " WHERE user_name=", sqlStringValue(username));

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
    private static RawLogins doSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawLogins result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawLogins.fromResultSet(rs);
            }
        }

        return result;
    }
}
