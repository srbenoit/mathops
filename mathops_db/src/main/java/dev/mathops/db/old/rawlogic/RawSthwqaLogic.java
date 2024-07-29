package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawSthwqa;

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
public final class RawSthwqaLogic extends AbstractRawLogic<RawSthwqa> {

    /** A single instance. */
    public static final RawSthwqaLogic INSTANCE = new RawSthwqaLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawSthwqaLogic() {

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
    public boolean insert(final Cache cache, final RawSthwqa record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawSthwqa for test student:");
            Log.info("  Student ID: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO sthwqa (serial_nbr,",
                    "question_nbr,answer_nbr,objective,stu_answer,stu_id,version,",
                    "ans_correct,hw_dt,finish_time) VALUES (",
                    sqlLongValue(record.serialNbr), ",",
                    sqlIntegerValue(record.questionNbr), ",",
                    sqlIntegerValue(record.answerNbr), ",",
                    sqlStringValue(record.objective), ",",
                    sqlStringValue(record.stuAnswer), ",",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.version), ",",
                    sqlStringValue(record.ansCorrect), ",",
                    sqlDateValue(record.hwDt), ",",
                    sqlIntegerValue(record.finishTime), ")");

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
    public boolean delete(final Cache cache, final RawSthwqa record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM sthwqa",
                " WHERE serial_nbr=", sqlLongValue(record.serialNbr),
                " AND question_nbr=", sqlIntegerValue(record.questionNbr),
                " AND answer_nbr=", sqlIntegerValue(record.answerNbr));

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
    public List<RawSthwqa> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache.conn, "SELECT * FROM sthwqa");
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

        final String sql = SimpleBuilder.concat("SELECT * FROM sthwqa WHERE stu_id=", sqlStringValue(stuId));

        return executeQuery(cache.conn, sql);
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

        final String sql = SimpleBuilder.concat("SELECT * FROM sthwqa WHERE serial_nbr=", sqlLongValue(serial));

        return executeQuery(cache.conn, sql);
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
                "WHERE serial_nbr=", sqlLongValue(record.serialNbr));

        try (final Statement stmt = cache.conn.createStatement()) {
            stmt.executeUpdate(sql);
            cache.conn.commit();
            return true;
        }
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param conn the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawSthwqa> executeQuery(final DbConnection conn, final String sql)
            throws SQLException {

        final List<RawSthwqa> result = new ArrayList<>(50);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawSthwqa.fromResultSet(rs));
            }
        }

        return result;
    }
}
