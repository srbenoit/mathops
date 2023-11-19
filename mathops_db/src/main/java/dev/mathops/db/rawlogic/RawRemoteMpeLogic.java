package dev.mathops.db.rawlogic;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawRemoteMpe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "remote_mpe" records.
 *
 * <pre>
 * Table:  'testing_centers'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * apln_term            char(4)                   no      PK
 * course               char(6)                   yes     PK
 * start_dt             date                      no      PK
 * end_dt               date                      no
 * </pre>
 */
public final class RawRemoteMpeLogic extends AbstractRawLogic<RawRemoteMpe> {

    /** A single instance. */
    public static final RawRemoteMpeLogic INSTANCE = new RawRemoteMpeLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawRemoteMpeLogic() {

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
    public boolean insert(final Cache cache, final RawRemoteMpe record) throws SQLException {

        if (record.termKey == null || record.aplnTerm == null || record.course == null
                || record.startDt == null || record.endDt == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat( //
                "INSERT INTO remote_mpe (",
                "term,term_yr,apln_term,course,start_dt,end_dt) VALUES (",
                sqlStringValue(record.termKey.termCode), ",",
                record.termKey.shortYear, ",",
                sqlTermValue(record.aplnTerm), ",",
                sqlStringValue(record.course), ",",
                sqlDateValue(record.startDt), ",",
                sqlDateValue(record.endDt), ")");

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
    public boolean delete(final Cache cache, final RawRemoteMpe record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM remote_mpe ",
                "WHERE term=", sqlStringValue(record.termKey.termCode),
                "  AND term_yr=", sqlIntegerValue(record.termKey.shortYear),
                "  AND apln_term=", sqlStringValue(record.aplnTerm.shortString),
                "  AND course=", sqlStringValue(record.course),
                "  AND start_dt=", sqlDateValue(record.startDt));

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
    public List<RawRemoteMpe> queryAll(final Cache cache) throws SQLException {

        final List<RawRemoteMpe> result = new ArrayList<>(20);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM remote_mpe")) {

            while (rs.next()) {
                result.add(RawRemoteMpe.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Queries all records with a specified application term.
     *
     * @param cache           the data cache
     * @param applicationTerm the application term
     * @return the complete set of records in the database
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawRemoteMpe> queryByApplicationTerm(final Cache cache,
                                                            final String applicationTerm) throws SQLException {

        final List<RawRemoteMpe> result = new ArrayList<>(10);

        final String sql = SimpleBuilder.concat("SELECT * FROM remote_mpe",
                " WHERE apln_term='", applicationTerm, "'");

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawRemoteMpe.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Queries all records with a specified course.
     *
     * @param cache  the data cache
     * @param course the course
     * @return the complete set of records in the database
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawRemoteMpe> queryByCourse(final Cache cache, final String course) throws SQLException {

        final List<RawRemoteMpe> result = new ArrayList<>(10);

        final String sql = SimpleBuilder.concat("SELECT * FROM remote_mpe",
                " WHERE course='", course, "'");

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawRemoteMpe.fromResultSet(rs));
            }
        }

        return result;
    }
}
