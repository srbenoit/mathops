package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.CourseSurveyRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "course survey" records.
 *
 * <pre>
 * CREATE TABLE main.course_survey (
 *     survey_id                char(10)       NOT NULL,  -- The survey ID
 *     open_week                smallint,                 -- The week when survey opens (null if open any time course
 *                                                        --     open, negative to indicate offset from end of term)
 *     open_day                 smallint,                 -- The weekday when the survey opens (at day start, null if
 *                                                        --     none)
 *     close_week               smallint,                 -- The week when the survey closes (null if no closure,
 *                                                        --     negative to indicate offset from end of term)
 *     close_day                smallint,                 -- The weekday when the survey closes (at day end, null if
 *                                                        --     none)
 *     PRIMARY KEY (survey_id)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class CourseSurveyLogic implements IRecLogic<CourseSurveyRec> {

    /** A single instance. */
    public static final CourseSurveyLogic INSTANCE = new CourseSurveyLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CourseSurveyLogic() {

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
    public boolean insert(final Cache cache, final CourseSurveyRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".course_survey (survey_id,open_week,open_day,close_week,close_day) VALUES (",
                    sqlStringValue(record.surveyId), ",",
                    sqlIntegerValue(record.openWeek), ",",
                    sqlIntegerValue(record.openDay), ",",
                    sqlIntegerValue(record.closeWeek), ",",
                    sqlIntegerValue(record.closeDay), ")");

            result = doUpdateOneRow(cache, sql);
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
    public boolean delete(final Cache cache, final CourseSurveyRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix, ".course_survey WHERE survey_id=",
                    sqlStringValue(record.surveyId));

            result = doUpdateOneRow(cache, sql);
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
    public List<CourseSurveyRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<CourseSurveyRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".course_survey");

            result = doListQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for a course survey by its ID.
     *
     * @param cache    the data cache
     * @param surveyId the survey ID for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public CourseSurveyRec query(final Cache cache, final String surveyId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final CourseSurveyRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey WHERE survey_id=", sqlStringValue(surveyId));

            result = doSingleQuery(cache, sql);
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
    public boolean update(final Cache cache, final CourseSurveyRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".course_survey SET open_week=", sqlIntegerValue(record.openWeek),
                    ",open_day=", sqlIntegerValue(record.openDay),
                    ",close_week=", sqlIntegerValue(record.closeWeek),
                    ",close_day=", sqlIntegerValue(record.closeDay),
                    " WHERE survey_id=", sqlStringValue(record.surveyId));

            result = doUpdateOneRow(cache, sql);
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
    public CourseSurveyRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theSurveyId = getStringField(rs, DataDict.FLD_SURVEY_ID);
        final Integer theOpenWeek = getIntegerField(rs, DataDict.FLD_OPEN_WEEK);
        final Integer theOpenDay = getIntegerField(rs, DataDict.FLD_OPEN_DAY);
        final Integer theCloseWeek = getIntegerField(rs, DataDict.FLD_CLOSE_WEEK);
        final Integer theCloseDay = getIntegerField(rs, DataDict.FLD_CLOSE_DAY);

        return new CourseSurveyRec(theSurveyId, theOpenWeek, theOpenDay, theCloseWeek, theCloseDay);
    }
}
