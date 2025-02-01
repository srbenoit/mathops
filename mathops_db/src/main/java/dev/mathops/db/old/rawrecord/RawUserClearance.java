package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * An immutable record of permission granted to a named database user.
 */
public final class RawUserClearance extends RecBase implements Comparable<RawUserClearance> {

    /** The table name. */
    public static final String TABLE_NAME = "user_clearance";

    /** A field name. */
    private static final String FLD_LOGIN = "login";

    /** A field name. */
    private static final String FLD_CLEAR_FUNCTION = "clear_function";

    /** A field name. */
    private static final String FLD_CLEAR_TYPE = "clear_type";

    /** A field name. */
    private static final String FLD_CLEAR_PASSWD = "clear_passwd";

    /** The 'login' field value. */
    public String login;

    /** The 'clear_function' field value. */
    public String clearFunction;

    /** The 'clear_type' field value. */
    public Integer clearType;

    /** The 'clear_passwd' field value. */
    public String clearPasswd;

    /**
     * Constructs a new {@code RawUserClearance}.
     */
    private RawUserClearance() {

        super();
    }

    /**
     * Constructs a new {@code RawUserClearance}.
     *
     * @param theUsername      the login
     * @param theClearFunction the function
     * @param theClearType     the clearance type
     * @param theClearPasswd   the password
     */
    public RawUserClearance(final String theUsername, final String theClearFunction,
                            final Integer theClearType, final String theClearPasswd) {

        super();

        this.login = theUsername;
        this.clearFunction = theClearFunction;
        this.clearType = theClearType;
        this.clearPasswd = theClearPasswd;
    }

    /**
     * Extracts a "user_clearance" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawUserClearance fromResultSet(final ResultSet rs) throws SQLException {

        final RawUserClearance result = new RawUserClearance();

        result.login = getStringField(rs, FLD_LOGIN);
        result.clearFunction = getStringField(rs, FLD_CLEAR_FUNCTION);
        result.clearType = getIntegerField(rs, FLD_CLEAR_TYPE);
        result.clearPasswd = getStringField(rs, FLD_CLEAR_PASSWD);

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
    public int compareTo(final RawUserClearance o) {

        int result = this.login.compareTo(o.login);

        if (result == 0) {
            result = compareAllowingNull(this.clearFunction, o.clearFunction);
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

        appendField(htm, FLD_LOGIN, this.login);
        htm.add(DIVIDER);
        appendField(htm, FLD_CLEAR_FUNCTION, this.clearFunction);
        htm.add(DIVIDER);
        appendField(htm, FLD_CLEAR_TYPE, this.clearType);
        htm.add(DIVIDER);
        appendField(htm, FLD_CLEAR_PASSWD, this.clearPasswd);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.login)
                + Objects.hashCode(this.clearFunction)
                + Objects.hashCode(this.clearType)
                + Objects.hashCode(this.clearPasswd);
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
        } else if (obj instanceof final RawUserClearance rec) {
            equal = Objects.equals(this.login, rec.login)
                    && Objects.equals(this.clearFunction, rec.clearFunction)
                    && Objects.equals(this.clearType, rec.clearType)
                    && Objects.equals(this.clearPasswd, rec.clearPasswd);
        } else {
            equal = false;
        }

        return equal;
    }
}
