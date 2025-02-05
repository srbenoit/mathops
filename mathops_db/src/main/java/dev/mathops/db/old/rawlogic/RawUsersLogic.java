package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawUsers;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "users" records.
 *
 * <pre>
 * Table:  'users'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * term                 char(2)                   no
 * term_yr              smallint                  no
 * serial_nbr           integer                   yes      PK
 * version              char(5)                   no
 * exam_dt              date                      yes
 * exam_score           smallint                  yes
 * calc_course          char(2)                   no
 * passed               char(1)                   yes
 * </pre>
 */
public enum RawUsersLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawUsers record) throws SQLException {

        if (record.stuId == null || record.termKey == null || record.serialNbr == null
            || record.version == null || record.calcCourse == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO users ",
                "(stu_id,term,term_yr,serial_nbr,version,exam_dt,exam_score,calc_course,passed) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlLongValue(record.serialNbr), ",",
                LogicUtils.sqlStringValue(record.version), ",",
                LogicUtils.sqlDateValue(record.examDt), ",",
                LogicUtils.sqlIntegerValue(record.examScore), ",",
                LogicUtils.sqlStringValue(record.calcCourse), ",",
                LogicUtils.sqlStringValue(record.passed), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement statement = conn.createStatement()) {
            final boolean result = statement.executeUpdate(sql) == 1;

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
    public static boolean delete(final Cache cache, final RawUsers record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM users WHERE stu_id=",
                LogicUtils.sqlStringValue(record.stuId),
                "  AND serial_nbr=", LogicUtils.sqlLongValue(record.serialNbr),
                "  AND version=", LogicUtils.sqlStringValue(record.version));

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
    public static List<RawUsers> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM users");
    }

    /**
     * Retrieves all records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawUsers> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM users WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

        return executeQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawUsers> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawUsers> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawUsers.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
