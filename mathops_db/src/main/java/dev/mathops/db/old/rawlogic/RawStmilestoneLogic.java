package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "stmilestone" records.
 *
 * <pre>
 * Table:  'stmilestone'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * stu_id               char(9)           no      PK
 * term                 char(2)           no      PK
 * term_yr              smallint          no      PK
 * pace_track           char(2)           no      PK
 * ms_nbr               smallint          no      PK
 * ms_type              char(8)           no      PK
 * ms_date              date              no
 * nbr_atmpts_allow     smallint          yes
 * </pre>
 */
public enum RawStmilestoneLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStmilestone record) throws SQLException {

        if (record.stuId == null || record.termKey == null || record.paceTrack == null
                || record.msNbr == null || record.msType == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO stmilestone (",
                "stu_id,term,term_yr,pace_track,ms_nbr,ms_type,ms_date,nbr_atmpts_allow) VALUES (",
                LogicUtils.sqlStringValue(record.stuId), ",",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                record.termKey.shortYear, ",",
                LogicUtils.sqlStringValue(record.paceTrack), ",",
                record.msNbr, ",",
                LogicUtils.sqlStringValue(record.msType), ",",
                LogicUtils.sqlDateValue(record.msDate), ",",
                LogicUtils.sqlIntegerValue(record.nbrAtmptsAllow),")");

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
    public static boolean delete(final Cache cache, final RawStmilestone record) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("DELETE FROM stmilestone ",
                " WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "   AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "   AND pace_track=", LogicUtils.sqlStringValue(record.paceTrack),
                "   AND ms_nbr=", LogicUtils.sqlIntegerValue(record.msNbr),
                "   AND ms_type=", LogicUtils.sqlStringValue(record.msType));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql.toString()) == 1;

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
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmilestone> queryAll(final Cache cache) throws SQLException {

        final List<RawStmilestone> result = new ArrayList<>(500);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM stmilestone")) {

            while (rs.next()) {
                result.add(RawStmilestone.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Gets all records for a particular student.
     *
     * @param cache   the data cache
     * @param stuId   the student ID
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmilestone> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("SELECT * FROM stmilestone WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

        final List<RawStmilestone> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                result.add(RawStmilestone.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Gets all records for a particular student.
     *
     * @param cache   the data cache
     * @param termKey the term key
     * @param stuId   the student ID
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmilestone> getStudentMilestones(final Cache cache, final TermKey termKey,
                                                            final String stuId) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("SELECT * FROM stmilestone",
                " WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                "   AND term_yr=", termKey.shortYear,
                "   AND stu_id=", LogicUtils.sqlStringValue(stuId));

        final List<RawStmilestone> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                result.add(RawStmilestone.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Gets all records for a particular student in a particular pace track.
     *
     * @param cache     the data cache
     * @param termKey   the term key
     * @param paceTrack the pace track
     * @param stuId     the student ID
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmilestone> getStudentMilestones(final Cache cache, final TermKey termKey,
                                                            final String paceTrack, final String stuId)
            throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("SELECT * FROM stmilestone",
                " WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(termKey.shortYear),
                "   AND pace_track=", LogicUtils.sqlStringValue(paceTrack),
                "   AND stu_id=", LogicUtils.sqlStringValue(stuId));

        final List<RawStmilestone> result = new ArrayList<>(20);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                result.add(RawStmilestone.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Updates the milestone date and number of attempts allowed for a student milestone record.
     *
     * @param cache  the data cache
     * @param record the record to update
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean update(final Cache cache, final RawStmilestone record) throws SQLException {

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("UPDATE stmilestone SET ms_date=", LogicUtils.sqlDateValue(record.msDate),
                ", nbr_atmpts_allow=", LogicUtils.sqlIntegerValue(record.nbrAtmptsAllow),
                " WHERE stu_id=", LogicUtils.sqlStringValue(record.stuId),
                "   AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear),
                "   AND pace_track=", LogicUtils.sqlStringValue(record.paceTrack),
                "   AND ms_nbr=", LogicUtils.sqlIntegerValue(record.msNbr),
                "   AND ms_type=", LogicUtils.sqlStringValue(record.msType));

        Log.info(sql.toString());

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql.toString()) == 1;

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
}
