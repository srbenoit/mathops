package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawMilestoneAppeal;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to manage milestone_appeal records.
 *
 * <pre>
 * Table:  'milestone_appeal'
 *
 * Column name          Type                       Nulls    Key
 * -------------------  -------------------------  -------  ---
 * stu_id               char(9)                    no       PK
 * term                 char(2)                    no       PK
 * term_yr              smallint                   no       PK
 * appeal_date_time     datetime year to second    no       PK
 * appeal_type          char(3)                    no       PK
 * pace                 smallint                   yes
 * pace_track           char(2)                    yes
 * ms_nbr               smallint                   yes
 * ms_type              char(8)                    yes
 * prior_ms_dt          date                       yes
 * new_ms_dt            date                       yes
 * attempts_allowed     smallint                   yes
 * circumstances        char(200)                  no
 * comment              char(200)                  yes
 * interviewer          char(20)                   no
 * </pre>
 */
public enum RawMilestoneAppealLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawMilestoneAppeal record) throws SQLException {

        // NOTE: This is a place where we send user-entered data into a table, so the insert is done
        //  with prepared statements.

        final boolean result;

        if (record.stuId.startsWith("99")) {
            result = false;
        } else {
            final String sql = "INSERT INTO milestone_appeal (stu_id,term,term_yr,appeal_date_time,appeal_type,pace,"
                               + "pace_track,ms_nbr,ms_type,prior_ms_dt,new_ms_dt,attempts_allowed,circumstances,"
                               + "comment,interviewer) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final PreparedStatement ps = conn.prepareStatement(sql)) {
                LogicUtils.setPsString(ps, 1, record.stuId);
                LogicUtils.setPsString(ps, 2, record.termKey.termCode);
                LogicUtils.setPsInteger(ps, 3, record.termKey.shortYear);
                LogicUtils.setPsTimestamp(ps, 4, record.appealDateTime);
                LogicUtils.setPsString(ps, 5, record.appealType);
                LogicUtils.setPsInteger(ps, 6, record.pace);
                LogicUtils.setPsString(ps, 7, record.paceTrack);
                LogicUtils.setPsInteger(ps, 8, record.msNbr);
                LogicUtils.setPsString(ps, 9, record.msType);
                LogicUtils.setPsDate(ps, 10, record.priorMsDt);
                LogicUtils.setPsDate(ps, 11, record.newMsDt);
                LogicUtils.setPsInteger(ps, 12, record.attemptsAllowed);
                LogicUtils.setPsString(ps, 13, record.circumstances);
                LogicUtils.setPsString(ps, 14, record.comment);
                LogicUtils.setPsString(ps, 15, record.interviewer);

                result = ps.executeUpdate() == 1;

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
    public static boolean delete(final Cache cache, final RawMilestoneAppeal record) throws SQLException {

        final boolean result;

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("DELETE FROM milestone_appeal",
                " WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "   AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "   AND appeal_date_time=", LogicUtils.sqlDateTimeValue(record.appealDateTime));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            result = stmt.executeUpdate(sql.toString()) == 1;

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
     * Updates a record.
     *
     * @param cache  the data cache
     * @param record the record to update
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean update(final Cache cache, final RawMilestoneAppeal record) throws SQLException {

        final boolean result;

        final HtmlBuilder sql = new HtmlBuilder(200);

        sql.add("UPDATE milestone_appeal",
                " SET pace=", LogicUtils.sqlIntegerValue(record.pace),
                ", pace_track=", LogicUtils.sqlStringValue(record.paceTrack),
                ", appeal_type=", LogicUtils.sqlStringValue(record.appealType),
                ", ms_nbr=", LogicUtils.sqlIntegerValue(record.msNbr),
                ", ms_type=", LogicUtils.sqlStringValue(record.msType),
                ", prior_ms_dt=", LogicUtils.sqlDateValue(record.priorMsDt),
                ", new_ms_dt=", LogicUtils.sqlDateValue(record.newMsDt),
                ", attempts_allowed=", LogicUtils.sqlIntegerValue(record.attemptsAllowed),
                ", circumstances=", LogicUtils.sqlStringValue(record.circumstances),
                ", comment=", LogicUtils.sqlStringValue(record.comment),
                ", interviewer=", LogicUtils.sqlStringValue(record.interviewer),
                " WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "   AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "   AND appeal_date_time=", LogicUtils.sqlDateTimeValue(record.appealDateTime));

        final String sqlString = sql.toString();

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final int count = stmt.executeUpdate(sqlString);

            if (count == 1) {
                result = true;
                conn.commit();
            } else {
                result = false;
                conn.rollback();
                Log.warning("Update to milestone_appeal indicated " + count + " rows would be changed - rolling back");
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Gets all milestone_appeal records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public List<RawMilestoneAppeal> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM milestone_appeal");
    }

    /**
     * Gets all milestone_appeal records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of pace_appeals records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMilestoneAppeal> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM milestone_appeal WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

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
    private static List<RawMilestoneAppeal> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawMilestoneAppeal> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMilestoneAppeal.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
