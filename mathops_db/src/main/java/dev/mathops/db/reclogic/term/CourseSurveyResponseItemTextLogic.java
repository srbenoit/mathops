package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.CourseSurveyResponseItemChoiceRec;
import dev.mathops.db.rec.term.CourseSurveyResponseItemTextRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "course survey response item text" records.
 *
 * <pre>
 * CREATE TABLE term_202510.course_survey_response_item_text (
 *     serial_nbr               integer        NOT NULL,  -- A unique serial number for the response
 *     item_nbr                 smallint       NOT NULL,  -- The item number
 *     response_text            text           NOT NULL,  -- The entered text
 *     PRIMARY KEY (serial_nbr, item_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class CourseSurveyResponseItemTextLogic implements IRecLogic<CourseSurveyResponseItemTextRec> {

    /** A single instance. */
    public static final CourseSurveyResponseItemTextLogic INSTANCE = new CourseSurveyResponseItemTextLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CourseSurveyResponseItemTextLogic() {

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
    public boolean insert(final Cache cache, final CourseSurveyResponseItemTextRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".course_survey_response_item_text (serial_nbr,item_nbr,response_text) VALUES (",
                    sqlIntegerValue(record.serialNbr), ",",
                    sqlIntegerValue(record.itemNbr), ",",
                    sqlStringValue(record.responseText), ")");

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
    public boolean delete(final Cache cache, final CourseSurveyResponseItemTextRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".course_survey_response_item_text WHERE serial_nbr=", sqlIntegerValue(record.serialNbr),
                    " AND item_nbr=", sqlIntegerValue(record.itemNbr));

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
    public List<CourseSurveyResponseItemTextRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<CourseSurveyResponseItemTextRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_response_item_text");

            result = doListQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for a standard assignment attempt answer by its serial number and question number.
     *
     * @param cache     the data cache
     * @param serialNbr the serial number for which to query
     * @param itemNbr   the item number for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public CourseSurveyResponseItemTextRec query(final Cache cache, final Integer serialNbr, final Integer itemNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final CourseSurveyResponseItemTextRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_response_item_text WHERE serial_nbr=", sqlIntegerValue(serialNbr),
                    " AND item_nbr=", sqlIntegerValue(itemNbr));

            result = doSingleQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries all course survey response item text records for a single serial number.
     *
     * @param cache     the data cache
     * @param serialNbr the serial number for which to query
     * @return the list of records for the specified student
     * @throws SQLException if there is an error performing the query
     */
    public List<CourseSurveyResponseItemTextRec> queryBySerialNbr(final Cache cache, final Integer serialNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<CourseSurveyResponseItemTextRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_response_item_text WHERE serial_nbr=", sqlIntegerValue(serialNbr));

            result = doListQuery(cache, sql);
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
    public boolean update(final Cache cache, final CourseSurveyResponseItemTextRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".course_survey_response_item_text SET response_text=", sqlStringValue(record.responseText),
                    " WHERE serial_nbr=", sqlIntegerValue(record.serialNbr),
                    " AND item_nbr=", sqlIntegerValue(record.itemNbr));

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
    public CourseSurveyResponseItemTextRec fromResultSet(final ResultSet rs) throws SQLException {

        final Integer theSerialNbr = getIntegerField(rs, DataDict.FLD_SERIAL_NBR);
        final Integer theItemNbr = getIntegerField(rs, DataDict.FLD_ITEM_NBR);
        final String theResponseText = getStringField(rs, DataDict.FLD_RESPONSE_TEXT);

        return new CourseSurveyResponseItemTextRec(theSerialNbr, theItemNbr, theResponseText);
    }
}
