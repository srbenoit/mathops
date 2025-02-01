package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * An immutable record of a zip code.
 */
public final class RawZipCode extends RecBase implements Comparable<RawZipCode> {

    /** A field name. */
    private static final String FLD_ZIP_CODE = "zip_code";

    /** A field name. */
    private static final String FLD_CITY = "city";

    /** A field name. */
    private static final String FLD_STATE = "state";

    /** The 'zip_code' field value. */
    public String zipCode;

    /** The 'city' field value. */
    public String city;

    /** The 'state' field value. */
    public String state;

    /**
     * Constructs a new {@code RawZipCode}.
     */
    private RawZipCode() {

        super();
    }

    /**
     * Constructs a new {@code RawZipCode}.
     *
     * @param theZipCode the zip code
     * @param theCity    the city
     * @param theState   the state
     */
    public RawZipCode(final String theZipCode, final String theCity, final String theState) {

        super();

        this.zipCode = theZipCode;
        this.city = theCity;
        this.state = theState;
    }

    /**
     * Extracts a "which_db" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawZipCode fromResultSet(final ResultSet rs) throws SQLException {

        final RawZipCode result = new RawZipCode();

        result.zipCode = getStringField(rs, FLD_ZIP_CODE);
        result.city = getStringField(rs, FLD_CITY);
        result.state = getStringField(rs, FLD_STATE);

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
    public int compareTo(final RawZipCode o) {

        return compareAllowingNull(this.zipCode, o.zipCode);
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

        appendField(htm, FLD_ZIP_CODE, this.zipCode);
        htm.add(DIVIDER);
        appendField(htm, FLD_CITY, this.city);
        htm.add(DIVIDER);
        appendField(htm, FLD_STATE, this.state);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.zipCode)
                + Objects.hashCode(this.city)
                + Objects.hashCode(this.state);
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
        } else if (obj instanceof final RawZipCode rec) {
            equal = Objects.equals(this.zipCode, rec.zipCode)
                    && Objects.equals(this.city, rec.city)
                    && Objects.equals(this.state, rec.state);
        } else {
            equal = false;
        }

        return equal;
    }
}
