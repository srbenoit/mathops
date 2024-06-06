package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.old.rawrecord.RawResource;

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
public final class RawResourceLogic extends AbstractRawLogic<RawResource> {

    /** A single instance. */
    public static final RawResourceLogic INSTANCE = new RawResourceLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawResourceLogic() {

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
    public boolean insert(final Cache cache, final RawResource record) throws SQLException {

        if (record.resourceId == null || record.resourceType == null || record.daysAllowed == null
                || record.holdsAllowed == null || record.holdId == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO resource ",
                "(resource_id,resource_type,resource_desc,days_allowed,holds_allowed,hold_id) VALUES (",
                sqlStringValue(record.resourceId), ",",
                sqlStringValue(record.resourceType), ",",
                sqlStringValue(record.resourceDesc), ",",
                sqlIntegerValue(record.daysAllowed), ",",
                sqlIntegerValue(record.holdsAllowed), ",",
                sqlStringValue(record.holdId), ")");

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
    public boolean delete(final Cache cache, final RawResource record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM resource ",
                "WHERE resource_id=", sqlStringValue(record.resourceId));

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
    public List<RawResource> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM resource";

        final List<RawResource> result = new ArrayList<>(250);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawResource.fromResultSet(rs));
            }
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
                " WHERE resource_id=", sqlStringValue(resourceId));

        return executeSingleQuery(cache.conn, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param conn the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static RawResource executeSingleQuery(final DbConnection conn, final String sql) throws SQLException {

        RawResource result = null;

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawResource.fromResultSet(rs);
            }
        }

        return result;
    }
}
