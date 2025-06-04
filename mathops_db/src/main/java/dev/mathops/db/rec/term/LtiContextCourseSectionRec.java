package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "LTI context course section" record.
 *
 * <p>
 * Each record represents a connection between an LMS context (course) and a CSU course section.
 *
 * <p>
 * The primary key on the underlying table is combination of client ID, issuer, deployment ID, context ID, course ID,
 * and section number.
 */
public final class LtiContextCourseSectionRec extends RecBase implements Comparable<LtiContextCourseSectionRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "lti_context_course_section";

    /** The 'client_id' field value. */
    public final String clientId;

    /** The 'issuer' field value (a hostname). */
    public final String issuer;

    /** The 'deployment_id' field value. */
    public final String deploymentId;

    /** The 'context_id' field value. */
    public final String contextId;

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'section_nbr' field value (a URI). */
    public final String sectionNbr;

    /**
     * Constructs a new {@code LtiContextCourseSectionRec}.
     *
     * @param theClientId     the client ID
     * @param theIssuer       the issuer host name
     * @param theDeploymentId the LMS deployment ID
     * @param theContextId    the LMS context (course) ID
     * @param theCourseId     the institution course ID
     * @param theSectionNbr   the institution section number
     */
    public LtiContextCourseSectionRec(final String theClientId, final String theIssuer, final String theDeploymentId,
                                      final String theContextId, final String theCourseId, final String theSectionNbr) {

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
        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theSectionNbr == null) {
            throw new IllegalArgumentException("Section number may not be null");
        }

        this.clientId = theClientId;
        this.issuer = theIssuer;
        this.deploymentId = theDeploymentId;
        this.contextId = theContextId;
        this.courseId = theCourseId;
        this.sectionNbr = theSectionNbr;
    }

    /**
     * Compares two records for order.  Order is based on issuer, issuer port, then client ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final LtiContextCourseSectionRec o) {

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
        appendField(htm, DataDict.FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_SECTION_NBR, this.sectionNbr);

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
               + this.courseId.hashCode()
               + this.sectionNbr.hashCode();
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
        } else if (obj instanceof final LtiContextCourseSectionRec rec) {
            equal = this.clientId.equals(rec.clientId)
                    && this.issuer.equals(rec.issuer)
                    && this.deploymentId.equals(rec.deploymentId)
                    && this.contextId.equals(rec.contextId)
                    && this.courseId.equals(rec.courseId)
                    && this.sectionNbr.equals(rec.sectionNbr);
        } else {
            equal = false;
        }

        return equal;
    }
}
