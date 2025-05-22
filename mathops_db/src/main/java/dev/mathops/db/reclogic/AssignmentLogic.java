package dev.mathops.db.reclogic;

import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.AssignmentRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

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

    /**
     * Private constructor to prevent direct instantiation.
     */
    private AssignmentLogic() {

        super();
    }

    /**
     * Gets the instance of {@code TermLogic} appropriate to a cache. The result will depend on the database
     * installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code TermLogic} object (null if none found)
     */
    public static AssignmentLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        AssignmentLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
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
     * Queries for an assignment by its ID.
     *
     * @param cache        the data cache
     * @param assignmentId the ID of the assignment to query
     * @return the record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public abstract AssignmentRec query(Cache cache, String assignmentId) throws SQLException;

    /**
     * A subclass of {@code AssignmentLogic} designed for the Informix schema.
     */
    public static final class Informix extends AssignmentLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

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

            final String sql = SimpleBuilder.concat(
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
        public boolean delete(final Cache cache, final AssignmentRec record) throws SQLException {

            final String sql = SimpleBuilder.concat(
                    "DELETE FROM homework WHERE version=",
                    sqlStringValue(record.assignmentId));

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
        public List<AssignmentRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM homework");
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

            return doListQuery(cache, ESchema.LEGACY, sql.toString());
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
        public AssignmentRec query(final Cache cache, final String assignmentId) throws SQLException {

            final String sql = SimpleBuilder.concat("SELECT * FROM homework WHERE version=",
                    sqlStringValue(assignmentId));

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
        public AssignmentRec fromResultSet(final ResultSet rs) throws SQLException {

            final String id = getStringField(rs, FLD_VERSION);
            final String type = getStringField(rs, FLD_HW_TYPE);
            final String course = getStringField(rs, FLD_COURSE);
            final Integer unit = getIntegerField(rs, FLD_UNIT);
            final Integer objective = getIntegerField(rs, FLD_OBJECTIVE);
            final String treeRef = getStringField(rs, FLD_TREE_REF);
            final String title = getStringField(rs, FLD_TITLE);

            final LocalDate active = getDateField(rs, FLD_ACTIVE_DT);
            final LocalDateTime whenActive = active == null ? null : LocalDateTime.of(active, LocalTime.of(0, 0));

            final LocalDate pulled = getDateField(rs, FLD_PULL_DT);
            final LocalDateTime whenPulled = pulled == null ? null : LocalDateTime.of(pulled, LocalTime.of(0, 0));

            return new AssignmentRec(id, type, course, unit, objective, treeRef, title, whenActive, whenPulled);
        }
    }

    /**
     * A subclass of {@code AssignmentLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends AssignmentLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

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

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("INSERT INTO ",
                    schemaPrefix, ".assignment ",
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
        public boolean delete(final Cache cache, final AssignmentRec record) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".assignment WHERE assignment_id=", sqlStringValue(record.assignmentId));

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
        public List<AssignmentRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".assignment");

            return doListQuery(cache, ESchema.LEGACY, sql);
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

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            sql.add("SELECT * FROM ", schemaPrefix, ".assignment WHERE course_id=", sqlStringValue(courseId),
                    " AND when_pulled IS NULL");

            if (assignmentType != null) {
                sql.add(" AND assignment_type=", sqlStringValue(assignmentType));
            }

            sql.add(" ORDER BY unit,objective");

            return doListQuery(cache, ESchema.LEGACY, sql.toString());
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
        public AssignmentRec query(final Cache cache, final String assignmentId) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".assignment WHERE assignment_id=", sqlStringValue(assignmentId));

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
        public AssignmentRec fromResultSet(final ResultSet rs) throws SQLException {

            final String id = getStringField(rs, FLD_ASSIGNMENT_ID);
            final String type = getStringField(rs, FLD_ASSIGNMENT_TYPE);
            final String course = getStringField(rs, FLD_COURSE_ID);
            final Integer unit = getIntegerField(rs, FLD_UNIT);
            final Integer objective = getIntegerField(rs, FLD_OBJECTIVE);
            final String treeRef = getStringField(rs, FLD_TREE_REF);
            final String title = getStringField(rs, FLD_TITLE);
            final LocalDateTime whenActive = getDateTimeField(rs, FLD_WHEN_ACTIVE);
            final LocalDateTime whenPulled = getDateTimeField(rs, FLD_WHEN_PULLED);

            return new AssignmentRec(id, type, course, unit, objective, treeRef, title, whenActive, whenPulled);
        }
    }
}
