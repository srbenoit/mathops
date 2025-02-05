package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with mpecr_denied records.
 *
 * <pre>
 * Table:  'mpecr_denied'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * stu_id               char(9)           no      PK
 * course               char(6)           no      PK
 * exam_placed          char(1)           no
 * exam_dt              date              no
 * why_denied           char(2)           no
 * serial_nbr           integer           yes     PK
 * version              char(5)           yes
 * exam_source          char(2)           yes
 * </pre>
 */
public enum RawMpecrDeniedLogic {
    ;

    /** The field code to indicate denial by prerequisite failure. */
    public static final String DENIED_BY_PREREQ = "PQ";

    /** The field code to indicate denial by validation failure. */
    public static final String DENIED_BY_VAL = "V";

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawMpecrDenied record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "INSERT INTO mpecr_denied (stu_id,course,exam_placed,exam_dt,",
                    "why_denied,serial_nbr,version,exam_source) VALUES (",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlStringValue(record.course), ",",
                    LogicUtils.sqlStringValue(record.examPlaced), ",",
                    LogicUtils.sqlDateValue(record.examDt), ",",
                    LogicUtils.sqlStringValue(record.whyDenied), ",",
                    LogicUtils.sqlLongValue(record.serialNbr), ",",
                    LogicUtils.sqlStringValue(record.version), ",",
                    LogicUtils.sqlStringValue(record.examSource), ")");

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
    public static boolean delete(final Cache cache, final RawMpecrDenied record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM mpecr_denied ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course),
                "  AND exam_dt=", LogicUtils.sqlDateValue(record.examDt),
                "  AND serial_nbr=", LogicUtils.sqlLongValue(record.serialNbr));

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
    public static List<RawMpecrDenied> queryAll(final Cache cache) throws SQLException {

        final List<RawMpecrDenied> result = new ArrayList<>(500);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM mpecr_denied")) {

            while (rs.next()) {
                result.add(RawMpecrDenied.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Queries for all records for a student.
     *
     * @param cache the data cache
     * @param stuId the ID of the student to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMpecrDenied> queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM mpecr_denied",
                " WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

        final List<RawMpecrDenied> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpecrDenied.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Queries for all records for a single exam.
     *
     * @param cache     the data cache
     * @param serialNbr the serial number of the exam to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMpecrDenied> queryByExam(final Cache cache, final Long serialNbr) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM mpecr_denied",
                " WHERE serial_nbr=", LogicUtils.sqlLongValue(serialNbr));

        final List<RawMpecrDenied> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpecrDenied.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
