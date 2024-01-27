package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.rawrecord.RawStlessonAssign;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stlesson_assign" records.
 *
 * <pre>
 * Table:  'stlesson_assign'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * lesson_id            char(12)                  no      PK
 * when_shown           datetime year to second   yes
 * when_opened          datetime year to second   yes
 * when_closed          datetime year to second   yes
 * when_hidden          datetime year to second   yes
 * when_started         datetime year to second   yes
 * when_finished        datetime year to second   yes
 * score_tenths         integer                   yes
 * </pre>
 */
public final class RawStlessonAssignLogic extends AbstractRawLogic<RawStlessonAssign> {

    /** A single instance. */
    public static final RawStlessonAssignLogic INSTANCE = new RawStlessonAssignLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawStlessonAssignLogic() {

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
    public boolean insert(final Cache cache, final RawStlessonAssign record) throws SQLException {

        if (record.stuId == null || record.lessonId == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawStlessonAssign for test student:");
            Log.info("stu_id: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO stlesson_assign ",
                    "(stu_id,course,lesson_id,when_shown,when_open,when_closed,",
                    "when_hidden,when_started,when_finished,score_tenths) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.course), ",",
                    sqlStringValue(record.lessonId), ",",
                    sqlDateTimeValue(record.whenShown), ",",
                    sqlDateTimeValue(record.whenOpen), ",",
                    sqlDateTimeValue(record.whenClosed), ",",
                    sqlDateTimeValue(record.whenHidden), ",",
                    sqlDateTimeValue(record.whenStarted), ",",
                    sqlDateTimeValue(record.whenFinished), ",",
                    sqlIntegerValue(record.scoreTenths), ")");

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
    public boolean delete(final Cache cache, final RawStlessonAssign record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stlesson_assign ",
                "WHERE stu_id=", sqlStringValue(record.stuId),
                "  AND course=", sqlStringValue(record.course),
                "  AND lesson_id=", sqlStringValue(record.lessonId));

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
    public List<RawStlessonAssign> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache.conn, "SELECT * FROM stlesson_assign");
    }

    /**
     * Retrieves all student term configurations.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of matching student term records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStlessonAssign> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stlesson_assign",
                " WHERE stu_id=", sqlStringValue(stuId));

        return executeListQuery(cache.conn, sql);
    }

    /**
     * Gets a single record.
     *
     * @param cache    the data cache
     * @param stuId    the student ID
     * @param course   the course
     * @param lessonId the lesson ID
     * @return the matching record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStlessonAssign query(final Cache cache, final String stuId, final String course,
                                          final String lessonId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stlesson_assign",
                " WHERE stu_id=", sqlStringValue(stuId),
                " AND course=", sqlStringValue(course),
                " AND lessod_id=", sqlStringValue(lessonId));

        return executeSingleQuery(cache.conn, sql);
    }

    /**
     * Executes a query that returns a sigle record.
     *
     * @param conn the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static RawStlessonAssign executeSingleQuery(final DbConnection conn, final String sql) throws SQLException {

        RawStlessonAssign result = null;

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawStlessonAssign.fromResultSet(rs);
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
    private static List<RawStlessonAssign> executeListQuery(final DbConnection conn,
                                                            final String sql) throws SQLException {

        final List<RawStlessonAssign> result = new ArrayList<>(50);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStlessonAssign.fromResultSet(rs));
            }
        }

        return result;
    }
}
