package dev.mathops.db.reclogic;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.rec.MasteryExamRec;
import dev.mathops.db.reclogic.iface.IInformixRecLogic;
import dev.mathops.db.reclogic.iface.IPostgresRecLogic;
import dev.mathops.db.reclogic.iface.IRecLogic;
import dev.mathops.db.reclogic.query.DateTimeCriteria;
import dev.mathops.db.reclogic.query.IntegerCriteria;
import dev.mathops.db.reclogic.query.StringCriteria;

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

    /** A single instance. */
    public static final Informix INFORMIX = new Informix();

    /** A single instance. */
    public static final Postgres POSTGRES = new Postgres();

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

        final EDbInstallationType type = IRecLogic.getDbType(cache);

        MasteryExamLogic result = null;
        if (type == EDbInstallationType.INFORMIX) {
            result = INFORMIX;
        } else if (type == EDbInstallationType.POSTGRESQL) {
            result = POSTGRES;
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
    public abstract List<MasteryExamRec> queryActiveByCourse(Cache cache, String courseId)
            throws SQLException;

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
    public abstract List<MasteryExamRec> queryActiveByCourseUnitObjective(Cache cache,
                                                                          String courseId, Integer unit,
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
    public abstract MasteryExamRec queryActive(Cache cache, String courseId, Integer unit,
                                               Integer objective, String examType) throws SQLException;

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
     * Queries for all records matching given criteria.
     *
     * @param cache         the data cache
     * @param queryCriteria the general query criteria
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<MasteryExamRec> generalQuery(Cache cache, Criteria queryCriteria)
            throws SQLException;

    /**
     * An "assignment" criteria record used to perform arbitrary queries.
     */
    public static final class Criteria {

        /** The criteria for the 'exam_id' field. */
        StringCriteria examId;

        /** The criteria for the 'exam_type' field. */
        StringCriteria examType;

        /** The criteria for the 'course_id' field. */
        StringCriteria courseId;

        /** The criteria for the 'unit' field. */
        IntegerCriteria unit;

        /** The criteria for the 'objective' field. */
        IntegerCriteria objective;

        /** The criteria for the 'tree_ref' field. */
        StringCriteria treeRef;

        /** The criteria for the 'title' field. */
        StringCriteria title;

        /** The criteria for the 'button_label' field. */
        StringCriteria buttonLabel;

        /** The criteria for the 'when_active' field. */
        DateTimeCriteria whenActive;

        /** The criteria for the 'when_pulled' field. */
        DateTimeCriteria whenPulled;

        /**
         * Constructs a new {@code Criteria}.
         */
        public Criteria() {

            // No action
        }
    }

    /**
     * A subclass of {@code MasteryExamLogic} designed for the Informix schema.
     */
    public static final class Informix extends MasteryExamLogic
            implements IInformixRecLogic<MasteryExamRec> {

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

            final String sql = SimpleBuilder.concat( //
                    "INSERT INTO mastery_exam (exam_id,exam_type,course_id,unit,",
                    "objective,tree_ref,title,button_label,when_active,when_pulled) ",
                    "VALUES (",
                    sqlStringValue(record.examId), ",",
                    sqlStringValue(record.examType), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.treeRef), ",",
                    sqlStringValue(record.title), ",",
                    sqlStringValue(record.buttonLabel), ",",
                    sqlDateValue(record.whenActive == null ? null : record.whenActive.toLocalDate()),
                    ",",
                    sqlDateValue(record.whenPulled == null ? null : record.whenPulled.toLocalDate()),
                    ")");

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
        public boolean delete(final Cache cache, final MasteryExamRec record) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "DELETE FROM mastery_exam WHERE exam_id=",
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
        public List<MasteryExamRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, "SELECT * FROM mastery_exam");
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

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND when_pulled IS NULL ORDER BY unit,objective");

            return doListQuery(cache, sql);
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
        public List<MasteryExamRec> queryActiveByCourseUnit(final Cache cache,
                                                            final String courseId, final Integer unit) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND when_pulled IS NULL ORDER BY objective");

            return doListQuery(cache, sql);
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
        public List<MasteryExamRec> queryActiveByCourseUnitObjective(final Cache cache,
                                                                     final String courseId, final Integer unit,
                                                                     final Integer objective)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND when_pulled IS NULL");

            return doListQuery(cache, sql);
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
        public MasteryExamRec queryActive(final Cache cache, final String courseId,
                                          final Integer unit, final Integer objective, final String examType)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM mastery_exam ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND exam_type=", sqlStringValue(examType),
                    " AND when_pulled IS NULL");

            return doSingleQuery(cache, sql);
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
        public List<MasteryExamRec> generalQuery(final Cache cache, final Criteria queryCriteria) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM mastery_exam");

            String w = stringWhere(sql, WHERE, "exam_id", queryCriteria.examId);
            w = stringWhere(sql, w, "exam_type", queryCriteria.examType);
            w = stringWhere(sql, w, "course_id", queryCriteria.courseId);
            w = integerWhere(sql, w, "unit", queryCriteria.unit);
            w = integerWhere(sql, w, "objective", queryCriteria.objective);
            w = stringWhere(sql, w, "tree_ref", queryCriteria.treeRef);
            w = stringWhere(sql, w, "title", queryCriteria.title);
            w = stringWhere(sql, w, "button_label", queryCriteria.buttonLabel);
            w = dateWhere(sql, w, "when_active", queryCriteria.whenActive);
            dateWhere(sql, w, "when_pulled", queryCriteria.whenPulled);

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
    public static final class Postgres extends MasteryExamLogic
            implements IPostgresRecLogic<MasteryExamRec> {

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

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    cache.mainSchemaName, ".mastery_exam ",
                    "(exam_id,exam_type,course_id,unit,objective,tree_ref,title,",
                    "button_label,when_active,when_pulled) VALUES (",
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
        public boolean delete(final Cache cache, final MasteryExamRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM ",
                    cache.mainSchemaName, //
                    ".mastery_exam WHERE exam_id=", sqlStringValue(record.examId));

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
        public List<MasteryExamRec> queryAll(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".mastery_exam");

            return doListQuery(cache, sql);
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

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".mastery_exam ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND when_pulled IS NULL ORDER BY unit,objective");

            return doListQuery(cache, sql);
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
        public List<MasteryExamRec> queryActiveByCourseUnit(final Cache cache,
                                                            final String courseId, final Integer unit) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".mastery_exam ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND when_pulled IS NULL ORDER BY objective");

            return doListQuery(cache, sql);
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
        public List<MasteryExamRec> queryActiveByCourseUnitObjective(final Cache cache,
                                                                     final String courseId, final Integer unit,
                                                                     final Integer objective)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".mastery_exam ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND when_pulled IS NULL");

            return doListQuery(cache, sql);
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
        public MasteryExamRec queryActive(final Cache cache, final String courseId,
                                          final Integer unit, final Integer objective, final String examType)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".mastery_exam ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND exam_type=", sqlStringValue(examType),
                    " AND when_pulled IS NULL");

            return doSingleQuery(cache, sql);
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

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".mastery_exam ",
                    "WHERE exam_id=", sqlStringValue(examId));

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
        public List<MasteryExamRec> generalQuery(final Cache cache, final Criteria queryCriteria) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM  ", cache.mainSchemaName, ".mastery_exam");

            String w = stringWhere(sql, WHERE, "exam_id", queryCriteria.examId);
            w = stringWhere(sql, w, "exam_type", queryCriteria.examType);
            w = stringWhere(sql, w, "course_id", queryCriteria.courseId);
            w = integerWhere(sql, w, "unit", queryCriteria.unit);
            w = integerWhere(sql, w, "objective", queryCriteria.objective);
            w = stringWhere(sql, w, "tree_ref", queryCriteria.treeRef);
            w = stringWhere(sql, w, "title", queryCriteria.title);
            w = stringWhere(sql, w, "button_label", queryCriteria.buttonLabel);
            w = dateWhere(sql, w, "when_active", queryCriteria.whenActive);
            dateWhere(sql, w, "when_pulled", queryCriteria.whenPulled);

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
