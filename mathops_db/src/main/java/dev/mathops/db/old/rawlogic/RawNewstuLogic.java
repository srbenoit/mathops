package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawNewstu;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with 'newstu' records.
 *
 * <pre>
 * Table:  'newstu'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * acad_level           char(2)                   no
 * reg_type             char(1)                   no
 * term                 char(6)                   no
 * </pre>
 */
public final class RawNewstuLogic extends AbstractRawLogic<RawNewstu> {

    /** A single instance. */
    public static final RawNewstuLogic INSTANCE = new RawNewstuLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawNewstuLogic() {

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
    public boolean insert(final Cache cache, final RawNewstu record) throws SQLException {

        if (record.stuId == null || record.acadLevel == null || record.regType == null || record.term == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO newstu (stu_id,acad_level,reg_type,term) VALUES (",
                sqlStringValue(record.stuId), ",",
                sqlStringValue(record.acadLevel), ",",
                sqlStringValue(record.regType), ",",
                sqlStringValue(record.term), ")");

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
    public boolean delete(final Cache cache, final RawNewstu record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM newstu ",
                "WHERE stu_id=", sqlStringValue(record.stuId));

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
    public List<RawNewstu> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM newstu";

        final List<RawNewstu> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawNewstu.fromResultSet(rs));
            }
        }

        return result;
    }

    /**
     * Deletes all records in the database.
     *
     * @param cache the data cache
     * @return the number of records deleted
     * @throws SQLException if there is an error accessing the database
     */
    public static int deleteAll(final Cache cache) throws SQLException {

        try (final Statement stmt = cache.conn.createStatement()) {
            final int result = stmt.executeUpdate("DELETE FROM newstu");
            cache.conn.commit();
            return result;
        }
    }
}
