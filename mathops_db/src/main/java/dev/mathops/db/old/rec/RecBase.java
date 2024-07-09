package dev.mathops.db.old.rec;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A base class for records with utility methods for extracting fields from result sets.
 */
public abstract class RecBase {

    /** The field divider for text representations. */
    public static final String DIVIDER = "\u001F";

    /**
     * True if this is a synthetic record (one that does not exist in the database or is not being created to insert
     * into the database).
     */
    public boolean synthetic;

    /**
     * Constructs a new {@code RecBase}.
     */
    protected RecBase() {

        this.synthetic = false;
    }

    /**
     * Retrieves a String field value from a result set, returning null if the result set indicates a null value was
     * present. The string is trimmed to remove leading or trailing whitespace.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    public static String getStringField(final ResultSet rs, final String name) throws SQLException {

        final String tmp = rs.getString(name);

        return tmp == null ? null : tmp.trim();
    }

    /**
     * Retrieves a Long field value from a result set, returning null if the result set indicates a null value was
     * present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    protected static Long getLongField(final ResultSet rs, final String name) throws SQLException {

        final long tmp = rs.getLong(name);

        return rs.wasNull() ? null : Long.valueOf(tmp);
    }

    /**
     * Retrieves an Integer field value from a result set, returning null if the result set indicates a null value was
     * present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    public static Integer getIntegerField(final ResultSet rs, final String name) throws SQLException {

        final int tmp = rs.getInt(name);

        return rs.wasNull() ? null : Integer.valueOf(tmp);
    }

    /**
     * Retrieves a Float field value from a result set, returning null if the result set indicates a null value was
     * present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    public static Float getFloatField(final ResultSet rs, final String name) throws SQLException {

        final float tmp = rs.getFloat(name);

        return rs.wasNull() ? null : Float.valueOf(tmp);
    }

    /**
     * Retrieves a LocalDate field value from a result set, returning null if the result set indicates a null value was
     * present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    public static LocalDate getDateField(final ResultSet rs, final String name) throws SQLException {

        final Date tmp = rs.getDate(name);

        return tmp == null ? null : tmp.toLocalDate();
    }

    /**
     * Retrieves a LocalDateTime field value from a result set, returning null if the result set indicates a null value
     * was present.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    protected static LocalDateTime getDateTimeField(final ResultSet rs, final String name) throws SQLException {

        final Timestamp tmp = rs.getTimestamp(name);

        return tmp == null ? null : tmp.toLocalDateTime();
    }

    /**
     * Retrieves a TermKey field value from a result set, returning null if the result set indicates a null value was
     * present, or the value found could not be parsed.
     *
     * @param rs   the result set
     * @param name the field name
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    protected static TermKey getShortTermStringField(final ResultSet rs, final String name) throws SQLException {

        TermKey result = null;

        try {
            final String str = rs.getString(name);
            if (str != null) {
                final String trim = str.trim();
                if (!trim.isEmpty()) {
                    result = new TermKey(trim);
                }
            }
        } catch (final IllegalArgumentException ex) {
            Log.warning(ex);
        }

        return result;
    }

    /**
     * Retrieves a TermKey field value from two specified fields in a result set, returning null if the result set
     * indicates a null value was present for either.
     *
     * @param rs         the result set
     * @param termName   the field name from which to query the 2-character term name
     * @param termYrName the field name from which to query the 2-digit term year
     * @return the value
     * @throws SQLException if there is an error retrieving the value
     */
    protected static TermKey getTermAndYear(final ResultSet rs, final String termName,
                                            final String termYrName) throws SQLException {

        final TermKey result;

        final String term = rs.getString(termName);
        final int termYr = rs.getInt(termYrName);

        if (term == null || rs.wasNull()) {
            result = null;
        } else {
            final ETermName parsedTerm = ETermName.forName(term);

            if (termYr > 80) {
                result = new TermKey(parsedTerm, 1900 + termYr);
            } else {
                result = new TermKey(parsedTerm, 2000 + termYr);
            }
        }

        return result;
    }

    /**
     * Performs an order comparison between two objects, either of which could be null.
     *
     * @param <T> the object type
     * @param o1  the first object
     * @param o2  the second object
     * @return a negative integer, zero, or a positive integer as the first object is less than, equal to, or greater
     *         than the second object
     */
    public static <T extends Comparable<T>> int compareAllowingNull(final T o1, final T o2) {

        final int result;

        if (o1 == null) {
            if (o2 == null) {
                result = 0;
            } else {
                // Sort nulls before anything
                result = -1;
            }
        } else if (o2 == null) {
            result = 1;
        } else {
            result = o1.compareTo(o2);
        }

        return result;
    }

    /**
     * Appends a value to a {@code HtmlBuilder}.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param name    the field name
     * @param value   the value to append (if null, nothing is appended)
     */
    protected static void appendField(final HtmlBuilder builder, final String name, final Object value) {

        if (value != null) {
            builder.add(name, "=", value);
        }
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    public abstract String toString();

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public abstract int hashCode();

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public abstract boolean equals(Object obj);
}
