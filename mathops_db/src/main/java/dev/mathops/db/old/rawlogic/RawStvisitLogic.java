package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.rawrecord.RawStvisit;

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
public final class RawStvisitLogic extends AbstractRawLogic<RawStvisit> {

    /** A single instance. */
    public static final RawStvisitLogic INSTANCE = new RawStvisitLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawStvisitLogic() {

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
    public boolean insert(final Cache cache, final RawStvisit record) throws SQLException {

        if (record.stuId == null || record.whenStarted == null || record.location == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawStvisit for test student:");
            Log.info("stu_id: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO stvisit ",
                    "(stu_id,when_started,when_ended,location,seat) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlDateTimeValue(record.whenStarted), ",",
                    sqlDateTimeValue(record.whenEnded), ",",
                    sqlStringValue(record.location), ",",
                    sqlStringValue(record.seat), ")");

            try (final Statement stmt = cache.conn.createStatement()) {
                result = stmt.executeUpdate(sql) == 1;

                if (result) {
                    cache.conn.commit();
                } else {
                    cache.conn.rollback();
                }
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
    @Override
    public boolean delete(final Cache cache, final RawStvisit record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stvisit ",
                "WHERE stu_id=", sqlStringValue(record.stuId),
                "  AND when_started=", sqlDateTimeValue(record.whenStarted));

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
    public List<RawStvisit> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache.conn, "SELECT * FROM stvisit");
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

        final String sql = SimpleBuilder.concat("SELECT * FROM stvisit",
                " WHERE stu_id=", sqlStringValue(stuId));

        return executeListQuery(cache.conn, sql);
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

        final String sql = SimpleBuilder.concat("SELECT * FROM stvisit",
                " WHERE stu_id=", sqlStringValue(stuId),
                " AND when_ended IS NULL");

        return executeListQuery(cache.conn, sql);
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
    public boolean startNewVisit(final Cache cache, final String stuId, final LocalDateTime whenStarted,
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

        try (final Statement stmt = cache.conn.createStatement()) {

            for (final RawStvisit rec : getInProgressStudentVisits(cache, stuId)) {

                final String sql = SimpleBuilder.concat("UPDATE stvisit",
                        " SET when_ended=", sqlDateTimeValue(endDateTime),
                        " WHERE stu_id=", sqlStringValue(stuId),
                        " AND when_started=", sqlDateTimeValue(rec.whenStarted));

                if (stmt.executeUpdate(sql) == 1) {
                    cache.conn.commit();
                } else {
                    cache.conn.rollback();
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param conn the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStvisit> executeListQuery(final DbConnection conn, final String sql) throws SQLException {

        final List<RawStvisit> result = new ArrayList<>(500);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStvisit.fromResultSet(rs));
            }
        }

        return result;
    }
}
