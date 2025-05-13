package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStpaceSummary;
import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stpace_summary" records.
 *
 * <pre>
 * Table:  'stpace_summary'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * course               char(6)                   no      PK
 * sect                 char(4)                   no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * i_in_progress        char(1)                   no
 * pace                 smallint                  no
 * pace_track           char(2)                   yes
 * pace_order           smallint                  no
 * ms_nbr               smallint                  no      PK
 * ms_unit              smallint                  no
 * ms_date              date                      no
 * new_ms_date          char(1)                   yes
 * exam_dt              date                      no
 * re_points            smallint                  no
 * </pre>
 */
public enum RawStpaceSummaryLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStpaceSummary record) throws SQLException {

        if (record.stuId == null || record.course == null || record.sect == null
            || record.termKey == null || record.iInProgress == null || record.pace == null
            || record.paceOrder == null || record.msNbr == null || record.msUnit == null
            || record.msDate == null || record.examDt == null || record.rePoints == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO stpace_summary (stu_id,course,sect,term,term_yr,",
                "i_in_progress,pace,pace_track,pace_order,ms_nbr,ms_unit,ms_date,",
                "new_ms_date,exam_dt,re_points) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlStringValue(record.sect), ",",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlStringValue(record.iInProgress), ",",
                LogicUtils.sqlIntegerValue(record.pace), ",",
                LogicUtils.sqlStringValue(record.paceTrack), ",",
                LogicUtils.sqlIntegerValue(record.paceOrder), ",",
                LogicUtils.sqlIntegerValue(record.msNbr), ",",
                LogicUtils.sqlIntegerValue(record.msUnit), ",",
                LogicUtils.sqlDateValue(record.msDate), ",",
                LogicUtils.sqlStringValue(record.newMsDate), ",",
                LogicUtils.sqlDateValue(record.examDt), ",",
                LogicUtils.sqlIntegerValue(record.rePoints), ")");

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
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawStpaceSummary record) throws SQLException {

        final boolean result;

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("DELETE FROM stpace_summary ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "  AND course=", LogicUtils.sqlStringValue(record.course),
                "  AND sect=", LogicUtils.sqlStringValue(record.sect),
                "  AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "  AND ms_nbr=", LogicUtils.sqlIntegerValue(record.msNbr));

        final String sql = builder.toString();

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
    public static List<RawStpaceSummary> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM stpace_summary";

        return executeQuery(cache, sql);
    }

    /**
     * Gets all records for a particular student, course, and term.
     *
     * @param cache   the data cache
     * @param stuId   the student ID
     * @param course  the course ID
     * @param sect    the section number
     * @param termKey the term key
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStpaceSummary> queryByStudentCourseSectionTerm(final Cache cache, final String stuId,
                                                                         final String course, final String sect,
                                                                         final TermKey termKey)
            throws SQLException {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("SELECT * FROM stpace_summary ",
                "WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                "  AND course=", LogicUtils.sqlStringValue(course),
                "  AND sect=", LogicUtils.sqlStringValue(sect),
                "  AND term=", LogicUtils.sqlStringValue(termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(termKey.shortYear));

        final String sql = builder.toString();

        return executeQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStpaceSummary> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStpaceSummary> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStpaceSummary.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
