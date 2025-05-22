package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.StandardsCourseStandardRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "standards course module" records.
 *
 * <pre>
 * CREATE TABLE main.standards_course_standard (
 *     course_id                char(10)        NOT NULL,  -- The course ID (references standards_course)
 *     module_nbr               smallint        NOT NULL,  -- The module number (1 for first module)
 *     standard_nbr             smallint        NOT NULL,  -- The standard number (1 for first standard in the module)
 *     learning_objective       text            NOT NULL,  -- The learning objective, in "I can ..." form.
 *     is_essential             char(1)         NOT NULL,  -- "Y" if essential , "N" if not
 *     PRIMARY KEY (course_id, module_nbr, standard_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardsCourseStandardLogic implements IRecLogic<StandardsCourseStandardRec> {

    /** A single instance. */
    public static final StandardsCourseStandardLogic INSTANCE = new StandardsCourseStandardLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardsCourseStandardLogic() {

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
    public boolean insert(final Cache cache, final StandardsCourseStandardRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".standards_course_standard (",
                    "course_id,module_nbr,standard_nbr,learning_objective,is_essential) VALUES (",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.moduleNbr), ",",
                    sqlIntegerValue(record.standardNbr), ",",
                    sqlStringValue(record.learningObjective), ",",
                    sqlStringValue(record.isEssential), ")");

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
    public boolean delete(final Cache cache, final StandardsCourseStandardRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".standards_course_standard WHERE course_id=", sqlStringValue(record.courseId),
                    " AND module_nbr=", sqlIntegerValue(record.moduleNbr),
                    " AND standard_nbr=", sqlIntegerValue(record.standardNbr));

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
    public List<StandardsCourseStandardRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<StandardsCourseStandardRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standards_course_standard");

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
    public List<StandardsCourseStandardRec> queryByCourse(final Cache cache, final String courseId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<StandardsCourseStandardRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standards_course_standard WHERE course_id=", sqlStringValue(courseId));

            result = doListQuery(cache, ESchema.MAIN, sql);
            result.sort(null);
        }

        return result;
    }

    /**
     * Queries for a standards course standard by its course ID, module number, and standard number.
     *
     * @param cache       the data cache
     * @param courseId    the course ID for which to query
     * @param moduleNbr   the module number for which to query
     * @param standardNbr the standard number for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardsCourseStandardRec query(final Cache cache, final String courseId, final Integer moduleNbr,
                                            final Integer standardNbr) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final StandardsCourseStandardRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standards_course_standard WHERE course_id=", sqlStringValue(courseId),
                    " AND module_nbr=", sqlIntegerValue(moduleNbr),
                    " AND standard_nbr=", sqlIntegerValue(standardNbr));

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
    public boolean update(final Cache cache, final StandardsCourseStandardRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standards_course_standard SET learning_objective=", sqlStringValue(record.learningObjective),
                    ",is_essential=", sqlStringValue(record.isEssential),
                    " WHERE course_id=", sqlStringValue(record.courseId),
                    " AND module_nbr=", sqlIntegerValue(record.moduleNbr),
                    " AND standard_nbr=", sqlIntegerValue(record.standardNbr));

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
    public StandardsCourseStandardRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final Integer theModuleNbr = getIntegerField(rs, DataDict.FLD_MODULE_NBR);
        final Integer theStandardNbr = getIntegerField(rs, DataDict.FLD_STANDARD_NBR);
        final String theLearningObjective = getStringField(rs, DataDict.FLD_LEARNING_OBJECTIVE);
        final String theIsEssential = getStringField(rs, DataDict.FLD_IS_ESSENTIAL);

        return new StandardsCourseStandardRec(theCourseId, theModuleNbr, theStandardNbr, theLearningObjective,
                theIsEssential);
    }
}
