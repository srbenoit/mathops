package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStchallengeqa;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stchallengeqa" records.
 *
 * <pre>
 * Table:  'stchallengeqa'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * version              char(5)                   no
 * exam_dt              date                      no      PK
 * finish_time          integer                   no      PK
 * question_nbr         smallint                  no      PK
 * stu_answer           char(5)                   yes
 * ans_correct          char(1)                   yes
 * </pre>
 */
public enum RawStchallengeqaLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStchallengeqa record) throws SQLException {

        if (record.stuId == null || record.course == null || record.version == null
                || record.examDt == null || record.finishTime == null || record.questionNbr == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO stchallengeqa (stu_id,course,version,exam_dt,finish_time,",
                "question_nbr,stu_answer,ans_correct) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlStringValue(record.version), ",",
                LogicUtils.sqlDateValue(record.examDt), ",",
                LogicUtils.sqlIntegerValue(record.finishTime), ",",
                LogicUtils.sqlIntegerValue(record.questionNbr), ",",
                LogicUtils.sqlStringValue(record.stuAnswer), ",",
                LogicUtils.sqlStringValue(record.ansCorrect), ")");

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
    public static boolean delete(final Cache cache, final RawStchallengeqa record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stchallengeqa ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course),
                "  AND exam_dt=", LogicUtils.sqlDateValue(record.examDt),
                "  AND finish_time=", LogicUtils.sqlIntegerValue(record.finishTime),
                "  AND question_nbr=", LogicUtils.sqlIntegerValue(record.questionNbr));

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
     * Deletes all records related to a single challenge exam attempt.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean deleteAllForAttempt(final Cache cache, final RawStchallenge record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stchallengeqa ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course),
                "  AND exam_dt=", LogicUtils.sqlDateValue(record.examDt));

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
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStchallengeqa> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM stchallengeqa";

        final List<RawStchallengeqa> result = new ArrayList<>(500);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStchallengeqa.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
