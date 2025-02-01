package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "discipline" record.
 */
public final class RawDiscipline extends RecBase implements Comparable<RawDiscipline> {

    /** The table name. */
    public static final String TABLE_NAME = "discipline";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_DT_INCIDENT = "dt_incident";

    /** A field name. */
    private static final String FLD_INCIDENT_TYPE = "incident_type";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_CHEAT_DESC = "cheat_desc";

    /** A field name. */
    private static final String FLD_ACTION_TYPE = "action_type";

    /** A field name. */
    private static final String FLD_ACTION_COMMENT = "action_comment";

    /** A field name. */
    private static final String FLD_INTERVIEWER = "interviewer";

    /** A field name. */
    private static final String FLD_PROCTOR = "proctor";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'dt_incident' field value. */
    public LocalDate dtIncident;

    /** The 'incident_type' field value. */
    public String incidentType;

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'cheat_desc' field value. */
    public String cheatDesc;

    /** The 'action_type' field value. */
    public String actionType;

    /** The 'action_comment' field value. */
    public String actionComment;

    /** The 'interviewer' field value. */
    public String interviewer;

    /** The 'proctor' field value. */
    public String proctor;

    /**
     * Constructs a new {@code RawDiscipline}.
     */
    private RawDiscipline() {

        super();
    }

    /**
     * Constructs a new {@code RawDiscipline}.
     *
     * @param theStuId         the student ID
     * @param theDtIncident    the incident date
     * @param theIncidentType  the incident type
     * @param theCourse        the course
     * @param theUnit          the unit
     * @param theCheatDesc     a description of the cheating
     * @param theActionType    the type of action taken (a numeric code)
     * @param theActionComment a comment
     * @param theInterviewer   the interviewer name(s) or login(s)
     * @param theProctor       the proctor
     */
    public RawDiscipline(final String theStuId, final LocalDate theDtIncident,
                         final String theIncidentType, final String theCourse, final Integer theUnit,
                         final String theCheatDesc, final String theActionType, final String theActionComment,
                         final String theInterviewer, final String theProctor) {

        super();

        this.stuId = theStuId;
        this.dtIncident = theDtIncident;
        this.incidentType = theIncidentType;
        this.course = theCourse;
        this.unit = theUnit;
        this.cheatDesc = theCheatDesc;
        this.actionType = theActionType;
        this.actionComment = theActionComment;
        this.interviewer = theInterviewer;
        this.proctor = theProctor;
    }

    /**
     * Extracts an "discipline" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawDiscipline fromResultSet(final ResultSet rs) throws SQLException {

        final RawDiscipline result = new RawDiscipline();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.dtIncident = getDateField(rs, FLD_DT_INCIDENT);
        result.incidentType = getStringField(rs, FLD_INCIDENT_TYPE);
        result.course = getStringField(rs, FLD_COURSE);
        result.unit = getIntegerField(rs, FLD_UNIT);
        result.cheatDesc = getStringField(rs, FLD_CHEAT_DESC);
        result.actionType = getStringField(rs, FLD_ACTION_TYPE);
        result.actionComment = getStringField(rs, FLD_ACTION_COMMENT);
        result.interviewer = getStringField(rs, FLD_INTERVIEWER);
        result.proctor = getStringField(rs, FLD_PROCTOR);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawDiscipline o) {

        int result = compareAllowingNull(this.dtIncident, o.dtIncident);

        if (result == 0) {
            result = compareAllowingNull(this.stuId, o.stuId);
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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_DT_INCIDENT, this.dtIncident);
        htm.add(DIVIDER);
        appendField(htm, FLD_INCIDENT_TYPE, this.incidentType);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_CHEAT_DESC, this.cheatDesc);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTION_TYPE, this.actionType);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTION_COMMENT, this.actionComment);
        htm.add(DIVIDER);
        appendField(htm, FLD_INTERVIEWER, this.interviewer);
        htm.add(DIVIDER);
        appendField(htm, FLD_PROCTOR, this.proctor);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId)
                + Objects.hashCode(this.dtIncident)
                + Objects.hashCode(this.incidentType)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.unit)
                + Objects.hashCode(this.cheatDesc)
                + Objects.hashCode(this.actionType)
                + Objects.hashCode(this.actionComment)
                + Objects.hashCode(this.interviewer)
                + Objects.hashCode(this.proctor);
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
        } else if (obj instanceof final RawDiscipline rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.dtIncident, rec.dtIncident)
                    && Objects.equals(this.incidentType, rec.incidentType)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.cheatDesc, rec.cheatDesc)
                    && Objects.equals(this.actionType, rec.actionType)
                    && Objects.equals(this.actionComment, rec.actionComment)
                    && Objects.equals(this.interviewer, rec.interviewer)
                    && Objects.equals(this.proctor, rec.proctor);
        } else {
            equal = false;
        }

        return equal;
    }
}
