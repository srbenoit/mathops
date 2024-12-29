package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
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
public final class RawMpecrDeniedLogic extends AbstractRawLogic<RawMpecrDenied> {

    /** A single instance. */
    public static final RawMpecrDeniedLogic INSTANCE = new RawMpecrDeniedLogic();

    /** The field code to indicate denial by prerequisite failure. */
    public static final String DENIED_BY_PREREQ = "PQ";

    /** The field code to indicate denial by validation failure. */
    public static final String DENIED_BY_VAL = "V";

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawMpecrDeniedLogic() {

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
    public boolean insert(final Cache cache, final RawMpecrDenied record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "INSERT INTO mpecr_denied (stu_id,course,exam_placed,exam_dt,",
                    "why_denied,serial_nbr,version,exam_source) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.course), ",",
                    sqlStringValue(record.examPlaced), ",",
                    sqlDateValue(record.examDt), ",",
                    sqlStringValue(record.whyDenied), ",",
                    sqlLongValue(record.serialNbr), ",",
                    sqlStringValue(record.version), ",",
                    sqlStringValue(record.examSource), ")");

            try (final Statement stmt = cache.conn.createStatement()) {
                result = stmt.executeUpdate(sql) == 1;

                if (result) {
                    cache.conn.commit();
                } else {
                    cache.conn.rollback();
                }
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
    @Override
    public boolean delete(final Cache cache, final RawMpecrDenied record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM mpecr_denied ",
                "WHERE stu_id=", sqlStringValue(record.stuId),
                "  AND course=", sqlStringValue(record.course),
                "  AND exam_dt=", sqlDateValue(record.examDt),
                "  AND serial_nbr=", sqlLongValue(record.serialNbr));

        try (final Statement stmt = cache.conn.createStatement()) {
            result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
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
    @Override
    public List<RawMpecrDenied> queryAll(final Cache cache) throws SQLException {

        final List<RawMpecrDenied> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM mpecr_denied")) {

            while (rs.next()) {
                result.add(RawMpecrDenied.fromResultSet(rs));
            }
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
                " WHERE stu_id=", sqlStringValue(stuId));

        final List<RawMpecrDenied> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpecrDenied.fromResultSet(rs));
            }
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
                " WHERE serial_nbr=", sqlLongValue(serialNbr));

        final List<RawMpecrDenied> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpecrDenied.fromResultSet(rs));
            }
        }

        return result;
    }
}
