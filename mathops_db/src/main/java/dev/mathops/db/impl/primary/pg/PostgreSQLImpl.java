package dev.mathops.db.impl.primary.pg;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.generalized.Field;
import dev.mathops.db.generalized.Record;
import dev.mathops.db.generalized.Table;
import dev.mathops.db.generalized.connection.AbstractGeneralConnection;
import dev.mathops.db.generalized.connection.JdbcGeneralConnection;
import dev.mathops.db.generalized.constraint.AbstractFieldConstraint;
import dev.mathops.db.generalized.constraint.ByteRangeConstraint;
import dev.mathops.db.generalized.constraint.DoubleRangeConstraint;
import dev.mathops.db.generalized.constraint.EFloatingPointAllow;
import dev.mathops.db.generalized.constraint.FloatRangeConstraint;
import dev.mathops.db.generalized.constraint.IntegerRangeConstraint;
import dev.mathops.db.generalized.constraint.LongRangeConstraint;
import dev.mathops.db.generalized.constraint.StringLengthConstraint;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for the PostgreSQL database system.
 *
 * <p>
 * In the PostgreSQL implementation, a table in the "example" schema will exist in three different actual schemata:
 * "example" (the PRODUCTION table), "example_d" (the DEVELOPMENT table), and "example_t" (the TEST table).  However, if
 * the schema is prefixed by "term_", then there will be the PRODUCTION table, in the named schema, but the DEVELOPMENT
 * table will live in a general "term_d" schema (representing the active term), and the TEST table will live in a
 * general "term_t" schema.
 *
 * <p>
 * PostgreSQL table names match the {@code Table}'s name, and field names match the {@code Field}'s names.
 */
public final class PostgreSQLImpl {

    /** The prefix for a "term" table. */
    private static final String TERM_PREFIX = "term_";

    /** The development schema for "term" tables. */
    private static final String TERM_DEV_SCHEMA = "term_d";

    /** The test schema for "term" tables. */
    private static final String TERM_TEST_SCHEMA = "term_t";

    /** The single instance. */
    public static final PostgreSQLImpl INSTANCE = new PostgreSQLImpl();

    /**
     * Constructs a new {@code PostgreSQLImpl}.
     */
    private PostgreSQLImpl() {

        // No action
    }

    /**
     * Performs a generalized query.
     *
     * @param connection  the connection on which to perform the query
     * @param table       the table to query
     * @param constraints zero or more constraints to apply
     * @return the list of records; can be empty but never {@code null}
     */
    public List<Record> query(final AbstractGeneralConnection connection, final Table table,
                              final AbstractFieldConstraint<?>... constraints) {

        final List<Record> result = new ArrayList<>(10);

        if (connection instanceof final JdbcGeneralConnection jdbc) {

            // Generate the actual schema name
            final String schema = table.getSchema();
            final String actualSchema =
                    switch (connection.getContext()) {
                        case PRODUCTION -> schema;
                        case DEVELOPMENT -> schema.startsWith(TERM_PREFIX) ? TERM_DEV_SCHEMA
                                : SimpleBuilder.concat(schema, "_d");
                        case TESTING -> schema.startsWith(TERM_PREFIX) ? TERM_TEST_SCHEMA
                                : SimpleBuilder.concat(schema, "_t");
                    };

            // Create an SQL query
            final HtmlBuilder sql = new HtmlBuilder(100);
            final String tableName = table.getName();
            sql.add("SELECT * FROM ", actualSchema, ".", tableName);
            if (constraints != null && constraints.length > 0) {
                boolean first = true;
                for (final AbstractFieldConstraint<?> constraint : constraints) {
                    sql.add(first ? " WHERE " : " AND ");
                    appendWhereClause(sql, constraint);
                    first = false;
                }
            }
            final String sqlStr = sql.toString();

            // Execute the query and extract results into {@code Record} objects
            final Connection jdbcConn = jdbc.getConnection();
            try (final Statement stmt = jdbcConn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sqlStr)) {

                while (rs.next()) {
                    final Record rec = extractRecord(table, rs);
                    result.add(rec);
                }
            } catch (final SQLException ex) {
                Log.warning("Exception querying PostgreSQL database [", sql, "]", ex);
            }
        } else {
            Log.warning("Call to PostgreSQL implementation with a non-JDBC connection object.");
        }

