package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "testing_centers" record.
 */
public final class RawTestingCenter extends RecBase implements Comparable<RawTestingCenter> {

    /** The table name. */
    public static final String TABLE_NAME = "testing_centers";

    /** A field name. */
    private static final String FLD_TESTING_CENTER_ID = "testing_center_id";

    /** A field name. */
    private static final String FLD_TC_NAME = "tc_name";

    /** A field name. */
    private static final String FLD_ADDRES_1 = "addres_1";

    /** A field name. */
    private static final String FLD_ADDRES_2 = "addres_2";

    /** A field name. */
    private static final String FLD_ADDRES_3 = "addres_3";

    /** A field name. */
    private static final String FLD_CITY = "city";

    /** A field name. */
    private static final String FLD_STATE = "state";

    /** A field name. */
    private static final String FLD_ZIP_CODE = "zip_code";

    /** A field name. */
    private static final String FLD_ACTIVE = "active";

    /** A field name. */
    private static final String FLD_DTIME_CREATED = "dtime_created";

    /** A field name. */
    private static final String FLD_DTIME_APPROVED = "dtime_approved";

    /** A field name. */
    private static final String FLD_DTIME_DENIED = "dtime_denied";

    /** A field name. */
    private static final String FLD_DTIME_REVOKED = "dtime_revoked";

    /** A field name. */
    private static final String FLD_IS_REMOTE = "is_remote";

    /** A field name. */
    private static final String FLD_IS_PROCTORED = "is_proctored";

    /** The 'testing_center_id' field value. */
    public String testingCenterId;

    /** The 'tc_name' field value. */
    public String tcName;

    /** The 'addres_1' field value. */
    public String addres1;

    /** The 'addres_2' field value. */
    public String addres2;

    /** The 'addres_3' field value. */
    public String addres3;

    /** The 'city' field value. */
    public String city;

    /** The 'state' field value. */
    public String state;

    /** The 'zip_code' field value. */
    public String zipCode;

    /** The 'active' field value. */
    public String active;

    /** The 'dtime_created' field value. */
    public LocalDateTime dtimeCreated;

    /** The 'dtime_approved' field value. */
    public LocalDateTime dtimeApproved;

    /** The 'dtime_denied' field value. */
    public LocalDateTime dtimeDenied;

    /** The 'dtime_revoked' field value. */
    public LocalDateTime dtimeRevoked;

    /** The 'is_remote' field value. */
    public String isRemote;

    /** The 'is_proctored' field value. */
    public String isProctored;

    /**
     * Constructs a new {@code RawTestingCenter}.
     */
    private RawTestingCenter() {

        super();
    }

    /**
     * Constructs a new {@code RawTestingCenter}.
     *
     * @param theTestingCenterId the testing center ID
     * @param theTcName          the testing center name
     * @param theAddres1         the first line of address
     * @param theAddres2         the second line of address
     * @param theAddres3         the third line of address
     * @param theCity            the city
     * @param theState           the state
     * @param theZipCode         the zip code
     * @param theActive          "Y" if active, "N" if not
     * @param theDtimeCreated    the date/time the record was created
     * @param theDtimeApproved   the date/time the record was approved
     * @param theDtimeDenied     the date/time the record was denied
     * @param theDtimeRevoked    the date/time the record was revoked
     * @param theIsRemote        "Y" if remote, "N" if not
     * @param theIsProctored     "Y" if proctored, "N" if not
     */
    public RawTestingCenter(final String theTestingCenterId, final String theTcName,
                            final String theAddres1, final String theAddres2, final String theAddres3,
                            final String theCity, final String theState, final String theZipCode,
                            final String theActive, final LocalDateTime theDtimeCreated,
                            final LocalDateTime theDtimeApproved, final LocalDateTime theDtimeDenied,
                            final LocalDateTime theDtimeRevoked, final String theIsRemote,
                            final String theIsProctored) {

        super();

        this.testingCenterId = theTestingCenterId;
        this.tcName = theTcName;
        this.addres1 = theAddres1;
        this.addres2 = theAddres2;
        this.addres3 = theAddres3;
        this.city = theCity;
        this.state = theState;
        this.zipCode = theZipCode;
        this.active = theActive;
        this.dtimeCreated = theDtimeCreated;
        this.dtimeApproved = theDtimeApproved;
        this.dtimeDenied = theDtimeDenied;
        this.dtimeRevoked = theDtimeRevoked;
        this.isRemote = theIsRemote;
        this.isProctored = theIsProctored;
    }

