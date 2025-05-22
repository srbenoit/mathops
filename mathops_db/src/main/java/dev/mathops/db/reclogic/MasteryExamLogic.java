package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.MasteryExamRec;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * A utility class to work with mastery_exam records.
 */
public abstract class MasteryExamLogic implements IRecLogic<MasteryExamRec> {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private MasteryExamLogic() {

        super();
    }

    /**
     * Gets the instance of {@code MasteryExamLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code MasteryExamLogic} object (null if none found)
     */
    public static MasteryExamLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        MasteryExamLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
        }

        return result;
    }

    /**
     * Queries for all active (having null pull date) exams in a course.
     *
     * @param cache    the data cache
     * @param courseId the course for which to query
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryExamRec> queryActiveByCourse(Cache cache, String courseId) throws SQLException;

    /**
     * Queries for all active (having null pull date) exams in a course unit.
     *
     * @param cache    the data cache
     * @param courseId the course for which to query
     * @param unit     the unit for which to query
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryExamRec> queryActiveByCourseUnit(Cache cache, String courseId,
                                                                 Integer unit) throws SQLException;

    /**
     * Queries for all active (having null pull date) exams in a course unit objective.
     *
     * @param cache     the data cache
     * @param courseId  the course for which to query
     * @param unit      the unit for which to query
     * @param objective the objective for which to query
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryExamRec> queryActiveByCourseUnitObjective(Cache cache, String courseId, Integer unit,
                                                                          Integer objective) throws SQLException;

    /**
     * Queries for an active (having null pull date) exam by its course, unit, and type (which should produce a unique
     * result or nothing).
     *
     * @param cache     the data cache
     * @param courseId  the course for which to query
     * @param unit      the unit for which to query
     * @param objective the objective for which to query
     * @param examType  the exam type for which to query
     * @return the exam; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract MasteryExamRec queryActive(Cache cache, String courseId, Integer unit, Integer objective,
                                               String examType) throws SQLException;

    /**
     * Queries for a mastery exam by its ID.
     *
     * @param cache  the data cache
     * @param examId the ID of the exam to query
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract MasteryExamRec query(Cache cache, String examId) throws SQLException;

    /**
     * A subclass of {@code MasteryExamLogic} designed for the Informix schema.
     */
    public static final class Informix extends MasteryExamLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

        /** A field name. */
        private static final String FLD_EXAM_ID = "exam_id";

        /** A field name. */
        private static final String FLD_EXAM_TYPE = "exam_type";

        /** A field name. */
        private static final String FLD_COURSE_ID = "course_id";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_OBJECTIVE = "objective";

        /** A field name. */
        private static final String FLD_TREE_REF = "tree_ref";

        /** A field name. */
        private static final String FLD_TITLE = "title";

        /** A field name. */
        private static final String FLD_BUTTON_LABEL = "button_label";

        /** A field name. */
        private static final String FLD_WHEN_ACTIVE = "when_active";

        /** A field name. */
        private static final String FLD_WHEN_PULLED = "when_pulled";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final MasteryExamRec record) throws SQLException {

            if (record.examId == null || record.examType == null || record.courseId == null
                || record.unit == null || record.objective == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO mastery_exam (exam_id,exam_type,course_id,unit,objective,tree_ref,title,button_label,",
                    "when_active,when_pulled) VALUES (",
                    sqlStringValue(record.examId), ",",
                    sqlStringValue(record.examType), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.treeRef), ",",
                    sqlStringValue(record.title), ",",
                    sqlStringValue(record.buttonLabel), ",",
                    sqlDateValue(record.whenActive == null ? null : record.whenActive.toLocalDate()), ",",
                    sqlDateValue(record.whenPulled == null ? null : record.whenPulled.toLocalDate()), ")");

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
        public boolean delete(final Cache cache, final MasteryExamRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM mastery_exam WHERE exam_id=",
                    sqlStringValue(record.examId));

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
        public List<MasteryExamRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM mastery_exam");
        }

        /**
         * Queries for all active (having null pull date) mastery exams in a course. Results are ordered by unit and
         * then by objective.
         *
         * @param cache    the data cache
         * @param courseId the course ID for which to query
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryExamRec> queryActiveByCourse(final Cache cache, final String courseId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam WHERE course_id=",
                    sqlStringValue(courseId), " AND when_pulled IS NULL ORDER BY unit,objective");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all active (having null pull date) mastery exams in a course unit. Results are ordered by
         * objective.
         *
         * @param cache    the data cache
         * @param courseId the course for which to query
         * @param unit     the unit for which to query
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryExamRec> queryActiveByCourseUnit(final Cache cache, final String courseId,
                                                            final Integer unit) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam WHERE course_id=",
                    sqlStringValue(courseId), " AND unit=", sqlIntegerValue(unit),
                    " AND when_pulled IS NULL ORDER BY objective");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all active (having null pull date) mastery exams in a course unit objective.
         *
         * @param cache     the data cache
         * @param courseId  the course for which to query
         * @param unit      the unit for which to query
         * @param objective the objective for which to query
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryExamRec> queryActiveByCourseUnitObjective(final Cache cache, final String courseId,
                                                                     final Integer unit, final Integer objective)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam WHERE course_id=",
                    sqlStringValue(courseId), " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective), " AND when_pulled IS NULL");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for an active (having null pull date) mastery exam by its type, course, unit, and objective (which
         * should produce a unique result or nothing).
         *
         * @param cache     the data cache
         * @param courseId  the course for which to query
         * @param unit      the unit for which to query
         * @param objective the objective for which to query
         * @param examType  the assignment type for which to query
         * @return the assignment; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public MasteryExamRec queryActive(final Cache cache, final String courseId, final Integer unit,
                                          final Integer objective, final String examType) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam WHERE course_id=",
                    sqlStringValue(courseId), " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND exam_type=", sqlStringValue(examType), " AND when_pulled IS NULL");

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a mastery exam by its exam ID.
         *
         * @param cache  the data cache
         * @param examId the version of the exam to query
         * @return the exam; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public MasteryExamRec query(final Cache cache, final String examId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam WHERE exam_id=",
                    sqlStringValue(examId));

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
        public MasteryExamRec fromResultSet(final ResultSet rs) throws SQLException {

            final MasteryExamRec result = new MasteryExamRec();

            result.examId = getStringField(rs, FLD_EXAM_ID);
            result.examType = getStringField(rs, FLD_EXAM_TYPE);
            result.courseId = getStringField(rs, FLD_COURSE_ID);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.objective = getIntegerField(rs, FLD_OBJECTIVE);
            result.treeRef = getStringField(rs, FLD_TREE_REF);
            result.title = getStringField(rs, FLD_TITLE);
            result.buttonLabel = getStringField(rs, FLD_BUTTON_LABEL);

            final LocalDate active = getDateField(rs, FLD_WHEN_ACTIVE);
            result.whenActive = active == null ? null : LocalDateTime.of(active, LocalTime.of(0, 0));

            final LocalDate pulled = getDateField(rs, FLD_WHEN_PULLED);
            result.whenPulled = pulled == null ? null : LocalDateTime.of(pulled, LocalTime.of(0, 0));

            return result;
        }
    }

    /**
     * A subclass of {@code MasteryExamLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends MasteryExamLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

        /** A field name. */
        private static final String FLD_EXAM_ID = "exam_id";

        /** A field name. */
        private static final String FLD_EXAM_TYPE = "exam_type";

        /** A field name. */
        private static final String FLD_COURSE_ID = "course_id";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_OBJECTIVE = "objective";

        /** A field name. */
        private static final String FLD_TREE_REF = "tree_ref";

        /** A field name. */
        private static final String FLD_TITLE = "title";

        /** A field name. */
        private static final String FLD_BUTTON_LABEL = "button_label";

        /** A field name. */
        private static final String FLD_WHEN_ACTIVE = "when_active";

        /** A field name. */
        private static final String FLD_WHEN_PULLED = "when_pulled";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final MasteryExamRec record) throws SQLException {

            if (record.examId == null || record.examType == null || record.courseId == null
                || record.unit == null || record.objective == null) {
                throw new SQLException("Null value in primary key or required field.");
            }

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".mastery_exam (exam_id,exam_type,course_id,unit,objective,tree_ref,title,button_label,",
                    "when_active,when_pulled) VALUES (",
                    sqlStringValue(record.examId), ",",
                    sqlStringValue(record.examType), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.treeRef), ",",
                    sqlStringValue(record.title), ",",
                    sqlStringValue(record.buttonLabel), ",",
                    sqlDateTimeValue(record.whenActive), ",",
                    sqlDateTimeValue(record.whenPulled), ")");

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
        public boolean delete(final Cache cache, final MasteryExamRec record) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".mastery_exam WHERE exam_id=", sqlStringValue(record.examId));

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
        public List<MasteryExamRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".mastery_exam");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all active (having null pull date) mastery exams in a course. Results are ordered by unit and
         * then by objective.
         *
         * @param cache    the data cache
         * @param courseId the course ID for which to query
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryExamRec> queryActiveByCourse(final Cache cache, final String courseId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".mastery_exam WHERE course_id=", sqlStringValue(courseId),
                    " AND when_pulled IS NULL ORDER BY unit,objective");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all active (having null pull date) mastery exams in a course unit. Results are ordered by
         * objective.
         *
         * @param cache    the data cache
         * @param courseId the course for which to query
         * @param unit     the unit for which to query
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryExamRec> queryActiveByCourseUnit(final Cache cache, final String courseId,
                                                            final Integer unit) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".mastery_exam WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit), " AND when_pulled IS NULL ORDER BY objective");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for all active (having null pull date) mastery exams in a course unit objective.
         *
         * @param cache     the data cache
         * @param courseId  the course for which to query
         * @param unit      the unit for which to query
         * @param objective the objective for which to query
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<MasteryExamRec> queryActiveByCourseUnitObjective(final Cache cache, final String courseId,
                                                                     final Integer unit, final Integer objective)
                throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".mastery_exam WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective), " AND when_pulled IS NULL");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for an active (having null pull date) mastery exam by its type, course, unit, and objective, and type
         * (which should produce a unique result or nothing).
         *
         * @param cache     the data cache
         * @param courseId  the course for which to query
         * @param unit      the unit for which to query
         * @param objective the objective for which to query
         * @param examType  the exam type for which to query
         * @return the exam; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public MasteryExamRec queryActive(final Cache cache, final String courseId, final Integer unit,
                                          final Integer objective, final String examType) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".mastery_exam WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND exam_type=", sqlStringValue(examType), " AND when_pulled IS NULL");

            return doSingleQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries for a mastery exam by its exam ID.
         *
         * @param cache  the data cache
         * @param examId the ID of the exam to query
         * @return the exam; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public MasteryExamRec query(final Cache cache, final String examId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".mastery_exam WHERE exam_id=", sqlStringValue(examId));

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
        public MasteryExamRec fromResultSet(final ResultSet rs) throws SQLException {

            final MasteryExamRec result = new MasteryExamRec();

            result.examId = getStringField(rs, FLD_EXAM_ID);
            result.examType = getStringField(rs, FLD_EXAM_TYPE);
            result.courseId = getStringField(rs, FLD_COURSE_ID);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.objective = getIntegerField(rs, FLD_OBJECTIVE);
            result.treeRef = getStringField(rs, FLD_TREE_REF);
            result.title = getStringField(rs, FLD_TITLE);
            result.buttonLabel = getStringField(rs, FLD_BUTTON_LABEL);
            result.whenActive = getDateTimeField(rs, FLD_WHEN_ACTIVE);
            result.whenPulled = getDateTimeField(rs, FLD_WHEN_PULLED);

            return result;
        }
    }
}