        return result;
    }

    /**
     * Appends a where clause like "index = 4" or "name is not null" from a constraint.
     *
     * @param sql the {@code HtmlBuilder} to which to append
     * @param constraint the constraint
     */
    private static void appendWhereClause(final HtmlBuilder sql, final AbstractFieldConstraint<?> constraint) {

        final Field targetField = constraint.getField();
        final String fieldName = targetField.getName();

        if (constraint instanceof final ByteRangeConstraint byteRange) {

            final byte minValue = byteRange.getMinValue();
            final String minValueStr = Byte.toString(minValue);
            final byte maxValue = byteRange.getMaxValue();
            final String maxValueStr = Byte.toString(maxValue);
            sql.add(fieldName, ">=", minValueStr, " AND ", fieldName, "<=", maxValueStr);

        } else if (constraint instanceof final IntegerRangeConstraint integerRange) {

            final int minValue = integerRange.getMinValue();
            final String minValueStr = Integer.toString(minValue);
            final int maxValue = integerRange.getMaxValue();
            final String maxValueStr = Integer.toString(maxValue);
            sql.add(fieldName, ">=", minValueStr, " AND ", fieldName, "<=", maxValueStr);

        } else if (constraint instanceof final LongRangeConstraint longRange) {

            final long minValue = longRange.getMinValue();
            final String minValueStr = Long.toString(minValue);
            final long maxValue = longRange.getMaxValue();
            final String maxValueStr = Long.toString(maxValue);
            sql.add(fieldName, ">=", minValueStr, " AND ", fieldName, "<=", maxValueStr);

        } else if (constraint instanceof final FloatRangeConstraint floatRange) {

            final float minValue = floatRange.getMinValue();
            final String minValueStr = Float.toString(minValue);
            final float maxValue = floatRange.getMaxValue();
            final String maxValueStr = Float.toString(maxValue);
            sql.add(fieldName, ">=", minValueStr, " AND ", fieldName, "<=", maxValueStr);

            final EFloatingPointAllow allowed = floatRange.getAllowed();
            if (allowed == EFloatingPointAllow.FINITE_ONLY) {
                sql.add(" AND ISFINITE(", fieldName, ")");
            } else if (allowed == EFloatingPointAllow.ALL_BUT_INFINITIES) {
                sql.add(" AND (ISFINITE(", fieldName, ") OR ", fieldName, "='NaN')");
            } else if (allowed == EFloatingPointAllow.ALL_BUT_NAN) {
                sql.add(" AND ", fieldName, "!='NaN'");
            }

        } else if (constraint instanceof final DoubleRangeConstraint doubleRange) {

            final double minValue = doubleRange.getMinValue();
            final String minValueStr = Double.toString(minValue);
            final double maxValue = doubleRange.getMaxValue();
            final String maxValueStr = Double.toString(maxValue);
            sql.add(fieldName, ">=", minValueStr, " AND ", fieldName, "<=", maxValueStr);

            final EFloatingPointAllow allowed = doubleRange.getAllowed();
            if (allowed == EFloatingPointAllow.FINITE_ONLY) {
                sql.add(" AND ISFINITE(", fieldName, ")");
            } else if (allowed == EFloatingPointAllow.ALL_BUT_INFINITIES) {
                sql.add(" AND (ISFINITE(", fieldName, ") OR ", fieldName, "='NaN')");
            } else if (allowed == EFloatingPointAllow.ALL_BUT_NAN) {
                sql.add(" AND ", fieldName, "!='NaN'");
            }

        } else if (constraint instanceof final StringLengthConstraint stringLength) {

            final double minLength = stringLength.getMinLength();
            final String minLengthStr = Double.toString(minLength);
            final double maxLength = stringLength.getMaxLength();
            final String maxLengthStr = Double.toString(maxLength);
            sql.add("LENGTH(", fieldName, ")>=", minLengthStr, " AND LENGTH(", fieldName, ")<=", maxLengthStr);

        }
    }

    /**
     * Appends a where clause like "index = 4" or "name is not null" from a constraint.
     *
     * @param table the {@code Table} from which to get the record's field names and types
     * @param rs the {@code ResultSet} from which to retrieve field values
     */
    private static Record extractRecord(final Table table, final ResultSet rs) throws SQLException {

        final int numFields = table.getNumFields();
        final Object[] fieldValues = new Object[numFields];

        for (int i = 0; i < numFields; ++i) {
            final Field field = table.getField(i);
            final String name = field.getName();

            fieldValues[i] = switch (field.getType()) {
                case STRING -> rs.getString(name);
                case BOOLEAN -> {
                    final boolean value = rs.getBoolean(name);
                    yield rs.wasNull() ? null : Boolean.valueOf(value);
                }
                case BYTE -> {
                    final byte value = rs.getByte(name);
                    yield rs.wasNull() ? null : Byte.valueOf(value);
                }
                case INTEGER -> {
                    final int value = rs.getInt(name);
                    yield rs.wasNull() ? null : Integer.valueOf(value);
                }
                case LONG -> {
                    final long value = rs.getLong(name);
                    yield rs.wasNull() ? null : Long.valueOf(value);
                }
                case FLOAT -> {
                    final float value = rs.getFloat(name);
                    yield rs.wasNull() ? null : Float.valueOf(value);
                }
                case DOUBLE -> {
                    final double value = rs.getDouble(name);
                    yield rs.wasNull() ? null : Double.valueOf(value);
                }
                case BLOB -> rs.getBlob(name);
                case LOCAL_DATE -> {
                    final Date value = rs.getDate(name);
                    yield value == null ? null : value.toLocalDate();
                }
                case LOCAL_TIME -> {
                    final Time value = rs.getTime(name);
                    yield value == null ? null : value.toLocalTime();
                }
                case LOCAL_DATE_TIME -> {
                    final Timestamp value = rs.getTimestamp(name);
                    yield value == null ? null : value.toLocalDateTime();
                }
            };
        }

        try {
            return new Record(table, fieldValues);
        } catch (final IllegalArgumentException ex) {
            final String tableName = table.getName();
            final String msg = SimpleBuilder.concat("Failed to extract a record from the '", tableName, "' table");
            throw new SQLException(msg, ex);
        }
    }
}
