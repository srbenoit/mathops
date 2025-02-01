package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * An immutable administrative hold type.
 */
public final class RawHoldType extends RecBase implements Comparable<RawHoldType> {

    /** The table name. */
    public static final String TABLE_NAME = "hold_type";

    /** A field name. */
    private static final String FLD_HOLD_ID = "hold_id";

    /** A field name. */
    private static final String FLD_SEV_ADMIN_HOLD = "sev_admin_hold";

    /** A field name. */
    private static final String FLD_HOLD_TYPE = "hold_type";

    /** A field name. */
    private static final String FLD_ADD_HOLD = "add_hold";

    /** A field name. */
    private static final String FLD_DELETE_HOLD = "delete_hold";

    /** The 'hold_id' field value. */
    public String holdId;

    /** The 'sev_admin_hold' field value. */
    public String sevAdminHold;

    /** The 'hold_type' field value. */
    public String holdType;

    /** The 'add_hold' field value. */
    public String addHold;

    /** The 'delete_hold' field value. */
    public String deleteHold;

    /**
     * Constructs a new {@code RawHoldType}.
     */
    private RawHoldType() {

        super();
    }

    /**
     * Constructs a new {@code RawHoldType}.
     *
     * @param theHoldId       the hold ID
     * @param theSevAdminHold the hold severity
     * @param theHoldType     the hold type
     * @param theAddHold      "Y" if office staff (permission level 3) can add this hold
     * @param theDeleteHold   "Y" if office staff (permission level 3) can delete this hold
     */
    public RawHoldType(final String theHoldId, final String theSevAdminHold,
                       final String theHoldType, final String theAddHold, final String theDeleteHold) {

        super();

        this.holdId = theHoldId;
        this.sevAdminHold = theSevAdminHold;
        this.holdType = theHoldType;
        this.addHold = theAddHold;
        this.deleteHold = theDeleteHold;
    }

    /**
     * Extracts a "hold_type" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawHoldType fromResultSet(final ResultSet rs) throws SQLException {

        final RawHoldType result = new RawHoldType();

        result.holdId = getStringField(rs, FLD_HOLD_ID);
        result.sevAdminHold = getStringField(rs, FLD_SEV_ADMIN_HOLD);
        result.holdType = getStringField(rs, FLD_HOLD_TYPE);
        result.addHold = getStringField(rs, FLD_ADD_HOLD);
        result.deleteHold = getStringField(rs, FLD_DELETE_HOLD);

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
    public int compareTo(final RawHoldType o) {

        return compareAllowingNull(this.holdId, o.holdId);
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

        appendField(htm, FLD_HOLD_ID, this.holdId);
        htm.add(DIVIDER);
        appendField(htm, FLD_SEV_ADMIN_HOLD, this.sevAdminHold);
        htm.add(DIVIDER);
        appendField(htm, FLD_HOLD_TYPE, this.holdType);
        htm.add(DIVIDER);
        appendField(htm, FLD_ADD_HOLD, this.addHold);
        htm.add(DIVIDER);
        appendField(htm, FLD_DELETE_HOLD, this.deleteHold);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.holdId)
                + Objects.hashCode(this.sevAdminHold)
                + Objects.hashCode(this.holdType)
                + Objects.hashCode(this.addHold)
                + Objects.hashCode(this.deleteHold);
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
        } else if (obj instanceof final RawHoldType rec) {
            equal = Objects.equals(this.holdId, rec.holdId)
                    && Objects.equals(this.sevAdminHold, rec.sevAdminHold)
                    && Objects.equals(this.holdType, rec.holdType)
                    && Objects.equals(this.addHold, rec.addHold)
                    && Objects.equals(this.deleteHold, rec.deleteHold);
        } else {
            equal = false;
        }

        return equal;
    }
}
