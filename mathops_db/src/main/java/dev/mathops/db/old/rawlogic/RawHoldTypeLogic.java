package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawHoldType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with hold_type records.
 *
 * <pre>
 * Table:  'hold_type'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * hold_id              char(2)                   yes     PK
 * sev_admin_hold       char(1)                   no
 * hold_type            char(10)                  no
 * add_hold             char(1)                   no
 * delete_hold          char(1)                   no
 * </pre>
 */
public final class RawHoldTypeLogic extends AbstractRawLogic<RawHoldType> {

    /** A single instance. */
    public static final RawHoldTypeLogic INSTANCE = new RawHoldTypeLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawHoldTypeLogic() {

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
    public boolean insert(final Cache cache, final RawHoldType record) throws SQLException {

        if (record.holdId == null || record.sevAdminHold == null || record.holdType == null
                || record.addHold == null || record.deleteHold == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO hold_type ("
                , "hold_id,sev_admin_hold,hold_type,add_hold,delete_hold) VALUES (",
                sqlStringValue(record.holdId), ",",
                sqlStringValue(record.sevAdminHold), ",",
                sqlStringValue(record.holdType), ",",
                sqlStringValue(record.addHold), ",",
                sqlStringValue(record.deleteHold), ")");

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
    public boolean delete(final Cache cache, final RawHoldType record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM hold_type ",
                "WHERE hold_id=", sqlStringValue(record.holdId));

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
    public List<RawHoldType> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM hold_type";

        final List<RawHoldType> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawHoldType.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Queries for a single hold_type.
     *
     * @param cache  the data cache
     * @param holdId the ID of the hold type for which to query
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    public static RawHoldType query(final Cache cache, final String holdId) throws SQLException {

        RawHoldType result = null;

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM hold_type WHERE hold_id=",
                sqlStringValue(holdId));

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawHoldType.fromResultSet(rs);
            }
        }

        return result;
    }
}
