package dev.mathops.db.rec.main;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "LTI registration" record.
 *
 * <p>
 * Each record represents a registration of an LTI tool with an LMS.
 *
 * <p>
 * The primary key on the underlying table is combination of client ID and issuer.
 */
public final class LtiRegistrationRec extends RecBase implements Comparable<LtiRegistrationRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "lti_registration";

    /** The 'client_id' field value. */
    public final String clientId;

    /** The 'issuer' field value (a hostname). */
    public final String issuer;

    /** The 'issuer_port' field value (a port string like ":20443" or an empty string). */
    public final String issuerPort;

    /** The 'redirect_uri' field value (a URI). */
    public final String redirectUri;

    /** The 'auth_endpoint' field value (a URI). */
    public final String authEndpoint;

    /** The 'token_endpoint' field value (a URI). */
    public final String tokenEndpoint;

    /** The 'reg_endpoint' field value (a URI). */
    public final String regEndpoint;

    /** The 'jwks_uri' field value (a URI). */
    public final String jwksUri;

    /**
     * Constructs a new {@code LtiRegistrationRec}.
     *
     * @param theClientId      the client ID
     * @param theIssuer        the issuer host name
     * @param theIssuerPort    the issuer port number
     * @param theRedirectUri   the redirect URI
     * @param theAuthEndpoint  the authorization endpoint
     * @param theTokenEndpoint the token endpoint
     * @param theRegEndpoint   the registration endpoint
     * @param theJwksUri       the JWKS URI (null if the LMS did not provide one)
     */
    public LtiRegistrationRec(final String theClientId, final String theIssuer, final String theIssuerPort,
                              final String theRedirectUri, final String theAuthEndpoint,
                              final String theTokenEndpoint, final String theRegEndpoint, final String theJwksUri) {

        super();

        if (theClientId == null) {
            throw new IllegalArgumentException("Client ID may not be null");
        }
        if (theIssuer == null) {
            throw new IllegalArgumentException("Issuer may not be null");
        }
        if (theIssuerPort == null) {
            throw new IllegalArgumentException("Issuer port may not be null");
        }
        if (theRedirectUri == null) {
            throw new IllegalArgumentException("Redirect URI endpoint may not be null");
        }
        if (theAuthEndpoint == null) {
            throw new IllegalArgumentException("Authentication endpoint may not be null");
        }
        if (theTokenEndpoint == null) {
            throw new IllegalArgumentException("Token endpoint may not be null");
        }
        if (theRegEndpoint == null) {
            throw new IllegalArgumentException("Registration endpoint may not be null");
        }

        this.clientId = theClientId;
        this.issuer = theIssuer;
        this.issuerPort = theIssuerPort;
        this.redirectUri = theRedirectUri;
        this.authEndpoint = theAuthEndpoint;
        this.tokenEndpoint = theTokenEndpoint;
        this.regEndpoint = theRegEndpoint;
        this.jwksUri = theJwksUri;
    }

    /**
     * Compares two records for order.  Order is based on issuer, issuer port, then client ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final LtiRegistrationRec o) {

        int result = this.issuer.compareTo(o.issuer);

        if (result == 0) {
            result = this.issuerPort.compareTo(o.issuerPort);

            if (result == 0) {
                result = this.clientId.compareTo(o.clientId);
            }
        }

        return result;
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, DataDict.FLD_CLIENT_ID, this.clientId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ISSUER, this.issuer);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ISSUER_PORT, this.issuerPort);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_REDIRECT_URI, this.redirectUri);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_AUTH_ENDPOINT, this.authEndpoint);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_TOKEN_ENDPOINT, this.tokenEndpoint);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_REG_ENDPOINT, this.regEndpoint);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_JWKS_URI, this.jwksUri);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.clientId.hashCode()
               + this.issuer.hashCode()
               + this.issuerPort.hashCode()
               + this.redirectUri.hashCode()
               + this.authEndpoint.hashCode()
               + this.tokenEndpoint.hashCode()
               + this.regEndpoint.hashCode()
               + Objects.hashCode(this.jwksUri);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final LtiRegistrationRec rec) {
            equal = this.clientId.equals(rec.clientId)
                    && this.issuer.equals(rec.issuer)
                    && this.issuerPort.equals(rec.issuerPort)
                    && this.redirectUri.equals(rec.redirectUri)
                    && this.authEndpoint.equals(rec.authEndpoint)
                    && this.tokenEndpoint.equals(rec.tokenEndpoint)
                    && this.regEndpoint.equals(rec.regEndpoint)
                    && Objects.equals(this.jwksUri, rec.jwksUri);
        } else {
            equal = false;
        }

        return equal;
    }
}
