package dev.mathops.db.reclogic;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.EDbInstallationType;
import dev.mathops.db.rec.StudentUnitMasteryRec;
import dev.mathops.db.reclogic.iface.IInformixRecLogic;
import dev.mathops.db.reclogic.iface.IPostgresRecLogic;
import dev.mathops.db.reclogic.iface.IRecLogic;
import dev.mathops.db.reclogic.query.IntegerCriteria;
import dev.mathops.db.reclogic.query.StringCriteria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to work with stu_unit_mastery records.
 */
public abstract class StudentUnitMasteryLogic implements IRecLogic<StudentUnitMasteryRec> {

    /** A single instance. */
    public static final Informix INFORMIX = new Informix();

    /** A single instance. */
    public static final Postgres POSTGRES = new Postgres();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentUnitMasteryLogic() {

        super();
    }

    /**
     * Gets the instance of {@code StudentUnitMasteryLogic} appropriate to a cache. The result will depend on the
     * database installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code StudentUnitMasteryLogic} object (null if none found)
     */
    public static StudentUnitMasteryLogic get(final Cache cache) {

        final EDbInstallationType type = IRecLogic.getDbType(cache);

        StudentUnitMasteryLogic result = null;
        if (type == EDbInstallationType.INFORMIX) {
            result = INFORMIX;
        } else if (type == EDbInstallationType.POSTGRESQL) {
            result = POSTGRES;
        }

        return result;
    }

    /**
     * Updates the student's current score in the unit.
     *
     * @param cache    the data cache
     * @param record   the record to be updated
     * @param newScore the new score
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateScore(Cache cache, StudentUnitMasteryRec record, Integer newScore)
            throws SQLException;

    /**
     * Updates the student's current status in the Skills Review in the unit.
     *
     * @param cache       the data cache
     * @param record      the record to be updated
     * @param newSrStatus the new status
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateSrStatus(Cache cache, StudentUnitMasteryRec record,
                                           String newSrStatus) throws SQLException;

    /**
     * Updates the student's current status in Standard 1 in the unit.
     *
     * @param cache       the data cache
     * @param record      the record to be updated
     * @param newS1Status the new status
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateS1Status(Cache cache, StudentUnitMasteryRec record,
                                           String newS1Status) throws SQLException;

    /**
     * Updates the student's current status in Standard 2 in the unit.
     *
     * @param cache       the data cache
     * @param record      the record to be updated
     * @param newS2Status the new status
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateS2Status(Cache cache, StudentUnitMasteryRec record,
                                           String newS2Status) throws SQLException;

    /**
     * Updates the student's current status in Standard 3 in the unit.
     *
     * @param cache       the data cache
     * @param record      the record to be updated
     * @param newS3Status the new status
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateS3Status(Cache cache, StudentUnitMasteryRec record,
                                           String newS3Status) throws SQLException;

    /**
     * Queries for all unit mastery status records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentUnitMasteryRec> queryByStudent(Cache cache, String stuId)
            throws SQLException;

    /**
     * Queries for all unit mastery status records for a student in a course.
     *
     * @param cache    the data cache
     * @param stuId    the student ID
     * @param courseId the course ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentUnitMasteryRec> queryByStudentCourse(Cache cache, String stuId,
                                                                     String courseId) throws SQLException;

    /**
     * Queries for a single unit mastery record by student and unit.
     *
     * @param cache    the data cache
     * @param stuId    the student ID
     * @param courseId the course ID
     * @param unit     the unit
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract StudentUnitMasteryRec query(Cache cache, String stuId, String courseId,
                                                Integer unit) throws SQLException;

    /**
     * Queries for all records matching given criteria.
     *
     * @param cache         the data cache
     * @param queryCriteria the general query criteria
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentUnitMasteryRec> generalQuery(Cache cache, Criteria queryCriteria)
            throws SQLException;

    /**
     * A "student unit mastery" criteria record used to perform arbitrary queries.
     */
    public static class Criteria {

        /** The criteria for the 'stu_id' field. */
        StringCriteria stuId;

        /** The criteria for the 'course_id' field. */
        StringCriteria courseId;

        /** The criteria for the 'unit' field. */
        IntegerCriteria unit;

        /** The criteria for the 'score' field. */
        IntegerCriteria score;

        /** The criteria for the 'sr_status' field. */
        StringCriteria srStatus;

        /** The criteria for the 's1_status' field. */
        StringCriteria s1Status;

        /** The criteria for the 's2_status' field. */
        StringCriteria s2Status;

        /** The criteria for the 's3_status' field. */
        StringCriteria s3Status;

        /**
         * Constructs a new {@code Criteria}.
         */
        public Criteria() {

            // No action
        }
    }

