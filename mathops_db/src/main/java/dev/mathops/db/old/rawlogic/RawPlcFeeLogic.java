package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawPlcFee;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "plc_fee" records.
 *
 * <pre>
 * Table:  'plc_fee'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                    no      PK
 * course               char(6)                    no      PK
 * exam_dt              date                       no
 * bill_dt              date                       no
 *
 * </pre>
 */
public final class RawPlcFeeLogic extends AbstractRawLogic<RawPlcFee> {

    /** A single instance. */
    public static final RawPlcFeeLogic INSTANCE = new RawPlcFeeLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawPlcFeeLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final RawPlcFee record) throws SQLException {

        if (record.stuId == null || record.course == null || record.examDt == null || record.billDt == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO plc_fee (stu_id,course,exam_dt,bill_dt) VALUES (",
                sqlStringValue(record.stuId), ",",
                sqlStringValue(record.course), ",",
                sqlDateValue(record.examDt), ",",
                sqlDateValue(record.billDt), ")");

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean delete(final Cache cache, final RawPlcFee record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM plc_fee ",
                "WHERE stu_id=", sqlStringValue(record.stuId),
                "  AND course=", sqlStringValue(record.course));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public List<RawPlcFee> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM plc_fee";

        final List<RawPlcFee> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawPlcFee.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Queries every record in the database.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the complete set of records in the database
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPlcFee queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        return executeSingleQuery(cache, "SELECT * FROM plc_fee WHERE stu_id=" + sqlStringValue(stuId));
    }

    /**
     * Queries the most recent date of a billing cycle.
     *
     * @param cache the data cache
     * @return the most recent bill date found
     * @throws SQLException if there is an error accessing the database
     */
    public static LocalDate queryMostRecentBillDate(final Cache cache) throws SQLException {

        final String sql = "SELECT MAX(bill_dt) FROM plc_fee";

        LocalDate result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                final Date tmp = rs.getDate(1);
                result = tmp == null ? null : tmp.toLocalDate();
            }
        }

        return result;
    }

    /**
     * Executes a query that returns a single record.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    private static RawPlcFee executeSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawPlcFee result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawPlcFee.fromResultSet(rs);
            }
        }

        return result;
    }
}
