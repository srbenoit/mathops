package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawMilestone;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with 'milestone' records.
 *
 * <pre>
 * Table:  'milestone'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * term                 char(2)           no      PK
 * term_yr              smallint          no      PK
 * pace                 smallint          no
 * pace_track           char(2)           no      PK
 * ms_nbr               smallint          no      PK
 * ms_type              char(8)           no      PK
 * ms_date              date              no
 * nbr_atmpts_allow     smallint          yes
 * </pre>
 */
public final class RawMilestoneLogic extends AbstractRawLogic<RawMilestone> {

    /** A single instance. */
    public static final RawMilestoneLogic INSTANCE = new RawMilestoneLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawMilestoneLogic() {

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
    public boolean insert(final Cache cache, final RawMilestone record) throws SQLException {

        if (record.termKey == null || record.paceTrack == null || record.msNbr == null || record.msType == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO milestone (term,term_yr,pace,pace_track,ms_nbr,ms_type,ms_date,nbr_atmpts_allow)",
                " VALUES (",
                "'", record.termKey.termCode, "',",
                record.termKey.shortYear, ",",
                sqlIntegerValue(record.pace), ",",
                sqlStringValue(record.paceTrack), ",",
                record.msNbr, ",",
                sqlStringValue(record.msType), ",",
                sqlDateValue(record.msDate), ",",
                sqlIntegerValue(record.nbrAtmptsAllow), ")");

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
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
    @Override
    public boolean delete(final Cache cache, final RawMilestone record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM milestone ",
                "WHERE term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "  AND pace=", sqlIntegerValue(record.pace),
                "  AND pace_track=", sqlStringValue(record.paceTrack),
                "  AND ms_nbr=", sqlIntegerValue(record.msNbr),
                "  AND ms_type=", sqlStringValue(record.msType));

        try (final Statement stmt = cache.conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                cache.conn.commit();
            } else {
                cache.conn.rollback();
            }

            return result;
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public List<RawMilestone> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM milestone");
    }

    /**
     * Gets all milestone records.
     *
     * @param cache   the data cache
     * @param termKey the term key for which to return records
     * @return the list of milestone records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMilestone> getAllMilestones(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM milestone",
                " WHERE term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(termKey.shortYear));

        return executeListQuery(cache, sql);
    }

    /**
     * Gets all milestone records for a specified pace, ordered by milestone date.
     *
     * @param cache     the data cache
     * @param termKey   the term key for which to return records
     * @param pace      the pace for which to return records
     * @param paceTrack the pace ID for which to return records
     * @return the list of milestone records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawMilestone> getAllMilestones(final Cache cache, final TermKey termKey, final int pace,
                                                      final String paceTrack) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM milestone",
                " WHERE term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(termKey.shortYear),
                "   AND pace=", sqlIntegerValue(Integer.valueOf(pace)),
                "   AND pace_track=", sqlStringValue(paceTrack));

        return executeListQuery(cache, sql);
    }

//    /**
//     * Finds the date and number of attempts for a specific milestone for a specific student.
//     *
//     * @param pace         the pace
//     * @param paceTrack    the pace track
//     * @param msNbr        the milestone number
//     * @param msType       the milestone type
//     * @param milestones   the list of milestones for the term
//     * @param stmilestones the list of student milestones for a student in that term
//     * @return the result; {@code null} if no milestone was found
//     */
//    public static CheckMilestoneResult checkMilestone(final Integer pace, final String paceTrack,
//                                                      final Integer msNbr, final String msType,
//                                                      final Iterable<? extends RawMilestone> milestones,
//                                                      final Iterable<? extends RawStmilestone> stmilestones) {
//
//        LocalDate date = null;
//        Integer numTries = null;
//
//        for (final RawMilestone milestone : milestones) {
//            if (milestone.pace.equals(pace) && milestone.paceTrack.equals(paceTrack) && milestone.msNbr.equals(msNbr)
//                    && milestone.msType.equals(msType)) {
//                date = milestone.msDate;
//                numTries = milestone.nbrAtmptsAllow;
//            }
//        }
//
//        if (date != null) {
//            for (final RawStmilestone stmilestone : stmilestones) {
//                if (stmilestone.paceTrack.equals(paceTrack) && stmilestone.msNbr.equals(msNbr)
//                        && stmilestone.msType.equals(msType)) {
//                    date = stmilestone.msDate;
//                    numTries = stmilestone.nbrAtmptsAllow;
//                }
//            }
//        }
//
//        CheckMilestoneResult result = null;
//
//        if (date != null) {
//            result = new CheckMilestoneResult(date, numTries);
//        }
//
//        return result;
//    }

//    /**
//     * Finds the date and number of attempts for a specific milestone for a specific student. This starts with the
//     * 'milestone' record, then tests for an updated date in a 'stmilestone' record.
//     *
//     * @param cache     the data cache
//     * @param termKey   the term key
//     * @param pace      the pace
//     * @param paceTrack the pace track
//     * @param msNbr     the milestone number
//     * @param msType    the milestone type
//     * @param stuId     an optional student ID - if non-null, the 'stmilestone' table is tested for an override,
//     and the
//     *                  overridden values are returned
//     * @return the result; {@code null} if no milestone was found
//     * @throws SQLException if there is an error accessing the database
//     */
//    public static CheckMilestoneResult checkMilestone(final Cache cache, final TermKey termKey, final Integer pace,
//                                                      final String paceTrack, final Integer msNbr, final String
//                                                      msType,
//                                                      final String stuId) throws SQLException {
//
//        final HtmlBuilder sql = new HtmlBuilder(100);
//
//        sql.add("SELECT ms_date,nbr_atmpts_allow FROM milestone",
//                " WHERE term = '", termKey.termCode, "'",
//                "   AND term_yr = ", termKey.shortYear,
//                "   AND pace = ", pace,
//                "   AND pace_track = '", paceTrack, "'",
//                "   AND ms_nbr = ", msNbr,
//                "   AND ms_type = '", msType, "'");
//
//        LocalDate msDate = null;
//        Integer nbrAtmptsAllow = null;
//
//        try (final Statement stmt1 = cache.conn.createStatement();
//             final ResultSet rs = stmt1.executeQuery(sql.toString())) {
//
//            if (rs.next()) {
//                final Date dt = rs.getDate(1);
//                if (dt == null) {
//                    Log.warning("MILESTONE record has no ms_date field!",
//                            " (term=", termKey.termCode, ", term_yr=",
//                            termKey.shortYear, ", pace=", pace,
//                            ", pace_track=", paceTrack, ", ms_nbr=", msNbr,
//                            ", ms_type=", msType);
//                } else {
//                    msDate = dt.toLocalDate();
//                }
//
//                final int atmpts = rs.getInt(2);
//                if (!rs.wasNull()) {
//                    // NOTE: Null value here is expected
//                    nbrAtmptsAllow = Integer.valueOf(atmpts);
//                }
//            }
//        }
//        sql.reset();
//
//        if (stuId != null && msDate != null) {
//            sql.add("SELECT ms_date,nbr_atmpts_allow FROM stmilestone",
//                    " WHERE stu_id = '", stuId, "'",
//                    "   AND term = '", termKey.termCode, "'",
//                    "   AND term_yr = ", termKey.shortYear,
//                    "   AND pace_track = '", paceTrack, "'",
//                    "   AND ms_nbr = ", msNbr,
//                    "   AND ms_type = '", msType, "'");
//
//            try (final Statement stmt = cache.conn.createStatement();
//                 final ResultSet rs = stmt.executeQuery(sql.toString())) {
//
//                if (rs.next()) {
//                    final Date dt = rs.getDate(1);
//                    if (dt == null) {
//                        Log.warning("STMILESTONE record has no ms_date field!",
//                                " (term=", termKey.termCode, ", term_yr=",
//                                termKey.shortYear, ", pace_track=", paceTrack,
//                                ", ms_nbr=", msNbr, ", ms_type=", msType);
//                    } else {
//                        msDate = dt.toLocalDate();
//                    }
//
//                    final int atmpts = rs.getInt(2);
//                    if (!rs.wasNull()) {
//                        // NOTE: Null value here is expected
//                        nbrAtmptsAllow = Integer.valueOf(atmpts);
//                    }
//                }
//            }
//            sql.reset();
//        }
//
//        CheckMilestoneResult result = null;
//
//        if (msDate != null) {
//            result = new CheckMilestoneResult(msDate, nbrAtmptsAllow);
//        }
//
//        return result;
//    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawMilestone> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawMilestone> result = new ArrayList<>(10);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawMilestone.fromResultSet(rs));
            }
        }

        return result;
    }

//    /**
//     * The results of a check for milestone date/number of attempts.
//     */
//    public static final class CheckMilestoneResult {
//
//        /** The milestone date. */
//        public final LocalDate msDate;
//
//        /** The number of attempts allowed. */
//        public final Integer nbrAtmptsAllow;
//
//        /**
//         * Constructs a new {@code CheckMilestoneResult}.
//         *
//         * @param theMsDate         the milestone date
//         * @param theNbrAtmptsAllow the number of attempts allowed
//         */
//        CheckMilestoneResult(final LocalDate theMsDate, final Integer theNbrAtmptsAllow) {
//
//            this.msDate = theMsDate;
//            this.nbrAtmptsAllow = theNbrAtmptsAllow;
//        }
//    }
}
