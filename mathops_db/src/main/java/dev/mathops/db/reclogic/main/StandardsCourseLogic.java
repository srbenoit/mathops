package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "standards course" records.
 *
 * <pre>
 * CREATE TABLE main.standards_course (
 *     course_id                char(10)        NOT NULL,  -- The unique course ID
 *     course_title             varchar(50)     NOT NULL,  -- The course title
 *     nbr_modules              smallint        NOT NULL,  -- The number of modules in the course
 *     nbr_credits              smallint        NOT NULL,  -- The number of credits the course carries
 *     allow_lend               integer         NOT NULL,  -- Bitwise OR of resource type identifiers (1=Textbook,
 *                                                         -- 2 = Calculator/manual, 4=Laptop, 8=Headphones)
 *     metadata_path            varchar(50),               -- For metadata-based courses, the relative path of metadata,
 *                                                         -- like "05_trig/MATH_125.json"
 *     PRIMARY KEY (course_id)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class StandardsCourseLogic implements IRecLogic<StandardsCourseRec> {

    /** A single instance. */
    public static final StandardsCourseLogic INSTANCE = new StandardsCourseLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StandardsCourseLogic() {

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
    public boolean insert(final Cache cache, final StandardsCourseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".standards_course ",
                    "(course_id,course_title,nbr_modules,nbr_credits,allow_lend,metadata_path) VALUES (",
                    sqlStringValue(record.courseId), ",",
                    sqlStringValue(record.courseTitle), ",",
                    sqlIntegerValue(record.nbrModules), ",",
                    sqlIntegerValue(record.nbrCredits), ",",
                    sqlIntegerValue(record.allowLend), ",",
                    sqlStringValue(record.metadataPath), ")");

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
    public boolean delete(final Cache cache, final StandardsCourseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".standards_course WHERE course_id=",
                    sqlStringValue(record.courseId));

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
    public List<StandardsCourseRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<StandardsCourseRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".standards_course");

            result = doListQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for a standards course by its ID.
     *
     * @param cache    the data cache
     * @param courseId the course ID for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public StandardsCourseRec query(final Cache cache, final String courseId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final StandardsCourseRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".standards_course WHERE course_id=", sqlStringValue(courseId));

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
    public boolean update(final Cache cache, final StandardsCourseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".standards_course SET course_title=", sqlStringValue(record.courseTitle),
                    ",nbr_modules=", sqlIntegerValue(record.nbrModules),
                    ",nbr_credits=", sqlIntegerValue(record.nbrCredits),
                    ",allow_lend=", sqlIntegerValue(record.allowLend),
                    ",metadata_path=", sqlStringValue(record.metadataPath),
                    " WHERE course_id=", sqlStringValue(record.courseId));

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
    public StandardsCourseRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final String theCourseTitle = getStringField(rs, DataDict.FLD_COURSE_TITLE);
        final Integer theNbrModules = getIntegerField(rs, DataDict.FLD_NBR_MODULES);
        final Integer theNbrCredits = getIntegerField(rs, DataDict.FLD_NBR_CREDITS);
        final Integer theAllowLend = getIntegerField(rs, DataDict.FLD_ALLOW_LEND);
        final String theMetadataPath = getStringField(rs, DataDict.FLD_METADATA_PATH);

        return new StandardsCourseRec(theCourseId, theCourseTitle, theNbrModules, theNbrCredits, theAllowLend,
                theMetadataPath);
    }
}
