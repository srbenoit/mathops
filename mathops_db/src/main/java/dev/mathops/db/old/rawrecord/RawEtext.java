package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "etext" record.
 */
public final class RawEtext extends RecBase implements Comparable<RawEtext> {

    /** The table name. */
    public static final String TABLE_NAME = "etext";

    /** A field name. */
    private static final String FLD_ETEXT_ID = "etext_id";

    /** A field name. */
    private static final String FLD_RETENTION = "retention";

    /** A field name. */
    private static final String FLD_PURCHASE_URL = "purchase_url";

    /** A field name. */
    private static final String FLD_REFUND_PERIOD = "refund_period";

    /** A field name. */
    private static final String FLD_KEY_ENTRY = "key_entry";

    /** A field name. */
    private static final String FLD_ACTIVE = "active";

    /** A field name. */
    private static final String FLD_BUTTON_LABEL = "button_label";

    /** The 'etext_id' field value. */
    public String etextId;

    /**
     * The 'retention' field value ('Y' if retained for all courses, C' if only retained for completed courses, 'N' if
     * not retained).
     */
    public String retention;

    /** The 'purchase_url' field value. */
    public String purchaseUrl;

    /** The 'refund_period' field value (in days after purchase). */
    public Integer refundPeriod;

    /** The 'key_entry' field value if students can enter a key directly. */
    public String keyEntry;

    /** The 'active' field value ('Y' or 'N'). */
    public String active;

    /** The 'button_label' field value. */
    public String buttonLabel;

    /**
     * Constructs a new {@code RawEtext}.
     */
    private RawEtext() {

        super();
    }

    /**
     * Constructs a new {@code RawEtext}.
     *
     * @param theEtextId      the e-text ID
     * @param theRetention    the retention
     * @param thePurchaseUrl  the purchase URL
     * @param theRefundPeriod the refund period
     * @param theKeyEntry     the key entry flag
     * @param theActive       the active flag
     * @param theButtonLabel  the button label
     */
    public RawEtext(final String theEtextId, final String theRetention, final String thePurchaseUrl,
                    final Integer theRefundPeriod, final String theKeyEntry, final String theActive,
                    final String theButtonLabel) {

        super();

        this.etextId = theEtextId;
        this.retention = theRetention;
        this.purchaseUrl = thePurchaseUrl;
        this.refundPeriod = theRefundPeriod;
        this.keyEntry = theKeyEntry;
        this.active = theActive;
        this.buttonLabel = theButtonLabel;
    }

    /**
     * Extracts an "etext_key" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawEtext fromResultSet(final ResultSet rs) throws SQLException {

        final RawEtext result = new RawEtext();

        result.etextId = getStringField(rs, FLD_ETEXT_ID);
        result.retention = getStringField(rs, FLD_RETENTION);
        result.purchaseUrl = getStringField(rs, FLD_PURCHASE_URL);
        result.refundPeriod = getIntegerField(rs, FLD_REFUND_PERIOD);
        result.keyEntry = getStringField(rs, FLD_KEY_ENTRY);
        result.active = getStringField(rs, FLD_ACTIVE);
        result.buttonLabel = getStringField(rs, FLD_BUTTON_LABEL);

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
    public int compareTo(final RawEtext o) {

        return compareAllowingNull(this.etextId, o.etextId);
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

        appendField(htm, FLD_ETEXT_ID, this.etextId);
        htm.add(DIVIDER);
        appendField(htm, FLD_RETENTION, this.retention);
        htm.add(DIVIDER);
        appendField(htm, FLD_PURCHASE_URL, this.purchaseUrl);
        htm.add(DIVIDER);
        appendField(htm, FLD_REFUND_PERIOD, this.refundPeriod);
        htm.add(DIVIDER);
        appendField(htm, FLD_KEY_ENTRY, this.keyEntry);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTIVE, this.active);
        htm.add(DIVIDER);
        appendField(htm, FLD_BUTTON_LABEL, this.buttonLabel);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.etextId)
                + Objects.hashCode(this.retention)
                + Objects.hashCode(this.purchaseUrl)
                + Objects.hashCode(this.refundPeriod)
                + Objects.hashCode(this.keyEntry)
                + Objects.hashCode(this.active)
                + Objects.hashCode(this.buttonLabel);
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
        } else if (obj instanceof final RawEtext rec) {
            equal = Objects.equals(this.etextId, rec.etextId)
                    && Objects.equals(this.retention, rec.retention)
                    && Objects.equals(this.purchaseUrl, rec.purchaseUrl)
                    && Objects.equals(this.refundPeriod, rec.refundPeriod)
                    && Objects.equals(this.keyEntry, rec.keyEntry)
                    && Objects.equals(this.active, rec.active)
                    && Objects.equals(this.buttonLabel, rec.buttonLabel);
        } else {
            equal = false;
        }

        return equal;
    }
}
