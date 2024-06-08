package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawCunit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with cunit records.
 *
 * <pre>
 * Table:  'cunit'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * course               char(6)                   no      PK
 * unit                 smallint                  no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * unit_exam_wgt        decimal(3,2)              yes
 * unit_desc            char(50)                  yes
 * unit_timelimit       smallint                  yes
 * possible_score       smallint                  yes
 * nbr_questions        smallint                  yes
 * unit_type            char(4)                   yes
 * </pre>
 */
public final class RawCunitLogic extends AbstractRawLogic<RawCunit> {

    /** A single instance. */
    public static final RawCunitLogic INSTANCE = new RawCunitLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawCunitLogic() {

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
    public boolean insert(final Cache cache, final RawCunit record) throws SQLException {

        if (record.course == null || record.unit == null || record.termKey == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO cunit (course,unit,term,term_yr,unit_exam_wgt,unit_desc,unit_timelimit,possible_score,",
                "nbr_questions,unit_type) VALUES (",
                sqlStringValue(record.course), ",",
                record.unit, ",",
                sqlStringValue(record.termKey.termCode), ",",
                record.termKey.shortYear, ",",
                sqlFloatValue(record.unitExamWgt), ",",
                sqlStringValue(record.unitDesc), ",",
                sqlIntegerValue(record.unitTimelimit), ",",
                sqlIntegerValue(record.possibleScore), ",",
                sqlIntegerValue(record.nbrQuestions), ",",
                sqlStringValue(record.unitType), ")");

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
    public boolean delete(final Cache cache, final RawCunit record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM cunit ",
                "WHERE course=", sqlStringValue(record.course),
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
    public List<RawCunit> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM cunit");
    }

    /**
     * Retrieves all course units in a given term.
     *
     * @param cache   the data cache
     * @param termKey the term key
     * @return the list of course units; {@code null} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawCunit> queryByTerm(final Cache cache, final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM cunit WHERE term='", termKey.termCode,
                "' AND term_yr=", termKey.shortYear);

        return executeListQuery(cache, sql);
    }

    /**
     * Retrieves all course units in a specified course in a given term.
     *
     * @param cache   the data cache
     * @param course  the course
     * @param termKey the term key
     * @return the list of course units; {@code null} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawCunit> queryByCourse(final Cache cache, final String course,
                                               final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM cunit WHERE term='", termKey.termCode,
                "' AND term_yr=", termKey.shortYear, " AND course='",
                course, "'");

        return executeListQuery(cache, sql);
    }

    /**
     * Retrieves a particular course unit.
     *
     * @param cache   the data cache
     * @param course  the ID of the course to retrieve
     * @param unit    the unit number
     * @param termKey the term key
     * @return the corresponding unit; {@code null} on any error or if no course unit exists with the specified course
     *         ID and unit number in the specified term
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCunit query(final Cache cache, final String course, final Integer unit,
                                 final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM cunit WHERE course='", course,
                "' AND unit=", unit, " AND term='", termKey.termCode,
                "' AND term_yr=", termKey.shortYear);

        return executeSingleQuery(cache, sql);
    }

    /**
     * Retrieves the (single) final exam unit in a course in a given term.
     *
     * @param cache   the data cache
     * @param course  the course
     * @param termKey the term key
     * @return the final exam unit; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public static RawCunit getFinalUnit(final Cache cache, final String course,
                                        final TermKey termKey) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM cunit WHERE term='", termKey.termCode,
                "' AND term_yr=", termKey.shortYear, " AND course='",
                course, "' AND unit_type='FIN'");

        return executeSingleQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawCunit> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawCunit> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawCunit.fromResultSet(rs));
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
    private static RawCunit executeSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawCunit result = null;

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawCunit.fromResultSet(rs);
            }
        }

        return result;
    }
}
