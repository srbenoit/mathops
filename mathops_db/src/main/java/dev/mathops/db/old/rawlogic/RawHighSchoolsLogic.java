package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawHighSchools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with 'high_schools' records.
 *
 * <pre>
 * Table:  'high_schools'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * hs_code              char(6)           no      PK
 * hs_name              char(35)          no
 * addres_1             char(35)          yes
 * city                 char(18)          yes
 * state                char(2)           yes
 * zip_code             char(10)          yes
 * </pre>
 */
public final class RawHighSchoolsLogic extends AbstractRawLogic<RawHighSchools> {

    /** A single instance. */
    public static final RawHighSchoolsLogic INSTANCE = new RawHighSchoolsLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawHighSchoolsLogic() {

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
    public boolean insert(final Cache cache, final RawHighSchools record) throws SQLException {

        if (record.hsCode == null || record.hsName == null) {
            throw new SQLException("Null value in primary key field.");
        }

        // FIXME: This needs to be a prepared statement! Address or HS name could include apostrophes or SQL escapes.

        final String sql = SimpleBuilder.concat(
                "INSERT INTO high_schools (",
                "hs_code,hs_name,addres_1,city,state,zip_code) VALUES (",
                sqlStringValue(record.hsCode), ",",
                sqlStringValue(record.hsName), ",",
                sqlStringValue(record.addres1), ",",
                sqlStringValue(record.city), ",",
                sqlStringValue(record.state), ",",
                sqlStringValue(record.zipCode), ")");

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
    public boolean delete(final Cache cache, final RawHighSchools record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM high_schools ",
                "WHERE hs_code=", sqlStringValue(record.hsCode));

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
    public List<RawHighSchools> queryAll(final Cache cache) throws SQLException {

        final String sql = "SELECT * FROM high_schools";

        final List<RawHighSchools> result = new ArrayList<>(1000);

        try (final Statement stmt = cache.conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawHighSchools.fromResultSet(rs));
            }
        }

        return result;
    }
}
