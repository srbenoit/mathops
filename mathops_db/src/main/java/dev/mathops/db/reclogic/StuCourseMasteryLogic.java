package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.StuCourseMasteryRec;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to work with stu_course_mastery records.
 */
public abstract class StuCourseMasteryLogic implements IRecLogic<StuCourseMasteryRec> {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StuCourseMasteryLogic() {

        super();
    }

    /**
     * Gets the instance of {@code StudentCourseMasteryLogic} appropriate to a cache. The result will depend on the
     * database installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code StudentCourseMasteryLogic} object (null if none found)
     */
    public static StuCourseMasteryLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        StuCourseMasteryLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
        }

        return result;
    }

    /**
     * Updates fields related to a student's current mastery relative to a course.
     *
     * @param cache            the data cache
     * @param record           the record to be updated
     * @param newNbrMasteredH1 the new number of standards mastered in the first half of the course
     * @param newNbrMasteredH2 the new number of standards mastered in the second half of the course
     * @param newNbrEligible   the new number of standards for which the student is eligible but has not demonstrated
     *                         mastery
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateMastery(Cache cache, StuCourseMasteryRec record, Integer newNbrMasteredH1,
                                          Integer newNbrMasteredH2, Integer newNbrEligible) throws SQLException;

    /**
     * Updates a student's score in a course.
     *
     * @param cache    the data cache
     * @param record   the record to be updated
     * @param newScore the new score
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public abstract boolean updateScore(Cache cache, StuCourseMasteryRec record,
                                        Integer newScore) throws SQLException;

    /**
     * Queries for all course mastery records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StuCourseMasteryRec> queryByStudent(Cache cache, String stuId) throws SQLException;

    /**
     * Queries for a single course mastery record by student and course.
     *
     * @param cache    the data cache
     * @param stuId    the student ID
     * @param courseId the course ID
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract StuCourseMasteryRec query(Cache cache, String stuId, String courseId) throws SQLException;

    /**
     * A subclass of {@code StudentCourseMasteryLogic} designed for the Informix schema.
     */
    public static final class Informix extends StuCourseMasteryLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

        /** A field name. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        private static final String FLD_COURSE_ID = "course_id";

        /** A field name. */
        private static final String FLD_SCORE = "score";

        /** A field name. */
        private static final String FLD_NBR_MASTERED_H1 = "nbr_mastered_h1";

        /** A field name. */
        private static final String FLD_NBR_MASTERED_H2 = "nbr_mastered_h2";

        /** A field name. */
        private static final String FLD_NBR_ELIGIBLE = "nbr_eligible";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StuCourseMasteryRec record)
                throws SQLException {

            if (record.stuId == null || record.courseId == null || record.score == null
                || record.nbrMasteredH1 == null || record.nbrMasteredH2 == null
                || record.nbrEligible == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO stu_course_mastery (stu_id,course_id,score,",
                    "nbr_mastered_h1,nbr_mastered_h2,nbr_eligible) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.score), ",",
                    sqlIntegerValue(record.nbrMasteredH1), ",",
                    sqlIntegerValue(record.nbrMasteredH2), ",",
                    sqlIntegerValue(record.nbrEligible), ")");

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
        public boolean delete(final Cache cache, final StuCourseMasteryRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

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
        public List<StuCourseMasteryRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM stu_course_mastery");
        }

        /**
         * Updates fields related to a student's current mastery relative to a course.
         *
         * @param cache            the data cache
         * @param record           the record to be updated
         * @param newNbrMasteredH1 the new number of standards mastered in the first half of the course
         * @param newNbrMasteredH2 the new number of standards mastered in the second half of the course
         * @param newNbrEligible   the new number of standards for which the student is eligible but has not
         *                         demonstrated mastery
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateMastery(final Cache cache, final StuCourseMasteryRec record,
                                     final Integer newNbrMasteredH1, final Integer newNbrMasteredH2,
                                     final Integer newNbrEligible) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE stu_course_mastery ",
                    "SET nbr_mastered_h1=", sqlIntegerValue(newNbrMasteredH1),
                    ", nbr_mastered_h2=", sqlIntegerValue(newNbrMasteredH2),
                    ", nbr_eligible=", sqlIntegerValue(newNbrEligible),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Updates a student's score in a course.
         *
         * @param cache    the data cache
         * @param record   the record to be updated
         * @param newScore the new score
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateScore(final Cache cache, final StuCourseMasteryRec record,
                                   final Integer newScore) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE stu_course_mastery ",
                    "SET score=", sqlIntegerValue(newScore),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all course mastery records for a student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StuCourseMasteryRec> queryByStudent(final Cache cache, final String stuId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a single course mastery record by student and course.
         *
         * @param cache    the data cache
         * @param stuId    the student ID
         * @param courseId the course ID
         * @return the assignment; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public StuCourseMasteryRec query(final Cache cache, final String stuId,
                                         final String courseId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId));

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
        public StuCourseMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

            final StuCourseMasteryRec result = new StuCourseMasteryRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.courseId = getStringField(rs, FLD_COURSE_ID);
            result.score = getIntegerField(rs, FLD_SCORE);
            result.nbrMasteredH1 = getIntegerField(rs, FLD_NBR_MASTERED_H1);
            result.nbrMasteredH2 = getIntegerField(rs, FLD_NBR_MASTERED_H2);
            result.nbrEligible = getIntegerField(rs, FLD_NBR_ELIGIBLE);

            return result;
        }
    }

    /**
     * A subclass of {@code StudentCourseMasteryLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends StuCourseMasteryLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

        /** A field name. */
        private static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        private static final String FLD_COURSE_ID = "course_id";

        /** A field name. */
        private static final String FLD_SCORE = "score";

        /** A field name. */
        private static final String FLD_NBR_MASTERED_H1 = "nbr_mastered_h1";

        /** A field name. */
        private static final String FLD_NBR_MASTERED_H2 = "nbr_mastered_h2";

        /** A field name. */
        private static final String FLD_NBR_ELIGIBLE = "nbr_eligible";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StuCourseMasteryRec record)
                throws SQLException {

            if (record.stuId == null || record.courseId == null || record.score == null
                || record.nbrMasteredH1 == null || record.nbrMasteredH2 == null
                || record.nbrEligible == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".stu_course_mastery (stu_id,course_id,score,nbr_mastered_h1,nbr_mastered_h2,nbr_eligible) VALUES" +
                    " (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.score), ",",
                    sqlIntegerValue(record.nbrMasteredH1), ",",
                    sqlIntegerValue(record.nbrMasteredH2), ",",
                    sqlIntegerValue(record.nbrEligible), ",");

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
        public boolean delete(final Cache cache, final StuCourseMasteryRec record) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

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
        public List<StuCourseMasteryRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".stu_course_mastery");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Updates fields related to a student's current mastery relative to a course.
         *
         * @param cache            the data cache
         * @param record           the record to be updated
         * @param newNbrMasteredH1 the new number of standards mastered in the first half of the course
         * @param newNbrMasteredH2 the new number of standards mastered in the second half of the course
         * @param newNbrEligible   the new number of standards for which the student is eligible but has not
         *                         demonstrated mastery
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateMastery(final Cache cache, final StuCourseMasteryRec record,
                                     final Integer newNbrMasteredH1, final Integer newNbrMasteredH2,
                                     final Integer newNbrEligible) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix, ".stu_course_mastery ",
                    "SET nbr_mastered_h1=", sqlIntegerValue(newNbrMasteredH1),
                    ", nbr_mastered_h2=", sqlIntegerValue(newNbrMasteredH2),
                    ", nbr_eligible=", sqlIntegerValue(newNbrEligible),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Updates a student's score in a course.
         *
         * @param cache    the data cache
         * @param record   the record to be updated
         * @param newScore the new score
         * @return true if successful; false if not
         * @throws SQLException if there is an error performing the update
         */
        @Override
        public boolean updateScore(final Cache cache, final StuCourseMasteryRec record,
                                   final Integer newScore) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix, ".stu_course_mastery ",
                    "SET score=", sqlIntegerValue(newScore),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all course mastery records for a student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StuCourseMasteryRec> queryByStudent(final Cache cache, final String stuId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a single course mastery record by student and course.
         *
         * @param cache    the data cache
         * @param stuId    the student ID
         * @param courseId the course ID
         * @return the assignment; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public StuCourseMasteryRec query(final Cache cache, final String stuId, final String courseId)
                throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId));

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
        public StuCourseMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

            final StuCourseMasteryRec result = new StuCourseMasteryRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.courseId = getStringField(rs, FLD_COURSE_ID);
            result.score = getIntegerField(rs, FLD_SCORE);
            result.nbrMasteredH1 = getIntegerField(rs, FLD_NBR_MASTERED_H1);
            result.nbrMasteredH2 = getIntegerField(rs, FLD_NBR_MASTERED_H2);
            result.nbrEligible = getIntegerField(rs, FLD_NBR_ELIGIBLE);

            return result;
        }
    }
}
