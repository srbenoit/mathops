package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "prereq" record.
 */
public final class RawPrereq extends RawTermRecordBase implements Comparable<RawPrereq> {

    /** The table name. */
    public static final String TABLE_NAME = "prereq";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_PREREQUISITE = "prerequisite";

    /** The 'course' field value. */
    public String course;

    /** The 'prerequisite' field value. */
    public String prerequisite;

    /**
     * Constructs a new {@code RawPrereq}.
     */
    private RawPrereq() {

        super();
    }

    /**
     * Constructs a new {@code RawPrereq}.
     *
     * @param theTermKey      the term key
     * @param theCourse       the course
     * @param thePrerequisite the prerequisite
     */
    public RawPrereq(final TermKey theTermKey, final String theCourse,
                     final String thePrerequisite) {

        super(theTermKey);

        this.course = theCourse;
        this.prerequisite = thePrerequisite;
    }

    /**
     * Extracts an "prereq" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPrereq fromResultSet(final ResultSet rs) throws SQLException {

        final RawPrereq result = new RawPrereq();

        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        result.course = getStringField(rs, FLD_COURSE);
        result.prerequisite = getStringField(rs, FLD_PREREQUISITE);

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
    public int compareTo(final RawPrereq o) {

        int result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.course, o.course);

            if (result == 0) {
                result = compareAllowingNull(this.course, o.course);
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

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_PREREQUISITE, this.prerequisite);

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
                + Objects.hashCode(this.prerequisite);
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
        } else if (obj instanceof final RawPrereq rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.prerequisite, rec.prerequisite);
        } else {
            equal = false;
        }

        return equal;
    }
}
