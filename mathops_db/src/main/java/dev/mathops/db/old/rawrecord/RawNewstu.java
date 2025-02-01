package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "newstu" record.
 */
public final class RawNewstu extends RecBase implements Comparable<RawNewstu> {

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_ACAD_LEVEL = "acad_level";

    /** A field name. */
    private static final String FLD_REG_TYPE = "reg_type";

    /** A field name. */
    private static final String FLD_TERM = "term";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'acad_level' field value. */
    public String acadLevel;

    /** The 'reg_type' field value. */
    public String regType;

    /** The 'term' field value. */
    public String term;

    /**
     * Constructs a new {@code RawNewstu}.
     */
    private RawNewstu() {

        super();
    }

    /**
     * Constructs a new {@code RawNewstu}.
     *
     * @param theStuId     the 'stu_id' field value
     * @param theAcadLevel the 'acad_level' field value
     * @param theRegType   the 'reg_type' field value
     * @param theTerm      the 'term' field value
     */
    public RawNewstu(final String theStuId, final String theAcadLevel, final String theRegType,
                     final String theTerm) {

        super();

        this.stuId = theStuId;
        this.acadLevel = theAcadLevel;
        this.regType = theRegType;
        this.term = theTerm;
    }

    /**
     * Extracts a "stcourse" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawNewstu fromResultSet(final ResultSet rs) throws SQLException {

        final RawNewstu result = new RawNewstu();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.acadLevel = getStringField(rs, FLD_ACAD_LEVEL);
        result.regType = getStringField(rs, FLD_REG_TYPE);
        result.term = getStringField(rs, FLD_TERM);

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
    public int compareTo(final RawNewstu o) {

        return compareAllowingNull(this.stuId, o.stuId);
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
        appendField(htm, FLD_ACAD_LEVEL, this.acadLevel);
        htm.add(DIVIDER);
        appendField(htm, FLD_REG_TYPE, this.regType);
        htm.add(DIVIDER);
        appendField(htm, FLD_TERM, this.term);

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
                + Objects.hashCode(this.acadLevel)
                + Objects.hashCode(this.regType)
                + Objects.hashCode(this.term);
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
        } else if (obj instanceof final RawNewstu rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.acadLevel, rec.acadLevel)
                    && Objects.equals(this.regType, rec.regType)
                    && Objects.equals(this.term, rec.term);
        } else {
            equal = false;
        }

        return equal;
    }
}
