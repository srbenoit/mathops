package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "msg" record.
 */
public final class RawMsg extends RawTermRecordBase implements Comparable<RawMsg> {

    /** The table name. */
    public static final String TABLE_NAME = "msg";

    /** A field name. */
    private static final String FLD_TOUCH_POINT = "touch_point";

    /** A field name. */
    private static final String FLD_MSG_CODE = "msg_code";

    /** A field name. */
    private static final String FLD_SUBJECT = "subject";

    /** A field name. */
    private static final String FLD_TEMPLATE = "template";

    /** The 'touch_point' field value. */
    public String touchPoint;

    /** The 'msg_code' field value. */
    public String msgCode;

    /** The 'subject' field value. */
    public String subject;

    /** The 'template' field value. */
    public String template;

    /**
     * Constructs a new {@code RawMsg}.
     */
    private RawMsg() {

        super();
    }

    /**
     * Constructs a new {@code RawMsg}.
     *
     * @param theTermKey    the term key
     * @param theTouchPoint the touch point
     * @param theMsgCode    the message code
     * @param theSubject    the subject
     * @param theTemplate   the template
     */
    public RawMsg(final TermKey theTermKey, final String theTouchPoint, final String theMsgCode,
                  final String theSubject, final String theTemplate) {

        super(theTermKey);

        this.touchPoint = theTouchPoint;
        this.msgCode = theMsgCode;
        this.subject = theSubject;
        this.template = theTemplate;
    }

    /**
     * Extracts a "parameters" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawMsg fromResultSet(final ResultSet rs) throws SQLException {

        final RawMsg result = new RawMsg();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.touchPoint = getStringField(rs, FLD_TOUCH_POINT);
        result.msgCode = getStringField(rs, FLD_MSG_CODE);
        result.subject = getStringField(rs, FLD_SUBJECT);
        result.template = getStringField(rs, FLD_TEMPLATE);

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
    public int compareTo(final RawMsg o) {

        int result = compareAllowingNull(this.touchPoint, o.touchPoint);

        if (result == 0) {
            result = compareAllowingNull(this.msgCode, o.msgCode);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_TOUCH_POINT, this.touchPoint);
        htm.add(DIVIDER);
        appendField(htm, FLD_MSG_CODE, this.msgCode);
        htm.add(DIVIDER);
        appendField(htm, FLD_SUBJECT, this.subject);
        htm.add(DIVIDER);
        appendField(htm, FLD_TEMPLATE, this.template);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.termKey)
                + Objects.hashCode(this.touchPoint)
                + Objects.hashCode(this.msgCode)
                + Objects.hashCode(this.subject)
                + Objects.hashCode(this.template);
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
        } else if (obj instanceof final RawMsg rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.touchPoint, rec.touchPoint)
                    && Objects.equals(this.msgCode, rec.msgCode)
                    && Objects.equals(this.subject, rec.subject)
                    && Objects.equals(this.template, rec.template);
        } else {
            equal = false;
        }

        return equal;
    }
}
