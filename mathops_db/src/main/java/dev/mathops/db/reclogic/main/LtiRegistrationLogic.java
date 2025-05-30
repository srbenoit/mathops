package dev.mathops.db.reclogic.main;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DataDict;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.db.reclogic.IRecLogic;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "lti_registration" records.
 *
 * <pre>
 * CREATE TABLE main.lti_registration (
 *     client_id                varchar(40)    NOT NULL,  -- The client ID provided by the LMS
 *     issuer                   varchar(250)   NOT NULL,  -- The issuer host name
 *     issuer_port              char(6)        NOT NULL,  -- The issuer port string, like ":20443" or ""
 *     redirect_uri             varchar(250)   NOT NULL,  -- The redirect URI
 *     auth_endpoint            varchar(250)   NOT NULL,  -- The authorization endpoint
 *     token_endpoint           varchar(250)   NOT NULL,  -- The token endpoint
 *     reg_endpoint             varchar(250)   NOT NULL,  -- The registration endpoint
 *     jwks_uri                 varchar(250)   NOT NULL,  -- The JWKS URI
 *     PRIMARY KEY (client_id, issuer, issuer_port)
 * ) TABLESPACE primary_ts;
 * </pre>
 */
public final class LtiRegistrationLogic implements IRecLogic<LtiRegistrationRec> {

    /** A single instance. */
    public static final LtiRegistrationLogic INSTANCE = new LtiRegistrationLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private LtiRegistrationLogic() {

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
    public boolean insert(final Cache cache, final LtiRegistrationRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("INSERT INTO ", schemaPrefix, ".lti_registration (client_id,",
                    "issuer,issuer_port,redirect_uri,auth_endpoint,token_endpoint,reg_endpoint,jwks_uri) VALUES (",
                    sqlStringValue(record.clientId), ",",
                    sqlStringValue(record.issuer), ",",
                    sqlStringValue(record.issuerPort), ",",
                    sqlStringValue(record.redirectUri), ",",
                    sqlStringValue(record.authEndpoint), ",",
                    sqlStringValue(record.tokenEndpoint), ",",
                    sqlStringValue(record.regEndpoint), ",",
                    sqlStringValue(record.jwksUri), ")");

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
    public boolean delete(final Cache cache, final LtiRegistrationRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("DELETE FROM ", schemaPrefix,
                    ".lti_registration WHERE client_id=", sqlStringValue(record.clientId),
                    " AND issuer=", sqlStringValue(record.issuer));

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
    public List<LtiRegistrationRec> queryAll(final Cache cache) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final List<LtiRegistrationRec> result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = new ArrayList<>(0);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".lti_registration");

            result = doListQuery(cache, ESchema.MAIN, sql);
        }

        return result;
    }

    /**
     * Queries for a facility by its ID.
     *
     * @param cache    the data cache
     * @param clientId the client ID for which to query
     * @param issuer   the issuer for which to query
     * @return the matching record; {@code null} if not found
     * @throws SQLException if there is an error performing the query
     */
    public LtiRegistrationRec query(final Cache cache, final String clientId, final String issuer) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final LtiRegistrationRec result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = null;
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix,
                    ".lti_registration WHERE client_id=", sqlStringValue(clientId),
                    " AND issuer=", sqlStringValue(issuer));

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
    public boolean update(final Cache cache, final LtiRegistrationRec record) throws SQLException {

        final String schemaPrefix = cache.getSchemaPrefix(ESchema.MAIN);

        final boolean result;
        if (schemaPrefix == null) {
            Log.warning("Cache profile '", cache.getProfile().id, "' does not support the MAIN schema");
            result = false;
        } else {
            final String sql = SimpleBuilder.concat("UPDATE ", schemaPrefix,
                    ".lti_registration SET issuer_port=", sqlStringValue(record.issuerPort),
                    ", redirect_uri=", sqlStringValue(record.redirectUri),
                    ", auth_endpoint=", sqlStringValue(record.authEndpoint),
                    ", token_endpoint=", sqlStringValue(record.tokenEndpoint),
                    ", reg_endpoint=", sqlStringValue(record.regEndpoint),
                    ", jwks_uri=", sqlStringValue(record.jwksUri),
                    " WHERE client_id=", sqlStringValue(record.clientId),
                    " AND issuer=", sqlStringValue(record.issuer));

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
    public LtiRegistrationRec fromResultSet(final ResultSet rs) throws SQLException {

        final String theClientId = getStringField(rs, DataDict.FLD_CLIENT_ID);
        final String theIssuer = getStringField(rs, DataDict.FLD_ISSUER);
        final String theIssuerPort = getStringField(rs, DataDict.FLD_ISSUER_PORT);
        final String theRedirectUri = getStringField(rs, DataDict.FLD_REDIRECT_URI);
        final String theAuthEndpoint = getStringField(rs, DataDict.FLD_AUTH_ENDPOINT);
        final String theTokenEndpoint = getStringField(rs, DataDict.FLD_TOKEN_ENDPOINT);
        final String theRegEndpoint = getStringField(rs, DataDict.FLD_REG_ENDPOINT);
        final String theJwksUri = getStringField(rs, DataDict.FLD_JWKS_URI);

        return new LtiRegistrationRec(theClientId, theIssuer, theIssuerPort, theRedirectUri, theAuthEndpoint,
                theTokenEndpoint, theRegEndpoint, theJwksUri);
    }
}
