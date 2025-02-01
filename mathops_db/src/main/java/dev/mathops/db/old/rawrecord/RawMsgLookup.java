package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "msg_lookup" record.
 */
public final class RawMsgLookup extends RecBase implements Comparable<RawMsgLookup> {

    /** The table name. */
    public static final String TABLE_NAME = "msg_lookup";

    /** A field name. */
    private static final String FLD_DOMAIN = "domain";

    /** A field name. */
    private static final String FLD_CODE = "code";

    /** A field name. */
    private static final String FLD_VALUE = "value";

    /** The 'domain' field value. */
    public String domain;

    /** The 'code' field value. */
    public String code;

    /** The 'value' field value. */
    public String value;

    /**
     * Constructs a new {@code RawMsgLookup}.
     */
    private RawMsgLookup() {

        super();
    }

    /**
     * Constructs a new {@code RawMsgLookup}.
     *
     * @param theDomain the message domain
     * @param theCode   the message code
     * @param theValue  the value
     */
    public RawMsgLookup(final String theDomain, final String theCode, final String theValue) {

        super();

        this.domain = theDomain;
        this.code = theCode;
        this.value = theValue;
    }

    /**
     * Extracts a "parameters" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawMsgLookup fromResultSet(final ResultSet rs) throws SQLException {

        final RawMsgLookup result = new RawMsgLookup();

        result.domain = getStringField(rs, FLD_DOMAIN);
        result.code = getStringField(rs, FLD_CODE);
        result.value = getStringField(rs, FLD_VALUE);

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
    public int compareTo(final RawMsgLookup o) {

        int result = compareAllowingNull(this.domain, o.domain);

        if (result == 0) {
            result = compareAllowingNull(this.code, o.code);
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

        appendField(htm, FLD_DOMAIN, this.domain);
        htm.add(DIVIDER);
        appendField(htm, FLD_CODE, this.code);
        htm.add(DIVIDER);
        appendField(htm, FLD_VALUE, this.value);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.domain)
               + Objects.hashCode(this.code)
               + Objects.hashCode(this.value);
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
        } else if (obj instanceof final RawMsgLookup rec) {
            equal = Objects.equals(this.domain, rec.domain)
                    && Objects.equals(this.code, rec.code)
                    && Objects.equals(this.value, rec.value);
        } else {
            equal = false;
        }

        return equal;
    }
}
