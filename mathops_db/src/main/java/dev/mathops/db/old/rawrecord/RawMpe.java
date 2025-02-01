package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A raw "mpe" record.
 */
public final class RawMpe extends RecBase implements Comparable<RawMpe> {

    /** The table name. */
    public static final String TABLE_NAME = "mpe";

    /** A field name. */
    private static final String FLD_VERSION = "version";

    /** A field name. */
    private static final String FLD_MAX_ONLINE_ATMPTS = "max_online_atmpts";

    /** A field name. */
    private static final String FLD_MAX_PROCTORED_ATMPTS = "max_proctored_atmpts";

    /** The 'version' field value. */
    public String version;

    /** The 'max_online_atmpts' field value. */
    public Integer maxOnlineAtmpts;

    /** The 'max_proctored_atmpts' field value. */
    public Integer maxProctoredAtmpts;

    /**
     * Constructs a new {@code RawMpe}.
     */
    private RawMpe() {

        super();
    }

    /**
     * Constructs a new {@code RawMpe}.
     *
     * @param theVersion            the version
     * @param theMaxOnlineAtmpts    the maximum number of online attempts
     * @param theMaxProctoredAtmpts the maximum number of proctored attempts
     */
    public RawMpe(final String theVersion, final Integer theMaxOnlineAtmpts, final Integer theMaxProctoredAtmpts) {

        super();

        this.version = theVersion;
        this.maxOnlineAtmpts = theMaxOnlineAtmpts;
        this.maxProctoredAtmpts = theMaxProctoredAtmpts;
    }

    /**
     * Extracts a "parameters" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawMpe fromResultSet(final ResultSet rs) throws SQLException {

        final RawMpe result = new RawMpe();

        result.version = getStringField(rs, FLD_VERSION);
        result.maxOnlineAtmpts = getIntegerField(rs, FLD_MAX_ONLINE_ATMPTS);
        result.maxProctoredAtmpts = getIntegerField(rs, FLD_MAX_PROCTORED_ATMPTS);

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
    public int compareTo(final RawMpe o) {

        return compareAllowingNull(this.version, o.version);
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

        appendField(htm, FLD_VERSION, this.version);
        htm.add(DIVIDER);
        appendField(htm, FLD_MAX_ONLINE_ATMPTS, this.maxOnlineAtmpts);
        htm.add(DIVIDER);
        appendField(htm, FLD_MAX_PROCTORED_ATMPTS, this.maxProctoredAtmpts);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.version)
                + Objects.hashCode(this.maxOnlineAtmpts)
                + Objects.hashCode(this.maxProctoredAtmpts);
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
        } else if (obj instanceof final RawMpe rec) {
            equal = Objects.equals(this.version, rec.version)
                    && Objects.equals(this.maxOnlineAtmpts, rec.maxOnlineAtmpts)
                    && Objects.equals(this.maxProctoredAtmpts, rec.maxProctoredAtmpts);
        } else {
            equal = false;
        }

        return equal;
    }
}
