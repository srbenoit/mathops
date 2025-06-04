package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "LTI context" record.
 *
 * <p>
 * Each record represents an LMS course in which the LTI tool has been configured.
 *
 * <p>
 * The primary key on the underlying table is combination of client ID, issuer, deployment ID, and context ID.
 */
public final class LtiContextRec extends RecBase implements Comparable<LtiContextRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "lti_context";

    /** The 'client_id' field value. */
    public final String clientId;

    /** The 'issuer' field value (a hostname). */
    public final String issuer;

    /** The 'deployment_id' field value. */
    public final String deploymentId;

    /** The 'context_id' field value. */
    public final String contextId;

    /** The 'lms_course_id' field value. */
    public final String lmsCourseId;

    /** The 'lms_course_title' field value. */
    public final String lmsCourseTitle;

    /**
     * Constructs a new {@code LtiContextRec}.
     *
     * @param theClientId       the client ID
     * @param theIssuer         the issuer host name
     * @param theDeploymentId   the LMS deployment ID
     * @param theContextId      the LMS context (course) ID
     * @param theLmsCourseId    the LMS course ID (for API access)
     * @param theLmsCourseTitle the LMS course title
     */
    public LtiContextRec(final String theClientId, final String theIssuer, final String theDeploymentId,
                         final String theContextId, final String theLmsCourseId, final String theLmsCourseTitle) {

        super();

        if (theClientId == null) {
            throw new IllegalArgumentException("Client ID may not be null");
        }
        if (theIssuer == null) {
            throw new IllegalArgumentException("Issuer may not be null");
        }
        if (theDeploymentId == null) {
            throw new IllegalArgumentException("Deployment ID may not be null");
        }
        if (theContextId == null) {
            throw new IllegalArgumentException("Context ID may not be null");
        }

        this.clientId = theClientId;
        this.issuer = theIssuer;
        this.deploymentId = theDeploymentId;
        this.contextId = theContextId;
        this.lmsCourseId = theLmsCourseId;
        this.lmsCourseTitle = theLmsCourseTitle;
    }

    /**
     * Compares two records for order.  Order is based on issuer, issuer port, then client ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final LtiContextRec o) {

        int result = this.issuer.compareTo(o.issuer);

        if (result == 0) {
            result = this.clientId.compareTo(o.clientId);

            if (result == 0) {
                result = this.deploymentId.compareTo(o.deploymentId);

                if (result == 0) {
                    result = this.contextId.compareTo(o.contextId);
                }
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
        appendField(htm, DataDict.FLD_DEPLOYMENT_ID, this.deploymentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CONTEXT_ID, this.contextId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_LMS_COURSE_ID, this.lmsCourseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_LMS_COURSE_TITLE, this.lmsCourseTitle);

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
               + this.deploymentId.hashCode()
               + this.contextId.hashCode()
               + Objects.hashCode(this.lmsCourseId)
               + Objects.hashCode(this.lmsCourseTitle);
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
        } else if (obj instanceof final LtiContextRec rec) {
            equal = this.clientId.equals(rec.clientId)
                    && this.issuer.equals(rec.issuer)
                    && this.deploymentId.equals(rec.deploymentId)
                    && this.contextId.equals(rec.contextId)
                    && Objects.equals(this.lmsCourseId, rec.lmsCourseId)
                    && Objects.equals(this.lmsCourseTitle, rec.lmsCourseTitle);
        } else {
            equal = false;
        }

        return equal;
    }
}
