package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.term.LtiContextCourseSectionRec;
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
 * CREATE TABLE IF NOT EXISTS term_202510.lti_context_course_section (
 *     client_id                varchar(40)    NOT NULL,  -- The client ID provided by the LMS
 *     issuer                   varchar(250)   NOT NULL,  -- The issuer host name
 *     deployment_id            varchar(250)   NOT NULL,  -- The deployment ID
 *     context_id               varchar(250)   NOT NULL,  -- The LMS course context ID
 *     course_id                char(10)       NOT NULL,  -- The institution course ID
 *     section_nbr              char(4)        NOT NULL,  -- The institution section number
 *     PRIMARY KEY (client_id, issuer, deployment_id, context_id, course_id, section_nbr)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class LtiContextCourseSectionLogic implements IRecLogic<LtiContextCourseSectionRec> {

    /** A single instance. */
    public static final LtiContextCourseSectionLogic INSTANCE = new LtiContextCourseSectionLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private LtiContextCourseSectionLogic() {

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
    public boolean insert(final Cache cache, final LtiContextCourseSectionRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".lti_context_course_section ",
                    "(client_id,issuer,deployment_id,context_id,course_id,section_nbr) VALUES (",
                    sqlStringValue(record.clientId), ",",
                    sqlStringValue(record.issuer), ",",
                    sqlStringValue(record.deploymentId), ",",
                    sqlStringValue(record.contextId), ",",
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
    public boolean delete(final Cache cache, final LtiContextCourseSectionRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".lti_context_course_section WHERE client_id=", sqlStringValue(record.clientId),
                    " AND issuer=", sqlStringValue(record.issuer),
                    " AND deployment_id=", sqlStringValue(record.deploymentId),
                    " AND context_id=", sqlStringValue(record.contextId),
                    " AND course_id=", sqlStringValue(record.courseId),
                    " AND section_nbr=", sqlStringValue(record.sectionNbr));

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
    public List<LtiContextCourseSectionRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<LtiContextCourseSectionRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".lti_context_course_section");

            result = doListQuery(cache, ESchema.TERM, sql);
        }

        return result;
    }

    /**
     * Queries all context course section records for a particular context.
     *
     * @param cache        the data cache
     * @param clientId     the client ID for which to query
     * @param issuer       the issuer for which to query
     * @param deploymentId the deployment ID for which to query
     * @param contextId    the context ID for which to query
     * @return the list of all matching records
     * @throws SQLException if there is an error performing the query
     */
    public List<LtiContextCourseSectionRec> queryForContext(final Cache cache, final String clientId,
                                                            final String issuer, final String deploymentId,
                                                            final String contextId) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final List<LtiContextCourseSectionRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".lti_context_course_section WHERE client_id=", sqlStringValue(clientId),
                    " AND issuer=", sqlStringValue(issuer),
                    " AND deployment_id=", sqlStringValue(deploymentId),
                    " AND context_id=", sqlStringValue(contextId));

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
     * @param courseId     the course ID for which to query
     * @param sectionNbr   the section number for which to query
     * @return the matching record; null if not found
     * @throws SQLException if there is an error performing the query
     */
    public LtiContextCourseSectionRec query(final Cache cache, final String clientId, final String issuer,
                                            final String deploymentId, final String contextId,
                                            final String courseId, final String sectionNbr) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

        final LtiContextCourseSectionRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the TERM schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".lti_context_course_section WHERE client_id=", sqlStringValue(clientId),
                    " AND issuer=", sqlStringValue(issuer),
                    " AND deployment_id=", sqlStringValue(deploymentId),
                    " AND context_id=", sqlStringValue(contextId),
                    " AND course_id=", sqlStringValue(courseId),
                    " AND section_nbr=", sqlStringValue(sectionNbr));

            result = doSingleQuery(cache, ESchema.TERM, sql);
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
    public LtiContextCourseSectionRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theClientId = getStringField(rs, DataDict.FLD_CLIENT_ID);
        final String theIssuer = getStringField(rs, DataDict.FLD_ISSUER);
        final String theDeploymentId = getStringField(rs, DataDict.FLD_DEPLOYMENT_ID);
        final String theContextId = getStringField(rs, DataDict.FLD_CONTEXT_ID);
        final String theCourseId = getStringField(rs, DataDict.FLD_COURSE_ID);
        final String theSectionNbr = getStringField(rs, DataDict.FLD_SECTION_NBR);

        return new LtiContextCourseSectionRec(theClientId, theIssuer, theDeploymentId, theContextId, theCourseId,
                theSectionNbr);
    }
}
