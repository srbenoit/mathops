package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
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
 * CREATE TABLE main.standards_course_module (
 *     course_id                char(10)        NOT NULL,  -- The course ID (references standards_course)
 *     module_nbr               smallint        NOT NULL,  -- The module number (1 for the first module)
 *     nbr_standards            smallint        NOT NULL,  -- The number of standards in the module
 *     nbr_essential            smallint        NOT NULL,  -- The number of "essential" standards in the module
 *     module_path              text,                      -- The relative path of the module like "05_trig/01_angles"
 *     PRIMARY KEY (course_id, module_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardsCourseModuleLogic implements IRecLogic<StandardsCourseModuleRec> {

    /** A single instance. */
    public static final StandardsCourseModuleLogic INSTANCE = new StandardsCourseModuleLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardsCourseModuleLogic() {

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
    public boolean insert(final Cache cache, final StandardsCourseModuleRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".standards_course_module (course_id,module_nbr,nbr_standards,nbr_essential,module_path) VALUES (",
                    sqlStringValue(record.courseId), ",",
                    sqlIntegerValue(record.moduleNbr), ",",
                    sqlIntegerValue(record.nbrStandards), ",",
                    sqlIntegerValue(record.nbrEssential), ",",
                    sqlStringValue(record.modulePath), ")");

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
    public boolean delete(final Cache cache, final StandardsCourseModuleRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".standards_course_module WHERE course_id=", sqlStringValue(record.courseId),
                    " AND module_nbr=", sqlIntegerValue(record.moduleNbr));

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
    public List<StandardsCourseModuleRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<StandardsCourseModuleRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standards_course_module");

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
    public List<StandardsCourseModuleRec> queryByCourse(final Cache cache, final String courseId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<StandardsCourseModuleRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standards_course_module WHERE course_id=", sqlStringValue(courseId));

            result = doListQuery(cache, ESchema.MAIN, sql);
            result.sort(null);
        }

        return result;
    }

    /**
     * Queries for a standards course module by its course ID and module number.
     *
     * @param cache     the data cache
     * @param courseId  the course ID for which to query
     * @param moduleNbr the module number for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardsCourseModuleRec query(final Cache cache, final String courseId, final Integer moduleNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final StandardsCourseModuleRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standards_course_module WHERE course_id=", sqlStringValue(courseId), " AND module_nbr=",
                    sqlIntegerValue(moduleNbr));

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
    public boolean update(final Cache cache, final StandardsCourseModuleRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standards_course_module SET nbr_standards=", sqlIntegerValue(record.nbrStandards),
                    ",nbr_essential=", sqlIntegerValue(record.nbrEssential),
                    ",module_path=", sqlStringValue(record.modulePath),
                    " WHERE course_id=", sqlStringValue(record.courseId),
                    " AND module_nbr=", sqlIntegerValue(record.moduleNbr));

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
    public StandardsCourseModuleRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final Integer theModuleNbr = getIntegerField(rs, DataDict.FLD_MODULE_NBR);
        final Integer theNbrStandards = getIntegerField(rs, DataDict.FLD_NBR_STANDARDS);
        final Integer theNbrEssential = getIntegerField(rs, DataDict.FLD_NBR_ESSENTIAL);
        final String theModulePath = getStringField(rs, DataDict.FLD_MODULE_PATH);

        return new StandardsCourseModuleRec(theCourseId, theModuleNbr, theNbrStandards, theNbrEssential, theModulePath);
    }
}
