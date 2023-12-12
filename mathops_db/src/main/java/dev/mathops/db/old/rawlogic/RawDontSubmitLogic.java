package dev.mathops.db.old.rawlogic;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawDontSubmit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with dont_submit records.
 *
 * <pre>
 * Table:  'dont_submit'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * course               char(6)                   no      PK
 * sect                 char(4)                   no      PK
 * term                 char(2)                   no      PK
 * term_yr              smallint                  no      PK
 * </pre>
 */
public final class RawDontSubmitLogic extends AbstractRawLogic<RawDontSubmit> {

    /** A single instance. */
    public static final RawDontSubmitLogic INSTANCE = new RawDontSubmitLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawDontSubmitLogic() {

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
    public boolean insert(final Cache cache, final RawDontSubmit record) throws SQLException {

        if (record.course == null || record.sect == null || record.termKey == null) {
            throw new SQLException("Null value in primary key field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO dont_submit (course,sect,term,term_yr) VALUES (",
                "'", record.course, "',",
                "'", record.sect, "',",
                "'", record.termKey.termCode, "',",
                record.termKey.shortYear, ")");

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
    public boolean delete(final Cache cache, final RawDontSubmit record) throws SQLException {

        final boolean result;

        final HtmlBuilder sql = new HtmlBuilder(100);

        sql.add("DELETE FROM dont_submit ",
                " WHERE course=", sqlStringValue(record.course),
                " AND sect=", sqlStringValue(record.sect),
                " AND term=", sqlStringValue(record.termKey.termCode),
                " AND term_yr=", sqlIntegerValue(record.termKey.shortYear));

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
    public List<RawDontSubmit> queryAll(final Cache cache) throws SQLException {

        final List<RawDontSubmit> result = new ArrayList<>(50);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM dont_submit")) {

            while (rs.next()) {
                result.add(RawDontSubmit.fromResultSet(rs));
            }
        }

        return result;
    }
}
