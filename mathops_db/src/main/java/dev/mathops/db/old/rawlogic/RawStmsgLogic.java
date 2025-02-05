package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStmsg;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stmsg" records.
 *
 * <pre>
 * Table:  'stmsg'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * msg_dt               date                      no      PK
 * pace                 smallint                  yes
 * course_index         smallint                  yes
 * touch_point          char(3)                   no      PK
 * msg_code             char(8)                   no      PK
 * sender               char(50)                  yes
 * </pre>
 */
public enum RawStmsgLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStmsg record) throws SQLException {

        if (record.stuId == null || record.msgDt == null || record.touchPoint == null || record.sender == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        final String sql = SimpleBuilder.concat(
                "INSERT INTO stmsg (stu_id,msg_dt,pace,course_index,touch_point,msg_code,sender) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlDateValue(record.msgDt), ",",
                LogicUtils.sqlIntegerValue(record.pace), ",",
                LogicUtils.sqlIntegerValue(record.courseIndex), ",",
                LogicUtils.sqlStringValue(record.touchPoint), ",",
                LogicUtils.sqlStringValue(record.msgCode), ",",
                LogicUtils.sqlStringValue(record.sender), ")");

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
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawStmsg record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM stmsg ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND msg_dt=", LogicUtils.sqlDateValue(record.msgDt),
                "  AND touch_point=", LogicUtils.sqlStringValue(record.touchPoint),
                "  AND msg_code=", LogicUtils.sqlStringValue(record.msgCode));

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
    public static List<RawStmsg> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM stmsg");
    }

    /**
     * Gets all purchased e-texts for a student, including all inactive (expired or refunded) e-texts. Results are
     * ordered by e-text ID, and includes any refunded or expired records.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student to query
     * @return the list of models that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmsg> queryByStudent(final Cache cache, final String studentId)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stmsg WHERE stu_id=",
                LogicUtils.sqlStringValue(studentId));

        return executeQuery(cache, sql);
    }

    /**
     * Gets the total number of STMSG records in the database.
     *
     * @param cache the data cache
     * @return the number of records, or {@code null} if the query returned no value
     * @throws SQLException if there is an error accessing the database
     */
    public static Integer count(final Cache cache) throws SQLException {

        final String sql = "SELECT count(*) FROM stmsg";

        return LogicUtils.executeSimpleIntQuery(cache, sql);
    }

    /**
     * Gets the date when the latest message was sent.
     *
     * @param cache the data cache
     * @return the latest message record date, or {@code null} if the query returned no value
     * @throws SQLException if there is an error accessing the database
     */
    public static LocalDate getLatest(final Cache cache) throws SQLException {

        final String sql = "SELECT max(msg_dt) FROM stmsg";

        return LogicUtils.executeSimpleDateQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStmsg> executeQuery(final Cache cache, final String sql) throws SQLException {

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        final List<RawStmsg> result = new ArrayList<>(50);

        try (final Statement s = conn.createStatement();
             final ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStmsg.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
