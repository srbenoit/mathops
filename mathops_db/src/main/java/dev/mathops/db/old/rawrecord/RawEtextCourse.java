package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "etext_course" record.
 */
public final class RawEtextCourse extends RecBase implements Comparable<RawEtextCourse> {

    /** The table name. */
    public static final String TABLE_NAME = "etext_course";

    /** A field name. */
    private static final String FLD_ETEXT_ID = "etext_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** The 'etext_id' field value. */
    public String etextId;

    /** The 'course' field value. */
    public String course;

    /**
     * Constructs a new {@code RawEtextCourse}.
     */
    private RawEtextCourse() {

        super();
    }

    /**
     * Constructs a new {@code RawEtextCourse}.
     *
     * @param theEtextId the e-text ID
     * @param theCourse  the course
     */
    public RawEtextCourse(final String theEtextId, final String theCourse) {

        super();

        this.etextId = theEtextId;
        this.course = theCourse;
    }

    /**
     * Extracts an "etext_course" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawEtextCourse fromResultSet(final ResultSet rs) throws SQLException {

        final RawEtextCourse result = new RawEtextCourse();

        result.etextId = getStringField(rs, FLD_ETEXT_ID);
        result.course = getStringField(rs, FLD_COURSE);

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
    public int compareTo(final RawEtextCourse o) {

        int result = compareAllowingNull(this.etextId, o.etextId);

        if (result == 0) {
            result = compareAllowingNull(this.course, o.course);
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
        appendField(htm, FLD_COURSE, this.course);

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
                + Objects.hashCode(this.course);
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
        } else if (obj instanceof final RawEtextCourse rec) {
            equal = Objects.equals(this.etextId, rec.etextId)
                    && Objects.equals(this.course, rec.course);
        } else {
            equal = false;
        }

        return equal;
    }
}
