package dev.mathops.db.old.reclogic;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.reclogic.iface.IInformixRecLogic;
import dev.mathops.db.old.reclogic.iface.IPostgresRecLogic;
import dev.mathops.db.old.reclogic.iface.IRecLogic;
import dev.mathops.db.old.reclogic.query.DateTimeCriteria;
import dev.mathops.db.old.reclogic.query.IntegerCriteria;
import dev.mathops.db.old.reclogic.query.StringCriteria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * A utility class to work with assignment records.
 */
public abstract class AssignmentLogic implements IRecLogic<AssignmentRec> {

    /** A single instance. */
    public static final Informix INFORMIX = new Informix();

    /** A single instance. */
    public static final Postgres POSTGRES = new Postgres();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private AssignmentLogic() {

        super();
    }

    /**
     * Gets the instance of {@code AssignmentLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code AssignmentLogic} object (null if none found)
     */
    public static AssignmentLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache);

        AssignmentLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = INFORMIX;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = POSTGRES;
        }

        return result;
    }

    /**
     * Queries for all active (having null pull date) homeworks in a course. Results are ordered by unit and then by
     * objective.
     *
     * @param cache          the data cache
     * @param courseId       the course ID for which to query
     * @param assignmentType null to retrieve all assignment types; or the type to retrieve
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<AssignmentRec> queryActiveByCourse(Cache cache, String courseId,
                                                            final String assignmentType) throws SQLException;

    /**
     * Queries for all active (having null pull date) homeworks in a course unit. Results are ordered by objective.
     *
     * @param cache          the data cache
     * @param courseId       the course for which to query
     * @param unit           the unit for which to query
     * @param assignmentType null to retrieve all assignment types; or the type to retrieve
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<AssignmentRec> queryActiveByCourseUnit(Cache cache, String courseId,
                                                                Integer unit, final String assignmentType) throws SQLException;

    /**
     * Queries for all active (having null pull date) homeworks in a course unit objective.
     *
     * @param cache          the data cache
     * @param course         the course for which to query
     * @param unit           the unit for which to query
     * @param objective      the objective for which to query
     * @param assignmentType null to retrieve all assignment types; or the type to retrieve
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<AssignmentRec> queryActiveByCourseUnitObjective(Cache cache, String course,
                                                                         Integer unit, Integer objective,
                                                                         final String assignmentType) throws SQLException;

    /**
     * Queries for an active (having null pull date) assignment by its type, course, unit, and objective, and type
     * (which should produce a unique result or nothing).
     *
     * @param cache          the data cache
     * @param course         the course for which to query
     * @param unit           the unit for which to query
     * @param objective      the objective for which to query
     * @param assignmentType the assignment type for which to query
     * @return the assignment; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract AssignmentRec queryActive(Cache cache, String course, Integer unit,
                                              Integer objective, String assignmentType) throws SQLException;

    /**
     * Queries for an assignment by its ID.
     *
     * @param cache        the data cache
     * @param assignmentId the ID of the assignment to query
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract AssignmentRec query(Cache cache, String assignmentId) throws SQLException;

    /**
     * Queries for all records matching given criteria.
     *
     * @param cache         the data cache
     * @param queryCriteria the general query criteria
     * @return the list of records returned
     * @throws SQLException if there is an error performing the query
     */
    public abstract List<AssignmentRec> generalQuery(Cache cache, Criteria queryCriteria)
            throws SQLException;

    /**
     * An "assignment" criteria record used to perform arbitrary queries.
     */
    public static final class Criteria {

        /** The criteria for the 'assignment_id' field. */
        public StringCriteria assignmentId;

        /** The criteria for the 'assignment_type' field. */
        public StringCriteria assignmentType;

        /** The criteria for the 'course_id' field. */
        public StringCriteria courseId;

        /** The criteria for the 'unit' field. */
        public IntegerCriteria unit;

        /** The criteria for the 'objective' field. */
        public IntegerCriteria objective;

        /** The criteria for the 'tree_ref' field. */
        public StringCriteria treeRef;

        /** The criteria for the 'title' field. */
        public StringCriteria title;

        /** The criteria for the 'when_active' field. */
        public DateTimeCriteria whenActive;

        /** The criteria for the 'when_pulled' field. */
        public DateTimeCriteria whenPulled;

        /**
         * Constructs a new {@code Criteria}.
         */
        public Criteria() {

            // No action
        }
    }

    /**
     * A subclass of {@code AssignmentLogic} designed for the Informix schema.
     */
    public static final class Informix extends AssignmentLogic
            implements IInformixRecLogic<AssignmentRec> {

        /** A field name. */
        private static final String FLD_VERSION = "version";

        /** A field name. */
        private static final String FLD_HW_TYPE = "hw_type";

        /** A field name. */
        private static final String FLD_COURSE = "course";

        /** A field name. */
        private static final String FLD_UNIT = "unit";

        /** A field name. */
        private static final String FLD_OBJECTIVE = "objective";

        /** A field name. */
        private static final String FLD_TREE_REF = "tree_ref";

        /** A field name. */
        private static final String FLD_TITLE = "title";

        /** A field name. */
        private static final String FLD_ACTIVE_DT = "active_dt";

        /** A field name. */
        private static final String FLD_PULL_DT = "pull_dt";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final AssignmentRec record) throws SQLException {

            if (record.assignmentId == null || record.assignmentType == null
                    || record.courseId == null || record.unit == null || record.objective == null) {
                throw new SQLException("Null value in required field.");
            }

            final String sql = SimpleBuilder.concat( //
                    "INSERT INTO homework (version,course,unit,objective,title,",
                    "tree_ref,hw_type,active_dt,pull_dt) VALUES (",
                    sqlStringValue(record.assignmentId), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.title), ",",
                    sqlStringValue(record.treeRef), ",",
                    sqlStringValue(record.assignmentType), ",",
                    sqlDateValue(record.whenActive == null ? null : record.whenActive.toLocalDate()), ",",
                    sqlDateValue(record.whenPulled == null ? null : record.whenPulled.toLocalDate()), ")");

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
        public boolean delete(final Cache cache, final AssignmentRec record) throws SQLException {

            final String sql = SimpleBuilder.concat(//
                    "DELETE FROM homework WHERE version=",
                    sqlStringValue(record.assignmentId));

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
        public List<AssignmentRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, "SELECT * FROM homework");
        }

        /**
         * Queries for all active (having null pull date) assignments in a course. Results are ordered by unit and then
         * by objective.
         *
         * @param cache          the data cache
         * @param courseId       the course ID for which to query
         * @param assignmentType null to retrieve all assignment types; or the type to retrieve
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<AssignmentRec> queryActiveByCourse(final Cache cache, final String courseId,
                                                       final String assignmentType) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("SELECT * FROM homework WHERE course=", sqlStringValue(courseId), " AND pull_dt IS NULL");

            if (assignmentType != null) {
                sql.add(" AND hw_type=", sqlStringValue(assignmentType));
            }

            sql.add(" ORDER BY unit,objective");

            return doListQuery(cache, sql.toString());
        }

        /**
         * Queries for all active (having null pull date) assignments in a course unit. Results are ordered by
         * objective.
         *
         * @param cache          the data cache
         * @param courseId       the course for which to query
         * @param unit           the unit for which to query
         * @param assignmentType null to retrieve all assignment types; or the type to retrieve
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<AssignmentRec> queryActiveByCourseUnit(final Cache cache, final String courseId, final Integer unit,
                                                           final String assignmentType) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("SELECT * FROM homework WHERE course=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND pull_dt IS NULL");

            if (assignmentType != null) {
                sql.add(" AND hw_type=", sqlStringValue(assignmentType));
            }

            sql.add(" ORDER BY objective");

            return doListQuery(cache, sql.toString());
        }

        /**
         * Queries for all active (having null pull date) assignments in a course unit objective.
         *
         * @param cache          the data cache
         * @param course         the course for which to query
         * @param unit           the unit for which to query
         * @param objective      the objective for which to query
         * @param assignmentType null to retrieve all assignment types; or the type to retrieve
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<AssignmentRec> queryActiveByCourseUnitObjective(final Cache cache, final String course,
                                                                    final Integer unit, final Integer objective,
                                                                    final String assignmentType) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("SELECT * FROM homework WHERE course=", sqlStringValue(course),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND pull_dt IS NULL");

            if (assignmentType != null) {
                sql.add(" AND hw_type=", sqlStringValue(assignmentType));
            }

            return doListQuery(cache, sql.toString());
        }

        /**
         * Queries for an active (having null pull date) assignment by its type, course, unit, and objective, and type
         * (which should produce a unique result or nothing).
         *
         * @param cache          the data cache
         * @param course         the course for which to query
         * @param unit           the unit for which to query
         * @param objective      the objective for which to query
         * @param assignmentType the assignment type for which to query
         * @return the assignment; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public AssignmentRec queryActive(final Cache cache, final String course, final Integer unit,
                                         final Integer objective, final String assignmentType) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM homework ",
                    "WHERE course=", sqlStringValue(course), " AND unit=",
                    sqlIntegerValue(unit), " AND objective=", sqlIntegerValue(objective),
                    " AND hw_type=", sqlStringValue(assignmentType),
                    " AND pull_dt IS NULL");

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for a homework by its version.
         *
         * @param cache        the data cache
         * @param assignmentId the version of the homework to query
         * @return the homework; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public AssignmentRec query(final Cache cache, final String assignmentId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM homework ",
                    "WHERE version=", sqlStringValue(assignmentId));

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
        public List<AssignmentRec> generalQuery(final Cache cache, final Criteria queryCriteria)
                throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM homework");

            String w = stringWhere(sql, WHERE, "version", queryCriteria.assignmentId);
            w = stringWhere(sql, w, "hw_type", queryCriteria.assignmentType);
            w = stringWhere(sql, w, "course", queryCriteria.courseId);
            w = integerWhere(sql, w, "unit", queryCriteria.unit);
            w = integerWhere(sql, w, "objective", queryCriteria.objective);
            w = stringWhere(sql, w, "tree_ref", queryCriteria.treeRef);
            w = stringWhere(sql, w, "title", queryCriteria.title);
            w = dateWhere(sql, w, "active_dt", queryCriteria.whenActive);
            dateWhere(sql, w, "pull_dt", queryCriteria.whenPulled);

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
        public AssignmentRec fromResultSet(final ResultSet rs) throws SQLException {

            final AssignmentRec result = new AssignmentRec();

            result.assignmentId = getStringField(rs, FLD_VERSION);
            result.assignmentType = getStringField(rs, FLD_HW_TYPE);
            result.courseId = getStringField(rs, FLD_COURSE);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.objective = getIntegerField(rs, FLD_OBJECTIVE);
            result.treeRef = getStringField(rs, FLD_TREE_REF);
            result.title = getStringField(rs, FLD_TITLE);

            final LocalDate active = getDateField(rs, FLD_ACTIVE_DT);
            result.whenActive = active == null ? null : LocalDateTime.of(active, LocalTime.of(0, 0));

            final LocalDate pulled = getDateField(rs, FLD_PULL_DT);
            result.whenPulled = pulled == null ? null : LocalDateTime.of(pulled, LocalTime.of(0, 0));

            return result;
        }
    }

    /**
     * A subclass of {@code AssignmentLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends AssignmentLogic
            implements IPostgresRecLogic<AssignmentRec> {

        /** A field name. */
        private static final String FLD_ASSIGNMENT_ID = "assignment_id";

        /** A field name. */
        private static final String FLD_ASSIGNMENT_TYPE = "assignment_type";

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
        public boolean insert(final Cache cache, final AssignmentRec record) throws SQLException {

            if (record.assignmentId == null || record.assignmentType == null
                    || record.courseId == null || record.unit == null || record.objective == null) {
                throw new SQLException("Null value in required field.");
            }

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    cache.mainSchemaName, ".assignment ",
                    "(assignment_id,assignment_type,course_id,unit,objective,tree_ref,",
                    "title,when_active,when_pulled) VALUES (",
                    sqlStringValue(record.assignmentId), ",",
                    sqlStringValue(record.assignmentType), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.unit), ",",
                    sqlIntegerValue(record.objective), ",",
                    sqlStringValue(record.treeRef), ",",
                    sqlStringValue(record.title), ",",
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
        public boolean delete(final Cache cache, final AssignmentRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM ",
                    cache.mainSchemaName, ".assignment WHERE assignment_id=",
                    sqlStringValue(record.assignmentId));

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
        public List<AssignmentRec> queryAll(final Cache cache) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".assignment");

            return doListQuery(cache, sql);
        }

        /**
         * Queries for all active (having null pull date) assignments in a course. Results are ordered by unit and then
         * by objective.
         *
         * @param cache          the data cache
         * @param courseId       the course ID for which to query
         * @param assignmentType null to retrieve all assignment types; or the type to retrieve
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<AssignmentRec> queryActiveByCourse(final Cache cache, final String courseId,
                                                       final String assignmentType) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("SELECT * FROM ", cache.mainSchemaName,
                    ".assignment WHERE course_id=", sqlStringValue(courseId),
                    " AND when_pulled IS NULL");

            if (assignmentType != null) {
                sql.add(" AND assignment_type=", sqlStringValue(assignmentType));
            }

            sql.add(" ORDER BY unit,objective");

            return doListQuery(cache, sql.toString());
        }

        /**
         * Queries for all active (having null pull date) assignments in a course unit. Results are ordered by
         * objective.
         *
         * @param cache          the data cache
         * @param courseId       the course for which to query
         * @param unit           the unit for which to query
         * @param assignmentType null to retrieve all assignment types; or the type to retrieve
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<AssignmentRec> queryActiveByCourseUnit(final Cache cache, final String courseId,
                                                           final Integer unit, final String assignmentType) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("SELECT * FROM ",
                    cache.mainSchemaName, ".assignment ",
                    "WHERE course_id=", sqlStringValue(courseId),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND when_pulled IS NULL");

            if (assignmentType != null) {
                sql.add(" AND assignment_type=", sqlStringValue(assignmentType));
            }

            sql.add(" ORDER BY objective");

            return doListQuery(cache, sql.toString());
        }

        /**
         * Queries for all active (having null pull date) assignments in a course unit objective.
         *
         * @param cache          the data cache
         * @param course         the course for which to query
         * @param unit           the unit for which to query
         * @param objective      the objective for which to query
         * @param assignmentType null to retrieve all assignment types; or the type to retrieve
         * @return the list of records returned
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<AssignmentRec> queryActiveByCourseUnitObjective(final Cache cache,
                                                                    final String course, final Integer unit,
                                                                    final Integer objective,
                                                                    final String assignmentType) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("SELECT * FROM ",
                    cache.mainSchemaName, ".assignment ",
                    "WHERE course_id=", sqlStringValue(course),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND when_pulled IS NULL");

            if (assignmentType != null) {
                sql.add(" AND assignment_type=", sqlStringValue(assignmentType));
            }

            return doListQuery(cache, sql.toString());
        }

        /**
         * Queries for an active (having null pull date) assignment by its type, course, unit, and objective, and type
         * (which should produce a unique result or nothing).
         *
         * @param cache          the data cache
         * @param course         the course for which to query
         * @param unit           the unit for which to query
         * @param objective      the objective for which to query
         * @param assignmentType the assignment type for which to query
         * @return the assignment; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public AssignmentRec queryActive(final Cache cache, final String course, final Integer unit,
                                         final Integer objective, final String assignmentType) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".assignment ",
                    "WHERE course_id=", sqlStringValue(course),
                    " AND unit=", sqlIntegerValue(unit),
                    " AND objective=", sqlIntegerValue(objective),
                    " AND assignment_type=", sqlStringValue(assignmentType),
                    " AND when_pulled IS NULL");

            return doSingleQuery(cache, sql);
        }

        /**
         * Queries for an assignment by its version.
         *
         * @param cache        the data cache
         * @param assignmentId the ID of the assignment to query
         * @return the assignment; {@code null} if not found
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public AssignmentRec query(final Cache cache, final String assignmentId)
                throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM ",
                    cache.mainSchemaName, ".assignment WHERE assignment_id=",
                    sqlStringValue(assignmentId));

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
        public List<AssignmentRec> generalQuery(final Cache cache, final Criteria queryCriteria) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(150);

            sql.add("SELECT * FROM ", cache.mainSchemaName,
                    ".assignment");

            String w = stringWhere(sql, WHERE, "assignment_id", queryCriteria.assignmentId);
            w = stringWhere(sql, w, "assignment_type", queryCriteria.assignmentType);
            w = stringWhere(sql, w, "course_id", queryCriteria.courseId);
            w = integerWhere(sql, w, "unit", queryCriteria.unit);
            w = integerWhere(sql, w, "objective", queryCriteria.objective);
            w = stringWhere(sql, w, "tree_ref", queryCriteria.treeRef);
            w = stringWhere(sql, w, "title", queryCriteria.title);
            w = dateTimeWhere(sql, w, "when_active", queryCriteria.whenActive);
            dateTimeWhere(sql, w, "when_pulled", queryCriteria.whenPulled);

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
        public AssignmentRec fromResultSet(final ResultSet rs) throws SQLException {

            final AssignmentRec result = new AssignmentRec();

            result.assignmentId = getStringField(rs, FLD_ASSIGNMENT_ID);
            result.assignmentType = getStringField(rs, FLD_ASSIGNMENT_TYPE);
            result.courseId = getStringField(rs, FLD_COURSE_ID);
            result.unit = getIntegerField(rs, FLD_UNIT);
            result.objective = getIntegerField(rs, FLD_OBJECTIVE);
            result.treeRef = getStringField(rs, FLD_TREE_REF);
            result.title = getStringField(rs, FLD_TITLE);
            result.whenActive = getDateTimeField(rs, FLD_WHEN_ACTIVE);
            result.whenPulled = getDateTimeField(rs, FLD_WHEN_PULLED);

            return result;
        }
    }
}
