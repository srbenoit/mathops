package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStvisit;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stvisit" records.
 *
 * <pre>
 * Table:  'stvisit'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * when_started         datetime year to second   no      PK
 * when_ended           datetime year to second   yes
 * location             char(2)                   no
 * seat                 char(2)                   yes
 * </pre>
 */
public enum RawStvisitLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStvisit record) throws SQLException {

        if (record.stuId == null || record.whenStarted == null || record.location == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawStvisit for test student:");
            Log.info("stu_id: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "INSERT INTO stvisit (stu_id,when_started,when_ended,location,seat) VALUES (",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlDateTimeValue(record.whenStarted), ",",
                    LogicUtils.sqlDateTimeValue(record.whenEnded), ",",
                    LogicUtils.sqlStringValue(record.location), ",",
                    LogicUtils.sqlStringValue(record.seat), ")");

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
    public static boolean delete(final Cache cache, final RawStvisit record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stvisit WHERE stu_id=",
                LogicUtils.sqlStringValue(record.stuId),
                "  AND when_started=", LogicUtils.sqlDateTimeValue(record.whenStarted));

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
    public static List<RawStvisit> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM stvisit");
    }

    /**
     * Gets all records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStvisit> queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stvisit WHERE stu_id=",
                LogicUtils.sqlStringValue(stuId));

        return executeListQuery(cache, sql);
    }

    /**
     * Retrieves all visit records for a student that have not been marked "ended". In the absence of failures to "check
     * out" of a location, there should be at most one such record.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of visit records (empty if none found)
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStvisit> getInProgressStudentVisits(final Cache cache, final String stuId)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stvisit WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                " AND when_ended IS NULL");

        return executeListQuery(cache, sql);
    }

    /**
     * Starts a new visit record for a student, ending any in-progress visit records found. This method does not commit
     * the insert.
     *
     * @param cache       the data cache
     * @param stuId       the student ID
     * @param whenStarted the start date/time
     * @param location    the location being visited
     * @param seat        the seat; if known
     * @return true if operation succeeded
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean startNewVisit(final Cache cache, final String stuId, final LocalDateTime whenStarted,
                                        final String location, final String seat) throws SQLException {

        endInProgressVisit(cache, stuId, whenStarted);

        return insert(cache, new RawStvisit(stuId, whenStarted, null, location, seat));
    }

    /**
     * Scans for any in-progress visits for a student and marks them as ended. This method does not commit the update.
     *
     * @param cache       the data cache
     * @param stuId       the student ID
     * @param endDateTime the start date/time
     * @return true if operation succeeded; false on failure or if no in-progress operations were found
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean endInProgressVisit(final Cache cache, final String stuId,
                                             final LocalDateTime endDateTime) throws SQLException {

        boolean result = true;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {

            for (final RawStvisit rec : getInProgressStudentVisits(cache, stuId)) {

                final String sql = SimpleBuilder.concat(
                        "UPDATE stvisit SET when_ended=", LogicUtils.sqlDateTimeValue(endDateTime),
                        " WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                        " AND when_started=", LogicUtils.sqlDateTimeValue(rec.whenStarted));

                if (stmt.executeUpdate(sql) == 1) {
                    conn.commit();
                } else {
                    conn.rollback();
                    result = false;
                    break;
                }
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStvisit> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStvisit> result = new ArrayList<>(500);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStvisit.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
