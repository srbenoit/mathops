package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawZipCode;
import dev.mathops.text.builder.SimpleBuilder;

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
public enum RawZipCodeLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawZipCode record) throws SQLException {

        if (record.zipCode == null || record.city == null || record.state == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat("INSERT INTO zip_code (zip_code,city,state) VALUES (",
                LogicUtils.sqlStringValue(record.zipCode), ",",
                LogicUtils.sqlStringValue(record.city), ",",
                LogicUtils.sqlStringValue(record.state), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
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
    public static boolean delete(final Cache cache, final RawZipCode record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM zip_code WHERE zip_code=",
                LogicUtils.sqlStringValue(record.zipCode));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawZipCode> queryAll(final Cache cache) throws SQLException {

        final List<RawZipCode> result = new ArrayList<>(500);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT * FROM zip_code")) {

            while (rs.next()) {
                result.add(RawZipCode.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
