package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawSthwqa;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "sthwqa" records.
 *
 * <pre>
 * Table:  'sthwqa'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * serial_nbr           integer                   no      PK
 * question_nbr         smallint                  no      PK
 * answer_nbr           smallint                  no      PK
 * objective            char(6)                   no
 * stu_answer           varchar(100)              no
 * stu_id               char(9)                   no
 * version              char(5)                   no
 * ans_correct          char(1)                   no
 * hw_dt                date                      no
 * finish_time          integer                   yes
 * </pre>
 */
public enum RawSthwqaLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawSthwqa record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawSthwqa for test student:");
            Log.info("  Student ID: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO sthwqa (serial_nbr,",
                    "question_nbr,answer_nbr,objective,stu_answer,stu_id,version,",
                    "ans_correct,hw_dt,finish_time) VALUES (",
                    LogicUtils.sqlLongValue(record.serialNbr), ",",
                    LogicUtils.sqlIntegerValue(record.questionNbr), ",",
                    LogicUtils.sqlIntegerValue(record.answerNbr), ",",
                    LogicUtils.sqlStringValue(record.objective), ",",
                    LogicUtils.sqlStringValue(record.stuAnswer), ",",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlStringValue(record.version), ",",
                    LogicUtils.sqlStringValue(record.ansCorrect), ",",
                    LogicUtils.sqlDateValue(record.hwDt), ",",
                    LogicUtils.sqlIntegerValue(record.finishTime), ")");

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
    public static boolean delete(final Cache cache, final RawSthwqa record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM sthwqa",
                " WHERE serial_nbr=", LogicUtils.sqlLongValue(record.serialNbr),
                " AND question_nbr=", LogicUtils.sqlIntegerValue(record.questionNbr),
                " AND answer_nbr=", LogicUtils.sqlIntegerValue(record.answerNbr));

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
    public static List<RawSthwqa> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM sthwqa");
    }

    /**
     * Gets all records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSthwqa> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM sthwqa WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

        return executeQuery(cache, sql);
    }

    /**
     * Gets all records for a single homework attempt, identified by serial number.
     *
     * @param cache  the data cache
     * @param serial the serial number
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawSthwqa> queryBySerial(final Cache cache, final Long serial) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM sthwqa WHERE serial_nbr=",
                LogicUtils.sqlLongValue(serial));

        return executeQuery(cache, sql);
    }

    /**
     * Deletes all records for a homework attempt.
     *
     * @param cache  the data cache
     * @param record the homework attempt whose corresponding answer records to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */

    public static boolean deleteAllForAttempt(final Cache cache, final RawSthomework record)
            throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM sthwqa ",
                "WHERE serial_nbr=", LogicUtils.sqlLongValue(record.serialNbr));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            conn.commit();
        } finally {
            Cache.checkInConnection(conn);
        }

        return true;
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawSthwqa> executeQuery(final Cache cache, final String sql)
            throws SQLException {

        final List<RawSthwqa> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawSthwqa.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
