package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.LtiCourseRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "LTI course" records.
 *
 * <pre>
 * CREATE TABLE IF NOT EXISTS term_202510.lti_course (
 *     client_id                varchar(40)    NOT NULL,  -- The client ID provided by the LMS
 *     issuer                   varchar(250)   NOT NULL,  -- The issuer host name
 *     deployment_id            varchar(250)   NOT NULL,  -- The deployment ID
 *     context_id               varchar(250)   NOT NULL,  -- The LMS course context ID
 *     lms_course_id            varchar(40),              -- The LMS course ID (typically a small integer)
 *     lms_course_title         varchar(250),             -- The LMS course title
 *     course_id                char(10)       NOT NULL,  -- The institution course ID
 *     section_nbr              char(4)        NOT NULL,  -- The institution section number
 *     PRIMARY KEY (client_id, issuer, deployment_id, context_id)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class LtiCourseLogic implements IRecLogic<LtiCourseRec> {

    /** A single instance. */
    public static final LtiCourseLogic INSTANCE = new LtiCourseLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private LtiCourseLogic() {

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
    public boolean insert(final Cache cache, final LtiCourseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".lti_course (client_id,issuer,",
                    "deployment_id,context_id,lms_course_id,lms_course_title,course_id,section_nbr) VALUES (",
                    sqlStringValue(record.clientId), ",",
                    sqlStringValue(record.issuer), ",",
                    sqlStringValue(record.deploymentId), ",",
                    sqlStringValue(record.contextId), ",",
                    sqlStringValue(record.lmsCourseId), ",",
                    sqlStringValue(record.lmsCourseTitle), ",",
                    sqlStringValue(record.courseId), ",",
                    sqlStringValue(record.sectionNbr), ")");

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
    public boolean delete(final Cache cache, final LtiCourseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".lti_course WHERE client_id=", sqlStringValue(record.clientId),
                    " AND issuer=", sqlStringValue(record.issuer),
                    " AND deployment_id=", sqlStringValue(record.deploymentId),
                    " AND context_id=", sqlStringValue(record.contextId));

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
    public List<LtiCourseRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<LtiCourseRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".lti_course");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries all preference records for a single student.
     *
     * @param cache        the data cache
     * @param clientId     the client ID for which to query
     * @param issuer       the issuer for which to query
     * @param deploymentId the deployment ID for which to query
     * @param contextId    the context ID for which to query
     * @return the matching record; null if not found
     * @throws SQLException if there is an error performing the query
     */
    public LtiCourseRec query(final Cache cache, final String clientId, final String issuer, final String deploymentId,
                              final String contextId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final LtiCourseRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".lti_course WHERE client_id=", sqlStringValue(clientId),
                    " AND issuer=", sqlStringValue(issuer),
                    " AND deployment_id=", sqlStringValue(deploymentId),
                    " AND context_id=", sqlStringValue(contextId));

            result = doSingleQuery(cache, ESchema.TERM, sql);
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
    public boolean update(final Cache cache, final LtiCourseRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".lti_course SET lms_course_id=", sqlStringValue(record.lmsCourseId),
                    ", lms_course_title=", sqlStringValue(record.lmsCourseTitle),
                    ", course_id=", sqlStringValue(record.courseId),
                    ", section_nbr=", sqlStringValue(record.sectionNbr),
                    " WHERE client_id=", sqlStringValue(record.clientId),
                    " AND issuer=", sqlStringValue(record.issuer),
                    " AND deployment_id=", sqlStringValue(record.deploymentId),
                    " AND context_id=", sqlStringValue(record.contextId));

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
    public LtiCourseRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theClientId = getStringField(rs, DataDict.FLD_CLIENT_ID);
        final String theIssuer = getStringField(rs, DataDict.FLD_ISSUER);
        final String theDeploymentId = getStringField(rs, DataDict.FLD_DEPLOYMENT_ID);
        final String theContextId = getStringField(rs, DataDict.FLD_CONTEXT_ID);
        final String theLmsCourseId = getStringField(rs, DataDict.FLD_LMS_COURSE_ID);
        final String theLmsCourseTitle = getStringField(rs, DataDict.FLD_LMS_COURSE_TITLE);
        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final String theSectionNbr = getStringField(rs, DataDict.FLD_SECTION_NBR);

        return new LtiCourseRec(theClientId, theIssuer, theDeploymentId, theContextId, theLmsCourseId,
                theLmsCourseTitle, theCourseId, theSectionNbr);
    }
}
