package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.CourseSurveyItemChoiceRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "course survey item choice" records.
 *
 * <pre>
 * CREATE TABLE IF NOT EXISTS main.course_survey_item_choice (
 *     survey_id                char(10)       NOT NULL,  -- The survey ID
 *     item_nbr                 smallint       NOT NULL,  -- The item number
 *     choice_nbr               smallint       NOT NULL,  -- The choice number
 *     choice_html              varchar(250)   NOT NULL,  -- The HTML of the choice
 *     PRIMARY KEY (survey_id, item_nbr, choice_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class CourseSurveyItemChoiceLogic implements IRecLogic<CourseSurveyItemChoiceRec> {

    /** A single instance. */
    public static final CourseSurveyItemChoiceLogic INSTANCE = new CourseSurveyItemChoiceLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CourseSurveyItemChoiceLogic() {

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
    public boolean insert(final Cache cache, final CourseSurveyItemChoiceRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".course_survey_item_choice (survey_id,item_nbr,choice_nbr,choice_html) VALUES (",
                    sqlStringValue(record.surveyId), ",",
                    sqlIntegerValue(record.itemNbr), ",",
                    sqlIntegerValue(record.choiceNbr), ",",
                    sqlStringValue(record.choiceHtml), ")");

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
    public boolean delete(final Cache cache, final CourseSurveyItemChoiceRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".course_survey_item_choice WHERE survey_id=", sqlStringValue(record.surveyId),
                    " AND item_nbr=", sqlIntegerValue(record.itemNbr),
                    " AND choice_nbr=", sqlIntegerValue(record.choiceNbr));

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
    public List<CourseSurveyItemChoiceRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<CourseSurveyItemChoiceRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".course_survey_item_choice");

            result = doListQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for a course survey item choice by its survey ID and item number.
     *
     * @param cache     the data cache
     * @param surveyId  the survey ID for which to query
     * @param itemNbr   the item number
     * @param choiceNbr the choice number
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public CourseSurveyItemChoiceRec query(final Cache cache, final String surveyId, final Integer itemNbr,
                                           final Integer choiceNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final CourseSurveyItemChoiceRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_item_choice  WHERE survey_id=", sqlStringValue(surveyId),
                    " AND item_nbr=", sqlIntegerValue(itemNbr),
                    " AND choice_nbr=", sqlIntegerValue(choiceNbr));

            result = doSingleQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for all course survey item choice records with a specified survey ID and item number.  Results are
     * ordered by choice number.
     *
     * @param cache    the data cache
     * @param surveyId the survey ID for which to query
     * @param itemNbr  the item number for which to query
     * @return the list of all matching records
     * @throws SQLException if there is an error performing the query
     */
    public List<CourseSurveyItemChoiceRec> queryBySurveyIdAndItemNbr(final Cache cache, final String surveyId,
                                                                     final Integer itemNbr) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<CourseSurveyItemChoiceRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_item_choice WHERE survey_id=", sqlStringValue(surveyId),
                    " AND item_nbr=", sqlIntegerValue(itemNbr));

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
    public boolean update(final Cache cache, final CourseSurveyItemChoiceRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".course_survey_item_choice SET choice_html=", sqlStringValue(record.choiceHtml),
                    " WHERE survey_id=", sqlStringValue(record.surveyId),
                    " AND item_nbr=", sqlIntegerValue(record.itemNbr),
                    " AND choice_nbr=", sqlIntegerValue(record.choiceNbr));

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
    public CourseSurveyItemChoiceRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theSurveyId = getStringField(rs, DataDict.FLD_SURVEY_ID);
        final Integer theItemNbr = getIntegerField(rs, DataDict.FLD_ITEM_NBR);
        final Integer theChoiceNbr = getIntegerField(rs, DataDict.FLD_CHOICE_NBR);
        final String theChoiceHtml = getStringField(rs, DataDict.FLD_CHOICE_HTML);

        return new CourseSurveyItemChoiceRec(theSurveyId, theItemNbr, theChoiceNbr, theChoiceHtml);
    }
}
