package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawUserClearance;

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
public final class RawUserClearanceLogic extends AbstractRawLogic<RawUserClearance> {

    /** A single instance. */
    public static final RawUserClearanceLogic INSTANCE = new RawUserClearanceLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawUserClearanceLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final RawUserClearance record) throws SQLException {

        if (record.login == null || record.clearFunction == null || record.clearType == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO user_clearance (login,clear_function,clear_type,clear_passwd) VALUES (",
                sqlStringValue(record.login), ",",
                sqlStringValue(record.clearFunction), ",",
                sqlIntegerValue(record.clearType), ",",
                sqlStringValue(record.clearPasswd), ")");

        try (final Statement stmt = cache.conn.createStatement()) {
            return stmt.executeUpdate(sql) == 1;
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
    public boolean delete(final Cache cache, final RawUserClearance record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM user_clearance ",
                "WHERE login=", sqlStringValue(record.login),
                "  AND clear_function=", sqlStringValue(record.clearFunction));

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
    public List<RawUserClearance> queryAll(final Cache cache) throws SQLException {

        final List<RawUserClearance> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM user_clearance")) {

            while (rs.next()) {
                result.add(RawUserClearance.fromResultSet(rs));
            }
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

        final String sql = SimpleBuilder.concat("SELECT * FROM user_clearance ",
                "WHERE login=", sqlStringValue(login));

        final List<RawUserClearance> result = new ArrayList<>(10);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawUserClearance.fromResultSet(rs));
            }
        }

        return result;
    }
}
