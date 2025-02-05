package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStcunit;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stcunit" records.
 *
 * <pre>
 * Table:  'stcunit'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * unit                 smallint                  no      PK
 * review_status        char(1)                   no
 * review_score         smallint                  yes
 * review_points        smallint                  yes
 * proctored_status     char(1)                   no
 * proctored_score      smallint                  yes
 * proctored_points     smallint                  yes
 * </pre>
 */
public enum RawStcunitLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStcunit record) throws SQLException {

        if (record.stuId == null || record.course == null || record.unit == null
                || record.reviewStatus == null || record.proctoredStatus == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of stcunit for test student:");
            Log.info("  Student ID: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "INSERT INTO stcunit (stu_id,course,unit,review_status,",
                    "review_score,review_points,proctored_status,proctored_score,",
                    "proctored_points) VALUES (",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlStringValue(record.course), ",",
                    LogicUtils.sqlIntegerValue(record.unit), ",",
                    LogicUtils.sqlStringValue(record.reviewStatus), ",",
                    LogicUtils.sqlIntegerValue(record.reviewScore), ",",
                    LogicUtils.sqlIntegerValue(record.reviewPoints), ",",
                    LogicUtils.sqlStringValue(record.proctoredStatus), ",",
                    LogicUtils.sqlIntegerValue(record.proctoredScore), ",",
                    LogicUtils.sqlIntegerValue(record.proctoredPoints), ")");

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                result = stmt.executeUpdate(sql) == 1;

                if (result) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } finally {
                Cache.checkInConnection(conn);
            }
        }

        return result;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawStcunit record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM stcunit ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course),
                "  AND unit=", LogicUtils.sqlIntegerValue(record.unit));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStcunit> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM stcunit";

        return doListQuery(cache, sql);
    }

    /**
     * Gets all records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStcunit> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stcunit where stu_id=",
                LogicUtils.sqlStringValue(stuId));

        return doListQuery(cache, sql);
    }

    /**
     * Performs a query that returns list of records.
     *
     * @param cache the data cache
     * @param sql   the query SQL
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    private static List<RawStcunit> doListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStcunit> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStcunit.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
