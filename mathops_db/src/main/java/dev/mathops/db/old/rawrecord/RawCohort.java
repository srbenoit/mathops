package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "cohort" record.
 */
public final class RawCohort extends RecBase implements Comparable<RawCohort> {

    /** The table name. */
    public static final String TABLE_NAME = "cohort";

    /** A field name. */
    private static final String FLD_COHORT = "cohort";

    /** A field name. */
    private static final String FLD_SIZE = "size";

    /** A field name. */
    private static final String FLD_INSTRUCTOR = "instructor";

    /** The 'cohort' field value. */
    public String cohort;

    /** The 'size' field value. */
    public Integer size;

    /** The 'instructor' field value. */
    public String instructor;

    /**
     * Constructs a new {@code RawCohort}.
     */
    private RawCohort() {

        super();
    }

    /**
     * Constructs a new {@code RawCohort}.
     *
     * @param theCohort     the cohort
     * @param theSize       the size
     * @param theInstructor the instructor
     */
    public RawCohort(final String theCohort, final Integer theSize, final String theInstructor) {

        super();

        this.cohort = theCohort;
        this.size = theSize;
        this.instructor = theInstructor;
    }

    /**
     * Extracts a "course" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCohort fromResultSet(final ResultSet rs) throws SQLException {

        final RawCohort result = new RawCohort();

        result.cohort = getStringField(rs, FLD_COHORT);
        result.size = getIntegerField(rs, FLD_SIZE);
        result.instructor = getStringField(rs, FLD_INSTRUCTOR);

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
    public int compareTo(final RawCohort o) {

        return compareAllowingNull(this.cohort, o.cohort);
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

        appendField(htm, FLD_COHORT, this.cohort);
        htm.add(DIVIDER);
        appendField(htm, FLD_SIZE, this.size);
        htm.add(DIVIDER);
        appendField(htm, FLD_INSTRUCTOR, this.instructor);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.cohort)
                + Objects.hashCode(this.size)
                + Objects.hashCode(this.instructor);
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
        } else if (obj instanceof final RawCohort rec) {
            equal = Objects.equals(this.cohort, rec.cohort)
                    && Objects.equals(this.size, rec.size)
                    && Objects.equals(this.instructor, rec.instructor);
        } else {
            equal = false;
        }

        return equal;
    }
}
