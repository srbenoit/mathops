package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawrecord.RawZipCode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class to work with "zip_code" records.
 *
 * <pre>
 * Table:  'zip_code'
 *
 * Column name          Type              Nulls   Key
 * -------------------  ----------------  ------  -----
 * zip_code             char(10)          no      PK
 * city                 char(18)          no
 * state                char(2)           no
 * </pre>
 */
public final class RawZipCodeLogic extends AbstractRawLogic<RawZipCode> {

    /** A single instance. */
    public static final RawZipCodeLogic INSTANCE = new RawZipCodeLogic();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private RawZipCodeLogic() {

        super();
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean insert(final Cache cache, final RawZipCode record)
            throws SQLException {

        if (record.zipCode == null || record.city == null || record.state == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO zip_code ",
                "(zip_code,city,state) VALUES (",
                sqlStringValue(record.zipCode), ",",
                sqlStringValue(record.city), ",",
                sqlStringValue(record.state), ")");

        try (final Statement stmt = cache.conn.createStatement()) { //
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
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public boolean delete(final Cache cache, final RawZipCode record)
            throws SQLException {

        final boolean result;

        final String sql = SimpleBuilder.concat("DELETE FROM zip_code ",
                "WHERE zip_code=", sqlStringValue(record.zipCode));

        try (final Statement stmt = cache.conn.createStatement()) {
            result = stmt.executeUpdate(sql) == 1;

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
    public List<RawZipCode> queryAll(final Cache cache) throws SQLException {

        final List<RawZipCode> result = new ArrayList<>(500);

        try (final Statement stmt = cache.conn.createStatement(); //
             final ResultSet rs = stmt.executeQuery("SELECT * FROM zip_code")) {

            while (rs.next()) {
                result.add(RawZipCode.fromResultSet(rs));
            }
        }

        return result;
    }
}
