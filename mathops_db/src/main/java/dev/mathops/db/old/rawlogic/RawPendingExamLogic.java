package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawPendingExam;

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
public final class RawPendingExamLogic extends AbstractRawLogic<RawPendingExam> {

    /** A single instance. */
    public static final RawPendingExamLogic INSTANCE = new RawPendingExamLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawPendingExamLogic() {

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
    public boolean insert(final Cache cache, final RawPendingExam record) throws SQLException {

        if (record.serialNbr == null || record.version == null || record.stuId == null
                || record.examDt == null || record.startTime == null || record.course == null
                || record.unit == null || record.examType == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO pending_exam (serial_nbr,version,stu_id,exam_dt,exam_score,",
                "start_time,finish_time,time_ok,passed,seq_nbr,course,unit,exam_type,",
                "timelimit_factor,stu_type) VALUES (",
                sqlLongValue(record.serialNbr), ",",
                sqlStringValue(record.version), ",",
                sqlStringValue(record.stuId), ",",
                sqlDateValue(record.examDt), ",",
                sqlIntegerValue(record.examScore), ",",
                sqlIntegerValue(record.startTime), ",",
                sqlIntegerValue(record.finishTime), ",",
                sqlStringValue(record.timeOk), ",",
                sqlStringValue(record.passed), ",",
                sqlIntegerValue(record.seqNbr), ",",
                sqlStringValue(record.course), ",",
                sqlIntegerValue(record.unit), ",",
                sqlStringValue(record.examType), ",",
                sqlFloatValue(record.timelimitFactor), ",",
                sqlStringValue(record.stuType), ")");

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
    public boolean delete(final Cache cache, final RawPendingExam record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM pending_exam ",
                " WHERE serial_nbr=", sqlLongValue(record.serialNbr),
                "   AND stu_id=", sqlStringValue(record.stuId));

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
                " WHERE serial_nbr=", sqlLongValue(serialNbr),
                "   AND stu_id=", sqlStringValue(stuId));

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
    public List<RawPendingExam> queryAll(final Cache cache) throws SQLException {

        final List<RawPendingExam> result = new ArrayList<>(100);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM pending_exam")) {

            while (rs.next()) {
                result.add(RawPendingExam.fromResultSet(rs));
            }
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

        final String sql = "SELECT * FROM pending_exam WHERE stu_id=" + sqlStringValue(stuId);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawPendingExam.fromResultSet(rs));
            }
        }

        return result;
    }
}