    /**
     * Extracts a "testing_centers" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawTestingCenter fromResultSet(final ResultSet rs) throws SQLException {

        final RawTestingCenter result = new RawTestingCenter();

        result.testingCenterId = getStringField(rs, FLD_TESTING_CENTER_ID);
        result.tcName = getStringField(rs, FLD_TC_NAME);
        result.addres1 = getStringField(rs, FLD_ADDRES_1);
        result.addres2 = getStringField(rs, FLD_ADDRES_2);
        result.addres3 = getStringField(rs, FLD_ADDRES_3);
        result.city = getStringField(rs, FLD_CITY);
        result.state = getStringField(rs, FLD_STATE);
        result.zipCode = getStringField(rs, FLD_ZIP_CODE);
        result.active = getStringField(rs, FLD_ACTIVE);
        result.dtimeCreated = getDateTimeField(rs, FLD_DTIME_CREATED);
        result.dtimeApproved = getDateTimeField(rs, FLD_DTIME_APPROVED);
        result.dtimeDenied = getDateTimeField(rs, FLD_DTIME_DENIED);
        result.dtimeRevoked = getDateTimeField(rs, FLD_DTIME_REVOKED);
        result.isRemote = getStringField(rs, FLD_IS_REMOTE);
        result.isProctored = getStringField(rs, FLD_IS_PROCTORED);

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
    public int compareTo(final RawTestingCenter o) {

        int result = compareAllowingNull(this.testingCenterId, o.testingCenterId);

        if (result == 0) {
            result = compareAllowingNull(this.tcName, o.tcName);
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

        appendField(htm, FLD_TESTING_CENTER_ID, this.testingCenterId);
        htm.add(DIVIDER);
        appendField(htm, FLD_TC_NAME, this.tcName);
        htm.add(DIVIDER);
        appendField(htm, FLD_ADDRES_1, this.addres1);
        htm.add(DIVIDER);
        appendField(htm, FLD_ADDRES_2, this.addres2);
        htm.add(DIVIDER);
        appendField(htm, FLD_ADDRES_3, this.addres3);
        htm.add(DIVIDER);
        appendField(htm, FLD_CITY, this.city);
        htm.add(DIVIDER);
        appendField(htm, FLD_STATE, this.state);
        htm.add(DIVIDER);
        appendField(htm, FLD_ZIP_CODE, this.zipCode);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTIVE, this.active);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_CREATED, this.dtimeCreated);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_APPROVED, this.dtimeApproved);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_DENIED, this.dtimeDenied);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_REVOKED, this.dtimeRevoked);
        htm.add(DIVIDER);
        appendField(htm, FLD_IS_REMOTE, this.isRemote);
        htm.add(DIVIDER);
        appendField(htm, FLD_IS_PROCTORED, this.isProctored);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.testingCenterId)
                + Objects.hashCode(this.tcName)
                + Objects.hashCode(this.addres1)
                + Objects.hashCode(this.addres2)
                + Objects.hashCode(this.addres2)
                + Objects.hashCode(this.city)
                + Objects.hashCode(this.state)
                + Objects.hashCode(this.zipCode)
                + Objects.hashCode(this.active)
                + Objects.hashCode(this.dtimeCreated)
                + Objects.hashCode(this.dtimeApproved)
                + Objects.hashCode(this.dtimeDenied)
                + Objects.hashCode(this.dtimeRevoked)
                + Objects.hashCode(this.isRemote)
                + Objects.hashCode(this.isProctored);
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
        } else if (obj instanceof final RawTestingCenter rec) {
            equal = Objects.equals(this.testingCenterId, rec.testingCenterId)
                    && Objects.equals(this.tcName, rec.tcName)
                    && Objects.equals(this.addres1, rec.addres1)
                    && Objects.equals(this.addres2, rec.addres2)
                    && Objects.equals(this.addres3, rec.addres3)
                    && Objects.equals(this.city, rec.city)
                    && Objects.equals(this.state, rec.state)
                    && Objects.equals(this.zipCode, rec.zipCode)
                    && Objects.equals(this.active, rec.active)
                    && Objects.equals(this.dtimeCreated, rec.dtimeCreated)
                    && Objects.equals(this.dtimeApproved, rec.dtimeApproved)
                    && Objects.equals(this.dtimeDenied, rec.dtimeDenied)
                    && Objects.equals(this.dtimeRevoked, rec.dtimeRevoked)
                    && Objects.equals(this.isRemote, rec.isRemote)
                    && Objects.equals(this.isProctored, rec.isProctored);
        } else {
            equal = false;
        }

        return equal;
    }
}
