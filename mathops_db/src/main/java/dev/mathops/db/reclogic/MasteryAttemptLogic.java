package dev.mathops.db.reclogic;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.rec.MasteryAttemptRec;
import dev.mathops.db.reclogic.iface.IInformixRecLogic;
import dev.mathops.db.reclogic.iface.IPostgresRecLogic;
import dev.mathops.db.reclogic.iface.IRecLogic;
import dev.mathops.db.reclogic.query.DateTimeCriteria;
import dev.mathops.db.reclogic.query.IntegerCriteria;
import dev.mathops.db.reclogic.query.StringCriteria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to work with mastery_attempt records.
 */
public abstract class MasteryAttemptLogic implements IRecLogic<MasteryAttemptRec> {

    /** A single instance. */
    public static final Informix INFORMIX = new Informix();

    /** A single instance. */
    public static final Postgres POSTGRES = new Postgres();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private MasteryAttemptLogic() {

        super();
    }

    /**
     * Gets the instance of {@code MasteryAttemptLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code MasteryAttemptLogic} object (null if none found)
     */
    public static MasteryAttemptLogic get(final Cache cache) {

        final EDbInstallationType type = IRecLogic.getDbType(cache);

        MasteryAttemptLogic result = null;
        if (type == EDbInstallationType.INFORMIX) {
            result = INFORMIX;
        } else if (type == EDbInstallationType.POSTGRESQL) {
            result = POSTGRES;
        }

        return result;
    }

    /**
     * Queries for all mastery attempts for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryAttemptRec> queryByStudent(Cache cache, String stuId)
            throws SQLException;

    /**
     * Queries for all mastery attempts for an exam.
     *
     * @param cache  the data cache
     * @param examId the exam ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryAttemptRec> queryByExam(Cache cache, String examId)
            throws SQLException;

    /**
     * Queries for all mastery attempts for a student on an exam.
     *
     * @param cache      the data cache
     * @param stuId      the student ID
     * @param examId     the exam ID
     * @param passedOnly true to only return records with "passed" set to"Y"
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryAttemptRec> queryByStudentExam(Cache cache, String stuId,
                                                               String examId, boolean passedOnly) throws SQLException;

    /**
     * Queries for a single mastery attempt record.
     *
     * @param cache     the data cache
     * @param serialNbr the serial number of the attempt for which to query
     * @param examId    the exam ID of the attempt for which to query
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract MasteryAttemptRec query(Cache cache, Integer serialNbr, String examId)
            throws SQLException;

    /**
     * Queries for all records matching given criteria.
     *
     * @param cache         the data cache
     * @param queryCriteria the general query criteria
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryAttemptRec> generalQuery(Cache cache, Criteria queryCriteria)
            throws SQLException;

    /**
     * A "mastery_attempt" criteria record used to perform arbitrary queries.
     */
    public static final class Criteria {

        /** The criteria for the 'serial_nbr' field. */
        IntegerCriteria serialNbr;

        /** The criteria for the 'exam_id' field. */
        StringCriteria examId;

        /** The criteria for the 'stu_id' field. */
        StringCriteria stuId;

        /** The criteria for the 'when_started' field. */
        DateTimeCriteria whenStarted;

        /** The criteria for the 'when_finished' field. */
        DateTimeCriteria whenFinished;

        /** The criteria for the 'exam_score' field. */
        IntegerCriteria examScore;

        /** The criteria for the 'mastery_score' field. */
        IntegerCriteria masteryScore;

        /** The criteria for the 'passed' field. */
        StringCriteria passed;

        /** The criteria for the 'is_first_passed' field. */
        StringCriteria isFirstPassed;

        /** The criteria for the 'exam_source' field. */
        StringCriteria examSource;

        /**
         * Constructs a new {@code Criteria}.
         */
        public Criteria() {

            // No action
        }
    }

