package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "etext_key" record.
 */
public final class RawEtextKey extends RecBase implements Comparable<RawEtextKey> {

    /** The table name. */
    public static final String TABLE_NAME = "etext_key";

    /** A field name. */
    private static final String FLD_ETEXT_ID = "etext_id";

    /** A field name. */
    private static final String FLD_ETEXT_KEY = "etext_key";

    /** A field name. */
    private static final String FLD_ACTIVE_DT = "active_dt";

    /** The 'etext_id' field value. */
    public String etextId;

    /** The 'etext_key' field value. */
    public String etextKey;

    /** The 'active_dt' field value. */
    public LocalDateTime activeDt;

    /**
     * Constructs a new {@code RawEtextKey}.
     */
    private RawEtextKey() {

        super();
    }

    /**
     * Constructs a new {@code RawEtextKey}.
     *
     * @param theEtextId  the e-text ID
     * @param theEtextKey the e-text key
     * @param theActiveDt the active date/time
     */
    public RawEtextKey(final String theEtextId, final String theEtextKey, final LocalDateTime theActiveDt) {

        super();

        this.etextId = theEtextId;
        this.etextKey = theEtextKey;
        this.activeDt = theActiveDt;
    }

    /**
     * Extracts an "etext_key" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawEtextKey fromResultSet(final ResultSet rs) throws SQLException {

        final RawEtextKey result = new RawEtextKey();

        result.etextId = getStringField(rs, FLD_ETEXT_ID);
        result.etextKey = getStringField(rs, FLD_ETEXT_KEY);
        result.activeDt = getDateTimeField(rs, FLD_ACTIVE_DT);

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
    public int compareTo(final RawEtextKey o) {

        int result = compareAllowingNull(this.etextId, o.etextId);

        if (result == 0) {
            result = compareAllowingNull(this.activeDt, o.activeDt);
            if (result == 0) {
                result = compareAllowingNull(this.etextKey, o.etextKey);
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

        appendField(htm, FLD_ETEXT_ID, this.etextId);
        htm.add(DIVIDER);
        appendField(htm, FLD_ETEXT_KEY, this.etextKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTIVE_DT, this.activeDt);

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
                + Objects.hashCode(this.etextKey)
                + Objects.hashCode(this.activeDt);
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
        } else if (obj instanceof final RawEtextKey rec) {
            equal = Objects.equals(this.etextId, rec.etextId)
                    && Objects.equals(this.etextKey, rec.etextKey)
                    && Objects.equals(this.activeDt, rec.activeDt);
        } else {
            equal = false;
        }

        return equal;
    }
}
