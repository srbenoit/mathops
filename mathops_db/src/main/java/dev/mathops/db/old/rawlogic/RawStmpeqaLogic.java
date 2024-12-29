package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStmpeqa;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stmpeqa" records.
 *
 * <pre>
 * Table:  'stmpeqa'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * version              char(5)                   no      PK
 * exam_dt              date                      yes     PK
 * finish_time          integer                   yes     PK
 * question_nbr         smallint                  yes     PK
 * stu_answer           char(5)                   yes
 * ans_correct          char(1)                   yes
 * subtest              char(3)                   yes
 * tree_ref             char(40)                  yes
 * </pre>
 */
public final class RawStmpeqaLogic extends AbstractRawLogic<RawStmpeqa> {

    /** A single instance. */
    public static final RawStmpeqaLogic INSTANCE = new RawStmpeqaLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawStmpeqaLogic() {

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
    public boolean insert(final Cache cache, final RawStmpeqa record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawStmpeqa for test student:");
            Log.info("  Student ID: ", record.stuId);
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO stmpeqa (stu_id,version,exam_dt,finish_time,"
                                                    + "question_nbr,stu_answer,ans_correct,subtest,tree_ref) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.version), ",",
                    sqlDateValue(record.examDt), ",",
                    sqlIntegerValue(record.finishTime), ",",
                    sqlIntegerValue(record.questionNbr), ",",
                    sqlStringValue(record.stuAnswer), ",",
                    sqlStringValue(record.ansCorrect), ",",
                    sqlStringValue(record.subtest), ",",
                    sqlStringValue(record.treeRef), ")");

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
    public boolean delete(final Cache cache, final RawStmpeqa record) throws SQLException {

        final String sql1 = SimpleBuilder.concat("DELETE FROM stmpeqa ",
                "WHERE version=", sqlStringValue(record.version),
                "  AND stu_id=", sqlStringValue(record.stuId),
                "  AND exam_dt=", sqlDateValue(record.examDt),
                "  AND finish_time=", sqlIntegerValue(record.finishTime),
                "  AND question_nbr=", sqlIntegerValue(record.questionNbr));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql1) == 1;

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
    public List<RawStmpeqa> queryAll(final Cache cache) throws SQLException {

        final List<RawStmpeqa> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM stmpeqa")) {

            while (rs.next()) {
                result.add(RawStmpeqa.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Deletes all records for a placement attempt.
     *
     * @param cache  the data cache
     * @param record the placement attempt whose corresponding answer records to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */

    public static boolean deleteAllForExam(final Cache cache, final RawStmpe record) throws SQLException {

        final String sql1 = SimpleBuilder.concat("DELETE FROM stmpeqa ",
                "WHERE version=", sqlStringValue(record.version),
                "  AND stu_id=", sqlStringValue(record.stuId),
                "  AND exam_dt=", sqlDateValue(record.examDt),
                "  AND finish_time=", sqlIntegerValue(record.finishTime));

        try (final Statement stmt = cache.conn.createStatement()) {
            stmt.executeUpdate(sql1);
            cache.conn.commit();
            return true;
        }
    }
}
