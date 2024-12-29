package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to look up pace_appeals by student, create new hold records, and delete hold records.
 *
 * <pre>
 * Table:  'pace_appeals'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * stu_id               char(9)           no      PK
 * term                 char(2)           no
 * term_yr              smallint          no
 * appeal_dt            date              no      PK
 * relief_given         char(1)           yes
 * pace                 smallint          no
 * pace_track           char(2)           yes
 * ms_nbr               smallint          no      PK
 * ms_type              char(8)           no      PK
 * ms_date              date              no
 * new_deadline_dt      date              yes
 * nbr_atmpts_allow     smallint          yes
 * circumstances        char(200)         no
 * comment              char(200)         yes
 * interviewer          char(20)          no
 * </pre>
 */
public final class RawPaceAppealsLogic extends AbstractRawLogic<RawPaceAppeals> {

    /** A single instance. */
    public static final RawPaceAppealsLogic INSTANCE = new RawPaceAppealsLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawPaceAppealsLogic() {

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
    public boolean insert(final Cache cache, final RawPaceAppeals record) throws SQLException {

        // NOTE: This is a place where we send user-entered data into a table, so the insert is done
        // with prepared statements.

        final boolean result;

        if (record.stuId.startsWith("99")) {
            result = false;
        } else {
            final String sql = "INSERT INTO pace_appeals (stu_id,term,term_yr,appeal_dt,relief_given,pace,pace_track,"
                               + "ms_nbr,ms_type,ms_date,new_deadline_dt,nbr_atmpts_allow,circumstances,comment," +
                               "interviewer"
                               + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (final PreparedStatement ps = cache.conn.prepareStatement(sql)) {
                setPsString(ps, 1, record.stuId);
                setPsString(ps, 2, record.termKey.termCode);
                setPsInteger(ps, 3, record.termKey.shortYear);
                setPsDate(ps, 4, record.appealDt);
                setPsString(ps, 5, record.reliefGiven);
                setPsInteger(ps, 6, record.pace);
                setPsString(ps, 7, record.paceTrack);
                setPsInteger(ps, 8, record.msNbr);
                setPsString(ps, 9, record.msType);
                setPsDate(ps, 10, record.msDate);
                setPsDate(ps, 11, record.newDeadlineDt);
                setPsInteger(ps, 12, record.nbrAtmptsAllow);
                setPsString(ps, 13, record.circumstances);
                setPsString(ps, 14, record.comment);
                setPsString(ps, 15, record.interviewer);

                result = ps.executeUpdate() == 1;

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
    public boolean delete(final Cache cache, final RawPaceAppeals record) throws SQLException {

        final boolean result;

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("DELETE FROM pace_appeals",
                " WHERE stu_id=", sqlStringValue(record.stuId),
                "   AND term=", sqlStringValue(record.termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "   AND appeal_dt=", sqlDateValue(record.appealDt),
                "   AND pace=", sqlIntegerValue(record.pace),
                "   AND pace_track=", sqlStringValue(record.paceTrack),
                "   AND ms_nbr=", sqlIntegerValue(record.msNbr),
                "   AND ms_type=", sqlStringValue(record.msType));

        try (final Statement stmt = cache.conn.createStatement()) {
            result = stmt.executeUpdate(sql.toString()) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }
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
    public static boolean update(final Cache cache, final RawPaceAppeals record) throws SQLException {

        final boolean result;

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("UPDATE pace_appeals",
                " SET relief_given=", sqlStringValue(record.reliefGiven),
                ", new_deadline_dt=", sqlDateValue(record.newDeadlineDt),
                ", nbr_atmpts_allow=", sqlIntegerValue(record.nbrAtmptsAllow),
                ", interviewer=", sqlStringValue(record.interviewer),
                ", circumstances=", sqlStringValue(record.circumstances),
                ", comment=", sqlStringValue(record.comment),
                " WHERE stu_id=", sqlStringValue(record.stuId),
                "   AND term=", sqlStringValue(record.termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "   AND appeal_dt=", sqlDateValue(record.appealDt),
                "   AND pace=", sqlIntegerValue(record.pace),
                "   AND pace_track=", sqlStringValue(record.paceTrack),
                "   AND ms_nbr=", sqlIntegerValue(record.msNbr),
                "   AND ms_type=", sqlStringValue(record.msType));

        final String sqlString = sql.toString();
//        Log.info(sqlString);

        try (final Statement stmt = cache.conn.createStatement()) {
            result = stmt.executeUpdate(sqlString) == 1;

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
    public List<RawPaceAppeals> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM pace_appeals");
    }

    /**
     * Gets all pace_appeals records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of pace_appeals records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawPaceAppeals> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM pace_appeals WHERE stu_id=", sqlStringValue(stuId));

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
    private static List<RawPaceAppeals> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawPaceAppeals> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawPaceAppeals.fromResultSet(rs));
            }
        }

        return result;
    }
}
