package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "pending_exam" records.
 *
 * <pre>
 * Table:  'pending_exam'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * serial_nbr           integer                   no      PK
 * version              char(5)                   no
 * stu_id               char(9)                   no      PK
 * exam_dt              date                      no
 * exam_score           smallint                  yes
 * start_time           integer                   no
 * finish_time          integer                   yes
 * time_ok              char(1)                   yes
 * passed               char(1)                   yes
 * seq_nbr              smallint                  yes
 * course               char(6)                   no
 * unit                 smallint                  no
 * exam_type            char(2)                   no
 * timelimit_factor     decimal(3,2)              yes
 * stu_type             char(3)                   yes
 * </pre>
 */
public enum RawPendingExamLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawPendingExam record) throws SQLException {

        if (record.serialNbr == null || record.version == null || record.stuId == null
            || record.examDt == null || record.startTime == null || record.course == null
            || record.unit == null || record.examType == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO pending_exam (serial_nbr,version,stu_id,exam_dt,exam_score,start_time,finish_time,",
                "time_ok,passed,seq_nbr,course,unit,exam_type,timelimit_factor,stu_type) VALUES (",
                LogicUtils.sqlLongValue(record.serialNbr), ",",
                LogicUtils.sqlStringValue(record.version), ",",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlDateValue(record.examDt), ",",
                LogicUtils.sqlIntegerValue(record.examScore), ",",
                LogicUtils.sqlIntegerValue(record.startTime), ",",
                LogicUtils.sqlIntegerValue(record.finishTime), ",",
                LogicUtils.sqlStringValue(record.timeOk), ",",
                LogicUtils.sqlStringValue(record.passed), ",",
                LogicUtils.sqlIntegerValue(record.seqNbr), ",",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlIntegerValue(record.unit), ",",
                LogicUtils.sqlStringValue(record.examType), ",",
                LogicUtils.sqlFloatValue(record.timelimitFactor), ",",
                LogicUtils.sqlStringValue(record.stuType), ")");

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
    public static boolean delete(final Cache cache, final RawPendingExam record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM pending_exam ",
                " WHERE serial_nbr=", LogicUtils.sqlLongValue(record.serialNbr),
                "   AND stu_id=", LogicUtils.sqlStringValue(record.stuId));

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
     * Deletes a pending_exam record. This call does not commit the deletion.
     *
     * @param cache     the data cache
     * @param serialNbr the serial number of the record to delete
     * @param stuId     the student ID of the record to delete
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final Long serialNbr, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM pending_exam ",
                " WHERE serial_nbr=", LogicUtils.sqlLongValue(serialNbr),
                "   AND stu_id=", LogicUtils.sqlStringValue(stuId));

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
    public static List<RawPendingExam> queryAll(final Cache cache) throws SQLException {

        final List<RawPendingExam> result = new ArrayList<>(100);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM pending_exam")) {

            while (rs.next()) {
                result.add(RawPendingExam.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Retrieves all records with a specified student ID.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of matching RawPendingExam records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawPendingExam> queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        final List<RawPendingExam> result = new ArrayList<>(10);

        final String sql = "SELECT * FROM pending_exam WHERE stu_id=" + LogicUtils.sqlStringValue(stuId);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawPendingExam.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
