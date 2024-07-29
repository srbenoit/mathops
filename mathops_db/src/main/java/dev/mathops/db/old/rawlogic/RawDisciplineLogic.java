package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawDiscipline;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with discipline records.
 *
 * <pre>
 * Table:  'discipline'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * stu_id               char(9)           no      PK
 * dt_incident          date              no      PK
 * incident_type        char(2)           no      PK
 * course               char(6)           no      PK
 * unit                 smallint          no      PK
 * cheat_desc           char(100)         yes
 * action_type          char(2)           yes
 * action_comment       char(100)         yes
 * interviewer          char(20)          yes
 * proctor              char(20)          yes
 * </pre>
 */
public final class RawDisciplineLogic extends AbstractRawLogic<RawDiscipline> {

    /** A single instance. */
    public static final RawDisciplineLogic INSTANCE = new RawDisciplineLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawDisciplineLogic() {

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
    public boolean insert(final Cache cache, final RawDiscipline record) throws SQLException {

        // NOTE: This is a place where we send user-entered data into a table, so the insert is done
        // with prepared statements.

        if (record.stuId == null || record.dtIncident == null || record.incidentType == null
                || record.course == null || record.unit == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        final String sql = "INSERT INTO discipline (stu_id,dt_incident,incident_type,course,unit,cheat_desc,"
                + "action_type,action_comment,interviewer,proctor) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (final PreparedStatement ps = cache.conn.prepareStatement(sql)) {
            setPsString(ps, 1, record.stuId);
            setPsDate(ps, 2, record.dtIncident);
            setPsString(ps, 3, record.incidentType);
            setPsString(ps, 4, record.course);
            setPsInteger(ps, 5, record.unit);
            setPsString(ps, 6, record.cheatDesc);
            setPsString(ps, 7, record.actionType);
            setPsString(ps, 8, record.actionComment);
            setPsString(ps, 9, record.interviewer);
            setPsString(ps, 10, record.proctor);

            result = ps.executeUpdate() == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
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
    public boolean delete(final Cache cache, final RawDiscipline record) throws SQLException {

        final boolean result;

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("DELETE FROM discipline ",
                " WHERE stu_id=", sqlStringValue(record.stuId),
                " AND dt_incident=", sqlDateValue(record.dtIncident),
                " AND incident_type=", sqlStringValue(record.incidentType),
                " AND course=", sqlStringValue(record.course),
                " AND unit=", sqlIntegerValue(record.unit));

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
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public List<RawDiscipline> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM discipline");
    }

    /**
     * Gets all discipline records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of discipline records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawDiscipline> queryByStudent(final Cache cache, final String stuId)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM discipline WHERE stu_id=", sqlStringValue(stuId));

        return executeQuery(cache, sql);
    }

    /**
     * Gets all discipline records for an action code.
     *
     * @param cache the data cache
     * @param action the action code
     * @return the list of discipline records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawDiscipline> queryByActionCode(final Cache cache, final String action)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM discipline WHERE action_type=", sqlStringValue(action));

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
    private static List<RawDiscipline> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawDiscipline> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawDiscipline.fromResultSet(rs));
            }
        }

        return result;
    }
}
