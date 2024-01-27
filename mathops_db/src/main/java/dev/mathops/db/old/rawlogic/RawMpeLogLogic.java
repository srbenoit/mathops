package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawMpeLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with mpe_log records.
 *
 * <pre>
 * Table:  'mpe_log'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * stu_id               char(9)           no      PK
 * academic_yr          char(4)           yes
 * course               char(6)           no      PK
 * version              char(5)           no      PK
 * start_dt             date              no      PK
 * exam_dt              date              yes
 * recover_dt           date              yes
 * serial_nbr           integer           no
 * start_time           integer           no      PK
 * calc_nbr             char(4)           yes
 * </pre>
 */
public final class RawMpeLogLogic extends AbstractRawLogic<RawMpeLog> {

    /** A single instance. */
    public static final RawMpeLogLogic INSTANCE = new RawMpeLogLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawMpeLogLogic() {

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
    public boolean insert(final Cache cache, final RawMpeLog record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "INSERT INTO mpe_log (stu_id,academic_yr,course,version,start_dt,",
                    "exam_dt,recover_dt,serial_nbr,start_time,calc_nbr) VALUES (",
                    sqlStringValue(record.stuId), ",",
                    sqlStringValue(record.academicYr), ",",
                    sqlStringValue(record.course), ",",
                    sqlStringValue(record.version), ",",
                    sqlDateValue(record.startDt), ",",
                    sqlDateValue(record.examDt), ",",
                    sqlDateValue(record.recoverDt), ",",
                    sqlLongValue(record.serialNbr), ",",
                    sqlIntegerValue(record.startTime), ",",
                    sqlStringValue(record.calcNbr), ")");

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
    public boolean delete(final Cache cache, final RawMpeLog record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM mpe_log ",
                "WHERE stu_id=", sqlStringValue(record.stuId),
                "  AND course=", sqlStringValue(record.course),
                "  AND version=", sqlStringValue(record.version),
                "  AND start_dt=", sqlDateValue(record.startDt),
                "  AND start_time=", sqlIntegerValue(record.startTime),
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
    public List<RawMpeLog> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM mpe_log";

        final List<RawMpeLog> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpeLog.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Updates an {@code mpe_log} record to indicate the exam was finished. This updates the finish date and optionally
     * the recovered date.
     *
     * @param cache     the data cache
     * @param stuId     the student ID of the record to update
     * @param startDt   the start date of the record to update
     * @param startTime the start time of the record to update
     * @param examDt    the new exam (finish) date
     * @param recoverDt the new recovery date (null if the exam was not recovered)
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean indicateFinished(final Cache cache, final String stuId, final LocalDate startDt,
                                           final Integer startTime, final LocalDate examDt,
                                           final LocalDate recoverDt) throws SQLException {

        final boolean result;

        if (stuId.startsWith("99")) {
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "UPDATE mpe_log SET exam_dt=",
                    sqlDateValue(examDt), ", recover_dt=",
                    sqlDateValue(recoverDt), " WHERE stu_id=",
                    sqlStringValue(stuId), " AND start_dt=",
                    sqlDateValue(startDt), " AND start_time=",
                    sqlIntegerValue(startTime));

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
}
