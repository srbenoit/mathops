package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "parameters" record.
 */
public final class RawParameters extends RecBase implements Comparable<RawParameters> {

    /** The table name. */
    public static final String TABLE_NAME = "parameters";

    /** A field name. */
    private static final String FLD_PGM_NAME = "pgm_name";

    /** A field name. */
    private static final String FLD_PARM1 = "parm1";

    /** A field name. */
    private static final String FLD_PARM2 = "parm2";

    /** A field name. */
    private static final String FLD_PARM3 = "parm3";

    /** A field name. */
    private static final String FLD_PARM4 = "parm4";

    /** A field name. */
    private static final String FLD_PARM5 = "parm5";

    /** A field name. */
    private static final String FLD_PARM6 = "parm6";

    /** A field name. */
    private static final String FLD_PARM7 = "parm7";

    /** A field name. */
    private static final String FLD_PARM8 = "parm8";

    /** A field name. */
    private static final String FLD_PARM9 = "parm9";

    /** A field name. */
    private static final String FLD_PARM10 = "parm10";

    /** The 'pgm_name' field value. */
    public String pgmName;

    /** The 'parm1' field value. */
    public String parm1;

    /** The 'parm2' field value. */
    public String parm2;

    /** The 'parm3' field value. */
    public String parm3;

    /** The 'parm4' field value. */
    public String parm4;

    /** The 'parm5' field value. */
    public String parm5;

    /** The 'parm6' field value. */
    public String parm6;

    /** The 'parm7' field value. */
    public String parm7;

    /** The 'parm8' field value. */
    public String parm8;

    /** The 'parm9' field value. */
    public String parm9;

    /** The 'parm10' field value. */
    public LocalDate parm10;

    /**
     * Constructs a new {@code RawParameters}.
     */
    private RawParameters() {

        super();
    }

    /**
     * Constructs a new {@code RawParameters}.
     *
     * @param thePgmName the program name
     * @param theParm1   the value for parameter 1
     * @param theParm2   the value for parameter 2
     * @param theParm3   the value for parameter 3
     * @param theParm4   the value for parameter 4
     * @param theParm5   the value for parameter 5
     * @param theParm6   the value for parameter 6
     * @param theParm7   the value for parameter 7
     * @param theParm8   the value for parameter 8
     * @param theParm9   the value for parameter 9
     * @param theParm10  the value for parameter 10
     */
    public RawParameters(final String thePgmName, final String theParm1, final String theParm2,
                         final String theParm3, final String theParm4, final String theParm5, final String theParm6,
                         final String theParm7, final String theParm8, final String theParm9,
                         final LocalDate theParm10) {

        super();

        this.pgmName = thePgmName;
        this.parm1 = theParm1;
        this.parm2 = theParm2;
        this.parm3 = theParm3;
        this.parm4 = theParm4;
        this.parm5 = theParm5;
        this.parm6 = theParm6;
        this.parm7 = theParm7;
        this.parm8 = theParm8;
        this.parm9 = theParm9;
        this.parm10 = theParm10;
    }

    /**
     * Extracts a "parameters" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawParameters fromResultSet(final ResultSet rs) throws SQLException {

        final RawParameters result = new RawParameters();

        result.pgmName = getStringField(rs, FLD_PGM_NAME);
        result.parm1 = getStringField(rs, FLD_PARM1);
        result.parm2 = getStringField(rs, FLD_PARM2);
        result.parm3 = getStringField(rs, FLD_PARM3);
        result.parm4 = getStringField(rs, FLD_PARM4);
        result.parm5 = getStringField(rs, FLD_PARM5);
        result.parm6 = getStringField(rs, FLD_PARM6);
        result.parm7 = getStringField(rs, FLD_PARM7);
        result.parm8 = getStringField(rs, FLD_PARM8);
        result.parm9 = getStringField(rs, FLD_PARM9);
        result.parm10 = getDateField(rs, FLD_PARM10);

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
    public int compareTo(final RawParameters o) {

        return compareAllowingNull(this.pgmName, o.pgmName);
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

        appendField(htm, FLD_PGM_NAME, this.pgmName);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM1, this.parm1);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM2, this.parm2);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM3, this.parm3);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM4, this.parm4);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM5, this.parm5);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM6, this.parm6);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM7, this.parm7);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM8, this.parm8);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM9, this.parm9);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARM10, this.parm10);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.pgmName)
                + Objects.hashCode(this.parm1)
                + Objects.hashCode(this.parm2)
                + Objects.hashCode(this.parm3)
                + Objects.hashCode(this.parm4)
                + Objects.hashCode(this.parm5)
                + Objects.hashCode(this.parm6)
                + Objects.hashCode(this.parm7)
                + Objects.hashCode(this.parm8)
                + Objects.hashCode(this.parm9)
                + Objects.hashCode(this.parm10);
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
        } else if (obj instanceof final RawParameters rec) {
            equal = Objects.equals(this.pgmName, rec.pgmName)
                    && Objects.equals(this.parm1, rec.parm1)
                    && Objects.equals(this.parm2, rec.parm2)
                    && Objects.equals(this.parm3, rec.parm3)
                    && Objects.equals(this.parm4, rec.parm4)
                    && Objects.equals(this.parm5, rec.parm5)
                    && Objects.equals(this.parm6, rec.parm6)
                    && Objects.equals(this.parm7, rec.parm7)
                    && Objects.equals(this.parm8, rec.parm8)
                    && Objects.equals(this.parm9, rec.parm9)
                    && Objects.equals(this.parm10, rec.parm10);
        } else {
            equal = false;
        }

        return equal;
    }
}
