package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "ststd" record. The primary key is stu_id/std_id.
 */
public final class RawStstd extends RecBase implements Comparable<RawStstd> {

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_STD_ID = "std_id";

    /** A field name. */
    private static final String FLD_WHEN_PLACED_OUT = "when_placed_out";

    /** A field name. */
    private static final String FLD_WHEN_MASTERED = "when_mastered";

    /** A field name. */
    private static final String FLD_MASTERED_GROUPS = "mastered_groups";

    /** A field name. */
    private static final String FLD_COMPLETED_STEPS = "completed_steps";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'std_id' field value. The unique ID of the standard. */
    public String stdId;

    /**
     * The 'placed_dt' field value. The date the student placed out of the standard via a placement activity; null if
     * not placed out. Note that mastery automatically satisfies placement.
     */
    public LocalDate whenPlacedOut;

    /**
     * The 'mastery_dt' field value. The date the student mastered the standard on a proctored assessment. This is the
     * date that one item from each required item group had been mastered.
     */
    public LocalDate whenMastered;

    /**
     * The 'mastered_groups' field value. An OR of bit flags, one for each item group assigned to the standard.
     */
    public Integer masteredGroups;

    /**
     * The 'completed_steps' field value. An OR of bit flags, one for each step (lesson, formative assessment, etc.)
     * related to the standard that the student can "complete" atomically.
     */
    public Integer completedSteps;

    /**
     * Constructs a new {@code RawStstd}.
     */
    private RawStstd() {

        super();
    }

    /**
     * Constructs a new {@code RawStstd}.
     *
     * @param theStuId          the student ID
     * @param theStdId          the standard ID
     * @param theWhenPlacedOut  the date the student placed out of the standard
     * @param theWhenMastered   the date the student mastered the standard
     * @param theMasteredGroups bit flags indicating the item groups the student has mastered
     * @param theCompletedSteps bit flags indicating the instructional steps the student has completed toward the
     *                          standard
     */
    public RawStstd(final String theStuId, final String theStdId, final LocalDate theWhenPlacedOut,
                    final LocalDate theWhenMastered, final Integer theMasteredGroups,
                    final Integer theCompletedSteps) {

        super();

        this.stuId = theStuId;
        this.stdId = theStdId;
        this.whenPlacedOut = theWhenPlacedOut;
        this.whenMastered = theWhenMastered;
        this.masteredGroups = theMasteredGroups;
        this.completedSteps = theCompletedSteps;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawStstd o) {

        int result = this.stdId.compareTo(o.stdId);

        if (result == 0) {
            result = this.stuId.compareTo(o.stuId);
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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_STD_ID, this.stdId);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_PLACED_OUT, this.whenPlacedOut);
        htm.add(DIVIDER);
        appendField(htm, FLD_WHEN_MASTERED, this.whenMastered);
        htm.add(DIVIDER);
        appendField(htm, FLD_MASTERED_GROUPS, this.masteredGroups);
        htm.add(DIVIDER);
        appendField(htm, FLD_COMPLETED_STEPS, this.completedSteps);

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
                + Objects.hashCode(this.stdId)
                + Objects.hashCode(this.whenPlacedOut)
                + Objects.hashCode(this.whenMastered)
                + Objects.hashCode(this.masteredGroups)
                + Objects.hashCode(this.completedSteps);
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
        } else if (obj instanceof final RawStstd rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.stdId, rec.stdId)
                    && Objects.equals(this.whenPlacedOut, rec.whenPlacedOut)
                    && Objects.equals(this.whenMastered, rec.whenMastered)
                    && Objects.equals(this.masteredGroups, rec.masteredGroups)
                    && Objects.equals(this.completedSteps, rec.completedSteps);
        } else {
            equal = false;
        }

        return equal;
    }
}
