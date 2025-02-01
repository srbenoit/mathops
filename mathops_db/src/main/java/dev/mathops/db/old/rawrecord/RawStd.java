package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A raw "std" record. The primary key is std_id.
 */
public final class RawStd extends RecBase implements Comparable<RawStd> {

    /** A field name. */
    private static final String FLD_STD_ID = "std_id";

    /** A field name. */
    private static final String FLD_MASTERY_GROUPS = "mastery_groups";

    /** The 'std_id' field value. The unique ID of the standard. */
    public String stdId;

    /**
     * The 'mastery_groups' field value. An OR of bit flags, one for each item group that must be mastered to have
     * mastered the standard. Typically, 0x01, 0x03, or 0x07, if there are 1, 2, or 3 item groups needed, respectively.
     */
    public Integer masteryGroups;

    /**
     * Constructs a new {@code RawStd}.
     */
    private RawStd() {

        super();
    }

    /**
     * Constructs a new {@code RawStd}.
     *
     * @param theStdId         the standard ID
     * @param theMasteryGroups bit flags indicating the item groups the student must master to master the standard
     */
    public RawStd(final String theStdId, final Integer theMasteryGroups) {

        super();

        this.stdId = theStdId;
        this.masteryGroups = theMasteryGroups;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawStd o) {

        return this.stdId.compareTo(o.stdId);
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

        appendField(htm, FLD_STD_ID, this.stdId);
        htm.add(DIVIDER);
        appendField(htm, FLD_MASTERY_GROUPS, this.masteryGroups);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stdId)
                + Objects.hashCode(this.masteryGroups);
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
        } else if (obj instanceof final RawStd rec) {
            equal = Objects.equals(this.stdId, rec.stdId)
                    && Objects.equals(this.masteryGroups, rec.masteryGroups);
        } else {
            equal = false;
        }

        return equal;
    }
}