    /**
     * A subclass of {@code StudentUnitMasteryLogic} designed for the Informix schema.
     */
    public static final class Informix extends StudentUnitMasteryLogic
            implements IInformixRecLogic<StudentUnitMasteryRec> {

        /** A field name. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        private static final String FLD_COURSE_ID = "course_id";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_SCORE = "score";

        /** A field name. */
        private static final String FLD_SR_STATUS = "sr_status";

        /** A field name. */
        private static final String FLD_S1_STATUS = "s1_status";

        /** A field name. */
        private static final String FLD_S2_STATUS = "s2_status";

        /** A field name. */
        private static final String FLD_S3_STATUS = "s3_status";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StudentUnitMasteryRec record) throws SQLException {

            if (record.stuId == null || record.courseId == null || record.unit == null || record.score == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat(//
                    "INSERT INTO stu_unit_mastery (stu_id,course_id,unit,",
                    "score,sr_status,s1_status,s2_status,s3_status) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.score), ",",
                    sqlStringValue(record.srStatus), ",",
                    sqlStringValue(record.s1Status), ",",
                    sqlStringValue(record.s2Status), ",",
                    sqlStringValue(record.s3Status), ")");

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
        public boolean delete(final Cache cache, final StudentUnitMasteryRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "DELETE FROM stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

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
        public List<StudentUnitMasteryRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, "SELECT * FROM stu_unit_mastery");
        }

        /**
         * Updates the student's current score in the unit.
         *
         * @param cache    the data cache
         * @param record   the record to be updated
         * @param newScore the new score
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateScore(final Cache cache, final StudentUnitMasteryRec record,
                                   final Integer newScore) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET score=", sqlIntegerValue(newScore),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the student's current status in the Skills Review in the unit.
         *
         * @param cache       the data cache
         * @param record      the record to be updated
         * @param newSrStatus the new status
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateSrStatus(final Cache cache, final StudentUnitMasteryRec record,
                                      final String newSrStatus) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET sr_status=", sqlStringValue(newSrStatus),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the student's current status in Standard 1 in the unit.
         *
         * @param cache       the data cache
         * @param record      the record to be updated
         * @param newS1Status the new status
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateS1Status(final Cache cache, final StudentUnitMasteryRec record,
                                      final String newS1Status) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET s1_status=", sqlStringValue(newS1Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the student's current status in Standard 2 in the unit.
         *
         * @param cache       the data cache
         * @param record      the record to be updated
         * @param newS2Status the new status
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateS2Status(final Cache cache, final StudentUnitMasteryRec record,
                                      final String newS2Status) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET s2_status=", sqlStringValue(newS2Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the student's current status in Standard 3 in the unit.
         *
         * @param cache       the data cache
         * @param record      the record to be updated
         * @param newS3Status the new status
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateS3Status(final Cache cache, final StudentUnitMasteryRec record,
                                      final String newS3Status) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET s3_status=", sqlStringValue(newS3Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Queries for all unit mastery status records for a student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentUnitMasteryRec> queryByStudent(final Cache cache, final String stuId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all unit mastery status records for a student in a course.
         *
         * @param cache    the data cache
         * @param stuId    the student ID
         * @param courseId the course ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentUnitMasteryRec> queryByStudentCourse(final Cache cache,
                                                                final String stuId, final String courseId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for a single unit mastery record by student and unit.
         *
         * @param cache    the data cache
         * @param stuId    the student ID
         * @param courseId the course ID
         * @param unit     the unit
         * @return the record; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public StudentUnitMasteryRec query(final Cache cache, final String stuId,
                                           final String courseId, final Integer unit) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit));

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for all records matching given criteria.
         *
         * @param cache the data cache
         * @param queryCriteria  the general query criteria
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentUnitMasteryRec> generalQuery(final Cache cache, final Criteria queryCriteria)
                throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM stu_unit_mastery");

            String w = stringWhere(sql, WHERE, "stu_id", queryCriteria.stuId);
            w = stringWhere(sql, w, "course_id", queryCriteria.courseId);
            w = integerWhere(sql, w, "unit", queryCriteria.unit);
            w = integerWhere(sql, w, "score", queryCriteria.score);
            w = stringWhere(sql, w, "sr_status", queryCriteria.srStatus);
            w = stringWhere(sql, w, "s1_status", queryCriteria.s1Status);
            w = stringWhere(sql, w, "s2_status", queryCriteria.s2Status);
            stringWhere(sql, w, "s3_status", queryCriteria.s3Status);

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
        public StudentUnitMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

            final StudentUnitMasteryRec result = new StudentUnitMasteryRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.courseId = getStringField(rs, FLD_COURSE_ID);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.score = getIntegerField(rs, FLD_SCORE);
            result.srStatus = getStringField(rs, FLD_SR_STATUS);
            result.s1Status = getStringField(rs, FLD_S1_STATUS);
            result.s2Status = getStringField(rs, FLD_S2_STATUS);
            result.s3Status = getStringField(rs, FLD_S3_STATUS);

            return result;
        }
    }

    /**
     * A subclass of {@code StudentUnitMasteryLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends StudentUnitMasteryLogic
            implements IPostgresRecLogic<StudentUnitMasteryRec> {

        /** A field name. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        private static final String FLD_COURSE_ID = "course_id";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_SCORE = "score";

        /** A field name. */
        private static final String FLD_SR_STATUS = "sr_status";

        /** A field name. */
        private static final String FLD_S1_STATUS = "s1_status";

        /** A field name. */
        private static final String FLD_S2_STATUS = "s2_status";

        /** A field name. */
        private static final String FLD_S3_STATUS = "s3_status";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StudentUnitMasteryRec record)
                throws SQLException {

            if (record.stuId == null || record.courseId == null || record.unit == null
                    || record.score == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    cache.termSchemaName, ".stu_unit_mastery (stu_id,course_id,unit,",
                    "score,sr_status,s1_status,s2_status,s3_status) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.score), ",",
                    sqlStringValue(record.srStatus), ",",
                    sqlStringValue(record.s1Status), ",",
                    sqlStringValue(record.s2Status), ",",
                    sqlStringValue(record.s3Status), ")");

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
        public boolean delete(final Cache cache, final StudentUnitMasteryRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

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
        public List<StudentUnitMasteryRec> queryAll(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.termSchemaName, ".stu_unit_mastery");

            return doListQuery(cache, sql);
        }

        /**
         * Updates the student's current score in the unit.
         *
         * @param cache    the data cache
         * @param record   the record to be updated
         * @param newScore the new score
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateScore(final Cache cache, final StudentUnitMasteryRec record,
                                   final Integer newScore) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "SET score=", sqlIntegerValue(newScore),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the student's current status in the Skills Review in the unit.
         *
         * @param cache       the data cache
         * @param record      the record to be updated
         * @param newSrStatus the new status
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateSrStatus(final Cache cache, final StudentUnitMasteryRec record,
                                      final String newSrStatus) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "SET sr_status=", sqlStringValue(newSrStatus),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the student's current status in Standard 1 in the unit.
         *
         * @param cache       the data cache
         * @param record      the record to be updated
         * @param newS1Status the new status
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateS1Status(final Cache cache, final StudentUnitMasteryRec record,
                                      final String newS1Status) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "SET s1_status=", sqlStringValue(newS1Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the student's current status in Standard 2 in the unit.
         *
         * @param cache       the data cache
         * @param record      the record to be updated
         * @param newS2Status the new status
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateS2Status(final Cache cache, final StudentUnitMasteryRec record,
                                      final String newS2Status) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "SET s2_status=", sqlStringValue(newS2Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Updates the student's current status in Standard 3 in the unit.
         *
         * @param cache       the data cache
         * @param record      the record to be updated
         * @param newS3Status the new status
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateS3Status(final Cache cache, final StudentUnitMasteryRec record,
                                      final String newS3Status) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "SET s3_status=", sqlStringValue(newS3Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, sql);
        }

        /**
         * Queries for all unit mastery status records for a student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentUnitMasteryRec> queryByStudent(final Cache cache, final String stuId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all unit mastery status records for a student in a course.
         *
         * @param cache    the data cache
         * @param stuId    the student ID
         * @param courseId the course ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentUnitMasteryRec> queryByStudentCourse(final Cache cache,
                                                                final String stuId, final String courseId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId));

            return doListQuery(cache, sql);
        }

        /**
         * Queries for a single unit mastery record by student and unit.
         *
         * @param cache    the data cache
         * @param stuId    the student ID
         * @param courseId the course ID
         * @param unit     the unit
         * @return the record; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public StudentUnitMasteryRec query(final Cache cache, final String stuId,
                                           final String courseId, final Integer unit) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.termSchemaName, ".stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit));

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
        public List<StudentUnitMasteryRec> generalQuery(final Cache cache, final Criteria queryCriteria)
                throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM  ", cache.termSchemaName, ".stu_unit_mastery");

            String w = stringWhere(sql, WHERE, "stu_id", queryCriteria.stuId);
            w = stringWhere(sql, w, "course_id", queryCriteria.courseId);
            w = integerWhere(sql, w, "unit", queryCriteria.unit);
            w = integerWhere(sql, w, "score", queryCriteria.score);
            w = stringWhere(sql, w, "sr_status", queryCriteria.srStatus);
            w = stringWhere(sql, w, "s1_status", queryCriteria.s1Status);
            w = stringWhere(sql, w, "s2_status", queryCriteria.s2Status);
            stringWhere(sql, w, "s3_status", queryCriteria.s3Status);

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
        public StudentUnitMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

            final StudentUnitMasteryRec result = new StudentUnitMasteryRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.courseId = getStringField(rs, FLD_COURSE_ID);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.score = getIntegerField(rs, FLD_SCORE);
            result.srStatus = getStringField(rs, FLD_SR_STATUS);
            result.s1Status = getStringField(rs, FLD_S1_STATUS);
            result.s2Status = getStringField(rs, FLD_S2_STATUS);
            result.s3Status = getStringField(rs, FLD_S3_STATUS);

            return result;
        }
    }
}
