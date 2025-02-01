package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "stmsg" record.
 */
public final class RawStmsg extends RecBase implements Comparable<RawStmsg> {

    /** The table name. */
    public static final String TABLE_NAME = "stmsg";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_MSG_DT = "msg_dt";

    /** A field name. */
    private static final String FLD_PACE = "pace";

    /** A field name. */
    private static final String FLD_COURSE_INDEX = "course_index";

    /** A field name. */
    private static final String FLD_TOUCH_POINT = "touch_point";

    /** A field name. */
    private static final String FLD_MSG_CODE = "msg_code";

    /** A field name. */
    private static final String FLD_SENDER = "sender";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'msg_dt' field value. */
    public LocalDate msgDt;

    /** The student's pace at the time the message was sent. */
    public Integer pace;

    /** The index (1-pace) of the course to which the message applies, 0 for pre-course. */
    public Integer courseIndex;

    /** The 'touch_point' field value. */
    public String touchPoint;

    /** The 'msg_code' field value. */
    public String msgCode;

    /** The 'sender' field value. */
    public String sender;

    /**
     * Constructs a new {@code RawStmsg}.
     */
    private RawStmsg() {

        super();
    }

    /**
     * Constructs a new {@code RawStmsg}.
     *
     * @param theStuId       the student ID
     * @param theMsgDt       the message date
     * @param thePace        the student's pace at the time the message was sent
     * @param theCourseIndex the index (1-pace) of the course to which the message applies, 0 for pre-course
     * @param theTouchPoint  the touch point
     * @param theMsgCode     the message code
     * @param theSender      the sender
     */
    public RawStmsg(final String theStuId, final LocalDate theMsgDt, final Integer thePace,
                    final Integer theCourseIndex, final String theTouchPoint, final String theMsgCode,
                    final String theSender) {

        super();

        this.stuId = theStuId;
        this.msgDt = theMsgDt;
        this.pace = thePace;
        this.courseIndex = theCourseIndex;
        this.touchPoint = theTouchPoint;
        this.msgCode = theMsgCode;
        this.sender = theSender;
    }

    /**
     * Extracts a "stmsg" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStmsg fromResultSet(final ResultSet rs) throws SQLException {

        final RawStmsg result = new RawStmsg();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.msgDt = getDateField(rs, FLD_MSG_DT);
        result.pace = getIntegerField(rs, FLD_PACE);
        result.courseIndex = getIntegerField(rs, FLD_COURSE_INDEX);
        result.touchPoint = getStringField(rs, FLD_TOUCH_POINT);
        result.msgCode = getStringField(rs, FLD_MSG_CODE);
        result.sender = getStringField(rs, FLD_SENDER);

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
    public int compareTo(final RawStmsg o) {

        int result = compareAllowingNull(this.stuId, o.stuId);

        if (result == 0) {
            result = compareAllowingNull(this.msgDt, o.msgDt);
            if (result == 0) {
                result = compareAllowingNull(this.msgCode, o.msgCode);
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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_MSG_DT, this.msgDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_INDEX, this.courseIndex);
        htm.add(DIVIDER);
        appendField(htm, FLD_TOUCH_POINT, this.touchPoint);
        htm.add(DIVIDER);
        appendField(htm, FLD_MSG_CODE, this.msgCode);
        htm.add(DIVIDER);
        appendField(htm, FLD_SENDER, this.sender);

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
                + Objects.hashCode(this.msgDt)
                + Objects.hashCode(this.pace)
                + Objects.hashCode(this.courseIndex)
                + Objects.hashCode(this.touchPoint)
                + Objects.hashCode(this.msgCode)
                + Objects.hashCode(this.sender);
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
        } else if (obj instanceof final RawStmsg rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.msgDt, rec.msgDt)
                    && Objects.equals(this.pace, rec.pace)
                    && Objects.equals(this.courseIndex, rec.courseIndex)
                    && Objects.equals(this.touchPoint, rec.touchPoint)
                    && Objects.equals(this.msgCode, rec.msgCode)
                    && Objects.equals(this.sender, rec.sender);
        } else {
            equal = false;
        }

        return equal;
    }
}
