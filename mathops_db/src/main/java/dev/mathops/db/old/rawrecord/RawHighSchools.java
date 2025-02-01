package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "high_schools" record.
 */
public final class RawHighSchools extends RecBase implements Comparable<RawHighSchools> {

    /** The table name. */
    public static final String TABLE_NAME = "high_schools";

    /** A field name. */
    private static final String FLD_HS_CODE = "hs_code";

    /** A field name. */
    private static final String FLD_HS_NAME = "hs_name";

    /** A field name. */
    private static final String FLD_ADDRES_1 = "addres_1";

    /** A field name. */
    private static final String FLD_CITY = "city";

    /** A field name. */
    private static final String FLD_STATE = "state";

    /** A field name. */
    private static final String FLD_ZIP_CODE = "zip_code";

    /** The 'hs_code' field value. */
    public String hsCode;

    /** The 'hs_name' field value. */
    public String hsName;

    /** The 'addres_1' field value. */
    public String addres1;

    /** The 'city' field value. */
    public String city;

    /** The 'state' field value. */
    public String state;

    /** The 'zip_code' field value. */
    public String zipCode;

    /**
     * Constructs a new {@code RawHighSchools}.
     */
    private RawHighSchools() {

        super();
    }

    /**
     * Constructs a new {@code RawHighSchools}.
     *
     * @param theHsCode  the high school code (CEEB)
     * @param theHsName  the high school name
     * @param theAddres1 the first line of the school's address
     * @param theCity    the city
     * @param theState   the state
     * @param theZipCode the zip code
     */
    public RawHighSchools(final String theHsCode, final String theHsName, final String theAddres1,
                          final String theCity, final String theState, final String theZipCode) {

        super();

        this.hsCode = theHsCode;
        this.hsName = theHsName;
        this.addres1 = theAddres1;
        this.city = theCity;
        this.state = theState;
        this.zipCode = theZipCode;
    }

    /**
     * Extracts a "high_schools" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawHighSchools fromResultSet(final ResultSet rs) throws SQLException {

        final RawHighSchools result = new RawHighSchools();

        result.hsCode = getStringField(rs, FLD_HS_CODE);
        result.hsName = getStringField(rs, FLD_HS_NAME);
        result.addres1 = getStringField(rs, FLD_ADDRES_1);
        result.city = getStringField(rs, FLD_CITY);
        result.state = getStringField(rs, FLD_STATE);
        result.zipCode = getStringField(rs, FLD_ZIP_CODE);

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
    public int compareTo(final RawHighSchools o) {

        int result = compareAllowingNull(this.hsCode, o.hsCode);

        if (result == 0) {
            result = compareAllowingNull(this.hsName, o.hsName);
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

        appendField(htm, FLD_HS_CODE, this.hsCode);
        htm.add(DIVIDER);
        appendField(htm, FLD_HS_NAME, this.hsName);
        htm.add(DIVIDER);
        appendField(htm, FLD_ADDRES_1, this.addres1);
        htm.add(DIVIDER);
        appendField(htm, FLD_CITY, this.city);
        htm.add(DIVIDER);
        appendField(htm, FLD_STATE, this.state);
        htm.add(DIVIDER);
        appendField(htm, FLD_ZIP_CODE, this.zipCode);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.hsCode)
                + Objects.hashCode(this.hsName)
                + Objects.hashCode(this.addres1)
                + Objects.hashCode(this.city)
                + Objects.hashCode(this.state)
                + Objects.hashCode(this.zipCode);
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
        } else if (obj instanceof final RawHighSchools rec) {
            equal = Objects.equals(this.hsCode, rec.hsCode)
                    && Objects.equals(this.hsName, rec.hsName)
                    && Objects.equals(this.addres1, rec.addres1)
                    && Objects.equals(this.city, rec.city)
                    && Objects.equals(this.state, rec.state)
                    && Objects.equals(this.zipCode, rec.zipCode);
        } else {
            equal = false;
        }

        return equal;
    }
}
