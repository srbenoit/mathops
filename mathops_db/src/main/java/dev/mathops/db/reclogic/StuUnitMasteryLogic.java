package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.StuUnitMasteryRec;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to work with stu_unit_mastery records.
 */
public abstract class StuUnitMasteryLogic implements IRecLogic<StuUnitMasteryRec> {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StuUnitMasteryLogic() {

        super();
    }

    /**
     * Gets the instance of {@code StudentUnitMasteryLogic} appropriate to a cache. The result will depend on the
     * database installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code StudentUnitMasteryLogic} object (null if none found)
     */
    public static StuUnitMasteryLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        StuUnitMasteryLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
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
    public abstract boolean updateScore(Cache cache, StuUnitMasteryRec record, Integer newScore)
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
    public abstract boolean updateSrStatus(Cache cache, StuUnitMasteryRec record,
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
    public abstract boolean updateS1Status(Cache cache, StuUnitMasteryRec record,
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
    public abstract boolean updateS2Status(Cache cache, StuUnitMasteryRec record,
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
    public abstract boolean updateS3Status(Cache cache, StuUnitMasteryRec record,
                                           String newS3Status) throws SQLException;

    /**
     * Queries for all unit mastery status records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StuUnitMasteryRec> queryByStudent(Cache cache, String stuId)
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
    public abstract List<StuUnitMasteryRec> queryByStudentCourse(Cache cache, String stuId,
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
    public abstract StuUnitMasteryRec query(Cache cache, String stuId, String courseId,
                                            Integer unit) throws SQLException;

    /**
     * A subclass of {@code StudentUnitMasteryLogic} designed for the Informix schema.
     */
    public static final class Informix extends StuUnitMasteryLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

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
        public boolean insert(final Cache cache, final StuUnitMasteryRec record) throws SQLException {

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
        public boolean delete(final Cache cache, final StuUnitMasteryRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "DELETE FROM stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

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
        public List<StuUnitMasteryRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM stu_unit_mastery");
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
        public boolean updateScore(final Cache cache, final StuUnitMasteryRec record,
                                   final Integer newScore) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET score=", sqlIntegerValue(newScore),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean updateSrStatus(final Cache cache, final StuUnitMasteryRec record,
                                      final String newSrStatus) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET sr_status=", sqlStringValue(newSrStatus),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean updateS1Status(final Cache cache, final StuUnitMasteryRec record,
                                      final String newS1Status) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET s1_status=", sqlStringValue(newS1Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean updateS2Status(final Cache cache, final StuUnitMasteryRec record,
                                      final String newS2Status) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET s2_status=", sqlStringValue(newS2Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean updateS3Status(final Cache cache, final StuUnitMasteryRec record,
                                      final String newS3Status) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "UPDATE stu_unit_mastery ",
                    "SET s3_status=", sqlStringValue(newS3Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public List<StuUnitMasteryRec> queryByStudent(final Cache cache, final String stuId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, ESchema.LEGACY, sql);
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
        public List<StuUnitMasteryRec> queryByStudentCourse(final Cache cache,
                                                            final String stuId, final String courseId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId));

            return doListQuery(cache, ESchema.LEGACY, sql);
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
        public StuUnitMasteryRec query(final Cache cache, final String stuId,
                                       final String courseId, final Integer unit) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit));

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
        public StuUnitMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

            final StuUnitMasteryRec result = new StuUnitMasteryRec();

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
    public static final class Postgres extends StuUnitMasteryLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

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
        public boolean insert(final Cache cache, final StuUnitMasteryRec record)
                throws SQLException {

            if (record.stuId == null || record.courseId == null || record.unit == null
                || record.score == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    schemaPrefix, ".stu_unit_mastery (stu_id,course_id,unit,",
                    "score,sr_status,s1_status,s2_status,s3_status) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.score), ",",
                    sqlStringValue(record.srStatus), ",",
                    sqlStringValue(record.s1Status), ",",
                    sqlStringValue(record.s2Status), ",",
                    sqlStringValue(record.s3Status), ")");

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
        public boolean delete(final Cache cache, final StuUnitMasteryRec record) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("DELETE FROM ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

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
        public List<StuUnitMasteryRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".stu_unit_mastery");

            return doListQuery(cache, ESchema.LEGACY, sql);
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
        public boolean updateScore(final Cache cache, final StuUnitMasteryRec record,
                                   final Integer newScore) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "SET score=", sqlIntegerValue(newScore),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean updateSrStatus(final Cache cache, final StuUnitMasteryRec record,
                                      final String newSrStatus) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "SET sr_status=", sqlStringValue(newSrStatus),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean updateS1Status(final Cache cache, final StuUnitMasteryRec record,
                                      final String newS1Status) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "SET s1_status=", sqlStringValue(newS1Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean updateS2Status(final Cache cache, final StuUnitMasteryRec record,
                                      final String newS2Status) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "SET s2_status=", sqlStringValue(newS2Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public boolean updateS3Status(final Cache cache, final StuUnitMasteryRec record,
                                      final String newS3Status) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "SET s3_status=", sqlStringValue(newS3Status),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND unit=", sqlIntegerValue(record.unit));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
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
        public List<StuUnitMasteryRec> queryByStudent(final Cache cache, final String stuId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, ESchema.LEGACY, sql);
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
        public List<StuUnitMasteryRec> queryByStudentCourse(final Cache cache, final String stuId,
                                                            final String courseId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId));

            return doListQuery(cache, ESchema.LEGACY, sql);
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
        public StuUnitMasteryRec query(final Cache cache, final String stuId, final String courseId,
                                       final Integer unit) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    schemaPrefix, ".stu_unit_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit));

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
        public StuUnitMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

            final StuUnitMasteryRec result = new StuUnitMasteryRec();

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
