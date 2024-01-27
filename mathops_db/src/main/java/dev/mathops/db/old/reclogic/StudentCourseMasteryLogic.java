package dev.mathops.db.old.reclogic;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.rec.StudentCourseMasteryRec;
import dev.mathops.db.old.reclogic.iface.IInformixRecLogic;
import dev.mathops.db.old.reclogic.iface.IPostgresRecLogic;
import dev.mathops.db.old.reclogic.iface.IRecLogic;
import dev.mathops.db.old.reclogic.query.IntegerCriteria;
import dev.mathops.db.old.reclogic.query.StringCriteria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * A utility class to work with stu_course_mastery records.
 */
public abstract class StudentCourseMasteryLogic implements IRecLogic<StudentCourseMasteryRec> {

    /** A single instance. */
    public static final Informix INFORMIX = new Informix();

    /** A single instance. */
    public static final Postgres POSTGRES = new Postgres();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentCourseMasteryLogic() {

        super();
    }

    /**
     * Gets the instance of {@code StudentCourseMasteryLogic} appropriate to a cache. The result will depend on the
     * database installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code StudentCourseMasteryLogic} object (null if none found)
     */
    public static StudentCourseMasteryLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache);

        StudentCourseMasteryLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = INFORMIX;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = POSTGRES;
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
    public abstract boolean updateMastery(Cache cache, StudentCourseMasteryRec record, Integer newNbrMasteredH1,
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
    public abstract boolean updateScore(Cache cache, StudentCourseMasteryRec record,
                                        Integer newScore) throws SQLException;

    /**
     * Queries for all course mastery records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentCourseMasteryRec> queryByStudent(Cache cache, String stuId) throws SQLException;

    /**
     * Queries for a single course mastery record by student and course.
     *
     * @param cache    the data cache
     * @param stuId    the student ID
     * @param courseId the course ID
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract StudentCourseMasteryRec query(Cache cache, String stuId, String courseId) throws SQLException;

    /**
     * Queries for all records matching given criteria.
     *
     * @param cache         the data cache
     * @param queryCriteria the general query criteria
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<StudentCourseMasteryRec> generalQuery(Cache cache, Criteria queryCriteria) throws SQLException;

    /**
     * A "student course mastery" criteria record used to perform arbitrary queries.
     */
    public static final class Criteria {

        /** The criteria for the 'stu_id' field. */
        StringCriteria stuId;

        /** The criteria for the 'course_id' field. */
        StringCriteria courseId;

        /** The criteria for the 'score' field. */
        IntegerCriteria score;

        /** The criteria for the 'nbr_mastered_h1' field. */
        IntegerCriteria nbrMasteredH1;

        /** The criteria for the 'nbr_mastered_h2' field. */
        IntegerCriteria nbrMasteredH2;

        /** The criteria for the 'nbr_eligible' field. */
        IntegerCriteria nbrEligible;

        /**
         * Constructs a new {@code Criteria}.
         */
        public Criteria() {

            // No action
        }
    }

    /**
     * A subclass of {@code StudentCourseMasteryLogic} designed for the Informix schema.
     */
    public static final class Informix extends StudentCourseMasteryLogic
            implements IInformixRecLogic<StudentCourseMasteryRec> {

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
        public boolean insert(final Cache cache, final StudentCourseMasteryRec record)
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
                    sqlIntegerValue(record.nbrEligible), ",");

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
        public boolean delete(final Cache cache, final StudentCourseMasteryRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

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
        public List<StudentCourseMasteryRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, "SELECT * FROM stu_course_mastery");
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
        public boolean updateMastery(final Cache cache, final StudentCourseMasteryRec record,
                                     final Integer newNbrMasteredH1, final Integer newNbrMasteredH2,
                                     final Integer newNbrEligible) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE stu_course_mastery ",
                    "SET nbr_mastered_h1=", sqlIntegerValue(newNbrMasteredH1),
                    ", nbr_mastered_h2=", sqlIntegerValue(newNbrMasteredH2),
                    ", nbr_eligible=", sqlIntegerValue(newNbrEligible),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

            return doUpdateOneRow(cache, sql);
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
        public boolean updateScore(final Cache cache, final StudentCourseMasteryRec record,
                                   final Integer newScore) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE stu_course_mastery ",
                    "SET score=", sqlIntegerValue(newScore),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

            return doUpdateOneRow(cache, sql);
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
        public List<StudentCourseMasteryRec> queryByStudent(final Cache cache, final String stuId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
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
        public StudentCourseMasteryRec query(final Cache cache, final String stuId,
                                             final String courseId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId));

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
        public List<StudentCourseMasteryRec> generalQuery(final Cache cache, final Criteria queryCriteria)
                throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM stu_course_mastery");

            String w = stringWhere(sql, WHERE, "stu_id", queryCriteria.stuId);
            w = stringWhere(sql, w, "course_id", queryCriteria.courseId);
            w = integerWhere(sql, w, "score", queryCriteria.score);
            w = integerWhere(sql, w, "nbr_mastered_h1", queryCriteria.nbrMasteredH1);
            w = integerWhere(sql, w, "nbr_mastered_h2", queryCriteria.nbrMasteredH2);
            w = integerWhere(sql, w, "nbr_eligible", queryCriteria.nbrEligible);

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
        public StudentCourseMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

            final StudentCourseMasteryRec result = new StudentCourseMasteryRec();

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
    public static final class Postgres extends StudentCourseMasteryLogic
            implements IPostgresRecLogic<StudentCourseMasteryRec> {

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
        public boolean insert(final Cache cache, final StudentCourseMasteryRec record)
                throws SQLException {

            if (record.stuId == null || record.courseId == null || record.score == null
                    || record.nbrMasteredH1 == null || record.nbrMasteredH2 == null
                    || record.nbrEligible == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO ", cache.termSchemaName,
                    ".stu_course_mastery (stu_id,course_id,score,nbr_mastered_h1,nbr_mastered_h2,nbr_eligible) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.score), ",",
                    sqlIntegerValue(record.nbrMasteredH1), ",",
                    sqlIntegerValue(record.nbrMasteredH2), ",",
                    sqlIntegerValue(record.nbrEligible), ",");

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
        public boolean delete(final Cache cache, final StudentCourseMasteryRec record)
                throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM ", cache.termSchemaName, ".stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

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
        public List<StudentCourseMasteryRec> queryAll(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.termSchemaName, ".stu_course_mastery");

            return doListQuery(cache, sql);
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
        public boolean updateMastery(final Cache cache, final StudentCourseMasteryRec record,
                                     final Integer newNbrMasteredH1, final Integer newNbrMasteredH2,
                                     final Integer newNbrEligible) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE ", cache.termSchemaName, ".stu_course_mastery ",
                    "SET nbr_mastered_h1=", sqlIntegerValue(newNbrMasteredH1),
                    ", nbr_mastered_h2=", sqlIntegerValue(newNbrMasteredH2),
                    ", nbr_eligible=", sqlIntegerValue(newNbrEligible),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

            return doUpdateOneRow(cache, sql);
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
        public boolean updateScore(final Cache cache, final StudentCourseMasteryRec record,
                                   final Integer newScore) throws SQLException {

            final String sql = SimpleBuilder.concat("UPDATE ", cache.termSchemaName, ".stu_course_mastery ",
                    "SET score=", sqlIntegerValue(newScore),
                    " WHERE stu_id=", sqlStringValue(record.stuId),
                    " AND course_id=", sqlStringValue(record.courseId));

            return doUpdateOneRow(cache, sql);
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
        public List<StudentCourseMasteryRec> queryByStudent(final Cache cache, final String stuId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.termSchemaName, ".stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId));

            return doListQuery(cache, sql);
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
        public StudentCourseMasteryRec query(final Cache cache, final String stuId,
                                             final String courseId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ", cache.termSchemaName, ".stu_course_mastery ",
                    "WHERE stu_id=", sqlStringValue(stuId),
                    " AND course_id=", sqlStringValue(courseId));

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
        public List<StudentCourseMasteryRec> generalQuery(final Cache cache, final Criteria queryCriteria)
                throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM  ", cache.termSchemaName,
                    ".stu_course_mastery");

            String w = stringWhere(sql, WHERE, "stu_id", queryCriteria.stuId);
            w = stringWhere(sql, w, "course_id", queryCriteria.courseId);
            w = integerWhere(sql, w, "score", queryCriteria.score);
            w = integerWhere(sql, w, "nbr_mastered_h1", queryCriteria.nbrMasteredH1);
            w = integerWhere(sql, w, "nbr_mastered_h2", queryCriteria.nbrMasteredH2);
            w = integerWhere(sql, w, "nbr_eligible", queryCriteria.nbrEligible);

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
        public StudentCourseMasteryRec fromResultSet(final ResultSet rs) throws SQLException {

            final StudentCourseMasteryRec result = new StudentCourseMasteryRec();

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
