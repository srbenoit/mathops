package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawResource;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "resource" records.
 *
 * <pre>
 * Table:  'resource'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * resource_id          char(7)                   no      PK
 * resource_type        char(2)                   no
 * resource_desc        char(80)                  yes
 * days_allowed         smallint                  no
 * holds_allowed        smallint                  no
 * hold_id              char(2)                   no
 * </pre>
 */
public enum RawResourceLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawResource record) throws SQLException {

        if (record.resourceId == null || record.resourceType == null || record.daysAllowed == null
            || record.holdsAllowed == null || record.holdId == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO resource ",
                "(resource_id,resource_type,resource_desc,days_allowed,holds_allowed,hold_id) VALUES (",
                LogicUtils.sqlStringValue(record.resourceId), ",",
                LogicUtils.sqlStringValue(record.resourceType), ",",
                LogicUtils.sqlStringValue(record.resourceDesc), ",",
                LogicUtils.sqlIntegerValue(record.daysAllowed), ",",
                LogicUtils.sqlIntegerValue(record.holdsAllowed), ",",
                LogicUtils.sqlStringValue(record.holdId), ")");

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
    public static boolean delete(final Cache cache, final RawResource record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM resource ",
                "WHERE resource_id=", LogicUtils.sqlStringValue(record.resourceId));

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
    public static List<RawResource> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM resource";

        final List<RawResource> result = new ArrayList<>(250);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawResource.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Retrieves all single resource.
     *
     * @param cache      the data cache
     * @param resourceId the student ID
     * @return the list of matching records
     * @throws SQLException if there is an error performing the query
     */
    public static RawResource query(final Cache cache, final String resourceId)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM resource",
                " WHERE resource_id=", LogicUtils.sqlStringValue(resourceId));

        return executeSingleQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static RawResource executeSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawResource result = null;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawResource.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
