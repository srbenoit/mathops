package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.CourseSurveyItemRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "course survey item" records.
 *
 * <pre>
 * CREATE TABLE main.course_survey_item (
 *     survey_id                char(10)       NOT NULL,  -- The survey ID
 *     item_nbr                 smallint       NOT NULL,  -- The item number
 *     item_type                smallint       NOT NULL,  -- The item type (1 = M/C, 2 = M/S, 3 = Likert, 4 = Text)
 *     prompt_html              varchar(250)   NOT NULL,  -- The HTML of the prompt
 *     PRIMARY KEY (survey_id, item_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class CourseSurveyItemLogic implements IRecLogic<CourseSurveyItemRec> {

    /** A single instance. */
    public static final CourseSurveyItemLogic INSTANCE = new CourseSurveyItemLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CourseSurveyItemLogic() {

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
    public boolean insert(final Cache cache, final CourseSurveyItemRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".course_survey_item (survey_id,item_nbr,item_type,prompt_html) VALUES (",
                    sqlStringValue(record.surveyId), ",",
                    sqlIntegerValue(record.itemNbr), ",",
                    sqlIntegerValue(record.itemType), ",",
                    sqlStringValue(record.promptHtml), ")");

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
    public boolean delete(final Cache cache, final CourseSurveyItemRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".course_survey_item WHERE survey_id=", sqlStringValue(record.surveyId),
                    " AND item_nbr=", sqlIntegerValue(record.itemNbr));

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
    public List<CourseSurveyItemRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<CourseSurveyItemRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".course_survey_item");

            result = doListQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for a course survey item by its survey ID and item number.
     *
     * @param cache    the data cache
     * @param surveyId the survey ID for which to query
     * @param itemNbr  the item number
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public CourseSurveyItemRec query(final Cache cache, final String surveyId, final Integer itemNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final CourseSurveyItemRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_item  WHERE survey_id=", sqlStringValue(surveyId),
                    " AND item_nbr=", sqlIntegerValue(itemNbr));

            result = doSingleQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for all course survey item records with a specified survey ID.  Results are ordered by item number.
     *
     * @param cache    the data cache
     * @param surveyId the survey ID for which to query
     * @return the list of all matching records
     * @throws SQLException if there is an error performing the query
     */
    public List<CourseSurveyItemRec> queryBySurveyId(final Cache cache, final String surveyId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<CourseSurveyItemRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_item WHERE survey_id=", sqlStringValue(surveyId));

            result = doListQuery(cache, ESchema.MAIN, sql);
            result.sort(null);
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
    public boolean update(final Cache cache, final CourseSurveyItemRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".course_survey_item SET item_type=", sqlIntegerValue(record.itemType),
                    ",prompt_html=", sqlStringValue(record.promptHtml),
                    " WHERE survey_id=", sqlStringValue(record.surveyId),
                    " AND item_nbr=", sqlIntegerValue(record.itemNbr));

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
    public CourseSurveyItemRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theSurveyId = getStringField(rs, DataDict.FLD_SURVEY_ID);
        final Integer theItemNbr = getIntegerField(rs, DataDict.FLD_ITEM_NBR);
        final Integer theItemType = getIntegerField(rs, DataDict.FLD_ITEM_TYPE);
        final String thePromptHtml = getStringField(rs, DataDict.FLD_PROMPT_HTML);

        return new CourseSurveyItemRec(theSurveyId, theItemNbr, theItemType, thePromptHtml);
    }
}
