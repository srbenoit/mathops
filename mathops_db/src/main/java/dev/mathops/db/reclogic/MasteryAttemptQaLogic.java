package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.MasteryAttemptQaRec;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * A utility class to work with mastery_attempt_qa records.
 */
public abstract class MasteryAttemptQaLogic implements IRecLogic<MasteryAttemptQaRec> {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private MasteryAttemptQaLogic() {

        super();
    }

    /**
     * Gets the instance of {@code MasteryAttemptQaLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code MasteryAttemptQaLogic} object (null if none found)
     */
    public static MasteryAttemptQaLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        MasteryAttemptQaLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
        }

        return result;
    }

    /**
     * Updates the "correct" field of a mastery attempt question answer.
     *
     * @param cache      the data cache
     * @param record     the record to update
     * @param newCorrect the new 'correct' field value
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the query
     */
    public abstract boolean updateCorrect(Cache cache, MasteryAttemptQaRec record,
                                          final String newCorrect) throws SQLException;

    /**
     * Queries for all question answers for a mastery attempt.
     *
     * @param cache     the data cache
     * @param serialNbr the student ID
     * @param examId    the student ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryAttemptQaRec> queryByAttempt(final Cache cache, final Integer serialNbr,
                                                             final String examId) throws SQLException;

    /**
     * Queries for a single mastery attempt record.
     *
     * @param cache       the data cache
     * @param serialNbr   the serial number of the attempt for which to query
     * @param examId      the exam ID of the attempt for which to query
     * @param questionNbr the question number of the question answer for which to query
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract MasteryAttemptQaRec query(Cache cache, Integer serialNbr, String examId,
                                              Integer questionNbr) throws SQLException;

    /**
     * A subclass of {@code MasteryAttemptQaLogic} designed for the Informix schema.
     */
    public static final class Informix extends MasteryAttemptQaLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

        /** A field name for serialization of records. */
        private static final String FLD_SERIAL_NBR = "serial_nbr";

        /** A field name for serialization of records. */
        private static final String FLD_EXAM_ID = "exam_id";

        /** A field name for serialization of records. */
        private static final String FLD_QUESTION_NBR = "question_nbr";

        /** A field name for serialization of records. */
        private static final String FLD_CORRECT = "correct";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final MasteryAttemptQaRec record) throws SQLException {

            if (record.serialNbr == null || record.examId == null || record.questionNbr == null
                || record.correct == null) {
                throw new SQLException("Null value in required field.");
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO mastery_attempt_qa (serial_nbr,exam_id,question_nbr,correct) VALUES (",
                    sqlIntegerValue(record.serialNbr), ",",
                    sqlStringValue(record.examId), ",",
                    sqlIntegerValue(record.questionNbr), ",",
                    sqlStringValue(record.correct), ")");

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean delete(final Cache cache, final MasteryAttemptQaRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM mastery_attempt_qa WHERE serial_nbr=",
                    sqlIntegerValue(record.serialNbr), " AND exam_id=",
                    sqlStringValue(record.examId), " AND question_nbr=",
                    sqlIntegerValue(record.questionNbr));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Updates the "correct" field of a mastery attempt question answer.
         *
         * @param cache      the data cache
         * @param record     the record to update
         * @param newCorrect the new 'correct' field value
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public boolean updateCorrect(final Cache cache, final MasteryAttemptQaRec record,
                                     final String newCorrect) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE mastery_attempt_qa SET correct=",
                    sqlStringValue(newCorrect), " WHERE serial_nbr=", sqlIntegerValue(record.serialNbr),
                    " AND exam_id=", sqlStringValue(record.examId),
                    " AND question_nbr=", sqlIntegerValue(record.questionNbr));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptQaRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM mastery_attempt_qa");
        }

        /**
         * Queries for all question answers for a mastery attempt.
         *
         * @param cache     the data cache
         * @param serialNbr the student ID
         * @param examId    the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptQaRec> queryByAttempt(final Cache cache, final Integer serialNbr,
                                                        final String examId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_attempt_qa WHERE serial_nbr=",
                    sqlIntegerValue(serialNbr), " AND exam_id=", sqlStringValue(examId));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a single mastery attempt record.
         *
         * @param cache       the data cache
         * @param serialNbr   the serial number of the attempt for which to query
         * @param examId      the exam ID of the attempt for which to query
         * @param questionNbr the question number of the question answer for which to query
         * @return the exam; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public MasteryAttemptQaRec query(final Cache cache, final Integer serialNbr,
                                         final String examId, final Integer questionNbr) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_attempt_qa WHERE serial_nbr=",
                    sqlIntegerValue(serialNbr), " AND exam_id=", sqlStringValue(examId),
                    " AND question_nbr=", sqlIntegerValue(questionNbr));

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public MasteryAttemptQaRec fromResultSet(final ResultSet rs) throws SQLException {

            final MasteryAttemptQaRec result = new MasteryAttemptQaRec();

            result.serialNbr = getIntegerField(rs, FLD_SERIAL_NBR);
            result.examId = getStringField(rs, FLD_EXAM_ID);
            result.questionNbr = getIntegerField(rs, FLD_QUESTION_NBR);
            result.correct = getStringField(rs, FLD_CORRECT);

            return result;
        }
    }

    /**
     * A subclass of {@code MasteryAttemptQaLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends MasteryAttemptQaLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

        /** A field name for serialization of records. */
        private static final String FLD_SERIAL_NBR = "serial_nbr";

        /** A field name for serialization of records. */
        private static final String FLD_EXAM_ID = "exam_id";

        /** A field name for serialization of records. */
        private static final String FLD_QUESTION_NBR = "question_nbr";

        /** A field name for serialization of records. */
        private static final String FLD_CORRECT = "correct";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final MasteryAttemptQaRec record) throws SQLException {

            if (record.serialNbr == null || record.examId == null || record.questionNbr == null
                || record.correct == null) {
                throw new SQLException("Null value in required field.");
            }

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".mastery_attempt_qa (serial_nbr,exam_id,question_nbr,correct) VALUES (",
                    sqlIntegerValue(record.serialNbr), ",",
                    sqlStringValue(record.examId), ",",
                    sqlIntegerValue(record.questionNbr), ",",
                    sqlStringValue(record.correct), ")");

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
        @Override
        public boolean delete(final Cache cache, final MasteryAttemptQaRec record) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".mastery_attempt_qa WHERE serial_nbr=", sqlIntegerValue(record.serialNbr),
                    " AND exam_id=", sqlStringValue(record.examId),
                    " AND question_nbr=", sqlIntegerValue(record.questionNbr));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Updates the "correct" field of a mastery attempt question answer.
         *
         * @param cache      the data cache
         * @param record     the record to update
         * @param newCorrect the new 'correct' field value
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public boolean updateCorrect(final Cache cache, final MasteryAttemptQaRec record,
                                     final String newCorrect) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".mastery_attempt_qa SET correct=", sqlStringValue(newCorrect),
                    " WHERE serial_nbr=", sqlIntegerValue(record.serialNbr),
                    " AND exam_id=", sqlStringValue(record.examId),
                    " AND question_nbr=", sqlIntegerValue(record.questionNbr));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptQaRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".mastery_attempt_qa");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all question answers for a mastery attempt.
         *
         * @param cache     the data cache
         * @param serialNbr the student ID
         * @param examId    the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptQaRec> queryByAttempt(final Cache cache, final Integer serialNbr,
                                                        final String examId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".mastery_attempt_qa WHERE serial_nbr=", sqlIntegerValue(serialNbr),
                    " AND exam_id=", sqlStringValue(examId));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a single mastery attempt record.
         *
         * @param cache       the data cache
         * @param serialNbr   the serial number of the attempt for which to query
         * @param examId      the exam ID of the attempt for which to query
         * @param questionNbr the question number of the question answer for which to query
         * @return the exam; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public MasteryAttemptQaRec query(final Cache cache, final Integer serialNbr, final String examId,
                                         final Integer questionNbr) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".mastery_attempt_qa WHERE serial_nbr=", sqlIntegerValue(serialNbr),
                    " AND exam_id=", sqlStringValue(examId),
                    " AND question_nbr=", sqlIntegerValue(questionNbr));

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public MasteryAttemptQaRec fromResultSet(final ResultSet rs) throws SQLException {

            final MasteryAttemptQaRec result = new MasteryAttemptQaRec();

            result.serialNbr = getIntegerField(rs, FLD_SERIAL_NBR);
            result.examId = getStringField(rs, FLD_EXAM_ID);
            result.questionNbr = getIntegerField(rs, FLD_QUESTION_NBR);
            result.correct = getStringField(rs, FLD_CORRECT);

            return result;
        }
    }
}
