package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawMpeLog;
import dev.mathops.text.builder.SimpleBuilder;

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
public enum RawMpeLogLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawMpeLog record) throws SQLException {

        final boolean result;

        if (record.stuId.startsWith("99")) {
            result = false;
        } else {
            final String sql = SimpleBuilder.concat(
                    "INSERT INTO mpe_log (stu_id,academic_yr,course,version,start_dt,",
                    "exam_dt,recover_dt,serial_nbr,start_time,calc_nbr) VALUES (",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlStringValue(record.academicYr), ",",
                    LogicUtils.sqlStringValue(record.course), ",",
                    LogicUtils.sqlStringValue(record.version), ",",
                    LogicUtils.sqlDateValue(record.startDt), ",",
                    LogicUtils.sqlDateValue(record.examDt), ",",
                    LogicUtils.sqlDateValue(record.recoverDt), ",",
                    LogicUtils.sqlLongValue(record.serialNbr), ",",
                    LogicUtils.sqlIntegerValue(record.startTime), ",",
                    LogicUtils.sqlStringValue(record.calcNbr), ")");

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
    public static boolean delete(final Cache cache, final RawMpeLog record) throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM mpe_log ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course),
                "  AND version=", LogicUtils.sqlStringValue(record.version),
                "  AND start_dt=", LogicUtils.sqlDateValue(record.startDt),
                "  AND start_time=", LogicUtils.sqlIntegerValue(record.startTime),
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
    public static List<RawMpeLog> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM mpe_log";

        final List<RawMpeLog> result = new ArrayList<>(500);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMpeLog.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
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
                    LogicUtils.sqlDateValue(examDt), ", recover_dt=",
                    LogicUtils.sqlDateValue(recoverDt), " WHERE stu_id=",
                    LogicUtils.sqlStringValue(stuId), " AND start_dt=",
                    LogicUtils.sqlDateValue(startDt), " AND start_time=",
                    LogicUtils.sqlIntegerValue(startTime));

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
}
