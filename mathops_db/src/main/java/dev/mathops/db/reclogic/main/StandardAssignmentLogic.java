package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.StandardAssignmentRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "standard assignment" records.
 *
 * <pre>
 * CREATE TABLE main.standard_assignment (
 *     assignment_id            varchar(20)     NOT NULL,  -- The unique assignment ID
 *     assignment_type          char(2)         NOT NULL,  -- The assignment type ('HW' homework, 'MA' Mastery Exam)
 *     course_id                char(10)        NOT NULL,  -- The course ID (references standards_course)
 *     module_nbr               smallint        NOT NULL,  -- The module number (1 for the first module)
 *     standard_nbr             smallint        NOT NULL,  -- The standard number (1 for the first standard in a module)
 *     pts_possible             smallint,                  -- The number of points possible
 *     min_passing_score        smallint,                  -- The minimum score that is considered "passing"
 *     tree_ref                 varchar(250)    NOT NULL,  -- For tree reference of the assessment
 *     PRIMARY KEY (assignment_id)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardAssignmentLogic implements IRecLogic<StandardAssignmentRec> {

    /** A single instance. */
    public static final StandardAssignmentLogic INSTANCE = new StandardAssignmentLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardAssignmentLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final StandardAssignmentRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".standard_assignment (assignment_id,assignment_type,course_id,module_nbr,standard_nbr,",
                    "pts_possible,min_passing_score,tree_ref) VALUES (",
                    sqlStringValue(record.assignmentId), ",",
                    sqlStringValue(record.assignmentType), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.moduleNbr), ",",
                    sqlIntegerValue(record.standardNbr), ",",
                    sqlIntegerValue(record.ptsPossible), ",",
                    sqlIntegerValue(record.minPassingScore), ",",
                    sqlStringValue(record.treeRef), ")");

            result = doUpdateOneRow(cache, ESchema.MAIN, sql);
        }

        return result;
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
    public boolean delete(final Cache cache, final StandardAssignmentRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".standard_assignment WHERE assignment_id=",
                    sqlStringValue(record.assignmentId));

            result = doUpdateOneRow(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries every record in the database.
     *
     * @param cache the data cache
     * @return the complete set of records in the database
     * @throws SQLException if there is an error performing the query
     */
    @Override
    public List<StandardAssignmentRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<StandardAssignmentRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standard_assignment");

            result = doListQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries all modules for a specified course.  Results are sorted by module number.
     *
     * @param cache    the data cache
     * @param courseId the course ID for which to query
     * @return the list of modules found for the specified course
     * @throws SQLException if there is an error performing the query
     */
    public List<StandardAssignmentRec> queryByCourse(final Cache cache, final String courseId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<StandardAssignmentRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standard_assignment WHERE course_id=", sqlStringValue(courseId));

            result = doListQuery(cache, ESchema.MAIN, sql);
            result.sort(null);
        }

        return result;
    }

    /**
     * Queries for a standards course by its ID.
     *
     * @param cache        the data cache
     * @param assignmentId the assignment ID for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardAssignmentRec query(final Cache cache, final String assignmentId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final StandardAssignmentRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standard_assignment WHERE assignment_id=", sqlStringValue(assignmentId));

            result = doSingleQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Updates a record.
     *
     * @param cache  the data cache
     * @param record the record to update
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public boolean update(final Cache cache, final StandardAssignmentRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standard_assignment SET assignment_type=", sqlStringValue(record.assignmentType),
                    ",course_id=", sqlStringValue(record.courseId),
                    ",module_nbr=", sqlIntegerValue(record.moduleNbr),
                    ",standard_nbr=", sqlIntegerValue(record.standardNbr),
                    ",pts_possible=", sqlIntegerValue(record.ptsPossible),
                    ",min_passing_score=", sqlIntegerValue(record.minPassingScore),
                    ",tree_ref=", sqlStringValue(record.treeRef),
                    " WHERE assignment_id=", sqlStringValue(record.assignmentId));

            result = doUpdateOneRow(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Extracts a record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public StandardAssignmentRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theAssignmentId = getStringField(rs, DataDict.FLD_ASSIGNMENT_ID);
        final String theAssignmentType = getStringField(rs, DataDict.FLD_ASSIGNMENT_TYPE);
        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final Integer theModuleNbr = getIntegerField(rs, DataDict.FLD_MODULE_NBR);
        final Integer theStandardNbr = getIntegerField(rs, DataDict.FLD_STANDARD_NBR);
        final Integer thePtsPossible = getIntegerField(rs, DataDict.FLD_PTS_POSSIBLE);
        final Integer theMinPassingScore = getIntegerField(rs, DataDict.FLD_MIN_PASSING_SCORE);
        final String theTreeRef = getStringField(rs, DataDict.FLD_TREE_REF);

        return new StandardAssignmentRec(theAssignmentId, theAssignmentType, theCourseId, theModuleNbr, theStandardNbr,
                thePtsPossible, theMinPassingScore, theTreeRef);
    }
}
