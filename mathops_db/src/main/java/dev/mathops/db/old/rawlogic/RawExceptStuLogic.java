package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawExceptStu;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with except_stu key records.
 *
 * <pre>
 * Table:  'etext_key'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * unit                 smallint                  no      PK
 * course_enroll        char(6)                   no
 * hwork_status         char(1)                   no
 * term                 char(2)                   no
 * term_yr              smallint                  no
 * sect                 char(4)                   no
 * sect_enroll          char(4)                   no
 * </pre>
 */
public enum RawExceptStuLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawExceptStu record) throws SQLException {

        if (record.stuId == null || record.course == null || record.unit == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO except_stu ",
                "(term,term_yr,stu_id,course,unit,course_enroll,hwork_status,sect,sect_enroll) VALUES (",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlIntegerValue(record.unit), ",",
                LogicUtils.sqlStringValue(record.courseEnroll), ",",
                LogicUtils.sqlStringValue(record.hworkStatus), ",",
                LogicUtils.sqlStringValue(record.sect), ",",
                LogicUtils.sqlStringValue(record.sectEnroll), ")");

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
    public static boolean delete(final Cache cache, final RawExceptStu record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM except_stu ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course),
                "  AND unit=", LogicUtils.sqlIntegerValue(record.unit));

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
    public static List<RawExceptStu> queryAll(final Cache cache) throws SQLException {

        return doListQuery(cache, "SELECT * FROM except_stu");
    }

    /**
     * Queries for all records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawExceptStu> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM except_stu WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

        return doListQuery(cache, sql);
    }

    /**
     * Queries for all records for a student and course.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param course the course ID
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawExceptStu> queryByStudentCourse(final Cache cache, final String stuId,
                                                          final String course) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM except_stu WHERE stu_id=",
                LogicUtils.sqlStringValue(stuId), " AND course=",
                LogicUtils.sqlStringValue(course));

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
    private static List<RawExceptStu> doListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawExceptStu> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawExceptStu.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
