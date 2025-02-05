package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stqa" records.
 *
 * <pre>
 * Table:  'stqa'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * serial_nbr           integer                   no      PK
 * question_nbr         smallint                  no      PK
 * answer_nbr           smallint                  yes     PK
 * objective            char(10)                  yes
 * stu_answer           varchar(100)              yes
 * stu_id               char(9)                   no
 * version              char(5)                   no
 * ans_correct          char(1)                   yes
 * exam_dt              date                      yes
 * subtest              char(1)                   yes
 * finish_time          integer                   yes
 * </pre>
 */
public enum RawStqaLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStqa record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawStqa for test student:");
            Log.info("  Student ID: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO stqa (serial_nbr,",
                    "question_nbr,answer_nbr,objective,stu_answer,stu_id,version,",
                    "ans_correct,exam_dt,subtest,finish_time) VALUES (",
                    LogicUtils.sqlLongValue(record.serialNbr), ",",
                    LogicUtils.sqlIntegerValue(record.questionNbr), ",",
                    LogicUtils.sqlIntegerValue(record.answerNbr), ",",
                    LogicUtils.sqlStringValue(record.objective), ",",
                    LogicUtils.sqlStringValue(record.stuAnswer), ",",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlStringValue(record.version), ",",
                    LogicUtils.sqlStringValue(record.ansCorrect), ",",
                    LogicUtils.sqlDateValue(record.examDt), ",",
                    LogicUtils.sqlStringValue(record.subtest), ",",
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
    public static boolean delete(final Cache cache, final RawStqa record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stqa",
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
    public static List<RawStqa> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM stqa");
    }

    /**
     * Queries for all exam answer records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of answer records with the specified serial number
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStqa> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stqa WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

        return executeQuery(cache, sql);
    }

    /**
     * Queries for all exam answer records for a single exam, identified by serial number.
     *
     * @param cache  the data cache
     * @param serial the serial number
     * @return the list of answer records with the specified serial number
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStqa> queryBySerial(final Cache cache, final Long serial) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stqa WHERE serial_nbr=",
                LogicUtils.sqlLongValue(serial));

        return executeQuery(cache, sql);
    }

    /**
     * Deletes a student exam and the associated question records.
     *
     * @param cache  the data cache
     * @param record the student exam whose associated answer records to delete
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean deleteAllForAttempt(final Cache cache, final RawStexam record)
            throws SQLException {

        final String sql1 = SimpleBuilder.concat("DELETE FROM stqa",
                " WHERE serial_nbr=", LogicUtils.sqlLongValue(record.serialNbr),
                " AND version=", LogicUtils.sqlStringValue(record.version),
                " AND stu_id=", LogicUtils.sqlStringValue(record.stuId));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql1);
            conn.commit();
        } finally {
            Cache.checkInConnection(conn);
        }

        return true;
    }

    /**
     * Updates the "correct" field in a student exam answer record.
     *
     * @param cache      the data cache
     * @param record     the object to update
     * @param newCorrect the new "correct" field value
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateAnsCorrect(final Cache cache, final RawStqa record,
                                           final String newCorrect) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping update of RawStqa for test student:");
            Log.info("  Student ID: ", record.stuId);
            result = false;
        } else {
            // TODO: We don't match on subtest here - should be moot, but this could update
            //  more than one row if an answer applies to multiple subtests

            final String sql = SimpleBuilder.concat("UPDATE stqa ",
                    " SET ans_correct=", LogicUtils.sqlStringValue(newCorrect),
                    " WHERE serial_nbr=", LogicUtils.sqlLongValue(record.serialNbr),
                    " AND question_nbr=", LogicUtils.sqlIntegerValue(record.questionNbr),
                    " AND answer_nbr=", LogicUtils.sqlIntegerValue(record.answerNbr));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                result = stmt.executeUpdate(sql) > 0;

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
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStqa> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStqa> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement statement = conn.createStatement();
             final ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStqa.fromResultSet(rs));
            }
        } catch (final SQLException ex) {
            Log.warning("Query failed: [", sql, "]", ex);
            throw ex;
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
