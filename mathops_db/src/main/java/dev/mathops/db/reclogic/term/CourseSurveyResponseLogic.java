package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.CourseSurveyResponseRec;
import dev.mathops.db.rec.term.StudentPreferenceRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "course survey response" records.
 *
 * <pre>
 * CREATE TABLE term_202510.course_survey_response (
 *     serial_nbr               integer        NOT NULL,  -- A unique serial number for the response
 *     survey_id                char(10)       NOT NULL,  -- The survey ID
 *     student_id               char(9)        NOT NULL,  -- The student ID
 *     response_date            date           NOT NULL,  -- The response date
 *     response_time            time           NOT NULL,  -- The response time
 *     PRIMARY KEY (serial_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class CourseSurveyResponseLogic implements IRecLogic<CourseSurveyResponseRec> {

    /** A single instance. */
    public static final CourseSurveyResponseLogic INSTANCE = new CourseSurveyResponseLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CourseSurveyResponseLogic() {

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
    public boolean insert(final Cache cache, final CourseSurveyResponseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix,
                    ".course_survey_response (serial_nbr,survey_id,student_id,response_date,response_time) VALUES (",
                    sqlIntegerValue(record.serialNbr), ",",
                    sqlStringValue(record.surveyId), ",",
                    sqlStringValue(record.studentId), ",",
                    sqlDateValue(record.responseDate), ",",
                    sqlTimeValue(record.responseTime), ")");

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
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
    public boolean delete(final Cache cache, final CourseSurveyResponseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".course_survey_response WHERE serial_nbr=", sqlIntegerValue(record.serialNbr));

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
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
    public List<CourseSurveyResponseRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<CourseSurveyResponseRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".course_survey_response");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries for a course survey response by its serial number.
     *
     * @param cache     the data cache
     * @param serialNbr the serial number for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public CourseSurveyResponseRec query(final Cache cache, final Integer serialNbr)
            throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final CourseSurveyResponseRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_response WHERE serial_nbr=", sqlIntegerValue(serialNbr));

            result = doSingleQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries all course survey response records for a single student.
     *
     * @param cache     the data cache
     * @param studentId the student for which to query
     * @return the list of records for the specified student
     * @throws SQLException if there is an error performing the query
     */
    public List<CourseSurveyResponseRec> queryByStudent(final Cache cache, final String studentId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<CourseSurveyResponseRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".course_survey_response WHERE student_id=", sqlStringValue(studentId));

            result = doListQuery(cache, ESchema.TERM, sql);
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
    public boolean update(final Cache cache, final CourseSurveyResponseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".course_survey_response SET survey_id=", sqlStringValue(record.surveyId),
                    ",student_id=", sqlStringValue(record.studentId),
                    ",response_date=", sqlDateValue(record.responseDate),
                    ",response_time=", sqlTimeValue(record.responseTime),
                    " WHERE serial_nbr=", sqlIntegerValue(record.serialNbr));

            result = doUpdateOneRow(cache, ESchema.TERM, sql);
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
    public CourseSurveyResponseRec fromResultSet(final ResultSet rs) throws SQLException {

        final Integer theSerialNbr = getIntegerField(rs, DataDict.FLD_SERIAL_NBR);
        final String theSurveyId = getStringField(rs, DataDict.FLD_SURVEY_ID);
        final String theStudentId = getStringField(rs, DataDict.FLD_STUDENT_ID);
        final LocalDate theResponseDate = getDateField(rs, DataDict.FLD_RESPONSE_DATE);
        final LocalTime theResponseTime = getTimeField(rs, DataDict.FLD_RESPONSE_TIME);

        return new CourseSurveyResponseRec(theSerialNbr, theSurveyId, theStudentId, theResponseDate, theResponseTime);
    }
}
