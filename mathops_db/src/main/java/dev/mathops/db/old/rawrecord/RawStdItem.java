package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A raw "stditem" record. The primary key is std_id/item_id.
 */
public final class RawStdItem extends RecBase implements Comparable<RawStdItem> {

    /** A field name. */
    private static final String FLD_STD_ID = "std_id";

    /** A field name. */
    private static final String FLD_ITEM_ID = "item_id";

    /** A field name. */
    private static final String FLD_MASTERY_GRP_NBR = "mastery_grp_nbr";

    /** A field name. */
    private static final String FLD_PRACTICE_GRP_NBR = "practice_grp_nbr";

    /** A field name. */
    private static final String FLD_FORMATIVE = "formative";

    /** A field name. */
    private static final String FLD_PLACEMENT = "placement";

    /** The 'std_id' field value. The unique ID of the standard. */
    public String stdId;

    /** The 'item_id' field value. The unique ID of the item. */
    public String itemId;

    /**
     * The 'mastery_grp_nbr' field value. The group number (from 1 to N) of the item group to which this item belongs. A
     * standard will define the set of group numbers that must be mastered in order to have "mastered" the standard -
     * each item can be assigned to one such group. When creating an assessment for the group within a standard, all
     * items with that group number are queried, and one is selected at random, randomized and generated, and presented
     * to the student as part of a mastery exam.
     */
    public Integer masteryGrpNbr;

    /**
     * The 'practice_grp_nbr' field value. The group number (from 1 to N) of the item group to which this item belongs.
     * See {@code masteryGroupNbr} for a description of group numbers. This code indicates the group number for which
     * this item can be used as practice. This allows the set of exam items to differ from the set of practice items,
     * but also allows one item to be used for both practice and exams. A student may be required to get some collection
     * of practice items correct in order to be eligible for a mastery exam.
     */
    public Integer practiceGrpNbr;

    /**
     * The 'formative' field. 'Y' if this item should be used within instructional content. Instructional content will
     * include materials and references to these items (by group number). When one of these is encountered, an item from
     * that group is randomized and presented to the student as part of instruction. It may require the student to
     * correctly complete the item to access the next element in the instructional content.
     */
    public String formative;

    /**
     * The 'placement' field. 'Y' if this item can be used to place out of the standard on a placement assessment; 'N'
     * if not. This should be set for items that somehow measure general competence on the standard, even when there are
     * multiple item groups attached to the standard.
     */
    public String placement;

    /**
     * Constructs a new {@code RawStdItem}.
     */
    private RawStdItem() {

        super();
    }

    /**
     * Constructs a new {@code RawStdItem}.
     *
     * @param theStdId          the standard ID
     * @param theItemId         the item ID
     * @param theMasteryGrpNbr  the mastery group number
     * @param thePracticeGrpNbr the practice group number
     * @param theFormative      "Y" if this can be used
     * @param thePlacement      "Y" if this item can be used to place out of a standard
     */
    public RawStdItem(final String theStdId, final String theItemId, final Integer theMasteryGrpNbr,
                      final Integer thePracticeGrpNbr, final String theFormative, final String thePlacement) {
        super();

        this.stdId = theStdId;
        this.itemId = theItemId;
        this.masteryGrpNbr = theMasteryGrpNbr;
        this.practiceGrpNbr = thePracticeGrpNbr;
        this.formative = theFormative;
        this.placement = thePlacement;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawStdItem o) {

        int result = this.stdId.compareTo(o.stdId);

        if (result == 0) {
            result = this.itemId.compareTo(o.itemId);
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

        appendField(htm, FLD_STD_ID, this.stdId);
        htm.add(DIVIDER);
        appendField(htm, FLD_ITEM_ID, this.itemId);
        htm.add(DIVIDER);
        appendField(htm, FLD_MASTERY_GRP_NBR, this.masteryGrpNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_PRACTICE_GRP_NBR, this.practiceGrpNbr);
        htm.add(DIVIDER);
        appendField(htm, FLD_FORMATIVE, this.formative);
        htm.add(DIVIDER);
        appendField(htm, FLD_PLACEMENT, this.placement);

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
                + Objects.hashCode(this.itemId)
                + Objects.hashCode(this.masteryGrpNbr)
                + Objects.hashCode(this.practiceGrpNbr)
                + Objects.hashCode(this.formative)
                + Objects.hashCode(this.placement);
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
        } else if (obj instanceof final RawStdItem rec) {
            equal = Objects.equals(this.stdId, rec.stdId)
                    && Objects.equals(this.itemId, rec.itemId)
                    && Objects.equals(this.masteryGrpNbr, rec.masteryGrpNbr)
                    && Objects.equals(this.practiceGrpNbr, rec.practiceGrpNbr)
                    && Objects.equals(this.formative, rec.formative)
                    && Objects.equals(this.placement, rec.placement);
        } else {
            equal = false;
        }

        return equal;
    }
}