    /**
     * A subclass of {@code MasteryAttemptLogic} designed for the Informix schema.
     */
    public static final class Informix extends MasteryAttemptLogic
            implements IInformixRecLogic<MasteryAttemptRec> {

        /** A field name for serialization of records. */
        private static final String FLD_SERIAL_NBR = "serial_nbr";

        /** A field name for serialization of records. */
        private static final String FLD_EXAM_ID = "exam_id";

        /** A field name for serialization of records. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name for serialization of records. */
        private static final String FLD_WHEN_STARTED = "when_started";

        /** A field name for serialization of records. */
        private static final String FLD_WHEN_FINISHED = "when_finished";

        /** A field name for serialization of records. */
        private static final String FLD_EXAM_SCORE = "exam_score";

        /** A field name for serialization of records. */
        private static final String FLD_MASTERY_SCORE = "mastery_score";

        /** A field name for serialization of records. */
        private static final String FLD_PASSED = "passed";

        /** A field name for serialization of records. */
        private static final String FLD_IS_FIRST_PASSED = "is_first_passed";

        /** A field name for serialization of records. */
        private static final String FLD_EXAM_SOURCE = "exam_source";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final MasteryAttemptRec record)
                throws SQLException {

            if (record.serialNbr == null || record.examId == null || record.stuId == null
                    || record.whenStarted == null || record.whenFinished == null
                    || record.examScore == null || record.passed == null) {
                throw new SQLException("Null value in required field.");
            }

            final String sql = SimpleBuilder.concat( //
                    "INSERT INTO mastery_attempt (serial_nbr,exam_id,stu_id,",
                    "when_started,when_finished,exam_score,mastery_score,passed,",
                    "is_first_passed,exam_source) VALUES (",
                    sqlIntegerValue(record.serialNbr), ",",
                    sqlStringValue(record.examId), ",",
                    sqlStringValue(record.stuId), ",",
                    sqlDateTimeValue(record.whenStarted), ",",
                    sqlDateTimeValue(record.whenFinished), ",",
                    sqlIntegerValue(record.examScore), ",",
                    sqlIntegerValue(record.masteryScore), ",",
                    sqlStringValue(record.passed), ",",
                    sqlStringValue(record.isFirstPassed), ",",
                    sqlStringValue(record.examSource), ")");

            return doUpdateOneRow(cache, sql);
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
        public boolean delete(final Cache cache, final MasteryAttemptRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "DELETE FROM mastery_attempt WHERE serial_nbr=",
                    sqlIntegerValue(record.serialNbr), " AND exam_id=",
                    sqlStringValue(record.examId));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, "SELECT * FROM mastery_attempt");
        }

        /**
         * Queries for all mastery attempts for a student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> queryByStudent(final Cache cache, final String stuId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_attempt ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all mastery attempts for an exam.
         *
         * @param cache  the data cache
         * @param examId the exam ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> queryByExam(final Cache cache, final String examId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_attempt ",
                    "WHERE exam_id=", sqlStringValue(examId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all mastery attempts for a student on an exam.
         *
         * @param cache      the data cache
         * @param stuId      the student ID
         * @param examId     the exam ID
         * @param passedOnly true to only return records with "passed" set to"Y"
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> queryByStudentExam(final Cache cache, final String stuId,
                                                          final String examId, final boolean passedOnly) throws SQLException {

            final String sql;

            if (passedOnly) {
                sql = SimpleBuilder.concat("SELECT * FROM mastery_attempt ",
                        "WHERE stu_id=", sqlStringValue(stuId),
                        " AND exam_id=", sqlStringValue(examId),
                        " AND passed='Y'");
            } else {
                sql = SimpleBuilder.concat("SELECT * FROM mastery_attempt ",
                        "WHERE stu_id=", sqlStringValue(stuId),
                        " AND exam_id=", sqlStringValue(examId));
            }

            return doListQuery(cache, sql);
        }

        /**
         * Queries for a single mastery attempt record.
         *
         * @param cache     the data cache
         * @param serialNbr the serial number of the attempt for which to query
         * @param examId    the exam ID of the attempt for which to query
         * @return the exam; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public MasteryAttemptRec query(final Cache cache, final Integer serialNbr,
                                       final String examId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_attempt ",
                    "WHERE serial_nbr=", sqlIntegerValue(serialNbr),
                    " AND exam_id=", sqlStringValue(examId));

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for all records matching given criteria.
         *
         * @param cache         the data cache
         * @param queryCriteria the general query criteria
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> generalQuery(final Cache cache, final Criteria queryCriteria)
                throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM mastery_attempt");

            String w = integerWhere(sql, WHERE, "serial_nbr", queryCriteria.serialNbr);
            w = stringWhere(sql, w, "exam_id", queryCriteria.examId);
            w = stringWhere(sql, w, "stu_id", queryCriteria.stuId);
            w = dateTimeWhere(sql, w, "when_started", queryCriteria.whenStarted);
            w = dateTimeWhere(sql, w, "whenFinished", queryCriteria.whenFinished);
            w = integerWhere(sql, w, "exam_score", queryCriteria.examScore);
            w = integerWhere(sql, w, "mastery_score", queryCriteria.masteryScore);
            w = stringWhere(sql, w, "passed", queryCriteria.passed);
            w = stringWhere(sql, w, "is_first_passed", queryCriteria.isFirstPassed);
            stringWhere(sql, w, "exam_source", queryCriteria.examSource);

            return doListQuery(cache, sql.toString());
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public MasteryAttemptRec fromResultSet(final ResultSet rs) throws SQLException {

            final MasteryAttemptRec result = new MasteryAttemptRec();

            result.serialNbr = getIntegerField(rs, FLD_SERIAL_NBR);
            result.examId = getStringField(rs, FLD_EXAM_ID);
            result.stuId = getStringField(rs, FLD_STU_ID);
            result.whenStarted = getDateTimeField(rs, FLD_WHEN_STARTED);
            result.whenFinished = getDateTimeField(rs, FLD_WHEN_FINISHED);
            result.examScore = getIntegerField(rs, FLD_EXAM_SCORE);
            result.masteryScore = getIntegerField(rs, FLD_MASTERY_SCORE);
            result.passed = getStringField(rs, FLD_PASSED);
            result.isFirstPassed = getStringField(rs, FLD_IS_FIRST_PASSED);
            result.examSource = getStringField(rs, FLD_EXAM_SOURCE);

            return result;
        }
    }

    /**
     * A subclass of {@code MasteryAttemptLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends MasteryAttemptLogic
            implements IPostgresRecLogic<MasteryAttemptRec> {

        /** A field name for serialization of records. */
        private static final String FLD_SERIAL_NBR = "serial_nbr";

        /** A field name for serialization of records. */
        private static final String FLD_EXAM_ID = "exam_id";

        /** A field name for serialization of records. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name for serialization of records. */
        private static final String FLD_WHEN_STARTED = "when_started";

        /** A field name for serialization of records. */
        private static final String FLD_WHEN_FINISHED = "when_finished";

        /** A field name for serialization of records. */
        private static final String FLD_EXAM_SCORE = "exam_score";

        /** A field name for serialization of records. */
        private static final String FLD_MASTERY_SCORE = "mastery_score";

        /** A field name for serialization of records. */
        private static final String FLD_PASSED = "passed";

        /** A field name for serialization of records. */
        private static final String FLD_IS_FIRST_PASSED = "is_first_passed";

        /** A field name for serialization of records. */
        private static final String FLD_EXAM_SOURCE = "exam_source";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final MasteryAttemptRec record)
                throws SQLException {

            if (record.serialNbr == null || record.examId == null || record.stuId == null
                    || record.whenStarted == null || record.whenFinished == null
                    || record.examScore == null || record.passed == null) {
                throw new SQLException("Null value in required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    cache.termSchemaName, ".mastery_attempt ",
                    "(serial_nbr,exam_id,stu_id,when_started,when_finished,exam_score,",
                    "mastery_score,passed,is_first_passed,exam_source) VALUES (",
                    sqlIntegerValue(record.serialNbr), ",",
                    sqlStringValue(record.examId), ",",
                    sqlStringValue(record.stuId), ",",
                    sqlDateTimeValue(record.whenStarted), ",",
                    sqlDateTimeValue(record.whenFinished), ",",
                    sqlIntegerValue(record.examScore), ",",
                    sqlIntegerValue(record.masteryScore), ",",
                    sqlStringValue(record.passed), ",",
                    sqlStringValue(record.isFirstPassed), ",",
                    sqlStringValue(record.examSource), ")");

            return doUpdateOneRow(cache, sql);
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
        public boolean delete(final Cache cache, final MasteryAttemptRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM ",
                    cache.termSchemaName, ".mastery_attempt ",
                    "WHERE serial_nbr=", sqlIntegerValue(record.serialNbr),
                    " AND exam_id=", sqlStringValue(record.examId));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> queryAll(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.termSchemaName, ".mastery_attempt");

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all mastery attempts for a student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> queryByStudent(final Cache cache, final String stuId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.termSchemaName, ".mastery_attempt ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all mastery attempts for an exam.
         *
         * @param cache  the data cache
         * @param examId the exam ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> queryByExam(final Cache cache, final String examId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.termSchemaName, ".mastery_attempt ",
                    "WHERE exam_id=", sqlStringValue(examId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all mastery attempts for a student on an exam.
         *
         * @param cache      the data cache
         * @param stuId      the student ID
         * @param examId     the exam ID
         * @param passedOnly true to only return records with "passed" set to"Y"
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> queryByStudentExam(final Cache cache, final String stuId,
                                                          final String examId, final boolean passedOnly) throws SQLException {

            final String sql;

            if (passedOnly) {
                sql = SimpleBuilder.concat("SELECT * FROM ",
                        cache.termSchemaName, ".mastery_attempt ",
                        "WHERE stu_id=", sqlStringValue(stuId),
                        " AND exam_id=", sqlStringValue(examId),
                        " AND passed='Y'");
            } else {
                sql = SimpleBuilder.concat("SELECT * FROM ",
                        cache.termSchemaName, ".mastery_attempt ",
                        "WHERE stu_id=", sqlStringValue(stuId),
                        " AND exam_id=", sqlStringValue(examId));
            }

            return doListQuery(cache, sql);
        }

        /**
         * Queries for a single mastery attempt record.
         *
         * @param cache     the data cache
         * @param serialNbr the serial number of the attempt for which to query
         * @param examId    the exam ID of the attempt for which to query
         * @return the exam; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public MasteryAttemptRec query(final Cache cache, final Integer serialNbr,
                                       final String examId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.termSchemaName, ".mastery_attempt ",
                    "WHERE serial_nbr=", sqlIntegerValue(serialNbr),
                    " AND exam_id=", sqlStringValue(examId));

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for all records matching given criteria.
         *
         * @param cache         the data cache
         * @param queryCriteria the general query criteria
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryAttemptRec> generalQuery(final Cache cache, final Criteria queryCriteria)
                throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM ", cache.termSchemaName,
                    ".mastery_attempt");

            String w = integerWhere(sql, WHERE, "serial_nbr", queryCriteria.serialNbr);
            w = stringWhere(sql, w, "exam_id", queryCriteria.examId);
            w = stringWhere(sql, w, "stu_id", queryCriteria.stuId);
            w = dateTimeWhere(sql, w, "when_started", queryCriteria.whenStarted);
            w = dateTimeWhere(sql, w, "whenFinished", queryCriteria.whenFinished);
            w = integerWhere(sql, w, "exam_score", queryCriteria.examScore);
            w = integerWhere(sql, w, "mastery_score", queryCriteria.masteryScore);
            w = stringWhere(sql, w, "passed", queryCriteria.passed);
            w = stringWhere(sql, w, "is_first_passed", queryCriteria.isFirstPassed);
            stringWhere(sql, w, "exam_source", queryCriteria.examSource);

            return doListQuery(cache, sql.toString());
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public MasteryAttemptRec fromResultSet(final ResultSet rs) throws SQLException {

            final MasteryAttemptRec result = new MasteryAttemptRec();

            result.serialNbr = getIntegerField(rs, FLD_SERIAL_NBR);
            result.examId = getStringField(rs, FLD_EXAM_ID);
            result.stuId = getStringField(rs, FLD_STU_ID);
            result.whenStarted = getDateTimeField(rs, FLD_WHEN_STARTED);
            result.whenFinished = getDateTimeField(rs, FLD_WHEN_FINISHED);
            result.examScore = getIntegerField(rs, FLD_EXAM_SCORE);
            result.masteryScore = getIntegerField(rs, FLD_MASTERY_SCORE);
            result.passed = getStringField(rs, FLD_PASSED);
            result.isFirstPassed = getStringField(rs, FLD_IS_FIRST_PASSED);
            result.examSource = getStringField(rs, FLD_EXAM_SOURCE);

            return result;
        }
    }
}
