package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawChallengeFee;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "challenge_fee" records.
 *
 * <pre>
 * Table:  'challenge_fee'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                    no      PK
 * course               char(6)                    no      PK
 * exam_dt              date                       no
 * bill_dt              date                       no
 * </pre>
 */
public enum RawChallengeFeeLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawChallengeFee record) throws SQLException {

        if (record.stuId == null || record.course == null || record.examDt == null || record.billDt == null) {

            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO challenge_fee (stu_id,course,exam_dt,bill_dt) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlDateValue(record.examDt), ",",
                LogicUtils.sqlDateValue(record.billDt), ")");

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
    public static boolean delete(final Cache cache, final RawChallengeFee record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM challenge_fee ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course));

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
    public static List<RawChallengeFee> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM challenge_fee");
    }

    /**
     * Queries every record in the database.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawChallengeFee> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        return executeListQuery(cache,
                "SELECT * FROM challenge_fee WHERE stu_id=" + LogicUtils.sqlStringValue(stuId));
    }

    /**
     * Queries every record in the database.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param course the course ID
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    public static RawChallengeFee queryByStudentCourse(final Cache cache, final String stuId,
                                                       final String course) throws SQLException {

        return executeSingleQuery(cache, "SELECT * FROM challenge_fee WHERE stu_id="
                                         + LogicUtils.sqlStringValue(stuId)
                                         + " AND course=" + LogicUtils.sqlStringValue(course));
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawChallengeFee> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawChallengeFee> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawChallengeFee.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Executes a query that returns a single record.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    private static RawChallengeFee executeSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawChallengeFee result = null;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawChallengeFee.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
