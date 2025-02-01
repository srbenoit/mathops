package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * An immutable record of permission granted to a named database user.
 */
public final class RawWhichDb extends RecBase implements Comparable<RawWhichDb> {

    /** A field name. */
    private static final String FLD_DESCR = "descr";

    /** The 'descr' field value. */
    public String descr;

    /**
     * Constructs a new {@code RawWhichDb}.
     */
    private RawWhichDb() {

        super();
    }

    /**
     * Constructs a new {@code RawWhichDb}.
     *
     * @param theDescr the description
     */
    public RawWhichDb(final String theDescr) {

        super();

        this.descr = theDescr;
    }

    /**
     * Extracts a "which_db" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawWhichDb fromResultSet(final ResultSet rs) throws SQLException {

        final RawWhichDb result = new RawWhichDb();

        result.descr = getStringField(rs, FLD_DESCR);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to which to compare
     * @return a negative value, 0, or a positive value as this object is less than, equal to, or greater than
     *         {@code o}.
     */
    @Override
    public int compareTo(final RawWhichDb o) {

        return compareAllowingNull(this.descr, o.descr);
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

        appendField(htm, FLD_DESCR, this.descr);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.descr);
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
        } else if (obj instanceof final RawWhichDb rec) {
            equal = Objects.equals(this.descr, rec.descr);
        } else {
            equal = false;
        }

        return equal;
    }
}
