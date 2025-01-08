package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with cusection records.
 *
 * <pre>
 * Table:  'cusection'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * course               char(6)                   no      PK
 * sect                 char(4)                   no      PK
 * unit                 smallint                  no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * timeout              smallint                  no
 * re_mastery_score     smallint                  yes
 * ue_mastery_score     smallint                  yes
 * hw_mastery_score     smallint                  no
 * hw_moveon_score      smallint                  no
 * nbr_atmpts_allow     smallint                  no
 * atmpts_per_review    smallint                  no
 * first_test_dt        date                      no
 * last_test_dt         date                      no
 * begin_test_period    integer                   no
 * end_test_period      integer                   no
 * coupon_cost          smallint                  yes
 * last_coupon_dt       date                      yes
 * show_test_window     char(1)                   yes
 * unproctored_exam     char(1)                   yes
 * re_points_ontime     smallint                  yes
 * </pre>
 */
public enum RawCusectionLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawCusection record) throws SQLException {

        if (record.course == null || record.sect == null || record.termKey == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO cusection (course,sect,unit,term,term_yr,timeout,re_mastery_score,ue_mastery_score,",
                "hw_mastery_score,hw_moveon_score,nbr_atmpts_allow,atmpts_per_review,first_test_dt,last_test_dt,",
                "begin_test_period,end_test_period,coupon_cost,last_coupon_dt,show_test_window,unproctored_exam,",
                "re_points_ontime) VALUES (",
                LogicUtils.sqlStringValue(record.course), ",",
                LogicUtils.sqlStringValue(record.sect), ",",
                LogicUtils.sqlIntegerValue(record.unit), ",",
                LogicUtils.sqlStringValue(record.termKey.termCode), ",",
                LogicUtils.sqlIntegerValue(record.termKey.shortYear), ",",
                LogicUtils.sqlIntegerValue(record.timeout), ",",
                LogicUtils.sqlIntegerValue(record.reMasteryScore), ",",
                LogicUtils.sqlIntegerValue(record.ueMasteryScore), ",",
                LogicUtils.sqlIntegerValue(record.hwMasteryScore), ",",
                LogicUtils.sqlIntegerValue(record.hwMoveonScore), ",",
                LogicUtils.sqlIntegerValue(record.nbrAtmptsAllow), ",",
                LogicUtils.sqlIntegerValue(record.atmptsPerReview), ",",
                LogicUtils.sqlDateValue(record.firstTestDt), ",",
                LogicUtils.sqlDateValue(record.lastTestDt), ",",
                LogicUtils.sqlIntegerValue(record.beginTestPeriod), ",",
                LogicUtils.sqlIntegerValue(record.endTestPeriod), ",",
                LogicUtils.sqlIntegerValue(record.couponCost), ",",
                LogicUtils.sqlDateValue(record.lastCouponDt), ",",
                LogicUtils.sqlStringValue(record.showTestWindow), ",",
                LogicUtils.sqlStringValue(record.unproctoredExam), ",",
                LogicUtils.sqlIntegerValue(record.rePointsOntime), ")");

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
    public static boolean delete(final Cache cache, final RawCusection record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM cusection ",
                "WHERE course=", LogicUtils.sqlStringValue(record.course),
                "  AND sect=", LogicUtils.sqlStringValue(record.sect),
                "  AND unit=", LogicUtils.sqlIntegerValue(record.unit),
                "  AND term=", LogicUtils.sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", LogicUtils.sqlIntegerValue(record.termKey.shortYear));

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
    public static List<RawCusection> queryAll(final Cache cache) throws SQLException {

        final List<RawCusection> result = new ArrayList<>(100);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM cusection")) {

            while (rs.next()) {
                result.add(RawCusection.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Retrieves all cusection records for a particular term.
     *
     * @param cache   the data cache
     * @param termKey the term key
     * @return the list of all course section unit records found
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawCusection> queryByTerm(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM cusection",
                " WHERE term=", LogicUtils.sqlStringValue(termKey.termCode),
                "   AND term_yr=", LogicUtils.sqlIntegerValue(termKey.shortYear));

        final List<RawCusection> result = new ArrayList<>(10);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCusection.fromResultSet(rs));
            }
        }

        return result;
    }
}
