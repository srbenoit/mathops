package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A raw "item" record.
 */
public final class RawItem extends RecBase implements Comparable<RawItem> {

    /** A field name. */
    private static final String FLD_ITEM_ID = "item_id";

    /** The item ID. */
    public String itemId;

    /**
     * Constructs a new {@code RawItem}.
     */
    private RawItem() {

        super();
    }

    /**
     * Constructs a new {@code RawItem}.
     *
     * @param theItemId the item ID
     */
    public RawItem(final String theItemId) {

        super();

        this.itemId = theItemId;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawItem o) {

        return compareAllowingNull(this.itemId, o.itemId);
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

        appendField(htm, FLD_ITEM_ID, this.itemId);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.itemId);
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
        } else if (obj instanceof final RawItem rec) {
            equal = Objects.equals(this.itemId, rec.itemId);
        } else {
            equal = false;
        }

        return equal;
    }
}
