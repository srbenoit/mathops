package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A raw "logins" record.
 */
public final class RawLogins extends RecBase implements Comparable<RawLogins> {

    /** The table name. */
    public static final String TABLE_NAME = "logins";

    /** A field name. */
    private static final String FLD_USER_ID = "user_id";

    /** A field name. */
    private static final String FLD_USER_TYPE = "user_type";

    /** A field name. */
    private static final String FLD_USER_NAME = "user_name";

    /** A field name. */
    private static final String FLD_STORED_KEY = "stored_key";

    /** A field name. */
    private static final String FLD_SERVER_KEY = "server_key";

    /** A field name. */
    private static final String FLD_DTIME_CREATED = "dtime_created";

    /** A field name. */
    private static final String FLD_DTIME_EXPIRES = "dtime_expires";

    /** A field name. */
    private static final String FLD_DTIME_LAST_LOGIN = "dtime_last_login";

    /** A field name. */
    private static final String FLD_FORCE_PW_CHANGE = "force_pw_change";

    /** A field name. */
    private static final String FLD_EMAIL = "email";

    /** A field name. */
    private static final String FLD_SALT = "salt";

    /** A field name. */
    private static final String FLD_NBR_INVALID_ATMPTS = "nbr_invalid_atmpts";

    /** The 'user_id' field value. */
    public String userId;

    /** The 'user_type' field value. */
    public String userType;

    /** The 'user_name' field value. */
    public String userName;

    /** The 'stored_key' field value. */
    public String storedKey;

    /** The 'server_key' field value. */
    public String serverKey;

    /** The 'dtime_created' field value. */
    public LocalDateTime dtimeCreated;

    /** The 'dtime_expires' field value. */
    public LocalDateTime dtimeExpires;

    /** The 'dtime_last_login' field value. */
    public LocalDateTime dtimeLastLogin;

    /** The 'force_pw_change' field value. */
    public String forcePwChange;

    /** The 'email' field value. */
    public String email;

    /** The 'salt' field value. */
    public String salt;

    /** The 'nbr_invalid_atmpts' field value. */
    public Integer nbrInvalidAtmpts;

    /**
     * Constructs a new {@code RawLogins}.
     */
    private RawLogins() {

        super();
    }

    /**
     * Constructs a new {@code RawLogins}.
     *
     * @param theUserId           the user ID
     * @param theUserType         the user type
     * @param theUserName         the username
     * @param theStoredKey        the stored key (Hex)
     * @param theServerKey        the server key (Hex)
     * @param theDtimeCreated     the date/time the record was created
     * @param theDtimeExpires     the date/time the login expires
     * @param theDtimeLastLogin   the last date/time the user logged in
     * @param theForcePwChange    "Y" to force password change
     * @param theEmail            the email address
     * @param theSalt             the salt (Base64)
     * @param theNbrInvalidAtmpts the number of invalid attempts so far
     */
    public RawLogins(final String theUserId, final String theUserType, final String theUserName,
                     final String theStoredKey, final String theServerKey, final LocalDateTime theDtimeCreated,
                     final LocalDateTime theDtimeExpires, final LocalDateTime theDtimeLastLogin,
                     final String theForcePwChange, final String theEmail, final String theSalt,
                     final Integer theNbrInvalidAtmpts) {

        super();

        this.userId = theUserId;
        this.userType = theUserType;
        this.userName = theUserName;
        this.storedKey = theStoredKey;
        this.serverKey = theServerKey;
        this.dtimeCreated = theDtimeCreated;
        this.dtimeExpires = theDtimeExpires;
        this.dtimeLastLogin = theDtimeLastLogin;
        this.forcePwChange = theForcePwChange;
        this.email = theEmail;
        this.salt = theSalt;
        this.nbrInvalidAtmpts = theNbrInvalidAtmpts;
    }

    /**
     * Extracts an "logins" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawLogins fromResultSet(final ResultSet rs) throws SQLException {

        final RawLogins result = new RawLogins();

        result.userId = getStringField(rs, FLD_USER_ID);
        result.userType = getStringField(rs, FLD_USER_TYPE);
        result.userName = getStringField(rs, FLD_USER_NAME);
        result.storedKey = getStringField(rs, FLD_STORED_KEY);
        result.serverKey = getStringField(rs, FLD_SERVER_KEY);
        result.dtimeCreated = getDateTimeField(rs, FLD_DTIME_CREATED);
        result.dtimeExpires = getDateTimeField(rs, FLD_DTIME_EXPIRES);
        result.dtimeLastLogin = getDateTimeField(rs, FLD_DTIME_LAST_LOGIN);
        result.forcePwChange = getStringField(rs, FLD_FORCE_PW_CHANGE);
        result.email = getStringField(rs, FLD_EMAIL);
        result.salt = getStringField(rs, FLD_SALT);
        result.nbrInvalidAtmpts = getIntegerField(rs, FLD_NBR_INVALID_ATMPTS);

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
    public int compareTo(final RawLogins o) {

        return compareAllowingNull(this.userName, o.userName);
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

        appendField(htm, FLD_USER_ID, this.userId);
        htm.add(DIVIDER);
        appendField(htm, FLD_USER_TYPE, this.userType);
        htm.add(DIVIDER);
        appendField(htm, FLD_USER_NAME, this.userName);
        htm.add(DIVIDER);
        appendField(htm, FLD_STORED_KEY, this.storedKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_SERVER_KEY, this.serverKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_CREATED, this.dtimeCreated);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_EXPIRES, this.dtimeExpires);
        htm.add(DIVIDER);
        appendField(htm, FLD_DTIME_LAST_LOGIN, this.dtimeLastLogin);
        htm.add(DIVIDER);
        appendField(htm, FLD_FORCE_PW_CHANGE, this.forcePwChange);
        htm.add(DIVIDER);
        appendField(htm, FLD_EMAIL, this.email);
        htm.add(DIVIDER);
        appendField(htm, FLD_SALT, this.salt);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_INVALID_ATMPTS, this.nbrInvalidAtmpts);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.userId)
                + Objects.hashCode(this.userType)
                + Objects.hashCode(this.userName)
                + Objects.hashCode(this.storedKey)
                + Objects.hashCode(this.serverKey)
                + Objects.hashCode(this.dtimeCreated)
                + Objects.hashCode(this.dtimeExpires)
                + Objects.hashCode(this.dtimeLastLogin)
                + Objects.hashCode(this.forcePwChange)
                + Objects.hashCode(this.email)
                + Objects.hashCode(this.salt)
                + Objects.hashCode(this.nbrInvalidAtmpts);
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
        } else if (obj instanceof final RawLogins rec) {
            equal = Objects.equals(this.userId, rec.userId)
                    && Objects.equals(this.userType, rec.userType)
                    && Objects.equals(this.userName, rec.userName)
                    && Objects.equals(this.storedKey, rec.storedKey)
                    && Objects.equals(this.serverKey, rec.serverKey)
                    && Objects.equals(this.dtimeCreated, rec.dtimeCreated)
                    && Objects.equals(this.dtimeExpires, rec.dtimeExpires)
                    && Objects.equals(this.dtimeLastLogin, rec.dtimeLastLogin)
                    && Objects.equals(this.forcePwChange, rec.forcePwChange)
                    && Objects.equals(this.email, rec.email)
                    && Objects.equals(this.salt, rec.salt)
                    && Objects.equals(this.nbrInvalidAtmpts, rec.nbrInvalidAtmpts);
        } else {
            equal = false;
        }

        return equal;
    }
}
