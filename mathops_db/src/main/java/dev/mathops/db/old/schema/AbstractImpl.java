package dev.mathops.db.old.schema;

import dev.mathops.db.DbConnection;
import dev.mathops.db.old.IDataDomainObject;
import dev.mathops.db.old.IDataObject;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * The base class for implementations of database objects.
 *
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @param <E> the type of record object
 */
public abstract class AbstractImpl<E> implements IDataObject<E> {

    /** The lowest three-digit number. */
    private static final int MAX_TWO_DIGIT = 99;

    /** The last 2-digit year we interpret as being in the 20th century. */
    private static final int LAST_1900_YEAR = 80;

    /** First year of the 20th century. */
    private static final int CENTURY_20 = 1900;

    /** First year of the 21st century. */
    private static final int CENTURY_21 = 2000;

    /**
     * Constructs a new {@code AbstractImpl}.
     */
    protected AbstractImpl() {

        super();
    }

    /**
     * Extracts a named string value from a result set, trimming off any leading or trailing spaces.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the value to extract
     * @return the trimmed string value, or {@code null} if the value was NULL
     * @throws SQLException if an exception occurs extracting the value
     */
    public static String getString(final ResultSet rs, final String fieldName) throws SQLException {

        String result = null;

        final String str = rs.getString(fieldName);

        if (str != null) {
            final String trimmed = str.trim();

            if (!trimmed.isEmpty()) {
                result = trimmed;
            }
        }

        return result;
    }

    /**
     * Extracts a named integer value from a result set.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the value to extract
     * @return the integer value, or {@code null} if the value was NULL
     * @throws SQLException if an exception occurs extracting the value
     */
    protected static Integer getInteger(final ResultSet rs, final String fieldName) throws SQLException {

        Integer result = null;

        final String test = rs.getString(fieldName);

        if (test != null) {
            final String trimmed = test.trim();

            if (!trimmed.isEmpty()) {
                result = Integer.valueOf(trimmed);
            }
        }

        return result;
    }

    /**
     * Extracts a named float value from a result set.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the value to extract
     * @return the float value, or {@code null} if the value was NULL
     * @throws SQLException if an exception occurs extracting the value
     */
    protected static Double getDouble(final ResultSet rs, final String fieldName) throws SQLException {

        Double result = null;

        final String test = rs.getString(fieldName);

        if (test != null) {
            final String trimmed = test.trim();

            if (!trimmed.isEmpty()) {
                result = Double.valueOf(trimmed);
            }
        }

        return result;
    }

    /**
     * Extracts a named date value from a result set.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the value to extract
     * @return the {@code IDate} value, or {@code null} if the value was NULL
     * @throws SQLException if an exception occurs extracting the value
     */
    protected static LocalDate getDate(final ResultSet rs, final String fieldName) throws SQLException {

        LocalDate result = null;

        final Date date = rs.getDate(fieldName);

        if (date != null) {
            result = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        }

        return result;
    }

    /**
     * Extracts an integer field from the database, and interprets it as a year. If the value is less than 100, then all
     * values between 0 and 89 are interpreted as '2000' through '2089', and values between 90 and 99 are interpreted as
     * '1990' through '1999'.
     *
     * @param rs        the {@code ResultSet} from which to extract the value
     * @param fieldName the name of the field to retrieve
     * @return the resulting year value
     * @throws SQLException if there is an error retrieving the field value
     */
    public static Integer getYear(final ResultSet rs, final String fieldName) throws SQLException {

        Integer year = getInteger(rs, fieldName);

        if (year != null && year.intValue() <= MAX_TWO_DIGIT) {

            if (year.intValue() >= LAST_1900_YEAR) {

                // We treat '80' through '99' as 1980 through 1999
                year = Integer.valueOf(year.intValue() + CENTURY_20);
            } else {

                // We treat '00' through '79' as 2000 through 2079
                year = Integer.valueOf(year.intValue() + CENTURY_21);
            }
        }

        return year;
    }

    // TODO: Add a "getTermName()" that does the conversion to ETermName

    /**
     * Builds a data object of the class appropriate to this implementation from a {@code ResultSet}.
     *
     * @param conn the database connection, in case the construction of records from the result set requires further
     *             queries
     * @param rs   the result set from which to construct the object
     * @return the data object (null to skip record)
     * @throws SQLException if there was an error retrieving a field
     */
    protected abstract E constructFromResultSet(DbConnection conn, ResultSet rs) throws SQLException;

    /**
     * Gets the primary table name for the implementation.
     *
     * @return the table name
     */
    protected abstract String getTableName();

    /**
     * Executes an SQL query and builds a list of result records.
     *
     * @param conn the database connection
     * @param sql  the SQL to execute
     * @return the results of the query
     * @throws SQLException if there is an error performing the query
     */
    protected final List<E> executeSimpleQuery(final DbConnection conn, final String sql) throws SQLException {

        try (final Statement stmt = conn.createStatement()) {
            final List<E> results = new ArrayList<>(10);

            try (final ResultSet rset = stmt.executeQuery(sql)) {
                while (rset.next()) {
                    final E record = constructFromResultSet(conn, rset);
                    if (record != null) {
                        results.add(record);
                    }
                }
            }

            return results;
        }
    }

    /**
     * Executes an SQL query that should return a single integer (such as a COUNT or MAX function).
     *
     * @param conn the database connection, checked out to this thread
     * @param sql  the SQL to execute
     * @return the result of the query, or {@code null} if the query returned no record
     * @throws SQLException if there is an error executing the query
     */
    private static Long executeSimpleIntQuery(final DbConnection conn, final String sql) throws SQLException {

        Long result = null;

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                final long value = rs.getLong(1);
                if (!rs.wasNull()) {
                    result = Long.valueOf(value);
                }
            }
        }

        return result;
    }

    /**
     * Returns the total number of objects in the database.
     *
     * @param conn the database connection, checked out to this thread
     * @return the number of objects in the database
     * @throws SQLException if there is an error performing the query
     */
    protected final int defaultCount(final DbConnection conn) throws SQLException {

        final String sql = "SELECT COUNT(*) FROM " + getTableName();

        return executeSimpleIntQuery(conn, sql).intValue();
    }

    /**
     * Queries for the all student course records.
     *
     * @param conn the database connection, checked out to this thread
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error performing the query
     */
    protected final List<E> defaultQueryAll(final DbConnection conn) throws SQLException {

        return executeSimpleQuery(conn, "SELECT * FROM " + getTableName());
    }

    /**
     * Compares this object with the specified object for order.
     * <p>
     * Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     *
     * @param obj the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public final int compareTo(final IDataDomainObject obj) {

        return getClass().getSimpleName().compareTo(obj.getClass().getSimpleName());
    }
}
