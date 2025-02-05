package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStresource;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stresource" records.
 *
 * <pre>
 * Table:  'stresource'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * resource_id          char(7)                   no      PK
 * loan_dt              date                      no      PK
 * start_time           integer                   no      PK
 * due_dt               date                      no
 * return_dt            date                      yes
 * finish_time          integer                   yes
 * times_display        smallint                  no
 * create_dt            date                      yes
 * </pre>
 */
public enum RawStresourceLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStresource record) throws SQLException {

        if (record.stuId == null || record.resourceId == null || record.loanDt == null || record.startTime == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        final String sql = SimpleBuilder.concat("INSERT INTO stresource ",
                "(stu_id,resource_id,loan_dt,start_time,due_dt,return_dt,finish_time,times_display,create_dt) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.resourceId), ",",
                LogicUtils.sqlDateValue(record.loanDt), ",",
                LogicUtils.sqlIntegerValue(record.startTime), ",",
                LogicUtils.sqlDateValue(record.dueDt), ",",
                LogicUtils.sqlDateValue(record.returnDt), ",",
                LogicUtils.sqlIntegerValue(record.finishTime), ",",
                LogicUtils.sqlIntegerValue(record.timesDisplay), ",",
                LogicUtils.sqlDateValue(record.createDt), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement s = conn.createStatement()) {
            result = s.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawStresource record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM stresource ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND resource_id=", LogicUtils.sqlStringValue(record.resourceId),
                "  AND loan_dt=", LogicUtils.sqlDateValue(record.loanDt),
                "  AND start_time=", LogicUtils.sqlIntegerValue(record.startTime));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStresource> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM stresource");
    }

    /**
     * Gets all resource loans for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of matching records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawStresource> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stresource",
                " WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

        return executeListQuery(cache, sql);
    }

    /**
     * Gets the record of an outstanding loan of a resource.
     *
     * @param cache      the data cache
     * @param resourceId the resource ID
     * @return the list of matching records
     * @throws SQLException if there is an error performing the query
     */
    public static RawStresource queryOutstanding(final Cache cache, final String resourceId) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stresource",
                " WHERE resource_id=", LogicUtils.sqlStringValue(resourceId),
                " AND return_dt IS NULL");

        return executeSingleQuery(cache, sql);
    }

    /**
     * Updates the return date and finish time.
     *
     * @param cache      the data cache
     * @param record     the record to update
     * @param returnDate the new return date
     * @param finishTime the new finish time
     * @return true if successful; false if not
     * @throws SQLException if there is an error performing the update
     */
    public static boolean updateReturnDateTime(final Cache cache, final RawStresource record,
                                               final LocalDate returnDate, final Integer finishTime)
            throws SQLException {

        final String sql = SimpleBuilder.concat("UPDATE stresource ",
                "SET return_dt=", LogicUtils.sqlDateValue(returnDate), ",",
                "    finish_time=", LogicUtils.sqlIntegerValue(finishTime),
                " WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "   AND resource_id=", LogicUtils.sqlStringValue(record.resourceId),
                "   AND loan_dt=", LogicUtils.sqlDateValue(record.loanDt),
                "   AND start_time=", LogicUtils.sqlIntegerValue(record.startTime));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStresource> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStresource> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStresource.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static RawStresource executeSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawStresource result = null;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawStresource.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
