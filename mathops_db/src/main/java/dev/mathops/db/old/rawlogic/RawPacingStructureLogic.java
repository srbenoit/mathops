package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.svc.term.TermLogic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "pacing_structure" records.
 *
 * <pre>
 * Table:  'pacing_structure'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * pacing_structure     char(1)                   no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * def_pace_track       char(2)                   yes
 * require_licensed     char(1)                   no
 * require_partic       char(1)                   no
 * max_partic_missed    smallint                  no
 * allow_inc            char(1)                   no
 * max_courses          smallint                  no
 * nbr_open_allowed     smallint                  no
 * require_unit_exams   char(1)                   yes
 * use_midterms         char(1)                   yes
 * allow_coupons        char(1)                   yes
 * coupons_after_win+   char(1)                   yes
 * users_progress_cr    smallint                  yes
 * hw_progress_cr       smallint                  yes
 * re_progress_cr       smallint                  yes
 * ue_progress_cr       smallint                  yes
 * fin_progress_cr      smallint                  yes
 * pacing_name          char(30)                  yes
 * schedule_source      char(9)                   yes
 * sr_due_date_enfor+   char(1)                   yes
 * re_due_date_enfor+   char(1)                   yes
 * ue_due_date_enfor+   char(1)                   yes
 * fe_due_date_enfor+   char(1)                   yes
 * first_obj_avail      char(1)                   yes
 * </pre>
 */
public final class RawPacingStructureLogic extends AbstractRawLogic<RawPacingStructure> {

    /** A single instance. */
    public static final RawPacingStructureLogic INSTANCE = new RawPacingStructureLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawPacingStructureLogic() {

        super();
    }

    /**
     * Inserts a new pacing_structure record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final RawPacingStructure record)
            throws SQLException {

        if (record.termKey == null || record.pacingStructure == null || record.requirePartic == null
                || record.maxParticMissed == null || record.allowInc == null
                || record.maxCourses == null || record.nbrOpenAllowed == null) {

            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO pacing_structure (term,term_yr,pacing_structure,def_pace_track,require_licensed,",
                "require_partic,max_partic_missed,allow_inc,max_courses,nbr_open_allowed,require_unit_exams,",
                "use_midterms,allow_coupons,coupons_after_window,users_progress_cr,hw_progress_cr,re_progress_cr,",
                "ue_progress_cr,fin_progress_cr,pacing_name,schedule_source,sr_due_date_enforced,re_due_date_enforced,",
                "ue_due_date_enforced,fe_due_date_enforced,first_obj_avail) VALUES (",
                sqlStringValue(record.termKey.termCode), ",",
                record.termKey.shortYear, ",",
                sqlStringValue(record.pacingStructure), ",",
                sqlStringValue(record.defPaceTrack), ",",
                sqlStringValue(record.requireLicensed), ",",
                sqlStringValue(record.requirePartic), ",",
                sqlIntegerValue(record.maxParticMissed), ",",
                sqlStringValue(record.allowInc), ",",
                sqlIntegerValue(record.maxCourses), ",",
                sqlIntegerValue(record.nbrOpenAllowed), ",",
                sqlStringValue(record.requireUnitExams), ",",
                sqlStringValue(record.useMidterms), ",",
                sqlStringValue(record.allowCoupons), ",",
                sqlStringValue(record.couponsAfterWindow), ",",
                sqlIntegerValue(record.usersProgressCr), ",",
                sqlIntegerValue(record.hwProgressCr), ",",
                sqlIntegerValue(record.reProgressCr), ",",
                sqlIntegerValue(record.ueProgressCr), ",",
                sqlIntegerValue(record.finProgressCr), ",",
                sqlStringValue(record.pacingName), ",",
                sqlStringValue(record.scheduleSource), ",",
                sqlStringValue(record.srDueDateEnforced), ",",
                sqlStringValue(record.reDueDateEnforced), ",",
                sqlStringValue(record.ueDueDateEnforced), ",",
                sqlStringValue(record.feDueDateEnforced), ",",
                sqlStringValue(record.firstObjAvail), ")");

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
    public boolean delete(final Cache cache, final RawPacingStructure record)
            throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM pacing_structure ",
                "WHERE pacing_structure=", sqlStringValue(record.pacingStructure),
                "  AND term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear));

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
    public List<RawPacingStructure> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM pacing_structure");
    }

    /**
     * Retrieves all pacing structures for a particular term.
     *
     * @param cache   the data cache
     * @param termKey the key of the term for which to query
     * @return the corresponding list of records
     * @throws SQLException if there is an error performing the query
     */
    public static List<RawPacingStructure> queryByTerm(final Cache cache, final TermKey termKey)
            throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM pacing_structure",
                " WHERE term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", termKey.shortYear);

        return executeListQuery(cache, sql);
    }

    /**
     * Gets the pacing structure object that has a given term and pacing structure ID.
     *
     * @param cache           the data cache
     * @param termKey         the key of the term for which to query
     * @param pacingStructure the pacing structure ID for which to query
     * @return the matching records, null if none matched
     * @throws SQLException if there is an error performing the query
     */
    public static RawPacingStructure query(final Cache cache, final TermKey termKey,
                                           final String pacingStructure) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM pacing_structure",
                " WHERE pacing_structure=", sqlStringValue(pacingStructure),
                "   AND term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", termKey.shortYear);

        return executeSingleQuery(cache, sql);
    }

    /**
     * Gets a pacing structure within the active term.
     *
     * @param cache           the data cache
     * @param pacingStructure the ID of the pacing structure to retrieve
     * @return the pacing structure record; {@code null} if not found
     * @throws SQLException if there is an error accessing the database
     */
    public static RawPacingStructure query(final Cache cache, final String pacingStructure) throws SQLException {

        return query(cache, TermLogic.get(cache).queryActive(cache).term, pacingStructure);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawPacingStructure> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawPacingStructure> result = new ArrayList<>(20);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawPacingStructure.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Executes a query that returns a single records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the record found; null if none returned
     * @throws SQLException if there is an error accessing the database
     */
    private static RawPacingStructure executeSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawPacingStructure result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawPacingStructure.fromResultSet(rs);
            }
        }

        return result;
    }
}
