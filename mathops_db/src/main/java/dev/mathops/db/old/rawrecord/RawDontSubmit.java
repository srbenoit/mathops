package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "dont_submit" record.
 */
public final class RawDontSubmit extends RawTermRecordBase implements Comparable<RawDontSubmit> {

    /** The table name. */
    public static final String TABLE_NAME = "dont_submit";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_SECT = "sect";

    /** The 'course' field value. */
    public String course;

    /** The 'sect' field value. */
    public String sect;

    /**
     * Constructs a new {@code RawDontSubmit}.
     */
    private RawDontSubmit() {

        super();
    }

    /**
     * Constructs a new {@code RawDontSubmit}.
     *
     * @param theTermKey the term key
     * @param theCourse  the course
     * @param theSect    the section number
     */
    public RawDontSubmit(final TermKey theTermKey, final String theCourse, final String theSect) {

        super(theTermKey);

        this.course = theCourse;
        this.sect = theSect;
    }

    /**
     * Extracts an "dont_submit" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawDontSubmit fromResultSet(final ResultSet rs) throws SQLException {

        final RawDontSubmit result = new RawDontSubmit();

        result.course = getStringField(rs, FLD_COURSE);
        result.sect = getStringField(rs, FLD_SECT);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);

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
    public int compareTo(final RawDontSubmit o) {

        int result = compareAllowingNull(this.course, o.course);

        if (result == 0) {
            result = compareAllowingNull(this.sect, o.sect);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT, this.sect);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.termKey)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.sect);
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
        } else if (obj instanceof final RawDontSubmit rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.sect, rec.sect);
        } else {
            equal = false;
        }

        return equal;
    }
}
