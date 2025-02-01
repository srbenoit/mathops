package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "stetext" record.
 */
public final class RawStetext extends RecBase {

    /** The table name. */
    public static final String TABLE_NAME = "stetext";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_ETEXT_ID = "etext_id";

    /** A field name. */
    private static final String FLD_ACTIVE_DT = "active_dt";

    /** A field name. */
    private static final String FLD_ETEXT_KEY = "etext_key";

    /** A field name. */
    private static final String FLD_EXPIRATION_DT = "expiration_dt";

    /** A field name. */
    private static final String FLD_REFUND_DEADLINE_DT = "refund_deadline_dt";

    /** A field name. */
    private static final String FLD_REFUND_DT = "refund_dt";

    /** A field name. */
    private static final String FLD_REFUND_REASON = "refund_reason";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'etext_id' field value. */
    public String etextId;

    /** The 'active_dt' field value. */
    public LocalDate activeDt;

    /** The 'etext_key' field value. */
    public String etextKey;

    /** The 'expiration_dt' field value. */
    public LocalDate expirationDt;

    /** The 'refund_deadline_dt' field value. */
    public LocalDate refundDeadlineDt;

    /** The 'refund_dt' field value. */
    public LocalDate refundDt;

    /** The 'refund_reason' field value. */
    public String refundReason;

    /**
     * Constructs a new {@code RawStetext}.
     */
    private RawStetext() {

        super();
    }

    /**
     * Constructs a new {@code RawStetext}.
     *
     * @param theStuId            the student ID
     * @param theEtextId          the e-text ID
     * @param theActiveDt         the active date
     * @param theEtextKey         the e-text key
     * @param theExpirationDt     the expiration date
     * @param theRefundDeadlineDt the refund deadline date
     * @param theRefundDt         the refund date
     * @param theRefundReason     the refund reason
     */
    public RawStetext(final String theStuId, final String theEtextId, final LocalDate theActiveDt,
                      final String theEtextKey, final LocalDate theExpirationDt,
                      final LocalDate theRefundDeadlineDt, final LocalDate theRefundDt,
                      final String theRefundReason) {
        super();

        this.stuId = theStuId;
        this.etextId = theEtextId;
        this.activeDt = theActiveDt;
        this.etextKey = theEtextKey;
        this.expirationDt = theExpirationDt;
        this.refundDeadlineDt = theRefundDeadlineDt;
        this.refundDt = theRefundDt;
        this.refundReason = theRefundReason;
    }

    /**
     * Extracts a "stetext" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawStetext fromResultSet(final ResultSet rs) throws SQLException {

        final RawStetext result = new RawStetext();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.etextId = getStringField(rs, FLD_ETEXT_ID);
        result.activeDt = getDateField(rs, FLD_ACTIVE_DT);
        result.etextKey = getStringField(rs, FLD_ETEXT_KEY);
        result.expirationDt = getDateField(rs, FLD_EXPIRATION_DT);
        result.refundDeadlineDt = getDateField(rs, FLD_REFUND_DEADLINE_DT);
        result.refundDt = getDateField(rs, FLD_REFUND_DT);
        result.refundReason = getStringField(rs, FLD_REFUND_REASON);

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
        appendField(htm, FLD_ETEXT_ID, this.etextId);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTIVE_DT, this.activeDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_ETEXT_KEY, this.etextKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXPIRATION_DT, this.expirationDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_REFUND_DEADLINE_DT, this.refundDeadlineDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_REFUND_DT, this.refundDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_REFUND_REASON, this.refundReason);

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
                + Objects.hashCode(this.etextId)
                + Objects.hashCode(this.activeDt)
                + Objects.hashCode(this.etextKey)
                + Objects.hashCode(this.expirationDt)
                + Objects.hashCode(this.refundDeadlineDt)
                + Objects.hashCode(this.refundDt)
                + Objects.hashCode(this.refundReason);
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
        } else if (obj instanceof final RawStetext rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.etextId, rec.etextId)
                    && Objects.equals(this.activeDt, rec.activeDt)
                    && Objects.equals(this.etextKey, rec.etextKey)
                    && Objects.equals(this.expirationDt, rec.expirationDt)
                    && Objects.equals(this.refundDeadlineDt, rec.refundDeadlineDt)
                    && Objects.equals(this.refundDt, rec.refundDt)
                    && Objects.equals(this.refundReason, rec.refundReason);
        } else {
            equal = false;
        }

        return equal;
    }
}
