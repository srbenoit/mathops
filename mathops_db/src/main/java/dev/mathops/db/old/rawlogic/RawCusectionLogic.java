package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

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
public final class RawCusectionLogic extends AbstractRawLogic<RawCusection> {

    /** A single instance. */
    public static final RawCusectionLogic INSTANCE = new RawCusectionLogic();

    /** Prefix for course numbers. */
    private static final String PREFIX = "M";

    /** Slash character. */
    private static final String SLASH = CoreConstants.SLASH;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawCusectionLogic() {

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
    public boolean insert(final Cache cache, final RawCusection record) throws SQLException {

        if (record.course == null || record.sect == null || record.termKey == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO cusection (course,sect,unit,term,term_yr,timeout,re_mastery_score,ue_mastery_score,",
                "hw_mastery_score,hw_moveon_score,nbr_atmpts_allow,atmpts_per_review,first_test_dt,last_test_dt,",
                "begin_test_period,end_test_period,coupon_cost,last_coupon_dt,show_test_window,unproctored_exam,",
                "re_points_ontime) VALUES (",
                sqlStringValue(record.course), ",",
                sqlStringValue(record.sect), ",",
                sqlIntegerValue(record.unit), ",",
                sqlStringValue(record.termKey.termCode), ",",
                sqlIntegerValue(record.termKey.shortYear), ",",
                sqlIntegerValue(record.timeout), ",",
                sqlIntegerValue(record.reMasteryScore), ",",
                sqlIntegerValue(record.ueMasteryScore), ",",
                sqlIntegerValue(record.hwMasteryScore), ",",
                sqlIntegerValue(record.hwMoveonScore), ",",
                sqlIntegerValue(record.nbrAtmptsAllow), ",",
                sqlIntegerValue(record.atmptsPerReview), ",",
                sqlDateValue(record.firstTestDt), ",",
                sqlDateValue(record.lastTestDt), ",",
                sqlIntegerValue(record.beginTestPeriod), ",",
                sqlIntegerValue(record.endTestPeriod), ",",
                sqlIntegerValue(record.couponCost), ",",
                sqlDateValue(record.lastCouponDt), ",",
                sqlStringValue(record.showTestWindow), ",",
                sqlStringValue(record.unproctoredExam), ",",
                sqlIntegerValue(record.rePointsOntime), ")");

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
    public boolean delete(final Cache cache, final RawCusection record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM cusection ",
                "WHERE course=", sqlStringValue(record.course),
                "  AND sect=", sqlStringValue(record.sect),
                "  AND unit=", sqlIntegerValue(record.unit),
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
    public List<RawCusection> queryAll(final Cache cache) throws SQLException {

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
                " WHERE term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(termKey.shortYear));

        final List<RawCusection> result = new ArrayList<>(10);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCusection.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Retrieves all cusection records for a particular course section.
     *
     * @param cache   the data cache
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param termKey the term key
     * @return the list of all course section unit records found
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawCusection> queryByCourseSection(final Cache cache, final String course,
                                                          final String sect, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM cusection",
                " WHERE course=", sqlStringValue(course),
                "   AND sect=", sqlStringValue(sect),
                "   AND term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(termKey.shortYear));

        final List<RawCusection> result = new ArrayList<>(10);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCusection.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Retrieves a particular course section unit.
     *
     * @param cache   the data cache
     * @param course  the ID of the course to retrieve
     * @param sect    the number of the section to retrieve
     * @param unit    the unit
     * @param termKey the term key
     * @return the course section unit record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCusection query(final Cache cache, final String course, final String sect,
                                     final Integer unit, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM cusection",
                " WHERE course=", sqlStringValue(course),
                "   AND sect=", sqlStringValue(sect),
                "   AND unit=", sqlIntegerValue(unit),
                "   AND term=", sqlStringValue(termKey.termCode),
                "   AND term_yr=", sqlIntegerValue(termKey.shortYear));

        RawCusection result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawCusection.fromResultSet(rs);
            }
        }

        return result;
    }

    /**
     * Given a mastery score, attempts to deduce the possible score (FIXME: this is a hardcode until the possible score
     * fields exist in the database).
     *
     * @param mastery the mastery score (could be null)
     * @return the deduced possible score (null if input is null)
     */
    public static Integer masteryToPossible(final Integer mastery) {

        Integer possible = null;

        if (mastery != null) {
            if (mastery.intValue() == 8 || mastery.intValue() == 7) {
                possible = Integer.valueOf(10);
            } else if (mastery.intValue() == 11 || mastery.intValue() == 12) {
                possible = Integer.valueOf(15);
            } else if (mastery.intValue() == 16 || mastery.intValue() == 14) {
                possible = Integer.valueOf(20);
            } else {
                possible = Integer.valueOf(mastery.intValue() * 10 / 8);
            }
        }

        return possible;
    }

    /**
     * Generate the topmatter associated with a record (FIXME: this is a hardcode until the possible score fields exist
     * in the database).
     *
     * @param record the record
     * @return the topmatter (null if none)
     */
    public static String getTopmatter(final RawCusection record) {

        String topmatter = null;

        if (RawRecordConstants.M100T.equals(record.course)) {
            topmatter = getM100tTopmatter(record.unit);
        } else if (RawRecordConstants.M117.equals(record.course)) {
            topmatter = getPrecalcTopmatter("117", record.unit);
        } else if (RawRecordConstants.M118.equals(record.course)) {
            topmatter = getPrecalcTopmatter("118", record.unit);
        } else if (RawRecordConstants.M124.equals(record.course)) {
            topmatter = getPrecalcTopmatter("124", record.unit);
        } else if (RawRecordConstants.M125.equals(record.course)) {
            topmatter = getPrecalcTopmatter("125", record.unit);
        } else if (RawRecordConstants.M126.equals(record.course)) {
            topmatter = getPrecalcTopmatter("126", record.unit);
        }

        return topmatter;
    }

    /**
     * Generates the topmatter value for the M 100T course (hardcoded here until the database supports the field).
     *
     * @param unit the unit number
     * @return the top matter
     */
    private static String getM100tTopmatter(final Integer unit) {

        String topmatter = null;

        final int unitValue = unit == null ? -1 : unit.intValue();

        if (unitValue == 1) {
            topmatter = setUnitVidProb("M100T/1MTU1.pdf");
        } else if (unitValue == 2) {
            topmatter = setUnitVidProb("M100T/1MTU2.pdf");
        } else if (unitValue == 3) {
            topmatter = setUnitVidProb("M100T/1MTU3.pdf");
        } else if (unitValue == 4) {
            topmatter = setUnitVidProb("M100T/1MTU4.pdf");
        }

        return topmatter;
    }

    /**
     * Generates the topmatter value for a Precalculus course (hardcoded here until the database supports the field).
     *
     * @param number the course number (117 through 126)
     * @param unit   the unit number
     * @return the top matter
     */
    private static String getPrecalcTopmatter(final String number, final Integer unit) {

        String topmatter = null;

        final int unitValue = unit == null ? -1 : unit.intValue();

        if (unitValue == 1) {
            topmatter = setUnitVidProb(PREFIX + number + SLASH + number + "U1.pdf");
        } else if (unitValue == 2) {
            topmatter = setUnitVidProb(PREFIX + number + SLASH + number + "U2.pdf");
        } else if (unitValue == 3) {
            topmatter = setUnitVidProb(PREFIX + number + SLASH + number + "U3.pdf");
        } else if (unitValue == 4) {
            topmatter = setUnitVidProb(PREFIX + number + SLASH + number + "U4.pdf");
        } else if (unitValue == 5) {
            topmatter = toComplete("MATH " + number);
        }

        return topmatter;
    }

    /**
     * Generates the topmatter to have a single PDF file link labeled "Unit Video Problems".
     *
     * @param file the file (a relative path below the /media directory on the streaming server)
     * @return the top matter
     */
    private static String setUnitVidProb(final String file) {

        final HtmlBuilder top = new HtmlBuilder(300);

        top.addln("<p class='indent33'><img src='/images/pdf.png' alt='' style='padding-right:3px;'/> ",
                "<a target='_blank' href='https://nibbler.math.colostate.edu/media/", file,
                "'>Unit Video Problems</a></p>");

        return top.toString();
    }

    /**
     * Generates the topmatter to instructions for completing the final exam in a resident course.
     *
     * @param course the course label
     * @return the top matter
     */
    private static String toComplete(final String course) {

        final HtmlBuilder top = new HtmlBuilder(300);

        top.div("clear");

        top.sP("indent33");
        top.addln(" To complete ", course, ", you must pass the <b>Final Exam</b> with a score of 16 or better.");
        top.eP();

        top.sP("indent33");
        top.addln(" The best way to review for the <b>Final Exam</b> is to  practice the <b>Review Exams</b>.");
        top.eP();

        return top.toString();
    }
}
