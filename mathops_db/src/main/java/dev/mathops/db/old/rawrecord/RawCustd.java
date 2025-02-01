package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * A raw "custd" record. The primary key is course/unit/index.
 */
public final class RawCustd extends RecBase implements Comparable<RawCustd> {

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_UNIT = "unit";

    /** A field name. */
    private static final String FLD_INDEX = "index";

    /** A field name. */
    private static final String FLD_STD_ID = "std_id";

    /** The 'course' field value. */
    public String course;

    /** The 'unit' field value. */
    public Integer unit;

    /** The 'index' 1-based index of the standard within the unit, to provide logical ordering. */
    public Integer index;

    /** The 'std_id' field value. */
    public String stdId;

    /**
     * Constructs a new {@code RawCustd}.
     */
    private RawCustd() {

        super();
    }

    /**
     * Constructs a new {@code RawCustd}.
     *
     * @param theCourse the course
     * @param theUnit   the unit
     * @param theIndex  the index
     * @param theStdId  the standard ID
     */
    public RawCustd(final String theCourse, final Integer theUnit, final Integer theIndex,
                    final String theStdId) {

        super();

        this.course = theCourse;
        this.unit = theUnit;
        this.index = theIndex;
        this.stdId = theStdId;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawCustd o) {

        int result = this.course.compareTo(o.course);

        if (result == 0) {
            result = this.unit.compareTo(o.unit);
            if (result == 0) {
                result = this.index.compareTo(o.index);
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

        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_UNIT, this.unit);
        htm.add(DIVIDER);
        appendField(htm, FLD_INDEX, this.index);
        htm.add(DIVIDER);
        appendField(htm, FLD_STD_ID, this.stdId);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.course)
                + Objects.hashCode(this.unit)
                + Objects.hashCode(this.index)
                + Objects.hashCode(this.stdId);
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
        } else if (obj instanceof final RawCustd rec) {
            equal = Objects.equals(this.course, rec.course)
                    && Objects.equals(this.unit, rec.unit)
                    && Objects.equals(this.index, rec.index)
                    && Objects.equals(this.stdId, rec.stdId);
        } else {
            equal = false;
        }

        return equal;
    }
}
