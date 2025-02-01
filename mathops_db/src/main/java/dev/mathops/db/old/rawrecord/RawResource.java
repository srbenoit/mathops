package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "resource" record.
 */
public final class RawResource extends RecBase implements Comparable<RawResource> {

    /** The table name. */
    public static final String TABLE_NAME = "resource";

    /** A field name. */
    private static final String FLD_RESOURCE_ID = "resource_id";

    /** A field name. */
    private static final String FLD_RESOURCE_TYPE = "resource_type";

    /** A field name. */
    private static final String FLD_RESOURCE_DESC = "resource_desc";

    /** A field name. */
    private static final String FLD_DAYS_ALLOWED = "days_allowed";

    /** A field name. */
    private static final String FLD_HOLDS_ALLOWED = "holds_allowed";

    /** A field name. */
    private static final String FLD_HOLD_ID = "hold_id";

    /** A field name. */
    public static final String TYPE_INHOUSE_CALC = "IC";

    /** A field name. */
    public static final String TYPE_INHOUSE_HEADSET = "IH";

    /** A field name. */
    public static final String TYPE_INHOUSE_IPAD = "II";

    /** A field name. */
    public static final String TYPE_INHOUSE_LOCK = "IL";

    /** A field name. */
    public static final String TYPE_INHOUSE_NOTEBOOK = "IN";

    /** A field name. */
    public static final String TYPE_INHOUSE_TEXT = "IT";

    /** A field name. */
    public static final String TYPE_OFFICE_CALC = "OC";

    /** A field name. */
    public static final String TYPE_OVERNIGHT_TEXT = "OT";

    /** A field name. */
    public static final String TYPE_RENTAL_CALC = "RC";

    /** A field name. */
    public static final String TYPE_RENTAL_MANUAL = "RM";

    /** A field name. */
    public static final String TYPE_TUTOR_TABLET = "TT";

    /** The 'resource_id' field value. */
    public String resourceId;

    /** The 'resource_type' field value. */
    public String resourceType;

    /** The 'resourceresource_desc_id' field value. */
    public String resourceDesc;

    /** The 'days_allowed' field value. */
    public Integer daysAllowed;

    /** The 'holds_allowed' field value. */
    public Integer holdsAllowed;

    /** The 'hold_id' field value. */
    public String holdId;

    /**
     * Constructs a new {@code RawResource}.
     */
    private RawResource() {

        super();
    }

    /**
     * Constructs a new {@code RawResource}.
     *
     * @param theResourceId   the resource ID
     * @param theResourceType the resource type
     * @param theResourceDesc the resource description
     * @param theDaysAllowed  the time (minutes past midnight) of the loan
     * @param theHoldsAllowed the date the resource is due
     * @param theHoldId       the date the resource was returned
     */
    public RawResource(final String theResourceId, final String theResourceType,
                       final String theResourceDesc, final Integer theDaysAllowed, final Integer theHoldsAllowed,
                       final String theHoldId) {

        super();

        this.resourceId = theResourceId;
        this.resourceType = theResourceType;
        this.resourceDesc = theResourceDesc;
        this.daysAllowed = theDaysAllowed;
        this.holdsAllowed = theHoldsAllowed;
        this.holdId = theHoldId;
    }

    /**
     * Extracts a "resource" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawResource fromResultSet(final ResultSet rs) throws SQLException {

        final RawResource result = new RawResource();

        result.resourceId = getStringField(rs, FLD_RESOURCE_ID);
        result.resourceType = getStringField(rs, FLD_RESOURCE_TYPE);
        result.resourceDesc = getStringField(rs, FLD_RESOURCE_DESC);
        result.daysAllowed = getIntegerField(rs, FLD_DAYS_ALLOWED);
        result.holdsAllowed = getIntegerField(rs, FLD_HOLDS_ALLOWED);
        result.holdId = getStringField(rs, FLD_HOLD_ID);

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
    public int compareTo(final RawResource o) {

        int result = this.resourceType.compareTo(o.resourceType);

        if (result == 0) {
            result = this.resourceId.compareTo(o.resourceId);
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

        appendField(htm, FLD_RESOURCE_ID, this.resourceId);
        htm.add(DIVIDER);
        appendField(htm, FLD_RESOURCE_TYPE, this.resourceType);
        htm.add(DIVIDER);
        appendField(htm, FLD_RESOURCE_DESC, this.resourceDesc);
        htm.add(DIVIDER);
        appendField(htm, FLD_DAYS_ALLOWED, this.daysAllowed);
        htm.add(DIVIDER);
        appendField(htm, FLD_HOLDS_ALLOWED, this.holdsAllowed);
        htm.add(DIVIDER);
        appendField(htm, FLD_HOLD_ID, this.holdId);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.resourceId)
                + Objects.hashCode(this.resourceType)
                + Objects.hashCode(this.resourceDesc)
                + Objects.hashCode(this.daysAllowed)
                + Objects.hashCode(this.holdsAllowed)
                + Objects.hashCode(this.holdId);
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
        } else if (obj instanceof final RawResource rec) {
            equal = Objects.equals(this.resourceId, rec.resourceId)
                    && Objects.equals(this.resourceType, rec.resourceType)
                    && Objects.equals(this.resourceDesc, rec.resourceDesc)
                    && Objects.equals(this.daysAllowed, rec.daysAllowed)
                    && Objects.equals(this.holdsAllowed, rec.holdsAllowed)
                    && Objects.equals(this.holdId, rec.holdId);
        } else {
            equal = false;
        }

        return equal;
    }
}
